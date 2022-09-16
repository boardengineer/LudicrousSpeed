package ludicrousspeed;

import basemod.interfaces.PreUpdateSubscriber;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;
import ludicrousspeed.simulator.ActionSimulator;

public class LudicrousSpeedMod implements PreUpdateSubscriber {
    /**
     * Set this controller to your own custom logic
     */
    public static Controller controller = null;

    /**
     * If true, the game's action manager will be replaced with a fast-action blocking loop.  The
     * only reliable way to interact with the game in this mode is via the controller
     */
    public static boolean plaidMode = false;

    public static boolean mustRestart = false;

    @Override
    public void receivePreUpdate() {
        if (AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom().monsters != null) {
            if (LudicrousSpeedMod.plaidMode) {
                ActionSimulator.actionLoop();
            } else if (shouldNormalUpdate()) {
                controller.step();
            }
        }
    }

    public static boolean shouldStep() {
        return shouldCheckForPlays() || isEndCommandAvailable() || ActionSimulator
                .shouldStepAiController();
    }

    private static boolean isInDungeon() {
        return CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY && AbstractDungeon
                .isPlayerInDungeon() && AbstractDungeon.currMapNode != null;
    }

    private static boolean shouldCheckForPlays() {
        return isInDungeon() && (AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.isScreenUp);
    }

    private static boolean isEndCommandAvailable() {
        return isInDungeon() && AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.isScreenUp;
    }

    private static boolean shouldNormalUpdate() {
        if (controller == null) {
            return false;
        }

        if (AbstractDungeon.actionManager == null || AbstractDungeon.player == null) {
            return false;
        }

        if (plaidMode) {
            return false;
        }

        // Wait for exhaust effects to finish so they don't come back to haunt us.
        for (AbstractGameEffect effect : AbstractDungeon.effectList) {
            if (effect instanceof ExhaustCardEffect) {
                return false;
            }
        }

        /**
         * If there are queued actions, only step is there is a selection screen up
         */
        if (AbstractDungeon.actionManager.turnHasEnded
                || (AbstractDungeon.actionManager.currentAction != null && AbstractDungeon.actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS)
                || !AbstractDungeon.actionManager.isEmpty()) {
            if (!(AbstractDungeon.isScreenUp
                    && (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD))) {
                return false;
            }
        }

        return shouldStep();
    }

    private static boolean playerUsingHandSelect() {
        return AbstractDungeon.isScreenUp && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT;
    }
}
