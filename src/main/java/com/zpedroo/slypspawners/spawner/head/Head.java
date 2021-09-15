package com.zpedroo.slypspawners.spawner.head;

import org.bukkit.inventory.ItemStack;

public class Head {

    private ItemStack item;
    private String displayName;
    private Double chance;
    private Double percentage;
    private Boolean stack;

    public Head(ItemStack item, String displayName, Double chance, Double percentage, Boolean stack) {
        this.item = item;
        this.displayName = displayName;
        this.chance = chance;
        this.percentage = percentage;
        this.stack = stack;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Double getChance() {
        return chance;
    }

    public Double getPercentage() {
        return percentage;
    }

    public Boolean stackHeads() {
        return stack;
    }
}