package com.zpedroo.slypspawners.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {

    public static Boolean canBuild(Player player, Location location) {
        return WorldGuardPlugin.inst().canBuild(player, location);
    }
}