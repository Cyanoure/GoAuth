package ga.cyanoure.goauth.spigot;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class Scheduler extends BukkitRunnable{
	private final Main plugin;
	public Scheduler(Main plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		long time = System.currentTimeMillis()/1000;
		
		if(!plugin.bungee) {
			/*for(int i = 0; i < plugin.auth.LoggedInPlayers.size(); i++) {
				String uuid = plugin.auth.LoggedInPlayers.get(i);
				plugin.auth.UpdateSession(uuid);
			}*/ // Mivel Sync-ben van, laggoltatja a szervert másodpercenként.
			
			for(Map.Entry<String,Long> entry : plugin.ConnectedPlayers.entrySet()) {
				long ConnectedTime = entry.getValue();
				Player p = plugin.getServer().getPlayer(UUID.fromString(entry.getKey()));
				if(p != null) {
					if(time % 10 == 0) {
						if(plugin.auth.CacheRegistered(p.getUniqueId().toString())) {
							if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("loginMessage")));
							}
						}else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("registerMessage")));
						}
					}
					if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString()) && plugin.config.getInt("login-timeout") > 0 && time - ConnectedTime >= plugin.config.getInt("login-timeout")) {
						p.kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.lang.getText("loginTimeout")));
					}
				}else {
					plugin.RemoveConnectedPlayer(entry.getKey());
				}
			}
		}
	}
}
