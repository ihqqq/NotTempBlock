package me.ihqqq.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.logging.Logger;

public final class PluginConfig {
    private final Set<String> enabledWorlds;
    private final boolean bypassPermissionEnabled;
    private final boolean worldGuardHookEnabled;

    private final boolean allBlocksEnabled;
    private final int allBlocksTimeSeconds;

    private final Map<Material, Integer> blockDelays;
    private final Map<EntityType, Integer> entityDelays;

    public PluginConfig(FileConfiguration config) {
        this.enabledWorlds = Set.copyOf(config.getStringList("enabled-worlds"));
        this.bypassPermissionEnabled = config.getBoolean("bypass-permission", true);
        this.worldGuardHookEnabled = config.getBoolean("worldguard-hook", false);

        this.allBlocksEnabled = config.getBoolean("all-blocks.enabled", false);
        this.allBlocksTimeSeconds = config.getInt("all-blocks.time-seconds", 20);

        this.blockDelays = parseBlockDelays(config);
        this.entityDelays = parseEntityDelays(config);
    }

    private static Map<Material, Integer> parseBlockDelays(FileConfiguration config) {
        var section = config.getConfigurationSection("blocks");
        if (section == null) return Map.of();

        Map<Material, Integer> map = new EnumMap<>(Material.class);
        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) {
                logWarning("Unknown material in config blocks section: '" + key + "' – skipping.");
                continue;
            }
            int time = section.getInt(key, -1);
            if (time <= 0) {
                logWarning("Invalid time for block '" + key + "' (must be > 0) – skipping.");
                continue;
            }
            map.put(material, time);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Map<EntityType, Integer> parseEntityDelays(FileConfiguration config) {
        var section = config.getConfigurationSection("entities");
        if (section == null) return Map.of();

        Map<EntityType, Integer> map = new EnumMap<>(EntityType.class);
        for (String key : section.getKeys(false)) {
            EntityType type;
            try {
                type = EntityType.valueOf(key.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                logWarning("Unknown entity type in config entities section: '" + key + "' – skipping.");
                continue;
            }
            int time = section.getInt(key, -1);
            if (time <= 0) {
                logWarning("Invalid time for entity '" + key + "' (must be > 0) – skipping.");
                continue;
            }
            map.put(type, time);
        }
        return Collections.unmodifiableMap(map);
    }

    private static void logWarning(String msg) {
        Logger.getLogger("notTempBlock").warning("[Config] " + msg);
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains(worldName);
    }

    public boolean isBypassPermissionEnabled() {
        return bypassPermissionEnabled;
    }

    public boolean isWorldGuardHookEnabled() {
        return worldGuardHookEnabled;
    }

    public boolean isAllBlocksEnabled() {
        return allBlocksEnabled;
    }

    public int getAllBlocksTimeSeconds() {
        return allBlocksTimeSeconds;
    }

    public OptionalInt getBlockDelay(Material material) {
        Integer delay = blockDelays.get(material);
        return delay != null ? OptionalInt.of(delay) : OptionalInt.empty();
    }

    public OptionalInt getEntityDelay(EntityType type) {
        Integer delay = entityDelays.get(type);
        return delay != null ? OptionalInt.of(delay) : OptionalInt.empty();
    }
}
