package me.odium.simplechatchannels.commands;

import java.util.List;

import me.odium.simplechatchannels.Loader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class addchan implements CommandExecutor {   

  public Loader plugin;
  public addchan(Loader plugin)  {
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

    if (args.length == 0) {
      sender.sendMessage("/addchan <channelname> - Create a channel");
      sender.sendMessage("/addchan lock <channelname> - Create a locked channel");
      return true;
    }

    String ChanName;
    String PlayerName;
    Boolean lockedChannel = false;
    if (args.length == 1) {
      ChanName = args[0].toLowerCase();
      PlayerName = player.getName().toLowerCase();
    } else if (args.length == 2 && args[0].equalsIgnoreCase("lock")) {
      ChanName = args[1].toLowerCase();
      PlayerName = player.getName().toLowerCase();
      lockedChannel = true;
    } else {
      return false;
    }

    // CHECK IF CHANNEL EXISTS
    if (plugin.getStorageConfig().contains(ChanName)) {
      sender.sendMessage(plugin.DARK_RED+"[SCC] "+plugin.GOLD + ChanName + plugin.DARK_RED+ " already exists");
      return true;
    }

    plugin.getStorageConfig().createSection(ChanName); // create the 'channel'
    List<String> ChList = plugin.getStorageConfig().getStringList(ChanName+".list"); // create/get the player list
    List<String> OwList = plugin.getStorageConfig().getStringList(ChanName+".owner"); // create/get the owner list
    List<String> AccList = plugin.getStorageConfig().getStringList(ChanName+".AccList"); // create/get the owner list
    List<String> ChannelsList = plugin.getStorageConfig().getStringList("Channels"); // create/get the owner list
    ChList.add(PlayerName);  // add the player to the list
    OwList.add(PlayerName);  // add the player to the owner list
    AccList.add(PlayerName);  // add the player to the access list
    ChannelsList.add(ChanName);
    plugin.getStorageConfig().set(ChanName+".list", ChList); // set the new list
    plugin.getStorageConfig().set(ChanName+".owner", OwList); // set the new list
    plugin.getStorageConfig().set(ChanName+".AccList", AccList); // set the new list
    plugin.getStorageConfig().set(ChanName+".Locked", lockedChannel); // set the new list
    plugin.getStorageConfig().set("Channels", ChannelsList); // set the new list
    plugin.saveStorageConfig();

    if (lockedChannel) {
      sender.sendMessage(plugin.DARK_GREEN+"[SCC] "+"Locked Channel " + plugin.GOLD + ChanName + plugin.DARK_GREEN + " Created");
    } else {
      sender.sendMessage(plugin.DARK_GREEN+"[SCC] "+ plugin.GOLD +  ChanName + plugin.DARK_GREEN + " Created");
    }

    plugin.setChannel(player, ChanName);
    
    // Save the state so we can re-join the user to his channels when the user joins
    plugin.setPersistentPlayerChannels(player.getName(), ChanName);

    return true;
  }
}