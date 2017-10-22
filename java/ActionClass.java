/*
final class ActionClass
{
   private ActionKind kind;
   private Entity entity;
   private WorldModel world;
   private ImageStore imageStore;
   private int repeatCount;

   public Action(ActionKind kind, Entity entity, WorldModel world,
      ImageStore imageStore, int repeatCount)
   {
      this.kind = kind;
      this.entity = entity;
      this.world = world;
      this.imageStore = imageStore;
      this.repeatCount = repeatCount;
   }

   public void executeAction(EventScheduler scheduler)
   {
      switch (kind)
      {
         case ACTIVITY:
            this.executeActivityAction(scheduler);
            break;

         case ANIMATION:
            this.executeAnimationAction(scheduler);
            break;
      }
   }
/*
   private void executeAnimationAction(EventScheduler scheduler)
   {
      entity.nextImage();

      if (repeatCount != 1)
      {
         scheduler.scheduleEvent(entity, createAnimationAction(entity, Math.max(repeatCount - 1, 0)), entity.getAnimationPeriod());
      }
   }
*/
/*
   private void executeActivityAction(EventScheduler scheduler)
   {
      switch (entity.kind())
      {
         case MINER_FULL:
            entity.executeMinerFullActivity(world, imageStore, scheduler);
            break;

         case MINER_NOT_FULL:
            entity.executeMinerNotFullActivity(world, imageStore, scheduler);
            break;

         case ORE:
            entity.executeOreActivity(world, imageStore, scheduler);
            break;

         case ORE_BLOB:
            entity.executeOreBlobActivity(world, imageStore, scheduler);
            break;

         case QUAKE:
            entity.executeQuakeActivity(world, imageStore, scheduler);
            break;

         case VEIN:
            entity.executeVeinActivity(world, imageStore, scheduler);
            break;

         default:
            throw new UnsupportedOperationException(
                    String.format("executeActivityAction not supported for %s",
                            entity.kind()));
      }
   }
*/
/*
   public static Action createAnimationAction(Entity entity, int repeatCount)
   {
      return new Action(ActionKind.ANIMATION, entity, null, null, repeatCount);
   }
*/
/*
   public static Action createActivityAction(Entity entity, WorldModel world,
                                             ImageStore imageStore)
   {
      return new Action(ActionKind.ACTIVITY, entity, world, imageStore, 0);
   }
*/

