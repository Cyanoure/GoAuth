package ga.cyanoure.goauth.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ga.cyanoure.goauth.bungee.utils.BungeeMinecraftBridge;
import ga.cyanoure.goauth.main.CommandReceiver;
import ga.cyanoure.goauth.main.GoAuthenticator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin {
	public Configuration config;
	public Language lang;
	public String prefix;
	public GoAuthenticator auth;
	public BungeeMinecraftBridge minecraftBridge = null;
	public ga.cyanoure.goauth.main.Main goAuth = null;

	static Main instance = null;

	@Override
	public void onEnable() {
		if(Main.instance == null) Main.instance = this;
		minecraftBridge = new BungeeMinecraftBridge(this);
		/*CopyConfig("config.yml");
		config = LoadConfig("config.yml");
		auth = new GoAuthenticator(config.getString("mysql.host"),config.getString("mysql.database"),config.getString("mysql.user"),config.getString("mysql.password"));
		auth.SessionLength = config.getInt("session-length");
		auth.HashPrefix = config.getString("password-prefix");
		
		lang = new Language(this,config.getString("language"));
		prefix = config.getString("prefix");
		
		getProxy().getPluginManager().registerListener(this, new Events(this));
		
		getProxy().registerChannel("cyanoure:goauth");
		
		logMSG(lang.GetText("pluginLoaded"));*/

		getProxy().getPluginManager().registerListener(this, new Events(this));
		goAuth = new ga.cyanoure.goauth.main.Main(minecraftBridge);

		List<String> cmds = goAuth.commandReceiver.getFullCommandList();
		for (String cmd : cmds){
			BungeeCommandListener listener = new BungeeCommandListener(this,cmd);
			listener.commandReceiver = goAuth.commandReceiver;
			getProxy().getPluginManager().registerCommand(this,listener);
		}

		//getProxy().getPluginManager().registerCommand(this,new BungeeCommandListener(this,"goauth"));

		getProxy().getScheduler().schedule(this, new Runnable() {
			@Override
			public void run() {
				goAuth.runSyncTasks();
			}
		},0,50, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void onDisable() {
		//auth.Stop();
	}
	
	public void logMSG(String msg) {
		getProxy().getLogger().info(ChatColor.translateAlternateColorCodes('&', prefix+msg));
	}
	
	public Configuration LoadConfig(String file) {
		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void CopyConfig(String file) {
		if(!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		File f = new File(getDataFolder(),file);
		if(!f.exists()) {
			try (InputStream in = getResourceAsStream(file)){
				Files.copy(in, f.toPath());
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
