package ludicrousspeed.simulator.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import ludicrousspeed.LudicrousSpeedMod;
import savestate.SaveState;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CardCommand implements Command {
    public final int cardIndex;
    public final int monsterIndex;
    public final String displayString;

    private String diffStateString = null;

    public CardCommand(int cardIndex, int monsterIndex, String displayString) {
        this.cardIndex = cardIndex;
        this.monsterIndex = monsterIndex;
        this.displayString = displayString;
    }

    public CardCommand(int cardIndex, String displayString) {
        this.cardIndex = cardIndex;
        this.monsterIndex = -1;
        this.displayString = displayString;
    }

    public CardCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardIndex = parsed.get("card_index").getAsInt();
        this.monsterIndex = parsed.get("monster_index").getAsInt();
        this.displayString = parsed.get("display_string").getAsString();
    }

    public CardCommand(String jsonString, String diffStateString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardIndex = parsed.get("card_index").getAsInt();
        this.monsterIndex = parsed.get("monster_index").getAsInt();
        this.displayString = parsed.get("display_string").getAsString();
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

        if (!LudicrousSpeedMod.plaidMode) {
            JsonObject cardLog = new JsonObject();
            cardLog.addProperty("card_name", AbstractDungeon.player.hand.group.get(cardIndex).name);
            cardLog.addProperty("card_index", cardIndex);
            if (monsterIndex != -1) {
                cardLog.addProperty("monster_index", monsterIndex);
            }

            JsonObject playerState = new JsonObject();
            playerState.addProperty("max_health", AbstractDungeon.player.maxHealth);
            playerState.addProperty("current_health", AbstractDungeon.player.currentHealth);
            cardLog.add("player_state", playerState);

            JsonArray monstersArray = new JsonArray();
            ArrayList<AbstractMonster> gameArray = AbstractDungeon.currMapNode.room.monsters.monsters;
            for(int i = 0; i < gameArray.size(); i++) {
                AbstractMonster monster = gameArray.get(i);
                JsonObject monsterJson = new JsonObject();

                monsterJson.addProperty("monster_index", i);
                monsterJson.addProperty("monster_name", monster.name);
                monsterJson.addProperty("max_hp", monster.maxHealth);
                monsterJson.addProperty("current_hp", monster.currentHealth);

                monstersArray.add(monsterJson);
            }
            cardLog.add("monster_state", monstersArray);

            LudicrousSpeedMod.jsonActionLog.add(cardLog.toString());
        }

        AbstractDungeon.player.hand.refreshHandLayout();
        AbstractCard card = AbstractDungeon.player.hand.group.get(cardIndex);
        AbstractMonster monster = null;

        if (monsterIndex != -1) {
            monster = AbstractDungeon.getMonsters().monsters.get(monsterIndex);

            if (AbstractDungeon.player.hasPower("Surrounded")) {
                AbstractDungeon.player.flipHorizontal = monster.drawX < AbstractDungeon.player.drawX;

                for (AbstractMonster toApply : AbstractDungeon.getMonsters().monsters) {
                    toApply.applyPowers();
                }
            }
        }

        AbstractDungeon.actionManager.cardQueue.add(new CardQueueItem(card, monster));

        if (!LudicrousSpeedMod.plaidMode) {
            AbstractDungeon.actionManager.addToBottom(new WaitAction(.2F));
        } else {
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
        }
    }

    @Override
    public String toString() {
        return displayString + monsterIndex;
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "CARD");

        cardCommandJson.addProperty("card_index", cardIndex);
        cardCommandJson.addProperty("monster_index", monsterIndex);
        cardCommandJson.addProperty("display_string", displayString);

        return cardCommandJson.toString();
    }
}
