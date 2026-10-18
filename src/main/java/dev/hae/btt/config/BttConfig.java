package dev.hae.btt.config;

import dev.doctor4t.wathe.game.GameConstants;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BttConfig {
    @SerialEntry(comment = "Server next-round mode: SCRIPT / DRAFT / DEBATE")
    public String serverMode = "SCRIPT";
    @SerialEntry(comment = "Server next-round script: random or a script id")
    public String serverScript = "random";
    @SerialEntry(comment = "Server next-round encounter: random, none or an implemented encounter id")
    public String serverEncounter = "random";
    public static ConfigClassHandler<BttConfig> HANDLER = ConfigClassHandler.createBuilder(BttConfig.class)
            .id(Identifier.of(BeforeTheTerminal.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve( BeforeTheTerminal.MOD_ID + ".json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Whether insane players will randomly see people as morphed.")
    public boolean insanePlayersSeeMorphs = true;

    @SerialEntry(comment = "Allow Natural deaths to trigger curse (deaths without an assigned killer)")
    public boolean curseNonKillerDeaths = false;






    @SerialEntry(comment = "Per-role draw weight (role id -> weight). Missing = 1. 0 = never drawn. Quotas are unaffected.")
    public Map<String, Integer> roleWeights = new LinkedHashMap<>();






    @SerialEntry(comment = "Cross-round fairness rotation: OFF / LIGHT / MID / STRONG")
    public String rotationStrength = "OFF";


    @SerialEntry(comment = "Instinct key: false = hold (vanilla), true = toggle")
    public boolean instinctToggle = false;





    @SerialEntry(comment = "Draft turn time limit in seconds (auto-picks a face-up slip on timeout)")
    public int draftTurnSeconds = 10;

}
