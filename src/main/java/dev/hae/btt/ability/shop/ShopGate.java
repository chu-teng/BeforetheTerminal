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





    public static final List<String> EXCLUSIVE_POOL =


            List.of("gun", "gas", "crossbow");


    public static String drawExclusive(net.minecraft.world.World world) {

        if (dev.hae.btt.game.GameModes.isDebate(world)) return "";
        return EXCLUSIVE_POOL.get(world.getRandom().nextInt(EXCLUSIVE_POOL.size()));
    }


    public static ShopEntry exclusiveEntry(String id) {
        if (id == null) return null;
        return switch (id) {

            case "gun" -> new ShopEntry(WatheItems.REVOLVER.getDefaultStack(), 400, ShopEntry.Type.WEAPON);


            case "crossbow" -> new ShopEntry(net.minecraft.item.Items.CROSSBOW.getDefaultStack(), 400, ShopEntry.Type.WEAPON);

            case "gas" -> new ShopEntry(new ItemStack(BttItems.POISON_GAS_GRENADE), 200, ShopEntry.Type.WEAPON);
            default -> null;
        };
    }








    public static List<ShopEntry> build(net.minecraft.world.World world, @Nullable PlayerEntity buyer) {
        ShopBuilder builder = new ShopBuilder(world, buyer, BTT_ENTRIES);
        builder.add(exclusiveEntry(dev.hae.btt.component.ShopState.KEY.get(world).exclusive));
        BttEvents.BuildShopEntries.EVENT.invoker().onBuild(world, buyer, builder);
        List<ShopEntry> entries = builder.build();

        if (dev.hae.btt.game.GameModes.isDebate(world)) {
            entries = entries.stream().filter(e -> e.type() != ShopEntry.Type.WEAPON).toList();
        }
        return entries;
    }


    public static final List<ShopEntry> BTT_ENTRIES = List.of(
            new ShopEntry(WatheItems.GRENADE.getDefaultStack(), 250, ShopEntry.Type.WEAPON),

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


    public static void init() {
        BeforeTheTerminalGameMode.LOGGER.info(
                "[BTT] shop builder: base entries = {} + 1 exclusive {} (bolt + master key; crowbar off-shelf)",
                BTT_ENTRIES.size(), EXCLUSIVE_POOL);
    }
}
