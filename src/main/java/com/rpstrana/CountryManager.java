package com.rpstrana;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CountryManager {
    
    private final Main plugin;
    private Map<String, String> countries; // ID -> Name
    private Map<UUID, String> playerCountries; // Player UUID -> Country ID
    private Map<UUID, Role> playerRoles; // Player UUID -> Role
    private Map<String, Map<String, DiplomacyState>> diplomacy; // Country ID -> {Other Country ID -> Diplomacy State}
    
    public CountryManager(Main plugin) {
        this.plugin = plugin;
        this.countries = new HashMap<>();
        this.playerCountries = new HashMap<>();
        this.playerRoles = new HashMap<>();
        this.diplomacy = new HashMap<>();
        
        loadPlayerData();
    }
    
    public void loadCountries() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File countriesFile = new File(dataFolder, "countries.json");
        if (countriesFile.exists()) {
            loadCountriesFromFile(countriesFile);
        } else {
            // Load default countries if file doesn't exist
            loadDefaultCountries();
        }
    }
    
    private void loadCountriesFromFile(File file) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            
            String content = readFile(file);
            countries = gson.fromJson(content, type);
            
            if (countries == null) {
                countries = new HashMap<>();
            }
            
            plugin.getLogger().info("Loaded " + countries.size() + " countries from file.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load countries from file: " + e.getMessage());
            loadDefaultCountries();
        }
    }
    
    private void loadDefaultCountries() {
        // Initially empty, will be populated by the countryload command
        plugin.getLogger().info("Initialized empty countries list. Use /rp countryload to populate from API.");
    }
    
    public void loadCountriesFromAPI() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://maprpstranacf.vercel.app/api/2025/country.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Rpstrana Plugin");
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    Gson gson = new Gson();
                    Type type = new TypeToken<Map<String, String>>(){}.getType();
                    Map<String, String> apiCountries = gson.fromJson(response.toString(), type);
                    
                    if (apiCountries != null) {
                        countries = apiCountries;
                        
                        // Save to file
                        saveCountriesToFile();
                        
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.getLogger().info("Successfully loaded " + countries.size() + " countries from API.");
                        });
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.getLogger().severe("Received invalid data from API.");
                        });
                    }
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getLogger().severe("Failed to fetch countries from API. Response code: " + responseCode);
                    });
                }
                
                connection.disconnect();
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().severe("Error fetching countries from API: " + e.getMessage());
                });
            }
        });
    }
    
    private void saveCountriesToFile() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File countriesFile = new File(dataFolder, "countries.json");
        try {
            Gson gson = new Gson();
            String json = gson.toJson(countries);
            
            try (FileWriter writer = new FileWriter(countriesFile, StandardCharsets.UTF_8)) {
                writer.write(json);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save countries to file: " + e.getMessage());
        }
    }
    
    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    public Map<String, String> getCountries() {
        return new HashMap<>(countries);
    }
    
    public String getCountryName(String countryId) {
        return countries.get(countryId);
    }
    
    public boolean hasCountry(UUID playerId) {
        return playerCountries.containsKey(playerId);
    }
    
    public String getPlayerCountry(UUID playerId) {
        return playerCountries.get(playerId);
    }
    
    public Role getPlayerRole(UUID playerId) {
        return playerRoles.getOrDefault(playerId, Role.RESIDENT);
    }
    
    public void setPlayerCountryAndRole(UUID playerId, String countryId, Role role) {
        playerCountries.put(playerId, countryId);
        playerRoles.put(playerId, role);
        savePlayerData();
        
        // Update player's prefix/suffix in LuckPerms
        Main.getInstance().getLuckPermsHandler().updatePlayerMetadata(playerId, countryId, role);
    }
    
    public void removePlayerFromCountry(UUID playerId) {
        String oldCountry = playerCountries.remove(playerId);
        playerRoles.remove(playerId);
        savePlayerData();
        
        // Remove player's prefix/suffix in LuckPerms
        Main.getInstance().getLuckPermsHandler().removePlayerMetadata(playerId);
    }
    
    public void setPlayerRole(UUID playerId, Role role) {
        playerRoles.put(playerId, role);
        savePlayerData();
        
        // Update player's prefix/suffix in LuckPerms
        String countryId = playerCountries.get(playerId);
        if (countryId != null) {
            Main.getInstance().getLuckPermsHandler().updatePlayerMetadata(playerId, countryId, role);
        }
    }
    
    public void setDiplomacyState(String countryId1, String countryId2, DiplomacyState state) {
        diplomacy.computeIfAbsent(countryId1, k -> new HashMap<>()).put(countryId2, state);
        savePlayerData(); // Using same file for now
    }
    
    public DiplomacyState getDiplomacyState(String countryId1, String countryId2) {
        Map<String, DiplomacyState> relations = diplomacy.get(countryId1);
        if (relations == null) {
            return DiplomacyState.NEUTRAL;
        }
        return relations.getOrDefault(countryId2, DiplomacyState.NEUTRAL);
    }
    
    private void loadPlayerData() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File playersFile = new File(dataFolder, "players.yml");
        if (playersFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playersFile);
            
            // Load player countries
            if (config.contains("players")) {
                for (String uuidStr : config.getConfigurationSection("players").getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidStr);
                    String countryId = config.getString("players." + uuidStr + ".country");
                    String roleStr = config.getString("players." + uuidStr + ".role");
                    
                    playerCountries.put(uuid, countryId);
                    if (roleStr != null) {
                        playerRoles.put(uuid, Role.valueOf(roleStr));
                    }
                }
            }
            
            // Load diplomacy data
            if (config.contains("diplomacy")) {
                for (String countryId1 : config.getConfigurationSection("diplomacy").getKeys(false)) {
                    Map<String, DiplomacyState> relations = new HashMap<>();
                    for (String countryId2 : config.getConfigurationSection("diplomacy." + countryId1).getKeys(false)) {
                        String stateStr = config.getString("diplomacy." + countryId1 + "." + countryId2);
                        relations.put(countryId2, DiplomacyState.valueOf(stateStr));
                    }
                    diplomacy.put(countryId1, relations);
                }
            }
        }
    }
    
    private void savePlayerData() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File playersFile = new File(dataFolder, "players.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playersFile);
        
        // Save player countries and roles
        for (UUID uuid : playerCountries.keySet()) {
            String countryId = playerCountries.get(uuid);
            Role role = playerRoles.get(uuid);
            
            config.set("players." + uuid.toString() + ".country", countryId);
            if (role != null) {
                config.set("players." + uuid.toString() + ".role", role.name());
            }
        }
        
        // Save diplomacy data
        for (String countryId1 : diplomacy.keySet()) {
            Map<String, DiplomacyState> relations = diplomacy.get(countryId1);
            for (String countryId2 : relations.keySet()) {
                DiplomacyState state = relations.get(countryId2);
                config.set("diplomacy." + countryId1 + "." + countryId2, state.name());
            }
        }
        
        try {
            config.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }
    
    public enum Role {
        PRESIDENT,
        VICE_PRESIDENT,
        MAYOR,
        RESIDENT
    }
    
    public Map<UUID, String> getPlayerCountryMap() {
        return new HashMap<>(playerCountries);
    }
    
    public enum DiplomacyState {
        NEUTRAL,
        WAR,
        ALLIANCE
    }
}