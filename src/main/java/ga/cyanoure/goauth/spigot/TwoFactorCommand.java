package ga.cyanoure.goauth.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TwoFactorCommand implements CommandExecutor{
public Main plugin;
	
	public TwoFactorCommand(Main plugin) {
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
			String[] ipa = p.getAddress().getAddress().toString().split("/");
			String ip = ipa[ipa.length-1];
			if(plugin.auth.LoggedIn(p.getUniqueId().toString(),ip)) {
				if(args.length >= 1) {
					//try {
						//int pwd = Integer.parseInt(args[0]);
						//String secret = "B2SANVQ3UT6MPGUN";
						/*String secret = plugin.auth.GetPlayer(p.getUniqueId().toString()).TwoFactorKey;
						boolean ok = secret != null && plugin.auth.gAuth.authorize(secret, pwd);*/
						
						if(plugin.auth.Authorize2FA(p.getUniqueId().toString(), args[0],ip)) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("2faAccepted")));
							plugin.GoLogin(p);
							return true;
						}
					/*}catch(Exception e) {
						
					}*/
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("2faDenied")));
				}else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("twofactorUsage")));
				}
			}else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("pleaseLogin")));
			}
		}
		return true;
	}
}
