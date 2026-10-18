package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import dev.hae.btt.api.event.BttEvents;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameModes;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import dev.hae.btt.ability.shop.ShopGate;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(PlayerShopComponent.class)
public abstract class ShopBuyMixin {
    @Shadow @Final private PlayerEntity player;
    @Unique private List<ShopEntry> bttEntries;
    @Unique private ShopEntry bttEntry;
    @Unique private int bttIndex;
    @Unique private boolean bttLunatic;

    @WrapMethod(method = "tryBuy")
    private void bttPurchase(int index, Operation<Void> original) {
        var previousEntries = bttEntries;
        var previousEntry = bttEntry;
        int previousIndex = bttIndex;
        boolean previousLunatic = bttLunatic;
        try {
            bttEntries = null;
            if (!SeatAssignment.isBttMode(player.getWorld())) {
                original.call(index);
                return;
            }
            if (PlayerState.KEY.get(player).isDrunk()) {
                player.sendMessage(Text.translatable("btt.action.shop.drunk")
                        .withColor(0xFFFFFF), true);
                return;
            }
            var entries = ShopGate.build(player.getWorld(), player);
            if (index < 0 || index >= entries.size()) return;
            var entry = entries.get(index);
            if (GameModes.isDebate(player.getWorld()) && entry.type() == ShopEntry.Type.WEAPON) return;
            bttEntries = entries;
            bttEntry = entry;
            bttIndex = index;
            bttLunatic = Roles.hasCurrentOrPriorRole(GameWorldComponent.KEY.get(player.getWorld()), player, Roles.LUNATIC);
            original.call(index);
        } finally {
            bttEntries = previousEntries;
            bttEntry = previousEntry;
            bttIndex = previousIndex;
            bttLunatic = previousLunatic;
        }
    }

    @WrapOperation(method = "tryBuy", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
            target = "Ldev/doctor4t/wathe/game/GameConstants;SHOP_ENTRIES:Ljava/util/List;"))
    private List<ShopEntry> bttShopEntries(Operation<List<ShopEntry>> original) {
        return bttEntries == null ? original.call() : bttEntries;
    }

    @WrapOperation(method = "tryBuy", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/ItemCooldownManager;isCoolingDown(Lnet/minecraft/item/Item;)Z"))
    private boolean bttBodyBagCooldown(ItemCooldownManager cooldowns, Item item, Operation<Boolean> original) {
        return !(bttEntries != null && item == WatheItems.BODY_BAG) && original.call(cooldowns, item);
    }


    @Inject(method = "tryBuy", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
            target = "Ldev/doctor4t/wathe/cca/PlayerShopComponent;balance:I", ordinal = 1, shift = At.Shift.AFTER))
    private void bttAfterDebit(int index, CallbackInfo ci) {
        if (bttEntries == null) return;
        if (player instanceof ServerPlayerEntity buyer) {
            BttEvents.ShopPurchase.EVENT.invoker().onPurchase(buyer, bttEntry, bttIndex, bttEntry.price());
        }
        if (!bttLunatic) return;
        for (PlayerEntity principal : player.getWorld().getPlayers()) {
            if (Roles.kindOf(GameWorldComponent.KEY.get(player.getWorld()).getRole(principal))
                    != Roles.Kind.PRINCIPAL || !GameFunctions.isPlayerAliveAndSurvival(principal)) continue;
            PlayerShopComponent shop = PlayerShopComponent.KEY.get(principal);
            shop.balance -= bttEntry.price();
            shop.sync();
            break;
        }
    }

    @ModifyArg(method = "tryBuy", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;sendMessage(Lnet/minecraft/text/Text;Z)V"), index = 0)
    private Text bttFailureMessage(Text original) {
        return bttEntries == null ? original
                : Text.translatable("btt.action.shop.failed").formatted(Formatting.DARK_RED);
    }
}
