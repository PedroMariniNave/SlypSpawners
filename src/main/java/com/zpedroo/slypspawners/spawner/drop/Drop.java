package com.zpedroo.slypspawners.spawner.drop;

import org.bukkit.inventory.ItemStack;

public class Drop {

    private String displayName;
    private ItemStack item;

    public Drop(String displayName, ItemStack item) {
        this.displayName = displayName;
        this.item = item;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getItem() {
        return item;
    }
}