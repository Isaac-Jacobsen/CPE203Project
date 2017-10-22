public class Animation implements Action{
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    public Animation(Entity entity, int repeatCount)
    {
        this.entity = entity;
        this.world = null;
        this.imageStore = null;
        this.repeatCount = repeatCount;
    }

    public void executeAction(EventScheduler scheduler) {
        entity.nextImage();

        if (repeatCount != 1)
        {
            scheduler.scheduleEvent(entity,
                    new Animation(entity, Math.max(repeatCount - 1, 0)),
                    entity.getAnimationPeriod());
        }
    }
/*
    public static Action createAnimationAction(Entity entity, int repeatCount)
    {
        return new Animation(entity, repeatCount);
    }
*/
}
