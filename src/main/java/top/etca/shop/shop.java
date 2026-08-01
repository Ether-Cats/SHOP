package top.etca.shop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class shop extends JavaPlugin {

    private static shop instance;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;

        // 初始化 Vault 经济挂钩
        if (!setupEconomy()) {
            getLogger().severe("未找到 Vault 或经济插件（如 Essentials），插件已停用！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化数据管理中心 (创建 JSON 文件)
        DataManager.init(getDataFolder());

        // 注册事件和命令
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getCommand("shop").setExecutor(new ShopCommand());

        getLogger().info("=== 全球玩家商店插件已启动 ===");
    }

    @Override
    public void onDisable() {
        // 保存数据
        DataManager.saveData();
        getLogger().info("=== 全球玩家商店插件数据已安全保存并关闭 ===");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static shop getInstance() {
        return instance;
    }
}