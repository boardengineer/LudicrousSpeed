package ludicrousspeed.simulator.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.rooms.EmptyRoom;
import ludicrousspeed.LudicrousSpeedMod;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ServerStartupPatches {
    private static final String HOST_IP = "127.0.0.1";
    private static final int SERVER_GAME_PORT = 5124;

    @SpirePatch(clz = CardCrawlGame.class, method = "create")
    public static class GameStartupPatch {
        @SpirePostfixPatch
        public static void afterStart(CardCrawlGame game) {
            if (LudicrousSpeedMod.plaidMode) {
                System.err.println("Skipping Splash Screen for Char Select");

                // Sets the current dungeon
                Settings.seed = 123L;
                AbstractDungeon.generateSeeds();

                // TODO this needs to be the actual character class or bad things happen
                new Exordium(CardCrawlGame.characterManager
                        .getCharacter(AbstractPlayer.PlayerClass.IRONCLAD), new ArrayList<>());

                CardCrawlGame.dungeon.currMapNode.room = new EmptyRoom();

                CardCrawlGame.mode = CardCrawlGame.GameMode.GAMEPLAY;
                sendSuccessToController();
            }
        }
    }

    @SpirePatch(clz = CardCrawlGame.class, method = "renderBlackFadeScreen")
    public static class SkipRenderBlackFadeScreen {
        @SpirePrefixPatch
        public static SpireReturn replaceOnServer(CardCrawlGame game, SpriteBatch sb) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "render")
    public static class NoRenderDungeon {
        @SpirePrefixPatch
        public static SpireReturn replaceOnServer(AbstractDungeon dungeon, SpriteBatch sb) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    private static void sendSuccessToController() {
        new Thread(() -> {
            try {
                Thread.sleep(5_000);
                Socket socket;
                socket = new Socket();
                System.out.println("Attempting to connect");
                socket.connect(new InetSocketAddress(HOST_IP, SERVER_GAME_PORT));
                System.out.println("sending success from server game");
                new DataOutputStream(socket.getOutputStream()).writeUTF("SUCCESS");
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                        .getInputStream()));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
