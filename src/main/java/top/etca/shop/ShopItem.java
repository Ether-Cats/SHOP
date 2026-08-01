import java.util.UUID;

public class ShopItem {
    public UUID id; // 唯一订单ID
    public UUID seller; // 卖家 UUID
    public String sellerName; // 卖家名称
    public String itemBase64; // 物品数据的 Base64
    public int amount; // 剩余数量
    public double pricePerUnit; // 单价
    public long listTime; // 上架时间
}