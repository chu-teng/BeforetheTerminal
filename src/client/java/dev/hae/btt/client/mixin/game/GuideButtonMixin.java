package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import dev.hae.btt.client.ui.GuideBookScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;







@Mixin(dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen.class)
public abstract class GuideButtonMixin {

    private static final int SIZE = 16;


    private int btt$iconX() {
        return 10;
    }

    private static final int ICON_Y = 10;

    @Inject(method = "render", at = @At("TAIL"))
    private void btt$guideIcon(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!((Object) this instanceof LimitedInventoryScreen)) return;
        int x = btt$iconX();

        context.drawGuiTexture(dev.doctor4t.wathe.Wathe.id("gui/shop_slot"), x - 7, ICON_Y - 7, SIZE + 14, SIZE + 14);
        context.drawItem(new ItemStack(Items.KNOWLEDGE_BOOK), x, ICON_Y);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void btt$guideClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof LimitedInventoryScreen)) return;
        int x = btt$iconX();
        if (mouseX >= x - 7 && mouseX <= x + SIZE + 7 && mouseY >= ICON_Y - 7 && mouseY <= ICON_Y + SIZE + 7) {
            MinecraftClient.getInstance().setScreen(new GuideBookScreen());
            cir.setReturnValue(true);
        }
    }
}
