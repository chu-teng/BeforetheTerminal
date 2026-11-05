package dev.hae.btt.ability.shop;

import dev.hae.btt.role.Roles;

import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.List;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.mixin.game.ShopBuyMixin;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.api.event.BttEvents;
import org.jetbrains.annotations.Nullable;







public final class ShopGate {
    private ShopGate() {}








    public static List<ShopEntry> build(net.minecraft.world.World world, @Nullable PlayerEntity buyer) {
        ShopBuilder builder = new ShopBuilder(world, buyer, catalogue(world, buyer));
        BttEvents.BuildShopEntries.EVENT.invoker().onBuild(world, buyer, builder);
        List<ShopEntry> entries = builder.build();

        if (dev.hae.btt.game.GameModes.isDebate(world)) {
            entries = entries.stream().filter(e -> e.type() != ShopEntry.Type.WEAPON).toList();
        }
        return entries;
    }


    public static final List<ShopEntry> BTT_ENTRIES = List.of(
            new ShopEntry(BttItems.POISON_GAS_GRENADE.getDefaultStack(), 200, ShopEntry.Type.WEAPON),

            new ShopEntry(WatheItems.PSYCHO_MODE.getDefaultStack(), 350, ShopEntry.Type.WEAPON) {
                @Override
                public boolean onBuy(PlayerEntity player) {
                    return PlayerShopComponent.usePsychoMode(player);
                }
            },

            new ShopEntry(WatheItems.DERRINGER.getDefaultStack(), 400, ShopEntry.Type.WEAPON),
            new ShopEntry(WatheItems.POISON_VIAL.getDefaultStack(), 25, ShopEntry.Type.POISON),
            new ShopEntry(WatheItems.SCORPION.getDefaultStack(), 25, ShopEntry.Type.POISON),

            new ShopEntry(new ItemStack(BttItems.REMOTE), 100, ShopEntry.Type.POISON),
            new ShopEntry(new ItemStack(BttItems.MASTER_KEY), 50, ShopEntry.Type.TOOL),


            new ShopEntry(WatheItems.LOCKPICK.getDefaultStack(), 25, ShopEntry.Type.TOOL),
            new ShopEntry(WatheItems.BODY_BAG.getDefaultStack(), 50, ShopEntry.Type.TOOL),

            new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), 400, ShopEntry.Type.TOOL) {
                @Override
                public boolean onBuy(PlayerEntity player) {
                    return PlayerShopComponent.useBlackout(player);
                }
            }
    );

    public static boolean canAccess(dev.doctor4t.wathe.cca.GameWorldComponent game, PlayerEntity player) {
        var identity = Roles.realRole(game, player);
        if (identity == Roles.DELINQUENT || identity == Roles.MARIONETTE || identity == Roles.TRAITOR || identity == Roles.BLACKDEATH) return false;
        var role = Roles.effectiveRole(game, player);
        var kind = Roles.kindOf(role);
        return role == Roles.CHESS_PLAYER || kind != null && (kind.isKillerSeat() || kind == Roles.Kind.LONE || kind == Roles.Kind.OUTSIDER);
    }
    private static ShopEntry entry(net.minecraft.item.Item item, int price, ShopEntry.Type type) { return new ShopEntry(item.getDefaultStack(), price, type); }
    private static ShopEntry potion(net.minecraft.registry.entry.RegistryEntry<net.minecraft.potion.Potion> potion, int price) {
        return new ShopEntry(net.minecraft.component.type.PotionContentsComponent.createStack(net.minecraft.item.Items.POTION, potion), price, ShopEntry.Type.TOOL);
    }
    private static List<ShopEntry> catalogue(net.minecraft.world.World world, PlayerEntity buyer) {
        if (buyer == null) return List.of();
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
        if (!canAccess(game, buyer)) return List.of();
        var role = Roles.effectiveRole(game, buyer); var kind = Roles.kindOf(role);
        var tool = ShopEntry.Type.TOOL; var weapon = ShopEntry.Type.WEAPON; var poison = ShopEntry.Type.POISON;
        var blackout = BTT_ENTRIES.getLast();
        if (kind == Roles.Kind.LONE) return List.of(entry(BttItems.MASTER_KEY,50,tool),entry(net.minecraft.item.Items.WIND_CHARGE,150,tool),entry(net.minecraft.item.Items.ENDER_PEARL,150,tool),potion(net.minecraft.potion.Potions.NIGHT_VISION,50),potion(net.minecraft.potion.Potions.STRONG_SWIFTNESS,150),potion(net.minecraft.potion.Potions.INVISIBILITY,300));
        if (kind == Roles.Kind.OUTSIDER) return List.of(entry(WatheItems.LOCKPICK,25,tool),entry(WatheItems.CROWBAR,25,tool),entry(WatheItems.POISON_VIAL,25,poison),entry(WatheItems.SCORPION,25,poison),entry(BttItems.REMOTE,100,poison),entry(BttItems.FAKE_REVOLVER,25,tool),entry(net.minecraft.item.Items.WIND_CHARGE,150,tool),entry(net.minecraft.item.Items.ENDER_PEARL,200,tool));
        if (kind == Roles.Kind.PRINCIPAL) return List.of(entry(net.minecraft.item.Items.CROSSBOW,250,weapon),entry(WatheItems.GRENADE,350,weapon),entry(WatheItems.REVOLVER,400,weapon),entry(WatheItems.CROWBAR,25,tool),entry(BttItems.FAKE_REVOLVER,25,tool),entry(BttItems.MASTER_KEY,50,tool),entry(BttItems.MICRO_WALKIE_TALKIE,50,tool),entry(WatheItems.BODY_BAG,50,tool),potion(net.minecraft.potion.Potions.STRONG_SWIFTNESS,150),blackout);
        return BTT_ENTRIES;
    }


    public static void init() {
        dev.hae.btt.api.event.BttEvents.TaskCompleted.EVENT.register((player, task) -> {
            var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld());
            if (game.isRunning() && Roles.hasCurrentRole(game, player, Roles.CHESS_PLAYER)) PlayerShopComponent.KEY.get(player).addToBalance(50);
        });
        dev.hae.btt.api.event.BttEvents.Death.EVENT.register((victim, killer, reason) -> {
            if (killer == null || killer == victim) return;
            var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(victim.getWorld());
            if (!game.isRunning() || !canAccess(game,killer)) return;
            boolean chess = Roles.hasCurrentRole(game,killer,Roles.CHESS_PLAYER);
            if (chess) {
                var kind = Roles.kindOfReal(game,victim);
                if (kind != null && kind.isNeutralKind() && dev.hae.btt.trait.TraitRules.passengerExecution(victim,killer,reason)) PlayerShopComponent.KEY.get(killer).addToBalance(200);
            } else if (!game.canUseKillerFeatures(killer)) PlayerShopComponent.KEY.get(killer).addToBalance(100);
        });
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] fixed role shop catalogues registered");
    }
}
