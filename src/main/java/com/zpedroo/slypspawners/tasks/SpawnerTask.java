package com.zpedroo.slypspawners.tasks;

import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerTask extends BukkitRunnable {

    private PlayerSpawner spawner;

    public SpawnerTask(PlayerSpawner spawner) {
        this.spawner = spawner;
        runTaskTimer(SlypSpawners.get(), spawner.getDelay(), spawner.getDelay());
    }

    @Override
    public void run() {
        if (spawner == null || !spawner.isEnabled() || !spawner.getLocation().getWorld().isChunkLoaded(spawner.getLocation().getChunk())) {
            this.cancel();
            return;
        }

        Long delay = spawner.getDelay();

        spawner.setDelay(delay);
        if (delay > 0) return;

        spawner.spawn();
        spawner.updateDelay();
    }
}