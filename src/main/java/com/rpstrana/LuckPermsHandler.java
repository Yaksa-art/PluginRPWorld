package com.rpstrana;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;

import java.util.concurrent.CompletableFuture;

public class LuckPermsHandler {
    
    private LuckPerms luckPerms;
    
    public void initialize(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }
    
    public void updatePlayerMetadata(java.util.UUID playerId, String countryId, CountryManager.Role role) {
        if (luckPerms == null) {
            return;
        }
        
        UserManager userManager = luckPerms.getUserManager();
        
        // Get user asynchronously
        CompletableFuture<User> userFuture = userManager.loadUser(playerId);
        userFuture.thenAccept(user -> {
            if (user == null) {
                Main.getInstance().getLogger().warning("Could not load user: " + playerId);
                return;
            }
            
            // Remove existing prefix and suffix nodes
            user.data().clear(node -> node.getType() == PrefixNode.TYPE || node.getType() == SuffixNode.TYPE);
            
            // Create new prefix and suffix
            String countryName = Main.getInstance().getCountryManager().getCountryName(countryId);
            if (countryName == null) {
                countryName = "Unknown";
            }
            
            // Format: [CountryName]
            String prefix = "[" + countryName + "]";
            // Format: (RoleName)
            String suffix = "(" + role.name() + ")";
            
            // Create prefix and suffix nodes
            PrefixNode prefixNode = PrefixNode.builder(prefix, 100).build();
            SuffixNode suffixNode = SuffixNode.builder(suffix, 100).build();
            
            // Add the nodes to the user's data
            user.data().add(prefixNode);
            user.data().add(suffixNode);
            
            // Save changes back to LuckPerms
            luckPerms.getUserManager().saveUser(user);
        }).exceptionally(throwable -> {
            Main.getInstance().getLogger().severe("Error updating player metadata: " + throwable.getMessage());
            return null;
        });
    }
    
    public void removePlayerMetadata(java.util.UUID playerId) {
        if (luckPerms == null) {
            return;
        }
        
        UserManager userManager = luckPerms.getUserManager();
        
        // Get user asynchronously
        CompletableFuture<User> userFuture = userManager.loadUser(playerId);
        userFuture.thenAccept(user -> {
            if (user == null) {
                Main.getInstance().getLogger().warning("Could not load user: " + playerId);
                return;
            }
            
            // Remove existing prefix and suffix nodes
            user.data().clear(node -> node.getType() == PrefixNode.TYPE || node.getType() == SuffixNode.TYPE);
            
            // Save changes back to LuckPerms
            luckPerms.getUserManager().saveUser(user);
        }).exceptionally(throwable -> {
            Main.getInstance().getLogger().severe("Error removing player metadata: " + throwable.getMessage());
            return null;
        });
    }
    
    public LuckPerms getApi() {
        return luckPerms;
    }
}