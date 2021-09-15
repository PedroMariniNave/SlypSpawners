package com.zpedroo.slypspawners.utils.config;

import com.zpedroo.slypspawners.utils.FileUtils;

import java.util.List;

public class Settings {

    public static final String GIVE_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.keys.give");

    public static final String BOOSTER_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.keys.booster");

    public static final Long SAVE_INTERVAL = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.save-interval");

    public static final Long MAX_BOOSTER = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.max-booster");

    public static final Integer STACK_RADIUS = FileUtils.get().getInt(FileUtils.Files.CONFIG, "Settings.stack-radius");

    public static final Double HOLOGRAM_HEIGHT = FileUtils.get().getDouble(FileUtils.Files.CONFIG, "Settings.hologram-height");

    public static final String MAIN_COMMAND = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command");

    public static final List<String> COMMAND_ALIASES = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.aliases");
}