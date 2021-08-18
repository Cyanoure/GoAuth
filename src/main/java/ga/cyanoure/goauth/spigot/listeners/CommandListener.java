package ga.cyanoure.goauth.spigot.listeners;

import ga.cyanoure.goauth.spigot.GoAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {
    private GoAuth plugin;
    public CommandListener(){
        plugin = GoAuth.getPlugin(GoAuth.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player)sender;
            plugin.goAuth.commandReceiver.onCommand(p.getUniqueId().toString(),command.getName(),args);
        }else{
            plugin.goAuth.commandReceiver.onCommand("server",command.getName(),args);
        }
        return true;
    }
}
