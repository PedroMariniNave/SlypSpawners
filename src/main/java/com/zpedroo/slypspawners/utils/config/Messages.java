package com.zpedroo.slypspawners.utils.config;

import com.zpedroo.slypspawners.utils.FileUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    public static final String ENABLED = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.enabled"));

    public static final String DISABLED = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.disabled"));

    public static final String INVALID_SPAWNER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-spawner"));

    public static final String INVALID_AMOUNT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-amount"));

    public static final String OFFLINE_PLAYER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.offline-player"));

    public static final String BOOST_REFOUND = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.boost-refound"));

    public static final String NEAR_SPAWNER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.near-spawner"));

    public static final String ONLY_OWNER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.only-owner"));

    public static final String HAS_PERMISSION = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.has-permission"));

    public static final String NEED_PERMISSION = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.need-permission"));

    public static final List<String> HELP = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.help"));

    public static final List<String> ADD_FRIEND = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.add-friend"));

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private static List<String> getColored(List<String> list) {
        List<String> colored = new ArrayList<>();
        for (String str : list) {
            colored.add(getColored(str));
        }

        return colored;
    }
}