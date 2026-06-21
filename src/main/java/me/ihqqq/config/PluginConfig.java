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

    private final boolean countdownDisplayEnabled;
    private final String countdownDisplayFormat;
    private final double countdownDisplayYOffset;

    private final Map<Material, Integer> blockDelays;
    private final Map<EntityType, Integer> entityDelays;

    private final Set<Material> blacklistBlocks;

    public PluginConfig(FileConfiguration config) {
        this.enabledWorlds = Set.copyOf(config.getStringList("enabled-worlds"));
        this.bypassPermissionEnabled = config.getBoolean("bypass-permission", true);
        this.worldGuardHookEnabled = config.getBoolean("worldguard-hook", false);

        this.allBlocksEnabled = config.getBoolean("all-blocks.enabled", false);
        this.allBlocksTimeSeconds = config.getInt("all-blocks.time-seconds", 20);

        this.countdownDisplayEnabled = config.getBoolean("countdown-display.enabled", true);
        this.countdownDisplayFormat = config.getString("countdown-display.format", "&e%time%s");
        this.countdownDisplayYOffset = config.getDouble("countdown-display.y-offset", 1.2);

        this.blockDelays = parseBlockDelays(config);
        this.entityDelays = parseEntityDelays(config);
        this.blacklistBlocks = parseBlacklistBlocks(config);
    }

    private static Map<Material, Integer> parseBlockDelays(FileConfiguration config) {
        var section = config.getConfigurationSection("blocks");
        if (section == null) return Map.of();

        Map<Material, Integer> map = new EnumMap<>(Material.class);
        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) {
                logWarning("Loại block không hợp lệ trong cấu hình mục 'blocks': '" + key + "' – bỏ qua.");
                continue;
            }
            int time = section.getInt(key, -1);
            if (time <= 0) {
                logWarning("Thời gian không hợp lệ cho block '" + key + "' (phải > 0) – bỏ qua.");
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
                logWarning("Loại entity không hợp lệ trong cấu hình mục 'entities': '" + key + "' – bỏ qua.");
                continue;
            }
            int time = section.getInt(key, -1);
            if (time <= 0) {
                logWarning("Thời gian không hợp lệ cho entity '" + key + "' (phải > 0) – bỏ qua.");
                continue;
            }
            map.put(type, time);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Set<Material> parseBlacklistBlocks(FileConfiguration config) {
        List<String> rawList = config.getStringList("blacklist-blocks");
        if (rawList.isEmpty()) return Set.of();

        Set<Material> set = EnumSet.noneOf(Material.class);
        for (String key : rawList) {
            Material material = Material.matchMaterial(key);
            if (material == null) {
                logWarning("Loại block không hợp lệ trong cấu hình mục 'blacklist-blocks': '" + key + "' – bỏ qua.");
                continue;
            }
            set.add(material);
        }
        return Collections.unmodifiableSet(set);
    }

    private static void logWarning(String msg) {
        Logger.getLogger("notTempBlock").warning("[Cấu hình] " + msg);
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

    public boolean isBlockExplicitlyConfigured(Material material) {
        return blockDelays.containsKey(material);
    }

    public OptionalInt getEntityDelay(EntityType type) {
        Integer delay = entityDelays.get(type);
        return delay != null ? OptionalInt.of(delay) : OptionalInt.empty();
    }
    public Map<Material, Integer> getBlockDelays() {
        return blockDelays;
    }

    public Map<EntityType, Integer> getEntityDelays() {
        return entityDelays;
    }

    public boolean isCountdownDisplayEnabled() {
        return countdownDisplayEnabled;
    }

    public String getCountdownDisplayFormat() {
        return countdownDisplayFormat;
    }

    public double getCountdownDisplayYOffset() {
        return countdownDisplayYOffset;
    }

    public Set<Material> getBlacklistBlocks() {
        return blacklistBlocks;
    }

    public boolean isBlacklisted(Material material) {
        return blacklistBlocks.contains(material);
    }

    public boolean isBlockTemporary(Material material) {
        if (isBlacklisted(material)) {
            return false;
        }
        return isBlockExplicitlyConfigured(material) || isAllBlocksEnabled();
    }
}