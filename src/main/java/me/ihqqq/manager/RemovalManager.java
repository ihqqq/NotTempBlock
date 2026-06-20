package me.ihqqq.manager;

import me.ihqqq.config.PluginConfig;
import me.ihqqq.notTempBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RemovalManager {

    private final notTempBlock plugin;

    private final Map<String, BukkitTask> activeTasks = new HashMap<>();
    private final Map<String, TextDisplay> activeDisplays = new HashMap<>();

    public RemovalManager(notTempBlock plugin) {
        this.plugin = plugin;
    }

    public void scheduleBlockRemoval(Block block, int delaySeconds) {
        String key = blockKey(block.getLocation());

        cancelTask(key);
        removeDisplay(key);

        PluginConfig config = plugin.getPluginConfig();
        boolean showCountdown = config.isCountdownDisplayEnabled();

        if (showCountdown) {
            spawnDisplay(key, block.getLocation(), delaySeconds);
        }

        int[] remaining = {delaySeconds};
        BukkitTask[] taskHolder = new BukkitTask[1];

        taskHolder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            remaining[0]--;

            if (remaining[0] <= 0) {
                activeTasks.remove(key);
                removeBlock(block);
                removeDisplay(key);
                taskHolder[0].cancel();
                return;
            }

            if (showCountdown) {
                updateDisplayText(key, remaining[0]);
            }
        }, 20L, 20L);

        activeTasks.put(key, taskHolder[0]);
    }

    public void scheduleEntityRemoval(Entity entity, int delaySeconds) {
        UUID uuid = entity.getUniqueId();
        String key = entityKey(uuid);

        cancelTask(key);

        long ticks = (long) delaySeconds * 20L;

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            activeTasks.remove(key);
            removeEntity(uuid);
        }, ticks);

        activeTasks.put(key, task);
    }

    public void cancelAll() {
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();

        activeDisplays.values().forEach(display -> {
            if (!display.isDead()) display.remove();
        });
        activeDisplays.clear();
    }

    public void cancelBlockRemoval(Location loc) {
        String key = blockKey(loc);
        cancelTask(key);
        removeDisplay(key);
    }

    public void cancelEntityRemoval(UUID uuid) { cancelTask(entityKey(uuid)); }

    public int activeTaskCount() { return activeTasks.size(); }

    private void cancelTask(String key) {
        BukkitTask existing = activeTasks.remove(key);
        if (existing != null) {
            existing.cancel();
        }
    }

    private void removeBlock(Block block) {
        Location loc = block.getLocation();
        if (!isChunkLoaded(loc)) return;

        if (block.getType() == Material.AIR) return;

        block.setType(Material.AIR);
    }

    private void removeEntity(UUID uuid) {
        Entity entity = plugin.getServer().getEntity(uuid);
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }

    public boolean isBlockTracked(Location loc) {
        return activeTasks.containsKey(blockKey(loc));
    }

    private boolean isChunkLoaded(Location loc) {
        if (loc.getWorld() == null) return false;
        return loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    private void spawnDisplay(String key, Location blockLocation, int initialSeconds) {
        if (blockLocation.getWorld() == null) return;
        if (!isChunkLoaded(blockLocation)) return;

        PluginConfig config = plugin.getPluginConfig();
        Location displayLoc = blockLocation.clone().add(0.5, config.getCountdownDisplayYOffset(), 0.5);

        TextDisplay display = blockLocation.getWorld().spawn(displayLoc, TextDisplay.class, td -> {
            td.text(formatCountdownText(initialSeconds));
            td.setBillboard(Display.Billboard.CENTER);
            td.setPersistent(false);
            td.setShadowed(true);
            td.setDefaultBackground(false);
        });

        activeDisplays.put(key, display);
    }

    private void updateDisplayText(String key, int secondsLeft) {
        TextDisplay display = activeDisplays.get(key);
        if (display != null && !display.isDead()) {
            display.text(formatCountdownText(secondsLeft));
        }
    }

    private void removeDisplay(String key) {
        TextDisplay display = activeDisplays.remove(key);
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }

    private Component formatCountdownText(int secondsLeft) {
        String raw = plugin.getPluginConfig().getCountdownDisplayFormat()
                .replace("%time%", String.valueOf(secondsLeft));
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }

    private static String blockKey(Location loc) {
        return "block" + loc.getWorld().getName()
                + ":" + loc.getBlockX()
                + ":" + loc.getBlockY()
                + ":" + loc.getBlockZ();
    }

    private static String entityKey(UUID uuid) {
        return "entity:" + uuid;
    }
}