package com.zpedroo.slypspawners.tasks;

import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.managers.SpawnerManager;
import com.zpedroo.slypspawners.utils.config.Settings;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask extends BukkitRunnable {

    public SaveTask(SlypSpawners slypSpawners) {
        runTaskTimerAsynchronously(slypSpawners, 20L * Settings.SAVE_INTERVAL, 20L * Settings.SAVE_INTERVAL);
    }

    @Override
    public void run() {
        SpawnerManager.getInstance().saveAll();
    }
}