package ludicrousspeed.simulator.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.OverlayMenu;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import ludicrousspeed.LudicrousSpeedMod;

public class ScreenPatches {
    @SpirePatch(
            clz = CombatRewardScreen.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class NoRenderBodyPatch {
        public static SpireReturn Prefix(CombatRewardScreen _instance, SpriteBatch sb) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = OverlayMenu.class,
            paramtypez = {},
            method = "showCombatPanels"
    )
    public static class TooMdddanyLinesPatch {
        public static SpireReturn Prefix(OverlayMenu _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
