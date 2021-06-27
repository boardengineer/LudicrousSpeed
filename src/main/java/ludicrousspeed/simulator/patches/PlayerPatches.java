package ludicrousspeed.simulator.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.LudicrousSpeedMod;

public class PlayerPatches {
    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {},
            method = "draw"
    )
    public static class NoSoundDrawPatch {
        public static void Replace(AbstractPlayer _instance) {
            if (_instance.hand.size() == 10) {
                _instance.createHandIsFullDialog();
            } else {
                _instance.draw(1);
                _instance.onCardDrawOrDiscard();
            }
        }
    }

    // THIS IS VOODOO, DON'T TOUCH IT
    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {int.class},
            method = "draw"
    )
    public static class NoSoundDrawPatch2 {
        @SpirePrefixPatch
        public static SpireReturn fastDraw(AbstractPlayer player, int numCards) {
            if (LudicrousSpeedMod.plaidMode) {
                for (int i = 0; i < numCards; ++i) {
                    if (!player.drawPile.isEmpty()) {
                        AbstractCard card = player.drawPile.getTopCard();

                        card.triggerWhenDrawn();
                        player.hand.addToHand(card);
                        player.drawPile.removeTopCard();

                        player.powers.forEach(power -> power.onCardDraw(card));
                        player.relics.forEach(relic -> relic.onCardDraw(card));
                    }
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "combatUpdate")
    public static class NoCombatUpdatePatch {
        @SpirePrefixPatch
        public static SpireReturn skipInFastMode(AbstractPlayer player) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "updateInput")
    public static class FastUpdateInputPatch {
        @SpirePrefixPatch
        public static SpireReturn skipInFastMode(AbstractPlayer player) {
            if (LudicrousSpeedMod.plaidMode) {
                if (!player.endTurnQueued) {
                    if (!AbstractDungeon.actionManager.turnHasEnded) {
                        return SpireReturn.Return(null);
                    }
                } else if (AbstractDungeon.actionManager.cardQueue
                        .isEmpty() && !AbstractDungeon.actionManager.hasControl &&
                        AbstractDungeon.actionManager.actions.isEmpty()) {
                    player.endTurnQueued = false;
                    player.isEndingTurn = true;
                }

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GainBlockAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class GainBlockActionFastPatch {
        @SpirePostfixPatch
        public static void insantUpdatePatch(GainBlockAction action) {
            if (LudicrousSpeedMod.plaidMode) {
                action.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = LoseEnergyAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class LoseEnergyActionFastPatch {
        @SpirePrefixPatch
        public static SpireReturn insantUpdatePatch(LoseEnergyAction action) {
            if (LudicrousSpeedMod.plaidMode) {
                int energyLoss = ReflectionHacks
                        .getPrivate(action, LoseEnergyAction.class, "energyLoss");
                AbstractDungeon.player.loseEnergy(energyLoss);

                action.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
