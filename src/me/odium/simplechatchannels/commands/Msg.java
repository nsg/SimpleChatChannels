package me.odium.simplechatchannels.commands;

import me.odium.simplechatchannels.Loader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class Msg implements CommandExecutor {

  Loader plugin;
  
  public Msg(Loader plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    
    if (args.length == 0) {
      plugin.clearMsgLock((Player)sender);
      if (plugin.InChannel.containsKey(sender)) {
        sender.sendMessage(ChatColor.GOLD + "You have returned to the channel");
      } else {
        sender.sendMessage(ChatColor.GOLD + "You have returned to global chat");
      }
      return true;
    }
    
    Player toplayer = plugin.getServer().getPlayer(args[0]);
    if (toplayer == null) {
      sender.sendMessage("Error, unable to find player '" + args[0] + "'");
      return true;
    }
    
    if (args.length > 1) {
      StringBuffer sb = new StringBuffer();
      for(int i=1; i < args.length; i++) {
        sb.append(args[i] + " ");
      }
      
      String message = sb.toString().trim();
      toplayer.sendMessage(ChatColor.GOLD + "[" + sender.getName() + " -> " + toplayer.getName() + "] " + ChatColor.WHITE + message);
    } else {
      plugin.setMsgLockTo((Player)sender,toplayer);
      sender.sendMessage(ChatColor.GOLD + "All messages will be sent to " + toplayer.getName() + ", type /msg to cancel");
    }
    
    return true;
  }

}
