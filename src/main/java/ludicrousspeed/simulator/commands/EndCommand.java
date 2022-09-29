package ludicrousspeed.simulator.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.LudicrousSpeedMod;
import savestate.SaveState;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class EndCommand implements Command {
    public StateDebugInfo stateDebugInfo = null;

    private String diffStateString = null;

    public EndCommand() {
    }

    public EndCommand(String jsonString, String diffStateString) {
        try {
            JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

            if (parsed.has("state_debug_info"))
                stateDebugInfo = new StateDebugInfo(parsed.get("state_debug_info").getAsString());
            this.diffStateString = diffStateString;
        } catch (Exception e) {
            System.err.println("Exception");
            // still return a plain End Command
        }

    }

    public EndCommand(String jsonString) {
        try {
            JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

            if (parsed.has("state_debug_info"))
                stateDebugInfo = new StateDebugInfo(parsed.get("state_debug_info").getAsString());
        } catch (Exception e) {
            System.err.println("Exception");
            // still return a plain End Command
        }

    }

    @Override
    public void execute() {
        if (diffStateString != null) {
            try {
                String actualState = new SaveState().diffEncode();
                String expectedState = "";
                try (FileInputStream fis = new FileInputStream(diffStateString);
                     InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                     BufferedReader reader = new BufferedReader(isr)) {
                    expectedState = reader.lines().collect(Collectors.joining());
                }

                if (!SaveState.diff(actualState, expectedState)) {
                    System.err.println("PANIC PANIC PANIC " + this.toString());
                    LudicrousSpeedMod.mustRestart = true;
                    return;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        AbstractDungeon.overlayMenu.endTurnButton.disable(true);
    }

    @Override
    public String toString() {

        String debugString = "";
        if (stateDebugInfo != null) {
            debugString = stateDebugInfo.encode();
        }

        return "EndCommand " + debugString + "\n";
    }

    @Override
    public String encode() {
        JsonObject endCommandJson = new JsonObject();

        endCommandJson.addProperty("type", "END");

        if (stateDebugInfo != null) {
            endCommandJson.addProperty("state_debug_info", stateDebugInfo.encode());
        }
        return endCommandJson.toString();
    }
}
