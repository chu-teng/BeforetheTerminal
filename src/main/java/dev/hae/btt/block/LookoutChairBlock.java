package dev.hae.btt.block;

import dev.doctor4t.wathe.block.HorizontalFacingMountableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;















public class LookoutChairBlock extends HorizontalFacingMountableBlock {


    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 8, 16);

    public LookoutChairBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }


    @Override
    public Vec3d getNorthFacingSitPos(World world, BlockState state, BlockPos pos) {
        return new Vec3d(0.5f, -0.55f, 0.5f);
    }
}
