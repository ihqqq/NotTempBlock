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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        PluginConfig config = plugin.getPluginConfig();
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        if (!config.isWorldEnabled(block.getWorld().getName())) return;

        if (config.isBypassPermissionEnabled() && player.hasPermission("nottempblock.bypass")) return;

        if (config.isWorldGuardHookEnabled()) {
            boolean allowed = plugin.getWorldGuardHook()
                    .map(hook -> hook.canEraseBlock(block.getLocation()))
                    .orElse(true);
            if (!allowed) return;
        }

        OptionalInt delay = resolveBlockDelay(config, block);
        delay.ifPresent(seconds ->
                plugin.getRemovalManager().scheduleBlockRemoval(block, seconds));
    }



    private static OptionalInt resolveBlockDelay(PluginConfig config, Block block) {
        if (config.isAllBlocksEnabled()) {
            return OptionalInt.of(config.getAllBlocksTimeSeconds());
        }
        return config.getBlockDelay(block.getType());
    }
}
