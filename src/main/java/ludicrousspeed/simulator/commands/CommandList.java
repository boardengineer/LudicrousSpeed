package ludicrousspeed.simulator.commands;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.TwinStrike;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.buttons.CardSelectConfirmButton;
import savestate.PotionState;

import java.util.*;
import java.util.stream.Stream;

public final class CommandList {
    public static List<Command> getAvailableCommands() {
        return getAvailableCommands(null);
    }

    public static List<Command> getAvailableCommands(Comparator<AbstractCard> cardComparator) {
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
                                            .format(card.cardID + " for " + card.baseDamage)));
                                }
                            }
                        }

                        if (card.target == AbstractCard.CardTarget.ALL_ENEMY || card.target == AbstractCard.CardTarget.ALL) {
                            if (card.canUse(player, null)) {
                                commands.add(new CardCommand(cardEntry
                                        .getValue(), card.cardID + " for " + card.baseBlock));
                            }
                        }

                        if (card.target == AbstractCard.CardTarget.SELF || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY || card.target == AbstractCard.CardTarget.NONE) {
                            if (card.canUse(player, null)) {
                                commands.add(new CardCommand(cardEntry
                                        .getValue(), card.cardID + " for " + card.baseMagicNumber));
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

                TwinStrike twinStrike;

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
                if (AbstractDungeon.actionManager.currentAction instanceof DiscardAction) {
                    HashMap<Integer, AbstractCard> indexToCardMap = new HashMap<>();

                    for (int i = 0; i < AbstractDungeon.player.hand.group.size(); i++) {
                        indexToCardMap.put(i, AbstractDungeon.player.hand.group.get(i));
                    }

                    indexToCardMap.entrySet().stream().sorted((e1, e2) -> {
                        AbstractCard card1 = e1.getValue();
                        AbstractCard card2 = e2.getValue();

                        if (DiscardOrder.CARD_RANKS
                                .containsKey(card1.cardID) && DiscardOrder.CARD_RANKS
                                .containsKey(card2.cardID)) {
                            return DiscardOrder.CARD_RANKS
                                    .get(card1.cardID) - DiscardOrder.CARD_RANKS.get(card2.cardID);
                        } else if (DiscardOrder.CARD_RANKS.containsKey(card1.cardID)) {
                            return 1;
                        }

                        return 0;
                    })
                                  .forEach(entry -> orderedIndeces.add(entry.getKey()));
                } else if (AbstractDungeon.actionManager.currentAction instanceof ExhaustAction) {
                    HashMap<Integer, AbstractCard> indexToCardMap = new HashMap<>();

                    for (int i = 0; i < AbstractDungeon.player.hand.group.size(); i++) {
                        indexToCardMap.put(i, AbstractDungeon.player.hand.group.get(i));
                    }

                    indexToCardMap.entrySet().stream().sorted((e1, e2) -> {
                        AbstractCard card1 = e1.getValue();
                        AbstractCard card2 = e2.getValue();

                        if (ExhaustOrder.CARD_RANKS
                                .containsKey(card1.cardID) && ExhaustOrder.CARD_RANKS
                                .containsKey(card2.cardID)) {
                            return ExhaustOrder.CARD_RANKS
                                    .get(card1.cardID) - ExhaustOrder.CARD_RANKS.get(card2.cardID);
                        } else if (ExhaustOrder.CARD_RANKS.containsKey(card1.cardID)) {
                            return 1;
                        }

                        return 0;
                    })
                                  .forEach(entry -> orderedIndeces.add(entry.getKey()));
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
                commands.add(new GridSelectCommand(i));
            }
        }

        if (isEndCommandAvailable()) {
            commands.add(new EndCommand());
        }

        return commands;
    }

    private static boolean shouldCheckForPlays() {
        return isInDungeon() &&
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

    private CommandList() {
    }
}
