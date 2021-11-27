package tk.piggyster.sellsigns;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SellSignsPlugin extends JavaPlugin {


    private File data;
    private FileConfiguration dataConfig;
    private File config;
    private FileConfiguration configuration;
    private AutosellManager autosellManager;
    private Economy economy = null;
    private static SellSignsPlugin instance;


    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new EventSignChange(), this);
        getServer().getPluginManager().registerEvents(new EventBlockBreak(), this);
        getServer().getPluginManager().registerEvents(new EventInteract(), this);
        getCommand("sellsigns").setExecutor(new CommandSellSigns());
        setup();
        setupEconomy();
        autosellManager = new AutosellManager(this);
    }

    private void setup() {
        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        data = new File(getDataFolder(), "data.yml");
        if(!data.exists()) {
            saveResource("data.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(data);
        try {
            dataConfig.load(data);
        } catch(IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
        config = new File(getDataFolder(), "config.yml");
        if(!config.exists()) {
            saveResource("config.yml", false);
        }
        configuration = YamlConfiguration.loadConfiguration(config);
        try {
            configuration.load(config);
        } catch(IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static SellSignsPlugin getInstance() {
        return instance;
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public Economy getEconomy() {
        return economy;
    }

    public FileConfiguration getConfig() {
        return configuration;
    }

    public void saveDataConfig() {
        try {
            dataConfig.save(data);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadDataConfig() {
        try {
            dataConfig.load(data);
            dataConfig.save(data);
        } catch(IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            configuration.save(config);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadConfig() {
        try {
            configuration.load(config);
            configuration.save(config);
        } catch(IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    public AutosellManager getAutosellManager() {
        return autosellManager;
    }

}
