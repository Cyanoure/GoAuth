package ga.cyanoure.goauth.spigot;

import ga.cyanoure.goauth.main.Main;
import ga.cyanoure.goauth.spigot.listeners.CommandListener;
import ga.cyanoure.goauth.spigot.listeners.PListener;
import ga.cyanoure.goauth.spigot.utils.LogFilter;
import ga.cyanoure.goauth.spigot.utils.SpigotMinecraftBridge;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class GoAuth extends JavaPlugin {
    public Main goAuth;
    public SpigotMinecraftBridge minecraftBridge;
    public static GoAuth instance;
    @Override
    public void onEnable() {
        instance = this;
        minecraftBridge = new SpigotMinecraftBridge();
        goAuth = new Main(minecraftBridge);
        CommandListener commandListener = new CommandListener();
        this.getCommand("login").setExecutor(commandListener);
        this.getCommand("register").setExecutor(commandListener);
        this.getCommand("2fa").setExecutor(commandListener);
        this.getCommand("goauth").setExecutor(commandListener);
        this.getCommand("changepassword").setExecutor(commandListener);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this,ga.cyanoure.goauth.bungee.MessagingHandler.channel);

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                goAuth.runSyncTasks();
            }
        },0,1);

        new PListener();
        new LogFilter();

        if (detectBungeeCord()){
            if (goAuth.bungeeMode()){
                minecraftBridge.SendConsole(goAuth.getPrefix()+goAuth.lang.get("bungeeMode"));
            }else{
                minecraftBridge.SendConsole(goAuth.getPrefix()+goAuth.lang.get("bungeeDetected"));
            }
            getServer().getMessenger().registerIncomingPluginChannel(this, ga.cyanoure.goauth.bungee.MessagingHandler.channel,new MessagingHandler());
        }
    }

    private boolean detectBungeeCord() {
        try {
            Boolean enabled = Class.forName("org.spigotmc.SpigotConfig").getDeclaredField("bungee").getBoolean(null);
            return enabled;
        } catch (ClassNotFoundException notFoundEx) {
            //ignore server has no bungee support
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
