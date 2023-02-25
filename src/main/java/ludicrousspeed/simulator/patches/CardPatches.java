package ludicrousspeed.simulator.patches;

import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.abstracts.CustomCard;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.defect.ScrapeFollowUpAction;
import com.megacrit.cardcrawl.actions.unique.ExhumeAction;
import com.megacrit.cardcrawl.actions.unique.RestoreRetainedCardsAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.SoulGroup;
import com.megacrit.cardcrawl.cards.green.DaggerSpray;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToHandEffect;
import ludicrousspeed.LudicrousSpeedMod;
import savestate.SaveStateMod;

import java.util.Iterator;
import java.util.UUID;

import static savestate.SaveStateMod.addRuntime;
import static savestate.SaveStateMod.shouldGoFast;

public class CardPatches {
    // Fast Mode doesn't load images which will NPE when trying to render, turn off rendering
    // in fast mode.
    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {SpriteBatch.class, boolean.class, boolean.class},
            method = "renderCard"
    )
    public static class NoRenderCardsPatch {
        public static SpireReturn Prefix(AbstractCard _instance, SpriteBatch sb, boolean hovered, boolean selected) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "updateTransparency"
    )
    public static class NoUpdateTransparencyPatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz=DiscardAtEndOfTurnAction.class,
            method = "update"
    )
    public static class NoRandomEtherealPatch {
        @SpirePrefixPatch
        public static SpireReturn noShuffle(DiscardAtEndOfTurnAction action) {
            float duration = ReflectionHacks.getPrivate(action, AbstractGameAction.class, "duration");
            if (duration == Settings.ACTION_DUR_XFAST) {
                Iterator c = AbstractDungeon.player.hand.group.iterator();

                while(true) {
                    AbstractCard e;
                    do {
                        if (!c.hasNext()) {
                            AbstractDungeon.actionManager.addToTop(new RestoreRetainedCardsAction(AbstractDungeon.player.limbo));
                            if (!AbstractDungeon.player.hasRelic("Runic Pyramid") && !AbstractDungeon.player.hasPower("Equilibrium")) {
                                int tempSize = AbstractDungeon.player.hand.size();

                                for(int i = 0; i < tempSize; ++i) {
                                    AbstractDungeon.actionManager.addToTop(new DiscardAction(AbstractDungeon.player, (AbstractCreature)null, AbstractDungeon.player.hand.size(), true, true));
                                }
                            }

                            for(AbstractCard toTrigger: AbstractDungeon.player.hand.group) {
                                toTrigger.triggerOnEndOfPlayerTurn();
                            }

                            action.isDone = true;
                            return SpireReturn.Return(null);
                        }

                        e = (AbstractCard)c.next();
                    } while(!e.retain && !e.selfRetain);

                    AbstractDungeon.player.limbo.addToTop(e);
                    c.remove();
                }
            }
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(
            clz = CardGroup.class,
            paramtypez = {},
            method = "glowCheck"
    )
    public static class NoGlowCheckPatch {
        public static SpireReturn Prefix(CardGroup _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = CardGroup.class,
            paramtypez = {AbstractCard.class},
            method = "moveToDiscardPile"
    )
    public static class FastDiscardPatch {
        @SpirePrefixPatch
        public static SpireReturn Prefix(CardGroup cardGroup, AbstractCard card) {
            if (LudicrousSpeedMod.plaidMode) {
                int startingSize = cardGroup.group.size();

                ReflectionHacks
                        .privateMethod(CardGroup.class, "resetCardBeforeMoving", AbstractCard.class)
                        .invoke(cardGroup, card);

                if (cardGroup.group.size() == startingSize) {
                    for (AbstractCard groupCard : cardGroup.group) {
                        if (groupCard.uuid.equals(card.uuid)) {
                            cardGroup.group.remove(groupCard);
                            break;
                        }
                    }
                }

                card.clearPowers();
                AbstractDungeon.player.discardPile.addToTop(card);
                AbstractDungeon.player.onCardDrawOrDiscard();

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ExhumeAction.class,
            method = "update"
    )
    public static class ClearPowersForExhumePatch {
        @SpireInsertPatch(loc = 118)
        public static void Prefix(ExhumeAction action) {
            AbstractDungeon.gridSelectScreen.selectedCards.forEach(AbstractCard::clearPowers);
        }
    }

    @SpirePatch(
            clz = CardGroup.class,
            paramtypez = {AbstractCard.class, boolean.class},
            method = "moveToDeck"
    )
    public static class FastMoveToDeckDiscardPatch {
        public static SpireReturn Prefix(CardGroup _instance, AbstractCard card, boolean randomSpot) {
            ReflectionHacks
                    .privateMethod(CardGroup.class, "resetCardBeforeMoving", AbstractCard.class)
                    .invoke(_instance, card);

            if (randomSpot) {
                AbstractDungeon.player.drawPile.addToRandomSpot(card);
            } else {
                AbstractDungeon.player.drawPile.addToTop(card);
            }

            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoUpdatePlayerPatch {
        public static SpireReturn Prefix(AbstractPlayer _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "updateColor"
    )
    public static class NoUpdateColorPatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "unfadeOut"
    )
    public static class NoFadeOutPatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {boolean.class},
            method = "darken"
    )
    public static class NoDarkenPatch {
        public static SpireReturn Prefix(AbstractCard _instance, boolean immediate) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "makeStatEquivalentCopy"
    )
    public static class UseCardPoolForRandomCreationPatch {
        @SpirePrefixPatch
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                AbstractCard card = _instance.makeCopy();

                for (int i = 0; i < _instance.timesUpgraded; ++i) {
                    card.upgrade();
                }

                card.name = _instance.name;
                card.target = _instance.target;
                card.upgraded = _instance.upgraded;
                card.timesUpgraded = _instance.timesUpgraded;
                card.baseDamage = _instance.baseDamage;
                card.baseBlock = _instance.baseBlock;
                card.block = _instance.block;
                card.baseMagicNumber = _instance.baseMagicNumber;
                card.magicNumber = _instance.magicNumber;
                card.cost = _instance.cost;
                card.costForTurn = _instance.costForTurn;
                card.isCostModified = _instance.isCostModified;
                card.isCostModifiedForTurn = _instance.isCostModifiedForTurn;
                card.inBottleLightning = _instance.inBottleLightning;
                card.inBottleFlame = _instance.inBottleFlame;
                card.inBottleTornado = _instance.inBottleTornado;
                card.isSeen = _instance.isSeen;
                card.isLocked = _instance.isLocked;
                card.misc = _instance.misc;
                card.freeToPlayOnce = _instance.freeToPlayOnce;

                return SpireReturn.Return(card);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "upgradeName")
    public static class NoUpgradeNamePatch {
        @SpirePrefixPatch
        public static SpireReturn noNameChange(AbstractCard card) {
            if (LudicrousSpeedMod.plaidMode) {
                ++card.timesUpgraded;
                card.upgraded = true;

                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "makeSameInstanceOf"
    )
    public static class UseCardPoolForRandomCreationAndInstancePatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                AbstractCard result = _instance.makeStatEquivalentCopy();

                while (result == null) {
                    System.err.println("Failed to create card, trying again...");
                    result = _instance.makeStatEquivalentCopy();
                }

                result.uuid = _instance.uuid;

                return SpireReturn.Return(result);
            }
            return SpireReturn.Continue();
        }
    }


    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {AbstractCard.class},
            method = "bottledCardUpgradeCheck"
    )
    public static class NoBottledDescriptionChangePatch {
        public static SpireReturn Prefix(AbstractPlayer _instance, AbstractCard card) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }


    @SpirePatch(
            clz = ShowCardAndAddToDiscardEffect.class,
            paramtypez = {},
            method = "update"
    )
    public static class ShowCardAndAddToDiscardEffectNoUpdatePatch {
        public static SpireReturn Prefix(ShowCardAndAddToDiscardEffect _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ShowCardAndAddToHandEffect.class,
            paramtypez = {AbstractCard.class, float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class ApplyPowersOnNewCardsPatch {
        @SpirePostfixPatch
        public static void applypowers(ShowCardAndAddToHandEffect _instance, AbstractCard card, float offsetX, float offsetY) {
            AbstractDungeon.player.hand.applyPowers();
        }
    }

    @SpirePatch(
            clz = ShowCardAndAddToHandEffect.class,
            paramtypez = {AbstractCard.class, float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class ShowCardAndAddToHandEffectPatchTwo {
        @SpirePrefixPatch
        public static SpireReturn Prefix(ShowCardAndAddToHandEffect _instance, AbstractCard card, float x, float y) {
            if (LudicrousSpeedMod.plaidMode) {
                if (card == null) {
                    throw new IllegalStateException("Card Is null, Nothing to return ");
                }

                if (card.type != AbstractCard.CardType.CURSE && card.type != AbstractCard.CardType.STATUS && AbstractDungeon.player
                        .hasPower("MasterRealityPower")) {
                    card.upgrade();
                }

                AbstractDungeon.player.hand.addToTop(card);

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MakeTempCardInHandAction.class,
            paramtypez = {},
            method = "makeNewCard"
    )
    public static class MakeNewCardPatch {
        public static SpireReturn Prefix(MakeTempCardInHandAction action) {
            if (LudicrousSpeedMod.plaidMode) {
                AbstractCard card = ReflectionHacks
                        .getPrivate(action, MakeTempCardInHandAction.class, "c");
                boolean sameUUID = ReflectionHacks
                        .getPrivate(action, MakeTempCardInHandAction.class, "sameUUID");

                AbstractCard result = null;

                while (result == null) {
                    result = sameUUID ? card.makeSameInstanceOf() : card.makeStatEquivalentCopy();

                    if (result == null) {
                        System.err.println("Failed to create card, retrying");
                    }
                }

                return SpireReturn.Return(result);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MakeTempCardInHandAction.class,
            paramtypez = {int.class},
            method = "addToHand"
    )
    public static class AddToHandPatch {
        public static SpireReturn Prefix(MakeTempCardInHandAction action, int handAmt) {
            if (LudicrousSpeedMod.plaidMode) {
                for (int i = 0; i < handAmt; ++i) {
                    AbstractCard card = null;
                    while (card == null) {
                        card = (AbstractCard) MakeNewCardPatch.Prefix(action)
                                                              .get();
                        if (card == null) {
                            System.err.println("card was null, retrying...");
                        }
                    }

                    if (card.type != AbstractCard.CardType.CURSE && card.type != AbstractCard.CardType.STATUS && AbstractDungeon.player
                            .hasPower("MasterRealityPower")) {
                        card.upgrade();
                    }

                    AbstractDungeon.player.hand.addToHand(card);
                    card.triggerWhenCopied();
                    AbstractDungeon.player.hand.applyPowers();
                    AbstractDungeon.player.onCardDrawOrDiscard();
                    if (AbstractDungeon.player
                            .hasPower("Corruption") && card.type == AbstractCard.CardType.SKILL) {
                        card.setCostForTurn(-9);
                    }
                }

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MakeTempCardInDiscardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class MakeTempCardsFastPatch {
        public static void Prefix(MakeTempCardInDiscardAction _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .00001F);

                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .00001F);
            }
        }

        public static void Postfix(MakeTempCardInDiscardAction _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = PlayTopCardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class PlayTopCardPatch {
        public static SpireReturn Prefix(PlayTopCardAction _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                if (AbstractDungeon.player.drawPile.size() + AbstractDungeon.player.discardPile
                        .size() == 0) {
                    _instance.isDone = true;
                    return SpireReturn.Return(null);
                }

                if (AbstractDungeon.player.drawPile.isEmpty()) {
                    boolean exhaustCards = ReflectionHacks
                            .getPrivate(_instance, PlayTopCardAction.class, "exhaustCards");

                    AbstractDungeon.actionManager
                            .addToTop(new PlayTopCardAction(_instance.target, exhaustCards));
                    AbstractDungeon.actionManager.addToTop(new EmptyDeckShuffleAction());
                    _instance.isDone = true;
                    return SpireReturn.Return(null);
                }
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ScrapeFollowUpAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class ScrapePatch {
        public static SpireReturn Prefix(ScrapeFollowUpAction _instance) {
            for (AbstractCard card : DrawCardAction.drawnCards) {
                if (card.costForTurn != 0 && !card.freeToPlayOnce) {
                    card.resetAttributes();
                    AbstractDungeon.player.hand.moveToDiscardPile(card);
                    card.triggerOnManualDiscard();
                    GameActionManager.incrementDiscard(false);
                }
            }

            _instance.isDone = true;
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(
            clz = DaggerSpray.class,
            method = "use"
    )
    public static class NoFxDaggerSprayPatch {
        public static SpireReturn Prefix(DaggerSpray spray, AbstractPlayer p, AbstractMonster m) {
            if (LudicrousSpeedMod.plaidMode) {

                AbstractDungeon.actionManager
                        .addToBottom(new DamageAllEnemiesAction(p, spray.multiDamage, spray.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));
                AbstractDungeon.actionManager
                        .addToBottom(new DamageAllEnemiesAction(p, spray.multiDamage, spray.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));

                return SpireReturn.Return(false);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = CardGroup.class, method = "moveToExhaustPile")
    public static class ResetOnExhaustPatch {
        @SpirePostfixPatch
        public static void Postfix(CardGroup cardGroup, AbstractCard c) {
            c.resetAttributes();
        }
    }

    @SpirePatch(clz = CardGroup.class, method = "sortAlphabetically")
    public static class NoNameSortPatch {
        @SpirePrefixPatch
        public static SpireReturn Prefix(CardGroup cardGroup, boolean ascending) {
            // names get messed up
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(
            clz = ShowCardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FreeShownCardPatch {
        public static SpireReturn Prefix(ShowCardAction _instance) {
            if (LudicrousSpeedMod.plaidMode) {
                AbstractCard card = ReflectionHacks
                        .getPrivate(_instance, ShowCardAction.class, "card");

                if (AbstractDungeon.player.limbo.contains(card)) {
                    AbstractDungeon.player.limbo.removeCard(card);
                }

                AbstractDungeon.player.cardInUse = null;
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GridCardSelectScreen.class,
            paramtypez = {},
            method = "hideCards"
    )
    public static class SkipHideCardPatch {
        public static SpireReturn Prefix(GridCardSelectScreen screen) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GridCardSelectScreen.class,
            paramtypez = {},
            method = "updateCardPositionsAndHoverLogic"
    )
    public static class SkipAutoUnhoverCardPatch {
        public static SpireReturn Prefix(GridCardSelectScreen screen) {
            if (LudicrousSpeedMod.controller != null && !LudicrousSpeedMod.controller.isDone()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {boolean.class},
            method = "lighten"
    )
    public static class NoLightenCardPatch {
        public static SpireReturn Prefix(AbstractCard card, boolean something) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = SoulGroup.class, method = "discard", paramtypez = {AbstractCard.class, boolean.class})
    public static class NoSoulDiscardPatch {
        @SpirePrefixPatch
        public static SpireReturn alwaysFalse(SoulGroup soulGroup, AbstractCard card, boolean visualOnly) {
            if (LudicrousSpeedMod.plaidMode) {
                if (!visualOnly) {
                    AbstractDungeon.player.discardPile.addToTop(card);
                }

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = SoulGroup.class, method = "obtain", paramtypez = {AbstractCard.class, boolean.class})
    public static class NoSoulObtainPatch {
        @SpirePrefixPatch
        public static SpireReturn alwaysFalse(SoulGroup soulGroup, AbstractCard card, boolean obtainCard) {
            if (LudicrousSpeedMod.plaidMode) {
                if (obtainCard) {
                    AbstractDungeon.player.masterDeck.addToTop(card);
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = SoulGroup.class, method = "shuffle", paramtypez = {AbstractCard.class, boolean.class})
    public static class NoSoulshufflePatch {
        @SpirePrefixPatch
        public static SpireReturn alwaysFalse(SoulGroup soulGroup, AbstractCard card, boolean isInvisible) {
            if (LudicrousSpeedMod.plaidMode) {
                card.clearPowers();
                AbstractDungeon.player.drawPile.addToTop(card);
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = SoulGroup.class, method = "onToDeck", paramtypez = {AbstractCard.class, boolean.class, boolean.class})
    public static class NoSoulOnToDeckPatch {
        @SpirePrefixPatch
        public static SpireReturn alwaysFalse(SoulGroup soulGroup, AbstractCard card, boolean randomSpot, boolean visualOnly) {
            if (LudicrousSpeedMod.plaidMode) {
                if (!visualOnly) {
                    if (randomSpot) {
                        AbstractDungeon.player.drawPile.addToRandomSpot(card);
                    } else {
                        AbstractDungeon.player.drawPile.addToTop(card);
                    }
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = SoulGroup.class, method = "isActive")
    public static class SoulsNeverActivePatch {
        @SpirePrefixPatch
        public static SpireReturn alwaysFalse() {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(false);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = DiscardAction.class, method = "update")
    public static class DiscardFastPatch {
        @SpirePostfixPatch
        public static void setStuffAtEnd(DiscardAction action) {
            if (LudicrousSpeedMod.plaidMode) {
                if (!AbstractDungeon.isScreenUp) {
                    ReflectionHacks.setPrivate(action, AbstractGameAction.class, "duration", 0);
                    action.isDone = true;
                } else {
                    action.isDone = false;
                }
            }
        }
    }

    @SpirePatch(clz = CardModifierManager.class, method = "removeEndOfTurnModifiers")
    public static class OptimizaCardModifierManagerPatch {
        @SpirePrefixPatch
        public static SpireReturn patchedRemove(AbstractCard card) {
            Iterator it = CardModifierManager.modifiers(card).iterator();

            while (it.hasNext()) {
                AbstractCardModifier mod = (AbstractCardModifier) it.next();
                if (mod.removeAtEndOfTurn(card)) {
                    it.remove();
                    mod.onRemove(card);
                }
            }

            if (!LudicrousSpeedMod.plaidMode) {
                card.initializeDescription();
            }

            return SpireReturn.Return(null);
        }
    }

    // Turn off Image loading from the constructor, it's slow.
    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {String.class, String.class, String.class, int.class, String.class, AbstractCard.CardType.class, AbstractCard.CardColor.class, AbstractCard.CardRarity.class, AbstractCard.CardTarget.class, DamageInfo.DamageType.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class FastCardConstructorPatch {
        private static long start = 0;
        static long uuId = 0;

        @SpireInsertPatch(loc = 366)
        public static SpireReturn Insert(AbstractCard _instance, String id, String name, String imgUrl, int cost, String rawDescription, AbstractCard.CardType type, AbstractCard.CardColor color, AbstractCard.CardRarity rarity, AbstractCard.CardTarget target, DamageInfo.DamageType dType) {
            if (SaveStateMod.shouldGoFast) {
                start = System.currentTimeMillis();
                _instance.originalName = name;
                _instance.name = name;
                _instance.cardID = id;
                _instance.assetUrl = imgUrl;
                _instance.cost = cost;
                _instance.costForTurn = cost;
                _instance.rawDescription = rawDescription;
                _instance.type = type;
                _instance.color = color;
                _instance.rarity = rarity;
                _instance.target = target;
                _instance.block = -1;

                ReflectionHacks.setPrivate(_instance, AbstractCard.class, "damageType", dType);
                _instance.damageTypeForTurn = dType;

                _instance.uuid = new UUID(0, uuId++);

                addRuntime("abstract card constructor internal", System
                        .currentTimeMillis() - start);
                return SpireReturn.Return(null);

            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = CustomCard.class, method = "loadCardImage")
    public static class classNoLoadCardImagePatc {
        @SpirePrefixPatch
        public static SpireReturn doNothing(CustomCard card, String img) {
            if (shouldGoFast) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "initializeDescription")
    public static class skipInitDescription {
        @SpirePrefixPatch
        public static SpireReturn doNothing(AbstractCard card) {
            if (shouldGoFast) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
