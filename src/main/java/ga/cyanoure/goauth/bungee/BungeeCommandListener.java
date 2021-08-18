package ga.cyanoure.goauth.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.Arrays;

public class BungeeCommandListener extends Command {
    public ga.cyanoure.goauth.main.CommandReceiver commandReceiver = null;
    public String command = "";
    public Main plugin = null;
    public BungeeCommandListener(Main _plugin,String _command){
        super(_command,null);
        this.command = _command;
        this.plugin = _plugin;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(sender.getName());
        if(commandReceiver == null || p == null) {
            commandReceiver.onCommand("server", command, args);
        }else {
            commandReceiver.onCommand(p.getUniqueId().toString(), command, args);
        }
    }
}
