package ga.cyanoure.goauth.spigot;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Events implements Listener{
	public Main plugin;
	public Events(Main plugin) {
		this.plugin = plugin;
	}
	
	private void SendAuthMSG(Player p) {
		String uuid = p.getUniqueId().toString();
		if(plugin.auth.CacheRegistered(uuid)) {
			if(!plugin.auth.CacheLoggedIn(uuid)) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("loginMessage")));
			}else if(plugin.auth.TwoFactorActive(uuid)) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("googleAuthCode")));
			}else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("googleAuthSetup").replace("<key>", plugin.auth.New2FASecret(uuid))));
			}
		}else {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("registerMessage")));
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMove(PlayerMoveEvent e) {
		Player p = (Player)e.getPlayer();
		if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())/* || !plugin.auth.TwoFactorAuthedCache(p.getUniqueId().toString())*/) {
			p.setAllowFlight(true);
			p.setFlying(true);
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = (Player)e.getPlayer();
		if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())/* || !plugin.auth.TwoFactorAuthedCache(p.getUniqueId().toString())*/) {
			SendAuthMSG(p);
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onCommand(PlayerCommandPreprocessEvent e) {
		Player p = (Player)e.getPlayer();
		if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())/* || !plugin.auth.TwoFactorAuthedCache(p.getUniqueId().toString())*/) {
			if(!(e.getMessage().split(" ")[0].equalsIgnoreCase("/login") || e.getMessage().split(" ")[0].equalsIgnoreCase("/l") || e.getMessage().split(" ")[0].equalsIgnoreCase("/register") || e.getMessage().split(" ")[0].equalsIgnoreCase("/reg") || e.getMessage().split(" ")[0].equalsIgnoreCase("/r") || e.getMessage().split(" ")[0].equalsIgnoreCase("/twofa") || e.getMessage().split(" ")[0].equalsIgnoreCase("/twofactor") || e.getMessage().split(" ")[0].equalsIgnoreCase("/2fa"))) {
				SendAuthMSG(p);
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = (Player)e.getPlayer();
		plugin.AddConnectedPlayer(p.getUniqueId().toString());
		String[] ipa = p.getAddress().getAddress().toString().split("/");
		String ip = ipa[ipa.length-1];
		boolean cL = plugin.auth.CacheLoggedIn(p.getUniqueId().toString());
		if(!plugin.bungee) {
			if(!plugin.auth.LoggedIn(p.getUniqueId().toString(),ip)/* || !plugin.auth.TwoFactorAuthed(p.getUniqueId().toString(), ip)*/) {
				SendAuthMSG(p);
				plugin.CanFly.put(p.getUniqueId().toString(), p.getAllowFlight());
				for (PotionEffect effect : p.getActivePotionEffects())
					p.removePotionEffect(effect.getType());
				p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,1000000,1));
			}else if(!cL) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix+plugin.lang.getText("autoLogin")));
				plugin.logMSG(plugin.lang.getText("autoLoginConsole").replace("<player>", p.getName()));
				plugin.GoLogin(p);
			}
		}
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = (Player)e.getPlayer();
		plugin.auth.ClearPlayer(p.getUniqueId().toString());
		plugin.ClearPlayer(p);
		
		plugin.RemoveConnectedPlayer(p.getUniqueId().toString());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player)e.getWhoClicked();
		if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())/* || !plugin.auth.TwoFactorAuthedCache(p.getUniqueId().toString())*/) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent e) {
		Player p = (Player)e.getPlayer();
		if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())/* || !plugin.auth.TwoFactorAuthedCache(p.getUniqueId().toString())*/) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDrop(PlayerDropItemEvent e) {
		Player p = (Player)e.getPlayer();
		if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onScroll(PlayerItemHeldEvent e) {
		Player p = (Player)e.getPlayer();
		if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player)e.getEntity();
			if(!plugin.auth.CacheLoggedIn(p.getUniqueId().toString())) {
				e.setCancelled(true);
			}
		}
	}
}
