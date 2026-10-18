package dev.hae.btt.block;

import dev.hae.btt.block_entity.BttTrapBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;









public class BttTrapBlock extends Block implements BlockEntityProvider {

    public enum Kind {

        LANTERN,

        WIRE
    }

    private final Kind kind;

    private static final VoxelShape WIRE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 4.0, 15.0);









    private static final VoxelShape LANTERN_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(3.0, 6.0, 3.0, 13.0, 16.0, 13.0),
            Block.createCuboidShape(2.0, 10.0, 2.0, 14.0, 12.0, 14.0));

    public BttTrapBlock(Kind kind, Settings settings) {
        super(settings);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return kind == Kind.LANTERN ? LANTERN_SHAPE : WIRE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return kind == Kind.LANTERN ? LANTERN_SHAPE : WIRE_SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BttTrapBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return (w, pos, st, be) -> {
            if (be instanceof BttTrapBlockEntity trap) BttTrapBlockEntity.serverTick(w, pos, st, trap);
        };
    }
}
