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

public final class BlacklistGUI implements InventoryHolder {

    private static final String TITLE_PREFIX = "Blacklist Block";

    private static final int ROWS = 6;
    public static final int SIZE = ROWS * 9;
    public static final int ITEMS_PER_PAGE = 45;

    public static final int SLOT_PREV = 45;
    public static final int SLOT_BACK = 46;
    public static final int SLOT_ADD = 49;
    public static final int SLOT_CLOSE = 51;
    public static final int SLOT_NEXT = 53;

    private final notTempBlock plugin;
    private final int page;
    private final List<Material> entries;
    private Inventory inventory;

    public BlacklistGUI(notTempBlock plugin, int requestedPage) {
        this.plugin = plugin;
        this.entries = new ArrayList<>(plugin.getPluginConfig().getBlacklistBlocks());
        this.entries.sort(Comparator.comparing(Enum::name));

        int maxPage = entries.isEmpty() ? 0 : (entries.size() - 1) / ITEMS_PER_PAGE;
        this.page = Math.max(0, Math.min(requestedPage, maxPage));

        build();
    }

    private void build() {
        Component title = Component.text()
                .append(Component.text(TITLE_PREFIX, NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .append(Component.text("  (Trang " + (page + 1) + ")", NamedTextColor.GRAY))
                .build();

        this.inventory = Bukkit.createInventory(this, SIZE, title);

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, entries.size());

        for (int i = start; i < end; i++) {
            Material material = entries.get(i);
            inventory.setItem(i - start, buildBlockIcon(material));
        }

        if (entries.isEmpty()) {
            inventory.setItem(22, GuiItems.named(Material.PAPER, NamedTextColor.GRAY,
                    "Danh sách trống",
                    List.of("Chưa có block nào trong blacklist.",
                            "Cầm một block và click \"Thêm block từ tay cầm\" bên dưới.",
                            "",
                            "Khi \"Tất cả block\" đang BẬT, đây là nơi",
                            "khai báo các block sẽ KHÔNG bị xóa.")));
        }

        ItemStack filler = GuiItems.filler();
        for (int slot : new int[]{47, 48, 50, 52}) {
            inventory.setItem(slot, filler);
        }

        if (page > 0) {
            inventory.setItem(SLOT_PREV, GuiItems.named(Material.ARROW, NamedTextColor.YELLOW,
                    "« Trang trước", List.of()));
        }
        if (end < entries.size()) {
            inventory.setItem(SLOT_NEXT, GuiItems.named(Material.ARROW, NamedTextColor.YELLOW,
                    "Trang sau »", List.of()));
        }

        inventory.setItem(SLOT_BACK, GuiItems.named(Material.ARROW, NamedTextColor.YELLOW,
                "« Quay lại Whitelist", List.of()));

        inventory.setItem(SLOT_ADD, GuiItems.named(Material.EMERALD, NamedTextColor.GREEN,
                "+ Thêm block từ tay cầm",
                List.of(
                        "Cầm một block trên tay rồi",
                        "click vào đây để thêm vào",
                        "danh sách blacklist.",
                        "",
                        "Block trong danh sách này sẽ",
                        "KHÔNG BAO GIỜ bị tự động xóa,",
                        "kể cả khi \"Tất cả block\" đang BẬT.",
                        "",
                        "Mẹo: bạn cũng có thể click",
                        "thẳng vào item trong kho đồ",
                        "của mình — trái để thêm,",
                        "phải để xóa."
                )));

        inventory.setItem(SLOT_CLOSE, GuiItems.named(Material.BARRIER, NamedTextColor.RED,
                "Đóng", List.of()));
    }

    private ItemStack buildBlockIcon(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(material.name(), NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Trạng thái: ", NamedTextColor.GRAY)
                .append(Component.text("BLACKLIST", NamedTextColor.LIGHT_PURPLE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Block này sẽ KHÔNG BAO GIỜ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("bị tự động xóa, kể cả khi", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("\"Tất cả block\" đang BẬT.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Click để xóa khỏi blacklist.", NamedTextColor.RED)
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