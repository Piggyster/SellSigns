package tk.piggyster.sellsigns;

import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.DoubleChestInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utils {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String getMessage(String string) {
        return color(SellSignsPlugin.getInstance().getConfig().getString("messages." + string));
    }

    public static List<Sign> getAutoSignsAttachedDouble(Chest chest) {
        List<Sign> signs = new ArrayList<>();
        BlockFace[] faces = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        if(chest.getInventory() instanceof DoubleChestInventory) {
            DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
            Chest leftSide = (Chest) doubleChest.getLeftSide();
            Chest rightSide = (Chest) doubleChest.getRightSide();
            for(BlockFace face : faces) {
                Block block = leftSide.getBlock().getRelative(face);
                if(block.getState() instanceof Sign && block.getBlockData() instanceof Directional) {
                    Sign sign = (Sign) block.getState();
                    Directional directional = (Directional) block.getBlockData();
                    if(block.getRelative(directional.getFacing().getOppositeFace()).equals(leftSide.getBlock())) {
                        if(sign.getLine(1).equals(Utils.color("&1[Autosell Chest]"))) {
                            signs.add(sign);
                        }
                    }

                }
            }
            for(BlockFace face : faces) {
                Block block = rightSide.getBlock().getRelative(face);
                if(block.getState() instanceof Sign && block.getBlockData() instanceof Directional) {
                    Sign sign = (Sign) block.getState();
                    Directional directional = (Directional) block.getBlockData();
                    if(block.getRelative(directional.getFacing().getOppositeFace()).equals(rightSide.getBlock())) {
                        if(sign.getLine(1).equals(Utils.color("&1[Autosell Chest]"))) {
                            signs.add(sign);
                        }
                    }
                }
            }
        } else {
            for(BlockFace face : faces) {
                Block block = chest.getBlock().getRelative(face);
                if (block.getState() instanceof Sign && block.getBlockData() instanceof Directional) {
                    Sign sign = (Sign) block.getState();
                    Directional directional = (Directional) block.getBlockData();
                    if (block.getRelative(directional.getFacing().getOppositeFace()).equals(chest.getBlock())) {
                        if (sign.getLine(1).equals(Utils.color("&1[Autosell Chest]"))) {
                            signs.add(sign);
                        }
                    }
                }
            }
        }
        return signs;
    }

    public static List<Sign> getAutoSignsAttached(Chest chest) {
        List<Sign> signs = new ArrayList<>();
        BlockFace[] faces = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for(BlockFace face : faces) {
            Block block = chest.getBlock().getRelative(face);
            if (block.getState() instanceof Sign && block.getBlockData() instanceof Directional) {
                Sign sign = (Sign) block.getState();
                Directional directional = (Directional) block.getBlockData();
                if (block.getRelative(directional.getFacing().getOppositeFace()).equals(chest.getBlock())) {
                    if (sign.getLine(1).equals(Utils.color("&1[Autosell Chest]"))) {
                        signs.add(sign);
                    }
                }
            }
        }
        return signs;
    }
}
