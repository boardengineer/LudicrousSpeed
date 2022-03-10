package ludicrousspeed.simulator.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import ludicrousspeed.LudicrousSpeedMod;

public class TopPanelPatches {
    @SpirePatch(clz = TopPanel.class, method = "setPlayerName")
    public static class quickSetPlayerName {
        @SpirePrefixPatch
        public static SpireReturn notOnServerPatch(TopPanel topPanel) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = TopPanel.class, method = "unhoverHitboxes")
    public static class quickUnhoverHitboxes {
        @SpirePrefixPatch
        public static SpireReturn notOnServerPatch(TopPanel topPanel) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = TopPanel.class, method = "update")
    public static class quickUpdate {
        @SpirePrefixPatch
        public static SpireReturn notOnServerPatch(TopPanel topPanel) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
