package me.ihqqq.command;

import me.ihqqq.gui.BlacklistGUI;
import me.ihqqq.gui.BlockWhitelistGUI;
import me.ihqqq.notTempBlock;
import me.ihqqq.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NotTempBlockCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = List.of("about", "reload", "gui", "blacklist");

    private final notTempBlock plugin;

    public NotTempBlockCommand(notTempBlock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("about")) {
            if (!sender.hasPermission("nottempblock.about")) {
                sender.sendMessage(noPermission());
                return true;
            }
            sendAbout(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nottempblock.reload")) {
                sender.sendMessage(noPermission());
                return true;
            }
            executeReload(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("whitelist")) {
            if (!sender.hasPermission("nottempblock.gui")) {
                sender.sendMessage(noPermission());
                return true;
            }
            openWhitelistGui(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("blacklist")) {
            if (!sender.hasPermission("nottempblock.gui")) {
                sender.sendMessage(noPermission());
                return true;
            }
            openBlacklistGui(sender);
            return true;
        }

        sender.sendMessage(MessageUtil.withPrefix(
                Component.text("Cách dùng: /" + label + " <about|reload|gui|blacklist>", NamedTextColor.RED)));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUB_COMMANDS.stream()
                    .filter(s -> s.startsWith(partial))
                    .toList();
        }
        return List.of();
    }

    private void openWhitelistGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.withPrefix(
                    Component.text("Lệnh này chỉ có thể được sử dụng trong game.", NamedTextColor.RED)));
            return;
        }
        new BlockWhitelistGUI(plugin, 0).open(player);
    }

    private void openBlacklistGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.withPrefix(
                    Component.text("Lệnh này chỉ có thể được sử dụng trong game.", NamedTextColor.RED)));
            return;
        }
        new BlacklistGUI(plugin, 0).open(player);
    }

    private void sendAbout(CommandSender sender) {
        var desc = plugin.getDescription();

        sender.sendMessage(MessageUtil.withPrefix(
                MessageUtil.pluginTitle()
                        .decoration(TextDecoration.BOLD, true)
                        .append(Component.text(" v" + desc.getVersion(), NamedTextColor.GRAY))));

        sender.sendMessage(MessageUtil.withPrefix(
                Component.text("Tác giả: ", NamedTextColor.GRAY)
                        .append(Component.text(String.join(", ", desc.getAuthors()), NamedTextColor.WHITE))));

        sender.sendMessage(MessageUtil.withPrefix(
                Component.text("Tác vụ xóa đang chạy: ", NamedTextColor.GRAY)
                        .append(Component.text(String.valueOf(plugin.getRemovalManager().activeTaskCount()),
                                NamedTextColor.WHITE))));

        if (desc.getWebsite() != null && !desc.getWebsite().isEmpty()) {
            sender.sendMessage(MessageUtil.withPrefix(
                    Component.text("Website: ", NamedTextColor.GRAY)
                            .append(Component.text(desc.getWebsite(), NamedTextColor.WHITE))));
        }
    }

    private void executeReload(CommandSender sender) {
        sender.sendMessage(MessageUtil.withPrefix(
                Component.text("Đang tải lại cấu hình...", NamedTextColor.AQUA)));
        try {
            plugin.reloadPluginConfig();
            sender.sendMessage(MessageUtil.withPrefix(
                    Component.text("✔ ", NamedTextColor.GREEN)
                            .append(Component.text("Tải lại cấu hình thành công.", NamedTextColor.GREEN))));
        } catch (Exception e) {
            sender.sendMessage(MessageUtil.withPrefix(
                    Component.text("⚠ ", NamedTextColor.RED)
                            .append(Component.text("Tải lại thất bại – kiểm tra console để biết thêm chi tiết.",
                                    NamedTextColor.RED))));
            plugin.getLogger().severe("Lỗi khi tải lại cấu hình: " + e.getMessage());
        }
    }

    private static Component noPermission() {
        return MessageUtil.withPrefix(
                Component.text("⚠ ", NamedTextColor.RED)
                        .append(Component.text("Bạn không có quyền thực hiện thao tác này.", NamedTextColor.RED)));
    }
}