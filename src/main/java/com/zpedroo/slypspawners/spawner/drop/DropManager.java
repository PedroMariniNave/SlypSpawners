package com.zpedroo.slypspawners.spawner.drop;

import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.math.BigInteger;

public class DropManager {

    private static DropManager instance;
    public static DropManager getInstance() { return instance; }

    public DropManager() {
        instance = this;
    }

    public void addStack(Item item, BigInteger value) {
        if (!item.hasMetadata("DropAmount")) return;

        final BigInteger stack = new BigInteger(item.getMetadata("DropAmount").get(0).asString());
        if (stack.signum() <= 0) return;

        BigInteger newValue = stack.add(value);

        item.setMetadata("DropAmount", new FixedMetadataValue(SlypSpawners.get(), newValue.toString()));

        if (item.getCustomName() == null) return;

        item.setCustomName(StringUtils.replaceEach(item.getCustomName(), new String[]{
                NumberFormatter.getInstance().format(stack)
        }, new String[]{
                NumberFormatter.getInstance().format(newValue)
        }));
    }

    public void removeStack(Item item, BigInteger value) {
        if (!item.hasMetadata("DropAmount")) return;

        final BigInteger stack = new BigInteger(item.getMetadata("DropAmount").get(0).asString());
        if (value.compareTo(stack) > 0) value = stack;

        BigInteger newValue = stack.subtract(value);

        if (newValue.signum() <= 0) {
            item.remove();
            return;
        }

        item.setMetadata("DropAmount", new FixedMetadataValue(SlypSpawners.get(), newValue.toString()));

        if (item.getCustomName() == null) return;

        item.setCustomName(StringUtils.replaceEach(item.getCustomName(), new String[]{
                NumberFormatter.getInstance().format(stack)
        }, new String[]{
                NumberFormatter.getInstance().format(newValue)
        }));
    }

    public void dropItems(PlayerSpawner spawner, Location location, BigInteger amount) {
        firstLoop: for (Drop drop : spawner.getSpawner().getDrops()) {
            if (drop == null) continue;

            for (Entity near : location.getWorld().getNearbyEntities(location, 10, 10, 10)) {
                if (near == null || !near.getType().equals(EntityType.DROPPED_ITEM)) continue;

                Item droppedItem = (Item) near;
                if (!droppedItem.getItemStack().isSimilar(drop.getItem())) continue;

                addStack(droppedItem, amount);
                continue firstLoop;
            }

            ItemStack dropItem = drop.getItem().clone();

            Item item = location.getWorld().dropItem(location, dropItem);
            String displayName = drop.getDisplayName();

            item.setCustomName(StringUtils.replaceEach(displayName, new String[]{
                    "{amount}"
            }, new String[]{
                    NumberFormatter.getInstance().format(amount)
            }));
            item.setCustomNameVisible(true);
            item.setMetadata("DropAmount", new FixedMetadataValue(SlypSpawners.get(), amount.toString()));
        }
    }
}