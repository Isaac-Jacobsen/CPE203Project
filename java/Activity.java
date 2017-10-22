public class Activity implements Action {
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    public Activity(Entity entity, WorldModel world, ImageStore imageStore) {
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = 0;
    }
    /*
    public static Action createActivityAction(Entity entity, WorldModel world,
                                              ImageStore imageStore)
    {
        return new Activity(entity, world, imageStore);
    }
    */
    public void executeAction(EventScheduler scheduler)
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

}
