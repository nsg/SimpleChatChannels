package me.odium.simplechatchannels.commands;

import me.odium.simplechatchannels.Loader;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Global implements CommandExecutor {

  Loader plugin;
  
  public Global(Loader plugin) {
    this.plugin = plugin;
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    
    if (!(sender instanceof Player)) {
      return false;
    }
    
    // Return player from list, to return to global
    plugin.InChannel.remove(sender);
    
    sender.sendMessage(ChatColor.DARK_GREEN + "[SCC] global is the active channel");
    
    return true;
  }
  
}
