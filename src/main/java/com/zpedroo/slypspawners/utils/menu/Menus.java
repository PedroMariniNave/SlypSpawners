package com.zpedroo.slypspawners.utils.menu;

import com.zpedroo.slypspawners.SlypSpawners;
import com.zpedroo.slypspawners.listeners.PlayerChatListener;
import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import com.zpedroo.slypspawners.utils.FileUtils;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import com.zpedroo.slypspawners.utils.config.Messages;
import com.zpedroo.slypspawners.utils.inv.InventoryBuilder;
import com.zpedroo.slypspawners.utils.inv.InventoryUtils;
import com.zpedroo.slypspawners.utils.inv.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Menus {

    public static Menus instance;
    public static Menus getInstance() { return instance; }

    public Menus() {
        instance = this;
        new InventoryUtils();
    }

    public void openSpawnerMenu(Player player, PlayerSpawner spawner) {
        File folder = new File(SlypSpawners.get().getDataFolder() + "/spawners/" + spawner.getSpawner().getType() + ".yml");
        FileConfiguration file = YamlConfiguration.loadConfiguration(folder);

        int size = file.getInt("Spawner-Menu.size");
        String title = ChatColor.translateAlternateColorCodes('&', file.getString("Spawner-Menu.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);
        List<ItemBuilder> builders = new ArrayList<>(54);

        for (String items : file.getConfigurationSection("Spawner-Menu.items").getKeys(false)) {
            if (items == null) continue;

            ItemStack item = ItemBuilder.build(file, "Spawner-Menu.items." + items, new String[]{
                    "{owner}",
                    "{stack}",
                    "{spawn_time}",
                    "{boost}"
            }, new String[]{
                    Bukkit.getOfflinePlayer(spawner.getOwnerUUID()).getName(),
                    NumberFormatter.getInstance().format(spawner.getStack()),
                    NumberFormatter.getInstance().formatDecimal((double) (spawner.getDelay()/20)),
                    NumberFormatter.getInstance().formatDecimal((double) (spawner.getBoost()/20))
            }).build();
            Integer slot = file.getInt("Spawner-Menu.items." + items + ".slot");
            InventoryUtils.Action action = null;
            String actionStr = file.getString("Spawner-Menu.items." + items + ".action", "NULL");

            switch (actionStr.toUpperCase()) {
                case "SWITCH":
                    action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, spawner::switchStatus);
                    break;
                case "FRIENDS":
                    action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                        if (!StringUtils.equals(player.getUniqueId().toString(), spawner.getOwnerUUID().toString())) {
                            player.sendMessage(Messages.ONLY_OWNER);
                            return;
                        }

                        openFriendsMenu(player, spawner);
                    });
                    break;
            }

            builders.add(ItemBuilder.build(item, slot, action));
        }

        InventoryBuilder.build(player, inventory, builders);
    }

    public void openFriendsMenu(Player player, PlayerSpawner spawner) {
        FileUtils.Files file = FileUtils.Files.FRIENDS;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);
        List<ItemBuilder> builders = new ArrayList<>(54);

        List<String> friends = spawner.getFriends();

        if (friends.size() <= 0) {
            ItemStack empty = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            Integer slot = FileUtils.get().getInt(file, "Empty.slot");

            inventory.setItem(slot, empty);
        } else {
            int i = -1;
            String[] slotsSplit = FileUtils.get().getString(file, "Inventory.friend-slots").replace(" ", "").split(",");

            for (String str : spawner.getFriends()) {
                if (str == null || str.isEmpty()) continue;
                if (++i >= slotsSplit.length) i = 0;

                OfflinePlayer friend = Bukkit.getOfflinePlayer(str);
                ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.item", new String[]{
                        "{friend}"
                }, new String[]{
                        friend.getName()
                }).build();
                Integer slot = Integer.parseInt(slotsSplit[i]);
                InventoryUtils.Action action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                    spawner.getFriends().remove(str);
                    openFriendsMenu(player, spawner);
                });

                builders.add(ItemBuilder.build(item, slot, action));
            }
        }

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            if (items == null) continue;

            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            Integer slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            InventoryUtils.Action action = null;
            String actionStr = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            switch (actionStr.toUpperCase()) {
                case "ADD_FRIEND":
                    action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                        player.closeInventory();
                        for (String msg : Messages.ADD_FRIEND) {
                            if (msg == null) continue;

                            player.sendMessage(msg);
                        }

                        PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(player, spawner));
                    });
                    break;
            }

            builders.add(ItemBuilder.build(item, slot, action));
        }

        ItemStack nextPageItem = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Next-Page").build();
        ItemStack previousPageItem = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Previous-Page").build();

        int nextPageSlot = FileUtils.get().getInt(file, "Next-Page.slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Previous-Page.slot");

        InventoryBuilder.build(player, inventory, builders, nextPageSlot, previousPageSlot, nextPageItem, previousPageItem);
    }
}