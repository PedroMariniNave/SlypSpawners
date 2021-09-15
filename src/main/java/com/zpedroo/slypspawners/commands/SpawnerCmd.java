package com.zpedroo.slypspawners.commands;

import com.zpedroo.slypspawners.managers.SpawnerManager;
import com.zpedroo.slypspawners.spawner.Spawner;
import com.zpedroo.slypspawners.spawner.booster.BoosterManager;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import com.zpedroo.slypspawners.utils.config.Messages;
import com.zpedroo.slypspawners.utils.config.Settings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spigotmc.SpigotConfig;

import java.math.BigInteger;

public class SpawnerCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spawners.admin")) {
            sender.sendMessage(SpigotConfig.unknownCommandMessage);
            return true;
        }

        if (args.length <= 0) {
            sendHelp(sender);
            return true;
        }

        Player target = null;
        BigInteger stack = null;
        CommandKeys key = getKey(args[0].toUpperCase());
        boolean executed = false;
        if (key != null) {
            switch (key) {
                case GIVE:
                    executed = true;

                    if (args.length < 4) {
                        sendHelp(sender);
                        break;
                    }

                    Spawner spawner = SpawnerManager.getInstance().getSpawner(args[2]);
                    if (spawner == null) {
                        sender.sendMessage(Messages.INVALID_SPAWNER);
                        break;
                    }

                    stack = NumberFormatter.getInstance().filter(args[3]);
                    if (stack.signum() <= 0) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        break;
                    }

                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(Messages.OFFLINE_PLAYER);
                        break;
                    }

                    target.getInventory().addItem(spawner.getItem(stack));
                    break;
                case BOOSTER:
                    executed = true;

                    if (args.length < 4) {
                        sendHelp(sender);
                        break;
                    }

                    Long ticks = null;
                    Integer amount = null;

                    try {
                        ticks = Long.parseLong(args[2]);
                        amount = Integer.parseInt(args[3]);
                    } catch (Exception ex) {
                        // ignore
                    }

                    if (ticks == null || ticks <= 0 || amount == null || amount <= 0) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        break;
                    }

                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(Messages.OFFLINE_PLAYER);
                        break;
                    }

                    target.getInventory().addItem(BoosterManager.getInstance().getItem(ticks, amount));
                    break;
            }
        }

        if (!executed) {
            sendHelp(sender);
        }
        return false;
    }

    private void sendHelp(CommandSender sender) {
        for (String msg : Messages.HELP) {
            if (msg == null) continue;

            sender.sendMessage(msg);
        }
    }

    private CommandKeys getKey(String str) {
        if (str == null || str.isEmpty()) return null;

        for (CommandKeys keys : CommandKeys.values()) {
            if (StringUtils.equals(keys.getKey(), str)) return keys;
        }

        return null;
    }

    enum CommandKeys {
        GIVE(Settings.GIVE_KEY),
        BOOSTER(Settings.BOOSTER_KEY);

        private String key;

        CommandKeys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}