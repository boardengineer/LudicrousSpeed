package ludicrousspeed.simulator.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.ExhaustBlurEffect;
import com.megacrit.cardcrawl.vfx.ExhaustEmberEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;

public class FastExhaustPatches {
    private static final float START_DURATION = .25F;

    @SpirePatch(clz = ExhaustCardEffect.class, method = SpirePatch.CONSTRUCTOR)
    public static class FastDurationPatch {
        @SpirePostfixPatch
        public static void fastDuration(ExhaustCardEffect effect, AbstractCard c) {
            effect.duration = START_DURATION;
        }
    }

    @SpirePatch(clz = ExhaustCardEffect.class, method = "update")
    public static class FastExhaustUpdatePatch {
        @SpirePrefixPatch
        public static SpireReturn doFast(ExhaustCardEffect effect) {
            AbstractCard c = ReflectionHacks.getPrivate(effect, ExhaustCardEffect.class, "c");

            if (effect.duration == START_DURATION) {
                CardCrawlGame.sound.play("CARD_EXHAUST", 0.2F);

                int i;
                for (i = 0; i < 90; ++i) {
                    AbstractDungeon.effectsQueue
                            .add(new ExhaustBlurEffect(c.current_x, c.current_y));
                }

                for (i = 0; i < 50; ++i) {
                    AbstractDungeon.effectsQueue
                            .add(new ExhaustEmberEffect(c.current_x, c.current_y));
                }
            }

            effect.duration -= Gdx.graphics.getDeltaTime();
            if (!c.fadingOut && effect.duration < (0.7F * START_DURATION) && !AbstractDungeon.player.hand
                    .contains(c)) {
                c.fadingOut = true;
            }

            if (effect.duration < 0.0F) {
                effect.isDone = true;
                c.resetAttributes();
            }

            return SpireReturn.Return(null);
        }
    }
}
