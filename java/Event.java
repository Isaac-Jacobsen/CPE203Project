final class Event
{
   private Action action;
   private long time;
   private Entity entity;

   public Action action() {
      return action;
   }

   public long time() {
      return time;
   }

   public Entity entity() {
      return entity;
   }

   public Event(Action action, long time, Entity entity)
   {
      this.action = action;
      this.time = time;
      this.entity = entity;
   }
}
