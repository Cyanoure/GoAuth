package ga.cyanoure.goauth.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;


public class Events implements Listener {
	public Main plugin;
	
	public Events(Main _plugin) {
		plugin = _plugin;
	}

	@EventHandler
	public void onJoin(PostLoginEvent event){
		ProxiedPlayer p = event.getPlayer();
		plugin.goAuth.JoinPlayer(p.getUniqueId().toString());
		plugin.goAuth.UsernameCheck(p.getUniqueId().toString());
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event){
		ProxiedPlayer p = event.getPlayer();
		plugin.goAuth.LeavePlayer(p.getUniqueId().toString());
	}

	@EventHandler
	public void onChat(ChatEvent event){
		ProxiedPlayer p = (ProxiedPlayer) event.getSender();
		String uuid = p.getUniqueId().toString();
		if (!plugin.goAuth.canPlayMsg(uuid)){
			Boolean allowed = false;
			String msg = event.getMessage();
			if(msg.startsWith("/")) {
				String cmd = msg.split(" ")[0].substring(1);
				allowed = plugin.goAuth.commandReceiver.CanUseCommand(uuid,cmd);
			}
			if(!allowed) event.setCancelled(true);
		}
	}

	@EventHandler
	public void onServerJoin(PluginMessageEvent event){
		if(event.getTag().equalsIgnoreCase(MessagingHandler.channel)){
			ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
			String msg = in.readUTF();
			if (msg.equalsIgnoreCase("getauth")){
				if (event.getReceiver() instanceof ProxiedPlayer){
					ProxiedPlayer p = (ProxiedPlayer) event.getReceiver();
					String uuid = p.getUniqueId().toString();
					if (plugin.goAuth.canPlay(uuid)){
						plugin.minecraftBridge.UnlockPlayer(uuid);
					}else{
						plugin.minecraftBridge.LockPlayer(uuid);
					}
				}
			}
		}
	}
}
