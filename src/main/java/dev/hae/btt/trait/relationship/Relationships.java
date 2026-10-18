package dev.hae.btt.trait.relationship;

import dev.doctor4t.wathe.api.Role;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import dev.hae.btt.BeforeTheTerminal;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;












public final class Relationships {
    private Relationships() {}


    public static void init() {
        if (MOD_LOVERS == null || MOD_ARCHENEMY == null || MOD_TWINS == null || MOD_FATED == null) {
            throw new IllegalStateException("relationship modifiers must be registered");
        }
    }





    public static final int COLOR_LOVERS = 0xFFC0CB;
    public static final int COLOR_ARCHENEMY = 0x800080;





    public static final int COLOR_RELATION_TRAIT = 0xFFFFFF;

    public static final Modifier MOD_LOVERS = HMLModifiers.registerModifier(
            new Modifier(Identifier.of(BeforeTheTerminal.MOD_ID, "lovers"), COLOR_RELATION_TRAIT, null, null, false, false));
    public static final Modifier MOD_ARCHENEMY = HMLModifiers.registerModifier(
            new Modifier(Identifier.of(BeforeTheTerminal.MOD_ID, "archenemy"), COLOR_RELATION_TRAIT, null, null, false, false));





    public static final Modifier MOD_TWINS = HMLModifiers.registerModifier(
            new Modifier(Identifier.of(BeforeTheTerminal.MOD_ID, "twins"), COLOR_RELATION_TRAIT, null, null, false, false));




    public static final Modifier MOD_FATED = HMLModifiers.registerModifier(
            new Modifier(Identifier.of(BeforeTheTerminal.MOD_ID, "fated"), COLOR_RELATION_TRAIT, null, null, false, false));


    public static final int MAX_MODIFIERS_PER_PLAYER = dev.hae.btt.trait.Traits.SLOTS_PER_PLAYER;


    public record Pair(UUID a, UUID b, String type) {}


    public static boolean canReceiveModifier(ServerWorld world, UUID uuid) {
        var list = WorldModifierComponent.KEY.get(world).getModifiers(uuid);
        return list.size() < MAX_MODIFIERS_PER_PLAYER && list.stream().noneMatch(mod ->
                mod.identifier().equals(MOD_LOVERS.identifier()) || mod.identifier().equals(MOD_ARCHENEMY.identifier())
                        || mod.identifier().equals(MOD_TWINS.identifier()) || mod.identifier().equals(MOD_FATED.identifier()));
    }


    public static int enforceModifierCap(ServerWorld world) {
        WorldModifierComponent comp = WorldModifierComponent.KEY.get(world);
        int trimmed = 0;
        for (java.util.Map.Entry<UUID, java.util.ArrayList<Modifier>> entry : comp.getModifiers().entrySet()) {
            java.util.ArrayList<Modifier> list = entry.getValue();
            while (list.size() > MAX_MODIFIERS_PER_PLAYER) {
                list.remove(list.size() - 1);
                trimmed++;
            }
        }
        return trimmed;
    }





    public static List<Pair> assign(ServerWorld world, List<ServerPlayerEntity> players) {



        return List.of();
    }


    public static void apply(ServerWorld world, Map<UUID, Role> seats, List<Pair> pairs,
                             java.util.function.BiConsumer<UUID, Text> notify) {
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(world);


        modifiers.getModifiers().clear();
        java.util.Set<UUID> paired = new java.util.HashSet<>();
        for (Pair pair : pairs) {

            if (paired.contains(pair.a()) || paired.contains(pair.b())) {
                BeforeTheTerminalGameMode.LOGGER.warn("[BTT] relationship pair skipped (member already paired): {}", pair);
                continue;
            }
            paired.add(pair.a());
            paired.add(pair.b());
            Modifier mod = switch (pair.type()) {
                case "ARCHENEMY" -> MOD_ARCHENEMY;
                default -> MOD_LOVERS;
            };
            modifiers.addModifier(pair.a(), mod);
            modifiers.addModifier(pair.b(), mod);

            ServerPlayerEntity pa = (ServerPlayerEntity) world.getPlayerByUuid(pair.a());
            ServerPlayerEntity pb = (ServerPlayerEntity) world.getPlayerByUuid(pair.b());
            if (pa != null) {
                PlayerState c = PlayerState.KEY.get(pa);
                c.partner = pair.b().toString();
                c.relType = pair.type();
            }
            if (pb != null) {
                PlayerState c = PlayerState.KEY.get(pb);
                c.partner = pair.a().toString();
                c.relType = pair.type();
            }

            if (notify == null) continue;
            switch (pair.type()) {
                case "LOVER" -> {
                    if (pa != null) notify.accept(pair.a(), Text.translatable("btt.action.rel.lover", nameOf(pb)).withColor(COLOR_LOVERS));
                    if (pb != null) notify.accept(pair.b(), Text.translatable("btt.action.rel.lover", nameOf(pa)).withColor(COLOR_LOVERS));
                }
                case "ARCHENEMY" -> {
                    if (pa != null) notify.accept(pair.a(), Text.translatable("btt.action.rel.archenemy",
                            nameOf(pb), SeatAssignment.displayName(seats.get(pair.b())).getString()).withColor(COLOR_ARCHENEMY));
                    if (pb != null) notify.accept(pair.b(), Text.translatable("btt.action.rel.archenemy",
                            nameOf(pa), SeatAssignment.displayName(seats.get(pair.a())).getString()).withColor(COLOR_ARCHENEMY));
                }
            }
        }

        int trimmed = enforceModifierCap(world);
        if (trimmed > 0) {
            BeforeTheTerminalGameMode.LOGGER.warn(
                    "[BTT] modifier cap ({} per player) trimmed {} extra modifier(s)", MAX_MODIFIERS_PER_PLAYER, trimmed);
        }
    }

    private static String nameOf(ServerPlayerEntity p) {
        return p == null ? "？" : p.getGameProfile().getName();
    }


    public static Modifier modifierFor(String type) {
        return switch (type == null ? "" : type) {
            case "ARCHENEMY" -> MOD_ARCHENEMY;
            case "TWINS" -> MOD_TWINS;
            case "FATED" -> MOD_FATED;
            default -> MOD_LOVERS;
        };
    }


    public static void attach(ServerWorld world, ServerPlayerEntity a, ServerPlayerEntity b, String type) {
        WorldModifierComponent comp = WorldModifierComponent.KEY.get(world);
        Modifier mod = modifierFor(type);
        for (ServerPlayerEntity p : List.of(a, b)) {
            comp.addModifier(p.getUuid(), mod);
            PlayerState pc = PlayerState.KEY.get(p);
            pc.partner = (p == a ? b : a).getUuidAsString();
            pc.relType = type;
            pc.sync();
        }

        if ("TWINS".equals(type)) {
            onCampChanged(a);
            onCampChanged(b);
        }
    }




    private static int campPriority(String camp) {
        if (camp == null) return 0;
        return switch (camp) {
            case Roles.CAMP_OUTSIDER -> 4;
            case Roles.CAMP_KILLER -> 3;
            case Roles.CAMP_PASSENGER -> 2;
            case Roles.CAMP_SOLO -> 1;
            default -> 0;
        };
    }





    private static String currentCamp(PlayerEntity p, dev.doctor4t.wathe.cca.GameWorldComponent gwc) {
        return Roles.campName(Roles.campOf(gwc, p));
    }











    public static void onCampChanged(ServerPlayerEntity player) {
        if (player == null) return;
        PlayerState pc = PlayerState.KEY.get(player);
        if (!"TWINS".equals(pc.relType)) return;
        dev.doctor4t.wathe.cca.GameWorldComponent gwc =
                dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld());
        UUID partnerId = partnerOf(player);
        if (partnerId == null
                || !(player.getServerWorld().getPlayerByUuid(partnerId) instanceof ServerPlayerEntity other)) return;
        String mine = currentCamp(player, gwc);
        String theirs = currentCamp(other, gwc);
        String shared = campPriority(mine) >= campPriority(theirs) ? mine : theirs;
        if (shared == null || shared.isEmpty()) return;
        ServerPlayerEntity source = shared.equals(mine) ? player : other;
        if (!shared.equals(mine)) dev.hae.btt.faction.Camps.joinCamp(player, source);
        if (!shared.equals(theirs)) dev.hae.btt.faction.Camps.joinCamp(other, source);
    }







    public static void onDeath(ServerPlayerEntity victim, ServerWorld world) {
        PlayerState pc = PlayerState.KEY.get(victim);
        if (!"FATED".equals(pc.relType)) return;
        UUID partnerId = partnerOf(victim);
        if (partnerId == null || !(world.getPlayerByUuid(partnerId) instanceof ServerPlayerEntity survivor)) return;
        if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(survivor)) return;
        dev.doctor4t.wathe.cca.GameWorldComponent gwc = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
        Role become = Roles.isPassengerCampFor(gwc, victim) ? Roles.JESTER : Roles.MAJO;
        dev.hae.btt.role.RoleTransitions.takeOver(survivor, become);


        PlayerState.KEY.get(survivor).setCampOverride(become == Roles.MAJO ? Roles.CAMP_OUTSIDER : Roles.CAMP_SOLO);


        dev.hae.btt.record.GameRecordManager.recordCampChange(survivor,
                become == Roles.MAJO ? "MAJO" : Roles.CAMP_SOLO, null);
        survivor.sendMessage(Text.translatable("btt.action.rel.fated.become",
                SeatAssignment.displayName(become).getString()).withColor(become.color()), true);
    }



    public static UUID partnerOf(PlayerEntity player) {
        String s = player == null ? null : PlayerState.KEY.get(player).partner;
        try {
            return s == null || s.isEmpty() ? null : UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isLover(PlayerEntity player) {
        return "LOVER".equals(typeOf(player));
    }

    public static String typeOf(PlayerEntity player) {
        return player == null ? null : PlayerState.KEY.get(player).relType;
    }
}
