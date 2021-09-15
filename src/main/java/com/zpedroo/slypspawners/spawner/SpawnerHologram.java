package com.zpedroo.slypspawners.spawner;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.utils.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

public class SpawnerHologram {

    private Hologram hologram;
    private String[] hologramLines;
    private TextLine[] textLines;
    private Double hologramHeight;
    private Item displayItem;

    public SpawnerHologram(PlayerSpawner spawner) {
        this.hologramLines = spawner.getSpawner().getHologram();
        this.textLines = new TextLine[16];
        this.hologramHeight = Settings.HOLOGRAM_HEIGHT;
        Bukkit.getScheduler().runTaskLater(SlypSpawners.get(), () -> update(spawner), 0L);
    }

    public void update(PlayerSpawner spawner) {
        spawner.getLocation().getBlock().setType(spawner.getSpawner().getBlock());
        if (spawner.getSpawner().getBlock().equals(Material.MOB_SPAWNER)) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) spawner.getLocation().getBlock().getState();
            creatureSpawner.setCreatureTypeByName(spawner.getSpawner().getEntity().toString());
            creatureSpawner.setDelay(Integer.MAX_VALUE);
        }

        if (hologram == null) {
            this.hologram = HologramsAPI.createHologram(SlypSpawners.get(), spawner.getLocation().clone().add(0.5D, hologramHeight, 0.5D));

            for (int line = 0; line < hologramLines.length; ++line) {
                textLines[line] = hologram.insertTextLine(line, spawner.replace(hologramLines[line]));
            }

            this.displayItem = spawner.getLocation().getWorld().dropItem(spawner.getLocation().clone().add(0.5D, 1D, 0.5D), spawner.getSpawner().getDisplayItem());

            displayItem.setVelocity(new Vector(0, 0.1, 0));
            displayItem.setPickupDelay(Integer.MAX_VALUE);
            displayItem.setCustomName("Spawner Item");
            displayItem.setCustomNameVisible(false);
        } else {
            for (int line = 0; line < hologramLines.length; ++line) {
                textLines[line].setText(spawner.replace(hologramLines[line]));
            }
        }
    }

    public void delete() {
        if (hologram == null) return;

        hologram.delete();
        displayItem.remove();

        this.hologram = null;
    }
}