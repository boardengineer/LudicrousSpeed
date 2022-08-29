package ludicrousspeed.simulator.commands;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HandSelectCommand implements Command {
    private final int cardIndex;
    public final String diffStateString;

    public HandSelectCommand(int cardIndex) {
        this.cardIndex = cardIndex;
        this.diffStateString = null;
    }

    public HandSelectCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.diffStateString = null;
        this.cardIndex = parsed.get("card_index").getAsInt();
    }

    public HandSelectCommand(String jsonString, String displayString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.diffStateString = displayString;
        this.cardIndex = parsed.get("card_index").getAsInt();
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

//        if (!shouldGoFast()) {
//            System.err.println("executing hand select command");
//            makeHandSelectScreenChoice(cardIndex);
//        } else {
//

        AbstractDungeon.handCardSelectScreen.hoveredCard = AbstractDungeon.player.hand.group
                .get(cardIndex);

        ReflectionHacks.privateMethod(HandCardSelectScreen.class, "selectHoveredCard")
                       .invoke(AbstractDungeon.handCardSelectScreen);

        //        AbstractDungeon.handCardSelectScreen.button.hb.clicked = true;
//        AbstractDungeon.handCardSelectScreen.update();

//        }
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "HAND_SELECT");
        cardCommandJson.addProperty("card_index", cardIndex);


        return cardCommandJson.toString();
    }

    public static void makeHandSelectScreenChoice(int choice) {
        HandCardSelectScreen screen = AbstractDungeon.handCardSelectScreen;
        screen.hoveredCard = AbstractDungeon.player.hand.group.get(choice);
        screen.hoveredCard.setAngle(0.0f, false); // This might not be necessary
        try {
            Method hotkeyCheck = HandCardSelectScreen.class.getDeclaredMethod("selectHoveredCard");
            hotkeyCheck.setAccessible(true);
            hotkeyCheck.invoke(screen);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("selectHoveredCard method somehow can't be called.");
        }
    }

    @Override
    public String toString() {
        return "HandSelectCommand" + cardIndex;
    }
}
