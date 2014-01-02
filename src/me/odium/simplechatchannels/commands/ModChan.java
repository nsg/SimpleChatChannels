package me.odium.simplechatchannels.commands;

import java.util.List;

import me.odium.simplechatchannels.Loader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class ModChan implements CommandExecutor {   

  public Loader plugin;
  public ModChan(Loader plugin)  {
    this.plugin = plugin;
  }


  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {

    if (args.length < 2) {
      return false;
    }
    
    String chanName = args[0].toLowerCase();

    if (!plugin.getStorageConfig().contains("Channels."+chanName)) {
      plugin.NotExist(sender, chanName);
      return true;
    }

    List<String> chowList = plugin.getStorageConfig().getStringList("Channels."+chanName+".owner");
    if (!chowList.contains(sender) && !sender.hasPermission("scc.admin")) {
      plugin.NotOwner(sender, chanName);
      return true;
    }
    
    if (args[1].equalsIgnoreCase("lock")) {
      plugin.getStorageConfig().set("Channels."+chanName+".Locked", true);
      sender.sendMessage(ChatColor.GREEN + "Channel "+chanName+" locked, only players on the access list can access the channel");
      plugin.saveStorageConfig();
      return true;
    } else if (args[1].equalsIgnoreCase("unlock")) {
      plugin.getStorageConfig().set("Channels."+chanName+".Locked", false);
      sender.sendMessage(ChatColor.GREEN + "Channel "+chanName+" unlocked, everybody can access the channel");
      plugin.saveStorageConfig();
      return true;
    }
    
    return false;
  }

}
