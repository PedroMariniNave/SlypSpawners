package com.zpedroo.slypspawners.spawner.cache;

import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import com.zpedroo.slypspawners.spawner.Spawner;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DataCache {

    /**
     * List of spawners
     *
     * Key = Type
     * Value = Spawner
     */
    private HashMap<String, Spawner> spawners;
    /**
     * Map with all placed spawners
     *
     * Key = Location
     * Value = PlayerSpawner
     */
    private HashMap<Location, PlayerSpawner> playerSpawners;
    /**
     * Set with all deleted spawners
     */
    private Set<Location> deletedSpawners;

    public DataCache() {
        this.spawners = new HashMap<>(32);
        this.playerSpawners = new HashMap<>(5120);
        this.deletedSpawners = new HashSet<>(5120);
    }

    public HashMap<String, Spawner> getSpawners() {
        return spawners;
    }

    public HashMap<Location, PlayerSpawner> getPlayerSpawners() {
        return playerSpawners;
    }

    public Set<Location> getDeletedSpawners() {
        return deletedSpawners;
    }

    public void setPlayerSpawners(HashMap<Location, PlayerSpawner> playerSpawners) {
        this.playerSpawners = playerSpawners;
    }
}
