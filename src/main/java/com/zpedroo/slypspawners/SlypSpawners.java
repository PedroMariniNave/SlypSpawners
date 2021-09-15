package com.zpedroo.slypspawners;

import com.zpedroo.slypspawners.commands.SpawnerCmd;
import com.zpedroo.slypspawners.listeners.PlayerChatListener;
import com.zpedroo.slypspawners.listeners.PlayerGeneralListeners;
import com.zpedroo.slypspawners.listeners.SpawnerListeners;
import com.zpedroo.slypspawners.managers.SpawnerManager;
import com.zpedroo.slypspawners.mysql.DBConnection;
import com.zpedroo.slypspawners.spawner.booster.BoosterManager;
import com.zpedroo.slypspawners.tasks.SaveTask;
import com.zpedroo.slypspawners.utils.FileUtils;
import com.zpedroo.slypspawners.utils.NumberFormatter;
import com.zpedroo.slypspawners.utils.config.Settings;
import com.zpedroo.slypspawners.utils.menu.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

public class SlypSpawners extends JavaPlugin {

    private static SlypSpawners instance;
    public static SlypSpawners get() { return instance; }

    public void onEnable() {
        instance = this;
        new FileUtils(this);

        if (!isMySQLEnabled(getConfig())) {
            getLogger().log(Level.SEVERE, "MySQL are disabled! You need to enable it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new DBConnection(getConfig());
        new NumberFormatter(getConfig());
        new Menus();
        new BoosterManager();
        new SaveTask(this);

        registerCommands();
        registerListeners();
    }

    public void onDisable() {
        if (!isMySQLEnabled(getConfig())) return;

        try {
            SpawnerManager.getInstance().saveAll();
            DBConnection.getInstance().closeConnection();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error ocurred while trying to save data!");
            ex.printStackTrace();
        }
    }

    private void registerCommands() {
        String command = Settings.MAIN_COMMAND;
        List<String> aliases = Settings.COMMAND_ALIASES;
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCmd = constructor.newInstance(command, this);
            pluginCmd.setAliases(aliases);
            pluginCmd.setExecutor(new SpawnerCmd());

            Field field = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(getName().toLowerCase(), pluginCmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(), this);
        getServer().getPluginManager().registerEvents(new SpawnerListeners(), this);
    }

    private Boolean isMySQLEnabled(FileConfiguration file) {
        if (!file.contains("MySQL.enabled")) return false;

        return file.getBoolean("MySQL.enabled");
    }
}