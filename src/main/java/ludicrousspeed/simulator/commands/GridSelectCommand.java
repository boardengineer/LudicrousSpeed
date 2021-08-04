package ludicrousspeed.simulator.commands;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

public class GridSelectCommand implements Command {
    private final int cardIndex;

    public GridSelectCommand(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public GridSelectCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardIndex = parsed.get("card_index").getAsInt();
    }

    @Override
    public void execute() {
        AbstractCard target = AbstractDungeon.gridSelectScreen.targetGroup.group.get(cardIndex);
        ReflectionHacks.setPrivate(
                AbstractDungeon.gridSelectScreen,
                GridCardSelectScreen.class,
                "hoveredCard",
                target);
        target.hb.clicked = true;

        AbstractDungeon.gridSelectScreen.update();
        AbstractDungeon.closeCurrentScreen();
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "GRID_SELECT");
        cardCommandJson.addProperty("card_index", cardIndex);


        return cardCommandJson.toString();
    }

    @Override
    public String toString() {
        return "GridSelectCommand" + cardIndex;
    }
}
