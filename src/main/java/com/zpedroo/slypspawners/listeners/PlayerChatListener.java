package com.zpedroo.slypspawners.listeners;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import com.zpedroo.slypspawners.utils.config.Messages;
import com.zpedroo.slypspawners.utils.menu.Menus;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class PlayerChatListener implements Listener {

    private static HashMap<Player, PlayerChat> playerChat;

    static {
        playerChat = new HashMap<>(64);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(ChatMessageEvent event) {
        if (!getPlayerChat().containsKey(event.getSender())) return;

        event.setCancelled(true);

        Player player = event.getSender();
        PlayerChat playerChat = getPlayerChat().remove(player);
        PlayerSpawner spawner = playerChat.getSpawner();

        Player friend = Bukkit.getPlayer(event.getMessage());
        if (friend == null) {
            player.sendMessage(Messages.OFFLINE_PLAYER);
            return;
        }

        if (spawner.getFriends().contains(friend.getUniqueId().toString()) || StringUtils.equals(player.getUniqueId().toString(), spawner.getOwnerUUID().toString())) {
            player.sendMessage(Messages.HAS_PERMISSION);
            return;
        }

        spawner.getFriends().add(friend.getUniqueId().toString());
        Menus.getInstance().openFriendsMenu(player, spawner);
    }

    public static HashMap<Player, PlayerChat> getPlayerChat() {
        return playerChat;
    }

    public static class PlayerChat {

        private Player player;
        private PlayerSpawner spawner;

        public PlayerChat(Player player, PlayerSpawner spawner) {
            this.player = player;
            this.spawner = spawner;
        }

        public Player getPlayer() {
            return player;
        }

        public PlayerSpawner getSpawner() {
            return spawner;
        }
    }
}