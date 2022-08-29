package ludicrousspeed.simulator.commands;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;

public class HandSelectConfirmCommand implements Command {
    private final String diffStateString;
    public static final HandSelectConfirmCommand INSTANCE = new HandSelectConfirmCommand();

    private HandSelectConfirmCommand() {
        this.diffStateString = null;
    }

    public HandSelectConfirmCommand(String diffString) {
        this.diffStateString = diffString;
    }

    @Override
    public void execute() {
//        if (diffStateString != null) {
//            try {
//                String actualState = new SaveState().diffEncode();
//                String expectedState = Files.lines(Paths.get(diffStateString))
//                                            .collect(Collectors.joining());
//
//                if (!SaveState.diff(actualState, expectedState)) {
//                    System.err.println("PANIC PANIC PANIC " + this.toString());
//                    LudicrousSpeedMod.mustRestart = true;
//                    return;
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }


        HandCardSelectScreen screen = AbstractDungeon.handCardSelectScreen;

        ReflectionHacks
                .setPrivate(AbstractDungeon.handCardSelectScreen, HandCardSelectScreen.class, "hand", AbstractDungeon.player.hand);

        screen.button.hb.clicked = true;
        screen.update();
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "HAND_SELECT_CONFIRM");

        return cardCommandJson.toString();
    }
}
