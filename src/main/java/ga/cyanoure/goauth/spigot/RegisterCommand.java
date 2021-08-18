package ga.cyanoure.goauth.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ga.cyanoure.goauth.main.GoAuthenticator.PlayerData;

public class RegisterCommand implements CommandExecutor{
public Main plugin;
	
	public RegisterCommand(Main plugin) {
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
			if(!plugin.auth.Registered(p.getUniqueId().toString())) {
				if(args.length >= 2) {
					if(args[0].equals(args[1])) {
						String[] ipa = p.getAddress().getAddress().toString().split("/");
						String ip = ipa[ipa.length-1];
						String password = args[0];
						PlayerData data = new PlayerData();
						data.LastServer = "default";
						data.RegIP = ip;
						data.LastIP = ip;
						data.Username = p.getName();
						data.UUID = p.getUniqueId().toString();
						data.PasswordHash = plugin.auth.HashPassword(password);
						if(plugin.auth.Register(data) && plugin.auth.IsRegistered(data.UUID)) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("successRegister")));
							plugin.logMSG(plugin.lang.getText("registerConsole").replace("<player>", p.getName()));
							plugin.GoLogin(p);
						}else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("error")));
						}
					}else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("passwordsNMatch")));
					}
				}else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("registerUsage")));
				}
			}else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("alreadyRegistered")));
			}
		}
		return true;
	}
}
