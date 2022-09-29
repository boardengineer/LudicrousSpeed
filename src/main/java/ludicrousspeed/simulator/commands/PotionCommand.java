package ludicrousspeed.simulator.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import ludicrousspeed.LudicrousSpeedMod;
import ludicrousspeed.simulator.ActionSimulator;
import savestate.SaveState;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class PotionCommand implements Command {
    private final int potionIndex;
    private final int monsterIndex;

    private String diffStateString = null;

    public PotionCommand(int potionIndex, int monsterIndex) {
        this.potionIndex = potionIndex;
        this.monsterIndex = monsterIndex;
    }

    public PotionCommand(int potionIndex) {
        this(potionIndex, -1);
    }

    public PotionCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.potionIndex = parsed.get("potion_index").getAsInt();
        this.monsterIndex = parsed.get("monster_index").getAsInt();
    }

    public PotionCommand(String jsonString, String diffStateString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.potionIndex = parsed.get("potion_index").getAsInt();
        this.monsterIndex = parsed.get("monster_index").getAsInt();

        this.diffStateString = diffStateString;
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


        AbstractPotion potion = AbstractDungeon.player.potions.get(potionIndex);
        AbstractCreature target = AbstractDungeon.player;

        if (monsterIndex != -1) {
            target = AbstractDungeon.getMonsters().monsters.get(monsterIndex);
            if (!LudicrousSpeedMod.plaidMode) {
                String allMonsters = AbstractDungeon.getMonsters().monsters.stream().map(m -> String
                        .format("hp:%s\t", m.currentHealth)).collect(Collectors.joining());
            }
        }

        potion.use(target);
        AbstractDungeon.player.relics.forEach(relic -> relic.onUsePotion());

        AbstractDungeon.topPanel.destroyPotion(potionIndex);

        if (!LudicrousSpeedMod.plaidMode) {
            AbstractDungeon.actionManager.addToBottom(new WaitAction(.2F));
        } else {
            ActionSimulator.ActionManageUpdate();
        }
    }

    @Override
    public String toString() {
        return "Potion " + potionIndex + " " + monsterIndex;
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "POTION");

        cardCommandJson.addProperty("potion_index", potionIndex);
        cardCommandJson.addProperty("monster_index", monsterIndex);

        return cardCommandJson.toString();
    }
}
