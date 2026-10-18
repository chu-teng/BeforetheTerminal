package dev.hae.btt.draft;

import dev.doctor4t.wathe.api.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


















public final class DraftPool {

    public static final int FACE_UP = 3;

    private final List<Role> remaining = new ArrayList<>();
    private final Random random;

    public DraftPool(List<Role> roster, Random random) {
        this.remaining.addAll(roster);
        this.random = random;
    }

    public int size() {
        return remaining.size();
    }

    public boolean isEmpty() {
        return remaining.isEmpty();
    }


    public List<Role> snapshot() {
        return List.copyOf(remaining);
    }






    public Hand deal() {
        List<Role> shuffled = new ArrayList<>(remaining);
        Collections.shuffle(shuffled, random);
        List<Role> faceUp = new ArrayList<>();
        for (int i = 0; i < Math.min(FACE_UP, shuffled.size()); i++) faceUp.add(shuffled.get(i));

        List<Role> hiddenPool = new ArrayList<>();
        for (Role r : remaining) {
            if (!faceUp.contains(r)) hiddenPool.add(r);
        }
        Role hidden = hiddenPool.isEmpty() ? null : hiddenPool.get(random.nextInt(hiddenPool.size()));
        return new Hand(faceUp, hidden);
    }


    public boolean take(Role role) {
        if (role == null) return false;
        for (int i = 0; i < remaining.size(); i++) {
            Role r = remaining.get(i);
            if (r != null && r.identifier().equals(role.identifier())) {
                remaining.remove(i);
                return true;
            }
        }
        return false;
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
