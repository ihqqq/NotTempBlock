package me.ihqqq.listener;

import me.ihqqq.notTempBlock;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class BlockBreakListener implements Listener {

    private final notTempBlock plugin;
    private final Set<String> markedBreaks = Collections.synchronizedSet(new HashSet<>());

    public BlockBreakListener(notTempBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockBreakMark(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!plugin.getRemovalManager().isBlockTracked(block.getLocation())) return;
        markedBreaks.add(blockKey(block));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreakOverride(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!markedBreaks.contains(blockKey(block))) return;
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockBreakFinalize(BlockBreakEvent event) {
        Block block = event.getBlock();
        String key = blockKey(block);

        if (!markedBreaks.remove(key)) return;

        if (event.isCancelled()) return;

        plugin.getRemovalManager().cancelBlockRemoval(block.getLocation());
    }

    private static String blockKey(Block block) {
        return block.getWorld().getName()
                + ":" + block.getX()
                + ":" + block.getY()
                + ":" + block.getZ();
    }
}
