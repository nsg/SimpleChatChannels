package me.odium.simplechatchannels.listeners;

import java.util.List;
import java.util.logging.Logger;

import me.odium.simplechatchannels.Loader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PListener implements Listener {

  Logger log = Logger.getLogger("Minecraft");

  public Loader plugin;
  public PListener(Loader plugin) {    
    this.plugin = plugin;    
    plugin.getServer().getPluginManager().registerEvents(this, plugin);  
  }


  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerQuit(PlayerQuitEvent event){
    Player player = event.getPlayer();
  
    // Use PersistentChannels to find the channels to part from
    if (plugin.getStorageConfig().contains("PersistentChannels")) {
      List<String> persistentPlayerChannels = plugin.getStorageConfig().getStringList("PersistentChannels." + player.getName());
      for (String channel : persistentPlayerChannels) {
        plugin.partChannel(player, channel);
      }
    }
    
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerJoin(PlayerJoinEvent event) {
    plugin.loadPersistentPlayerChannels(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerChat(AsyncPlayerChatEvent chat) {
    Player player = chat.getPlayer();
    String message = chat.getMessage();

    if (plugin.hasMsgLock(player)) {
      Player mlock = plugin.getMsgLock(player);
      mlock.sendMessage(ChatColor.GOLD + "[" + player.getName() + " -> " + mlock.getName() + "] " + ChatColor.WHITE + message);
      chat.setCancelled(true);
      return;
    }
    
    if(plugin.InChannel.containsKey(player)){ // if key says player is in a channel
      String Chan = plugin.ChannelMap.get(player); // get the channel
      log.info("[#" + Chan + "] <" + player.getDisplayName() + "> " + message); // log the message to console

      Player[] players = Bukkit.getOnlinePlayers(); // get all online players      
      List<String> ChanList = plugin.getStorageConfig().getStringList("Channels."+Chan+".list"); // get the list of users in channel

      String prefixTemp = plugin.getConfig().getString("ChatPrefix.Prefix").replace("`player", player.getDisplayName()).replace("`channel", Chan);      
      String prefix = plugin.replaceColorMacros(prefixTemp); 

      for(Player op : players){
        if (plugin.SpyMap.containsKey(op)) { // If player is using spychan
          if (plugin.SpyMap.get(op) == "all" || plugin.SpyMap.get(op).equalsIgnoreCase(Chan)) { // if player is spying on all or specific channel being used
            op.sendMessage(ChatColor.RED+"SPY: "+ChatColor.RESET+prefix +" "+ message); // send them the channel message
          }          
        }


        if(ChanList.contains(op.getName().toLowerCase())) { // if the channel list contains the name of an online player
          op.sendMessage(prefix +" "+ message); // send them the channel message
        }
      }
      chat.setCancelled(true); // cancel the message for everyone else
    }
  }

}

