package tk.piggyster.sellsigns;

import com.mysql.fabric.xmlrpc.base.Array;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;

public class EventSignChange implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if(Arrays.stream(event.getLines()).anyMatch("[sellchest]"::equals) || Arrays.stream(event.getLines()).anyMatch(Utils.color("&1[Sell Chest]")::equals)) {
            BlockData data = event.getBlock().getBlockData();
            if(!(data instanceof Directional)) {
                return;
            }
            Directional directional = (Directional) data;
            Block blockBehind = event.getBlock().getRelative(directional.getFacing().getOppositeFace());
            if(blockBehind.getType() == Material.CHEST) {
                for (int i = 0; i < 4; i++) {
                    event.setLine(i, "");
                }
                event.getPlayer().sendMessage(Utils.getMessage("sellsign_placed"));
                event.setLine(1, Utils.color("&1[Sell Chest]"));
            } else {
                event.getBlock().breakNaturally();
                event.getPlayer().sendMessage(Utils.getMessage("sellsign_required_on_chest"));
            }
        } else if(Arrays.stream(event.getLines()).anyMatch("[autosell]"::equals) || Arrays.stream(event.getLines()).anyMatch(Utils.color("&1[Autosell Chest]")::equals)) {
            BlockData data = event.getBlock().getBlockData();
            if(!(data instanceof Directional)) {
                return;
            }
            Directional directional = (Directional) data;
            Block blockBehind = event.getBlock().getRelative(directional.getFacing().getOppositeFace());
            if(blockBehind.getType() == Material.CHEST) {
                if(Utils.getAutoSignsAttachedDouble((Chest) blockBehind.getState()).size() != 0) {
                    event.getBlock().breakNaturally();
                    event.getPlayer().sendMessage(Utils.getMessage("autosell_already_in_place"));
                    return;
                }
                if(SellSignsPlugin.getInstance().getAutosellManager().getLimit(event.getPlayer()) <= SellSignsPlugin.getInstance().getAutosellManager().getPlaced(event.getPlayer()) && !event.getPlayer().isOp()) {
                    event.getBlock().breakNaturally();
                    event.getPlayer().sendMessage(Utils.getMessage("autosell_limit_reached"));
                    return;
                }
                for (int i = 0; i < 4; i++) {
                    event.setLine(i, "");
                }
                event.getPlayer().sendMessage(Utils.getMessage("autosell_placed"));
                event.setLine(1, Utils.color("&1[Autosell Chest]"));
                event.setLine(2, event.getPlayer().getName());
                event.setLine(3, "Selling in " + SellSignsPlugin.getInstance().getAutosellManager().getCounter() + " seconds...");
                SellSignsPlugin.getInstance().getAutosellManager().createSign(event.getPlayer(), (Sign) event.getBlock().getState());
            } else {
                event.getBlock().breakNaturally();
                event.getPlayer().sendMessage(Utils.getMessage("autosell_required_on_chest"));
            }
        }
    }


}
