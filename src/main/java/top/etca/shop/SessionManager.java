package top.etca.shop;

import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    public static class SellSession {
        public ItemStack sampleItem;
        public int amount = 1;
        public double price = 10.0;
    }

    public static class BuySession {
        public DataManager.ShopItem targetItem;
        public int buyAmount = 1;
    }

    public static Map<UUID, SellSession> sellSessions = new HashMap<>();
    public static Map<UUID, BuySession> buySessions = new HashMap<>();
}