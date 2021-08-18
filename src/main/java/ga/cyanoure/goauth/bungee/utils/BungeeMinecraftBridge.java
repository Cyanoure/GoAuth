package ga.cyanoure.goauth.bungee.utils;

import ga.cyanoure.goauth.bungee.Main;
import ga.cyanoure.goauth.bungee.MessagingHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.logging.Level;

public class BungeeMinecraftBridge implements ga.cyanoure.goauth.main.MinecraftBridge{
    public Main plugin = null;
    public BungeeMinecraftBridge(Main _plugin){
        this.plugin = _plugin;
    }
    public MessagingHandler messaging = new MessagingHandler();

    @Override
    public void SendChat(String uuid, String message) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        if (p != null){
            p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',message)));
        }
    }

    @Override
    public void KickPlayer(String uuid, String reason) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        if (p != null){
            p.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&',reason)));
        }
        /*ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        if (p != null){

        }*/
    }

    @Override
    public boolean HasPermission(String uuid, String permission) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        if (p != null){
            return p.hasPermission(permission);
        }
        return false;
    }

    @Override
    public String GetPlayerName(String uuid) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        if (p != null){
            return p.getName();
        }
        return null;
    }

    @Override
    public String GetPluginDir() {
        return plugin.getDataFolder().toString();
    }

    @Override
    public void SendConsole(String msg) {
        plugin.getProxy().getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',msg));
    }

    @Override
    public boolean IsOnline(String uuid) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        if (p != null){
            return p.isConnected();
        }
        return false;
    }

    @Override
    public boolean IsOnlineName(String name) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(name);
        if (p != null){
            return p.isConnected();
        }
        return false;
    }

    @Override
    public String GetAddress(String uuid) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        if (p != null){
            String[] a = p.getAddress().getAddress().toString().split("/");
            return a[a.length-1];
        }
        return null;
    }

    @Override
    public String GetUUID(String name) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(name);
        if (p != null){
            return p.getUniqueId().toString();
        }
        return null;
    }

    @Override
    public void LockPlayer(String uuid) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        //if(p.getServer() != null) System.out.println("::GOAUTH:: PLAYER LOCK : "+uuid);
        messaging.sendLock(uuid);
    }

    @Override
    public void UnlockPlayer(String uuid) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(uuid));
        //if(p.getServer() != null) System.out.println("::GOAUTH:: PLAYER UNLOCK : "+uuid);
        messaging.sendUnlock(uuid);
    }

    @Override
    public String GetServerType() {
        return "bungeecord";
    }
}
