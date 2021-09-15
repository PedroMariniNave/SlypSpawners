package com.zpedroo.slypspawners.spawner;

import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.managers.EntityManager;
import com.zpedroo.slypspawners.managers.SpawnerManager;
import com.zpedroo.slypspawners.tasks.SpawnerTask;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import com.zpedroo.slypspawners.utils.config.Messages;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.*;

public class PlayerSpawner {

    private Location location;
    private UUID ownerUUID;
    private BigInteger stack;
    private Spawner spawner;
    private Long boost;
    private List<String> friends;
    private Boolean status;
    private Boolean update;
    private Long delay;
    private SpawnerHologram hologram;
    private Set<Entity> entities;
    private SpawnerTask task;

    public PlayerSpawner(Location location, UUID ownerUUID, BigInteger stack, Spawner spawner, Long boost, List<String> friends) {
        this.location = location;
        this.ownerUUID = ownerUUID;
        this.stack = stack;
        this.spawner = spawner;
        this.boost = boost;
        this.friends = friends;
        this.status = false;
        this.update = false;
        this.delay = spawner.getDelay();
        this.hologram = new SpawnerHologram(this);
        this.entities = new HashSet<>(4);
        this.task = null;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public BigInteger getStack() {
        return stack;
    }

    public Spawner getSpawner() {
        return spawner;
    }

    public Long getBoost() {
        return boost;
    }

    public List<String> getFriends() {
        return friends;
    }

    public Boolean isEnabled() {
        return status;
    }

    public Boolean isQueueUpdate() {
        return update;
    }

    public Boolean hasReachedMaxStack() {
        if (getSpawner().getMaxStack().signum() <= 0) return false;

        return getStack().compareTo(getSpawner().getMaxStack()) >= 0;
    }

    public Boolean canInteract(Player player) {
        String uuid = player.getUniqueId().toString();

        return StringUtils.equals(uuid, getOwnerUUID().toString()) || getFriends().contains(uuid) || player.hasPermission("spawners.admin");
    }

    public Long getDelay() {
        return delay - boost;
    }

    public SpawnerHologram getHologram() {
        return hologram;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public ItemStack delete() {
        SpawnerManager.getInstance().getDataCache().getDeletedSpawners().add(getLocation());
        SpawnerManager.getInstance().getDataCache().getPlayerSpawners().remove(getLocation());

        getHologram().delete();
        getLocation().getBlock().setType(Material.AIR);
        removeEntities();

        return getSpawner().getItem(getStack());
    }

    public String replace(String str) {
        return StringUtils.replaceEach(str, new String[] {
                "{owner}",
                "{stack}",
                "{max_stack}",
                "{status}"
        }, new String[] {
                Bukkit.getOfflinePlayer(getOwnerUUID()).getName(),
                NumberFormatter.getInstance().format(getStack()),
                NumberFormatter.getInstance().format(getSpawner().getMaxStack()),
                isEnabled() ? Messages.ENABLED : Messages.DISABLED
        });
    }

    public void addBoost(Long amount) {
        this.boost += amount;
    }

    public void switchStatus() {
        this.status = !status;
        getHologram().update(this);

        if (status) {
            this.task = new SpawnerTask(this);
        } else {
            this.task.cancel();
            this.task = null;
        }
    }

    public void updateDelay() {
        this.delay = getSpawner().getDelay() - getBoost();
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public void setQueueUpdate(Boolean status) {
        this.update = status;
    }

    public void addStack(BigInteger amount) {
        this.stack = getStack().add(amount);
        this.update = true;
        getHologram().update(this);
    }

    public void addEntity(Entity entity) {
        getEntities().add(entity);
    }

    public void removeEntities() {
        for (Entity entity : getEntities()) {
            if (entity == null) continue;

            entity.remove();
        }

        getEntities().clear();
    }

    public void spawn() {
        SlypSpawners.get().getServer().getScheduler().runTaskLater(SlypSpawners.get(), () -> EntityManager.spawn(this), 0L); // fix async entity
    }

    public void cache() {
        SpawnerManager.getInstance().getDataCache().getPlayerSpawners().put(getLocation(), this);
        setQueueUpdate(true);
    }
}