package ludicrousspeed.simulator.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.ApplyPowerToRandomEnemyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.RelicAboveCreatureEffect;
import ludicrousspeed.LudicrousSpeedMod;
import relicstats.patches.relics.TheSpecimenInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class RelicPatches {
    // Fast Mode doesn't load images which will NPE when trying to render, turn off rendering
    // in fast mode.
    @SpirePatch(
            clz = AbstractRelic.class,
            paramtypez = {SpriteBatch.class},
            method = "renderInTopPanel"
    )
    public static class NoRenderRelicsPatch {
        public static SpireReturn Prefix(AbstractRelic _instance, SpriteBatch sb) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }


    @SpirePatch(clz = AbstractRelic.class, method = "instantObtain", paramtypez = {AbstractPlayer.class, int.class, boolean.class})
    public static class InstantObtainPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> noDesc(AbstractRelic relic, AbstractPlayer p, int slot, boolean callOnEquip) {
            if (relic.relicId.equals("Circlet") && p != null && p.hasRelic("Circlet")) {
                AbstractRelic circ = p.getRelic("Circlet");
                ++circ.counter;
                circ.flash();
                relic.isDone = true;
                relic.isObtained = true;
                relic.discarded = true;
            } else {
                relic.isDone = true;
                relic.isObtained = true;
                if (slot >= p.relics.size()) {
                    p.relics.add(relic);
                } else {
                    p.relics.set(slot, relic);
                }

                float START_X = ReflectionHacks.getPrivateStatic(AbstractRelic.class, "START_X");
                float START_Y = ReflectionHacks.getPrivateStatic(AbstractRelic.class, "START_Y");

                relic.currentX = START_X + (float)slot * AbstractRelic.PAD_X;
                relic.currentY = START_Y;
                relic.targetX = relic.currentX;
                relic.targetY = relic.currentY;
                relic.hb.move(relic.currentX, relic.currentY);
                if (callOnEquip) {
                    relic.onEquip();
                    relic.relicTip();
                }

                UnlockTracker.markRelicAsSeen(relic.relicId);
                if (AbstractDungeon.topPanel != null) {
                    AbstractDungeon.topPanel.adjustRelicHbs();
                }
            }
            return SpireReturn.Return(null);
        }
    }

    // Turn off Image loading from the constructor, it's slow.
    @SpirePatch(
            clz = AbstractRelic.class,
            paramtypez = {String.class, String.class, AbstractRelic.RelicTier.class, AbstractRelic.LandingSound.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class FastRelicConstructorPatch {
        @SpireInsertPatch(loc = 127)
        public static SpireReturn Insert(AbstractRelic _instance, String setId, String imgName, AbstractRelic.RelicTier tier, AbstractRelic.LandingSound sfx) {
            if (LudicrousSpeedMod.plaidMode) {
                RelicStrings relicStrings = ReflectionHacks.getPrivate(_instance, AbstractRelic.class, "relicStrings");
                System.err.println(relicStrings.NAME);
                System.err.println(_instance.relicId);

                RelicStrings values = CardCrawlGame.languagePack.getRelicStrings(_instance.relicId);

                System.err.println("relic strings " + values.NAME + " " + values.FLAVOR + " " + Arrays.toString(values.DESCRIPTIONS));

                ReflectionHacks.setPrivateFinal(_instance, AbstractRelic.class, "name", relicStrings.NAME);
                _instance.tier = tier;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = DuVuDoll.class,
            paramtypez = {},
            method = "onMasterDeckChange"
    )
    public static class DuVuDollPatch {
        @SpirePrefixPatch
        public static SpireReturn Prefix(DuVuDoll doll) {
            if (LudicrousSpeedMod.plaidMode) {
                doll.counter = 0;

                AbstractDungeon.player.masterDeck.group.forEach(card -> {
                    if (card.type == AbstractCard.CardType.CURSE) {
                        doll.counter++;
                    }
                });

                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = RelicAboveCreatureEffect.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class DisableRenderAboveCreaturePatch {
        public static SpireReturn Prefix(RelicAboveCreatureEffect _instance, SpriteBatch sprites) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractRelic.class,
            paramtypez = {},
            method = "initializeTips"
    )
    public static class FastRelicInitializeTipsPatch {
        public static SpireReturn Prefix(AbstractRelic _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.tips = new ArrayList<>();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GremlinHorn.class,
            paramtypez = {AbstractPlayer.PlayerClass.class},
            method = "updateDescription"
    )
    public static class GremlinHornPatch {
        public static SpireReturn Prefix(GremlinHorn _instance, AbstractPlayer.PlayerClass c) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AncientTeaSet.class,
            paramtypez = {AbstractPlayer.PlayerClass.class},
            method = "updateDescription"
    )
    public static class TeasetDescriptionPatch {
        public static SpireReturn Prefix(AncientTeaSet _instance, AbstractPlayer.PlayerClass c) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    // TODO this is a hack to fix an NPE
    @SpirePatch(
            clz = BottledFlame.class,
            paramtypez = {},
            method = "setDescriptionAfterLoading"
    )
    public static class FixDescriptionNPE {
        public static void Replace(BottledFlame _instance) {

        }
    }

    @SpirePatch(clz = Omamori.class, method = "use")
    public static class UseOmaPatch {
        @SpirePrefixPatch
        public static SpireReturn noDescriptionUpdate(Omamori omamori) {
            if (LudicrousSpeedMod.plaidMode) {
                --omamori.counter;
                if (omamori.counter == 0) {
                    omamori.setCounter(0);
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = Omamori.class, method = "setCounter")
    public static class SetCounterOmaPatch {
        @SpirePrefixPatch
        public static SpireReturn noDescriptionUpdate(Omamori omamori, int setCounter) {
            if (LudicrousSpeedMod.plaidMode) {
                omamori.counter = setCounter;
                if (setCounter == 0) {
                    omamori.usedUp();
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = VelvetChoker.class, method = "canPlay")
    public static class VelvetChokerPatch {
        @SpirePrefixPatch
        public static SpireReturn noDescriptionUpdate(VelvetChoker velvetChoker, AbstractCard card) {
            if (LudicrousSpeedMod.plaidMode) {
                if(velvetChoker.counter >= 6) {
                    return SpireReturn.Return(false);
                }
                return SpireReturn.Return(true);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = ImageMaster.class, paramtypez = {String.class}, method = "loadImage")
    public static class noLoadImagesPatch {
        @SpirePrefixPatch
        public static SpireReturn noLoad(String imgUrl) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(ImageMaster.BOSS_CHEST_OPEN);
            }
//            System.err.println("sanity this is happening");
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = TheSpecimenInfo.ApplyPowerPatch.class, method = "postfix", optional = true, requiredModId = "RelicStats")
    public static class NoUpdateNullSpecimenPatch {
        @SpirePrefixPatch
        public static SpireReturn checkForNulls(ApplyPowerToRandomEnemyAction _instance) {
            if (_instance == null) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
