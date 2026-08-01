package top.etca.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class DataManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File shopFile;
    private static File recordFile;

    public static List<ShopItem> shopItems = new ArrayList<>();
    public static List<Transaction> transactions = new ArrayList<>();

    // 数据结构类
    public static class ShopItem {
        public String id = UUID.randomUUID().toString();
        public String sellerUUID;
        public String sellerName;
        public String itemBase64;
        public int amount;
        public double pricePerUnit;
    }

    public static class Transaction {
        public String buyerName;
        public String sellerName;
        public String itemName;
        public int amount;
        public double totalPrice;
        public long timestamp;
    }

    public static void init(File dataFolder) {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        shopFile = new File(dataFolder, "shop.json");
        recordFile = new File(dataFolder, "transactions.json");
        loadData();
    }

    public static void loadData() {
        try {
            if (shopFile.exists()) {
                Reader reader = new FileReader(shopFile);
                List<ShopItem> loaded = gson.fromJson(reader, new TypeToken<List<ShopItem>>(){}.getType());
                if (loaded != null) shopItems = loaded;
                reader.close();
            }
            if (recordFile.exists()) {
                Reader reader = new FileReader(recordFile);
                List<Transaction> loaded = gson.fromJson(reader, new TypeToken<List<Transaction>>(){}.getType());
                if (loaded != null) transactions = loaded;
                reader.close();
            }
            cleanOldRecords();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        try {
            Writer shopWriter = new FileWriter(shopFile);
            gson.toJson(shopItems, shopWriter);
            shopWriter.close();

            Writer recordWriter = new FileWriter(recordFile);
            gson.toJson(transactions, recordWriter);
            recordWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 清理超过 7 天的日志
    public static void cleanOldRecords() {
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        transactions.removeIf(record -> record.timestamp < sevenDaysAgo);
    }
}