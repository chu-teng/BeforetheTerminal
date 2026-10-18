package dev.hae.btt.mechanic;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.registry.BttItems;












public final class DaggerKnockback {
    private DaggerKnockback() {}

    private static final Identifier MODIFIER_ID = Identifier.of(BeforeTheTerminal.MOD_ID, "dagger_knockback_modifier");
    private static final EntityAttributeModifier MODIFIER = new EntityAttributeModifier(
            MODIFIER_ID, 0.5, EntityAttributeModifier.Operation.ADD_VALUE);


    public static void tick(PlayerEntity player) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        if (attribute == null) return;
        boolean dagger = player.getMainHandStack().isOf(BttItems.DAGGER)
                || player.getMainHandStack().isOf(BttItems.SWORD);
        boolean hasModifier = attribute.hasModifier(MODIFIER_ID);
        if (dagger && !hasModifier) {
            attribute.addPersistentModifier(MODIFIER);
        } else if (!dagger && hasModifier) {
            attribute.removeModifier(MODIFIER_ID);
        }
    }
}
