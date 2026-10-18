package dev.hae.btt.registry;

import dev.doctor4t.wathe.game.GameConstants;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.item.BalloonItem;
import dev.hae.btt.item.BombItem;
import dev.hae.btt.item.DaggerItem;
import dev.hae.btt.item.FireAxeItem;
import dev.hae.btt.item.PipeItem;
import dev.hae.btt.item.PoisonGasGrenadeItem;
import dev.hae.btt.item.PoisonNeedleItem;
import dev.hae.btt.item.SwordItem;
import dev.hae.btt.item.WalkieTalkieItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;









public final class BttItems {
    private BttItems() {}


    public static void init() {
        registerCreativeTab();
        GameConstants.ITEM_COOLDOWNS.put(CAGE, 2400);
        GameConstants.ITEM_COOLDOWNS.put(DAGGER, GameConstants.getInTicks(1, 0));
        GameConstants.ITEM_COOLDOWNS.put(POISON_NEEDLE, GameConstants.getInTicks(0, 30));
        GameConstants.ITEM_COOLDOWNS.put(BOMB, GameConstants.getInTicks(0, 30));
        GameConstants.ITEM_COOLDOWNS.put(SWORD, GameConstants.getInTicks(2, 0));
        GameConstants.ITEM_COOLDOWNS.put(BALLOON, GameConstants.getInTicks(0, 30));
        GameConstants.ITEM_COOLDOWNS.put(PIPE, GameConstants.getInTicks(2, 0));


        GameConstants.ITEM_COOLDOWNS.put(FIRE_AXE, GameConstants.getInTicks(1, 0));

        GameConstants.ITEM_COOLDOWNS.put(FAKE_REVOLVER, GameConstants.getInTicks(0, 8));

        GameConstants.ITEM_COOLDOWNS.put(dev.doctor4t.wathe.index.WatheItems.DERRINGER, GameConstants.getInTicks(0, 15));
        GameConstants.ITEM_COOLDOWNS.put(REMOTE, GameConstants.getInTicks(1, 0));

        GameConstants.ITEM_COOLDOWNS.put(dev.doctor4t.wathe.index.WatheItems.LOCKPICK, GameConstants.getInTicks(2, 0));

        GameConstants.ITEM_COOLDOWNS.put(dev.doctor4t.wathe.index.WatheItems.GRENADE, GameConstants.getInTicks(2, 0));
    }






    private static void registerCreativeTab() {
        net.minecraft.registry.Registry.register(net.minecraft.registry.Registries.ITEM_GROUP,
                net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.ITEM_GROUP,
                        Identifier.of(BeforeTheTerminal.MOD_ID, "items")),
                net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
                        .icon(() -> new net.minecraft.item.ItemStack(MASTER_KEY))
                        .displayName(net.minecraft.text.Text.translatable("itemGroup.btt.items"))
                        .entries((context, entries) -> {
                            entries.add(MASTER_KEY);
                            entries.add(DAGGER);
                            entries.add(POISON_NEEDLE);
                            entries.add(BOMB);
                            entries.add(CAGE);
                            entries.add(POISON_GAS_GRENADE);
                            entries.add(WALKIE_TALKIE);
                            entries.add(SWORD);
                            entries.add(PIPE);
                            entries.add(FIRE_AXE);
                            entries.add(BALLOON);
                            entries.add(SLIP);
                            entries.add(REMOTE);
                            entries.add(dev.hae.btt.registry.BttBlocks.LANTERN_TRAP_ITEM);
                            entries.add(dev.hae.btt.registry.BttBlocks.WIRE_TRAP_ITEM);
                            entries.add(dev.hae.btt.registry.BttBlocks.CONTROL_CONSOLE_ITEM);
                            entries.add(FAKE_REVOLVER);
                            entries.add(dev.hae.btt.registry.BttBlocks.LOOKOUT_CHAIR_ITEM);
                        })
                        .build());
    }


    public static final Item MASTER_KEY = register(
            new Item(new Item.Settings().maxCount(1)),
            "master_key"
    );

    public static final Item DAGGER = register(
            new DaggerItem(new Item.Settings().maxCount(1)),
            "dagger"
    );
    public static final Item CAGE = register(new dev.hae.btt.item.CageItem(new Item.Settings().maxCount(1)), "cage");
    public static final Item CAGE_CHOICE = register(new dev.hae.btt.item.CageChoiceItem(new Item.Settings().maxCount(1)), "cage_choice");

    public static final Item POISON_NEEDLE = register(
            new PoisonNeedleItem(new Item.Settings().maxCount(1)),
            "poison_needle"
    );

    public static final Item BOMB = register(
            new BombItem(new Item.Settings().maxCount(1)),
            "bomb"
    );

    public static final Item POISON_GAS_GRENADE = register(
            new PoisonGasGrenadeItem(new Item.Settings().maxCount(16)),
            "poison_gas_grenade"
    );

    public static final Item WALKIE_TALKIE = register(
            new WalkieTalkieItem(new Item.Settings().maxCount(1)),
            "walkie_talkie"
    );

    public static final Item SWORD = register(
            new SwordItem(new Item.Settings().maxCount(1)),
            "sword"
    );

    public static final Item PIPE = register(
            new PipeItem(new Item.Settings().maxCount(1)),
            "pipe"
    );





    public static final Item FIRE_AXE = register(
            new FireAxeItem(new Item.Settings().maxCount(1)
                    .attributeModifiers(net.minecraft.item.AxeItem.createAttributeModifiers(
                            net.minecraft.item.ToolMaterials.WOOD, 0.0F, -3.0F))),
            "fire_axe"
    );








    public static final Item FAKE_REVOLVER = register(
            new dev.doctor4t.wathe.item.RevolverItem(new Item.Settings().maxCount(1)),
            "fake_revolver"
    );

    public static final Item BALLOON = register(
            new BalloonItem(new Item.Settings().maxCount(1)),
            "balloon"
    );




    public static final Item SLIP = register(
            new dev.hae.btt.item.SlipItem(new Item.Settings().maxCount(1)),
            "slip"
    );

    public static final Item REMOTE = register(
            new dev.hae.btt.item.RemoteItem(new Item.Settings().maxCount(1)),
            "remote"
    );

    public static Item register(Item item, String id) {
        Identifier itemID = Identifier.of(BeforeTheTerminal.MOD_ID, id);
        return Registry.register(Registries.ITEM, itemID, item);
    }
}
