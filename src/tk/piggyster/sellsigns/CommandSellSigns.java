package tk.piggyster.sellsigns;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandSellSigns implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp() && !sender.hasPermission("*") && !sender.hasPermission("sellsigns.admin")) {
            sender.sendMessage(Utils.getMessage("no_permission"));
            return false;
        }
        if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            long ms = System.currentTimeMillis();
            SellSignsPlugin.getInstance().reloadConfig();
            SellSignsPlugin.getInstance().reloadDataConfig();
            SellSignsPlugin.getInstance().getAutosellManager().fetchSigns();
            sender.sendMessage(Utils.getMessage("reloaded").replace("{ms}", (System.currentTimeMillis() - ms) + ""));
            return true;
        } else if(args.length == 2 && args[0].equalsIgnoreCase("view")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if(!target.hasPlayedBefore()) {
                sender.sendMessage(Utils.getMessage("invalid_player").replace("{player}", args[1]));
                return false;
            }
            List<Sign> signs = SellSignsPlugin.getInstance().getAutosellManager().getSigns(target);
            if(signs.size() == 0) {
                sender.sendMessage(Utils.getMessage("view_signs_has_none").replace("{player}", target.getName()));
                return false;
            }
            List<String> values = new ArrayList<>();
            for(Sign sign : signs) {
                String location = sign.getBlock().getX() + ", " + sign.getBlock().getY() + ", " + sign.getBlock().getZ() + " in " + sign.getWorld().getName();
                values.add(Utils.getMessage("view_signs_value").replace("{location}", location));
            }
            sender.sendMessage(Utils.getMessage("view_signs").replace("{player}", target.getName()).replace("{signs}", String.join("\n", values)));
            return true;
        } else {
            sender.sendMessage(Utils.getMessage("invalid_usage"));
            return false;
        }
    }
}
