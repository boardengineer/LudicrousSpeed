package ludicrousspeed.simulator.commands;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class CardRewardSelectCommand implements Command {
    // Patch logic copied from com mod for now so that it doesn't need to be added as a dep.
    int cardIndex;
    public static boolean doHover = false;
    public static AbstractCard hoverCard;

    public CardRewardSelectCommand(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public CardRewardSelectCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardIndex = parsed.get("card_index").getAsInt();
    }

    @Override
    public void execute() {
        AbstractCard selectedCard = AbstractDungeon.cardRewardScreen.rewardGroup.get(cardIndex);

        CardRewardSelectCommand.doHover = true;
        CardRewardSelectCommand.hoverCard = selectedCard;
        selectedCard.hb.clicked = true;

        AbstractDungeon.cardRewardScreen.update();

        AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "CARD_REWARD_SELECT");
        cardCommandJson.addProperty("card_index", cardIndex);

        return cardCommandJson.toString();
    }

    @SpirePatch(
            clz = CardRewardScreen.class,
            method = "cardSelectUpdate"
    )
    public static class AcquireCardPatch {
        public AcquireCardPatch() {
        }

        @SpireInsertPatch(
                locator = AcquireCardPatch.Locator.class
        )
        public static void Insert(CardRewardScreen _instance) {
            CardRewardSelectCommand.doHover = false;
        }

        private static class Locator extends SpireInsertLocator {
            private Locator() {
            }

            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(CardRewardScreen.class, "skipButton");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList(), matcher);
            }
        }
    }

    @SpirePatch(
            clz = CardRewardScreen.class,
            method = "cardSelectUpdate"
    )
    public static class HoverCardPatch {
        public HoverCardPatch() {
        }

        @SpireInsertPatch(
                locator = CardRewardSelectCommand.HoverCardPatch.Locator.class,
                localvars = {"c"}
        )
        public static void Insert(CardRewardScreen _instance, AbstractCard c) {
            if (CardRewardSelectCommand.doHover) {
                if (c.equals(CardRewardSelectCommand.hoverCard)) {
                    CardRewardSelectCommand.hoverCard.hb.hovered = true;
                } else {
                    c.hb.hovered = false;
                }
            }

        }

        private static class Locator extends SpireInsertLocator {
            private Locator() {
            }

            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(AbstractCard.class, "updateHoverLogic");
                int[] match = LineFinder.findInOrder(ctMethodToPatch, new ArrayList(), matcher);
                int var10002 = match[0]++;
                return match;
            }
        }
    }

    @Override
    public String toString() {
        return "CardRewardSelectCommand" + cardIndex;
    }
}
