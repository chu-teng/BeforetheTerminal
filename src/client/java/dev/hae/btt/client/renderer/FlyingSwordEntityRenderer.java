package dev.hae.btt.client.renderer;

import dev.hae.btt.entity.FlyingSwordEntity;
import dev.hae.btt.registry.BttItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;





@Environment(EnvType.CLIENT)
public class FlyingSwordEntityRenderer extends EntityRenderer<FlyingSwordEntity> {

    private final ItemRenderer itemRenderer;
    private final float scale = 1.6F;

    public FlyingSwordEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FlyingSwordEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        ItemStack itemStack = entity.getItemStack();
        if (itemStack.isEmpty()) itemStack = new ItemStack(BttItems.SWORD);

        matrices.push();
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, entity.getWorld(), null, entity.getId());

        if (!entity.isStuckInBlock()) {
            float rotation = (entity.getTicksAlive() + tickDelta) * 8.0F;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation * 0.7F));
        } else {
            switch (entity.getHitDirection()) {
                case UP -> {
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
                    matrices.translate(0.0F, -0.35F, 0.0F);
                }
                case DOWN -> matrices.translate(0.0F, -0.35F, 0.0F);
                case NORTH -> {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
                    matrices.translate(0.0F, 0.0F, 0.35F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-50.0F));
                }
                case WEST -> {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0F));
                    matrices.translate(0.0F, 0.0F, 0.35F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-50.0F));
                }
                case EAST -> {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
                    matrices.translate(0.0F, 0.0F, 0.35F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-50.0F));
                }
                default -> {
                    matrices.translate(0.0F, 0.0F, 0.35F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-50.0F));
                }
            }
        }

        matrices.scale(this.scale, this.scale, this.scale);
        this.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrices,
                vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(FlyingSwordEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
