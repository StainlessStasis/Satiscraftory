package io.github.stainlessstasis.manifold.factory_power.network;

import net.minecraft.core.GlobalPos;

import java.util.Set;
import java.util.UUID;

public class PowerNetwork {
    private final UUID id = UUID.randomUUID();
    private final Set<GlobalPos> members;

    public PowerNetwork(Set<GlobalPos> members) {
        this.members = members;
    }

    public UUID getId() {
        return id;
    }

    public Set<GlobalPos> getMembers() {
        return members;
    }

    public int size() {
        return members.size();
    }

    public boolean contains(GlobalPos pos) {
        return members.contains(pos);
    }

    @Override
    public String toString() {
        return "PowerNetwork{id=" + id + ", size=" + members.size() + "}";
    }
}