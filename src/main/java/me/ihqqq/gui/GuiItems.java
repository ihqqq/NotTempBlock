package me.ihqqq.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

final class GuiItems {

    private GuiItems() {
    }

    static ItemStack named(Material material, NamedTextColor color, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        if (!loreLines.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack filler() {
        return named(Material.GRAY_STAINED_GLASS_PANE, NamedTextColor.GRAY, " ", List.of());
    }
}