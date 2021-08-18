package ga.cyanoure.goauth.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand  implements CommandExecutor{
	public Main plugin;
	
	public LoginCommand(Main plugin) {
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
			if(plugin.auth.Registered(p.getUniqueId().toString())) {
				String[] ipa = p.getAddress().getAddress().toString().split("/");
				String ip = ipa[ipa.length-1];
				if(!plugin.auth.LoggedIn(p.getUniqueId().toString(),ip)) {
					if(args.length >= 1) {
						if(plugin.auth.LoginProcess(p.getUniqueId().toString(), args[0], p.getName(), ip)) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("successLogin")));
							plugin.logMSG(plugin.lang.getText("loginConsole").replace("<player>", p.getName()));
							if(!p.hasPermission("goauth.twofactor") || true) {
								plugin.GoLogin(p);
							}else if(plugin.auth.TwoFactorActive(p.getUniqueId().toString()) || plugin.auth.TwoFactorAuthed(p.getUniqueId().toString(),ip)) {
								if(plugin.auth.TwoFactorAuthed(p.getUniqueId().toString(), ip)) {
									plugin.GoLogin(p);
								}else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("googleAuthCode")));
								}
							}else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("googleAuthSetup").replace("<key>", plugin.auth.New2FASecret(p.getUniqueId().toString()))));
							}
						}else {
							p.kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.lang.getText("incorrectPassword")));
						}
					}else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("loginUsage")));
					}
				}else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("alreadyLoggedIn")));
				}
			}else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("registerMessage")));
			}
		}
		return true;
	}
	
}
