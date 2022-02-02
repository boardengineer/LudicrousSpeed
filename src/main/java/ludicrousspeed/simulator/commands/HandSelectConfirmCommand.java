package ludicrousspeed.simulator.commands;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;

public class HandSelectConfirmCommand implements Command {
    public static final HandSelectConfirmCommand INSTANCE = new HandSelectConfirmCommand();

    private HandSelectConfirmCommand() {
    }

    @Override
    public void execute() {
        HandCardSelectScreen screen = AbstractDungeon.handCardSelectScreen;

        ReflectionHacks
                .setPrivate(AbstractDungeon.handCardSelectScreen, HandCardSelectScreen.class, "hand", AbstractDungeon.player.hand);

        screen.button.hb.clicked = true;
        screen.update();
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "HAND_SELECT_CONFIRM");

        return cardCommandJson.toString();
    }
}
