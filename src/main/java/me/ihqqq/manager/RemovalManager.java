package me.ihqqq.manager;

import me.ihqqq.notTempBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RemovalManager {

    private final notTempBlock plugin;

    private final Map<String, BukkitTask> activeTasks = new HashMap<>();

    public RemovalManager(notTempBlock plugin) {
        this.plugin = plugin;
    }

    public void scheduleBlockRemoval(Block block, int delaySeconds) {
        String key = blockKey(block.getLocation());

        cancelTask(key);

        long ticks = (long) delaySeconds * 20L;

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            activeTasks.remove(key);
            removeBlock(block);
        }, ticks);

        activeTasks.put(key, task);
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
    }

    public void cancelBlockRemoval(Location loc) {
        cancelTask(blockKey(loc));
    }

    public void cancelEntityRemoval(UUID uuid) { cancelTask(entityKey(uuid)); }

    public int activeTaskCount() { return activeTasks.size(); }

    private void cancelTask(String key) {
        BukkitTask existing = activeTasks.remove(key);
        if(existing != null) {
            existing.cancel();
        }
    }

    private void removeBlock(Block block) {
        Location loc = block.getLocation();
        if(!isChunkLoaded(loc)) return;

        if(block.getType() == Material.AIR) return;

        block.setType(Material.AIR);
    }

    private void removeEntity(UUID uuid) {
        Entity entity = plugin.getServer().getEntity(uuid);
        if(entity != null && !entity.isDead()) {
            entity.remove();
        }
    }

    public boolean isBlockTracked(Location loc) {
        return activeTasks.containsKey(blockKey(loc));
    }

    private boolean isChunkLoaded(Location loc) {
        if(loc.getWorld() == null) return false;
        return loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
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