package com.zpedroo.slypspawners.listeners;

import com.zpedroo.slypspawners.spawner.drop.DropManager;
import com.zpedroo.slypspawners.spawner.head.HeadManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

public class PlayerGeneralListeners implements Listener {

    private DropManager dropManager;
    private HeadManager headManager;

    public PlayerGeneralListeners() {
        this.dropManager = new DropManager();
        this.headManager = new HeadManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPickupDrop(PlayerPickupItemEvent event) {
        if (!event.getItem().hasMetadata("DropAmount")) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Item item = event.getItem();
        BigInteger amount = new BigInteger(item.getMetadata("DropAmount").get(0).asString());
        Integer toGive = getFreeSpace(player, item.getItemStack());

        if (toGive <= 0) return;
        if (BigInteger.valueOf(toGive).compareTo(amount) > 0) toGive = amount.intValue();

        ItemStack items = item.getItemStack();
        items.setAmount(toGive);

        getDropManager().removeStack(item, BigInteger.valueOf(toGive));
        player.getInventory().addItem(items);
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1f, 1f);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPickupHead(PlayerPickupItemEvent event) {
        if (!event.getItem().hasMetadata("HeadAmount")) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Item item = event.getItem();
        BigInteger amount = new BigInteger(item.getMetadata("HeadAmount").get(0).asString());
        Integer toGive = getFreeSpace(player, item.getItemStack());

        if (toGive <= 0) return;
        if (BigInteger.valueOf(toGive).compareTo(amount) > 0) toGive = amount.intValue();

        ItemStack items = item.getItemStack();
        items.setAmount(toGive);

        getHeadManager().removeStack(item, BigInteger.valueOf(toGive));
        player.getInventory().addItem(items);
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1f, 1f);
    }

    private Integer getFreeSpace(Player player, ItemStack item) {
        int free = 0;

        for (ItemStack items : player.getInventory().getContents()) {
            if (items == null || items.getType().equals(Material.AIR)) {
                free += item.getMaxStackSize();
                continue;
            }

            if (!items.isSimilar(item)) continue;

            free += item.getMaxStackSize() - items.getAmount();
        }

        return free;
    }

    private DropManager getDropManager() {
        return dropManager;
    }

    private HeadManager getHeadManager() {
        return headManager;
    }
}