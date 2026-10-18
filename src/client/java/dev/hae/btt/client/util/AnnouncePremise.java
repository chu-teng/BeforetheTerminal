package dev.hae.btt.client.util;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.trait.relationship.Relationships;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.Modifier;


public final class AnnouncePremise {
    private AnnouncePremise() {}


    private static int serverKind = -1;
    private static String serverArg1 = "";
    private static String serverArg2 = "";


    public static void acceptServerLine(int kind, String arg1, String arg2) {
        serverKind = kind;
        serverArg1 = arg1 == null ? "" : arg1;
        serverArg2 = arg2 == null ? "" : arg2;
    }


    public static void clearServerLine() {
        serverKind = -1;
        serverArg1 = "";
        serverArg2 = "";
    }


    public static Text line() {

        if (serverKind >= 0) {
            return switch (serverKind) {
                case 6 -> {
                    var line = serverArg1.isEmpty() ? Text.empty() : Text.translatable("btt.trait.intro.joined", serverArg1);
                    if (!serverArg2.isEmpty()) {
                        if (!serverArg1.isEmpty()) line.append("；");
                        line.append(serverArg2);
                    }
                    yield line;
                }
                case 1 -> Text.translatable("btt.trait.intro", serverArg1);

                case 2 -> Text.translatable("btt.action.rel.lover", serverArg1)
                        .withColor(Relationships.COLOR_RELATION_TRAIT);
                case 3 -> Text.translatable("btt.action.rel.archenemy", serverArg1, serverArg2)
                        .withColor(Relationships.COLOR_RELATION_TRAIT);
                case 4 -> Text.translatable("btt.action.rel.twins", serverArg1, serverArg2)
                        .withColor(Relationships.COLOR_RELATION_TRAIT);
                case 5 -> Text.translatable("btt.action.rel.fated", serverArg1)
                        .withColor(Relationships.COLOR_RELATION_TRAIT);
                default -> Text.empty();
            };
        }
        return localLine();
    }


    private static Text localLine() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return Text.empty();
        PlayerState pc = PlayerState.KEY.get(client.player);

        PlayerEntity partner = partnerOf(client, pc.partner);
        if ("LOVER".equals(pc.relType)) {
            return Text.translatable("btt.action.rel.lover", nameOf(partner))
                    .withColor(Relationships.COLOR_RELATION_TRAIT);
        }
        if ("ARCHENEMY".equals(pc.relType)) {
            return Text.translatable("btt.action.rel.archenemy", nameOf(partner), roleNameOf(client, partner))
                    .withColor(Relationships.COLOR_RELATION_TRAIT);
        }
        if ("TWINS".equals(pc.relType)) {
            return Text.translatable("btt.action.rel.twins", nameOf(partner), roleNameOf(client, partner))
                    .withColor(Relationships.COLOR_RELATION_TRAIT);
        }
        if ("FATED".equals(pc.relType)) {
            return Text.translatable("btt.action.rel.fated", nameOf(partner))
                    .withColor(Relationships.COLOR_RELATION_TRAIT);
        }
        Modifier trait = traitOf(client);
        if (trait != null) {
            return Text.translatable("btt.trait.intro", trait.getName()).withColor(trait.color());
        }
        return Text.empty();
    }

    private static PlayerEntity partnerOf(MinecraftClient client, String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;
        try {
            return client.world.getPlayerByUuid(java.util.UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String nameOf(PlayerEntity player) {
        return player == null ? "？" : player.getName().getString();
    }


    private static String roleNameOf(MinecraftClient client, PlayerEntity partner) {
        if (partner == null) return "？";
        var role = GameWorldComponent.KEY.get(client.world).getRole(partner);
        return role == null ? "？" : Harpymodloader.getRoleName(role).getString();
    }


    private static Modifier traitOf(MinecraftClient client) {
        WorldModifierComponent comp = WorldModifierComponent.KEY.get(client.world);
        if (comp == null) return null;
        for (Modifier mod : comp.getModifiers(client.player.getUuid())) {
            if (mod == null || mod.identifier() == null) continue;
            if (mod.identifier().equals(Relationships.MOD_LOVERS.identifier())) continue;
            if (mod.identifier().equals(Relationships.MOD_ARCHENEMY.identifier())) continue;
            if (mod.identifier().equals(Relationships.MOD_TWINS.identifier())) continue;
            if (mod.identifier().equals(Relationships.MOD_FATED.identifier())) continue;
            if (!BeforeTheTerminal.MOD_ID.equals(mod.identifier().getNamespace())) continue;
            return mod;
        }
        return null;
    }
}
