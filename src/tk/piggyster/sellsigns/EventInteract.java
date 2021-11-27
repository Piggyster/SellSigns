package tk.piggyster.sellsigns;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.VaultEco;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

public class EventInteract implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if(event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR)
            return;
        if(event.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign) event.getClickedBlock().getState();
            if(sign.getLine(1).equals(Utils.color("&1[Sell Chest]"))) {
                Directional directional = (Directional) event.getClickedBlock().getBlockData();
                Block blockBehind = event.getClickedBlock().getRelative(directional.getFacing().getOppositeFace());
                 if(blockBehind.getState() instanceof Chest) {
                    Chest chest = (Chest) blockBehind.getState();
                    double total = 0;
                    int count = 0;
                    Inventory inventory = chest.getInventory();
                    if(inventory instanceof DoubleChestInventory) {
                        DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                        inventory = doubleChest.getInventory();
                    }
                    for(ItemStack item : inventory.getContents()) {
                        double amount = ShopGuiPlusApi.getItemStackPriceSell(event.getPlayer(), item);
                        if(amount <= 0) {
                            continue;
                        }
                        count = count + item.getAmount();
                        total = total + amount;
                        item.setAmount(0);
                    }
                    if(total == 0)
                        return;
                    SellSignsPlugin.getInstance().getEconomy().depositPlayer(event.getPlayer(), total);
                    DecimalFormat df = new DecimalFormat("#,###.##");
                    event.getPlayer().sendMessage(Utils.getMessage("sellsign_sold").replace("{items}", df.format(count)).replace("{amount}", df.format(total)));
                }
            }
        }
    }
}
