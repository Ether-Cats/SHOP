package top.etca.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        // 统一拦截防止玩家拿走 GUI 里面的装饰物
        if (title.contains("全球商店") || title.contains("上架设置") || title.contains("确认购买") || title.contains("我的上架商品") || title.contains("交易记录")) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return; // 忽略点击自己背包
        } else {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // ================= 1. 主页逻辑 =================
        if (title.contains("全球商店")) {
            int page = Integer.parseInt(org.bukkit.ChatColor.stripColor(title).replaceAll("[^0-9]", ""));
            if (clicked.getType() == Material.ARROW) {
                if (clicked.getItemMeta().getDisplayName().contains("上一页")) ShopGUI.openMainMenu(player, page - 1);
                if (clicked.getItemMeta().getDisplayName().contains("下一页")) ShopGUI.openMainMenu(player, page + 1);
            } else if (clicked.getType() == Material.CHEST) {
                ShopGUI.openMyItemsMenu(player);
            } else if (clicked.getType() == Material.NETHER_STAR) {
                // 打开卖出
                ItemStack offHand = player.getInventory().getItemInOffHand();
                if (offHand.getType() == Material.AIR) {
                    player.sendMessage("§c错误：请将想要卖出的物品拿到左手（副手）上！");
                    player.closeInventory();
                    return;
                }
                SessionManager.SellSession session = new SessionManager.SellSession();
                session.sampleItem = offHand.clone();
                SessionManager.sellSessions.put(player.getUniqueId(), session);
                ShopGUI.openSellMenu(player);
            } else if (clicked.getType() == Material.BOOK) {
                ShopGUI.openRecordMenu(player);
            } else {
                // 点击了某个出售的商品
                int index = (page - 1) * 45 + event.getSlot();
                if (index < DataManager.shopItems.size()) {
                    DataManager.ShopItem target = DataManager.shopItems.get(index);
                    SessionManager.BuySession session = new SessionManager.BuySession();
                    session.targetItem = target;
                    session.buyAmount = 1;
                    SessionManager.buySessions.put(player.getUniqueId(), session);
                    ShopGUI.openBuyMenu(player);
                }
            }
        }

        // ================= 2. 上架调控逻辑 =================
        else if (title.contains("上架设置")) {
            SessionManager.SellSession session = SessionManager.sellSessions.get(player.getUniqueId());
            if (session == null) return;

            if (clicked.getType() == Material.RED_STAINED_GLASS_PANE) {
                if (clicked.getItemMeta().getDisplayName().contains("数量")) session.amount = Math.max(1, session.amount - 1);
                if (clicked.getItemMeta().getDisplayName().contains("单价")) session.price = Math.max(0.1, session.price - 10);
                ShopGUI.openSellMenu(player);
            } else if (clicked.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                if (clicked.getItemMeta().getDisplayName().contains("数量")) session.amount = Math.min(128, session.amount + 1); // 限制单次最高128
                if (clicked.getItemMeta().getDisplayName().contains("单价")) session.price += 10;
                ShopGUI.openSellMenu(player);
            } else if (clicked.getType() == Material.ORANGE_STAINED_GLASS_PANE) {
                // 真正的上架扣除逻辑
                if (ItemUtils.deductItems(player, session.sampleItem, session.amount)) {
                    DataManager.ShopItem newItem = new DataManager.ShopItem();
                    newItem.sellerUUID = player.getUniqueId().toString();
                    newItem.sellerName = player.getName();
                    newItem.amount = session.amount;
                    newItem.pricePerUnit = session.price;
                    newItem.itemBase64 = ItemUtils.itemToBase64(session.sampleItem);

                    DataManager.shopItems.add(newItem);
                    DataManager.saveData();

                    player.sendMessage("§a成功上架了 " + session.amount + " 个商品！");
                    SessionManager.sellSessions.remove(player.getUniqueId());
                    ShopGUI.openMainMenu(player, 1);
                } else {
                    player.sendMessage("§c上架失败：你背包中的该物品不足 " + session.amount + " 个！");
                }
            }
        }

        // ================= 3. 购买调控逻辑 =================
        else if (title.contains("确认购买")) {
            SessionManager.BuySession session = SessionManager.buySessions.get(player.getUniqueId());
            if (session == null) return;

            if (clicked.getType() == Material.RED_STAINED_GLASS_PANE) {
                session.buyAmount = Math.max(1, session.buyAmount - 1);
                ShopGUI.openBuyMenu(player);
            } else if (clicked.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                session.buyAmount = Math.min(session.targetItem.amount, session.buyAmount + 1);
                ShopGUI.openBuyMenu(player);
            } else if (clicked.getType() == Material.BLUE_STAINED_GLASS_PANE) {
                session.buyAmount = session.targetItem.amount; // 拉满
                ShopGUI.openBuyMenu(player);
            } else if (clicked.getType() == Material.ORANGE_STAINED_GLASS_PANE) {
                // 执行买交易与 Essentials 扣款/发钱
                double totalPrice = session.buyAmount * session.targetItem.pricePerUnit;
                if (shop.getEconomy().getBalance(player) < totalPrice) {
                    player.sendMessage("§c购买失败：你的金币不足！需要 " + totalPrice);
                    return;
                }

                // 1. 扣除买家金币
                shop.getEconomy().withdrawPlayer(player, totalPrice);

                // 2. 给予卖家金币 (Vault 原生支持离线玩家，无需上线即可入账)
                OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(session.targetItem.sellerUUID));
                shop.getEconomy().depositPlayer(seller, totalPrice);

                // 3. 给予买家物品
                ItemStack reward = ItemUtils.itemFromBase64(session.targetItem.itemBase64);
                reward.setAmount(session.buyAmount);
                player.getInventory().addItem(reward);

                // 4. 记录交易数据
                DataManager.Transaction record = new DataManager.Transaction();
                record.buyerName = player.getName();
                record.sellerName = session.targetItem.sellerName;
                record.itemName = reward.getItemMeta().hasDisplayName() ? reward.getItemMeta().getDisplayName() : reward.getType().name();
                record.amount = session.buyAmount;
                record.totalPrice = totalPrice;
                record.timestamp = System.currentTimeMillis();
                DataManager.transactions.add(0, record);

                // 5. 更新或移除库存 JSON 数据
                session.targetItem.amount -= session.buyAmount;
                if (session.targetItem.amount <= 0) {
                    DataManager.shopItems.remove(session.targetItem);
                }
                DataManager.saveData();

                player.sendMessage("§a购买成功！消耗了 " + totalPrice + " 币。");
                SessionManager.buySessions.remove(player.getUniqueId());
                ShopGUI.openMainMenu(player, 1);
            }
        }

        // ================= 4. 下架逻辑 =================
        else if (title.contains("我的上架商品")) {
            int slot = event.getSlot();
            // 找出这个玩家对应的商品
            int current = 0;
            for (DataManager.ShopItem item : new java.util.ArrayList<>(DataManager.shopItems)) {
                if (item.sellerUUID.equals(player.getUniqueId().toString())) {
                    if (current == slot) {
                        // 下架归还物品
                        ItemStack backItem = ItemUtils.itemFromBase64(item.itemBase64);
                        backItem.setAmount(item.amount);
                        player.getInventory().addItem(backItem);

                        DataManager.shopItems.remove(item);
                        DataManager.saveData();

                        player.sendMessage("§e成功下架商品并返还至背包！");
                        ShopGUI.openMyItemsMenu(player);
                        return;
                    }
                    current++;
                }
            }
        }
    }
}