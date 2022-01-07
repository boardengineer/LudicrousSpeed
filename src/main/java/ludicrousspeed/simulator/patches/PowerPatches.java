package ludicrousspeed.simulator.patches;

import basemod.ReflectionHacks;
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
        public static SpireReturn Prefix(ApplyPowerAction _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                if (_instance.target != null && !_instance.target.isDeadOrEscaped()) {
                    AbstractPower powerToApply = ReflectionHacks
                            .getPrivate(_instance, ApplyPowerAction.class, "powerToApply");

                    if (powerToApply instanceof NoDrawPower && _instance.target
                            .hasPower(powerToApply.ID)) {
                        _instance.isDone = true;
                        return SpireReturn.Return(null);
                    }

                    if (_instance.source != null) {
                        for (AbstractPower power : _instance.source.powers) {
                            power.onApplyPower(powerToApply, _instance.target, _instance.source);
                        }
                    }

                    if (AbstractDungeon.player
                            .hasRelic("Champion Belt") && _instance.source != null && _instance.source.isPlayer && _instance.target != _instance.source && powerToApply.ID
                            .equals("Vulnerable") && !_instance.target.hasPower("Artifact")) {
                        AbstractDungeon.player.getRelic("Champion Belt")
                                              .onTrigger(_instance.target);
                    }

                    if (_instance.target instanceof AbstractMonster && _instance.target
                            .isDeadOrEscaped()) {
                        return SpireReturn.Return(null);
                    }

                    if (AbstractDungeon.player
                            .hasRelic("Ginger") && _instance.target.isPlayer && powerToApply.ID
                            .equals("Weakened")) {
                        return SpireReturn.Return(null);
                    }

                    if (AbstractDungeon.player
                            .hasRelic("Turnip") && _instance.target.isPlayer && powerToApply.ID
                            .equals("Frail")) {
                        return SpireReturn.Return(null);
                    }

                    if (_instance.target
                            .hasPower("Artifact") && powerToApply.type == AbstractPower.PowerType.DEBUFF) {
                        _instance.target.getPower("Artifact").onSpecificTrigger();
                        return SpireReturn.Return(null);
                    }

                    boolean hasBuffAlready = false;
                    for (AbstractPower power : _instance.target.powers) {
                        if (power.ID.equals(powerToApply.ID) && !power.ID.equals("Night Terror")) {
                            power.stackPower(_instance.amount);
                            hasBuffAlready = true;
                            AbstractDungeon.onModifyPower();
                        }
                    }

                    if (!hasBuffAlready) {
                        _instance.target.powers.add(powerToApply);
                        Collections.sort(_instance.target.powers);
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
            if(LudicrousSpeedMod.plaidMode) {
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
