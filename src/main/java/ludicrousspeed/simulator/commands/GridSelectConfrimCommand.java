package ludicrousspeed.simulator.commands;

import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

public class GridSelectConfrimCommand implements Command {
    public static final GridSelectConfrimCommand INSTANCE = new GridSelectConfrimCommand();

    @Override
    public void execute() {
        GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
        screen.confirmButton.hb.clicked = true;
        screen.update();

        if (AbstractDungeon.isScreenUp) {
            System.err.println("screen didn't close after pressing confirm button");
        }
        AbstractDungeon.closeCurrentScreen();

    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "GRID_SELECT_CONFIRM");

        return cardCommandJson.toString();
    }

    @Override
    public String toString() {
        return "GridConfirm";
    }
}
