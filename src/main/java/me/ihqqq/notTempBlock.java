package me.ihqqq;

import me.ihqqq.command.NotTempBlockCommand;
import me.ihqqq.config.PluginConfig;
import me.ihqqq.hook.WorldGuardHook;
import me.ihqqq.listener.BlockBreakListener;
import me.ihqqq.listener.BlockPlaceListener;
import me.ihqqq.listener.EntityPlaceListener;
import me.ihqqq.listener.PlayerInteractListener;
import me.ihqqq.listener.WhitelistGUIListener;
import me.ihqqq.manager.RemovalManager;
import me.ihqqq.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public final class notTempBlock extends JavaPlugin {

    public static notTempBlock plugin;

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
        plugin = this;

        reloadPluginConfig();

        this.removalManager = new RemovalManager(this);

        validateWorldGuardHook();
        registerListeners();
        registerCommands();

        printEnableBanner();
    }

    @Override
    public void onDisable() {
        printDisableBanner();
        if (removalManager != null) {
            removalManager.cancelAll();
        }
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
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new WhitelistGUIListener(this), this);
    }

    private void registerCommands() {
        var cmd = getCommand("nottempblock");
        if (cmd != null) {
            var handler = new NotTempBlockCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    private void printEnableBanner() {
        String ver     = getDescription().getVersion();
        String authors = String.join(", ", getDescription().getAuthors());

        boolean wgEnabledInConfig = pluginConfig.isWorldGuardHookEnabled();
        boolean wgHooked          = worldGuardHook != null;

        Component wgStatus = !wgEnabledInConfig
                ? Component.text("Tắt", NamedTextColor.RED)
                : (wgHooked
                   ? Component.text("Hoạt động", NamedTextColor.GREEN)
                   : Component.text("Bật ", NamedTextColor.YELLOW)
                .append(Component.text("(WorldGuard chưa cài)", NamedTextColor.DARK_GRAY)));

        Component allBlocksStatus = pluginConfig.isAllBlocksEnabled()
                ? Component.text("BẬT", NamedTextColor.GREEN)
                : Component.text("TẮT", NamedTextColor.GRAY);

        Component bypassStatus = pluginConfig.isBypassPermissionEnabled()
                ? Component.text("BẬT", NamedTextColor.GREEN)
                : Component.text("TẮT", NamedTextColor.GRAY);

        Component sep = separator();

        MessageUtil.log(sep);
        MessageUtil.log(Component.empty());
        MessageUtil.log(Component.text()
                .append(Component.text("    ", NamedTextColor.WHITE))
                .append(MessageUtil.pluginTitle().decoration(TextDecoration.BOLD, true))
                .append(Component.text("  ", NamedTextColor.GRAY))
                .append(Component.text("»", NamedTextColor.DARK_GRAY))
                .append(Component.text(" v" + ver, NamedTextColor.GRAY))
                .build());
        MessageUtil.log(Component.text()
                .append(Component.text("    ", NamedTextColor.WHITE))
                .append(Component.text("Tác giả", NamedTextColor.GRAY))
                .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                .append(Component.text(authors, NamedTextColor.GREEN))
                .build());
        MessageUtil.log(Component.empty());
        MessageUtil.log(sep);
        MessageUtil.log(Component.empty());
        MessageUtil.log(statusLine("WorldGuard", wgStatus));
        MessageUtil.log(statusLine("Bypass permission", bypassStatus));
        MessageUtil.log(statusLine("Tất cả block tạm thời", allBlocksStatus));
        MessageUtil.log(statusLine("Block đã cấu hình", Component.text(
                String.valueOf(pluginConfig.getBlockDelays().size()), NamedTextColor.AQUA)));
        MessageUtil.log(statusLine("Entity đã cấu hình", Component.text(
                String.valueOf(pluginConfig.getEntityDelays().size()), NamedTextColor.AQUA)));
        MessageUtil.log(statusLine("Blacklist", Component.text(
                String.valueOf(pluginConfig.getBlacklistBlocks().size()) + " block", NamedTextColor.AQUA)));
        MessageUtil.log(Component.empty());
        MessageUtil.log(sep);
        MessageUtil.log(Component.text()
                .append(Component.text("  ✔ ", NamedTextColor.GREEN))
                .append(Component.text("NotTempBlock đã khởi động thành công!", NamedTextColor.GREEN))
                .build());
        MessageUtil.log(sep);
    }

    private void printDisableBanner() {
        Component sep = separator();

        MessageUtil.log(sep);
        MessageUtil.log(Component.text()
                .append(Component.text("  ✘ ", NamedTextColor.RED))
                .append(Component.text("NotTempBlock đang tắt...", NamedTextColor.RED))
                .build());
        MessageUtil.log(Component.text("  Đang hủy các tác vụ xóa đang chờ...", NamedTextColor.GRAY));
        MessageUtil.log(Component.text()
                .append(Component.text("  Plugin thuộc sở hữu của ", NamedTextColor.DARK_GRAY))
                .append(Component.text("NotMC", NamedTextColor.YELLOW))
                .append(Component.text(" — Leak là con chó ghẻ!", NamedTextColor.DARK_GRAY))
                .build());
        MessageUtil.log(sep);
    }

    private static Component statusLine(String label, Component status) {
        return Component.text()
                .append(Component.text("  » ", NamedTextColor.DARK_GRAY))
                .append(Component.text(label, NamedTextColor.GRAY))
                .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                .append(status)
                .build();
    }

    private static Component separator() {
        return Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY);
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