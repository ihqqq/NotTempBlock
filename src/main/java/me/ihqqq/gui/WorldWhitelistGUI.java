package me.ihqqq.gui;

import me.ihqqq.notTempBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WorldWhitelistGUI implements InventoryHolder {

    private static final int SIZE = 54;
    private static final int MAX_WORLDS_SHOWN = 45;

    public static final int SLOT_BACK = 45;
    public static final int SLOT_CLOSE = 51;

    private final notTempBlock plugin;
    private final List<World> worlds;
    private Inventory inventory;

    public WorldWhitelistGUI(notTempBlock plugin) {
        this.plugin = plugin;
        this.worlds = new ArrayList<>(Bukkit.getWorlds());
        this.worlds.sort(Comparator.comparing(World::getName));
        build();
    }

    private void build() {
        Component title = Component.text("Quản lý World", NamedTextColor.GOLD, TextDecoration.BOLD);
        this.inventory = Bukkit.createInventory(this, SIZE, title);

        int shown = Math.min(worlds.size(), MAX_WORLDS_SHOWN);
        for (int i = 0; i < shown; i++) {
            World world = worlds.get(i);
            boolean enabled = plugin.getPluginConfig().isWorldEnabled(world.getName());
            inventory.setItem(i, buildWorldIcon(world, enabled));
        }

        for (int slot = 46; slot <= 53; slot++) {
            if (slot == SLOT_CLOSE) continue;
            inventory.setItem(slot, GuiItems.filler());
        }

        inventory.setItem(SLOT_BACK, GuiItems.named(Material.ARROW, NamedTextColor.YELLOW,
                "« Quay lại Block", List.of()));
        inventory.setItem(SLOT_CLOSE, GuiItems.named(Material.BARRIER, NamedTextColor.RED,
                "Đóng", List.of()));
    }

    private ItemStack buildWorldIcon(World world, boolean enabled) {
        Material icon = switch (world.getEnvironment()) {
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.GRASS_BLOCK;
        };

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(world.getName(),
                        enabled ? NamedTextColor.GREEN : NamedTextColor.GRAY, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Trạng thái: ", NamedTextColor.GRAY)
                .append(Component.text(enabled ? "BẬT" : "TẮT",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Loại world: " + world.getEnvironment().name(), NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Click để chuyển đổi BẬT/TẮT.", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public World getWorldAt(int slot) {
        if (slot < 0 || slot >= worlds.size() || slot >= MAX_WORLDS_SHOWN) return null;
        return worlds.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}