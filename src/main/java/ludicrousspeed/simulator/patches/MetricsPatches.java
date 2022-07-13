package ludicrousspeed.simulator.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.metrics.MetricData;
import ludicrousspeed.LudicrousSpeedMod;

public class MetricsPatches {
    @SpirePatch(clz = MetricData.class, method = "addEncounterData")
    public static class NoEncounterDataPatch {
        @SpirePrefixPatch
        public static SpireReturn doNothing(MetricData metricData) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
