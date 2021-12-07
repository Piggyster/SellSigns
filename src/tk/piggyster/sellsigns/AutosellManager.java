package tk.piggyster.sellsigns;

import me.aqua.fadepets.PetsPlugin;
import me.aqua.fadepets.player.PlayerManager;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.*;

public class AutosellManager {

    private Map<UUID, List<Sign>> autosigns;
    private SellSignsPlugin plugin;
    private int counter;

    public AutosellManager(SellSignsPlugin plugin) {
        this.plugin = plugin;
        autosigns = new HashMap<>();
        SellSignsPlugin.getInstance().reloadConfig();
        SellSignsPlugin.getInstance().reloadDataConfig();
        fetchSigns();
        DecimalFormat df = new DecimalFormat("#,###.##");
        int count = SellSignsPlugin.getInstance().getConfig().getInt("settings.autosell_interval");
        boolean usePets = SellSignsPlugin.getInstance().getServer().getPluginManager().getPlugin("SkyblockPets") != null;
        PlayerManager manager = usePets ? PetsPlugin.getInstance().getPlayerManager() : null;
        counter = count;
        new BukkitRunnable() {
            @Override
            public void run() {
                long ms = System.currentTimeMillis();
                counter--;
                if(counter == 0) {
                    counter = count;
                }
                for(Map.Entry<UUID, List<Sign>> entry : autosigns.entrySet()) {
                    List<Sign> signs = entry.getValue();
                    if(!Bukkit.getOfflinePlayer(entry.getKey()).isOnline()) {
                        for(Sign sign : signs) {
                            sign.setLine(1, Utils.color("&1[Autosell Chest]"));
                            sign.setLine(2, Bukkit.getOfflinePlayer(entry.getKey()).getName());
                            sign.setLine(3, Utils.color("&cOwner not online..."));
                            sign.update();
                        }
                        continue;
                    }
                    Player player = Bukkit.getPlayer(entry.getKey());
                    List<Sign> removed = new ArrayList<>();
                    for(Sign sign : signs) {
                        sign.setLine(1, Utils.color("&1[Autosell Chest]"));
                        sign.setLine(2, player.getName());
                        sign.setLine(3, "Selling in " + counter);
                        sign.update();
                        if(!(sign.getBlock().getState() instanceof Sign) || !sign.getLine(1).equals(Utils.color("&1[Autosell Chest]"))) {
                            player.sendMessage(Utils.getMessage("obstructed"));
                            removed.add(sign);
                        }

                    }
                    if(removed.size() > 0) {
                        signs.removeAll(removed);
                        removeSigns(player, removed);
                    }
                    entry.setValue(signs);
                }

                if(counter != count) {
                    return;
                }


                for(Map.Entry<UUID, List<Sign>> entry : autosigns.entrySet()) {
                    if(Bukkit.getOfflinePlayer(entry.getKey()).isOnline()) {
                        Player player = Bukkit.getPlayer(entry.getKey());
                        double total = 0;
                        int count = 0;
                        List<Sign> signs = entry.getValue();
                        List<Sign> removed = new ArrayList<>();
                        for(Sign sign : signs) {
                            if(!(sign.getBlock().getState() instanceof Sign) || !sign.getLine(1).equals(Utils.color("&1[Autosell Chest]"))) {
                                removed.add(sign);
                            }
                            Directional directional = (Directional) sign.getBlock().getBlockData();
                            Block blockBehind = sign.getBlock().getRelative(directional.getFacing().getOppositeFace());
                            if(blockBehind.getState() instanceof Chest) {
                                Chest chest = (Chest) blockBehind.getState();
                                Inventory inventory = chest.getInventory();
                                if(inventory instanceof DoubleChestInventory) {
                                    DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                                    inventory = doubleChest.getInventory();
                                }
                                for(ItemStack item : inventory.getContents()) {
                                    double amount = ShopGuiPlusApi.getItemStackPriceSell(player, item);
                                    if (amount <= 0) {
                                        continue;
                                    }
                                    count = count + item.getAmount();
                                    total = total + amount;
                                    item.setAmount(0);
                                }
                            }
                        }
                        if(total == 0)
                            continue;
                        double boostmoney = 0;
                        if(usePets) {
                            boostmoney = (manager.getPlayerData(player.getUniqueId()).getMoneyBoost() / 100D) * total;
                        }
                        player.sendMessage(Utils.getMessage("autosell_sold").replace("{items}", df.format(count)).replace("{amount}", df.format(total)));
                        total += boostmoney;
                        if(boostmoney > 0) {
                            player.sendMessage(Utils.color("&a&l+ $" + df.format(boostmoney) + " &7(Pet Boost)"));
                        }
                        SellSignsPlugin.getInstance().getEconomy().depositPlayer(player, total);

                        if(removed.size() > 0) {
                            signs.removeAll(removed);
                            removeSigns(player, removed);
                        }
                    }
                }
                SellSignsPlugin.getInstance().getLogger().info((System.currentTimeMillis() - ms) + "ms sell time");
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void fetchSigns() {
        for(String node : plugin.getDataConfig().getConfigurationSection("autosells").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(node);
                List<String> strings = plugin.getDataConfig().getStringList("autosells." + node);
                List<Sign> signs = new ArrayList<>();
                for(String string : strings) {
                    String[] split = string.split(";");
                    int x = Integer.parseInt(split[0]);
                    int y = Integer.parseInt(split[1]);
                    int z = Integer.parseInt(split[2]);
                    World world = Bukkit.getWorld(split[3]);
                    Location location = new Location(world, x, y, z);
                    if(location.getBlock().getState() instanceof Sign) {
                        Sign sign = (Sign) location.getBlock().getState();
                        signs.add(sign);
                    }
                }
                autosigns.put(uuid, signs);
            } catch(Exception ex) {
            }
        }
    }


    public void createSign(OfflinePlayer player, Sign sign) {
        List<Sign> signs = autosigns.getOrDefault(player.getUniqueId(), new ArrayList<>());
        signs.add(sign);
        autosigns.put(player.getUniqueId(), signs);
        List<String> locations = SellSignsPlugin.getInstance().getDataConfig().getStringList("autosells." + player.getUniqueId());
        locations.add(sign.getLocation().getBlockX() + ";" + sign.getLocation().getBlockY() + ";" + sign.getLocation().getBlockZ() + ";" + sign.getLocation().getWorld().getName());
        SellSignsPlugin.getInstance().getDataConfig().set("autosells." + player.getUniqueId(), locations);
        SellSignsPlugin.getInstance().saveDataConfig();
    }

    public void removeSign(OfflinePlayer player, Sign sign) {
        List<Sign> signs = autosigns.getOrDefault(player.getUniqueId(), new ArrayList<>());
        signs.remove(sign);
        autosigns.put(player.getUniqueId(), signs);
        List<String> locations = SellSignsPlugin.getInstance().getDataConfig().getStringList("autosells." + player.getUniqueId());
        locations.remove(sign.getLocation().getBlockX() + ";" + sign.getLocation().getBlockY() + ";" + sign.getLocation().getBlockZ() + ";" + sign.getLocation().getWorld().getName());
        SellSignsPlugin.getInstance().getDataConfig().set("autosells." + player.getUniqueId(), locations);
        SellSignsPlugin.getInstance().saveDataConfig();
    }

    private void removeSigns(Player player, List<Sign> signs) {
        List<Sign> currentSigns = autosigns.getOrDefault(player.getUniqueId(), new ArrayList<>());
        List<String> locations = SellSignsPlugin.getInstance().getDataConfig().getStringList("autosells." + player.getUniqueId());
        for(Sign sign : signs) {
            currentSigns.remove(sign);
            locations.remove(sign.getLocation().getBlockX() + ";" + sign.getLocation().getBlockY() + ";" + sign.getLocation().getBlockZ() + ";" + sign.getLocation().getWorld().getName());
        }
        autosigns.put(player.getUniqueId(), currentSigns);
        SellSignsPlugin.getInstance().getDataConfig().set("autosells." + player.getUniqueId(), locations);
        SellSignsPlugin.getInstance().saveDataConfig();
    }

    public UUID getOwner(Sign sign) {
        for(Map.Entry<UUID, List<Sign>> entry : autosigns.entrySet()) {
            if(entry.getValue().contains(sign)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public int getLimit(Player player) {
        for(int i = 100; i > 0; i--) {
            if(player.hasPermission("sellsigns.limit." + i)) {
                return i;
            }
        }
        return 0;
    }

    public int getPlaced(OfflinePlayer player) {
        return getSigns(player).size();
    }

    public List<Sign> getSigns(OfflinePlayer player) {
        return autosigns.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    public int getCounter() {
        return counter;
    }
}
