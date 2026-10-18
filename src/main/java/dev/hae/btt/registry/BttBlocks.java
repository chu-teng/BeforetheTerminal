package dev.hae.btt.registry;

import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.block.BttTrapBlock;
import dev.hae.btt.block_entity.BttTrapBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;








public final class BttBlocks {

    private BttBlocks() {}

    private static final AbstractBlock.Settings TRAP_SETTINGS = AbstractBlock.Settings.create()
            .strength(-1.0F, 3600000.0F)
            .dropsNothing()
            .sounds(BlockSoundGroup.METAL);






    private static final AbstractBlock.Settings FIXTURE_SETTINGS = AbstractBlock.Settings.create()
            .strength(-1.0F, 3600000.0F)
            .dropsNothing()
            .sounds(BlockSoundGroup.METAL)
            .nonOpaque()
            .suffocates((state, world, pos) -> false)
            .blockVision((state, world, pos) -> false);


    public static final Block LANTERN_TRAP = Registry.register(
            Registries.BLOCK,
            Identifier.of(BeforeTheTerminal.MOD_ID, "lantern_trap"),
            new BttTrapBlock(BttTrapBlock.Kind.LANTERN, FIXTURE_SETTINGS)
    );

    public static final Block WIRE_TRAP = Registry.register(
            Registries.BLOCK,
            Identifier.of(BeforeTheTerminal.MOD_ID, "wire_trap"),
            new BttTrapBlock(BttTrapBlock.Kind.WIRE, FIXTURE_SETTINGS)
    );

    public static final Block CONTROL_CONSOLE = Registry.register(
            Registries.BLOCK,
            Identifier.of(BeforeTheTerminal.MOD_ID, "control_console"),
            new dev.hae.btt.block.ControlConsoleBlock(TRAP_SETTINGS)
    );







    public static final Block LOOKOUT_CHAIR = Registry.register(
            Registries.BLOCK,
            Identifier.of(BeforeTheTerminal.MOD_ID, "lookout_chair"),
            new dev.hae.btt.block.LookoutChairBlock(AbstractBlock.Settings.create()
                    .strength(0.6F)
                    .sounds(BlockSoundGroup.WOOD)
                    .nonOpaque())
    );

    public static final Item LANTERN_TRAP_ITEM = registerItem("lantern_trap", LANTERN_TRAP);
    public static final Item WIRE_TRAP_ITEM = registerItem("wire_trap", WIRE_TRAP);
    public static final Item CONTROL_CONSOLE_ITEM = registerItem("control_console", CONTROL_CONSOLE);
    public static final Item LOOKOUT_CHAIR_ITEM = registerItem("lookout_chair", LOOKOUT_CHAIR);

    public static final BlockEntityType<BttTrapBlockEntity> TRAP_BLOCK_ENTITY =
            FabricBlockEntityTypeBuilder.create(BttTrapBlockEntity::new, LANTERN_TRAP, WIRE_TRAP).build();

    private static Item registerItem(String path, Block block) {
        return Registry.register(Registries.ITEM, Identifier.of(BeforeTheTerminal.MOD_ID, path),
                new BlockItem(block, new Item.Settings()));
    }

    public static void init() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(BeforeTheTerminal.MOD_ID, "trap"), TRAP_BLOCK_ENTITY);
    }
}
