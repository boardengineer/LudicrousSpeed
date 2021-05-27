package ludicrousspeed.simulator.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import savestate.SaveState;

import java.util.stream.Collectors;

public class StateDebugInfo {
    private final int playerHealth;
    private final int monsterHealth;
    private final String debugString;

    private final int numBurns;

    public StateDebugInfo(SaveState saveState) {
        this.playerHealth = saveState.getPlayerHealth();
        this.monsterHealth = saveState.curMapNodeState.monsterData.stream()
                                                                  .map(monster -> monster.currentHealth)
                                                                  .reduce(Integer::sum).orElse(0);

        this.debugString = saveState.curMapNodeState.monsterData.stream().map(monster -> String
                .format("%s %s/%s", monster.id, monster.currentHealth, monster.maxHealth))
                                                                .collect(Collectors.joining(" "));

        this.numBurns = 0;
    }

    public StateDebugInfo(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        playerHealth = parsed.get("player_health").getAsInt();
        monsterHealth = parsed.get("monster_health").getAsInt();

        this.debugString = parsed.get("debug_string").getAsString();

        numBurns = parsed.get("num_burns").getAsInt();
    }

    public String encode() {
        JsonObject stateDebugInfoJson = new JsonObject();

        stateDebugInfoJson.addProperty("player_health", playerHealth);
        stateDebugInfoJson.addProperty("monster_health", monsterHealth);

        stateDebugInfoJson.addProperty("num_burns", numBurns);
        stateDebugInfoJson.addProperty("debug_string", debugString);

        return stateDebugInfoJson.toString();
    }
}
