package dev.hae.btt.draft;

import dev.doctor4t.wathe.api.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public final class DraftPool {

    public static final int FACE_UP = 3;

    private final List<dev.hae.btt.role.Roles.Kind> remaining = new ArrayList<>();
    private final java.util.Set<Role> used = new java.util.HashSet<>();
    private final java.util.Set<Role> appeared = new java.util.HashSet<>();
    private final java.util.Map<Role, Role> shown = new java.util.HashMap<>();
    private final Random random;

    public DraftPool(List<Role> roster, Random random) { this(roster, List.of(), random); }
    public DraftPool(List<Role> roster, java.util.Collection<Role> forced, Random random) {
        for (Role role : roster) remaining.add(dev.hae.btt.role.Roles.kindOf(role));
        used.addAll(forced);
        appeared.addAll(forced);
        this.random = random;
    }
    public int size() { return remaining.size(); }
    public boolean isEmpty() { return remaining.isEmpty(); }
    public Role displayRole(Role role) { return shown.getOrDefault(role, role); }

    public Hand deal() {
        shown.clear();
        if (remaining.isEmpty()) return new Hand(List.of(), null);
        var slots = new ArrayList<dev.hae.btt.role.Roles.Kind>();
        while (slots.size() < 4) {
            var cycle = new ArrayList<>(remaining);
            Collections.shuffle(cycle, random);
            for (var kind : cycle) {
                if (slots.size() == 4) break;
                slots.add(kind);
            }
        }
        var pools = dev.hae.btt.role.Roles.kindPools();
        var offered = new ArrayList<Role>();
        var reserved = new java.util.HashSet<>(used);
        for (var kind : slots) {
            var candidates = new ArrayList<>(pools.getOrDefault(kind, List.of()));
            candidates.removeIf(reserved::contains);

            if (candidates.isEmpty()) {
                candidates.addAll(pools.getOrDefault(kind, List.of()));
                candidates.removeIf(used::contains);
            }
            Role chosen = null;
            while (!candidates.isEmpty()) {
                Role candidate = dev.hae.btt.role.RoleWeights.pick(candidates, 1).getFirst();
                candidates.remove(candidate);
                if (candidate == dev.hae.btt.role.Roles.MARIONETTE || candidate == dev.hae.btt.role.Roles.LUNATIC) {
                    if (!shown.containsKey(candidate)) {
                        var facades = dev.hae.btt.role.Decoy.cognitivePool(candidate).stream()
                                .filter(r -> !reserved.contains(r) && !appeared.contains(r)).toList();
                        if (facades.isEmpty()) continue;
                        shown.put(candidate, facades.get(random.nextInt(facades.size())));
                    }
                }
                chosen = candidate;
                break;
            }
            if (chosen == null) throw new IllegalStateException("No draft candidate for " + kind);
            offered.add(chosen);
            reserved.add(chosen);
            reserved.add(displayRole(chosen));
        }
        return new Hand(List.copyOf(offered.subList(0, FACE_UP)), offered.get(FACE_UP));
    }
    public boolean take(Role role) {
        if (role == null || used.contains(role) || !remaining.remove(dev.hae.btt.role.Roles.kindOf(role))) return false;
        used.add(role);
        appeared.add(role);
        Role facade = shown.get(role);
        if (facade != null) {
            used.add(facade);
        }
        return true;
    }


    public record Hand(List<Role> faceUp, Role hidden) {

        public Role at(int slot) {
            if (slot < 0) return null;
            if (slot < faceUp.size()) return faceUp.get(slot);
            if (slot == faceUp.size()) return hidden;
            return null;
        }

        public boolean isHiddenSlot(int slot) {
            return slot == faceUp.size();
        }

        public int slotCount() {
            return faceUp.size() + (hidden == null ? 0 : 1);
        }
    }
}
