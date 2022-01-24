package ludicrousspeed.simulator.commands;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Panache;
import com.megacrit.cardcrawl.cards.colorless.SadisticNature;
import com.megacrit.cardcrawl.cards.curses.*;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;

import java.util.HashMap;

public class DiscardOrder {
    public static HashMap<String, Integer> uglyThing;
    public static final HashMap<String, Integer> CARD_RANKS = makeRank();

    public static HashMap<String, Integer> makeRank() {
        uglyThing = new HashMap<>();

        add(new Tactician());
        add(new Reflex());

        // non-exhausting statuses and curses
        add(new Normality());
        add(new Burn());
        add(new Pain());
        add(new Regret());
        add(new Shame());
        add(new Writhe());
        add(new Doubt());

        add(new Wound());
        add(new Slimed());
        add(new CurseOfTheBell());
        add(new Injury());
        add(new Necronomicurse());

        add(new Defend_Green());
        add(new Strike_Green());

        add(new SadisticNature());

        add(new Flechettes());
        add(new Burst());
        add(new Footwork());
        add(new Adrenaline());
        add(new Malaise());
        add(new ToolsOfTheTrade());
        add(new Choke());
        add(new AfterImage());
        add(new Caltrops());
        add(new LegSweep());
        add(new NoxiousFumes());
        add(new CripplingPoison());
        add(new Envenom());
        add(new AThousandCuts());
        add(new Nightmare());
        add(new Alchemize());
        add(new PhantasmalKiller());
        add(new Outmaneuver());
        add(new Accuracy());
        add(new CalculatedGamble());

        add(new BouncingFlask());
        add(new CorpseExplosion());
        add(new DeadlyPoison());
        add(new Catalyst());

        add(new Terror());
        add(new WellLaidPlans());

        add(new Acrobatics());

        add(new Dash());
        add(new Skewer());
        add(new RiddleWithHoles());
        add(new Neutralize());
        add(new QuickSlash());
        add(new SuckerPunch());
        add(new AllOutAttack());
        add(new Backstab());
        add(new HeelHook());
        add(new Bane());
        add(new FlyingKnee());
        add(new DaggerThrow());
        add(new DieDieDie());
        add(new Slice());
        add(new DaggerSpray());
        add(new GlassKnife());
        add(new PoisonedStab());
        add(new MasterfulStab());
        add(new Eviscerate());
        add(new Predator());
        add(new Unload());
        add(new CloakAndDagger());

        add(new StormOfSteel());
        add(new BladeDance());

        add(new Setup());
        add(new WraithForm());

        add(new Finisher());

        add(new Doppelganger());

        add(new Survivor());
        add(new Prepared());
        add(new PiercingWail());
        add(new Distraction());
        add(new EndlessAgony());
        add(new DodgeAndRoll());
        add(new Blur());
        add(new EscapePlan());
        add(new Deflect());

        add(new Concentrate());

        add(new Expertise());
        add(new BulletTime());

        add(new Panache());
        add(new Backflip());
        add(new GrandFinale());
        add(new SneakyStrike());
        add(new InfiniteBlades());
        add(new Shiv());

        return uglyThing;
    }

    private static void add(AbstractCard card) {
        uglyThing.put(card.cardID, uglyThing.size());
    }
}
