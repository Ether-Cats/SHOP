package top.etca.shop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            ShopGUI.openMainMenu(player, 1);
        } else {
            sender.sendMessage("该指令只能由玩家执行！");
        }
        return true;
    }
}