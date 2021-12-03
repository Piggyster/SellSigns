package tk.piggyster.sellsigns;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.DoubleChestInventory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EventBlockBreak implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) event.getBlock().getState();
            if(sign.getLine(1).equals(Utils.color("&1[Sell Chest]"))) {
                event.getPlayer().sendMessage(Utils.getMessage("sellsign_removed"));
            } else if(sign.getLine(1).equals(Utils.color("&1[Autosell Chest]"))) {
                UUID owner = SellSignsPlugin.getInstance().getAutosellManager().getOwner(sign);
                if(owner == null)
                    return;
                OfflinePlayer target = Bukkit.getOfflinePlayer(owner);
                if((event.getPlayer().isOp() || event.getPlayer().hasPermission("sellsigns.admin")) && !owner.toString().equals(event.getPlayer().getUniqueId().toString())) {
                    SellSignsPlugin.getInstance().getAutosellManager().removeSign(target, sign);
                    event.getPlayer().sendMessage(Utils.getMessage("autosell_removed_other").replace("{player}", target.getName()));
                    return;
                }
                if(!owner.toString().equals(event.getPlayer().getUniqueId().toString())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Utils.getMessage("not_your_autosell"));
                    return;
                }
                SellSignsPlugin.getInstance().getAutosellManager().removeSign(event.getPlayer(), sign);
                event.getPlayer().sendMessage(Utils.getMessage("autosell_removed"));
            }
        } else if(event.getBlock().getType() == Material.CHEST) {
            Chest chest = (Chest) event.getBlock().getState();
            List<Sign> signs = Utils.getAutoSignsAttachedDouble(chest);
            for(Sign sign : signs) {
                UUID owner = SellSignsPlugin.getInstance().getAutosellManager().getOwner(sign);
                if(owner == null)
                    continue;
                if(!owner.toString().equals(event.getPlayer().getUniqueId().toString()) && !(event.getPlayer().isOp() || event.getPlayer().hasPermission("sellsigns.admin"))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Utils.getMessage("chest_not_your_autosell"));
                    return;
                }
            }
            signs = Utils.getAutoSignsAttached(chest);
            for(Sign sign : signs) {
                UUID owner = SellSignsPlugin.getInstance().getAutosellManager().getOwner(sign);
                if(owner == null)
                    continue;
                OfflinePlayer target = Bukkit.getOfflinePlayer(owner);
                if((event.getPlayer().isOp() || event.getPlayer().hasPermission("sellsigns.admin")) && !owner.toString().equals(event.getPlayer().getUniqueId().toString())) {
                    SellSignsPlugin.getInstance().getAutosellManager().removeSign(target, sign);
                    event.getPlayer().sendMessage(Utils.getMessage("autosell_removed_other").replace("{player}", target.getName()));
                } else {
                    SellSignsPlugin.getInstance().getAutosellManager().removeSign(event.getPlayer(), sign);
                    event.getPlayer().sendMessage(Utils .getMessage("autosell_removed"));
                }
            }
        }
    }
}
