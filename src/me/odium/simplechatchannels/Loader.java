package me.odium.simplechatchannels;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.odium.simplechatchannels.commands.addchan;
import me.odium.simplechatchannels.commands.addowner;
import me.odium.simplechatchannels.commands.adduser;
import me.odium.simplechatchannels.commands.chanlist;
import me.odium.simplechatchannels.commands.delchan;
import me.odium.simplechatchannels.commands.delowner;
import me.odium.simplechatchannels.commands.deluser;
import me.odium.simplechatchannels.commands.joinchan;
import me.odium.simplechatchannels.commands.kuser;
import me.odium.simplechatchannels.commands.leavechan;
import me.odium.simplechatchannels.commands.scc;
import me.odium.simplechatchannels.commands.spychan;
import me.odium.simplechatchannels.commands.topic;
import me.odium.simplechatchannels.commands.Global;
import me.odium.simplechatchannels.commands.Msg;
import me.odium.simplechatchannels.listeners.PListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Loader extends JavaPlugin {
  public static Loader plugin;
  public Logger log = Logger.getLogger("Minecraft");

  //ColorMacros
  public ChatColor WHITE = ChatColor.WHITE;
  public ChatColor RED = ChatColor.RED;
  public ChatColor AQUA = ChatColor.AQUA;
  public ChatColor BLUE = ChatColor.BLUE;
  public ChatColor GREEN = ChatColor.GREEN;
  public ChatColor DARK_GREEN = ChatColor.DARK_GREEN;
  public ChatColor DARK_RED = ChatColor.DARK_RED;
  public ChatColor GRAY = ChatColor.GRAY;
  public ChatColor GOLD = ChatColor.GOLD;

  public Map<Player, String> ChannelMap = new HashMap<Player, String>();
  public Map<Player, Boolean> InChannel = new HashMap<Player, Boolean>();
  public Map<Player, String> SpyMap = new HashMap<Player, String>();
  public int overRide = 0;
  
  private Map<Player, Player> lockMsgTo = null;

  // Custom Config  
  private FileConfiguration StorageConfig = null;
  private File StorageConfigFile = null;

  public void reloadStorageConfig() {
    if (StorageConfigFile == null) {
      StorageConfigFile = new File(getDataFolder(), "StorageConfig.yml");
    }
    StorageConfig = YamlConfiguration.loadConfiguration(StorageConfigFile);

    // Look for defaults in the jar
    InputStream defConfigStream = getResource("StorageConfig.yml");
    if (defConfigStream != null) {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
      StorageConfig.setDefaults(defConfig);
    }
  }
  public FileConfiguration getStorageConfig() {
    if (StorageConfig == null) {
      reloadStorageConfig();
    }
    return StorageConfig;
  }
  public void saveStorageConfig() {
    if (StorageConfig == null || StorageConfigFile == null) {
      return;
    }
    try {
      StorageConfig.save(StorageConfigFile);
    } catch (IOException ex) {
      Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + StorageConfigFile, ex);
    }
  }
  // End Custom Config

  public String myGetPlayerName(String name) { 
    Player caddPlayer = getServer().getPlayerExact(name);
    String pName;
    if(caddPlayer == null) {
      caddPlayer = getServer().getPlayer(name);
      if(caddPlayer == null) {
        pName = name;
      } else {
        pName = caddPlayer.getName();
      }
    } else {
      pName = caddPlayer.getName();
    }
    return pName;
  }

  public void onEnable() {
    // declare new listener
    new PListener(this);
    // Load Config.yml
    FileConfiguration cfg = getConfig();
    FileConfigurationOptions cfgOptions = cfg.options();
    cfgOptions.copyDefaults(true);
    cfgOptions.copyHeader(true);
    saveConfig(); 
    // Load Custom Config
    FileConfiguration ccfg = getStorageConfig();
    FileConfigurationOptions ccfgOptions = ccfg.options();
    ccfgOptions.copyDefaults(true);
    ccfgOptions.copyHeader(true);
    saveStorageConfig();
    // Load command executors
    this.getCommand("addchan").setExecutor(new addchan(this));
    this.getCommand("addowner").setExecutor(new addowner(this));
    this.getCommand("adduser").setExecutor(new adduser(this));
    this.getCommand("chanlist").setExecutor(new chanlist(this));
    this.getCommand("delchan").setExecutor(new delchan(this));
    this.getCommand("delowner").setExecutor(new delowner(this));
    this.getCommand("deluser").setExecutor(new deluser(this));
    this.getCommand("joinchan").setExecutor(new joinchan(this));
    this.getCommand("kuser").setExecutor(new kuser(this));
    this.getCommand("leavechan").setExecutor(new leavechan(this));
    this.getCommand("scc").setExecutor(new scc(this));
    this.getCommand("topic").setExecutor(new topic(this));
    this.getCommand("spychan").setExecutor(new spychan(this));
    this.getCommand("global").setExecutor(new Global(this));
    this.getCommand("msg").setExecutor(new Msg(this));

    cleanOldDecayedChannels();
    
    plugin = this;

    log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " enabled.");
  }

  public void onDisable() {
    Map<String,Object> ChansList = getStorageConfig().getConfigurationSection("Channels").getValues(false);
    Iterator<Entry<String, Object>> it = ChansList.entrySet().iterator();
    while(it.hasNext()) {
      Entry<String,Object> itv = it.next();
      String ch = itv.getKey();
      List<String> PList = getStorageConfig().getStringList("Channels."+ch+".list"); // get the player list
      PList.removeAll(PList);
      getStorageConfig().set("Channels."+ch+".list", PList); // set the new list
    }
    List<String> InChatList = getStorageConfig().getStringList("InChatList");
    InChatList.clear();
    getStorageConfig().set("InChatList", InChatList); // set the new list
    saveStorageConfig();
    PluginDescriptionFile pdfFile = this.getDescription();
    log.info(pdfFile.getName() + " disabled.");
  }

  public String replaceColorMacros(String str) {
    str = str.replace("`r", ChatColor.RED.toString());
    str = str.replace("`R", ChatColor.DARK_RED.toString());        
    str = str.replace("`y", ChatColor.YELLOW.toString());
    str = str.replace("`Y", ChatColor.GOLD.toString());
    str = str.replace("`g", ChatColor.GREEN.toString());
    str = str.replace("`G", ChatColor.DARK_GREEN.toString());        
    str = str.replace("`c", ChatColor.AQUA.toString());
    str = str.replace("`C", ChatColor.DARK_AQUA.toString());        
    str = str.replace("`b", ChatColor.BLUE.toString());
    str = str.replace("`B", ChatColor.DARK_BLUE.toString());        
    str = str.replace("`p", ChatColor.LIGHT_PURPLE.toString());
    str = str.replace("`P", ChatColor.DARK_PURPLE.toString());
    str = str.replace("`0", ChatColor.BLACK.toString());
    str = str.replace("`1", ChatColor.DARK_GRAY.toString());
    str = str.replace("`2", ChatColor.GRAY.toString());
    str = str.replace("`w", ChatColor.WHITE.toString());    
    return str;
  }

  public void setChannel(Player player, String channel){
    InChannel.put(player, true);
    ChannelMap.put(player, channel);
    
    // Note the channel for the channel decay
    int channelUsage = getStorageConfig().getInt("Channels."+channel + ".Usage");
    channelUsage = (int) (System.currentTimeMillis() / 1000L);
    getStorageConfig().set("Channels."+channel + ".Usage", channelUsage);
    saveStorageConfig();
    
    player.sendMessage(DARK_GREEN+"[SCC] "+ ChatColor.GOLD + player.getDisplayName() + ChatColor.DARK_GREEN + " " +channel + " is active");
  }

  public void joinChannel(Player player, String channel){
    joinChannel(player, channel, true);
  }
  
  public void joinChannel(Player player, String channel, Boolean isActive){

    String playerName = player.getName().toLowerCase(); // get the player name
    Player[] players = Bukkit.getOnlinePlayers(); // get all online players
    String playerDisplayName = player.getDisplayName();

    if (isActive) {
      setChannel(player, channel);
    }

    List<String> ChList = getStorageConfig().getStringList("Channels."+channel+".list"); // get the player list
    ChList.add(playerName);  // add the player to the list
    getStorageConfig().set("Channels."+channel+".list", ChList); // set the new list
    saveStorageConfig();

    // NOTIFY USERS IN CHANNEL OF A JOIN
    List<String> ChanList = getStorageConfig().getStringList("Channels."+channel+".list");
    for(Player op: players){
      if(ChanList.contains(op.getName().toLowerCase())) {
        op.sendMessage(DARK_GREEN+"[SCC] "+ ChatColor.GOLD + playerDisplayName + ChatColor.DARK_GREEN+" joined "+ channel);              
      }
    }
    // SHOW TOPIC
    if (getStorageConfig().getString("Channels."+channel+".topic") != null) {
      String topic = getStorageConfig().getString("Channels."+channel+".topic");
      player.sendMessage(DARK_GREEN+"[SCC] Topic for " + channel + ": "+ChatColor.WHITE+ topic);
    }
    
    // Save the state so we can re-join the user to his channels when the user joins
    setPersistentPlayerChannels(playerName, channel);

  }

  public void partChannel(Player player, String channel){

    String playerName = player.getName().toLowerCase(); // get the player name
    String playerDisplayName = player.getDisplayName();
    Player[] players = Bukkit.getOnlinePlayers(); // get all online players

    InChannel.remove(player);
    ChannelMap.remove(player);

    List<String> ChList = getStorageConfig().getStringList("Channels."+channel+".list"); // get the player list
    ChList.remove(playerName);  // remove the player from the list
    getStorageConfig().set("Channels."+channel+".list", ChList); // set the new list
    saveStorageConfig();

    // NOTIFY USERS IN CHANNEL OF A PART
    player.sendMessage(DARK_GREEN+"[SCC] "+ ChatColor.GOLD + playerDisplayName + ChatColor.DARK_GREEN+" left "+ channel);
    List<String> ChanList = getStorageConfig().getStringList("Channels."+channel+".list");
    for(Player play : players){
      if(ChanList.contains(play.getName())) {
        play.sendMessage(DARK_GREEN+"[SCC] "+ ChatColor.GOLD + playerDisplayName + ChatColor.DARK_GREEN+" left "+ channel);
      }
    }

  }

  public void toggleChannel(Player player, String channel){
    if (InChannel.containsKey(player)) { // IF PLAYER IS IN A CHANNEL
      partChannel(player, channel);
    } else { // if player is not in  a channel
      joinChannel(player, channel);
    }
  }
  
  public void setPersistentPlayerChannels(String playerName, String channel) {
    if (!getStorageConfig().contains("PersistentChannels")) {
      getStorageConfig().createSection("PersistentChannels");
    }
    List<String> persistentPlayerChannels = getStorageConfig().getStringList("PersistentChannels." + playerName);
    if (!persistentPlayerChannels.contains(channel)) {
      persistentPlayerChannels.add(channel);
      getStorageConfig().set("PersistentChannels." + playerName, persistentPlayerChannels);
      saveStorageConfig();
    }
  }

  public void removePersistentPlayerChannels(String playerName, String channel) {
    if (getStorageConfig().contains("PersistentChannels")) {
      List<String> persistentPlayerChannels = getStorageConfig().getStringList("PersistentChannels." + playerName);
      if (persistentPlayerChannels.contains(channel)) {
        persistentPlayerChannels.remove(channel);
        getStorageConfig().set("PersistentChannels." + playerName, persistentPlayerChannels);
        saveStorageConfig();
      }
    }
  }  
  
  public void loadPersistentPlayerChannels(Player player) {
    if (getStorageConfig().contains("PersistentChannels")) {
      List<String> persistentPlayerChannels = getStorageConfig().getStringList("PersistentChannels." + player.getName());
      for (String channel : persistentPlayerChannels) {
        joinChannel(player, channel, false);
      }
    }
  }
  
  public void cleanOldDecayedChannels() {
    
    Logger log = getLogger();
    log.info("cleanOldDecayedChannels triggered");
    
    Map<String,Object> channelData = getStorageConfig().getConfigurationSection("Channels").getValues(false);
    Iterator<Entry<String, Object>> it = channelData.entrySet().iterator();
    while(it.hasNext()) {
      Entry<String,Object> itv = it.next();
      String channel = itv.getKey();
      int channelUsage = getStorageConfig().getInt("Channels."+channel + ".Usage");
      int oneFortnight = 60; //86400 * 14;
      int timeStamp = (int) (System.currentTimeMillis() / 1000L);
      log.info("cleanOldDecayedChannels: Check " + channel + "; usage: " + channelUsage);
      
      if (timeStamp - oneFortnight > channelUsage) {
        log.info("cleanOldDecayedChannels: " + channel + " is to old, remove from Channels");
        getStorageConfig().set("Channels."+channel, null);
        Map<String,Object> persistentPlayers = getStorageConfig().getConfigurationSection("PersistentChannels").getValues(false);
        Iterator<Entry<String, Object>> it2 = persistentPlayers.entrySet().iterator();
        while(it2.hasNext()) {
          Entry<String,Object> itv2 = it2.next();
          String pplayer = itv2.getKey();
          List<String> persistentPlayerChannels = getStorageConfig().getStringList("PersistentChannels." + pplayer);
          if (persistentPlayerChannels.contains(channel)) {
            persistentPlayerChannels.remove(channel);
            log.info("cleanOldDecayedChannels: remove " + channel + " from " + pplayer + " auto-join list");
          }
          getStorageConfig().set("PersistentChannels." + pplayer, persistentPlayerChannels);
        }
      }
      
    }
    
    saveStorageConfig();
  }

  public boolean NotExist(CommandSender sender, String ChanName) {
    sender.sendMessage(DARK_RED+"[SCC] Channel "+ChatColor.GOLD+ChanName+ChatColor.DARK_RED+" does not exist!");
    return true;
  }
  public boolean NotOwner(CommandSender sender, String ChanName) {
    sender.sendMessage(DARK_RED+"[SCC] "+ChatColor.DARK_RED+"you do not have owner access to: "+ChatColor.GOLD+ChanName);
    return true;    
  }
  public boolean AlreadyInChannel(CommandSender sender) {
    sender.sendMessage(DARK_RED+"[SCC] "+ChatColor.DARK_RED+"You are already in a channel");
    return true;
  }

  public boolean hasMsgLock(Player player) {
    if (lockMsgTo == null) {
      return false;
    }
    
    return lockMsgTo.containsKey(player);
  }
  
  public void setMsgLockTo(Player player, Player toplayer) {
    lockMsgTo = new HashMap<Player, Player>();
    lockMsgTo.put(player, toplayer);
  }

  public Player getMsgLock(Player player) {
    return lockMsgTo.get(player);
  }
  public void clearMsgLock(Player player) {
    lockMsgTo.remove(player);
  }

}
