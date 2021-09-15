package com.zpedroo.slypspawners.spawner.head;

import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.spawner.Spawner;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeadManager {

    private static HeadManager instance;
    public static HeadManager getInstance() { return instance; }

    public HeadManager() {
        instance = this;
    }

    public void addStack(Item item, BigInteger value) {
        if (!item.hasMetadata("HeadAmount")) return;

        final BigInteger stack = new BigInteger(item.getMetadata("HeadAmount").get(0).asString());
        if (stack.signum() <= 0) return;

        BigInteger newValue = stack.add(value);

        item.setMetadata("HeadAmount", new FixedMetadataValue(SlypSpawners.get(), newValue.toString()));

        if (item.getCustomName() == null) return;

        item.setCustomName(StringUtils.replaceEach(item.getCustomName(), new String[]{
                NumberFormatter.getInstance().format(stack)
        }, new String[]{
                NumberFormatter.getInstance().format(newValue)
        }));
    }

    public void removeStack(Item item, BigInteger value) {
        if (!item.hasMetadata("HeadAmount")) return;

        final BigInteger stack = new BigInteger(item.getMetadata("HeadAmount").get(0).asString());
        if (value.compareTo(stack) > 0) value = stack;

        BigInteger newValue = stack.subtract(value);

        if (newValue.signum() <= 0) {
            item.remove();
            return;
        }

        item.setMetadata("HeadAmount", new FixedMetadataValue(SlypSpawners.get(), newValue.toString()));

        if (item.getCustomName() == null) return;

        item.setCustomName(StringUtils.replaceEach(item.getCustomName(), new String[]{
                NumberFormatter.getInstance().format(stack)
        }, new String[]{
                NumberFormatter.getInstance().format(newValue)
        }));
    }

    public void dropHead(Spawner spawner, Location location, BigInteger amount) {
        Head head = spawner.getHead();
        Double chance = head.getChance();
        if (new Random().nextDouble() * 100D > chance) return;

        ItemStack headItem = head.getItem().clone();

        Double percentageAmount = (head.getPercentage() / 100) * amount.doubleValue();
        BigInteger finalAmount = new BigInteger(String.format("%.0f", percentageAmount));
        if (finalAmount.signum() <= 0) finalAmount = BigInteger.ONE;

        if (head.stackHeads()) {
            for (Entity near : location.getWorld().getNearbyEntities(location, 10, 10, 10)) {
                if (near == null || !near.getType().equals(EntityType.DROPPED_ITEM)) continue;

                Item droppedItem = (Item) near;
                if (!droppedItem.getItemStack().isSimilar(head.getItem())) continue;

                addStack(droppedItem, finalAmount);
                return;
            }

            ItemMeta meta = headItem.getItemMeta();

            if (meta != null) {
                String displayName = headItem.getItemMeta().hasDisplayName() ? headItem.getItemMeta().getDisplayName() : null;
                List<String> lore = headItem.getItemMeta().hasLore() ? headItem.getItemMeta().getLore() : null;

                if (displayName != null) meta.setDisplayName(StringUtils.replaceEach(displayName, new String[] {
                        "{amount}"
                }, new String[] {
                        NumberFormatter.getInstance().format(amount)
                }));

                if (lore != null) {
                    List<String> newLore = new ArrayList<>(lore.size());

                    for (String str : lore) {
                        newLore.add(StringUtils.replaceEach(str, new String[] {
                                "{amount}"
                        }, new String[] {
                                NumberFormatter.getInstance().format(amount)
                        }));
                    }

                    meta.setLore(newLore);
                }

                headItem.setItemMeta(meta);
            }

            NBTItem nbt = new NBTItem(headItem);
            nbt.setString("HeadAmount", finalAmount.toString());

            Item item = location.getWorld().dropItem(location, nbt.getItem());
            String displayName = head.getDisplayName();

            item.setCustomName(StringUtils.replaceEach(displayName, new String[]{
                    "{amount}"
            }, new String[]{
                    NumberFormatter.getInstance().format(finalAmount)
            }));
            item.setCustomNameVisible(true);
            return;
        }

        for (Entity near : location.getWorld().getNearbyEntities(location, 10, 10, 10)) {
            if (near == null || !near.getType().equals(EntityType.DROPPED_ITEM)) continue;

            Item droppedItem = (Item) near;
            if (!droppedItem.getItemStack().isSimilar(head.getItem())) continue;

            addStack(droppedItem, finalAmount);
            return;
        }

        Item item = location.getWorld().dropItem(location, headItem);
        String displayName = head.getDisplayName();

        item.setCustomName(StringUtils.replaceEach(displayName, new String[]{
                "{amount}"
        }, new String[]{
                NumberFormatter.getInstance().format(finalAmount)
        }));
        item.setCustomNameVisible(true);
        item.setMetadata("HeadAmount", new FixedMetadataValue(SlypSpawners.get(), finalAmount.toString()));
    }
}