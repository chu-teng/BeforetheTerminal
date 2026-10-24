package dev.hae.btt.combat;

import net.minecraft.util.Identifier;





public final class DeathReasons {
    private DeathReasons() {}





    public static final Identifier MAGIC = Identifier.of("btt", "magic");




    public static final Identifier FAILURE = Identifier.of("btt", "failure");

    public static final Identifier AXE = Identifier.of("btt", "axe");


    public static final Identifier DIGESTED = Identifier.of("btt", "digested");



    public static final Identifier LOVER_SUICIDE = Identifier.of("btt", "lover_suicide");

    public static final Identifier MARTYRDOM = Identifier.of("btt", "martyrdom");

    public static final Identifier MONK_DEIFY = Identifier.of("btt", "monk_deify");

    public static final Identifier SELF_EXECUTION = Identifier.of("btt", "self_execution");

    public static final Identifier HELIUM_SELF_DESTRUCT = Identifier.of("btt", "helium_self_destruct");

    public static final Identifier BOMB = Identifier.of("btt", "bomb");

    public static final Identifier BLOOM = Identifier.of("btt", "bloom");

    public static final Identifier COLLAPSE = Identifier.of("btt", "collapse");

    public static final Identifier FLYING_SWORD = Identifier.of("btt", "flying_sword");

    public static final Identifier JESTER_CURTAIN = Identifier.of("btt", "jester_curtain");

    public static final Identifier PIPE = Identifier.of("btt", "pipe");

    public static final Identifier ARROW = Identifier.of("btt", "arrow");

    public static final Identifier BLACKDEATH_CURTAIN = Identifier.of("btt", "blackdeath_curtain");

    public static final Identifier ESCAPE = Identifier.of("btt", "escape");

    public static final Identifier DAGGER = Identifier.of("btt", "dagger");

    public static final Identifier REVENGE = Identifier.of("btt", "revenge");




    public static final Identifier EXECUTION = Identifier.of("btt", "execution");
    public static final Identifier DISSIPATION = Identifier.of("btt", "dissipation");

    public static boolean isSuicide(Identifier reason) {
        if (reason == null) return false;
        return reason.equals(LOVER_SUICIDE) || reason.equals(MARTYRDOM) || reason.equals(BLACKDEATH_CURTAIN)
                || reason.getPath().contains("suicide") || reason.getPath().contains("self");
    }
}
