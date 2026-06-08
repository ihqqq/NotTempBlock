package me.ihqqq;

import me.ihqqq.command.NotTempBlockCommand;
import me.ihqqq.config.PluginConfig;
import me.ihqqq.hook.WorldGuardHook;
import me.ihqqq.listener.BlockPlaceListener;
import me.ihqqq.listener.EntityPlaceListener;
import me.ihqqq.manager.RemovalManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.logging.Level;

public final class notTempBlock extends JavaPlugin {

    private PluginConfig pluginConfig;
    private RemovalManager removalManager;
    private WorldGuardHook worldGuardHook;

    @Override
    public void onEnable() {
        reloadPluginConfig();

        this.removalManager = new RemovalManager(this);

        loadWorldGuardHook();
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


    private void loadWorldGuardHook() {
        if (!pluginConfig.isWorldGuardHookEnabled()) return;

        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().warning("WorldGuard hook is enabled in config but WorldGuard is not installed. Disabling hook.");
            return;
        }

        try {
            worldGuardHook = new WorldGuardHook();
            worldGuardHook.registerFlags();
            getLogger().info("WorldGuard hook enabled.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialise WorldGuard hook.", e);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityPlaceListener(this), this);
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
}
