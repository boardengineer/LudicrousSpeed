package ludicrousspeed.simulator.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.AncientTeaSet;
import com.megacrit.cardcrawl.relics.BottledFlame;
import com.megacrit.cardcrawl.relics.GremlinHorn;
import com.megacrit.cardcrawl.vfx.RelicAboveCreatureEffect;
import ludicrousspeed.LudicrousSpeedMod;

import java.util.ArrayList;

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
}
