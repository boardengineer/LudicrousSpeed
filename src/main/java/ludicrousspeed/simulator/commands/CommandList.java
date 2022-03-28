package ludicrousspeed.simulator.commands;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.Clumsy;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.cards.status.VoidCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.CardSelectConfirmButton;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import savestate.PotionState;

import java.util.*;
import java.util.stream.Stream;

public final class CommandList {
    public static List<Command> getAvailableCommands() {
        return getAvailableCommands(null);
    }


    public static List<Command> getAvailableCommands(Comparator<AbstractCard> playHeuristic) {
        return getAvailableCommands(playHeuristic, new HashMap<>());
    }

    public static List<Command> getAvailableCommands(Comparator<AbstractCard> cardComparator, HashMap<Class, Comparator<AbstractCard>> actionHeuristics) {
        List<Command> commands = new ArrayList<>();
        AbstractPlayer player = AbstractDungeon.player;
        List<AbstractCard> hand = player.hand.group;
        List<AbstractPotion> potions = player.potions;

        List<AbstractMonster> monsters = AbstractDungeon.currMapNode.room.monsters.monsters;
        Set<String> seenCommands = new HashSet<>();

        if (shouldCheckForPlays()) {
            HashMap<AbstractCard, Integer> cardIndeces = new HashMap<>();

            for (int i = 0; i < hand.size(); i++) {
                cardIndeces.put(hand.get(i), i);
            }

            Stream<Map.Entry<AbstractCard, Integer>> cardStream = cardIndeces.entrySet().stream();

            if (cardComparator != null) {
                cardStream = cardStream.sorted((card1, card2) -> cardComparator
                        .compare(card1.getKey(), card2.getKey()));
            }

            cardStream.forEach(
                    cardEntry -> {
                        AbstractCard card = cardEntry.getKey();

                        // Only populate the first time you've seen a card with this specific {name X upgraded}
                        String setName = card.name + (card.upgraded ? "+" : "");
                        int oldCount = seenCommands.size();
                        seenCommands.add(setName);
                        if (oldCount == seenCommands.size()) {
                            return;
                        }

                        if (card.target == AbstractCard.CardTarget.ENEMY || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                            for (int j = 0; j < monsters.size(); j++) {
                                AbstractMonster monster = monsters.get(j);
                                if (card.canUse(player, monster) && !monster.isDeadOrEscaped()) {
                                    commands.add(new CardCommand(cardEntry.getValue(), j, String
                                            .format(card.cardID)));
                                }
                            }
                        }

                        if (card.target == AbstractCard.CardTarget.ALL_ENEMY || card.target == AbstractCard.CardTarget.ALL) {
                            if (card.canUse(player, null)) {
                                commands.add(new CardCommand(cardEntry
                                        .getValue(), card.cardID));
                            }
                        }

                        if (card.target == AbstractCard.CardTarget.SELF || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY || card.target == AbstractCard.CardTarget.NONE) {
                            if (card.canUse(player, null)) {
                                commands.add(new CardCommand(cardEntry
                                        .getValue(), card.cardID));
                            }
                        }

                    }
            );

            for (int i = 0; i < potions.size(); i++) {
                AbstractPotion potion = potions.get(i);
                if (!potion
                        .canUse() || !potion.isObtained || potion instanceof PotionSlot || PotionState.UNPLAYABLE_POTIONS
                        .contains(potion.ID)) {
                    continue;
                }

                // Dedupe potions
                String setName = potion.name;
                int oldCount = seenCommands.size();
                seenCommands.add(setName);
                if (oldCount == seenCommands.size()) {
                    continue;
                }

                if (potion.targetRequired) {
                    for (int j = 0; j < monsters.size(); j++) {
                        AbstractMonster monster = monsters.get(j);
                        if (!monster.isDeadOrEscaped()) {
                            commands.add(new PotionCommand(i, j));
                        }
                    }
                } else {
                    commands.add(new PotionCommand(i));
                }
            }
        }

        if (isInHandSelect()) {
            if (AbstractDungeon.handCardSelectScreen.selectedCards.group
                    .size() < AbstractDungeon.handCardSelectScreen.numCardsToSelect) {

                ArrayList<Integer> orderedIndeces = new ArrayList<>();

                if (actionHeuristics
                        .containsKey(AbstractDungeon.actionManager.currentAction.getClass())) {
                    HashMap<Integer, AbstractCard> indexToCardMap = new HashMap<>();

                    for (int i = 0; i < AbstractDungeon.player.hand.group.size(); i++) {
                        indexToCardMap.put(i, AbstractDungeon.player.hand.group.get(i));
                    }

                    indexToCardMap.entrySet().stream().sorted((e1, e2) -> {
                        Comparator<AbstractCard> heuristic = actionHeuristics
                                .get(AbstractDungeon.actionManager.currentAction.getClass());
                        return heuristic.compare(e1.getValue(), e2.getValue());
                    }).forEach(entry -> orderedIndeces.add(entry.getKey()));

                } else {
                    for (int i = 0; i < AbstractDungeon.player.hand.group.size(); i++) {
                        orderedIndeces.add(i);
                    }
                }

                orderedIndeces.forEach(index -> commands.add(new HandSelectCommand(index)));
            }

            if (isHandSelectConfirmButtonEnabled()) {
                commands.add(HandSelectConfirmCommand.INSTANCE);
            }
        }

        if (isInGridSelect()) {
            for (int i = 0; i < AbstractDungeon.gridSelectScreen.targetGroup.size(); i++) {
                AbstractCard card = AbstractDungeon.gridSelectScreen.targetGroup.group.get(i);
                if (!card.isGlowing) {
                    // Weak hack to only scry basics curses and statuses
                    boolean canClick = true;

                    if (AbstractDungeon.actionManager.currentAction instanceof ScryAction) {
                        canClick = false;
                        if (card.type == AbstractCard.CardType.STATUS) {
                            if (card.cardID != Dazed.ID && card.cardID != VoidCard.ID) {
                                canClick = true;
                            }
                        }

                        if (card.type == AbstractCard.CardType.CURSE) {
                            if (card.cardID != Clumsy.ID) {
                                canClick = true;
                            }
                        }

                        if (card.hasTag(AbstractCard.CardTags.STARTER_DEFEND) || card
                                .hasTag(AbstractCard.CardTags.STARTER_STRIKE)) {
                            canClick = true;
                        }
                    }


                    if (canClick) {
                        commands.add(new GridSelectCommand(i));
                    }
                }
            }

            if (isGridScreenConfirmAvailable()) {
                commands.add(GridSelectConfrimCommand.INSTANCE);
            }
        }

        if (isInCardRewardSelect()) {
            for (int i = 0; i < AbstractDungeon.cardRewardScreen.rewardGroup.size(); i++) {
                commands.add(new CardRewardSelectCommand(i));
            }
        }


        if (isEndCommandAvailable()) {
            commands.add(new EndCommand());
        }

        return commands;
    }

    private static boolean shouldCheckForPlays() {
        return isInDungeon() &&
                !(AbstractDungeon.player.isDead || AbstractDungeon.player.isDying) &&
                (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT &&
                        !AbstractDungeon.isScreenUp &&
                        (AbstractDungeon.actionManager.currentAction == null && AbstractDungeon.actionManager.actions
                                .isEmpty()));
    }

    private static boolean isInDungeon() {
        return CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY && AbstractDungeon
                .isPlayerInDungeon() && AbstractDungeon.currMapNode != null;
    }

    private static boolean isEndCommandAvailable() {
        return isInDungeon() && AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.isScreenUp;
    }

    private static boolean isInGridSelect() {
        return isInDungeon() &&
                AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT &&
                AbstractDungeon.isScreenUp &&
                AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID;
    }

    private static boolean isInCardRewardSelect() {
        return isInDungeon() &&
                AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT &&
                AbstractDungeon.isScreenUp &&
                AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD;
    }

    private static boolean isInHandSelect() {
        return isInDungeon() &&
                AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT &&
                AbstractDungeon.isScreenUp &&
                AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT;
    }

    private static boolean isHandSelectConfirmButtonEnabled() {
        CardSelectConfirmButton button = AbstractDungeon.handCardSelectScreen.button;
        boolean isHidden = ReflectionHacks
                .getPrivate(button, CardSelectConfirmButton.class, "isHidden");
        boolean isDisabled = button.isDisabled;
        return !(isHidden || isDisabled);
    }

    private static boolean isGridScreenConfirmAvailable() {
        GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
        if (screen.confirmScreenUp || screen.isJustForConfirming) {
            return true;
        } else if ((!screen.confirmButton.isDisabled) && (!(boolean) ReflectionHacks
                .getPrivate(screen.confirmButton, GridSelectConfirmButton.class, "isHidden"))) {
            return screen.forUpgrade || screen.forTransform || screen.forPurge || screen.anyNumber;
        }
        return false;
    }

    private CommandList() {
    }
}
