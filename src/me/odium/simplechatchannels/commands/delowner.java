package me.odium.simplechatchannels.commands;

import java.util.List;

import me.odium.simplechatchannels.Loader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class delowner implements CommandExecutor {   

  public Loader plugin;
  public delowner(Loader plugin)  {
    this.plugin = plugin;
  }


  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if(cmd.getName().equalsIgnoreCase("delowner")){
      if(args.length != 2){
        sender.sendMessage("/delowner <ChannelName> <PlayerName>");
        return true;
      }
      String ChanName = args[0].toLowerCase();
      String PlayerName = args[1].toLowerCase();
      List<String> ChowList = plugin.getStorageConfig().getStringList("Channels."+ChanName+".owner");
      if (ChowList.contains(sender.getName()) || player.hasPermission("scc.admin")) {
        String AddPlayName = plugin.myGetPlayerName(args[1]);
        Player target = plugin.getServer().getPlayer(args[1]);
        boolean ChanTemp = plugin.getStorageConfig().contains("Channels."+ChanName);
        if(ChanTemp == false) {
          plugin.NotExist(sender, ChanName);
          return true;
        } else {        
          if (!ChowList.contains(PlayerName)) {
            sender.sendMessage(plugin.DARK_RED+"[SCC] "+plugin.GOLD + AddPlayName + plugin.DARK_RED + " does not have owner access to " + plugin.GOLD + ChanName);
            return true;
          } else {
            ChowList.remove(PlayerName);  // remove the player from the list
            plugin.getStorageConfig().set("Channels."+ChanName+".owner", ChowList); // set the new list          
            sender.sendMessage(plugin.DARK_GREEN+"[SCC] "+plugin.GOLD+ AddPlayName + plugin.DARK_GREEN + " removed From " + plugin.RED + ChanName + "'s" + plugin.DARK_GREEN + " owner list");
            if (target != null) {
              target.sendMessage(plugin.DARK_GREEN+"[SCC] "+"You have been removed from " + plugin.GOLD + ChanName + "'s" + plugin.DARK_GREEN + " owner list");              
            }
            plugin.saveStorageConfig();
            return true;
          }
        }
      } else {
        plugin.NotOwner(sender, ChanName);
        return true;
      }
    }

    return true;   
  }
}