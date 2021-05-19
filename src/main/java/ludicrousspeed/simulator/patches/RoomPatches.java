package ludicrousspeed.simulator.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import ludicrousspeed.LudicrousSpeedMod;

public class RoomPatches {
    @SpirePatch(
            clz = AbstractRoom.class,
            paramtypez = {},
            method = "addPotionToRewards"
    )
    public static class PotionRemovePatch {
        public static SpireReturn Prefix(AbstractRoom _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
