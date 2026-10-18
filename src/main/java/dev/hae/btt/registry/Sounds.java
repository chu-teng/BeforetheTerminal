package dev.hae.btt.registry;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;










public final class Sounds {
    private Sounds() {}

    public static final SoundEvent EPILOGUE_MAJO = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_majo"));
    public static final SoundEvent EPILOGUE_MESSIAH = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_messiah"));
    public static final SoundEvent EPILOGUE_KIDNAPPER = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_kidnapper"));
    public static final SoundEvent EPILOGUE_GARDENER = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_gardener"));
    public static final SoundEvent EPILOGUE_ASTRAL = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_astral"));
    public static final SoundEvent EPILOGUE_COLD_WAVE = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_cold_wave"));
    public static final SoundEvent EPILOGUE_SURVIVAL = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_survival"));

    public static final SoundEvent[] EPILOGUE_ALL = {
            EPILOGUE_MAJO, EPILOGUE_MESSIAH, EPILOGUE_KIDNAPPER, EPILOGUE_GARDENER, EPILOGUE_SURVIVAL,
            EPILOGUE_ASTRAL, EPILOGUE_COLD_WAVE
    };




    public static final SoundEvent POUR_GASOLINE = btt("pour_gasoline");

    public static final SoundEvent PIPE_HIT = btt("pipe_hit");

    public static final SoundEvent BOMB_BEEP = btt("bomb_beep");

    public static final SoundEvent NOVELIST_CORRECT = btt("novelist_correct");

    public static final SoundEvent FLOWER_GROW = btt("flower_grow");

    public static final SoundEvent FLOWER_BLOOM = btt("flower_bloom");

    public static final SoundEvent SWAPPER_SWAP = btt("swapper_swap");

    public static final SoundEvent RIGGER_BIND = btt("rigger_bind");

    public static final SoundEvent SCAVENGE = btt("thief_scavenge");

    public static final SoundEvent JUDGMENT = btt("judgment");

    public static final SoundEvent COLD_WAVE_CONTAMINATION = btt("cold_wave_contamination");

    public static final SoundEvent STAR_DEATH = btt("star_death");

    public static final SoundEvent UI_KEY_E = btt("ui_key_e");

    public static final SoundEvent UI_KEY_G = btt("ui_key_g");

    public static final SoundEvent MAGIC_DEATH = btt("astral_magic");

    public static final SoundEvent TRAP_LANTERN = btt("trap_lantern");

    public static final SoundEvent TRAP_WIRE = btt("trap_wire");

    private static SoundEvent btt(String name) {
        return SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "btt_" + name));
    }


    public static SoundEvent forEpilogue(String type) {
        return switch (type) {
            case "MAJO" -> EPILOGUE_MAJO;
            case "MESSIAH" -> EPILOGUE_MESSIAH;
            case "KIDNAPPER" -> EPILOGUE_KIDNAPPER;
            case "GARDENER" -> EPILOGUE_GARDENER;
            case "ASTRAL" -> EPILOGUE_ASTRAL;
            case "COLD_WAVE" -> EPILOGUE_COLD_WAVE;
            default -> EPILOGUE_SURVIVAL;
        };
    }
}
