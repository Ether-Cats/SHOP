package top.etca.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShopGUI {

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String l : lore) loreList.add(l);
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    // 填充黑色/灰色玻璃板背景
    private static void fillFiller(Inventory gui) {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) gui.setItem(i, border);
        }
    }

    // 1. 主页 (45格内容 + 9格功能栏)
    public static void openMainMenu(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8全球商店 - 第 " + page + " 页");

        List<DataManager.ShopItem> items = DataManager.shopItems;
        int start = (page - 1) * 45;
        int end = Math.min(start + 45, items.size());

        for (int i = start; i < end; i++) {
            DataManager.ShopItem data = items.get(i);
            ItemStack display = ItemUtils.itemFromBase64(data.itemBase64);
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("§7-------------------");
            lore.add("§e卖家: §f" + data.sellerName);
            lore.add("§e剩余数量: §a" + data.amount);
            lore.add("§e单价: §d" + data.pricePerUnit + " 币");
            lore.add("§a[点击进入购买界面]");
            meta.setLore(lore);
            display.setItemMeta(meta);
            gui.setItem(i - start, display);
        }

        fillFiller(gui);

        // 功能栏组件
        if (page > 1) gui.setItem(45, createItem(Material.ARROW, "§a上一页", "§7翻至第 " + (page - 1) + " 页"));
        gui.setItem(48, createItem(Material.CHEST, "§b我的商品", "§7查看并管理你上架的商品"));
        gui.setItem(49, createItem(Material.NETHER_STAR, "§e上架商品", "§7先将要卖的物品放在左手(副手)"));
        gui.setItem(50, createItem(Material.BOOK, "§d交易记录", "§7查看近 7 日的买卖记录"));
        if (end < items.size()) gui.setItem(53, createItem(Material.ARROW, "§a下一页", "§7翻至第 " + (page + 1) + " 页"));

        player.openInventory(gui);
    }

    // 2. 上架商品界面 (加减红绿玻璃板)
    public static void openSellMenu(Player player) {
        SessionManager.SellSession session = SessionManager.sellSessions.get(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(null, 27, "§8上架设置 - 点击调整");

        fillFiller(gui);

        // 数量调控 (-1 红, +1 绿)
        gui.setItem(10, createItem(Material.RED_STAINED_GLASS_PANE, "§c数量 -1", "§7当前设定数量: §e" + session.amount));
        gui.setItem(11, createItem(Material.GREEN_STAINED_GLASS_PANE, "§a数量 +1", "§7当前设定数量: §e" + session.amount));

        // 展示左手要卖的商品
        ItemStack preview = session.sampleItem.clone();
        preview.setAmount(Math.min(session.amount, session.sampleItem.getMaxStackSize()));
        gui.setItem(13, preview);

        // 单价调控 (-10 红, +10 绿)
        gui.setItem(15, createItem(Material.RED_STAINED_GLASS_PANE, "§c单价 -10", "§7当前设定单价: §d" + session.price));
        gui.setItem(16, createItem(Material.GREEN_STAINED_GLASS_PANE, "§a单价 +10", "§7当前设定单价: §d" + session.price));

        // 橙色玻璃板确认卖出
        gui.setItem(22, createItem(Material.ORANGE_STAINED_GLASS_PANE, "§6确认上架售卖", "§7总售价: §e" + (session.amount * session.price)));

        player.openInventory(gui);
    }

    // 3. 购买确认界面 (红减, 绿加, 蓝拉满, 橙确认)
    public static void openBuyMenu(Player player) {
        SessionManager.BuySession session = SessionManager.buySessions.get(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(null, 27, "§8确认购买商品");

        fillFiller(gui);

        gui.setItem(10, createItem(Material.RED_STAINED_GLASS_PANE, "§c数量 -1", "§7打算购买: §e" + session.buyAmount));
        gui.setItem(11, createItem(Material.GREEN_STAINED_GLASS_PANE, "§a数量 +1", "§7打算购买: §e" + session.buyAmount));
        gui.setItem(12, createItem(Material.BLUE_STAINED_GLASS_PANE, "§b购买拉满", "§7拉满数量: §e" + session.targetItem.amount));

        ItemStack display = ItemUtils.itemFromBase64(session.targetItem.itemBase64);
        display.setAmount(Math.min(session.buyAmount, display.getMaxStackSize()));
        gui.setItem(13, display);

        double totalPrice = session.buyAmount * session.targetItem.pricePerUnit;
        gui.setItem(22, createItem(Material.ORANGE_STAINED_GLASS_PANE, "§6确认购买", "§7需要花费: §d" + totalPrice + " 币"));

        player.openInventory(gui);
    }

    // 4. 我的商品列表
    public static void openMyItemsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8我的上架商品 (点击下架)");
        fillFiller(gui);

        int slot = 0;
        for (DataManager.ShopItem item : DataManager.shopItems) {
            if (item.sellerUUID.equals(player.getUniqueId().toString())) {
                ItemStack display = ItemUtils.itemFromBase64(item.itemBase64);
                ItemMeta meta = display.getItemMeta();
                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                lore.add("§7-------------------");
                lore.add("§e剩余: §a" + item.amount);
                lore.add("§e单价: §d" + item.pricePerUnit);
                lore.add("§c[点击此物品下架退回背包]");
                meta.setLore(lore);
                display.setItemMeta(meta);

                gui.setItem(slot++, display);
                if (slot >= 54) break;
            }
        }
        player.openInventory(gui);
    }

    // 5. 7日交易记录列表
    public static void openRecordMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8最近 7 日交易记录");
        fillFiller(gui);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int slot = 0;
        for (DataManager.Transaction record : DataManager.transactions) {
            ItemStack paper = createItem(Material.PAPER, "§f" + record.itemName,
                    "§7买家: §b" + record.buyerName,
                    "§7卖家: §e" + record.sellerName,
                    "§7交易数量: §a" + record.amount,
                    "§7总金额: §d" + record.totalPrice,
                    "§7时间: §8" + sdf.format(new Date(record.timestamp)));
            gui.setItem(slot++, paper);
            if (slot >= 54) break;
        }
        player.openInventory(gui);
    }
}