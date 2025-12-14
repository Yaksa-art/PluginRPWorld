package com.rpstrana;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryManager {
    
    public static void openMainMenu(Player player) {
        CountryManager countryManager = Main.getInstance().getCountryManager();
        
        if (countryManager.hasCountry(player.getUniqueId())) {
            // Player has a country - show country dashboard
            openCountryDashboard(player);
        } else {
            // Player doesn't have a country - show country selection
            openCountrySelection(player);
        }
    }
    
    private static void openCountryDashboard(Player player) {
        CountryManager countryManager = Main.getInstance().getCountryManager();
        String countryId = countryManager.getPlayerCountry(player.getUniqueId());
        String countryName = countryManager.getCountryName(countryId);
        CountryManager.Role role = countryManager.getPlayerRole(player.getUniqueId());
        
        Inventory inventory = Bukkit.createInventory(null, 54, 
                ChatColor.DARK_BLUE + "Country Dashboard - " + ChatColor.GOLD + countryName);
        
        // Fill with placeholder items
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glassPane);
        }
        
        // Country info item
        ItemStack countryInfo = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta countryMeta = countryInfo.getItemMeta();
        countryMeta.setDisplayName(ChatColor.GOLD + "Country Information");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "Name: " + countryName);
        lore.add(ChatColor.WHITE + "ID: " + countryId);
        lore.add(ChatColor.WHITE + "Your Role: " + role.name());
        countryMeta.setLore(lore);
        countryInfo.setItemMeta(countryMeta);
        inventory.setItem(4, countryInfo);
        
        // Members list
        ItemStack membersItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = membersItem.getItemMeta();
        membersMeta.setDisplayName(ChatColor.AQUA + "Members");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to view country members");
        membersMeta.setLore(lore);
        membersItem.setItemMeta(membersMeta);
        inventory.setItem(20, membersItem);
        
        // Diplomacy item
        ItemStack diplomacyItem = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta diplomacyMeta = diplomacyItem.getItemMeta();
        diplomacyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Diplomacy");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Manage diplomatic relations");
        diplomacyMeta.setLore(lore);
        diplomacyItem.setItemMeta(diplomacyMeta);
        inventory.setItem(22, diplomacyItem);
        
        // Manage country (President only)
        if (role == CountryManager.Role.PRESIDENT) {
            ItemStack manageItem = new ItemStack(Material.ANVIL);
            ItemMeta manageMeta = manageItem.getItemMeta();
            manageMeta.setDisplayName(ChatColor.RED + "Manage Country");
            lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Rename country, manage settings");
            manageMeta.setLore(lore);
            manageItem.setItemMeta(manageMeta);
            inventory.setItem(24, manageItem);
        }
        
        // Leave country button
        ItemStack leaveItem = new ItemStack(Material.BARRIER);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName(ChatColor.RED + "Leave Country");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to leave your country");
        leaveMeta.setLore(lore);
        leaveItem.setItemMeta(leaveMeta);
        inventory.setItem(49, leaveItem);
        
        player.openInventory(inventory);
    }
    
    private static void openCountrySelection(Player player) {
        CountryManager countryManager = Main.getInstance().getCountryManager();
        Map<String, String> countries = countryManager.getCountries();
        
        // Calculate inventory size based on number of countries
        int size = Math.min(Math.max(9, (int) Math.ceil(countries.size() / 9.0) * 9), 54); // Max 54 slots (6 rows)
        
        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.DARK_GREEN + "Select a Country");
        
        // Fill with country items
        int slot = 0;
        for (Map.Entry<String, String> entry : countries.entrySet()) {
            if (slot >= size) break;
            
            ItemStack countryItem = new ItemStack(Material.WOOL);
            ItemMeta meta = countryItem.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + entry.getValue());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE + "ID: " + entry.getKey());
            lore.add(ChatColor.GRAY + "Click to join this country");
            meta.setLore(lore);
            countryItem.setItemMeta(meta);
            
            inventory.setItem(slot, countryItem);
            slot++;
        }
        
        player.openInventory(inventory);
    }
    
    public static void openMembersList(Player player) {
        CountryManager countryManager = Main.getInstance().getCountryManager();
        String countryId = countryManager.getPlayerCountry(player.getUniqueId());
        
        // Calculate inventory size based on number of members
        int memberCount = 0;
        for (java.util.UUID uuid : countryManager.getPlayerCountryMap().keySet()) {
            if (countryId.equals(countryManager.getPlayerCountry(uuid))) {
                memberCount++;
            }
        }
        
        int size = Math.min(Math.max(9, (int) Math.ceil(memberCount / 9.0) * 9), 54); // Max 54 slots (6 rows)
        
        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.AQUA + "Country Members");
        
        // Add members to inventory
        int slot = 0;
        for (java.util.UUID uuid : countryManager.getPlayerCountryMap().keySet()) {
            if (!countryId.equals(countryManager.getPlayerCountry(uuid))) continue;
            if (slot >= size) break;
            
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String playerName = offlinePlayer.getName();
            if (playerName == null) playerName = "Unknown Player";
            
            CountryManager.Role role = countryManager.getPlayerRole(uuid);
            
            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerItem.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + playerName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Role: " + role.name());
            meta.setLore(lore);
            playerItem.setItemMeta(meta);
            
            inventory.setItem(slot, playerItem);
            slot++;
        }
        
        player.openInventory(inventory);
    }
    
    public static void openDiplomacyMenu(Player player) {
        CountryManager countryManager = Main.getInstance().getCountryManager();
        String countryId = countryManager.getPlayerCountry(player.getUniqueId());
        CountryManager.Role role = countryManager.getPlayerRole(player.getUniqueId());
        
        if (role != CountryManager.Role.PRESIDENT && role != CountryManager.Role.VICE_PRESIDENT) {
            player.sendMessage(ChatColor.RED + "Only President and Vice President can manage diplomacy.");
            return;
        }
        
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "Diplomacy Management");
        
        // Fill with placeholder items
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glassPane);
        }
        
        // Display diplomatic relations
        int slot = 9; // Start after top row
        for (String otherCountryId : countryManager.getCountries().keySet()) {
            if (otherCountryId.equals(countryId)) continue; // Skip own country
            if (slot >= 45) break; // Stop before bottom row
            
            String otherCountryName = countryManager.getCountryName(otherCountryId);
            CountryManager.DiplomacyState relation = countryManager.getDiplomacyState(countryId, otherCountryId);
            
            Material material;
            ChatColor color;
            switch (relation) {
                case ALLIANCE:
                    material = Material.EMERALD_BLOCK;
                    color = ChatColor.GREEN;
                    break;
                case WAR:
                    material = Material.REDSTONE_BLOCK;
                    color = ChatColor.RED;
                    break;
                default:
                    material = Material.COBBLESTONE;
                    color = ChatColor.GRAY;
                    break;
            }
            
            ItemStack relationItem = new ItemStack(material);
            ItemMeta meta = relationItem.getItemMeta();
            meta.setDisplayName(color + otherCountryName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE + "Relation: " + relation.name());
            lore.add(ChatColor.GRAY + "Left-click: Change relation");
            lore.add(ChatColor.GRAY + "Right-click: View details");
            meta.setLore(lore);
            relationItem.setItemMeta(meta);
            
            inventory.setItem(slot, relationItem);
            slot++;
        }
        
        player.openInventory(inventory);
    }
}