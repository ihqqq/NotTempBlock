package me.ihqqq.listener;

import me.ihqqq.config.PluginConfig;
import me.ihqqq.notTempBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class PlayerInteractListener implements Listener {

    private final notTempBlock plugin;
    private final Set<String> markedInteracts = Collections.synchronizedSet(new HashSet<>());

    public PlayerInteractListener(notTempBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractEarly(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        Player player = event.getPlayer();
        PluginConfig config = plugin.getPluginConfig();

        Block target = clickedBlock.getRelative(event.getBlockFace());

        if (!config.isWorldEnabled(target.getWorld().getName())) return;
        if (config.isBypassPermissionEnabled() && player.hasPermission("nottempblock.bypass")) return;
        if (!config.isWorldGuardHookEnabled()) return;

        Material material = item.getType();
        if (!material.isBlock()) return;

        boolean isConfigured = config.isAllBlocksEnabled()
                || config.getBlockDelay(material).isPresent();
        if (!isConfigured) return;

        plugin.getWorldGuardHook().ifPresent(hook -> {
            if (!hook.isAllowConfiguredBlocks(target.getLocation())) return;
            markedInteracts.add(player.getUniqueId().toString());
            event.setCancelled(true);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteractOverride(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!markedInteracts.remove(event.getPlayer().getUniqueId().toString())) return;
        event.setCancelled(false);
    }
}
