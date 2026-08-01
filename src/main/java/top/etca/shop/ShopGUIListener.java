import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // 拦截所有商店界面的点击，防止物品被拿走
        if (title.contains("商店主页") || title.contains("上架商品设置") || title.contains("购买商品")) {
            event.setCancelled(true);
        }

        if (event.getCurrentItem() == null) return;
        ItemStack clicked = event.getCurrentItem();
        Material type = clicked.getType();

        // ========== 出售逻辑 ==========
        if (title.equals("§8上架商品设置")) {
            // 这里你需要用一个临时 Map<UUID, TempSellData> 来存玩家正在设置的数值
            // 假设我们已经拿到了玩家当前的临时设置数据 data

            if (type == Material.RED_STAINED_GLASS_PANE && clicked.getItemMeta().getDisplayName().contains("数量 -1")) {
                // 处理数量减少，最低为 1
                // 刷新 GUI 显示
            } else if (type == Material.GREEN_STAINED_GLASS_PANE && clicked.getItemMeta().getDisplayName().contains("数量 +1")) {
                // 处理数量增加，不得超过 128 (你的需求)
                // 检查玩家背包中该物品的实际总量是否足够
            } else if (type == Material.ORANGE_STAINED_GLASS_PANE) {
                // 确认上架
                // 1. 从玩家背包扣除相应数量的物品
                // 2. 写入 shop.json
                // 3. 关闭菜单，提示上架成功
                player.closeInventory();
                player.sendMessage("§a商品上架成功！");
            }
        }

        // ========== 购买逻辑 ==========
        if (title.equals("§8购买商品")) {
            if (type == Material.RED_STAINED_GLASS_PANE) {
                // 购买数 -1
            } else if (type == Material.GREEN_STAINED_GLASS_PANE) {
                // 购买数 +1
            } else if (type == Material.BLUE_STAINED_GLASS_PANE) {
                // 拉满购买数 (取玩家余额可买最大值 和 商品剩余库存 的较小值)
            } else if (type == Material.ORANGE_STAINED_GLASS_PANE) {
                // 确认购买核心逻辑：
                // double cost = quantity * pricePerUnit;
                // if (shop.getEconomy().getBalance(player) >= cost) {
                //     shop.getEconomy().withdrawPlayer(player, cost);
                //     shop.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(sellerUUID), cost);
                //     给玩家物品;
                //     写入交易记录 JSON;
                //     更新 shop.json 库存;
                // }
            }
        }

        // ========== 主页逻辑 ==========
        if (title.equals("§8商店主页")) {
            if (type == Material.ARROW) {
                // 翻页逻辑：重新渲染 Inventory
            }
            // 否则如果点到商品，打开该商品的“购买商品”GUI
        }
    }
}