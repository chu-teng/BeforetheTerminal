package dev.hae.btt.game;

import dev.hae.btt.encounter.Memory;

import dev.hae.btt.encounter.EncounterRules;

import dev.hae.btt.encounter.Backgrounds;

import dev.hae.btt.victory.Endings;

import dev.hae.btt.victory.EndingPipeline;

import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.network.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.Harpymodloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.RoleTransitions;
import dev.hae.btt.mixin.game.HornStartMixin;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.trait.relationship.Relationships;
import dev.hae.btt.role.BlackDeath;
import dev.hae.btt.role.Decoy;
import dev.hae.btt.role.Flowers;
import dev.hae.btt.role.Kidnapper;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.RoleGroups;
import dev.hae.btt.role.Roles;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.registry.BttItems;







public class BeforeTheTerminalGameMode extends GameMode {
    public static final Logger LOGGER = LoggerFactory.getLogger("btt");

    public static final Identifier ID = Identifier.of("btt", "before_the_terminal");

    public static final Identifier DRAFT_ID = Identifier.of("btt", "draft");

    public static final Identifier MEMORY_ID = Identifier.of("btt", "memory");

    public static final Identifier CUSTOM_ID = Identifier.of("btt", "custom");

    public static final Identifier MOB_ID = Identifier.of("btt", "mob");








    public enum Mode {

        SCRIPT,

        DRAFT,

        MEMORY,

        MOB,

        CUSTOM;


        public static java.util.List<Mode> displayOrder() { return java.util.List.of(SCRIPT, DRAFT, MEMORY, MOB, CUSTOM); }


        public java.util.Set<Rule> rules() {
            return switch (this) {
                case SCRIPT -> java.util.Set.of(Rule.SCRIPT, Rule.DEBATE);
                case DRAFT -> java.util.Set.of(Rule.DRAFT, Rule.DUEL);
                case MEMORY -> java.util.Set.of(Rule.MEMORY, Rule.JUDGMENT);
                case MOB -> java.util.Set.of();
                case CUSTOM -> ServerSettings.customRules();
            };
        }

        public boolean hasRule(Rule rule) { return rules().contains(rule); }
    }

    public static final int DEFAULT_START_MINUTES = 8;

    public static final int MIN_PLAYERS = 6;


    public int maxPlayers() {
        return mode == Mode.SCRIPT ? 14 : mode == Mode.DRAFT ? SeatAssignment.MAX_PLAYERS : Integer.MAX_VALUE;
    }

    public BeforeTheTerminalGameMode() {
        this(ID, Mode.SCRIPT);
    }

    public BeforeTheTerminalGameMode(Identifier id, Mode mode) {
        super(id, DEFAULT_START_MINUTES, MIN_PLAYERS);
        this.mode = mode;
    }


    public final Mode mode;

    @Override
    public void initializeGame(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> players) {
        boolean explicitMapEffect = dev.hae.btt.map.MapEnhancements.consumeExplicitEffect(world);
        var mapState = dev.hae.btt.component.MapVoteState.KEY.get(world);
        var mapEntry = dev.hae.btt.map.BttMaps.current(world);
        if (mapState.enhancements.isDefault() && mapEntry != null) {
            mapState.enhancements = mapEntry.enhancements();
            mapState.sync();
        }

        if (players.size() < SeatAssignment.MIN_PLAYERS || players.size() > maxPlayers()) {
            for (ServerPlayerEntity player : players) {
                player.sendMessage(Text.translatable("btt.start_error.player_range",
                        SeatAssignment.MIN_PLAYERS, maxPlayers() == Integer.MAX_VALUE ? "∞" : maxPlayers(), players.size()).formatted(Formatting.RED), true);
            }
            LOGGER.warn("[BTT] start refused: {} ready players, requires {}-{} ({}).",
                    players.size(), SeatAssignment.MIN_PLAYERS, maxPlayers(), mode);
            GameFunctions.stopGame(world);
            return;
        }

        gameWorld.clearRoleMap();


        gameWorld.setBackfireChancePerInnocentKill(0f);


        for (ServerPlayerEntity p : players) {
            p.getInventory().selectedSlot = 5;
            PlayerState.KEY.get(p).reset();

            dev.hae.btt.component.LetterState.KEY.get(p).reset();
        }

        ServerSettings.RoundSettings settings = ServerSettings.consume(world);
        dev.hae.btt.component.GameState roundState = dev.hae.btt.component.GameState.KEY.get(world);
        java.util.Set<Rule> rules = settings == null || settings.rules().isEmpty()
                ? mode.rules() : Rule.normalize(settings.rules());
        roundState.rules = Rule.toCsv(rules);
        roundState.sync();
        LOGGER.info("[BTT] rules this round: {} (mode={})", roundState.rules, mode);


        dev.hae.btt.game.Scripts.Script script = settings == null
                ? (rules.contains(Rule.SCRIPT) ? Scripts.draw(world.getRandom()) : null)
                : Scripts.byId(settings.script());
        roundState.script = script == null ? "" : script.id();
        roundState.sync();
        if (script != null) {
            LOGGER.info("[BTT] script this round: {} (killers={}, enforcers={})",
                    script.id(), script.killers(), script.enforcers());
        } else {
            LOGGER.info("[BTT] script this round: none (mode={})", mode);
        }

        var draft = dev.hae.btt.draft.DraftManager.consume(world);
        java.util.List<Role> fixedRoles = new java.util.ArrayList<>();
        if (draft != null) {
            fixedRoles.addAll(draft.picks().values());
            fixedRoles.addAll(draft.roster().forced().values());
        }
        dev.hae.btt.encounter.Backgrounds.set(world, mode == Mode.MOB ? ""
                : settings == null ? Backgrounds.draw(world, fixedRoles) : settings.encounter());
        LOGGER.info("[BTT] background this round: {}", roundState.background);

        dev.hae.btt.game.SeatAssignment.setOpposed(
                dev.hae.btt.encounter.Backgrounds.opposedRoles(roundState.background));
        SeatAssignment.setDuplicates(Backgrounds.DUPLICATES.equals(roundState.background));
        Map<UUID, Role> seats;
        java.util.Map<UUID, Integer> mobTeams = null;
        if (mode == Mode.MOB) {
            var mob = SeatAssignment.assignMobSeats(players.stream().map(ServerPlayerEntity::getUuid).toList());
            if (mob != null) { seats = mob.roles(); mobTeams = mob.teams(); }
            else seats = null;
            LOGGER.info("[BTT] mob result applied: {} players", players.size());
        } else if (draft != null) {
            seats = SeatAssignment.assignFromDraft(draft.roster(), draft.picks());
            LOGGER.info("[BTT] draft result applied: {} picked, {} forced",
                    draft.picks().size(), draft.roster().forced().size());
        } else {

            seats = SeatAssignment.assignSeats(
                    players.stream().map(ServerPlayerEntity::getUuid).toList(), world);
        }
        if (seats == null) {
            for (var player : players) player.sendMessage(Text.translatable("btt.start_error.role_pool"), true);
            GameFunctions.stopGame(world);
            return;
        }
        roundState.openingRoles.clear();
        seats.forEach((uuid, role) -> roundState.openingRoles.put(uuid, role.identifier().toString()));
        roundState.stormRole = "";
        roundState.reborn = "";
        roundState.traitResonance = false;
        roundState.rebirthPending = "";
        roundState.dreamUsed = false;
        roundState.dreamDeadline = 0;
        roundState.dreamVoters = "";
        roundState.dreamNo = "";

        dev.hae.btt.api.event.BttEvents.GameStarted.EVENT.invoker().onGameStarted(world);




        if (!explicitMapEffect) {
            dev.hae.btt.map.MapEnhancements.applyVisual(world,
                    dev.hae.btt.component.MapVoteState.KEY.get(world).enhancements);
        }

        dev.hae.btt.map.MapVoting.cancel(world);

        java.util.List<Relationships.Pair> relPairs = Relationships.assign(world, players);
        for (ServerPlayerEntity player : players) {
            Role role = seats.get(player.getUuid());
            gameWorld.addRole(player, role);
        }
        gameWorld.sync();
        if (mobTeams != null) {
            for (ServerPlayerEntity player : players) {
                var st = dev.hae.btt.component.PlayerState.KEY.get(player);
                st.mobTeam = mobTeams.getOrDefault(player.getUuid(), 0);
                st.campOverride = st.mobTeam == dev.hae.btt.role.Mob.PINK ? Roles.CAMP_MOB_PINK
                        : st.mobTeam == dev.hae.btt.role.Mob.AZURE ? Roles.CAMP_MOB_AZURE : "";
                st.moodOverride = "NONE";
                st.sync();
            }
        }
        Relationships.apply(world, seats, relPairs, null);
        dev.hae.btt.trait.Traits.assign(world, players);


        dev.hae.btt.map.Rooms.distribute(world, players, seats);

        for (ServerPlayerEntity p : players) {
            PlayerState roomPc = PlayerState.KEY.get(p);
            roomPc.roomX = p.getX();
            roomPc.roomY = p.getY();
            roomPc.roomZ = p.getZ();
        }
        for (ServerPlayerEntity player : players) {
            Role role = gameWorld.getRole(player);
            org.agmas.harpymodloader.events.ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, role);
            dev.hae.btt.api.event.BttEvents.RoleAssigned.EVENT.invoker().onRoleAssigned(player, role);
        }

        java.util.List<Role> assignedRoles = players.stream().map(gameWorld::getRole).toList();
        for (ServerPlayerEntity p : players) {
            Role r = gameWorld.getRole(p);
            if (r == Roles.MERCENARY) {


                giveInheritedKit(world, p, assignedRoles, cand -> Roles.kindOf(cand) == Roles.Kind.ACCOMPLICE, "从犯",
                        Roles.CAMP_PASSENGER, true);

                PlayerState.KEY.get(p).moodOverride = "REAL";
                PlayerState.KEY.get(p).sync();
            } else if (r == Roles.TRAITOR) {

                giveInheritedKit(world, p, assignedRoles, Roles::isCivilianPassenger, "无道具乘客",
                        Roles.CAMP_KILLER, false);

                PlayerState.KEY.get(p).moodOverride = "FAKE";
                PlayerState.KEY.get(p).sync();
            }
        }
        GameState btt = GameState.KEY.get(world);
        btt.misfireCount = 0;
        btt.lastExecutionAt = world.getTime();
        btt.lastEnding = "NONE";
        btt.winners = "";
        btt.clearVictoryRequests();
        btt.roleEffects.putLong("openingUntil", world.getTime() + 200 + dev.doctor4t.wathe.game.GameConstants.FADE_TIME * 2);
        for (var player : world.getPlayers()) player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.GLOWING);
        btt.epilogueType = "";
        btt.traitResonance = false;
        btt.traitEpilogueHost = "";
        btt.traitEpilogueOwner = "";
        btt.epiloguePlayed = "";
        btt.sync();

        RoundCleanup.prepareWorld(world, players);
        Backgrounds.apply(world, players);
        dev.hae.btt.trait.TraitEffects.initialize(world, players);
        RoleGroups.assignAll(world, gameWorld, players);
        if (draft != null) for (var player : players) {
            Role facade = draft.facades().get(player.getUuid());
            if (facade != null) PlayerState.KEY.get(player).decoyRole = facade.identifier().toString();
        }
        Decoy.assignAll(world, gameWorld);


        for (ServerPlayerEntity p : players) {
            Role shownRole = Decoy.displayRole(p, Roles.realRole(gameWorld, p));
            if (shownRole != null && !Memory.isBlind(p)) {
                dev.hae.btt.component.LetterState.KEY.get(p).markSelf(shownRole.identifier().getPath());
            }
        }
        BlackDeath.onRoundStart(world, players);


        for (ServerPlayerEntity p : players) {
            Role r = gameWorld.getRole(p);
            if (r == Roles.NOBLE) {
                dev.hae.btt.record.GameRecordManager.recordTalentUse(p, "noble_kins", null);
            }
        }

        if (rules.contains(Rule.DEBATE)) dev.hae.btt.game.Debate.onRoundStart(world, players);
        LOGGER.info("[BTT] opening state: time={}, camps={}", GameTimeComponent.KEY.get(world).getTime(),
                players.stream().map(p -> btt.numberOf(p.getUuid()) + ":" + gameWorld.getRole(p).identifier() + "/"
                        + Roles.campOf(gameWorld, p) + "/messiahFollower=" + PlayerState.KEY.get(p).isMessiahFollower()
                        + "/majo=" + PlayerState.KEY.get(p).isMajoRecruit()).toList());
        LOGGER.info("[BTT] round initialized: {} players seated.", players.size());




        java.util.List<ServerPlayerEntity> enforcers = new java.util.ArrayList<>();
        for (ServerPlayerEntity p : players) {
            if (Roles.kindOf(seats.get(p.getUuid())) == Roles.Kind.ENFORCER) enforcers.add(p);
        }
        if (!enforcers.isEmpty()) {
            enforcers.get(world.getRandom().nextInt(enforcers.size()))
                    .giveItemStack(new net.minecraft.item.ItemStack(dev.hae.btt.registry.BttItems.MASTER_KEY));
        }



        if (!EncounterRules.apply(world, players)) {
            LOGGER.warn("[BTT] encounter refused: no eligible roster for {}", roundState.background);
            for (var player : players) player.sendMessage(Text.translatable("btt.admin.start_conflict"), true);
            GameFunctions.stopGame(world);
            return;
        }
        for (ServerPlayerEntity p : players) {
            Role r = seats.get(p.getUuid());
            if (r == null) continue;


            if (r == Roles.TRAITOR || r == Roles.DELINQUENT || r == Roles.MARIONETTE) continue;
            if (Roles.isKillerCampFor(gameWorld, p) || r == Roles.UNDERCOVER) {
                p.giveItemStack(new net.minecraft.item.ItemStack(dev.hae.btt.registry.BttItems.WALKIE_TALKIE));
            }
        }

        int killers = 0;
        int passengers = 0;
        for (ServerPlayerEntity player : players) {
            if (Roles.isKillerCampFor(gameWorld, player)) killers++;
            else if (Roles.isPassengerCampFor(gameWorld, player)) passengers++;
        }
        for (ServerPlayerEntity player : players) {
            Role role = seats.get(player.getUuid());

            Role shown = Decoy.displayRole(player, role);



            PlayerState firstState = PlayerState.KEY.get(player);
            firstState.initialRole = role == null ? "" : role.identifier().toString();
            GameState.KEY.get(world).roleEffects.putString("initialRole:" + player.getUuidAsString(), firstState.initialRole);
            for (var item : java.util.List.of(dev.doctor4t.wathe.index.WatheItems.LOCKPICK, dev.doctor4t.wathe.index.WatheItems.CROWBAR, dev.hae.btt.registry.BttItems.REMOTE, dev.hae.btt.registry.BttItems.WALKIE_TALKIE))
                if (player.getInventory().contains(new net.minecraft.item.ItemStack(item))) GameState.KEY.get(world).roleEffects.putBoolean("initialItem:" + player.getUuidAsString() + ":" + net.minecraft.registry.Registries.ITEM.getId(item), true);
            dev.hae.btt.role.CampItems.captureInitialWeapon(player);
            firstState.sync();
            int index = Memory.isBlind(player) ? RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(dev.hae.btt.role.RoleAnnouncements.MEMORY) : announcementIndex(shown);
            ServerPlayNetworking.send(player, new AnnounceWelcomePayload(index, killers, passengers));


            ServerPlayNetworking.send(player, welcomeLine(player));
        }

        dev.hae.btt.advancement.BttAdvancements.ready(world);

    }


    private static dev.hae.btt.network.WelcomeLineS2CPacket welcomeLine(ServerPlayerEntity player) {
        PlayerState pc = PlayerState.KEY.get(player);
        java.util.UUID partnerId = dev.hae.btt.trait.relationship.Relationships.partnerOf(player);
        ServerPlayerEntity partner = partnerId == null ? null
                : (player.getServerWorld().getPlayerByUuid(partnerId) instanceof ServerPlayerEntity p ? p : null);
        String number = partnerId == null ? "？" : Integer.toString(GameState.KEY.get(player.getWorld()).numberOf(partnerId));
        String relation = switch (pc.relType) {
            case "LOVER" -> Text.translatable("btt.action.rel.lover", number).getString();
            case "ARCHENEMY" -> Text.translatable("btt.action.rel.archenemy", number, partnerRoleName(player, partner)).getString();
            case "TWINS" -> Text.translatable("btt.action.rel.twins", number, partnerRoleName(player, partner)).getString();
            case "FATED" -> Text.translatable("btt.action.rel.fated", number).getString();
            default -> "";
        };
        var comp = org.agmas.harpymodloader.component.WorldModifierComponent.KEY.get(player.getWorld());
        java.util.List<String> traits = new java.util.ArrayList<>();
        for (var mod : comp.getModifiers(player.getUuid())) {
            if (mod == null || !dev.hae.btt.BeforeTheTerminal.MOD_ID.equals(mod.identifier().getNamespace())) continue;
            if (java.util.Set.of("lovers", "archenemy", "twins", "fated").contains(mod.identifier().getPath())) continue;
            traits.add(Text.translatable("announcement.modifier." + mod.identifier().getNamespace() + "."
                    + mod.identifier().getPath()).getString());
        }
        return new dev.hae.btt.network.WelcomeLineS2CPacket(6, String.join("、", traits), relation);
    }


    private static String partnerRoleName(ServerPlayerEntity player, ServerPlayerEntity partner) {
        Role partnerRole = partner == null ? null
                : GameWorldComponent.KEY.get(player.getWorld()).getRole(partner);
        return partnerRole == null ? "？" : SeatAssignment.displayName(partnerRole).getString();
    }

    private static String nameOr(ServerPlayerEntity p) {
        return p == null ? "？" : p.getName().getString();
    }










    private static void giveInheritedKit(ServerWorld world, ServerPlayerEntity p, java.util.List<Role> assigned,
                                         java.util.function.Predicate<Role> poolFilter, String label,
                                         String campOverride, boolean stripKnife) {
        java.util.List<Role> pool = Roles.allRoles().stream()
                .filter(poolFilter)
                .filter(r -> RoleDefs.get(r) != null)
                .toList();
        java.util.List<Role> candidates = pool.stream().filter(r -> !assigned.contains(r)).toList();


        boolean fallback = candidates.isEmpty();
        if (fallback) candidates = pool;
        if (candidates.isEmpty()) {
            LOGGER.warn("[BTT] inherit skipped for {} ({}) —— 角色池为空", p.getName().getString(), label);
            return;
        }
        Role inherited = candidates.get(world.getRandom().nextInt(candidates.size()));

        RoleTransitions.replace(p, inherited, 0, campOverride, stripKnife);



        PlayerState notice = PlayerState.KEY.get(p);
        notice.openingNoticeAt = world.getTime();
        notice.openingNoticeRole = inherited.identifier().toString();
        notice.openingNoticeLabel = label;
        LOGGER.info("[BTT] inherit: {} -> {} ({}{})", p.getName().getString(),
                inherited.identifier().toString(), label, fallback ? ", 池已抽空→允许与在场角色重复" : "");
    }

    private static void startEpilogueBroadcast(String type, java.util.List<ServerPlayerEntity> players) {
        if (!"SURVIVAL".equals(type) && RoleDefs.host(type) == null) return;

        if (!players.isEmpty()) {
            dev.hae.btt.record.GameRecordManager.recordEpilogueStart(players.get(0).getServerWorld(), type);
        }
        String key;
        int color;
        switch (type) {
            case "MAJO" -> { key = "majo"; color = Roles.MAJO.color(); }
            case "MESSIAH" -> { key = "messiah"; color = Roles.MESSIAH.color(); }
            case "KIDNAPPER" -> { key = "kidnapper"; color = Roles.KIDNAPPER.color(); }
            case "GARDENER" -> { key = "gardener"; color = Roles.GARDENER.color(); }
            case "ASTRAL" -> { key = "astral"; color = Roles.ASTRAL.color(); }
            case "COLD_WAVE" -> { key = "cold_wave"; color = Roles.MESSENGER.color(); }

            default -> { key = "survival"; color = FACTION_PASSENGER_COLOR; }
        }
        var hostDef = RoleDefs.host(type);
        if (hostDef != null) {
            key = hostDef.outsiderHost().type().toLowerCase(java.util.Locale.ROOT);
            color = hostDef.role.color();
        }
        net.minecraft.sound.SoundEvent track = hostDef == null ? Sounds.forEpilogue(type) : hostDef.outsiderHost().music();
        for (ServerPlayerEntity p : players) {

            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(
                    Text.translatable("btt.epilogue." + key + ".title").withColor(color).formatted(Formatting.BOLD)));
            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(
                    Text.translatable("btt.epilogue." + key + ".line").withColor(color)));
            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 70, 10));
        }
        if (!players.isEmpty()) Sounds.requestEpilogue(players.get(0).getServerWorld(), track, 2 * 60 * 20);
    }


    public static final int FACTION_KILLER_COLOR = 0xFF0000;

    public static final int FACTION_PASSENGER_COLOR = 0x66FF00;


    private static void stopEpilogueSounds(ServerPlayerEntity p) {
        Sounds.stopEpilogue(p);
    }

    private static java.util.function.Predicate<ServerPlayerEntity> isWinnerByKiller(GameWorldComponent gwc) {
        return p -> Roles.isKillerCampFor(gwc, p);
    }

    private static java.util.function.Predicate<ServerPlayerEntity> isWinnerByInnocent(GameWorldComponent gwc) {
        return p -> Roles.isPassengerCampFor(gwc, p);
    }

    public static int announcementIndex(Role role) {
        var text = Harpymodloader.autogeneratedAnnouncements.get(role);
        if (text == null) {
            if (role == WatheRoles.VIGILANTE) text = RoleAnnouncementTexts.VIGILANTE;
            else if (role == WatheRoles.KILLER) text = RoleAnnouncementTexts.KILLER;
            else text = RoleAnnouncementTexts.CIVILIAN;
        }
        int index = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(text);
        return Math.max(index, 0);
    }

    @Override
    public void tickServerGameLoop(ServerWorld world, GameWorldComponent gameWorld) {

        dev.hae.btt.role.Godfather.tick(world);
        Flowers.tick(world);
        BlackDeath.tick(world);


        boolean roleKillerVictory = dev.hae.btt.role.RoleDefs.killerVictory(world);
        java.util.Map<java.util.UUID, dev.doctor4t.wathe.api.Role> swapSnapshot =
                dev.hae.btt.role.RoleTransitions.swapToOriginalRoles(world);
        try {
            tickServerGameLoopBody(world, gameWorld, roleKillerVictory);
        } finally {
            if (!swapSnapshot.isEmpty()
                    && gameWorld.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
                dev.hae.btt.role.RoleTransitions.swapBackToReplacedRoles(world, swapSnapshot);
            } else if (!swapSnapshot.isEmpty()) {

                gameWorld.sync();
            }
        }
    }


    private void tickServerGameLoopBody(ServerWorld world, GameWorldComponent gameWorld, boolean roleKillerVictory) {
        if (EncounterRules.tick(world)) return;
        List<ServerPlayerEntity> players = world.getPlayers();

        if (players.isEmpty()) {
            LOGGER.info("[BTT] all players left; stopping round.");
            GameFunctions.stopGame(world);
            return;
        }

        int alivePrincipals = 0;
        int aliveAccomplices = 0;
        int alivePassengers = 0;
        java.util.Set<Role> aliveOutsiderCamps = new java.util.HashSet<>();
        int aliveOutsiderNeutrals = 0;
        int aliveLone = 0;
        int aliveMessiahCamp = 0;
        int aliveTotal = 0;
    boolean majoAlive = false;

    boolean majoSideAlive = false;

    int aliveMajoRecruits = 0;

    boolean killerSeatsExist = false;
        boolean messiahAlive = false;
        boolean gardenerAlive = false;
        boolean anySeats = false;
        boolean blackdeathPresent = false;
        boolean blackdeathAlive = false;

        for (ServerPlayerEntity player : players) {
            Role role = gameWorld.getRole(player);
            if (role == null) continue;
            anySeats = true;


            Role seatRole = Roles.realRole(gameWorld, player);

            if (Roles.isKillerCamp(seatRole)) {
                killerSeatsExist = true;
            }


            if (role == Roles.BLACKDEATH) {
                blackdeathPresent = true;
                if (BlackDeath.isPresent(world, player)) {
                    blackdeathAlive = true;
                    alivePrincipals++;
                }
                continue;
            }
            if (GameFunctions.isPlayerAliveAndSurvival(player) || dev.hae.btt.role.Smuggler.hiddenAlive(player)) {
                aliveTotal++;
                if (Roles.campOf(gameWorld, player) == Roles.Faction.OUTSIDER) {
                    role = dev.hae.btt.faction.Camps.campRole(gameWorld, player);
                    if (role != Roles.MESSIAH && !PlayerState.KEY.get(player).isMessiahFollower()
                            && !PlayerState.KEY.get(player).isMajoRecruit()) aliveOutsiderCamps.add(role);
                    if (role == Roles.GARDENER) gardenerAlive = true;
                }


                if ((role == Roles.MESSIAH || PlayerState.KEY.get(player).isMessiahFollower())
                        && Roles.campOf(gameWorld, player) == Roles.Faction.OUTSIDER) {
                    aliveMessiahCamp++;
                    if (role == Roles.MESSIAH) messiahAlive = true;
                    continue;
                }




                if (PlayerState.KEY.get(player).isMajoRecruit()
                        && Roles.campOf(gameWorld, player) == Roles.Faction.OUTSIDER) {
                    majoSideAlive = true;
                    aliveMajoRecruits++;
                    continue;
                }



                if (role == Roles.MAJO && Roles.campOf(gameWorld, player) == Roles.Faction.OUTSIDER) {
                    majoAlive = true;
                    majoSideAlive = true;
                }
                var faction = dev.hae.btt.role.Roles.factionOf(role);

                String campOverride = PlayerState.KEY.get(player).campOverride;
                if (!campOverride.isEmpty()) {
                    if (Roles.CAMP_KILLER.equals(campOverride)) aliveAccomplices++;
                    else if (Roles.CAMP_PASSENGER.equals(campOverride)) alivePassengers++;
                    else if (Roles.CAMP_SOLO.equals(campOverride)) aliveLone++;
                    else if (Roles.CAMP_OUTSIDER.equals(campOverride)) aliveOutsiderNeutrals++;
                    continue;
                }


                if (faction == Roles.Faction.KILLER) {

                    if (Roles.kindOf(role) == Roles.Kind.PRINCIPAL) alivePrincipals++;
                    else aliveAccomplices++;
                } else if (faction == Roles.Faction.OUTSIDER) aliveOutsiderNeutrals++;
                else if (faction == Roles.Faction.SOLO) aliveLone++;
                else if (faction == Roles.Faction.PASSENGER) alivePassengers++;
                if (role == Roles.GARDENER) gardenerAlive = true;

            }
        }
        if (!anySeats) return;

        GameTimeComponent gameTime = GameTimeComponent.KEY.get(world);
        GameState bttState = GameState.KEY.get(world);






        boolean majoWin = majoSideAlive && alivePrincipals == 0 && aliveAccomplices == 0
                && alivePassengers == 0 && aliveMessiahCamp == 0
                && aliveOutsiderCamps.stream().allMatch(r -> r == Roles.MAJO);
        boolean messiahCampWin = aliveMessiahCamp > 0 && alivePrincipals == 0 && aliveAccomplices == 0
                && alivePassengers == 0 && aliveOutsiderNeutrals == 0 && aliveLone == 0;


        boolean soleOutsiderConditions = aliveMajoRecruits == 0 && alivePrincipals == 0 && aliveAccomplices == 0
                && alivePassengers == 0 && aliveMessiahCamp == 0 && aliveOutsiderCamps.size() == 1;
        boolean gardenerWin = gardenerAlive && soleOutsiderConditions;






        int murderers = alivePrincipals + aliveAccomplices;


        int outsidersTotal = aliveOutsiderNeutrals + aliveMessiahCamp;

        final int epilogueTicks = dev.doctor4t.wathe.game.GameConstants.getInTicks(2, 0);
        if (mode != Mode.MOB && gameTime.getTime() > epilogueTicks && murderers + outsidersTotal > 0
                && murderers + outsidersTotal > alivePassengers) {
            LOGGER.info("[BTT] epilogue compressed: time {} -> {} (killers={} outsiders={} > passengers={})",
                    gameTime.getTime(), epilogueTicks, murderers, outsidersTotal, alivePassengers);
            gameTime.setTime(epilogueTicks);
        }





        boolean onlyBlackdeathLeft = blackdeathAlive && alivePrincipals == 1 && aliveAccomplices == 0
                && alivePassengers == 0 && aliveOutsiderNeutrals == 0 && aliveLone == 0 && aliveMessiahCamp == 0;





        boolean blackdeathLastThree = blackdeathAlive && aliveTotal <= 3;
        boolean killerWinBlocked = blackdeathPresent && !onlyBlackdeathLeft && !blackdeathLastThree;

        var conductor = players.stream().filter(p -> gameWorld.getRole(p) == Roles.CONDUCTOR)
                .filter(GameFunctions::isPlayerAliveAndSurvival).filter(p -> !Drunk.isDrunk(p)).findFirst().orElse(null);
        boolean conductorAlive = conductor != null;
        var conductorEnding = conductor == null ? Endings.Ending.NONE : EndingPipeline.factionWin(conductor);

        Endings.Ending ending = Endings.Ending.NONE;
        boolean mobMode = mode == Mode.MOB;
        Endings.Ending mobEnding = mobMode ? mobEnding(world, players) : Endings.Ending.NONE;












        if (mobMode) { ending = mobEnding; } else if (blackdeathLastThree && gameTime.getTime() > 0) {




            ending = Endings.Ending.BLACKDEATH_KILLER;

        } else if (GameModes.hasRule(world, Rule.DUEL) && killerSeatsExist && alivePrincipals == 0) {

            ending = Endings.Ending.TRIAL_COMPLETE;
        } else if (majoWin) {
            ending = Endings.Ending.MAJO_WIN;
        } else if (gardenerWin) {
            ending = Endings.Ending.GARDENER_WIN;
        } else if (messiahCampWin) {
            ending = Endings.Ending.MESSIAH_WIN;
        } else if (soleOutsiderConditions && RoleDefs.host(RoleDefs.firstHost(aliveOutsiderCamps)) != null) {
            ending = RoleDefs.host(RoleDefs.firstHost(aliveOutsiderCamps)).outsiderHost().ending();
        } else if (gameTime.getTime() <= epilogueTicks) {


            Endings.Ending wipe = Endings.decide(alivePrincipals, aliveAccomplices,
                    alivePassengers, aliveOutsiderNeutrals + aliveMajoRecruits + aliveMessiahCamp, false, killerSeatsExist);
            if ((wipe == Endings.Ending.BLOOD_EXPRESS || wipe == Endings.Ending.NAKU_KORO) && killerWinBlocked) {
                wipe = Endings.Ending.NONE;
            }
            if (wipe != Endings.Ending.NONE) {
                ending = wipe;
            } else {


            String desired = majoSideAlive ? "MAJO" : messiahAlive ? "MESSIAH" : RoleDefs.firstHost(aliveOutsiderCamps);
            if (!desired.equals(bttState.epilogueType)) {
                bttState.epilogueType = desired;
                LOGGER.info("[BTT] epilogue host: {} (time={} alivePassengers={} alivePrincipals={} "
                                + "aliveAccomplices={} aliveOutsiders={} aliveLone={} aliveMessiahCamp={})",
                        desired, gameTime.getTime(), alivePassengers, alivePrincipals, aliveAccomplices,
                        aliveOutsiderNeutrals, aliveLone, aliveMessiahCamp);



                if (bttState.epiloguePlayed.contains(desired)) {
                    LOGGER.info("[BTT] epilogue host switched back to {} (already played this round) -> 不重播",
                            desired);
                } else {
                    bttState.epiloguePlayed = bttState.epiloguePlayed.isEmpty()
                            ? desired : bttState.epiloguePlayed + "," + desired;
                    startEpilogueBroadcast(desired, players);
                }
                bttState.sync();
            }



            if (alivePassengers + alivePrincipals + aliveAccomplices + aliveOutsiderNeutrals
                    + aliveLone + aliveMessiahCamp == 0) {
                gameTime.setTime(0);
            }
            if (gameTime.getTime() <= 0) {

                ending = switch (desired) {
                    case "MAJO" -> Endings.Ending.MAJO_WIN;
                    case "MESSIAH" -> Endings.Ending.MESSIAH_WIN;

                    case "GARDENER" -> Endings.Ending.GARDENER_WIN;
                    default -> {
                        var host = RoleDefs.host(desired);
                        if (host != null) yield host.outsiderHost().ending();

                        if (blackdeathLastThree) yield Endings.Ending.BLACKDEATH_KILLER;
                        Endings.Ending d = Endings.decide(alivePrincipals, aliveAccomplices,
                                alivePassengers, aliveOutsiderNeutrals + aliveMajoRecruits + aliveMessiahCamp, true, killerSeatsExist);

                        if ((d == Endings.Ending.BLOOD_EXPRESS || d == Endings.Ending.NAKU_KORO) && killerWinBlocked) {
                            d = Endings.Ending.NONE;
                        }
                        yield d;
                    }
                };
            }
            }
        } else {
            bttState.epilogueType = "";

            Endings.Ending decided = Endings.decide(alivePrincipals, aliveAccomplices,
                    alivePassengers, aliveOutsiderNeutrals + aliveMajoRecruits + aliveMessiahCamp, false, killerSeatsExist);

            if ((decided == Endings.Ending.BLOOD_EXPRESS || decided == Endings.Ending.NAKU_KORO)
                    && killerWinBlocked) {
                decided = Endings.Ending.NONE;
            }
            ending = decided;
        }





        if (roleKillerVictory && (ending == Endings.Ending.NONE
                || ending == Endings.Ending.TRIAL_COMPLETE || ending == Endings.Ending.JOURNEY_END)) {
            ending = Endings.Ending.BLOOD_EXPRESS;
        }

        var factionObjective = Endings.byName(bttState.roleEffects.getString("factionObjective"));
        ending = EndingPipeline.preferFactionEnding(ending, factionObjective);
        var factionLoss = Endings.byName(bttState.roleEffects.getString("factionLoss"));
        if (factionLoss != Endings.Ending.NONE) ending = factionLoss;


        if (ending != Endings.Ending.NONE && conductorAlive) {
            LOGGER.info("[BTT] conductor alive with {} left: {} -> {}",
                    aliveTotal, ending, conductorEnding);
            ending = conductorEnding;
        }


        if (ending != Endings.Ending.NONE && blackdeathPresent) {
            ending = switch (ending) {
                case TRIAL_COMPLETE, JOURNEY_END -> Endings.Ending.BLACKDEATH_PASSENGER;
                case BLOOD_EXPRESS, NAKU_KORO -> Endings.Ending.BLACKDEATH_KILLER;
                default -> ending;
            };
        }




        var pendingRequest = dev.hae.btt.victory.VictoryRequest.choose(bttState.pendingVictories.stream()
                .filter(request -> dev.hae.btt.victory.SoloWin.eligible(world, request)).toList());
        Endings.Ending pending = pendingRequest == null ? Endings.Ending.NONE : pendingRequest.ending();
        if (pending != Endings.Ending.NONE) {
            if (conductorAlive) {
                ending = conductorEnding;
            } else if (blackdeathLastThree) {
                ending = Endings.Ending.BLACKDEATH_KILLER;
            } else if (Endings.isRelationEnding(pending)) {

                LOGGER.info("[BTT] pending {} takes precedence (relation outranks lower tiers)", pending);
                ending = pending;
            } else {
                ending = pending;
            }

        }

        ending = EndingPipeline.rewrite(world, ending);
        if (ending != Endings.Ending.NONE && conductorAlive) ending = conductorEnding;


        final dev.doctor4t.wathe.api.Role complicityHost =
                dev.hae.btt.encounter.Backgrounds.isActive(world, dev.hae.btt.encounter.Backgrounds.COMPLICITY)
                        ? RoleDefs.hostRoleForEnding(ending) : null;
        if (complicityHost != null) ending = Endings.Ending.BLOOD_EXPRESS;
        final java.util.UUID objectiveWinner = ending == pending && pendingRequest != null
                && (isSoloEnding(ending) || ending == Endings.Ending.ARCHENEMY_WIN) ? pendingRequest.actor() : null;




        java.util.function.Predicate<ServerPlayerEntity> isWinner = switch (ending) {
            case TRIAL_COMPLETE, JOURNEY_END -> p -> Roles.isPassengerCampFor(gameWorld, p);
            case MOB_PINK_WIN -> p -> dev.hae.btt.component.PlayerState.KEY.get(p).mobTeam == dev.hae.btt.role.Mob.PINK;
            case MOB_AZURE_WIN -> p -> dev.hae.btt.component.PlayerState.KEY.get(p).mobTeam == dev.hae.btt.role.Mob.AZURE;
            case MOB_DRAW -> p -> false;
            case BLOOD_EXPRESS, NAKU_KORO -> p -> Roles.isKillerCampFor(gameWorld, p)
                    || complicityHost != null && dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, complicityHost);


            case GODFATHER_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.GODFATHER);
            case MAJO_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.MAJO);
            case MESSIAH_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.MESSIAH);
            case KIDNAPPER_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.KIDNAPPER);
            case VOODOO_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.VOODOO);
            case LEGION_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.LEGION);
            case GARDENER_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.GARDENER);
            case ASTRAL_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.ASTRAL);
            case MESSENGER_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.MESSENGER);
            case ARSONIST_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.ARSONIST);
            case ANGEL_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.ANGEL);
            case EXECUTIONER_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.EXECUTIONER);

            case THIEF_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.THIEF);
            case NOVELIST_WIN -> p -> dev.hae.btt.faction.Camps.sharesRoleCamp(gameWorld, p, Roles.NOVELIST);
            case LOVERS_WIN -> p -> {
                if (!Relationships.isLover(p)) return false;
                UUID partner = Relationships.partnerOf(p);
                if (partner == null) return false;
                ServerPlayerEntity pp = (ServerPlayerEntity) world.getPlayerByUuid(partner);
                return pp != null && GameFunctions.isPlayerAliveAndSurvival(pp);
            };

            case ARCHENEMY_WIN -> p -> {
                if (!"ARCHENEMY".equals(Relationships.typeOf(p))) return false;
                java.util.UUID archenemyTarget = Relationships.partnerOf(p);
                if (archenemyTarget == null) return false;
                ServerPlayerEntity target = (ServerPlayerEntity) world.getPlayerByUuid(archenemyTarget);
                return target != null && !GameFunctions.isPlayerAliveAndSurvival(target);
            };
            default -> p -> false;
        };
        String winners = players.stream()
                .filter(p -> gameWorld.getRole(p) != null)
                .filter(p -> objectiveWinner != null ? p.getUuid().equals(objectiveWinner) : isWinner.test(p))
                .map(p -> p.getUuid().toString()).collect(java.util.stream.Collectors.joining(","));



        GameFunctions.WinStatus ws = Endings.winStatusOf(ending);

        boolean heretic = players.stream()
                .anyMatch(p -> gameWorld.getRole(p) == Roles.HERETIC
                        && !PlayerState.KEY.get(p).isDrunk());
        if (heretic && ws != GameFunctions.WinStatus.NONE) {
            ws = Endings.flip(ws);
            ending = ws == GameFunctions.WinStatus.KILLERS
                    ? Endings.Ending.HERETIC_KILLER
                    : Endings.Ending.HERETIC_PASSENGER;
        }


        java.util.List<UUID> loverWinners = new java.util.ArrayList<>();
        for (ServerPlayerEntity p : players) {
            if (!Relationships.isLover(p) || !GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            UUID partner = Relationships.partnerOf(p);
            ServerPlayerEntity partnerPlayer = partner == null ? null : (ServerPlayerEntity) world.getPlayerByUuid(partner);
            if (partnerPlayer == null || !GameFunctions.isPlayerAliveAndSurvival(partnerPlayer)) continue;
            if (!loverWinners.contains(p.getUuid())) loverWinners.add(p.getUuid());
            if (!loverWinners.contains(partnerPlayer.getUuid())) loverWinners.add(partnerPlayer.getUuid());
        }
        java.util.function.Predicate<ServerPlayerEntity> flipWinner = ws == GameFunctions.WinStatus.KILLERS
                ? (java.util.function.Predicate<ServerPlayerEntity>) isWinnerByKiller(gameWorld)
                : isWinnerByInnocent(gameWorld);
        String finalWinners = ws == GameFunctions.WinStatus.NONE
                ? winners
                : players.stream()
                        .filter(p -> gameWorld.getRole(p) != null)
                        .filter(flipWinner)
                        .map(p -> p.getUuid().toString()).collect(java.util.stream.Collectors.joining(","));

        boolean passengerWin = ws == GameFunctions.WinStatus.PASSENGERS || ws == GameFunctions.WinStatus.TIME;
        for (ServerPlayerEntity p : players) {
            if (gameWorld.getRole(p) != Roles.HERMIT) continue;

            if (PlayerState.KEY.get(p).isDrunk()) continue;
            String uuid = p.getUuid().toString();
            boolean alive = GameFunctions.isPlayerAliveAndSurvival(p);
            if (ws == GameFunctions.WinStatus.KILLERS && alive) {
                if (!finalWinners.contains(uuid)) {
                    finalWinners = finalWinners.isEmpty() ? uuid : finalWinners + "," + uuid;
                }
            } else if (passengerWin && !alive) {
                finalWinners = java.util.Arrays.stream(finalWinners.split(","))
                        .filter(s -> !s.isEmpty() && !s.equals(uuid))
                        .collect(java.util.stream.Collectors.joining(","));
            }
        }


        String joinSide = joinableSide(ending);
        if (joinSide != null) {
            for (ServerPlayerEntity p : players) {
                if (gameWorld.getRole(p) == null) continue;


                boolean hermitJoin = gameWorld.getRole(p) == Roles.HERMIT
                        && GameFunctions.isPlayerAliveAndSurvival(p)
                        && !PlayerState.KEY.get(p).isDrunk()
                        && !"PASSENGER".equals(joinSide);
                if (!hermitJoin) continue;
                String uuid = p.getUuid().toString();
                if (!finalWinners.contains(uuid)) {
                    finalWinners = finalWinners.isEmpty() ? uuid : finalWinners + "," + uuid;
                }
            }
        }

        if (!EndingPipeline.allow(world, ending)) return;
        if (ending != Endings.Ending.NONE && gameWorld.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
            java.util.Set<String> combined = new java.util.LinkedHashSet<>(java.util.Arrays.asList(finalWinners.split(",")));
            combined.remove("");
            for (UUID lover : loverWinners) combined.add(lover.toString());
            for (ServerPlayerEntity player : players)
                if (dev.hae.btt.victory.SoloWin.deferredWinner(player)) combined.add(player.getUuidAsString());
            finalWinners = String.join(",", combined);
            LOGGER.info("[BTT] ending decided: {} (aliveP={} alivePr={} aliveAc={} aliveON={} station={})",
                    ending, alivePassengers, alivePrincipals, aliveAccomplices, aliveOutsiderNeutrals,
                    gameTime.getTime() <= 0);

            GameState btt = GameState.KEY.get(world);
            btt.lastEnding = ending.name();
            btt.winners = finalWinners;
            btt.clearVictoryRequests();
            btt.sync();

            GameRoundEndComponent.KEY.get(world).setRoundEndData(
                    players.stream().filter(p -> gameWorld.getRole(p) != null)
                            .collect(java.util.stream.Collectors.toList()), ws);
            GameFunctions.stopGame(world);
        }
    }






    private static Endings.Ending mobEnding(ServerWorld world, java.util.List<ServerPlayerEntity> players) {
        int pink = 0, azure = 0;
        for (ServerPlayerEntity player : players) {
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) continue;
            int team = dev.hae.btt.component.PlayerState.KEY.get(player).mobTeam;
            if (team == dev.hae.btt.role.Mob.PINK) pink++;
            else if (team == dev.hae.btt.role.Mob.AZURE) azure++;
        }
        if (pink == 0 && azure == 0) return Endings.Ending.MOB_DRAW;
        if (pink == 0) return Endings.Ending.MOB_AZURE_WIN;
        if (azure == 0) return Endings.Ending.MOB_PINK_WIN;
        if (GameTimeComponent.KEY.get(world).getTime() <= 0)
            return pink == azure ? Endings.Ending.MOB_DRAW
                    : pink > azure ? Endings.Ending.MOB_PINK_WIN : Endings.Ending.MOB_AZURE_WIN;
        return Endings.Ending.NONE;
    }

    private static String joinableSide(Endings.Ending ending) {
        return switch (ending) {
            case TRIAL_COMPLETE, JOURNEY_END, HERETIC_PASSENGER -> Roles.CAMP_PASSENGER;
            case BLACKDEATH_PASSENGER -> Roles.CAMP_PASSENGER;
            case BLOOD_EXPRESS, NAKU_KORO, BLACKDEATH_KILLER, HERETIC_KILLER -> Roles.CAMP_KILLER;
            case MAJO_WIN, MESSIAH_WIN, GARDENER_WIN, LEGION_WIN, VOODOO_WIN, GODFATHER_WIN -> Roles.CAMP_OUTSIDER;
            case THIEF_WIN, NOVELIST_WIN, ARSONIST_WIN, EXECUTIONER_WIN, ANGEL_WIN, KIDNAPPER_WIN, ASTRAL_WIN, MESSENGER_WIN, JESTER_WIN -> Roles.CAMP_SOLO;
            case LOVERS_WIN, ARCHENEMY_WIN -> JOIN_RELATION;
            default -> null;
        };
    }


    private static final String JOIN_RELATION = "RELATION";


    private static boolean isSoloEnding(Endings.Ending ending) {
        return switch (ending) {
            case THIEF_WIN, NOVELIST_WIN, ARSONIST_WIN, EXECUTIONER_WIN, ANGEL_WIN, KIDNAPPER_WIN, ASTRAL_WIN, MESSENGER_WIN, JESTER_WIN -> true;
            default -> false;
        };
    }

    @Override
    public void finalizeGame(ServerWorld world, GameWorldComponent gameWorld) {
        for (var player : world.getPlayers()) dev.hae.btt.role.Smuggler.releaseOwned(player);
        ServerSettings.cancel(world);
        dev.hae.btt.map.MapEnhancements.consumeExplicitEffect(world);
        if (GameModes.hasRule(world, Rule.DEBATE)) Debate.finishRound(world);

        dev.hae.btt.api.event.BttEvents.GameEnded.EVENT.invoker()
                .onGameEnded(world, GameState.KEY.get(world).lastEnding);
        RoundCleanup.clearWorldRules(world);


        GameState btt = GameState.KEY.get(world);

        btt.clearVictoryRequests();
        btt.sync();



        var scoreboard = world.getScoreboard();
        var team = scoreboard.getTeam("sacrifice");
        if (team != null) {
            for (String name : List.copyOf(team.getPlayerList())) {
                scoreboard.removeScoreHolderFromTeam(name, team);
            }
            scoreboard.removeTeam(team);
        }
        for (ServerPlayerEntity p : world.getPlayers()) {
            p.getInventory().clear();
            stopEpilogueSounds(p);
            RoundCleanup.resetPlayerState(p);
        }

        var voteState = dev.hae.btt.component.MapVoteState.KEY.get(world);
        voteState.enhancements = dev.hae.btt.map.MapEnhancements.NONE;
        voteState.sync();
        for (var player : world.getPlayers()) player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.GLOWING);
        btt.epilogueType = "";
        btt.traitResonance = false;
        btt.traitEpilogueHost = "";
        btt.traitEpilogueOwner = "";
        btt.epiloguePlayed = "";
        btt.sync();

        dev.hae.btt.draft.DraftManager.reset(world);

        dev.hae.btt.map.MapVoting.startAfterGame(world);
    }
}
