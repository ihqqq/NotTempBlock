package me.ihqqq.listener;

import me.ihqqq.config.PluginConfig;
import me.ihqqq.notTempBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.OptionalInt;

public final class BlockPlaceListener implements Listener {

    private final notTempBlock plugin;

    public BlockPlaceListener(notTempBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockPlaceEarly(BlockPlaceEvent event) {
        PluginConfig config = plugin.getPluginConfig();
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        if (!config.isWorldEnabled(block.getWorld().getName())) return;
        if (config.isBypassPermissionEnabled() && player.hasPermission("nottempblock.bypass")) return;
        if (!config.isWorldGuardHookEnabled()) return;

        plugin.getWorldGuardHook().ifPresent(hook -> {
            if (!hook.isAllowConfiguredBlocks(block.getLocation())) return;

            if (!config.isBlockTemporary(block.getType())) return;

            plugin.getForceAllowedBlocks().add(blockKey(block));
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockPlaceOverride(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (!plugin.getForceAllowedBlocks().contains(blockKey(block))) return;

        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        PluginConfig config = plugin.getPluginConfig();
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();
        String key = blockKey(block);

        if (!config.isWorldEnabled(block.getWorld().getName())) return;
        if (config.isBypassPermissionEnabled() && player.hasPermission("nottempblock.bypass")) return;

        boolean isForced = plugin.getForceAllowedBlocks().remove(key);

        if (!isForced && config.isWorldGuardHookEnabled()) {
            boolean allowed = plugin.getWorldGuardHook()
                    .map(hook -> hook.canEraseBlock(block.getLocation()))
                    .orElse(true);
            if (!allowed) return;
        }

        OptionalInt delay = resolveBlockDelay(config, block);
        delay.ifPresent(seconds -> plugin.getRemovalManager().scheduleBlockRemoval(block, seconds));
    }

    private static OptionalInt resolveBlockDelay(PluginConfig config, Block block) {
        if (config.isBlacklisted(block.getType())) {
            return OptionalInt.empty();
        }

        OptionalInt specific = config.getBlockDelay(block.getType());
        if (specific.isPresent()) {
            return specific;
        }

        if (config.isAllBlocksEnabled()) {
            return OptionalInt.of(config.getAllBlocksTimeSeconds());
        }

        return OptionalInt.empty();
    }

    private static String blockKey(Block block) {
        return block.getWorld().getName()
                + ":" + block.getX()
                + ":" + block.getY()
                + ":" + block.getZ();
    }
}