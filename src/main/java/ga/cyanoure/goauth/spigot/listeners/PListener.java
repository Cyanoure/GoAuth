package ga.cyanoure.goauth.spigot.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import ga.cyanoure.goauth.bungee.MessagingHandler;
import ga.cyanoure.goauth.spigot.GoAuth;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitScheduler;

public class PListener implements Listener {
    GoAuth plugin;
    public PListener(){
        plugin = GoAuth.getPlugin(GoAuth.class);
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent e) {
        if (!plugin.goAuth.canPlay(e.getPlayer().getUniqueId().toString())){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.goAuth.canPlayMsg(e.getPlayer().getUniqueId().toString())){
            e.setCancelled(true);
        }
    }

    private boolean cc(String c1, String c2){
        return plugin.goAuth.commandReceiver.CommandEquals(c1,c2);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String cmd = StringUtils.strip(e.getMessage().split(" ")[0],"/");
        boolean commandOK = (cc("login",cmd) || cc("register",cmd) || cc("2fa",cmd)) && !plugin.goAuth.bungeeMode();
        if (!commandOK && !plugin.goAuth.canPlayMsg(e.getPlayer().getUniqueId().toString())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.goAuth.JoinPlayer(p.getUniqueId().toString());
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("getauth");
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(plugin.getServer().getPlayer(p.getUniqueId()) != null) p.sendPluginMessage(plugin, MessagingHandler.channel,out.toByteArray());
            }
        },10);
        plugin.goAuth.UsernameCheck(p.getUniqueId().toString());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        plugin.goAuth.LeavePlayer(e.getPlayer().getUniqueId().toString());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!plugin.goAuth.canPlay(e.getWhoClicked().getUniqueId().toString())){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (!plugin.goAuth.canPlayMsg(e.getPlayer().getUniqueId().toString())){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent e) {
        if (!plugin.goAuth.canPlayMsg(e.getPlayer().getUniqueId().toString())){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onScroll(PlayerItemHeldEvent e) {
        if (!plugin.goAuth.canPlay(e.getPlayer().getUniqueId().toString())){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!plugin.goAuth.canPlay(p.getUniqueId().toString())){
            e.setCancelled(true);
        }
    }
}
