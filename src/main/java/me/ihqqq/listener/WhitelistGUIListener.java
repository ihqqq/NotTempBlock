package me.ihqqq.listener;

import me.ihqqq.gui.BlacklistGUI;
import me.ihqqq.gui.BlockWhitelistGUI;
import me.ihqqq.gui.WorldWhitelistGUI;
import me.ihqqq.notTempBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class WhitelistGUIListener implements Listener {

    private final notTempBlock plugin;

    public WhitelistGUIListener(notTempBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = false)
    public void onDrag(InventoryDragEvent event) {
        Object holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof BlockWhitelistGUI || holder instanceof WorldWhitelistGUI
                || holder instanceof BlacklistGUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = false)
    public void onClick(InventoryClickEvent event) {
        Object holder = event.getView().getTopInventory().getHolder();

        if (holder instanceof BlockWhitelistGUI gui) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            var clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) return;

            if (clickedInventory == event.getView().getTopInventory()) {
                handleTopClick(event, player, gui);
            } else {

                handlePlayerInventoryClick(event, player, gui);
            }
            return;
        }

        if (holder instanceof WorldWhitelistGUI gui) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            if (event.getClickedInventory() == null
                    || event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            handleWorldClick(event, player, gui);
            return;
        }

        if (holder instanceof BlacklistGUI gui) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            var clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) return;

            if (clickedInventory == event.getView().getTopInventory()) {
                handleBlacklistTopClick(event, player, gui);
            } else {
                handleBlacklistPlayerInventoryClick(event, player, gui);
            }
        }
    }


    private void handleTopClick(InventoryClickEvent event, Player player, BlockWhitelistGUI gui) {
        int slot = event.getSlot();

        if (slot == BlockWhitelistGUI.SLOT_PREV) {
            reopen(player, gui.getPage() - 1);
            return;
        }
        if (slot == BlockWhitelistGUI.SLOT_NEXT) {
            reopen(player, gui.getPage() + 1);
            return;
        }
        if (slot == BlockWhitelistGUI.SLOT_CLOSE) {
            player.closeInventory();
            return;
        }
        if (slot == BlockWhitelistGUI.SLOT_TOGGLE_ALL) {
            toggleAllBlocks(player, gui);
            return;
        }
        if (slot == BlockWhitelistGUI.SLOT_WORLDS) {
            openWorldGui(player);
            return;
        }
        if (slot == BlockWhitelistGUI.SLOT_BLACKLIST) {
            openBlacklistGui(player);
            return;
        }
        if (slot == BlockWhitelistGUI.SLOT_ADD) {
            handleAdd(player, gui);
            return;
        }

        handleBlockClick(event, player, gui, slot);
    }

    private void handlePlayerInventoryClick(InventoryClickEvent event, Player player, BlockWhitelistGUI gui) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Material material = clickedItem.getType();
        if (!material.isBlock()) {
            player.sendMessage(Component.text(
                    material.name() + " không phải là block, không thể thêm vào whitelist.",
                    NamedTextColor.RED));
            return;
        }

        boolean isWhitelisted = plugin.getPluginConfig().getBlockDelay(material).isPresent();
        int currentDelay = plugin.getPluginConfig().getBlockDelay(material)
                .orElse(BlockWhitelistGUI.DEFAULT_NEW_DELAY_SECONDS);

        if (event.isLeftClick()) {
            if (!isWhitelisted) {
                setBlockDelay(material, BlockWhitelistGUI.DEFAULT_NEW_DELAY_SECONDS);
                player.sendMessage(Component.text(
                        "Đã thêm " + material.name() + " (" + BlockWhitelistGUI.DEFAULT_NEW_DELAY_SECONDS
                                + " giây) vào whitelist.",
                        NamedTextColor.GREEN));
            } else {
                int newDelay = currentDelay + BlockWhitelistGUI.STEP_SECONDS;
                setBlockDelay(material, newDelay);
                player.sendMessage(Component.text(
                        material.name() + ": " + newDelay + " giây.", NamedTextColor.GREEN));
            }
            reopen(player, gui.getPage());
            return;
        }

        if (event.isRightClick()) {
            if (!isWhitelisted) {
                player.sendMessage(Component.text(
                        material.name() + " chưa có trong whitelist. Click trái vào item để thêm trước.",
                        NamedTextColor.YELLOW));
                return;
            }
            int newDelay = Math.max(BlockWhitelistGUI.MIN_DELAY_SECONDS,
                    currentDelay - BlockWhitelistGUI.STEP_SECONDS);
            setBlockDelay(material, newDelay);
            player.sendMessage(Component.text(
                    material.name() + ": " + newDelay + " giây.", NamedTextColor.GREEN));
            reopen(player, gui.getPage());
        }
    }

    private void toggleAllBlocks(Player player, BlockWhitelistGUI gui) {
        boolean newState = !plugin.getPluginConfig().isAllBlocksEnabled();
        plugin.getConfig().set("all-blocks.enabled", newState);
        plugin.saveConfig();
        plugin.reloadPluginConfig();

        player.sendMessage(Component.text(
                "Tất cả block: " + (newState ? "đã BẬT." : "đã TẮT."),
                newState ? NamedTextColor.GREEN : NamedTextColor.GRAY));

        reopen(player, gui.getPage());
    }

    private void handleAdd(Player player, BlockWhitelistGUI gui) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        Material material = hand.getType();

        if (material.isAir() || !material.isBlock()) {
            player.sendMessage(Component.text(
                    "Bạn cần cầm một block trên tay để thêm vào danh sách.", NamedTextColor.RED));
            return;
        }

        if (plugin.getPluginConfig().getBlockDelay(material).isPresent()) {
            player.sendMessage(Component.text(
                    material.name() + " đã có trong danh sách whitelist.", NamedTextColor.YELLOW));
            return;
        }

        setBlockDelay(material, BlockWhitelistGUI.DEFAULT_NEW_DELAY_SECONDS);

        player.sendMessage(Component.text(
                "Đã thêm " + material.name() + " (" + BlockWhitelistGUI.DEFAULT_NEW_DELAY_SECONDS
                        + " giây) vào whitelist.",
                NamedTextColor.GREEN));

        reopen(player, gui.getPage());
    }

    private void handleBlockClick(InventoryClickEvent event, Player player, BlockWhitelistGUI gui, int slot) {
        Material material = gui.getMaterialAt(slot);
        if (material == null) return;

        ClickType click = event.getClick();
        int currentDelay = plugin.getPluginConfig().getBlockDelay(material)
                .orElse(BlockWhitelistGUI.DEFAULT_NEW_DELAY_SECONDS);

        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
            removeBlockDelay(material);
            player.sendMessage(Component.text(
                    "Đã xóa " + material.name() + " khỏi whitelist.", NamedTextColor.RED));
            reopen(player, gui.getPage());
            return;
        }

        int newDelay;
        if (click == ClickType.LEFT) {
            newDelay = currentDelay + BlockWhitelistGUI.STEP_SECONDS;
        } else if (click == ClickType.RIGHT) {
            newDelay = Math.max(BlockWhitelistGUI.MIN_DELAY_SECONDS,
                    currentDelay - BlockWhitelistGUI.STEP_SECONDS);
        } else {
            return;
        }

        setBlockDelay(material, newDelay);
        reopen(player, gui.getPage());
    }

    private void setBlockDelay(Material material, int seconds) {
        plugin.getConfig().set("blocks." + material.name(), seconds);
        plugin.saveConfig();
        plugin.reloadPluginConfig();
    }

    private void removeBlockDelay(Material material) {
        plugin.getConfig().set("blocks." + material.name(), null);
        plugin.saveConfig();
        plugin.reloadPluginConfig();
    }

    private void reopen(Player player, int page) {
        plugin.getServer().getScheduler().runTask(plugin,
                () -> new BlockWhitelistGUI(plugin, page).open(player));
    }


    private void handleWorldClick(InventoryClickEvent event, Player player, WorldWhitelistGUI gui) {
        int slot = event.getSlot();

        if (slot == WorldWhitelistGUI.SLOT_BACK) {
            reopen(player, 0);
            return;
        }
        if (slot == WorldWhitelistGUI.SLOT_CLOSE) {
            player.closeInventory();
            return;
        }

        World world = gui.getWorldAt(slot);
        if (world == null) return;

        toggleWorldEnabled(player, world.getName());
    }

    private void toggleWorldEnabled(Player player, String worldName) {
        List<String> enabledWorlds = new ArrayList<>(plugin.getConfig().getStringList("enabled-worlds"));
        boolean wasEnabled = enabledWorlds.removeIf(w -> w.equals(worldName));
        if (!wasEnabled) {
            enabledWorlds.add(worldName);
        }

        plugin.getConfig().set("enabled-worlds", enabledWorlds);
        plugin.saveConfig();
        plugin.reloadPluginConfig();

        player.sendMessage(Component.text(
                "World \"" + worldName + "\": " + (wasEnabled ? "đã TẮT." : "đã BẬT."),
                wasEnabled ? NamedTextColor.GRAY : NamedTextColor.GREEN));

        reopenWorld(player);
    }

    private void openWorldGui(Player player) {
        reopenWorld(player);
    }

    private void reopenWorld(Player player) {
        plugin.getServer().getScheduler().runTask(plugin,
                () -> new WorldWhitelistGUI(plugin).open(player));
    }


    private void handleBlacklistTopClick(InventoryClickEvent event, Player player, BlacklistGUI gui) {
        int slot = event.getSlot();

        if (slot == BlacklistGUI.SLOT_PREV) {
            reopenBlacklist(player, gui.getPage() - 1);
            return;
        }
        if (slot == BlacklistGUI.SLOT_NEXT) {
            reopenBlacklist(player, gui.getPage() + 1);
            return;
        }
        if (slot == BlacklistGUI.SLOT_CLOSE) {
            player.closeInventory();
            return;
        }
        if (slot == BlacklistGUI.SLOT_BACK) {
            reopen(player, 0);
            return;
        }
        if (slot == BlacklistGUI.SLOT_ADD) {
            handleBlacklistAdd(player, gui);
            return;
        }

        Material material = gui.getMaterialAt(slot);
        if (material == null) return;

        removeFromBlacklist(material);
        player.sendMessage(Component.text(
                "Đã xóa " + material.name() + " khỏi blacklist.", NamedTextColor.RED));
        reopenBlacklist(player, gui.getPage());
    }

    private void handleBlacklistPlayerInventoryClick(InventoryClickEvent event, Player player, BlacklistGUI gui) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Material material = clickedItem.getType();
        if (!material.isBlock()) {
            player.sendMessage(Component.text(
                    material.name() + " không phải là block, không thể thêm vào blacklist.",
                    NamedTextColor.RED));
            return;
        }

        boolean isBlacklisted = plugin.getPluginConfig().isBlacklisted(material);

        if (event.isLeftClick()) {
            if (isBlacklisted) {
                player.sendMessage(Component.text(
                        material.name() + " đã có trong blacklist rồi.", NamedTextColor.YELLOW));
                return;
            }
            addToBlacklist(material);
            player.sendMessage(Component.text(
                    "Đã thêm " + material.name() + " vào blacklist.", NamedTextColor.GREEN));
            reopenBlacklist(player, gui.getPage());
            return;
        }

        if (event.isRightClick()) {
            if (!isBlacklisted) {
                player.sendMessage(Component.text(
                        material.name() + " chưa có trong blacklist.", NamedTextColor.YELLOW));
                return;
            }
            removeFromBlacklist(material);
            player.sendMessage(Component.text(
                    "Đã xóa " + material.name() + " khỏi blacklist.", NamedTextColor.RED));
            reopenBlacklist(player, gui.getPage());
        }
    }

    private void handleBlacklistAdd(Player player, BlacklistGUI gui) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        Material material = hand.getType();

        if (material.isAir() || !material.isBlock()) {
            player.sendMessage(Component.text(
                    "Bạn cần cầm một block trên tay để thêm vào blacklist.", NamedTextColor.RED));
            return;
        }

        if (plugin.getPluginConfig().isBlacklisted(material)) {
            player.sendMessage(Component.text(
                    material.name() + " đã có trong blacklist.", NamedTextColor.YELLOW));
            return;
        }

        addToBlacklist(material);

        player.sendMessage(Component.text(
                "Đã thêm " + material.name() + " vào blacklist.", NamedTextColor.GREEN));

        reopenBlacklist(player, gui.getPage());
    }

    private void openBlacklistGui(Player player) {
        reopenBlacklist(player, 0);
    }

    private void addToBlacklist(Material material) {
        List<String> list = new ArrayList<>(plugin.getConfig().getStringList("blacklist-blocks"));
        if (!list.contains(material.name())) {
            list.add(material.name());
        }
        plugin.getConfig().set("blacklist-blocks", list);
        plugin.saveConfig();
        plugin.reloadPluginConfig();
    }

    private void removeFromBlacklist(Material material) {
        List<String> list = new ArrayList<>(plugin.getConfig().getStringList("blacklist-blocks"));
        list.removeIf(s -> s.equalsIgnoreCase(material.name()));
        plugin.getConfig().set("blacklist-blocks", list);
        plugin.saveConfig();
        plugin.reloadPluginConfig();
    }

    private void reopenBlacklist(Player player, int page) {
        plugin.getServer().getScheduler().runTask(plugin,
                () -> new BlacklistGUI(plugin, page).open(player));
    }
}