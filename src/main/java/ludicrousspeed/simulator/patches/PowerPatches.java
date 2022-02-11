package ludicrousspeed.simulator.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.OnReceivePowerPower;
import com.evacipated.cardcrawl.mod.stslib.relics.OnAnyPowerAppliedRelic;
import com.evacipated.cardcrawl.mod.stslib.relics.OnReceivePowerRelic;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.unique.RemoveDebuffsAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.AwakenedOne;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.NoDrawPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import ludicrousspeed.LudicrousSpeedMod;

import java.util.Collections;

public class PowerPatches {
    @SpirePatch(
            clz = AbstractPower.class,
            paramtypez = {int.class},
            method = "stackPower"
    )
    public static class FastRelicInitializeTipsPatch {
        public static SpireReturn Prefix(AbstractPower _instance, int amount) {
            if (LudicrousSpeedMod.plaidMode) {
                if (amount != -1) {
                    _instance.amount += amount;
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ApplyPowerAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastApplyPowerActionPatch {
        public static SpireReturn Prefix(ApplyPowerAction action) {
            if (LudicrousSpeedMod.plaidMode) {
                action.isDone = true;
                AbstractCreature target = action.target;
                AbstractCreature source = action.source;
                if (action.target != null && !target.isDeadOrEscaped()) {
                    AbstractPower powerToApply = ReflectionHacks
                            .getPrivate(action, ApplyPowerAction.class, "powerToApply");

                    if (powerToApply instanceof NoDrawPower && target
                            .hasPower(powerToApply.ID)) {
                        action.isDone = true;
                        return SpireReturn.Return(null);
                    }

                    if (action.source != null) {
                        for (AbstractPower power : source.powers) {
                            power.onApplyPower(powerToApply, target, source);
                        }
                    }


                    if (target != null) {
                        for (AbstractPower power : target.powers) {
                            if (power instanceof OnReceivePowerPower) {
                                // Allows changing the stackAmount
                                action.amount = ((OnReceivePowerPower) power).onReceivePowerStacks(powerToApply, target, source, action.amount);
                                // Allows negating the power
                                ((OnReceivePowerPower) power).onReceivePower(powerToApply, target, source);
                            }
                        }

                        if (target.isPlayer) {
                            for (AbstractRelic relic : AbstractDungeon.player.relics) {
                                if (relic instanceof OnReceivePowerRelic) {
                                    // Allows changing the stackAmount
                                    action.amount = ((OnReceivePowerRelic) relic).onReceivePowerStacks(powerToApply, source, action.amount);
                                    // Allows negating the power
                                    ((OnReceivePowerRelic) relic).onReceivePower(powerToApply, source);
                                }
                            }
                        }

                        for (AbstractRelic relic : AbstractDungeon.player.relics) {
                            if (relic instanceof OnAnyPowerAppliedRelic) {
                                // Allows changing the stackAmount
                                action.amount = ((OnAnyPowerAppliedRelic) relic).onAnyPowerApplyStacks(powerToApply, target, source, action.amount);
                                // Allows negating the power
                                ((OnAnyPowerAppliedRelic) relic).onAnyPowerApply(powerToApply, target, source);
                            }
                        }
                    }

                    if (AbstractDungeon.player
                            .hasRelic("Champion Belt") && action.source != null && action.source.isPlayer && action.target != action.source && powerToApply.ID
                            .equals("Vulnerable") && !action.target.hasPower("Artifact")) {
                        AbstractDungeon.player.getRelic("Champion Belt")
                                              .onTrigger(action.target);
                    }

                    if (action.target instanceof AbstractMonster && action.target
                            .isDeadOrEscaped()) {
                        return SpireReturn.Return(null);
                    }

                    if (AbstractDungeon.player
                            .hasRelic("Ginger") && action.target.isPlayer && powerToApply.ID
                            .equals("Weakened")) {
                        return SpireReturn.Return(null);
                    }

                    if (AbstractDungeon.player
                            .hasRelic("Turnip") && action.target.isPlayer && powerToApply.ID
                            .equals("Frail")) {
                        return SpireReturn.Return(null);
                    }

                    if (action.target
                            .hasPower("Artifact") && powerToApply.type == AbstractPower.PowerType.DEBUFF) {
                        action.target.getPower("Artifact").onSpecificTrigger();
                        return SpireReturn.Return(null);
                    }

                    boolean hasBuffAlready = false;
                    for (AbstractPower power : action.target.powers) {
                        if (power.ID.equals(powerToApply.ID) && !power.ID.equals("Night Terror")) {
                            power.stackPower(action.amount);
                            hasBuffAlready = true;
                            AbstractDungeon.onModifyPower();
                        }
                    }

                    if (!hasBuffAlready) {
                        action.target.powers.add(powerToApply);
                        Collections.sort(action.target.powers);
                        powerToApply.onInitialApplication();

                        AbstractDungeon.onModifyPower();
                    }

                }

                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = RemoveSpecificPowerAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastRemovePowerPatch {
        public static void Prefix(RemoveSpecificPowerAction _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
            }
        }

        public static void Postfix(RemoveSpecificPowerAction _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(clz = AbstractPower.class, method = "flash")
    public static class NoFlashClass {
        @SpirePrefixPatch
        public static SpireReturn noFlash(AbstractPower power) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    // Some powers, such as strength, chat from buff to debuffs when their counters are negative,
    // make sure to call updateDescription so they're purged properly.
    @SpirePatch(clz = RemoveDebuffsAction.class, method = "update")
    public static class updateDescriptionForRemovePatch {
        @SpirePrefixPatch
        public static void updateDescription(RemoveDebuffsAction action) {
            if (LudicrousSpeedMod.plaidMode) {
                AbstractCreature c = ReflectionHacks
                        .getPrivate(action, RemoveDebuffsAction.class, "c");

                c.powers.forEach(p -> p.updateDescription());
            }
        }
    }

    @SpirePatch(clz = AwakenedOne.class, method = "damage")
    public static class updateDescriptionForRemoveAwakenedOneDebuffsPatch {
        @SpirePrefixPatch
        public static void updateDescription(AwakenedOne awakenedOne, DamageInfo info) {
            if (LudicrousSpeedMod.plaidMode) {
                awakenedOne.powers.forEach(p -> p.updateDescription());
            }
        }
    }
}
