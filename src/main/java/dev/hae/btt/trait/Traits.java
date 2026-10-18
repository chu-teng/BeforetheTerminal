package dev.hae.btt.trait;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.trait.relationship.Relationships;
import dev.hae.btt.role.Roles;


public final class Traits {
    public static final int SLOTS_PER_PLAYER = 3;
    private Traits() {}
    private static final java.util.Map<Modifier, Roles.Faction> CAMP_RESTRICTIONS = new java.util.HashMap<>();










    private static Modifier reg(String path, int color, boolean killerOnly, boolean civilianOnly) {
        return HMLModifiers.registerModifier(
                new Modifier(Identifier.of(BeforeTheTerminal.MOD_ID, path), color, null, null, killerOnly, civilianOnly));
    }

    private static Modifier regFor(String path, Roles.Faction faction) {
        Modifier modifier = reg(path, COLOR_GENERIC, false, false);
        CAMP_RESTRICTIONS.put(modifier, faction);
        return modifier;
    }






    public static final int COLOR_GENERIC = 0xFFFFFF;
    public static final int COLOR_PASSENGER = 0xFFFFFF;
    public static final int COLOR_KILLER = 0xFFFFFF;


    public static final Modifier SLEIGHT = reg("sleight", COLOR_GENERIC, false, false);
    public static final Modifier TENACIOUS = reg("tenacious", COLOR_GENERIC, false, false);
    public static final Modifier CHILDLIKE = reg("childlike", COLOR_GENERIC, false, false);

    public static final Modifier TEASE = reg("tease", COLOR_GENERIC, false, false);
    public static final Modifier ENERGETIC = reg("energetic", COLOR_GENERIC, false, false);
    public static final Modifier LIGHT = reg("light", COLOR_GENERIC, false, false);
    public static final Modifier SENSITIVE = reg("sensitive", COLOR_GENERIC, false, false);
    public static final Modifier OBSTINATE = reg("obstinate", COLOR_GENERIC, false, false);

    public static final Modifier NIGHT_OWL = reg("night_owl", COLOR_GENERIC, false, false);
    public static final Modifier IMPATIENT = reg("impatient", COLOR_GENERIC, false, false);




    public static final Modifier JUSTICE = reg("justice", COLOR_PASSENGER, false, true);
    public static final Modifier GLUTTON = reg("glutton", COLOR_PASSENGER, false, true);
    public static final Modifier CHASTITY = reg("chastity", COLOR_PASSENGER, false, true);
    public static final Modifier HOLLOW = reg("hollow", COLOR_PASSENGER, false, true);
    public static final Modifier PURITY = reg("purity", COLOR_PASSENGER, false, true);
    public static final Modifier ENDURANCE = reg("endurance", COLOR_PASSENGER, false, true);
    public static final Modifier OPTIMIST = reg("optimist", COLOR_PASSENGER, false, true);
    public static final Modifier ANXIOUS = reg("anxious", COLOR_PASSENGER, false, true);


    public static final Modifier ANATOMY = reg("anatomy", COLOR_KILLER, true, false);
    public static final Modifier CLOSURE = reg("closure", COLOR_KILLER, true, false);
    public static final Modifier RECKLESS = reg("reckless", COLOR_KILLER, true, false);
    public static final Modifier PREMEDITATION = reg("premeditation", COLOR_KILLER, true, false);
    public static final Modifier SLACKER = reg("slacker", COLOR_KILLER, true, false);
    public static final Modifier GREED = reg("greed", COLOR_KILLER, true, false);

    public static final Modifier RESERVED = reg("reserved", COLOR_GENERIC, false, false);
    public static final Modifier ALLERGIC = reg("allergic", COLOR_GENERIC, false, false);
    public static final Modifier HOARDING = reg("hoarding", COLOR_PASSENGER, false, true);
    public static final Modifier TRUST = reg("trust", COLOR_GENERIC, false, false);
    public static final Modifier CRUSH = reg("crush", COLOR_GENERIC, false, false);
    public static final Modifier RESONANT = reg("resonant", COLOR_GENERIC, false, false);
    public static final Modifier TALKATIVE = reg("talkative", COLOR_PASSENGER, false, true);
    public static final Modifier PLAYFUL = reg("playful", COLOR_KILLER, true, false);
    public static final Modifier AFTERLIFE = regFor("afterlife", Roles.Faction.SOLO);
    public static final Modifier FAMILIAR = reg("familiar", COLOR_GENERIC, false, false);
    public static final Modifier EXCELLENT = reg("excellent", COLOR_GENERIC, false, false);
    public static final Modifier LUCKY = reg("lucky", COLOR_GENERIC, false, false);
    public static final Modifier CATCHER = regFor("catcher", Roles.Faction.SOLO);
    public static final Modifier DISGUISED = regFor("disguised", Roles.Faction.SOLO);
    public static final Modifier UPRISING = regFor("uprising", Roles.Faction.SOLO);
    public static final Modifier INSIGHT = regFor("insight", Roles.Faction.OUTSIDER);

    public static final Modifier WISH = regFor("wish", Roles.Faction.OUTSIDER);
    public static final Modifier BOND = regFor("bond", Roles.Faction.OUTSIDER);

    public static final Modifier STEALTH = reg("stealth", COLOR_KILLER, true, false);
    public static final Modifier PROFILING = regFor("profiling", Roles.Faction.OUTSIDER);


    private static final List<Modifier> IMPLEMENTED_POOL = List.of(


            LIGHT, SENSITIVE, OBSTINATE, SLEIGHT, TENACIOUS, CHILDLIKE, TEASE, ENERGETIC,
            NIGHT_OWL, IMPATIENT,

            PURITY, ENDURANCE, OPTIMIST, HOLLOW, JUSTICE, GLUTTON, CHASTITY, ANXIOUS,

            ANATOMY, CLOSURE, RECKLESS, PREMEDITATION, SLACKER, GREED,
            FAMILIAR, EXCELLENT, LUCKY, CATCHER, DISGUISED, UPRISING, INSIGHT, STEALTH,
            TRUST, CRUSH, RESONANT, TALKATIVE, PLAYFUL, AFTERLIFE, RESERVED, ALLERGIC, HOARDING, WISH, BOND, PROFILING);


    public static boolean isPassengerSide(ServerPlayerEntity p) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(p.getWorld());
        return Roles.isPassengerCampFor(gwc, p);
    }


    public static boolean isKillerSide(ServerPlayerEntity p) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(p.getWorld());
        return Roles.isKillerCampFor(gwc, p);
    }





    public static void init() {

        if (IMPLEMENTED_POOL.isEmpty()) {
            throw new IllegalStateException("trait pool must not be empty");
        }
        dev.hae.btt.api.event.BttEvents.DeathReaction.EVENT.register(TraitEffects::onDeath);


        dev.hae.btt.api.event.BttEvents.TaskCompleted.EVENT.register((player, task) -> {
            if (!has(player, SLACKER)) return;
            if (!dev.hae.btt.game.Needs.active(player).isEmpty()) return;
            dev.doctor4t.wathe.cca.PlayerShopComponent.KEY.get(player).addToBalance(SLACKER_REWARD);

        });
    }




    public static boolean has(PlayerEntity player, Modifier trait) {
        if (player == null) return false;
        WorldModifierComponent comp = WorldModifierComponent.KEY.get(player.getWorld());
        if (comp == null) return false;
        for (Modifier mod : comp.getModifiers(player.getUuid())) {
            if (mod != null && mod.identifier().equals(trait.identifier())) return true;
        }
        return false;
    }




    public static void assign(ServerWorld world, List<ServerPlayerEntity> players) {
        if (players.isEmpty()) return;
        WorldModifierComponent comp = WorldModifierComponent.KEY.get(world);
        List<Modifier> pool = new ArrayList<>(IMPLEMENTED_POOL);
        Collections.shuffle(pool);
        List<String> given = new ArrayList<>();
        java.util.Set<java.util.UUID> handed = new java.util.HashSet<>();




        java.util.Set<Modifier> forcedTraits = new java.util.HashSet<>();
        for (var entry : org.agmas.harpymodloader.Harpymodloader.FORCED_MODDED_MODIFIER.entrySet()) {
            Modifier trait = entry.getKey();
            if (!IMPLEMENTED_POOL.contains(trait)) continue;
            for (java.util.UUID uuid : entry.getValue()) {
                if (!(world.getPlayerByUuid(uuid) instanceof ServerPlayerEntity forced)) continue;
                if (comp.getModifiers(uuid).size() >= SLOTS_PER_PLAYER || has(forced, trait)) continue;
                comp.addModifier(uuid, trait);
                forcedTraits.add(trait);
                handed.add(uuid);
                given.add("forced:" + trait.identifier().getPath() + "->" + forced.getGameProfile().getName());

            }
        }







        {
            List<ServerPlayerEntity> relOrder = new ArrayList<>(players);
            Collections.shuffle(relOrder);
            GameWorldComponent gw = GameWorldComponent.KEY.get(world);
            int traits = IMPLEMENTED_POOL.size();
            for (ServerPlayerEntity drawer : relOrder) {
                if (handed.contains(drawer.getUuid())) continue;
                if (!Relationships.canReceiveModifier(world, drawer.getUuid())) continue;
                boolean drawerPassenger = Roles.isPassengerCampFor(gw, drawer);
                boolean canArchenemy = Roles.kindOf(gw.getRole(drawer)) == Roles.Kind.ACCOMPLICE;
                List<String> types = new ArrayList<>();
                types.add("LOVER");
                types.add("TWINS");
                if (canArchenemy) types.add("ARCHENEMY");
                if (btt$anyOppositeCamp(world, players, handed, drawer, gw, drawerPassenger)) types.add("FATED");

                int roll = world.getRandom().nextInt(traits + types.size());
                if (roll < traits) continue;
                String type = types.get(roll - traits);
                List<ServerPlayerEntity> partners = new ArrayList<>();
                for (ServerPlayerEntity p : players) {
                    if (p == drawer || handed.contains(p.getUuid())) continue;
                    if (!Relationships.canReceiveModifier(world, p.getUuid())) continue;
                    switch (type) {
                        case "ARCHENEMY" -> {
                            if (Roles.isInfoOrSupportPassenger(gw.getRole(p))) partners.add(p);
                        }
                        case "FATED" -> {
                            if (Roles.isPassengerCampFor(gw, p) != drawerPassenger) partners.add(p);
                        }
                        default -> partners.add(p);
                    }
                }
                if (partners.isEmpty()) continue;
                ServerPlayerEntity partner = partners.get(world.getRandom().nextInt(partners.size()));
                Relationships.attach(world, drawer, partner, type);
                for (ServerPlayerEntity m : List.of(drawer, partner)) {
                    handed.add(m.getUuid());
                    given.add("rel:" + type + "->" + m.getGameProfile().getName());
                }
            }
        }
        for (Modifier trait : pool) {
            if (forcedTraits.contains(trait)) continue;

            List<ServerPlayerEntity> candidates = new ArrayList<>();
            for (ServerPlayerEntity p : players) {
                if (handed.contains(p.getUuid())) continue;
                if (!Relationships.canReceiveModifier(world, p.getUuid())) continue;
                if (!isEligible(p, trait)) continue;
                candidates.add(p);
            }
            if (candidates.isEmpty()) {
                BeforeTheTerminalGameMode.LOGGER.info("[BTT] trait {} skipped (no eligible player)", trait.identifier().getPath());
                continue;
            }
            ServerPlayerEntity chosen = candidates.get(world.getRandom().nextInt(candidates.size()));
            comp.addModifier(chosen.getUuid(), trait);
            handed.add(chosen.getUuid());
            given.add(trait.identifier().getPath() + "->" + chosen.getGameProfile().getName());

        }
        for (ServerPlayerEntity player : players) {
            List<Modifier> eligible = IMPLEMENTED_POOL.stream()
                    .filter(trait -> isEligible(player, trait) && !has(player, trait))
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            while (comp.getModifiers(player.getUuid()).size() < SLOTS_PER_PLAYER && !eligible.isEmpty()) {
                Modifier trait = eligible.remove(world.getRandom().nextInt(eligible.size()));
                comp.addModifier(player.getUuid(), trait);
                given.add(trait.identifier().getPath() + "->" + player.getGameProfile().getName());
            }
        }
        comp.sync();
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] traits assigned ({}): {}", given.size(), given);
    }


    private static boolean btt$anyOppositeCamp(ServerWorld world, List<ServerPlayerEntity> players,
                                               java.util.Set<UUID> handed, ServerPlayerEntity drawer,
                                               GameWorldComponent gw, boolean drawerPassenger) {
        for (ServerPlayerEntity p : players) {
            if (p == drawer || handed.contains(p.getUuid())) continue;
            if (!Relationships.canReceiveModifier(world, p.getUuid())) continue;
            if (Roles.isPassengerCampFor(gw, p) != drawerPassenger) return true;
        }
        return false;
    }






    private static boolean isEligible(ServerPlayerEntity p, Modifier trait) {
        var game = GameWorldComponent.KEY.get(p.getWorld());
        var restriction = CAMP_RESTRICTIONS.get(trait);
        if (Roles.hasCurrentOrPriorRole(game, p, Roles.MARIONETTE))
            return restriction == null && !trait.killerOnly;
        if (Roles.hasCurrentOrPriorRole(game, p, Roles.LUNATIC))
            return restriction == null && !trait.civilianOnly;
        if (restriction != null) return Roles.campOf(game, p) == restriction;
        if (trait.killerOnly) return isKillerSide(p);
        if (trait.civilianOnly) return isPassengerSide(p);
        return true;
    }



    public static void tick(ServerPlayerEntity player) {
        TraitEffects.tick(player);
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        TraitStatusEffects.maintain(player, StatusEffects.SLOW_FALLING, has(player, LIGHT));

        if (has(player, ENDURANCE)) {
            GameEvents.refillSprint(player);
        }

        applyChildlikeScale(player);



        boolean wantsNightVision = has(player, NIGHT_OWL)
                || dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld())
                .isRole(player, Roles.NIGHT_WATCHMAN);
        TraitStatusEffects.maintain(player, StatusEffects.NIGHT_VISION, wantsNightVision);

        applyImpatientSpeed(player);

        tickTenacious(player);
    }



    private static final Identifier IMPATIENT_MODIFIER_ID =
            Identifier.of(BeforeTheTerminal.MOD_ID, "impatient_speed");

    private static final double IMPATIENT_SPEED_DELTA = 0.1;

    private static void applyImpatientSpeed(ServerPlayerEntity player) {
        var instance = player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (instance == null) return;
        boolean present = instance.getModifier(IMPATIENT_MODIFIER_ID) != null;
        boolean wanted = has(player, IMPATIENT);
        if (wanted && !present) {
            instance.addPersistentModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                    IMPATIENT_MODIFIER_ID, IMPATIENT_SPEED_DELTA,
                    net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else if (!wanted && present) {
            instance.removeModifier(IMPATIENT_MODIFIER_ID);
        }
    }



    private static final Identifier CHILDLIKE_MODIFIER_ID =
            Identifier.of(BeforeTheTerminal.MOD_ID, "childlike_scale");

    private static final double CHILDLIKE_SCALE_DELTA = -0.15;

    private static void applyChildlikeScale(ServerPlayerEntity player) {
        var instance = player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_SCALE);
        if (instance == null) return;
        boolean present = instance.getModifier(CHILDLIKE_MODIFIER_ID) != null;
        boolean wanted = has(player, CHILDLIKE);
        if (wanted && !present) {
            instance.addPersistentModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                    CHILDLIKE_MODIFIER_ID, CHILDLIKE_SCALE_DELTA,
                    net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
        } else if (!wanted && present) {
            instance.removeModifier(CHILDLIKE_MODIFIER_ID);
        }
    }




    public static final int TENACIOUS_DOWN_TICKS = 100;

    public static final int ELDER_DOWN_TICKS = 100;










    public static boolean vetoDeath(ServerPlayerEntity victim, Identifier reason) {
        return vetoDeath(victim, null, reason);
    }

    public static boolean vetoDeath(ServerPlayerEntity victim, net.minecraft.entity.player.PlayerEntity killer, Identifier reason) {
        PlayerState pc = PlayerState.KEY.get(victim);
        if (pc.tenaciousTicks > 0) return false;


        boolean elder = !pc.tenaciousUsed
                && dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(victim.getWorld()).isRole(victim, Roles.ELDER);
        if (pc.tenaciousUsed || (!has(victim, TENACIOUS) && !elder)) return false;
        pc.tenaciousUsed = true;
        if (elder) victim.giveItemStack(new net.minecraft.item.ItemStack(dev.doctor4t.wathe.index.WatheItems.REVOLVER));
        int downTicks = elder ? ELDER_DOWN_TICKS : TENACIOUS_DOWN_TICKS;
        pc.tenaciousTicks = downTicks;
        pc.tenaciousReason = reason == null ? "" : reason.toString();
        pc.tenaciousKiller = killer == null ? "" : killer.getUuidAsString();
        pc.fakeDead = true;
        victim.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, downTicks, 6, false, false));
        pc.sync();

        return true;
    }


    private static void tickTenacious(ServerPlayerEntity player) {
        PlayerState pc = PlayerState.KEY.get(player);
        if (pc.tenaciousTicks <= 0) return;
        if (--pc.tenaciousTicks > 0) return;
        pc.fakeDead = false;
        pc.sync();
        Identifier reason = Identifier.tryParse(pc.tenaciousReason);
        ServerPlayerEntity killer = null;
        try { killer = player.getServerWorld().getPlayerByUuid(UUID.fromString(pc.tenaciousKiller)) instanceof ServerPlayerEntity found ? found : null; }
        catch (IllegalArgumentException ignored) { }
        pc.tenaciousKiller = "";
        GameFunctions.killPlayer(player, true, killer,
                reason == null ? dev.doctor4t.wathe.game.GameConstants.DeathReasons.GENERIC : reason);
    }




    private static final int TEASE_TICKS = 1200;

    private static final java.util.Map<UUID, Integer> TEASE_LEFT = new java.util.HashMap<>();





    public static void onDeath(ServerPlayerEntity victim, ServerWorld world) {
        if (!has(victim, TEASE)) return;
        String type = pickAbsentEpilogue(world);
        if (type == null) return;
        String key = switch (type) {
            case "MAJO" -> "majo";
            case "MESSIAH" -> "messiah";
            case "KIDNAPPER" -> "kidnapper";
            case "GARDENER" -> "gardener";
            case "ASTRAL" -> "astral";
            case "COLD_WAVE" -> "cold_wave";
            default -> "cold_wave";
        };
        int color = switch (type) {
            case "MAJO" -> Roles.MAJO.color();
            case "MESSIAH" -> Roles.MESSIAH.color();
            case "KIDNAPPER" -> Roles.KIDNAPPER.color();
            case "GARDENER" -> Roles.GARDENER.color();
            case "ASTRAL" -> Roles.ASTRAL.color();
            default -> Roles.COLD_WAVE.color();
        };
        net.minecraft.sound.SoundEvent track = dev.hae.btt.registry.Sounds.forEpilogue(type);

        for (ServerPlayerEntity p : world.getPlayers()) {
            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(
                    Text.translatable("btt.epilogue." + key + ".title")
                            .withColor(color).formatted(net.minecraft.util.Formatting.BOLD)));
            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(
                    Text.translatable("btt.epilogue." + key + ".line").withColor(color)));
            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 70, 10));
            p.playSoundToPlayer(track, net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);
            TEASE_LEFT.put(p.getUuid(), TEASE_TICKS);
        }
    }


    private static String pickAbsentEpilogue(ServerWorld world) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        java.util.List<String> pool = new ArrayList<>();
        if (!anyRoleInPlay(gwc, Roles.MAJO)) pool.add("MAJO");
        if (!anyRoleInPlay(gwc, Roles.MESSIAH)) pool.add("MESSIAH");
        if (!anyRoleInPlay(gwc, Roles.KIDNAPPER)) pool.add("KIDNAPPER");
        if (!anyRoleInPlay(gwc, Roles.GARDENER)) pool.add("GARDENER");
        if (!anyRoleInPlay(gwc, Roles.ASTRAL)) pool.add("ASTRAL");
        if (!anyRoleInPlay(gwc, Roles.COLD_WAVE)) pool.add("COLD_WAVE");
        if (pool.isEmpty()) return null;
        return pool.get(world.getRandom().nextInt(pool.size()));
    }

    private static boolean anyRoleInPlay(GameWorldComponent gwc, Role role) {
        return gwc.getRoles().containsValue(role);
    }


    public static void tickTease(ServerPlayerEntity player) {
        Integer left = TEASE_LEFT.get(player.getUuid());
        if (left == null) return;
        if (left <= 1) {
            TEASE_LEFT.remove(player.getUuid());
            for (net.minecraft.sound.SoundEvent s : dev.hae.btt.registry.Sounds.EPILOGUE_ALL) {
                player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
                        s.getId(), net.minecraft.sound.SoundCategory.MASTER));
            }
            return;
        }
        TEASE_LEFT.put(player.getUuid(), left - 1);
    }


    public static void clearRoundState() {
        TEASE_LEFT.clear();
    }

    public static void clearRoundState(net.minecraft.server.world.ServerWorld world) {
        for (var player : world.getPlayers()) TEASE_LEFT.remove(player.getUuid());
    }


    public static void onDisconnect(UUID uuid) {
        if (uuid == null) return;
        TEASE_LEFT.remove(uuid);
    }




    private static final int SLACKER_REWARD = 25;









    public static void tickMood(ServerPlayerEntity player) {
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;

    }
}
