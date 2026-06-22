package me.ihqqq.gui;

import me.ihqqq.notTempBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class BlockWhitelistGUI implements InventoryHolder {

    private static final String TITLE_PREFIX = "Whitelist Block";

    private static final int ROWS = 6;
    public static final int SIZE = ROWS * 9;
    public static final int ITEMS_PER_PAGE = 45;

    public static final int SLOT_PREV = 45;
    public static final int SLOT_WORLDS = 46;
    public static final int SLOT_TOGGLE_ALL = 47;
    public static final int SLOT_BLACKLIST = 48;
    public static final int SLOT_ADD = 49;
    public static final int SLOT_CLOSE = 51;
    public static final int SLOT_NEXT = 53;

    public static final int STEP_SECONDS = 5;
    public static final int MIN_DELAY_SECONDS = 1;

    public static final int DEFAULT_NEW_DELAY_SECONDS = STEP_SECONDS;

    private final notTempBlock plugin;
    private final int page;
    private final List<Material> entries;
    private Inventory inventory;

    public BlockWhitelistGUI(notTempBlock plugin, int requestedPage) {
        this.plugin = plugin;
        this.entries = plugin.getPluginConfig().getBlockDelays().keySet().stream()
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());

        int maxPage = entries.isEmpty() ? 0 : (entries.size() - 1) / ITEMS_PER_PAGE;
        this.page = Math.max(0, Math.min(requestedPage, maxPage));

        build();
    }

    private void build() {
        Component title = Component.text()
                .append(Component.text(TITLE_PREFIX, NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("   Trang " + (page + 1), NamedTextColor.GRAY))
                .build();

        this.inventory = Bukkit.createInventory(this, SIZE, title);

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, entries.size());

        for (int i = start; i < end; i++) {
            Material material = entries.get(i);
            int delay = plugin.getPluginConfig().getBlockDelay(material).orElse(0);
            inventory.setItem(i - start, buildBlockIcon(material, delay));
        }

        if (entries.isEmpty()) {
            inventory.setItem(22, GuiItems.named(Material.PAPER, NamedTextColor.GRAY,
                    "Chưa có block nào",
                    List.of(
                            "Cầm một block và bấm",
                            "\"Thêm block\" để bắt đầu."
                    )));
        }

        ItemStack filler = GuiItems.filler();
        for (int slot : new int[]{50, 52}) {
            inventory.setItem(slot, filler);
        }

        if (page > 0) {
            inventory.setItem(SLOT_PREV, GuiItems.named(Material.ARROW, NamedTextColor.YELLOW,
                    "Trang trước", List.of()));
        }
        if (end < entries.size()) {
            inventory.setItem(SLOT_NEXT, GuiItems.named(Material.ARROW, NamedTextColor.YELLOW,
                    "Trang sau", List.of()));
        }

        inventory.setItem(SLOT_WORLDS, GuiItems.named(Material.COMPASS, NamedTextColor.AQUA,
                "Quản lý World",
                List.of("Bật/tắt tính năng theo từng world.")));

        boolean allBlocksEnabled = plugin.getPluginConfig().isAllBlocksEnabled();
        inventory.setItem(SLOT_TOGGLE_ALL, GuiItems.named(
                allBlocksEnabled ? Material.LIME_DYE : Material.GRAY_DYE,
                allBlocksEnabled ? NamedTextColor.GREEN : NamedTextColor.GRAY,
                (allBlocksEnabled ? "✔ " : "✕ ") + "Tất cả block: " + (allBlocksEnabled ? "BẬT" : "TẮT"),
                List.of(
                        "Áp dụng cho mọi block đặt mới,",
                        "trừ block trong Whitelist & Blacklist."
                )));

        int blacklistCount = plugin.getPluginConfig().getBlacklistBlocks().size();
        inventory.setItem(SLOT_BLACKLIST, GuiItems.named(Material.BOOK, NamedTextColor.RED,
                "Blacklist (" + blacklistCount + ")",
                List.of("Block không bao giờ bị tự xóa.")));

        inventory.setItem(SLOT_ADD, GuiItems.named(Material.EMERALD, NamedTextColor.GREEN,
                "Thêm block từ tay cầm",
                List.of(
                        "Mặc định " + DEFAULT_NEW_DELAY_SECONDS + "s.",
                        "Trái/Phải để +/-5s khi đã có."
                )));

        inventory.setItem(SLOT_CLOSE, GuiItems.named(Material.BARRIER, NamedTextColor.RED,
                "Đóng", List.of()));
    }

    private ItemStack buildBlockIcon(Material material, int delaySeconds) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(material.name(), NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Xóa sau: ", NamedTextColor.GRAY)
                .append(Component.text(delaySeconds + "s", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Trái +5s   Phải -5s", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("⚠ Shift: xóa khỏi danh sách", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public int getPage() {
        return page;
    }

    public Material getMaterialAt(int slot) {
        if (slot < 0 || slot >= ITEMS_PER_PAGE) return null;
        int index = page * ITEMS_PER_PAGE + slot;
        if (index >= entries.size()) return null;
        return entries.get(index);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}