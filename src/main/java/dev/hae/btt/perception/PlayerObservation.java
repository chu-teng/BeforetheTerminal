package dev.hae.btt.perception;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.encounter.Memory;
import dev.hae.btt.role.Roles;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.Voodoo;
import dev.hae.btt.trait.Traits;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;


public final class PlayerObservation {
    private PlayerObservation() {}
    private static final int GREEN = 0x4EDD35;
    private static final java.util.Map<ServerPlayerEntity, net.minecraft.entity.Entity> LOOKS = new java.util.HashMap<>();
    private static boolean batching;
    private static final java.util.Set<String> JESTER_TARGETS = new java.util.HashSet<>();
    private static final java.util.Set<ServerPlayerEntity> POPPY_PLAYERS = new java.util.HashSet<>();
    public static void beginBatch(net.minecraft.server.world.ServerWorld world) {
        LOOKS.clear(); JESTER_TARGETS.clear(); POPPY_PLAYERS.clear(); batching = true;
        var game = GameWorldComponent.KEY.get(world);
        for (var player : world.getPlayers()) {
            if (dev.doctor4t.wathe.cca.PlayerPsychoComponent.KEY.get(player).getPsychoTicks() > 0)
                JESTER_TARGETS.add(PlayerState.KEY.get(player).jesterExecutioner);
            if (player.isAlive() && game.isRole(player, Roles.POPPY_GROWER) && !Drunk.isDrunk(player))
                POPPY_PLAYERS.add(player);
        }
    }
    public static void endBatch() { LOOKS.clear(); JESTER_TARGETS.clear(); POPPY_PLAYERS.clear(); batching = false; }

    public static void append(NbtCompound view, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        var game = GameWorldComponent.KEY.get(target.getWorld());
        view.putBoolean("observedHideHeld", game.isRunning() && Traits.has(target, Traits.SLEIGHT));
        view.putInt("observedInstinctColor", instinctColor(game, target, viewer));
        view.putInt("observedPassiveColor", passiveColor(game, target, viewer));
        boolean chair = viewer.getVehicle() instanceof dev.doctor4t.wathe.block.entity.SeatEntity seat
                && viewer.getWorld().getBlockState(seat.getBlockPos()).getBlock() == dev.hae.btt.registry.BttBlocks.LOOKOUT_CHAIR;
        var role = game.getRole(target);
        boolean immune = (role == Roles.DEVUSHKA || role == Roles.AVIATEUR) && !Drunk.isDrunk(target);
        view.putBoolean("observedLookout", chair && game.isRunning() && !Voodoo.isRemnant(viewer)
                && GameFunctions.isPlayerAliveAndSurvival(target) && viewer.canSee(target) && !immune);
        view.putString("observedVoters", dev.hae.btt.game.Debate.isVoting(viewer.getWorld())
                && isLookingAt(target, viewer) ? dev.hae.btt.game.Debate.votersFor(viewer.getServerWorld(), target.getUuid()) : "");
        boolean looking = canInspect(target, viewer);
        view.putString("observedGroup", looking ? group(game, target, viewer) : "");
        view.putString("observedCohort", looking ? cohort(game, target, viewer) : "");
        Role inspected = looking && game.isRole(viewer, Roles.DUCE)
                ? inspectRole(game, target, viewer) : null;
        view.putString("observedInspectRole", inspected == null ? "" : inspected.identifier().toString());
    }

    public static int passiveColor(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (!game.isRunning() || Voodoo.isRemnant(viewer) || Memory.isBlind(viewer) || target.isSpectator()) return -1;
        if (batching) {
            if (JESTER_TARGETS.contains(target.getUuidAsString())) return Roles.JESTER.color();
        } else for (var player : viewer.getServerWorld().getPlayers())
            if (dev.doctor4t.wathe.cca.PlayerPsychoComponent.KEY.get(player).getPsychoTicks() > 0
                    && PlayerState.KEY.get(player).jesterExecutioner.equals(target.getUuidAsString())) return Roles.JESTER.color();
        var poison = dev.doctor4t.wathe.cca.PlayerPoisonComponent.KEY.get(target);
        if (game.isRole(viewer, Roles.PHARMACIST) && poison.poisonTicks > 0) return 0xFFFF0000;
        if (game.isRole(viewer, Roles.NIGHT_WATCHMAN)
                && dev.doctor4t.wathe.cca.PlayerPsychoComponent.KEY.get(target).getPsychoTicks() > 0)
            return Roles.NIGHT_WATCHMAN.color();
        if (game.isRole(viewer, Roles.EXECUTIONER)
                && target.getUuidAsString().equals(PlayerState.KEY.get(viewer).executionerTarget)) return 0xFFFFFF00;
        return -1;
    }

    public static int instinctColor(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (!game.isRunning() || Voodoo.isRemnant(viewer) || Memory.isBlind(viewer)) return -1;

        if (dev.hae.btt.game.GameModes.modeOf(game) == dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.MOB) {
            int viewerTeam = dev.hae.btt.role.Mob.teamOf(viewer);
            int targetTeam = dev.hae.btt.role.Mob.teamOf(target);
            if (viewerTeam == 0 || targetTeam == 0 || !GameFunctions.isPlayerAliveAndSurvival(target)) return -1;
            return viewerTeam == targetTeam ? dev.hae.btt.role.Mob.colorOf(viewerTeam) : -1;
        }
        if (NativeRoleView.fullObserver(viewer)) {
            Role role = Roles.realRole(game, target);
            return GameFunctions.isPlayerAliveAndSurvival(target) && role != null ? role.color() : -1;
        }
        if (GameFunctions.isPlayerAliveAndSurvival(viewer) && Roles.hasCurrentRole(game, viewer, Roles.PSYCHIC)
                && dev.hae.btt.role.Psychic.target(viewer) == target) {
            return Roles.PSYCHIC.color();
        }
        if (GameFunctions.isPlayerAliveAndSurvival(viewer) && GameFunctions.isPlayerAliveAndSurvival(target)
                && Roles.hasCurrentRole(game, viewer, Roles.ALCHEMIST)
                && dev.doctor4t.wathe.cca.PlayerPoisonComponent.KEY.get(target).poisonTicks > 0)
            return Roles.ALCHEMIST.color();
        if (Traits.greenInstinct(viewer)
                && GameFunctions.isPlayerAliveAndSurvival(target)) return GREEN;
        if (!Roles.isKillerCampFor(game, viewer) && dev.hae.btt.faction.Camps.sharesRoleCamp(game, viewer, Roles.GODFATHER))
            return GameFunctions.isPlayerAliveAndSurvival(target) && dev.hae.btt.faction.Camps.sameCamp(game, viewer, target)
                    ? Roles.realRole(game, target).color() : -1;
        if (game.isRole(viewer, Roles.VOODOO) && PlayerState.KEY.get(target).voodooPhase == 1)
            return Roles.VOODOO.color();
        if (!GameFunctions.isPlayerAliveAndSurvival(target)
                || Roles.hasCurrentOrPriorRole(game, viewer, Roles.DELINQUENT)
                || Roles.hasCurrentOrPriorRole(game, viewer, Roles.MARIONETTE)) return -1;
        var own = PlayerState.KEY.get(viewer);
        var subject = PlayerState.KEY.get(target);
        if (game.isRole(viewer, Roles.COMEDIAN) && subject.voicePitchTicks > 0) return Roles.COMEDIAN.color();
        boolean messiahFollower = game.isRole(viewer, Roles.MESSIAH) || own.isMessiahFollower();
        boolean majo = game.isRole(viewer, Roles.MAJO) || own.isMajoRecruit();
        if (!Roles.isKillerCampFor(game, viewer) && messiahFollower && (game.isRole(target, Roles.MESSIAH) || subject.isMessiahFollower())) return 0xFFFF00FF;
        if (!Roles.isKillerCampFor(game, viewer) && majo && (game.isRole(target, Roles.MAJO) || subject.isMajoRecruit())) return Roles.MAJO.color();
        if ((messiahFollower || majo) && !Roles.isKillerCampFor(game, viewer)) return -1;
        if (game.isRole(viewer, Roles.ARSONIST) && GameFunctions.isPlayerAliveAndSurvival(viewer)
                && subject.isDoused()) return Roles.ARSONIST.color();
        if (!GameFunctions.isPlayerAliveAndSurvival(viewer)
                || !Roles.isKillerCampFor(game, viewer) && !Roles.hasCurrentOrPriorRole(game, viewer, Roles.LUNATIC)) return -1;
        if (dev.hae.btt.game.GameModes.hasRule(viewer.getWorld(), dev.hae.btt.game.Rule.DUEL)
                && Roles.isKillerCampFor(game, viewer)
                && (Roles.realRole(game, target) == Roles.GODFATHER
                    || Roles.kindOfReal(game, target) == Roles.Kind.LONE)
                && PlayerState.KEY.get(target).joinedCampRole.equals(Roles.GODFATHER.identifier().toString()))
            return Roles.realRole(game, target).color();
        Role role = game.getRole(target);
        if (role == null) return -1;
        if ((role == Roles.DEVUSHKA || role == Roles.AVIATEUR) && !Drunk.isDrunk(target)) return -1;
        if (Roles.hasCurrentOrPriorRole(game, viewer, Roles.LUNATIC)
                && Roles.kindOfReal(game, target) == Roles.Kind.PRINCIPAL) return GREEN;
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.MARIONETTE) && Roles.isKillerCampFor(game, viewer))
            return Roles.MARIONETTE.color();
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.LUNATIC) && Roles.isKillerCampFor(game, viewer))
            return Roles.LUNATIC.color();
        if (batching) {
            if (POPPY_PLAYERS.size() > (POPPY_PLAYERS.contains(viewer) ? 1 : 0)) return GREEN;
        } else for (var other : viewer.getServerWorld().getPlayers())
            if (other != viewer && other.isAlive() && game.isRole(other, Roles.POPPY_GROWER) && !Drunk.isDrunk(other))
                return GREEN;
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.DELINQUENT) && !Roles.isKillerCampFor(game, target)) return GREEN;
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.BLACKDEATH)) return Roles.BLACKDEATH.color();
        if (Roles.isKillerCampFor(game, target))
            return 0xDD0000;
        if (Roles.campOf(game, target) == Roles.Faction.OUTSIDER && Roles.factionOf(role) != Roles.Faction.OUTSIDER)
            return 0xFF00FF;
        return role == Roles.UNDERCOVER ? 0xDD0000 : GREEN;
    }


    private static boolean canInspect(ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (viewer == target || Voodoo.isRemnant(viewer) || Memory.isBlind(viewer)) return false;
        var game = GameWorldComponent.KEY.get(viewer.getWorld());
        if (!game.isRunning() || target.getWorld() != viewer.getWorld() || PlayerState.KEY.get(target).isFakeDead()) return false;
        return isLookingAt(target, viewer);
    }

    private static boolean isLookingAt(ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (target == viewer || target.getWorld() != viewer.getWorld()) return false;
        double range = GameFunctions.isPlayerSpectatingOrCreative(viewer) ? 8 : 2;
        if (viewer.squaredDistanceTo(target) > (range + 1) * (range + 1)) return false;
        if (!batching) return lookedAt(viewer, range) == target;
        if (!LOOKS.containsKey(viewer)) LOOKS.put(viewer, lookedAt(viewer, range));
        return LOOKS.get(viewer) == target;
    }

    private static net.minecraft.entity.Entity lookedAt(ServerPlayerEntity viewer, double range) {
        var hit = ProjectileUtil.getCollision(viewer, entity -> entity instanceof PlayerEntity, range);
        return hit instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
    }

    private static String cohort(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        if (!Roles.isKillerCampFor(game, viewer) || Roles.hasCurrentOrPriorRole(game, viewer, Roles.MARIONETTE) || Roles.hasCurrentOrPriorRole(game, viewer, Roles.DELINQUENT)
                || Roles.hasCurrentOrPriorRole(game, viewer, Roles.LUNATIC)) return "";
        Role role = game.getRole(target);
        if (role == null) return "";
        if (Roles.hasCurrentOrPriorRole(game, target, Roles.MARIONETTE)) return "btt.hud.cohort.marionette";
        boolean lunatic = Roles.hasCurrentOrPriorRole(game, target, Roles.LUNATIC);
        if (role == Roles.UNDERCOVER) return "btt.hud.cohort.accomplice";
        if (!Roles.isKillerCampFor(game, target) && !lunatic) return "";
        if (lunatic) return "btt.hud.cohort.lunatic";
        if (Roles.isKillerCampFor(game, target) && !Roles.isKillerSeatFor(game, target)
                && role != Roles.BLACKDEATH) return "btt.hud.cohort.fellow";
        if (role == Roles.BLACKDEATH) return "btt.hud.cohort.black_death";
        if (Roles.kindOf(role) == Roles.Kind.PRINCIPAL) return "btt.hud.cohort.principal";
        if (Roles.kindOf(role) == Roles.Kind.ACCOMPLICE) return "btt.hud.cohort.accomplice";
        return "";
    }

    private static String group(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        var own = PlayerState.KEY.get(viewer);
        if (!game.isRole(viewer, Roles.NOBLE)) return "";
        boolean drunk = Drunk.isDrunk(viewer);
        if (dev.hae.btt.role.RoleGroups.inGroup(drunk ? own.drunkKins : own.kins, target.getUuid())) return "btt.hud.kins";
        if (dev.hae.btt.role.RoleGroups.inGroup(drunk ? own.drunkBranchKins : own.branchKins, target.getUuid())) return "btt.hud.branch_kins";
        return "";
    }

    private static Role inspectRole(GameWorldComponent game, ServerPlayerEntity target, ServerPlayerEntity viewer) {
        Role role = game.getRole(target);
        if (role == null) return null;
        if (role == Roles.HERETIC) {
            var passengers = WatheRoles.ROLES.stream().filter(r -> Roles.kindOf(r) != null && Roles.kindOf(r).isCivilian()).toList();
            role = passengers.isEmpty() ? Roles.DETECTIVE : passengers.get(Math.floorMod(target.getUuid().hashCode(), passengers.size()));
        }
        if (Drunk.isDrunk(viewer) && !WatheRoles.ROLES.isEmpty()) {
            long seed = target.getUuid().getMostSignificantBits() ^ target.getUuid().getLeastSignificantBits();
            role = WatheRoles.ROLES.get(Math.floorMod(new java.util.Random(seed).nextInt(), WatheRoles.ROLES.size()));
        }
        return role;
    }
}
