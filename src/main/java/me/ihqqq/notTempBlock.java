package me.ihqqq;

import me.ihqqq.command.NotTempBlockCommand;
import me.ihqqq.config.PluginConfig;
import me.ihqqq.hook.WorldGuardHook;
import me.ihqqq.listener.BlockBreakListener;
import me.ihqqq.listener.BlockPlaceListener;
import me.ihqqq.listener.EntityPlaceListener;
import me.ihqqq.manager.RemovalManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public final class notTempBlock extends JavaPlugin {

    private PluginConfig pluginConfig;
    private RemovalManager removalManager;
    private WorldGuardHook worldGuardHook;

    private final Set<String> forceAllowedBlocks = Collections.synchronizedSet(new HashSet<>());

    private final Set<String> forceAllowedEntities = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                worldGuardHook = new WorldGuardHook();
                worldGuardHook.registerFlags();
                getLogger().info("WorldGuard flags registered successfully.");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to register WorldGuard flags in onLoad().", e);
                worldGuardHook = null;
            }
        }
    }

    @Override
    public void onEnable() {
        reloadPluginConfig();

        this.removalManager = new RemovalManager(this);

        validateWorldGuardHook();
        registerListeners();
        registerCommands();

        getLogger().info("notTempBlock v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (removalManager != null) {
            removalManager.cancelAll();
        }
        getLogger().info("notTempBlock disabled – all pending tasks cancelled.");
    }

    public void reloadPluginConfig() {
        saveDefaultConfig();
        reloadConfig();
        this.pluginConfig = new PluginConfig(getConfig());
        getLogger().info("Configuration loaded successfully.");
    }

    private void validateWorldGuardHook() {
        if (!pluginConfig.isWorldGuardHookEnabled()) {
            worldGuardHook = null;
            return;
        }

        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().warning("WorldGuard hook is enabled in config but WorldGuard is not installed. Disabling hook.");
            worldGuardHook = null;
            return;
        }

        if (worldGuardHook != null) {
            getLogger().info("WorldGuard hook enabled.");
        } else {
            getLogger().warning("WorldGuard hook could not be initialised (check onLoad errors).");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    private void registerCommands() {
        var cmd = getCommand("nottempblock");
        if (cmd != null) {
            var handler = new NotTempBlockCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public RemovalManager getRemovalManager() {
        return removalManager;
    }

    public Optional<WorldGuardHook> getWorldGuardHook() {
        return Optional.ofNullable(worldGuardHook);
    }

    public Set<String> getForceAllowedBlocks() {
        return forceAllowedBlocks;
    }

    public Set<String> getForceAllowedEntities() {
        return forceAllowedEntities;
    }
}