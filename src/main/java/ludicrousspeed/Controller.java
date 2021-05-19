package ludicrousspeed;

public interface Controller {
    /**
     * Will be called whenever the game is waiting for a user action.  a step call should execute
     * at most one command.
     */
    void step();

    /**
     * while isDone returns false, the action simulator will block and step.
     */
    boolean isDone();
}
