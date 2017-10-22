import java.util.List;
import java.util.Optional;
import processing.core.PImage;


final class Entity {
   private EntityKind kind;
   private String id;
   private Point position;
   private List<PImage> images;
   private int imageIndex;
   private int resourceLimit;
   private int resourceCount;
   private int actionPeriod;
   private int animationPeriod;

   public Entity(EntityKind kind, String id, Point position,
                 List<PImage> images, int resourceLimit, int resourceCount,
                 int actionPeriod, int animationPeriod) {
      this.kind = kind;
      this.id = id;
      this.position = position;
      this.images = images;
      this.imageIndex = 0;
      this.resourceLimit = resourceLimit;
      this.resourceCount = resourceCount;
      this.actionPeriod = actionPeriod;
      this.animationPeriod = animationPeriod;
   }

   public Point position() {
      return position;
   }

   public void setPosition(Point position){
      this.position = position;
   }

   public EntityKind kind() {
      return kind;
   }

   public int actionPeriod(){
      return this.actionPeriod;
   }

   public PImage getCurrentImage() {
      return (images.get(imageIndex));
   }

   public int getAnimationPeriod() {
      switch (kind) {
         case MINER_FULL:
         case MINER_NOT_FULL:
         case ORE_BLOB:
         case QUAKE:
            return animationPeriod;
         default:
            throw new UnsupportedOperationException(
                    String.format("getAnimationPeriod not supported for %s",
                            kind));
      }
   }

   public void nextImage() {
      imageIndex = (imageIndex + 1) % images.size();
   }

   public void executeMinerFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> fullTarget = Functions.findNearest(world, this.position,
              EntityKind.BLACKSMITH);

      if (fullTarget.isPresent() &&
              this.moveToFull(world, fullTarget.get(), scheduler))
      {
         this.transformFull(world, scheduler, imageStore);
      }
      else
      {
         scheduler.scheduleEvent(this,
                 new Activity(this, world, imageStore),
                 this.actionPeriod);
      }
   }

   public void executeMinerNotFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> notFullTarget = Functions.findNearest(world, this.position,
              EntityKind.ORE);

      if (!notFullTarget.isPresent() ||
              !this.moveToNotFull(world, notFullTarget.get(), scheduler) ||
              !this.transformNotFull(world, scheduler, imageStore))
      {
         scheduler.scheduleEvent(this,
                 new Activity(this, world, imageStore),
                 this.actionPeriod);
      }
   }

   public void executeOreActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Point pos = this.position;  // store current position before removing

      world.removeEntity(this);
      scheduler.unscheduleAllEvents(this);

      Entity blob = WorldModel.createOreBlob(this.id + Functions.BLOB_ID_SUFFIX,
              pos, this.actionPeriod / Functions.BLOB_PERIOD_SCALE,
              Functions.BLOB_ANIMATION_MIN +
                      Functions.rand.nextInt(Functions.BLOB_ANIMATION_MAX - Functions.BLOB_ANIMATION_MIN),
              imageStore.getImageList(Functions.BLOB_KEY));

      world.addEntity(blob);
      scheduler.scheduleActions(blob, world, imageStore);
   }

   public void executeOreBlobActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> blobTarget = Functions.findNearest(world,
              this.position, EntityKind.VEIN);
      long nextPeriod = this.actionPeriod;

      if (blobTarget.isPresent())
      {
         Point tgtPos = blobTarget.get().position;

         if (this.moveToOreBlob(world, blobTarget.get(), scheduler))
         {
            Entity quake = WorldModel.createQuake(tgtPos,
                    imageStore.getImageList(Functions.QUAKE_KEY));

            world.addEntity(quake);
            nextPeriod += this.actionPeriod;
            scheduler.scheduleActions(quake, world, imageStore);
         }
      }

      scheduler.scheduleEvent(this,
              new Activity(this, world, imageStore) {
              },
              nextPeriod);
   }

   public void executeQuakeActivity(WorldModel world,
                                           ImageStore imageStore, EventScheduler scheduler)
   {
      scheduler.unscheduleAllEvents(this);
      world.removeEntity(this);
   }

   public void executeVeinActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Point> openPt = world.findOpenAround(this.position);

      if (openPt.isPresent())
      {
         Entity ore = WorldModel.createOre(Functions.ORE_ID_PREFIX + this.id,
                 openPt.get(), Functions.ORE_CORRUPT_MIN +
                         Functions.rand.nextInt(Functions.ORE_CORRUPT_MAX - Functions.ORE_CORRUPT_MIN),
                 imageStore.getImageList(Functions.ORE_KEY));
         world.addEntity(ore);
         scheduler.scheduleActions(ore, world, imageStore);
      }

      scheduler.scheduleEvent(this,
              new Activity(this, world, imageStore),
              this.actionPeriod);
   }

   private boolean transformNotFull(WorldModel world, EventScheduler scheduler, ImageStore imageStore)
   {
      if (this.resourceCount >= this.resourceLimit)
      {
         Entity miner = WorldModel.createMinerFull(this.id, this.resourceLimit,
                 this.position, this.actionPeriod, this.animationPeriod,
                 this.images);

         world.removeEntity(this);
         scheduler.unscheduleAllEvents(this);

         world.addEntity(miner);
         scheduler.scheduleActions(miner, world, imageStore);

         return true;
      }

      return false;
   }

   private void transformFull(WorldModel world, EventScheduler scheduler, ImageStore imageStore)
   {
      Entity miner = WorldModel.createMinerNotFull(this.id, this.resourceLimit,
              this.position, this.actionPeriod, this.animationPeriod,
              this.images);

      world.removeEntity(this);
      scheduler.unscheduleAllEvents(this);

      world.addEntity(miner);
      scheduler.scheduleActions(miner, world, imageStore);
   }

   private Point nextPositionMiner(WorldModel world, Point destPos)
   {
      int horiz = Integer.signum(destPos.x() - this.position.x());
      Point newPos = new Point(this.position.x() + horiz,
              this.position.y());

      if (horiz == 0 || world.isOccupied(newPos))
      {
         int vert = Integer.signum(destPos.y() - this.position.y());
         newPos = new Point(this.position.x(),
                 this.position.y() + vert);

         if (vert == 0 || world.isOccupied(newPos))
         {
            newPos = this.position;
         }
      }

      return newPos;
   }

   private Point nextPositionOreBlob(WorldModel world, Point destPos)
   {
      int horiz = Integer.signum(destPos.x() - this.position.x());
      Point newPos = new Point(this.position.x() + horiz,
              this.position.y());

      Optional<Entity> occupant = world.getOccupant(newPos);

      if (horiz == 0 ||
              (occupant.isPresent() && !(occupant.get().kind == EntityKind.ORE)))
      {
         int vert = Integer.signum(destPos.y() - this.position.y());
         newPos = new Point(this.position.x(), this.position.y() + vert);
         occupant = world.getOccupant(newPos);

         if (vert == 0 ||
                 (occupant.isPresent() && !(occupant.get().kind == EntityKind.ORE)))
         {
            newPos = this.position;
         }
      }

      return newPos;
   }
/*
   public Action createAnimationAction(int repeatCount)
   {
      return new Action(ActionKind.ANIMATION, this, null, null, repeatCount);
   }

   public Action createActivityAction(WorldModel world, ImageStore imageStore)
   {
      return new Action(ActionKind.ACTIVITY, this, world, imageStore, 0);
   }
*/
   private boolean moveToNotFull(WorldModel world, Entity target, EventScheduler scheduler)
   {
      if (this.position.adjacent(target.position))
      {
         this.resourceCount += 1;
         world.removeEntity(target);
         scheduler.unscheduleAllEvents(target);

         return true;
      }
      else
      {
         Point nextPos = this.nextPositionMiner(world, target.position);

         if (!this.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            world.moveEntity(this, nextPos);
         }
         return false;
      }
   }

   private boolean moveToFull(WorldModel world, Entity target, EventScheduler scheduler)
   {
      if (this.position.adjacent(target.position))
      {
         return true;
      }
      else
      {
         Point nextPos = this.nextPositionMiner(world, target.position);

         if (!this.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            world.moveEntity(this, nextPos);
         }
         return false;
      }
   }

   private boolean moveToOreBlob(WorldModel world, Entity target, EventScheduler scheduler)
   {
      if (this.position.adjacent(target.position))
      {
         world.removeEntity(target);
         scheduler.unscheduleAllEvents(target);
         return true;
      }
      else
      {
         Point nextPos = this.nextPositionOreBlob(world, target.position);

         if (!this.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            world.moveEntity(this, nextPos);
         }
         return false;
      }
   }
}

