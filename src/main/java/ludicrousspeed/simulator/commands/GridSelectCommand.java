package ludicrousspeed.simulator.commands;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.common.BetterDiscardPileToHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

public class GridSelectCommand implements Command {
    private final int cardIndex;
    private static boolean ignoreHoverLogic = false;

    public GridSelectCommand(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public GridSelectCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardIndex = parsed.get("card_index").getAsInt();
    }

    @Override
    public void execute() {
        AbstractDungeon.CurrentScreen screenBeforeCommand = AbstractDungeon.screen;
        AbstractDungeon.CurrentScreen previousScreenBeforeCommand = AbstractDungeon.previousScreen;

        if (AbstractDungeon.actionManager.currentAction instanceof BetterDiscardPileToHandAction) {
            if (AbstractDungeon.isScreenUp) {
                int numCards = ReflectionHacks
                        .getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "numCards");
                int cardSelectAmount = ReflectionHacks
                        .getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "cardSelectAmount");
            }
        }

        AbstractCard target = AbstractDungeon.gridSelectScreen.targetGroup.group.get(cardIndex);

        ReflectionHacks.setPrivate(
                AbstractDungeon.gridSelectScreen,
                GridCardSelectScreen.class,
                "hoveredCard",
                target);
        target.hb.hovered = true;
        target.hb.clicked = true;

        ignoreHoverLogic = true;
        AbstractDungeon.gridSelectScreen.update();
        ignoreHoverLogic = false;

        if (target.hb.clicked) {
            System.err.println("should have unclicked");
        }

        target.hb.clicked = false;
        target.hb.hovered = false;

        ReflectionHacks.setPrivate(
                AbstractDungeon.gridSelectScreen,
                GridCardSelectScreen.class,
                "hoveredCard",
                null);

        if (AbstractDungeon.actionManager.currentAction instanceof BetterDiscardPileToHandAction) {
            if (AbstractDungeon.isScreenUp) {
                int numCards = ReflectionHacks
                        .getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "numCards");
                int cardSelectAmount = ReflectionHacks
                        .getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "cardSelectAmount");

                System.err
                        .println("BetterDiscardPileToHandAction screen didn't close after pressing card " + numCards + " " + cardSelectAmount + " " + AbstractDungeon.gridSelectScreen.anyNumber + " " + AbstractDungeon.gridSelectScreen.forUpgrade + " " + AbstractDungeon.gridSelectScreen.forTransform + " " + AbstractDungeon.gridSelectScreen.forPurge + " " + AbstractDungeon.previousScreen + " " + AbstractDungeon.screen + " " + screenBeforeCommand + " " + previousScreenBeforeCommand);
            }
//            AbstractDungeon.closeCurrentScreen();
        }
//
//        if (AbstractDungeon.actionManager.currentAction instanceof DiscardPileToTopOfDeckAction) {
//            if (AbstractDungeon.isScreenUp) {
//                int numCards = ReflectionHacks
//                        .getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "numCards");
//                int cardSelectAmount = ReflectionHacks
//                        .getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "cardSelectAmount");
//
//                System.err
//                        .println("DiscardPileToTopOfDeckAction screen didn't close after pressing card " + numCards + " " + cardSelectAmount + " " + AbstractDungeon.gridSelectScreen.anyNumber + " " + AbstractDungeon.gridSelectScreen.forUpgrade + " " + AbstractDungeon.gridSelectScreen.forTransform + " " + AbstractDungeon.gridSelectScreen.forPurge + " " + AbstractDungeon.previousScreen + " " + AbstractDungeon.screen);
//            }
////            AbstractDungeon.closeCurrentScreen();
//        }
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

    // The Grid Select Screen checks to see where the cursor is at during update, disable
    // the check so we can fake whatever hovered card we want.
    @SpirePatch(clz = AbstractCard.class, method = "updateHoverLogic")
    public static class DisableHoverLogicPatch {
        @SpirePrefixPatch
        public static SpireReturn disableHover(AbstractCard card) {
            if (ignoreHoverLogic) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
