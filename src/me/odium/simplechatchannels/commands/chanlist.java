package me.odium.simplechatchannels.commands;

import java.util.ArrayList;
import java.util.List;

import me.odium.simplechatchannels.Loader;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class chanlist implements CommandExecutor {   

  public Loader plugin;
  public chanlist(Loader plugin)  {
    this.plugin = plugin;
  }


  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {
    if(args.length == 0) {
      return listAllChannels(sender);
    } else if(args.length == 1) {
      return listChannel(sender, args);
    } else if(args.length == 2 && args[0].contains("hasplayer")) {
      return playerInChannels(sender, args);
    }

    return false;
  }

  /**
   * List all channels
   * @param sender
   */
  private boolean listAllChannels(CommandSender sender) {
    List<String> ChannelsList = plugin.getStorageConfig().getStringList("Channels");
    sender.sendMessage(ChatColor.GOLD + "[ "+ChatColor.WHITE+"Channel List"+ChatColor.GOLD+" ]");
    ArrayList<String> chanList = new ArrayList<String>();

    for (String chan : ChannelsList) {
      int UserCount = plugin.getStorageConfig().getStringList(chan+".list").size();
      String topic = plugin.getStorageConfig().getString(chan+".topic");

      if (topic == null) {
        topic = " (No Topic Set) ";
      } else if (topic.length() > 42) {
        topic = topic.substring(0, 42)+"...";
      }

      String lock = "";
      if (plugin.getStorageConfig().getBoolean(chan+".Locked")) {
        lock = ChatColor.RED + "(L)" + ChatColor.WHITE;
      }

      chanList.add(plugin.WHITE + chan + lock + ChatColor.GOLD + "["+ChatColor.WHITE + UserCount + ChatColor.GOLD + "]" + " "+ChatColor.WHITE+topic);
    }

    sender.sendMessage("All channels:");
    for (String c : chanList) {
      sender.sendMessage(ChatColor.GOLD + " - " + c);
    }

    return true;
  }

  /**
   * List detailed channel info
   * @param sender
   * @param args
   */
  private boolean listChannel(CommandSender sender, String[] args) {
    String ChanName = args[0].toLowerCase();

    if (!plugin.getStorageConfig().getStringList("Channels").contains(ChanName)) {
      plugin.NotExist(sender, ChanName);
      return true;
    }

    ArrayList<String> ownerList = new ArrayList<String>();
    ArrayList<String> inACL = new ArrayList<String>();
    ArrayList<String> listPlayer = new ArrayList<String>();
    ArrayList<String> listPlayerRAW = new ArrayList<String>();

    // Owners of the channel
    List<String> OwList = plugin.getStorageConfig().getStringList(ChanName+".owner");
    for(int i = 0; i < OwList.size(); ++i) {
      String ChOwners = OwList.get(i);
      ownerList.add(ChOwners);
    }

    // List ACL
    List<String> AccList = plugin.getStorageConfig().getStringList(ChanName+".AccList"); // create/get the owner list
    for(int i = 0; i < AccList.size(); ++i) {
      String ChAccess = AccList.get(i);
      inACL.add(ChAccess);
    }

    // Players in channel
    java.util.List<String> ChList = plugin.getStorageConfig().getStringList(ChanName+".list");
    sender.sendMessage(ChatColor.GOLD + "Channel " + ChanName);

    // List users in channel
    for (String p : ChList) {
      if (ownerList.contains(p)) {
        listPlayer.add(ChatColor.WHITE + p + ChatColor.GOLD + "(owner)" + ChatColor.WHITE);
      } else if (inACL.contains(p)) {
        listPlayer.add(ChatColor.WHITE + p + ChatColor.GREEN + "(acl)" + ChatColor.WHITE);
      } else {
        listPlayer.add(ChatColor.WHITE + p);
      }
      listPlayerRAW.add(p);
    }

    sender.sendMessage(ChatColor.GOLD+" In channel: "+plugin.WHITE + listPlayer.toString());
    listPlayer.clear();

    // List owners not listed above
    for (String o : ownerList) {
      if (!listPlayerRAW.contains(o)) {
        listPlayer.add(ChatColor.GRAY + o + "(owner)" + ChatColor.WHITE);
        listPlayerRAW.add(o);
      }
    }

    // List users with ACL, not listed above
    for (String a : inACL) {
      if (!listPlayerRAW.contains(a)) {
        listPlayer.add(ChatColor.GRAY + a + "(in acl)" + ChatColor.WHITE);
        listPlayerRAW.add(a);
      }
    }

    sender.sendMessage(ChatColor.GRAY + " Not here: "+plugin.WHITE + listPlayer.toString());

    return true;
  }

  /**
   * List what channels a player is part of
   * @param sender
   * @param args
   * @return
   */
  private boolean playerInChannels(CommandSender sender, String[] args) {
    String playerCheck = plugin.myGetPlayerName(args[1]);
    Boolean playerFound = false;
    ArrayList<String> chanList = new ArrayList<String>();

    List<String> ChannelsList = plugin.getStorageConfig().getStringList("Channels");
    for (String Chan : ChannelsList) {
      List<String> ChanList = plugin.getStorageConfig().getStringList(Chan+".list");
      for(String p : ChanList) {
        if (playerCheck.equals(p)) {
          chanList.add("#" + Chan);
          playerFound = true;
        }
      }
    }

    if (playerFound) {
      sender.sendMessage(plugin.DARK_GREEN+ "Player "+ ChatColor.GOLD +playerCheck+plugin.DARK_GREEN+" is in "+ChatColor.GOLD + chanList.toString());
    } else {
      sender.sendMessage(plugin.DARK_GREEN+ "Player "+ ChatColor.GOLD +playerCheck+plugin.DARK_GREEN+" is not in a channel");
    }

    return true;
  }

}