package dev.hae.btt.ability.shop;

import dev.hae.btt.role.Roles;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;















public final class ShopBuilder {
    private final World world;
    private final @Nullable PlayerEntity buyer;
    private final List<ShopEntry> entries;

    ShopBuilder(World world, @Nullable PlayerEntity buyer, List<ShopEntry> base) {
        this.world = world;
        this.buyer = buyer;
        this.entries = new ArrayList<>(base);
    }


    public World world() {
        return this.world;
    }


    public @Nullable PlayerEntity buyer() {
        return this.buyer;
    }





    public boolean hasCurrentRole(Role role) {
        if (this.buyer == null || role == null) return false;
        return Roles.hasCurrentRole(GameWorldComponent.KEY.get(this.world), this.buyer, role);
    }


    public boolean isPlayingAs(Role role) { return hasCurrentRole(role); }


    public List<ShopEntry> entries() {
        return this.entries;
    }

    public int size() {
        return this.entries.size();
    }


    public ShopBuilder add(ShopEntry entry) {
        if (entry != null) this.entries.add(entry);
        return this;
    }


    public ShopBuilder addAfter(ShopEntry anchor, ShopEntry entry) {
        if (entry == null) return this;
        int at = this.entries.indexOf(anchor);
        if (at < 0) this.entries.add(entry);
        else this.entries.add(at + 1, entry);
        return this;
    }


    public ShopBuilder removeIf(Predicate<ShopEntry> filter) {
        this.entries.removeIf(filter);
        return this;
    }


    public List<ShopEntry> build() {
        return List.copyOf(this.entries);
    }
}
