package ga.cyanoure.goauth.spigot;

import java.util.HashMap;
import java.util.Map;

import ga.cyanoure.goauth.main.GoConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import ga.cyanoure.goauth.main.GoAuthenticator;

public class Main extends JavaPlugin {
	public Language lang;
	public FileConfiguration config;
	public String prefix;
	public GoAuthenticator auth;
	public boolean bungee = false;
	public Map<String,Boolean> CanFly = new HashMap<String,Boolean>();
	public Map<String,Long> ConnectedPlayers = new HashMap<String,Long>();
	
	public void RemoveConnectedPlayer(String uuid) {
		while(ConnectedPlayers.containsKey(uuid)) {
			ConnectedPlayers.remove(uuid);
		}
	}
	
	public void AddConnectedPlayer(String uuid) {
		RemoveConnectedPlayer(uuid);
		ConnectedPlayers.put(uuid, System.currentTimeMillis()/1000);
	}
	
	public void logMSG(String msg) {
		getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+msg));
	}
	
	@Override
	public void onEnable() {
		loadConfig();
		auth = new GoAuthenticator(config.getString("mysql.host"),config.getString("mysql.database"),config.getString("mysql.user"),config.getString("mysql.password"));
		auth.SessionLength = config.getInt("session-length");
		auth.HashPrefix = config.getString("password-prefix");
		
		if(IsBungeecord()) {
			if(config.getBoolean("bungeecord")) {
				bungee = true;
				logMSG(lang.getText("bungeeMode"));
			}else {
				logMSG(lang.getText("bungeeDetected"));
			}
		}
		
		getServer().getPluginManager().registerEvents(new Events(this), this);
		this.getCommand("login").setExecutor(new LoginCommand(this));
		this.getCommand("register").setExecutor(new RegisterCommand(this));
		this.getCommand("2fa").setExecutor(new TwoFactorCommand(this));
		this.getCommand("goauth").setExecutor(new MainCommand(this));
		this.getCommand("changepassword").setExecutor(new ChangePassCommand(this));
		
		/*BukkitTask task = */new Scheduler(this).runTaskTimer(this, 20, 20);
		
		logMSG(lang.getText("pluginLoaded"));

		//LogFilter logFilter = new LogFilter();
		((Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
		
		
	}
	
	@Override
	public void onDisable() {
		auth.Stop();
	}
	
	public void loadConfig() {
		SaveResources();
		saveDefaultConfig();
		config = getConfig();
		lang = new Language(getDataFolder(),config.getString("language"));
		prefix = config.getString("prefix");
	}
	
	private boolean IsBungeecord() {
		return getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean( "bungeecord" );
	}
	
	private void SaveResources() {
		this.saveResource("lang/hu.yml", false);
	}
	
	public void GoLogin(Player p) {
		/*if(CanFly.containsKey(p.getUniqueId().toString())) {
			if(!CanFly.get(p.getUniqueId().toString())) {
				p.setFlying(false);
				p.setAllowFlight(false);
			}
		}*/
		GameMode gamemode = p.getGameMode();
		if(!(gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR)) {
			p.setFlying(false);
			p.setAllowFlight(false);
		}
		for (PotionEffect effect : p.getActivePotionEffects())
			p.removePotionEffect(effect.getType());
	}
	
	public void ClearPlayer(Player p) {
		if(CanFly.containsKey(p.getUniqueId().toString())) {
			CanFly.remove(p.getUniqueId().toString());
		}
	}
}
