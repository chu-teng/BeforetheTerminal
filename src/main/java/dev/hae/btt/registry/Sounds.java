package dev.hae.btt.registry;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;










public final class Sounds {
    private Sounds() {}

    public static final SoundEvent EPILOGUE_MAJO = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_majo"));
    public static final SoundEvent EPILOGUE_MESSIAH = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_messiah"));
    public static final SoundEvent EPILOGUE_GARDENER = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_gardener"));
    public static final SoundEvent EPILOGUE_VOODOO = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_voodoo"));
    public static final SoundEvent EPILOGUE_LEGION = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_legion"));
    public static final SoundEvent EPILOGUE_WEREWOLF = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_werewolf"));
    public static final SoundEvent EPILOGUE_SURVIVAL = SoundEvent.of(Identifier.of(BeforeTheTerminal.MOD_ID, "epilogue_survival"));

    public static final SoundEvent[] EPILOGUE_ALL = {
            EPILOGUE_MAJO, EPILOGUE_MESSIAH, EPILOGUE_GARDENER, EPILOGUE_SURVIVAL,
            EPILOGUE_VOODOO, EPILOGUE_LEGION, EPILOGUE_WEREWOLF
    };




    public static final SoundEvent POUR_GASOLINE = btt("pour_gasoline");

    public static final SoundEvent PIPE_HIT = btt("pipe_hit");
    public static final SoundEvent FIRE_AXE_HIT = btt("fire_axe_hit");
    public static final SoundEvent MEETING_BELL = btt("meeting_bell");
    public static final SoundEvent METAL_NOTICE = btt("metal_notice");

    public static final SoundEvent BOMB_BEEP = btt("bomb_beep");

    public static final SoundEvent NOVELIST_CORRECT = btt("novelist_correct");

    public static final SoundEvent FLOWER_GROW = btt("flower_grow");

    public static final SoundEvent FLOWER_BLOOM = btt("flower_bloom");

    public static final SoundEvent SWAPPER_SWAP = btt("swapper_swap");

    public static final SoundEvent RIGGER_BIND = btt("rigger_bind");

    public static final SoundEvent SCAVENGE = btt("thief_scavenge");

    public static final SoundEvent JUDGMENT = btt("judgment");

    public static final SoundEvent PUBLIC_NOTICE = btt("public_notice");
    public static final SoundEvent HARPY_SHROUD = btt("harpy_shroud");
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
            case "GARDENER" -> EPILOGUE_GARDENER;
            case "VOODOO" -> EPILOGUE_VOODOO;
            case "LEGION" -> EPILOGUE_LEGION;
            case "WEREWOLF" -> EPILOGUE_WEREWOLF;
            default -> EPILOGUE_SURVIVAL;
        };
    }
}
