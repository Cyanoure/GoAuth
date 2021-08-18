package ga.cyanoure.goauth.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class MessagingHandler {
    private ProxyServer proxy;
    public static String channel = "cyanoure:goauth";
    public MessagingHandler(){
        proxy = Main.instance.getProxy();
        proxy.registerChannel(MessagingHandler.channel);
    }

    public void sendMessage(String uuid, String... texts){
        if(uuid == null) return;
        //System.out.println("MESSAGE ("+MessagingHandler.channel+") ::::: "+uuid);
        ProxiedPlayer p = proxy.getPlayer(UUID.fromString(uuid));
        if (p != null && p.getServer() != null){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Main.instance.goAuth.getSecret());
            out.writeUTF(uuid);
            for (String text : texts){
                out.writeUTF(text);
            }
            p.getServer().getInfo().sendData(MessagingHandler.channel,out.toByteArray());
        }else if(p != null){
            //System.out.println("GoAuth: SERVER IS NULL");
        }
    }

    public void sendLock(String uuid){
        sendMessage(uuid,"lock");
    }

    public void sendUnlock(String uuid){
        sendMessage(uuid,"unlock");
    }
}
