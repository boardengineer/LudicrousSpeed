package ludicrousspeed.simulator.patches;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.audio.SoundMaster;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.desktop.DesktopLauncher;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.TimeWarpPower;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.ShockWaveEffect;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;
import com.megacrit.cardcrawl.vfx.combat.StrikeEffect;
import ludicrousspeed.LudicrousSpeedMod;

public class FXPatches {
    @SpirePatch(
            clz = VFXAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoFxUpdatePatch {
        public static SpireReturn Prefix(VFXAction _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = VFXAction.class,
            paramtypez = {AbstractCreature.class, AbstractGameEffect.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoFxConstructorPatch {
        public static SpireReturn Prefix(VFXAction _instance, AbstractCreature source, AbstractGameEffect effect, float duration) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = VFXAction.class,
            paramtypez = {AbstractCreature.class, AbstractGameEffect.class, float.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoFxConstructorPatchOther {
        public static SpireReturn Prefix(VFXAction _instance, AbstractCreature source, AbstractGameEffect effect, float duration, boolean topLevel) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SmallLaserEffect.class,
            paramtypez = {float.class, float.class, float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoLaserFxConstructorPatch {
        public static SpireReturn Prefix(SmallLaserEffect _instance, float sX, float sY, float dX, float dY) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SmallLaserEffect.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoLaserFxUpdatePatch {
        public static SpireReturn Prefix(SmallLaserEffect _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SmallLaserEffect.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class NoLaserFxRenderPatch {
        public static SpireReturn Prefix(SmallLaserEffect _instance, SpriteBatch sb) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ShockWaveEffect.class,
            paramtypez = {float.class, float.class, Color.class, ShockWaveEffect.ShockWaveType.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoShockwaveFxConstructorPatch {
        public static SpireReturn Prefix(ShockWaveEffect _instance, float x, float y, Color color, ShockWaveEffect.ShockWaveType type) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ShockWaveEffect.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoShockwaveFxUpdatePatch {
        public static SpireReturn Prefix(ShockWaveEffect _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = StrikeEffect.class,
            paramtypez = {AbstractCreature.class, float.class, float.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class TooManyLinesPatch {
        public static SpireReturn Prefix(StrikeEffect _instance, AbstractCreature target, float x, float y, int number) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SoundMaster.class,
            paramtypez = {String.class, boolean.class},
            method = "play"
    )
    public static class NoPlaySoundPatch {
        @SpirePrefixPatch
        public static SpireReturn<Long> Prefix(SoundMaster _instance, String key, boolean useBgmVolume) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(1L);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SoundMaster.class,
            paramtypez = {String.class},
            method = "play"
    )
    public static class NoPlaySoundPatch2 {
        @SpirePrefixPatch
        public static SpireReturn<Long> noSoundInPlaidMode(SoundMaster _instance, String key) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(1L);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SoundMaster.class,
            paramtypez = {String.class, float.class},
            method = "play"
    )
    public static class NoPlaySoundPatch3 {
        @SpirePrefixPatch
        public static SpireReturn<Long> noSoundInPlaidMode(SoundMaster _instance, String key, float pitchVariation) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(1L);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GLTexture.class,
            method = "setFilter"
    )
    public static class NoTexturePatch {
        public static SpireReturn Prefix(GLTexture _instance, Texture.TextureFilter minFilter, Texture.TextureFilter maxFilter) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = TimeWarpPower.class,
            method = "playApplyPowerSfx"
    )
    public static class NoPlaySoundInTimeWarpPatch {
        @SpirePrefixPatch
        public static SpireReturn Prefix(TimeWarpPower _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }


    @SpirePatch(
            clz = LocalizedStrings.class,
            method = "getRelicStrings"
    )
    public static class NoLocalRelicStringsPatch {
        public static SpireReturn Prefix(LocalizedStrings _instance, String relicName) {
//            if (LudicrousSpeedMod.plaidMode) {
//                return SpireReturn.Return(new RelicStrings());
//            }
            return SpireReturn.Continue();
        }
    }

    public static final CardStrings MOCK_CARD_STRINGS = CardStrings.getMockCardString();

    @SpirePatch(
            clz = LocalizedStrings.class,
            method = "getCardStrings"
    )
    public static class NoLocalCardStringsPatch {
        public static SpireReturn<CardStrings> Prefix(LocalizedStrings _instance, String cardName) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(MOCK_CARD_STRINGS);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = basemod.patches.com.megacrit.cardcrawl.core.CardCrawlGame.ApplyScreenPostProcessor.class,
            method = "BeforeSpriteBatchBegin"
    )
    public static class Makeitstoppatch {
        public static SpireReturn<Void> Prefix() {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = FontHelper.class,
            method = "colorString"
    )
    public static class NoFontHelperColorStringPatch {
        public static SpireReturn Prefix(String input, String colorValue) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return("");
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = DamageAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceDamageActionPatch {
        public static void Prefix(DamageAction _instance) {
            _instance.isDone = true;
        }
    }

    private RelicStrings getMockRelicStrings() {
        RelicStrings result = new RelicStrings();

        result.DESCRIPTIONS = new String[]{"[MISSING_0]", "[MISSING_1]", "[MISSING_2]", "[MISSING_3]"};
        return result;
    }


    @SpirePatch(clz = AbstractCreature.class, method = "decrementBlock")
    public static class NoFXOnDecrementBlockPatch {
        @SpirePrefixPatch
        public static SpireReturn NoFx(AbstractCreature creature, DamageInfo info, int tempDamageAmount) {
            if (LudicrousSpeedMod.plaidMode) {
                int damageAmount = tempDamageAmount;

                if (info.type != DamageInfo.DamageType.HP_LOSS && creature.currentBlock > 0) {
                    if (damageAmount > creature.currentBlock) {
                        damageAmount -= creature.currentBlock;

                        creature.loseBlock();

                        // Just reimplement in place
                        // creature.brokeBlock();
                        if (creature instanceof AbstractMonster) {
                            AbstractDungeon.player.relics
                                    .forEach(relic -> relic.onBlockBroken(creature));
                        }

                    } else if (damageAmount == creature.currentBlock) {
                        damageAmount = 0;
                        creature.loseBlock();

                        //this.brokeBlock();
                        if (creature instanceof AbstractMonster) {
                            AbstractDungeon.player.relics
                                    .forEach(relic -> relic.onBlockBroken(creature));
                        }

                    } else {
                        creature.loseBlock(damageAmount);
                        damageAmount = 0;
                    }
                }

                return SpireReturn.Return(damageAmount);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = DesktopLauncher.class, method = "loadSettings")
    public static class MessWithOutputPatch {
        @SpirePrefixPatch
        public static SpireReturn smallHeight(LwjglApplicationConfiguration config) {
            String isServerFlag = System.getProperty("isServer");

            if (isServerFlag != null) {
                if (Boolean.parseBoolean(isServerFlag)) {
                    config.fullscreen = false;
                    config.height = 800;
                    config.width = 800;
                    return SpireReturn.Return(null);
                }
            }

            return SpireReturn.Continue();
        }
    }
}
