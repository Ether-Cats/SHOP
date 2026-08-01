import java.util.UUID;

public class Transaction {
    public UUID buyer;
    public UUID seller;
    public String itemName;
    public int amount;
    public double totalPrice;
    public long timestamp; // 交易时间戳，用于清理 7 天前的数据
}