package me.ihqqq.listener;

import me.ihqqq.config.PluginConfig;
import me.ihqqq.notTempBlock;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;

public final class EntityPlaceListener implements Listener {

    private final notTempBlock plugin;

    public EntityPlaceListener(notTempBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityPlaceEarly(EntityPlaceEvent event) {
        PluginConfig config = plugin.getPluginConfig();
        Entity entity = event.getEntity();
        Player player = event.getPlayer();

        if (!config.isWorldEnabled(entity.getWorld().getName())) return;
        if (player == null) return;
        if (config.isBypassPermissionEnabled() && player.hasPermission("nottempblock.bypass")) return;
        if (!config.isWorldGuardHookEnabled()) return;

        plugin.getWorldGuardHook().ifPresent(hook -> {
            if (!hook.isAllowConfiguredBlocks(entity.getLocation())) return;

            EntityType type = entity.getType();
            if (config.getEntityDelay(type).isEmpty()) return;

            Location loc = entity.getLocation().clone();
            int seconds = config.getEntityDelay(type).getAsInt();

            event.setCancelled(true);
            plugin.getForceAllowedEntities().add(entity.getUniqueId().toString());

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Entity spawned = loc.getWorld().spawnEntity(loc, type);
                plugin.getRemovalManager().scheduleEntityRemoval(spawned, seconds);
            }, 1L);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        PluginConfig config = plugin.getPluginConfig();
        Entity entity = event.getEntity();
        Player player = event.getPlayer();

        if (!config.isWorldEnabled(entity.getWorld().getName())) return;
        if (player == null) return;
        if (config.isBypassPermissionEnabled() && player.hasPermission("nottempblock.bypass")) return;

        if (plugin.getForceAllowedEntities().contains(entity.getUniqueId().toString())) return;

        if (config.isWorldGuardHookEnabled()) {
            boolean allowed = plugin.getWorldGuardHook()
                    .map(hook -> hook.canEraseEntity(entity.getLocation()))
                    .orElse(true);
            if (!allowed) return;
        }

        config.getEntityDelay(entity.getType())
                .ifPresent(seconds ->
                        plugin.getRemovalManager().scheduleEntityRemoval(entity, seconds));
    }
}