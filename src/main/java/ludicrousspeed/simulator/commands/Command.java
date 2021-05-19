package ludicrousspeed.simulator.commands;

public interface Command {
    void execute();

    String encode();
}