package ga.cyanoure.goauth.spigot.utils;

import ga.cyanoure.goauth.spigot.GoAuth;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class SpigotMinecraftBridge implements ga.cyanoure.goauth.main.MinecraftBridge {
    private GoAuth plugin;

    public SpigotMinecraftBridge(){
        plugin = GoAuth.getPlugin(GoAuth.class);
    }

    @Override
    public void SendChat(String uuid, String message) {
        plugin.getServer().getPlayer(UUID.fromString(uuid)).sendMessage(ChatColor.translateAlternateColorCodes('&',message));
    }

    @Override
    public void KickPlayer(String uuid, String reason) {
        plugin.getServer().getPlayer(UUID.fromString(uuid)).kickPlayer(ChatColor.translateAlternateColorCodes('&',reason));
    }

    @Override
    public boolean HasPermission(String uuid, String permission) {
        return plugin.getServer().getPlayer(UUID.fromString(uuid)).hasPermission(permission);
    }

    @Override
    public String GetPlayerName(String uuid) {
        return plugin.getServer().getPlayer(UUID.fromString(uuid)).getName();
    }

    @Override
    public String GetPluginDir() {
        return plugin.getDataFolder().toString();
    }

    @Override
    public void SendConsole(String msg) {
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
    }

    @Override
    public boolean IsOnline(String uuid) {
        return plugin.getServer().getPlayer(UUID.fromString(uuid)) != null;
    }

    @Override
    public boolean IsOnlineName(String name) {
        return plugin.getServer().getPlayerExact(name) != null;
    }

    @Override
    public String GetAddress(String uuid) {
        String[] a = plugin.getServer().getPlayer(UUID.fromString(uuid)).getAddress().getAddress().toString().split("/");
        return a[a.length-1];
    }

    @Override
    public String GetUUID(String name) {
        Player p = plugin.getServer().getPlayerExact(name);
        if(p != null){
            return plugin.getServer().getPlayerExact(name).getUniqueId().toString();
        }else{
            return null;
        }
    }

    @Override
    public void LockPlayer(String uuid) {
        Player p = plugin.getServer().getPlayer(UUID.fromString(uuid));
        for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,1000000,1));
    }

    @Override
    public void UnlockPlayer(String uuid) {
        if(uuid == null) return;
        Player p = plugin.getServer().getPlayer(UUID.fromString(uuid));
        GameMode gamemode = p.getGameMode();
        if(!(gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR)) {
            p.setFlying(false);
            p.setAllowFlight(false);
        }
        for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());
    }

    @Override
    public String GetServerType() {
        return "spigot";
    }
}
