package com.janboerman.invsee.glowstone;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.internal.inventory.ShallowCopy;
import net.glowstone.inventory.GlowInventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class EnderInventory extends GlowInventory implements EnderSpectatorInventory, ShallowCopy<EnderInventory> {

    final UUID targetPlayerUuid;
    final String targetPlayerName;

    final CreationOptions<EnderChestSlot> creationOptions;

    public EnderInventory(UUID targetPlayerUuid, String targetPlayerName, CreationOptions<EnderChestSlot> creationOptions, int size) {
        super(null, InventoryType.CHEST, size, creationOptions.getTitle().titleFor(Target.byGameProfile(targetPlayerUuid, targetPlayerName)));

        this.targetPlayerUuid = targetPlayerUuid;
        this.targetPlayerName = targetPlayerName;
        this.creationOptions = creationOptions;
        setMaxStackSize(defaultMaxStack());
    }

    @Override
    public String getSpectatedPlayerName() {
        return targetPlayerName;
    }

    @Override
    public UUID getSpectatedPlayerId() {
        return targetPlayerUuid;
    }

    @Override
    public Mirror<EnderChestSlot> getMirror() {
        return creationOptions.getMirror();
    }

    @Override
    public CreationOptions<EnderChestSlot> getCreationOptions() {
        return creationOptions.clone();
    }

    @Override
    public void setContents(EnderSpectatorInventory newContents) {
        EnderSpectatorInventory.super.setContents(newContents);
    }

    @Override
    public int defaultMaxStack() {
        return 64;
    }

    @Override
    public void shallowCopyFrom(EnderInventory from) {
        GlowstoneHacks.setSlots(this, GlowstoneHacks.getSlots(from));
    }

    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        getSlots().forEach(slot -> action.accept(slot.getItem()));
    }

    // Glowstone faulty implementations overrides

    @Override
    public boolean containsAtLeast(ItemStack stack, int amount) {
        if (amount <= 0)
            return true;
        if (stack == null)
            return false; //this is a bit weird, but this is what CraftInventory does.

        int encountered = 0;
        for (int slot = 0; slot < getSize(); slot++) {
            ItemStack item = getItem(slot);
            if (item != null && item.isSimilar(stack)) {
                encountered += item.getAmount();
                if (encountered >= amount)
                    return true;
            }
        }

        return false;
    }

}