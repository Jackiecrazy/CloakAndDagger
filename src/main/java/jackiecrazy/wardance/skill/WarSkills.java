package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.skill.crownchampion.CrownChampion;
import jackiecrazy.wardance.skill.descend.Descend;
import jackiecrazy.wardance.skill.descend.PhantomDive;
import jackiecrazy.wardance.skill.execution.Guillotine;
import jackiecrazy.wardance.skill.feint.Feint;
import jackiecrazy.wardance.skill.warcry.*;
import jackiecrazy.wardance.skill.grapple.Clinch;
import jackiecrazy.wardance.skill.grapple.Grapple;
import jackiecrazy.wardance.skill.grapple.Submission;
import jackiecrazy.wardance.skill.heavyblow.*;
import jackiecrazy.wardance.skill.hex.Hex;
import jackiecrazy.wardance.skill.hex.ItchyCurse;
import jackiecrazy.wardance.skill.hex.Petrify;
import jackiecrazy.wardance.skill.ironguard.*;
import jackiecrazy.wardance.skill.kick.*;
import jackiecrazy.wardance.skill.mementomori.DeathDenial;
import jackiecrazy.wardance.skill.mementomori.MementoMori;
import jackiecrazy.wardance.skill.mementomori.PoundOfFlesh;
import jackiecrazy.wardance.skill.regenspirit.*;
import jackiecrazy.wardance.skill.shieldbash.ArmLock;
import jackiecrazy.wardance.skill.shieldbash.Berserk;
import jackiecrazy.wardance.skill.shieldbash.Overbear;
import jackiecrazy.wardance.skill.shieldbash.ShieldBash;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class WarSkills {
    public static DeferredRegister<Skill> SKILLS = DeferredRegister
            .create(Skill.class, WarDance.MODID);

    public static final RegistryObject<Skill> HEAVY_BLOW = SKILLS.register("heavy_blow", HeavyBlow::new);
    public static final RegistryObject<Skill> SHATTER = SKILLS.register("shatter", Shatter::new);
    public static final RegistryObject<Skill> STAGGER = SKILLS.register("stagger", Stagger::new);
    public static final RegistryObject<Skill> POISE = SKILLS.register("poise", Poise::new);
    public static final RegistryObject<Skill> VAULT = SKILLS.register("vault", Vault::new);
    public static final RegistryObject<Skill> BACKSTAB = SKILLS.register("backstab", Silencer::new);
    public static final RegistryObject<Skill> IRON_GUARD = SKILLS.register("iron_guard", IronGuard::new);
    public static final RegistryObject<Skill> AFTERIMAGE = SKILLS.register("afterimage", Afterimage::new);
    public static final RegistryObject<Skill> BIND = SKILLS.register("bind", Bind::new);
    public static final RegistryObject<Skill> MIKIRI = SKILLS.register("mikiri", Mikiri::new);
    public static final RegistryObject<Skill> OVERPOWER = SKILLS.register("overpower", Overpower::new);
    public static final RegistryObject<Skill> RECOVERY = SKILLS.register("recovery", Recovery::new);
    public static final RegistryObject<Skill> COUP_DE_GRACE = SKILLS.register("coup_de_grace", CoupDeGrace::new);
    public static final RegistryObject<Skill> REINVIGORATE = SKILLS.register("reinvigorate", CoupDeGrace.Reinvigorate::new);
    public static final RegistryObject<Skill> DANSE_MACABRE = SKILLS.register("danse_macabre", CoupDeGrace.DanseMacabre::new);
    public static final RegistryObject<Skill> FRENZY = SKILLS.register("frenzy", CoupDeGrace.Frenzy::new);
    public static final RegistryObject<Skill> REAPING = SKILLS.register("reaping", CoupDeGrace.Reaping::new);
    public static final RegistryObject<Skill> RUPTURE = SKILLS.register("rupture", CoupDeGrace.Rupture::new);
    public static final RegistryObject<Skill> KICK = SKILLS.register("kick", Kick::new);
    public static final RegistryObject<Skill> IRON_KNEE = SKILLS.register("iron_knee", SabatonSmash::new);
    public static final RegistryObject<Skill> TACKLE = SKILLS.register("tackle", Tackle::new);
    public static final RegistryObject<Skill> BACKFLIP = SKILLS.register("backflip", Kick.Backflip::new);
    public static final RegistryObject<Skill> LOW_SWEEP = SKILLS.register("low_sweep", LowSweep::new);
    public static final RegistryObject<Skill> TRIP = SKILLS.register("trip", Trip::new);
    public static final RegistryObject<Skill> GRAPPLE = SKILLS.register("grapple", Grapple::new);
    public static final RegistryObject<Skill> CLINCH = SKILLS.register("clinch", Clinch::new);
    public static final RegistryObject<Skill> REVERSAL = SKILLS.register("reversal", Grapple.Reversal::new);
    public static final RegistryObject<Skill> SUBMISSION = SKILLS.register("submission", Submission::new);
    public static final RegistryObject<Skill> SUPLEX = SKILLS.register("suplex", Grapple.Suplex::new);
    public static final RegistryObject<Skill> SHIELD_BASH = SKILLS.register("shield_bash", ShieldBash::new);
    public static final RegistryObject<Skill> RIM_PUNCH = SKILLS.register("rim_punch", ShieldBash.RimPunch::new);
    public static final RegistryObject<Skill> ARM_LOCK = SKILLS.register("arm_lock", ArmLock::new);
    public static final RegistryObject<Skill> FOOT_SLAM = SKILLS.register("foot_slam", ShieldBash.FootSlam::new);
    public static final RegistryObject<Skill> OVERBEAR = SKILLS.register("overbear", Overbear::new);
    public static final RegistryObject<Skill> BERSERK = SKILLS.register("berserk", Berserk::new);
    public static final RegistryObject<Skill> WAR_CRY = SKILLS.register("war_cry", WarCry::new);
    public static final RegistryObject<Skill> BOULDER_BRACE = SKILLS.register("boulder_brace", BoulderBrace::new);
    public static final RegistryObject<Skill> WIND_SCAR = SKILLS.register("wind_scar", WindScar::new);
    public static final RegistryObject<Skill> FLAME_DANCE = SKILLS.register("flame_dance", FlameDance::new);
    public static final RegistryObject<Skill> FROST_FANG = SKILLS.register("frost_fang", FrostFang::new);
    public static final RegistryObject<Skill> TIMBERFALL = SKILLS.register("timberfall", Timberfall::new);
    public static final RegistryObject<Skill> CROWN_CHAMPION = SKILLS.register("crown_champion", CrownChampion::new);
    public static final RegistryObject<Skill> VENGEFUL_MIGHT = SKILLS.register("vengeful_might", CrownChampion.VengefulMight::new);
    public static final RegistryObject<Skill> HIDDEN_MIGHT = SKILLS.register("hidden_might", CrownChampion.HiddenMight::new);
    public static final RegistryObject<Skill> PRIDEFUL_MIGHT = SKILLS.register("prideful_might", CrownChampion.PridefulMight::new);
    public static final RegistryObject<Skill> ELEMENTAL_MIGHT = SKILLS.register("elemental_might", CrownChampion.ElementalMight::new);
    public static final RegistryObject<Skill> FEINT = SKILLS.register("feint", Feint::new);
    public static final RegistryObject<Skill> LAST_SURPRISE = SKILLS.register("last_surprise", Feint.LastSurprise::new);
    public static final RegistryObject<Skill> SMIRKING_SHADOW = SKILLS.register("smirking_shadow", Feint.SmirkingShadow::new);
    public static final RegistryObject<Skill> SCORPION_STING = SKILLS.register("scorpion_sting", Feint.ScorpionSting::new);
    public static final RegistryObject<Skill> UPPER_HAND = SKILLS.register("upper_hand", Feint.UpperHand::new);
    public static final RegistryObject<Skill> CAPRICIOUS_STRIKE = SKILLS.register("capricious_strike", Feint.CapriciousStrike::new);
    public static final RegistryObject<Skill> GUILLOTINE = SKILLS.register("guillotine", Guillotine::new);
    public static final RegistryObject<Skill> CROWD_PLEASER = SKILLS.register("crowd_pleaser", Guillotine.CrowdPleaser::new);
    public static final RegistryObject<Skill> LICHTENBERG_SCAR = SKILLS.register("lichtenberg_scar", Guillotine.LichtenbergScar::new);
    public static final RegistryObject<Skill> FEVER_DREAM = SKILLS.register("fever_dream", Guillotine.FeverDream::new);
    public static final RegistryObject<Skill> MASTERS_LESSON = SKILLS.register("masters_lesson", Guillotine.MastersLesson::new);
    public static final RegistryObject<Skill> BRUTALIZE = SKILLS.register("brutalize", Guillotine.Brutalize::new);
    public static final RegistryObject<Skill> MEMENTO_MORI = SKILLS.register("memento_mori", MementoMori::new);
    public static final RegistryObject<Skill> RAPID_CLOTTING = SKILLS.register("rapid_clotting", MementoMori.RapidClotting::new);
    public static final RegistryObject<Skill> PANIC = SKILLS.register("panic", MementoMori.Panic::new);
    public static final RegistryObject<Skill> DEATH_DENIAL = SKILLS.register("death_denial", DeathDenial::new);
    public static final RegistryObject<Skill> HEAL_SHOCK = SKILLS.register("heal_shock", MementoMori.HealShock::new);
    public static final RegistryObject<Skill> POUND_OF_FLESH = SKILLS.register("pound_of_flesh", PoundOfFlesh::new);
    public static final RegistryObject<Skill> DESCEND = SKILLS.register("descend", Descend::new);
    public static final RegistryObject<Skill> HAWK_DIVE = SKILLS.register("hawk_dive", PhantomDive::new);
    public static final RegistryObject<Skill> LIGHTS_OUT = SKILLS.register("lights_out", Descend.LightsOut::new);
    public static final RegistryObject<Skill> SHOCKWAVE = SKILLS.register("shockwave", Descend.Shockwave::new);
    public static final RegistryObject<Skill> TITANS_FALL = SKILLS.register("titans_fall", Descend.TitansFall::new);
    public static final RegistryObject<Skill> HEX = SKILLS.register("hex", Hex::new);
    public static final RegistryObject<Skill> ITCHY_CURSE = SKILLS.register("itchy_curse", ItchyCurse::new);
    public static final RegistryObject<Skill> UNRAVEL = SKILLS.register("unravel", Hex.Unravel::new);
    public static final RegistryObject<Skill> SNAKEBITE = SKILLS.register("snakebite", Hex.Snakebite::new);
    public static final RegistryObject<Skill> BLACK_MARK = SKILLS.register("black_mark", Hex.BlackMark::new);
    public static final RegistryObject<Skill> PETRIFY = SKILLS.register("petrify", Petrify::new);
    public static final RegistryObject<Skill> MORALE = SKILLS.register("morale", Morale::new);
    public static final RegistryObject<Skill> ARCHERS_PARADOX = SKILLS.register("archers_paradox", ArchersParadox::new);
    public static final RegistryObject<Skill> APATHY = SKILLS.register("apathy", Apathy::new);
    public static final RegistryObject<Skill> LADY_LUCK = SKILLS.register("lady_luck", LadyLuck::new);
    public static final RegistryObject<Skill> NATURAL_SPRINTER = SKILLS.register("natural_sprinter", NaturalSprinter::new);
    public static final RegistryObject<Skill> SPEED_DEMON = SKILLS.register("speed_demon", SpeedDemon::new);
    //public static final RegistryObject<Skill> WEAPON_THROW = SKILLS.register("weapon_throw", HeavyBlow::new);

}
