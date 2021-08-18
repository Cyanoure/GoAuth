package ga.cyanoure.goauth.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor{
public Main plugin;
	
	public MainCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		/*if(args.length == 1) {
			try {
				int pwd = Integer.parseInt(args[0]);
				String secret = "B2SANVQ3UT6MPGUN";
				boolean ok = plugin.auth.gAuth.authorize(secret, pwd);
				if(ok) {
					sender.sendMessage("Kód elfogadva.");
				}else {
					sender.sendMessage("Kód elutasítva.");
				}
				return true;
			}catch(Exception e) {
				
			}
		}*/
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.lang.getText("pluginHelp")));
		return true;
	}
}
