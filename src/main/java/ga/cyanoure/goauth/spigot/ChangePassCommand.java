package ga.cyanoure.goauth.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePassCommand  implements CommandExecutor{
	public Main plugin;
	
	public ChangePassCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!plugin.auth.MySQLConnected()) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("mysqlConnectionError")));
			return true;
		}
		if(sender instanceof Player) {
			Player p = (Player)sender;
			if(args.length >= 3) {
				if(args[1].equals(args[2])) {
					if(plugin.auth.CanLogin(p.getUniqueId().toString(), args[0])) {
						if(plugin.auth.ChangePassword(p.getUniqueId().toString(), args[1])) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("chpassSuccess")));
							plugin.logMSG(plugin.lang.getText("chpassConsole").replace("<player>", p.getName()));
						}else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("error")));
						}
					}else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("incorrectPassword")));
					}
				}else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("passwordsNMatch")));
				}
			}else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("chpassUsage")));
			}
		}
		return true;
	}
	
}
