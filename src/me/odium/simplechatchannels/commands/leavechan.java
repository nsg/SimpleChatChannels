package me.odium.simplechatchannels.commands;

import java.util.List;

import me.odium.simplechatchannels.Loader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class leavechan implements CommandExecutor {   

  public Loader plugin;
  public leavechan(Loader plugin)  {
    this.plugin = plugin;
  }


  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }


    if (player == null) {
      sender.sendMessage("This command can only be run by a player");
      return true;
    }
    if (args.length > 2) {
      sender.sendMessage("/leavechan");
      return true;
    } else if (args.length == 0) {
      if (plugin.InChannel.containsKey(player)) {
        String Chan = plugin.ChannelMap.get(player);
        plugin.partChannel(player, Chan);          
        return true;        
      } else {
        sender.sendMessage(plugin.DARK_RED+"[SCC] You are not in a channel.");
        return true;
      }   


    } else if (args.length == 1) {
      String ChanName = args[0].toLowerCase();
      String PlayerName = player.getName().toLowerCase();
      boolean ChanTemp = plugin.getStorageConfig().contains("Channels."+ChanName);
      if(ChanTemp == false) {
        plugin.NotExist(sender, ChanName);
        return true;
      } else {
        List<String> ChList = plugin.getStorageConfig().getStringList("Channels."+ChanName+".list"); // get the player list
        if (!ChList.contains(PlayerName)) {
          sender.sendMessage(plugin.DARK_RED+"[SCC] You are not in " + plugin.GOLD+ ChanName);
          return true;
        } else {
          plugin.partChannel(player, ChanName);          
          return true;

        }
      }
    }
    return true;
  }
}