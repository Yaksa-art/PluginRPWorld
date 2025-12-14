package com.rpstrana;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandManager implements CommandExecutor, TabCompleter {
    
    private final Main plugin;
    
    public CommandManager(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open GUI
            InventoryManager.openMainMenu(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "countryload":
                if (!player.hasPermission("rpstrana.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                plugin.getCountryManager().loadCountriesFromAPI();
                player.sendMessage(ChatColor.GREEN + "Loading countries from API...");
                return true;
                
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rp invite <player>");
                    return true;
                }
                
                handleInviteCommand(player, args[1]);
                return true;
                
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rp join <country_id>");
                    return true;
                }
                
                handleJoinCommand(player, args[1]);
                return true;
                
            case "promote":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rp promote <player>");
                    return true;
                }
                
                handlePromoteCommand(player, args[1]);
                return true;
                
            case "demote":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /rp demote <player>");
                    return true;
                }
                
                handleDemoteCommand(player, args[1]);
                return true;
                
            case "leave":
                handleLeaveCommand(player);
                return true;
                
            case "help":
                sendHelpMessage(player);
                return true;
                
            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid invitation.");
                    return true;
                }
                handleAcceptInvite(player, args[1]);
                return true;
                
            case "deny":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Invalid invitation.");
                    return true;
                }
                handleDenyInvite(player, args[1]);
                return true;
                
            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Use /rp help for available commands.");
                return true;
        }
    }
    
    private void handleInviteCommand(Player sender, String targetPlayerName) {
        Player target = Bukkit.getPlayer(targetPlayerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + targetPlayerName);
            return;
        }
        
        // Check if sender has a country and appropriate role
        if (!plugin.getCountryManager().hasCountry(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You must be in a country to invite players.");
            return;
        }
        
        CountryManager.Role senderRole = plugin.getCountryManager().getPlayerRole(sender.getUniqueId());
        if (senderRole != CountryManager.Role.PRESIDENT && 
            senderRole != CountryManager.Role.VICE_PRESIDENT && 
            senderRole != CountryManager.Role.MAYOR) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to invite players.");
            return;
        }
        
        String senderCountryId = plugin.getCountryManager().getPlayerCountry(sender.getUniqueId());
        
        // Create invitation message with clickable components
        TextComponent message = new TextComponent(ChatColor.GOLD + "[Rpstrana] " + ChatColor.WHITE + sender.getName() + 
                                                " invites you to join their country (" + 
                                                plugin.getCountryManager().getCountryName(senderCountryId) + "). ");
        
        TextComponent acceptButton = new TextComponent("[ACCEPT]");
        acceptButton.setColor(ChatColor.GREEN);
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rp accept " + sender.getUniqueId()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to accept the invitation")));
        
        TextComponent separator = new TextComponent(" ");
        
        TextComponent denyButton = new TextComponent("[DENY]");
        denyButton.setColor(ChatColor.RED);
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rp deny " + sender.getUniqueId()));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to deny the invitation")));
        
        message.addExtra(acceptButton);
        message.addExtra(separator);
        message.addExtra(denyButton);
        
        target.spigot().sendMessage(message);
        sender.sendMessage(ChatColor.GREEN + "Invitation sent to " + target.getName());
    }
    
    private void handleJoinCommand(Player player, String countryId) {
        if (plugin.getCountryManager().hasCountry(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in a country. Leave your current country first.");
            return;
        }
        
        if (!plugin.getCountryManager().getCountries().containsKey(countryId)) {
            player.sendMessage(ChatColor.RED + "Invalid country ID: " + countryId);
            return;
        }
        
        plugin.getCountryManager().setPlayerCountryAndRole(player.getUniqueId(), countryId, CountryManager.Role.RESIDENT);
        player.sendMessage(ChatColor.GREEN + "You have joined " + plugin.getCountryManager().getCountryName(countryId));
    }
    
    private void handlePromoteCommand(Player sender, String targetPlayerName) {
        Player target = Bukkit.getPlayer(targetPlayerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + targetPlayerName);
            return;
        }
        
        // Check permissions
        if (!plugin.getCountryManager().hasCountry(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You must be in a country to promote players.");
            return;
        }
        
        if (!plugin.getCountryManager().hasCountry(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Target player is not in a country.");
            return;
        }
        
        if (!plugin.getCountryManager().getPlayerCountry(sender.getUniqueId()).equals(
                plugin.getCountryManager().getPlayerCountry(target.getUniqueId()))) {
            sender.sendMessage(ChatColor.RED + "Target player is not in your country.");
            return;
        }
        
        CountryManager.Role senderRole = plugin.getCountryManager().getPlayerRole(sender.getUniqueId());
        if (senderRole != CountryManager.Role.PRESIDENT) {
            sender.sendMessage(ChatColor.RED + "Only the president can promote players.");
            return;
        }
        
        CountryManager.Role targetRole = plugin.getCountryManager().getPlayerRole(target.getUniqueId());
        if (targetRole == CountryManager.Role.PRESIDENT) {
            sender.sendMessage(ChatColor.RED + "Cannot promote the president.");
            return;
        }
        
        // Determine next role in promotion hierarchy
        CountryManager.Role newRole;
        switch (targetRole) {
            case RESIDENT:
                newRole = CountryManager.Role.MAYOR;
                break;
            case MAYOR:
                newRole = CountryManager.Role.VICE_PRESIDENT;
                break;
            case VICE_PRESIDENT:
                newRole = CountryManager.Role.PRESIDENT;
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Cannot promote this player further.");
                return;
        }
        
        plugin.getCountryManager().setPlayerRole(target.getUniqueId(), newRole);
        sender.sendMessage(ChatColor.GREEN + "Promoted " + target.getName() + " to " + newRole.name());
        target.sendMessage(ChatColor.GREEN + "You have been promoted to " + newRole.name());
    }
    
    private void handleDemoteCommand(Player sender, String targetPlayerName) {
        Player target = Bukkit.getPlayer(targetPlayerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + targetPlayerName);
            return;
        }
        
        // Check permissions
        if (!plugin.getCountryManager().hasCountry(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You must be in a country to demote players.");
            return;
        }
        
        if (!plugin.getCountryManager().hasCountry(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Target player is not in a country.");
            return;
        }
        
        if (!plugin.getCountryManager().getPlayerCountry(sender.getUniqueId()).equals(
                plugin.getCountryManager().getPlayerCountry(target.getUniqueId()))) {
            sender.sendMessage(ChatColor.RED + "Target player is not in your country.");
            return;
        }
        
        CountryManager.Role senderRole = plugin.getCountryManager().getPlayerRole(sender.getUniqueId());
        if (senderRole != CountryManager.Role.PRESIDENT) {
            sender.sendMessage(ChatColor.RED + "Only the president can demote players.");
            return;
        }
        
        CountryManager.Role targetRole = plugin.getCountryManager().getPlayerRole(target.getUniqueId());
        if (targetRole == CountryManager.Role.PRESIDENT) {
            sender.sendMessage(ChatColor.RED + "Cannot demote the president. Consider transferring presidential duties first.");
            return;
        }
        
        // Determine next role in demotion hierarchy
        CountryManager.Role newRole;
        switch (targetRole) {
            case VICE_PRESIDENT:
                newRole = CountryManager.Role.MAYOR;
                break;
            case MAYOR:
                newRole = CountryManager.Role.RESIDENT;
                break;
            case RESIDENT:
                sender.sendMessage(ChatColor.RED + "Cannot demote this player further.");
                return;
            default:
                newRole = CountryManager.Role.RESIDENT;
                break;
        }
        
        plugin.getCountryManager().setPlayerRole(target.getUniqueId(), newRole);
        sender.sendMessage(ChatColor.GREEN + "Demoted " + target.getName() + " to " + newRole.name());
        target.sendMessage(ChatColor.YELLOW + "You have been demoted to " + newRole.name());
    }
    
    private void handleLeaveCommand(Player player) {
        if (!plugin.getCountryManager().hasCountry(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not in a country.");
            return;
        }
        
        String countryName = plugin.getCountryManager().getCountryName(
                plugin.getCountryManager().getPlayerCountry(player.getUniqueId()));
        
        plugin.getCountryManager().removePlayerFromCountry(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "You have left " + countryName);
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Rpstrana Commands ===");
        player.sendMessage(ChatColor.AQUA + "/rp" + ChatColor.WHITE + " - Open main menu");
        player.sendMessage(ChatColor.AQUA + "/rp invite <player>" + ChatColor.WHITE + " - Invite player to your country");
        player.sendMessage(ChatColor.AQUA + "/rp join <country_id>" + ChatColor.WHITE + " - Join a country");
        player.sendMessage(ChatColor.AQUA + "/rp promote <player>" + ChatColor.WHITE + " - Promote a player");
        player.sendMessage(ChatColor.AQUA + "/rp demote <player>" + ChatColor.WHITE + " - Demote a player");
        player.sendMessage(ChatColor.AQUA + "/rp leave" + ChatColor.WHITE + " - Leave your country");
        player.sendMessage(ChatColor.AQUA + "/rp countryload" + ChatColor.WHITE + " - Admin: Load countries from API");
        player.sendMessage(ChatColor.AQUA + "/rp help" + ChatColor.WHITE + " - Show this help message");
    }
    
    public void handleAcceptInvite(Player player, String senderUuidStr) {
        try {
            UUID senderUuid = UUID.fromString(senderUuidStr);
            Player sender = Bukkit.getPlayer(senderUuid);
            
            if (sender == null) {
                player.sendMessage(ChatColor.RED + "The player who invited you is no longer online.");
                return;
            }
            
            if (!plugin.getCountryManager().hasCountry(senderUuid)) {
                player.sendMessage(ChatColor.RED + "The player who invited you is no longer in a country.");
                return;
            }
            
            String countryId = plugin.getCountryManager().getPlayerCountry(senderUuid);
            plugin.getCountryManager().setPlayerCountryAndRole(player.getUniqueId(), countryId, CountryManager.Role.RESIDENT);
            
            player.sendMessage(ChatColor.GREEN + "You have joined " + plugin.getCountryManager().getCountryName(countryId));
            sender.sendMessage(ChatColor.GREEN + player.getName() + " has accepted your invitation.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid invitation.");
        }
    }
    
    public void handleDenyInvite(Player player, String senderUuidStr) {
        try {
            UUID senderUuid = UUID.fromString(senderUuidStr);
            Player sender = Bukkit.getPlayer(senderUuid);
            
            if (sender != null) {
                sender.sendMessage(ChatColor.YELLOW + player.getName() + " has declined your invitation.");
            }
            
            player.sendMessage(ChatColor.YELLOW + "You have declined the invitation.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid invitation.");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("help");
            completions.add("invite");
            completions.add("join");
            completions.add("promote");
            completions.add("demote");
            completions.add("leave");
            completions.add("accept");
            completions.add("deny");
            
            if (sender.hasPermission("rpstrana.admin")) {
                completions.add("countryload");
            }
            
            return filterCompletions(completions, args[0]);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("accept") || 
                                        args[0].equalsIgnoreCase("deny"))) {
            // These commands expect a UUID, so we don't provide completions
            return Collections.emptyList();
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || 
                                        args[0].equalsIgnoreCase("promote") || 
                                        args[0].equalsIgnoreCase("demote"))) {
            List<String> completions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            return filterCompletions(completions, args[1]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            List<String> completions = new ArrayList<>(plugin.getCountryManager().getCountries().keySet());
            return filterCompletions(completions, args[1]);
        }
        
        return Collections.emptyList();
    }
    
    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) {
            return completions;
        }
        
        String lowerInput = input.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(lowerInput)) {
                filtered.add(completion);
            }
        }
        return filtered;
    }
}