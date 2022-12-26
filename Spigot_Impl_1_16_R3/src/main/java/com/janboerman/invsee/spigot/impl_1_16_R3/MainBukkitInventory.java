package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.MainSpectatorInventory;
import com.janboerman.invsee.spigot.api.template.Mirror;
import com.janboerman.invsee.spigot.api.template.PlayerInventorySlot;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class MainBukkitInventory extends CraftInventory implements MainSpectatorInventory {

    protected MainBukkitInventory(MainNmsInventory nmsInventory) {
        super(nmsInventory);
    }

    @Override
    public MainNmsInventory getInventory() {
        return (MainNmsInventory) super.getInventory();
    }

    @Override
    public String getSpectatedPlayerName() {
        return getInventory().spectatedPlayerName;
    }

    @Override
    public UUID getSpectatedPlayerId() {
        return getInventory().spectatedPlayerUuid;
    }

    @Override
    public String getTitle() {
        return getInventory().title;
    }

    @Override
    public ItemStack[] getStorageContents() {
        return getInventory().storageContents.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setStorageContents(ItemStack[] storageContents) {
        Objects.requireNonNull(storageContents, "storageContents cannot be null");
        int storageContentsSize = getInventory().storageContents.size();
        if (storageContents.length != storageContentsSize)
            throw new IllegalArgumentException("storageContents must be of length " + storageContentsSize);

        for (int i = 0; i < storageContentsSize; i++) {
            getInventory().storageContents.set(i, CraftItemStack.asNMSCopy(storageContents[i]));
        }
    }

    @Override
    public void watch(InventoryView targetPlayerView) {
        Objects.requireNonNull(targetPlayerView, "targetPlayerView cannot be null");

        MainNmsInventory nms = getInventory();
        Inventory top = targetPlayerView.getTopInventory();
        if (top instanceof CraftInventoryCrafting /*does not extend CraftResultInventory for some reason.*/) {
            //includes a player's own crafting slots
            InventoryCrafting targetCrafting = (InventoryCrafting) ((CraftInventoryCrafting) top).getInventory();
            nms.personalContents = targetCrafting.getContents(); //luckily this getContents() method does not copy.
        } else if (top instanceof CraftResultInventory) {
            //anvil, grindstone, loom, smithing table, cartography table, stone cutter
            IInventory repairItems = ((CraftResultInventory) top).getInventory();
            nms.personalContents = repairItems.getContents();
        } else if (top instanceof CraftInventoryEnchanting /*does not extend CraftResultInventory for some reason.*/) {
            IInventory enchantItems = ((CraftInventoryEnchanting) top).getInventory();
            nms.personalContents = enchantItems.getContents();
        } else if (top instanceof CraftInventoryMerchant /*does not extend CraftResultInventory for some reason.*/) {
            InventoryMerchant merchantItems = ((CraftInventoryMerchant) top).getInventory();
            nms.personalContents = merchantItems.getContents();
        }

        //do this at the nms level so that I can save on packets? (only need to update the last 9 slots :-))
        for (HumanEntity viewer : getViewers()) {
            if (viewer instanceof Player) {
                ((Player) viewer).updateInventory();
            }
        }
    }

    @Override
    public void unwatch() {
        MainNmsInventory nms = getInventory();
        nms.personalContents = nms.playerCraftingContents;

        //do this at the nms level so that I can save on packets? (only need to update the last 9 slots :-))
        for (HumanEntity viewer : getViewers()) {
            if (viewer instanceof Player) {
                ((Player) viewer).updateInventory();
            }
        }
    }

    @Override
    public ItemStack[] getArmourContents() {
        return getInventory().armourContents.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setArmourContents(ItemStack[] armourContents) {
        Objects.requireNonNull(armourContents, "armourContents cannot be null");
        int armourContententsSize = getInventory().armourContents.size();
        if (armourContents.length != armourContententsSize)
            throw new IllegalArgumentException("armourContents must be of length " + armourContententsSize);

        for (int i = 0; i < armourContententsSize; i++) {
            getInventory().armourContents.set(i, CraftItemStack.asNMSCopy(armourContents[i]));
        }
    }

    @Override
    public ItemStack[] getOffHandContents() {
        return getInventory().offHand.stream().map(CraftItemStack::asCraftMirror).toArray(ItemStack[]::new);
    }

    @Override
    public void setOffHandContents(ItemStack[] offHand){
        Objects.requireNonNull(offHand, "offHand cannot be null");
        int offHandContentsSize = getInventory().offHand.size();
        if (offHand.length != offHandContentsSize)
            throw new IllegalArgumentException("offHand must be of length " + offHandContentsSize);

        for (int i = 0; i < offHandContentsSize; i++) {
            getInventory().offHand.set(i, CraftItemStack.asNMSCopy(offHand[i]));
        }
    }

    @Override
    public void setPersonalContents(ItemStack[] craftingContents) {
        Objects.requireNonNull(craftingContents, "craftingContents cannot be null");

        MainNmsInventory nms = getInventory();
        var nmsCraftingItems = nms.personalContents;
        if (nmsCraftingItems != null) {
            int craftingContentsSize = nmsCraftingItems.size();
            if (craftingContents.length != craftingContentsSize)
                throw new IllegalArgumentException("craftingContents must be of length " + craftingContentsSize);

            for (int i = 0; i < craftingContentsSize; i++) {
                nmsCraftingItems.set(i, CraftItemStack.asNMSCopy(craftingContents[i]));
            }
        }
    }

    @Override
    public ItemStack[] getPersonalContents() {
        var nmsCraftingItems = getInventory().personalContents;
        if (nmsCraftingItems != null) {
            int craftingContentsSize = nmsCraftingItems.size();
            ItemStack[] result = new ItemStack[craftingContentsSize];
            for (int i = 0; i < craftingContentsSize; i++) {
                result[i] = CraftItemStack.asCraftMirror(nmsCraftingItems.get(i));
            }
            return result;
        } else {
            return new ItemStack[0];
        }
    }

    @Override
    public int getPersonalContentsSize() {
        var nmsCraftingItems = getInventory().personalContents;
        if (nmsCraftingItems != null) {
            return nmsCraftingItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public void setCursorContents(ItemStack cursor) {
        var onCursor = getInventory().onCursor;
        if (onCursor != null) {
            onCursor.set(CraftItemStack.asNMSCopy(cursor));
        }
    }

    @Override
    public ItemStack getCursorContents() {
        var onCursor = getInventory().onCursor;
        if (onCursor != null) {
            return CraftItemStack.asCraftMirror(onCursor.get());
        } else {
            return null;
        }
    }

    @Override
    public Mirror<PlayerInventorySlot> getMirror() {
        return getInventory().mirror;
    }

    // org.bukkit.inventory.Inventory overrides

    @Override
    public int first(ItemStack stack) {
        assert stack != null;

        ItemStack[] storageContents = getStorageContents();
        for (int i = 0; i < storageContents.length; i++) {
            if (storageContents[i] != null && storageContents[i].isSimilar(stack))
                return i;
        }

        ItemStack[] armourContents = getArmourContents();
        for (int i = 0; i < armourContents.length; i++) {
            if (armourContents[i] != null && armourContents[i].isSimilar(stack))
                return i + storageContents.length;
        }

        ItemStack[] offHandContents = getOffHandContents();
        for (int i = 0; i < offHandContents.length; i++) {
            if (offHandContents[i] != null && offHandContents[i].isSimilar(stack))
                return i + storageContents.length + armourContents.length;
        }

        return -1;
    }

    @Override
    public int first(Material material) {
        assert material != null;

        ItemStack[] storageContents = getStorageContents();
        for (int i = 0; i < storageContents.length; i++) {
            if (storageContents[i] != null && storageContents[i].getType() == material)
                return i;
        }

        ItemStack[] armourContents = getArmourContents();
        for (int i = 0; i < armourContents.length; i++) {
            if (armourContents[i] != null && armourContents[i].getType() == material)
                return i + storageContents.length;
        }

        ItemStack[] offHandContents = getOffHandContents();
        for (int i = 0; i < offHandContents.length; i++) {
            if (offHandContents[i] != null && offHandContents[i].getType() == material)
                return i + storageContents.length + armourContents.length;
        }

        return -1;
    }

    @Override
    public int firstEmpty() {

        ItemStack[] storageContents = getStorageContents();
        for (int i = 0; i < storageContents.length; i++) {
            if (storageContents[i] == null || storageContents[i].getAmount() == 0)
                return i;
        }

        ItemStack[] armourContents = getArmourContents();
        for (int i = 0; i < armourContents.length; i++) {
            if (armourContents[i] == null || armourContents[i].getAmount() == 0)
                return i + storageContents.length;
        }

        ItemStack[] offHandContents = getOffHandContents();
        for (int i = 0; i < offHandContents.length; i++) {
            if (offHandContents[i] == null || offHandContents[i].getAmount() == 0)
                return i + storageContents.length + armourContents.length;
        }

        return -1;
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack[] items) {
        assert items != null;

        HashMap<Integer, ItemStack> leftOvers = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack leftOver = addItem(items[i]);
            if (leftOver != null && leftOver.getAmount() > 0) {
                leftOvers.put(i, leftOver);
            }
        }

        return leftOvers;
    }

    private ItemStack addItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getAmount() == 0) return null;

        ItemStack[] storageContents = getStorageContents();
        addItem(storageContents, itemStack, getMaxStackSize());
        setStorageContents(storageContents);

        if (itemStack.getAmount() == 0) return null;

        ItemStack[] armourContents = getArmourContents();
        addItem(armourContents, itemStack, getMaxStackSize());
        setArmourContents(armourContents);

        if (itemStack.getAmount() == 0) return null;

        ItemStack[] offHand = getOffHandContents();
        addItem(offHand, itemStack, getMaxStackSize());
        setOffHandContents(offHand);

        return itemStack;
    }

    private static void addItem(final ItemStack[] contents, final ItemStack itemStack, final int maxStackSize) {
        assert contents != null && itemStack != null;

        //merge with existing similar item stacks
        for (int i = 0; i < contents.length; i++) {
            ItemStack existingStack = contents[i];
            if (existingStack != null) {
                if (existingStack.isSimilar(itemStack) && existingStack.getAmount() < maxStackSize) {
                    //how many can we merge (at most)?
                    int maxMergeAmount = Math.min(maxStackSize - existingStack.getAmount(), itemStack.getAmount());
                    if (maxMergeAmount > 0) {
                        if (itemStack.getAmount() <= maxMergeAmount) {
                            //full merge
                            existingStack.setAmount(existingStack.getAmount() + itemStack.getAmount());
                            itemStack.setAmount(0);
                        } else {
                            //partial merge
                            existingStack.setAmount(maxStackSize);
                            itemStack.setAmount(itemStack.getAmount() - maxMergeAmount);
                        }
                    }
                }
            }

            if (itemStack.getAmount() == 0) break;
        }

        //merge with empty slots
        if (itemStack.getAmount() > 0) {
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] == null || contents[i].getAmount() == 0 || contents[i].getType() == Material.AIR) {
                    if (itemStack.getAmount() <= maxStackSize) {
                        //full merge
                        contents[i] = itemStack.clone();
                        itemStack.setAmount(0);
                    } else {
                        //partial merge
                        ItemStack clone = itemStack.clone(); clone.setAmount(maxStackSize);
                        contents[i] = clone;
                        itemStack.setAmount(itemStack.getAmount() - maxStackSize);
                    }
                }

                if (itemStack.getAmount() == 0) break;
            }
        }
    }

}
