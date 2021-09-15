package com.zpedroo.slypspawners.managers;

import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.spawner.cache.DataCache;
import com.zpedroo.slypspawners.spawner.drop.Drop;
import com.zpedroo.slypspawners.mysql.DBConnection;
import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import com.zpedroo.slypspawners.spawner.Spawner;
import com.zpedroo.slypspawners.spawner.head.Head;
import com.zpedroo.slypspawners.utils.inv.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SpawnerManager {

    private static SpawnerManager instance;
    public static SpawnerManager getInstance() { return instance; }

    private DataCache dataCache;

    public SpawnerManager() {
        instance = this;
        this.dataCache = new DataCache();
        this.loadSpawners();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (!entity.getType().equals(EntityType.DROPPED_ITEM)) continue;
                        if (!StringUtils.equals(entity.getName(), "Spawner Item")) continue;

                        entity.remove();
                    }
                }

                loadPlacedSpawners();
            }
        }.runTaskLaterAsynchronously(SlypSpawners.get(), 100L);
    }

    public PlayerSpawner getSpawner(Location location) {
        return getDataCache().getPlayerSpawners().get(location);
    }

    public Spawner getSpawner(String type) {
        return getDataCache().getSpawners().get(type.toUpperCase());
    }

    private void loadSpawners() {
        File folder = new File(SlypSpawners.get().getDataFolder(), "/spawners");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));

        if (files == null) return;

        for (File file : files) {
            if (file == null) continue;

            YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);

            EntityType entity = EntityType.valueOf(yamlConfig.getString("Spawner-Settings.entity"));
            String entityName = getColored(yamlConfig.getString("Spawner-Settings.entity-name"));
            ItemStack item = ItemBuilder.build(yamlConfig, "Spawner-Settings.item").build();
            Material block = Material.valueOf(yamlConfig.getString("Spawner-Settings.spawner-block"));
            String type = file.getName().replace(".yml", "");
            Long delay = yamlConfig.getLong("Spawner-Settings.spawn-delay");
            BigInteger maxStack = new BigInteger(yamlConfig.getString("Spawner-Settings.max-stack"));
            String permission = yamlConfig.getString("Spawner-Settings.permission", "NULL");
            String permissionMessage = ChatColor.translateAlternateColorCodes('&', yamlConfig.getString("Spawner-Settings.permission-message", ""));
            String[] hologram = getColored(yamlConfig.getStringList("Spawner-Hologram")).toArray(new String[16]);
            List<Drop> drops = new ArrayList<>(4);

            ItemStack headItem = ItemBuilder.build(yamlConfig, "Spawner-Settings.head-drop.item").build();
            String displayName = ChatColor.translateAlternateColorCodes('&', yamlConfig.getString("Spawner-Settings.head-drop.display-name"));
            Double chance = yamlConfig.getDouble("Spawner-Settings.head-drop.chance");
            Double percentage = yamlConfig.getDouble("Spawner-Settings.head-drop.percentage");
            Boolean stack = yamlConfig.getBoolean("Spawner-Settings.head-drop.stack");

            Head head = new Head(headItem, displayName, chance, percentage, stack);

            for (String str : yamlConfig.getConfigurationSection("Spawner-Settings.drops").getKeys(false)) {
                if (str == null) continue;

                displayName = ChatColor.translateAlternateColorCodes('&', yamlConfig.getString("Spawner-Settings.drops." + str + ".display-name"));
                ItemStack dropItem = ItemBuilder.build(yamlConfig, "Spawner-Settings.drops." + str).build();

                Drop drop = new Drop(displayName, dropItem);

                drops.add(drop);
            }

            getDataCache().getSpawners().put(type.toUpperCase(), new Spawner(entity, entityName, item, block, type, delay, maxStack, permission, permissionMessage, hologram, drops, head));
        }
    }

    private void loadPlacedSpawners() {
        getDataCache().setPlayerSpawners(DBConnection.getInstance().getDBManager().getPlacedSpawners());
    }

    public void saveAll() {
        new HashSet<>(getDataCache().getDeletedSpawners()).forEach(location -> {
            if (location == null) return;

            DBConnection.getInstance().getDBManager().deleteSpawner(serializeLocation(location));
        });

        getDataCache().getDeletedSpawners().clear();

        new HashSet<>(getDataCache().getPlayerSpawners().values()).forEach(spawner -> {
            if (spawner == null) return;
            if (!spawner.isQueueUpdate()) return;

            DBConnection.getInstance().getDBManager().saveSpawner(spawner);
            spawner.setQueueUpdate(false);
        });
    }

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private List<String> getColored(List<String> list) {
        List<String> colored = new ArrayList<>();
        for (String str : list) {
            colored.add(getColored(str));
        }

        return colored;
    }

    public String serializeLocation(Location location) {
        if (location == null) return null;

        StringBuilder builder = new StringBuilder(4);
        builder.append(location.getWorld().getName());
        builder.append("#" + location.getX());
        builder.append("#" + location.getY());
        builder.append("#" + location.getZ());

        return builder.toString();
    }

    public Location deserializeLocation(String location) {
        if (location == null) return null;

        String[] split = location.split("#");

        World world = Bukkit.getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);

        return new Location(world, x, y, z);
    }

    public DataCache getDataCache() {
        return dataCache;
    }
}
