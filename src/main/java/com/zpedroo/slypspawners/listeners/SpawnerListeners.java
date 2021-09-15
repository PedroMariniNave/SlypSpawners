package com.zpedroo.slypspawners.listeners;

import com.zpedroo.slypspawners.spawner.booster.BoosterManager;
import com.zpedroo.slypspawners.spawner.drop.DropManager;
import com.zpedroo.slypspawners.hooks.WorldGuardHook;
import com.zpedroo.slypspawners.managers.EntityManager;
import com.zpedroo.slypspawners.managers.SpawnerManager;
import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import com.zpedroo.slypspawners.spawner.Spawner;
import com.zpedroo.slypspawners.spawner.head.HeadManager;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import com.zpedroo.slypspawners.utils.config.Messages;
import com.zpedroo.slypspawners.utils.config.Settings;
import com.zpedroo.slypspawners.utils.menu.Menus;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.ArrayList;

public class SpawnerListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || !entity.hasMetadata("MobAmount")) return;

        event.getDrops().clear();

        String serialized = entity.getMetadata("Spawner").get(0).asString();
        PlayerSpawner spawner = SpawnerManager.getInstance().getSpawner(SpawnerManager.getInstance().deserializeLocation(serialized));
        if (spawner == null) return;

        Player player = entity.getKiller();
        if (player == null) return;

        boolean killAll = player.isSneaking();
        BigInteger toKill = new BigInteger(entity.getMetadata("MobAmount").get(0).asString());

        if (!killAll) {
            toKill = toKill.divide(BigInteger.TEN);
        }

        if (toKill.signum() <= 0) toKill = BigInteger.ONE;

        EntityManager.removeStack(entity, toKill, spawner);
        entity.remove();

        Location location = entity.getLocation();
        DropManager.getInstance().dropItems(spawner, location, toKill);
        HeadManager.getInstance().dropHead(spawner.getSpawner(), entity.getLocation(), toKill);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        PlayerSpawner spawner = SpawnerManager.getInstance().getSpawner(block.getLocation());
        if (spawner == null) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        if (!spawner.canInteract(player)) {
            player.sendMessage(Messages.NEED_PERMISSION);
            return;
        }

        ItemStack item = player.getItemInHand().clone();
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbt = new NBTItem(item);
            if (nbt.hasKey("SpawnerBooster")) {
                Long ticks = nbt.getLong("SpawnerBooster");
                Long maxBooster = Settings.MAX_BOOSTER;
                Long overLimit = spawner.getBoost() + ticks > maxBooster ? spawner.getBoost() + ticks - maxBooster : 0;

                spawner.addBoost(ticks - overLimit);

                item.setAmount(1);
                player.getInventory().removeItem(item);

                if (overLimit <= 0) return;

                player.getInventory().addItem(BoosterManager.getInstance().getItem(overLimit, 1));
                player.sendMessage(StringUtils.replaceEach(Messages.BOOST_REFOUND, new String[]{
                        "{time}",
                        "{time_formatted}"
                }, new String[]{
                        overLimit.toString(),
                        NumberFormatter.getInstance().formatDecimal((double) overLimit/20)
                }));
                return;
            }
        }

        Menus.getInstance().openSpawnerMenu(player, spawner);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final ItemStack item = event.getItemInHand().clone();
        if (item.getType().equals(Material.AIR)) return;

        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("SpawnersAmount")) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        if (!WorldGuardHook.canBuild(player, block.getLocation())) return;

        Spawner spawner = SpawnerManager.getInstance().getDataCache().getSpawners().get(nbt.getString("SpawnersType").toUpperCase());
        if (spawner == null) return;

        if (!StringUtils.equals(spawner.getPermission(), "NULL")) {
            if (!player.hasPermission(spawner.getPermission())) {
                player.sendMessage(spawner.getPermissionMessage());
                return;
            }
        }

        BigInteger amount = new BigInteger(nbt.getString("SpawnersAmount"));
        BigInteger overLimit = BigInteger.ZERO;

        int r = Settings.STACK_RADIUS;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (!WorldGuardHook.canBuild(player, new Location(block.getWorld(), x, y, z))) continue;

                    Block blockFound = block.getRelative(x, y, z);
                    if (blockFound.getType().equals(Material.AIR)) continue;

                    PlayerSpawner nearSpawner = SpawnerManager.getInstance().getSpawner(blockFound.getLocation());
                    if (nearSpawner == null) continue;

                    if (!StringUtils.equals(nearSpawner.getSpawner().getType(), spawner.getType()) || nearSpawner.hasReachedMaxStack() || !nearSpawner.canInteract(player)) {
                        player.sendMessage(Messages.NEAR_SPAWNER);
                        return;
                    }

                    if (nearSpawner.getStack().add(amount).compareTo(spawner.getMaxStack()) > 0) {
                        overLimit = nearSpawner.getStack().add(amount).subtract(spawner.getMaxStack());
                    }

                    nearSpawner.addStack(amount.subtract(overLimit));

                    item.setAmount(1);
                    player.getInventory().removeItem(item);
                    if (overLimit.signum() <= 0) return;

                    player.getInventory().addItem(spawner.getItem(overLimit));
                    return;
                }
            }
        }

        if (spawner.getMaxStack().signum() > 0 && amount.compareTo(spawner.getMaxStack()) > 0) {
            overLimit = amount.subtract(spawner.getMaxStack());
        }

        PlayerSpawner playerSpawner = new PlayerSpawner(block.getLocation(), player.getUniqueId(), amount.subtract(overLimit), spawner, 0L, new ArrayList<>());
        playerSpawner.cache();

        item.setAmount(1);
        player.getInventory().removeItem(item);
        if (overLimit.signum() <= 0) return;

        player.getInventory().addItem(spawner.getItem(overLimit));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        PlayerSpawner spawner = SpawnerManager.getInstance().getSpawner(block.getLocation());
        if (spawner == null) return;

        Player player = event.getPlayer();

        event.setCancelled(true);

        if (!StringUtils.equals(spawner.getOwnerUUID().toString(), player.getUniqueId().toString())) {
            player.sendMessage(Messages.ONLY_OWNER);
            return;
        }

        player.getInventory().addItem(spawner.delete());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDespawn(ItemDespawnEvent event) {
        if (event.getEntity().getCustomName() == null) return;
        if (!StringUtils.equals(event.getEntity().getCustomName(), "Spawner Item")) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEggSpawnEvent(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (item.getItemStack().getType() == Material.EGG) {
            for (Entity entity : item.getNearbyEntities(0.5D, 1D, 0.5D)) {
                if (entity.getType() == EntityType.CHICKEN) {
                    event.setCancelled(true);
                }
            }
        }
    }
}