package com.zpedroo.slypspawners.spawner.booster;

import com.zpedroo.slypspawners.utils.FileUtils;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import com.zpedroo.slypspawners.utils.inv.ItemBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BoosterManager {

    private static BoosterManager instance;
    public static BoosterManager getInstance() { return instance; }

    private ItemStack item;

    public BoosterManager() {
        instance = this;
        this.item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Spawner-Booster").build();
    }

    private ItemStack getItem() {
        return item;
    }

    public ItemStack getItem(Long ticks, Integer amount) {
        NBTItem nbt = new NBTItem(getItem().clone());
        nbt.setLong("SpawnerBooster", ticks);

        ItemStack item = nbt.getItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String name = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
            List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : null;

            if (name != null) meta.setDisplayName(StringUtils.replaceEach(name, new String[] {
                    "{time}",
                    "{time_formatted}"
            }, new String[] {
                    ticks.toString(),
                    NumberFormatter.getInstance().formatDecimal((double) ticks/20)
            }));

            if (lore != null) {
                List<String> newLore = new ArrayList<>(lore.size());

                for (String str : lore) {
                    newLore.add(StringUtils.replaceEach(str, new String[] {
                            "{time}",
                            "{time_formatted}"
                    }, new String[] {
                            ticks.toString(),
                            NumberFormatter.getInstance().formatDecimal((double) ticks/20)
                    }));
                }

                meta.setLore(newLore);
            }

            item.setItemMeta(meta);
        }
        item.setAmount(amount);

        return item;
    }
}