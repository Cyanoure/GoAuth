package ga.cyanoure.goauth.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import ga.cyanoure.goauth.main.enums.AuthState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class MessagingHandler implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        //System.out.println("----- PLUGIN MESSAGE -----");
        //System.out.println(channel);
        if(!GoAuth.instance.goAuth.bungeeMode()) return;
        if (!channel.equalsIgnoreCase(ga.cyanoure.goauth.bungee.MessagingHandler.channel)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String remoteSecret = in.readUTF();
        String localSecret = GoAuth.instance.goAuth.getSecret();
        if (remoteSecret.equals(localSecret)){
            String uuid = "";
            String command = "";
            try{
                uuid = in.readUTF();
                command = in.readUTF();
            }catch(Exception e){
                e.printStackTrace();
            }
            if(uuid == null || uuid.equals("")) return;
            //String command = in.readUTF();
            //String uuid = player.getUniqueId().toString();
            switch (command){
                case "lock":
                    GoAuth.instance.goAuth.SetPlayerStatus(uuid, AuthState.BUNGEECORD);
                    GoAuth.instance.minecraftBridge.LockPlayer(uuid);
                    //System.out.println("BUNGEE -> LOCK");
                    break;
                case "unlock":
                    GoAuth.instance.goAuth.SetPlayerStatus(uuid,AuthState.LOGGEDIN);
                    GoAuth.instance.minecraftBridge.UnlockPlayer(uuid);
                    //System.out.println("BUNGEE -> UNLOCK");
                    break;
            }
        }
    }
}
