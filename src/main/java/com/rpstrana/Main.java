package com.rpstrana;

import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    
    private static Main instance;
    private CountryManager countryManager;
    private LuckPermsHandler luckPermsHandler;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        countryManager = new CountryManager(this);
        luckPermsHandler = new LuckPermsHandler();
        
        // Register commands
        getCommand("rp").setExecutor(new CommandManager(this));
        getCommand("rp").setTabCompleter(new CommandManager(this));
        
        // Load country data
        countryManager.loadCountries();
        
        // Initialize LuckPerms
        initializeLuckPerms();
    }
    
    @Override
    public void onDisable() {
        // Clean up resources if needed
    }
    
    private void initializeLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsHandler.initialize(provider.getProvider());
        } else {
            getLogger().warning("Could not find LuckPerms! Some features may not work properly.");
        }
    }
    
    public static Main getInstance() {
        return instance;
    }
    
    public CountryManager getCountryManager() {
        return countryManager;
    }
    
    public LuckPermsHandler getLuckPermsHandler() {
        return luckPermsHandler;
    }
}