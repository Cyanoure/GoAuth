package ga.cyanoure.goauth.main;

import ga.cyanoure.goauth.main.enums.AuthState;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommandReceiver {
    public Main main;
    public CommandReceiver(Main _main){
        main = _main;
    }

    private void log(String message){
        main.RunSync(new Runnable() {
            @Override
            public void run() {
                main.minecraftBridge.SendConsole(message);
            }
        });
    }

    private void msg(String uuid, String msg){
        main.RunSync(new Runnable() {
            @Override
            public void run() {
                if (isPlayer(uuid)) {
                    main.minecraftBridge.SendChat(uuid, msg);
                }else{
                    main.minecraftBridge.SendConsole(msg);
                }
            }
        });
    }

    private void kick(String uuid, String msg){
        main.RunSync(new Runnable() {
            @Override
            public void run() {
                main.minecraftBridge.KickPlayer(uuid,msg);
            }
        });
    }

    public Map<String,String[]> commandAliases = new HashMap<String,String[]>(){{
        put("login",new String[]{"l"});
        put("register",new String[]{"reg"});
        put("2fa",new String[]{"twofa","twofactor"});
        put("goauth",new String[]{"gauth","auth"});
        put("changepassword",new String[]{"changepass","chpass","chpwd","chpasswd","chpassword","chpw"});
    }};

    public List<String> getFullCommandList(){
        List<String> cmds = new ArrayList<String>();
        for (Map.Entry<String,String[]> entry : commandAliases.entrySet()){
            String cmd = entry.getKey();
            cmds.add(cmd);
            String[] aliases = entry.getValue();
            for (String alias : aliases){
                cmds.add(alias);
            }
        }
        return cmds;
    }

    public boolean CommandEquals(String command1,String command2){
        command1 = command1.toLowerCase();
        command2 = command2.toLowerCase();
        if(command1.equals(command2)) return true;
        if(commandAliases.containsKey(command1)){
            return Arrays.asList(commandAliases.get(command1)).contains(command2.toLowerCase());
        }else if (commandAliases.containsKey(command2)){
            return Arrays.asList(commandAliases.get(command2)).contains(command1.toLowerCase());
        }else{
            return false;
        }
    }

    public boolean CanUseCommand(String uuid, String command){
        if (main.canPlay(uuid)){
            return true;
        }else if (CommandEquals("login",command) || CommandEquals("register",command) || CommandEquals("2fa",command)){
            return true;
        }
        return false;
    }

    public String getHelp(String uuid){
        String help = main.lang.get("pluginHelp");
        if (uuid != null && (uuid.equalsIgnoreCase("server") || main.minecraftBridge.HasPermission(uuid,"goauth.admin"))){
            help+="\n"+main.lang.get("pluginHelpAdmin");
        }
        return help;
    }

    public Boolean onlyPlayer(String uuid){
        Boolean isPlayer = isPlayer(uuid);
        if (!isPlayer){
            msg(uuid,main.lang.get("onlyPlayer"));
        }
        return isPlayer;
    }

    public Boolean isPlayer(String uuid){
        return uuid != null && !uuid.equalsIgnoreCase("server");
    }

    public void lmsg(String uuid, String textID, String... texts){
        msg(uuid,(String)main.config.getValue("prefix")+main.lang.get(textID,texts));
    }

    public void onCommand(String uuid, String cmd, String[] args){
        if(main.bungeeMode()){
            lmsg(uuid,"commandsDisabledBungee");
            return;
        };
        if(CommandEquals("login",cmd) && onlyPlayer(uuid)){
            if (args.length >= 1){
                if(main.GetPlayerStatus(uuid) == AuthState.LOGGEDIN){
                    msg(uuid,(String) main.config.getValue("prefix")+(String) main.lang.get("alreadyLoggedIn"));
                    return;
                }
                if(main.GetPlayerStatus(uuid) == AuthState.LOGIN) {
                    String pass = args[0];
                    main.RunAsync(new Runnable() {
                        @Override
                        public void run() {
                            if(main.authenticator.MySQLConnected()) {
                                if (main.authenticator.LoginProcess(uuid, pass, main.minecraftBridge.GetPlayerName(uuid), main.minecraftBridge.GetAddress(uuid),(String)main.config.getValue("server"))) {
                                    main.CheckStatusSync(uuid, main.minecraftBridge.GetAddress(uuid));
                                    msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("successLogin"));
                                    log(main.config.getValue("prefix") + main.lang.get("loginConsole", main.minecraftBridge.GetPlayerName(uuid)));
                                    main.canPlayMsg(uuid);
                                } else {
                                    //main.minecraftBridge.SendChat(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("incorrectPassword"));
                                    kick(uuid, main.lang.get("incorrectPassword"));
                                }
                            }else{
                                msg(uuid,(String) main.config.getValue("prefix") + (String) main.lang.get("mysqlConnectionError"));
                            }
                        }
                    });
                }else{
                    main.canPlayMsg(uuid);
                }
            }else{
                msg(uuid,(String) main.config.getValue("prefix")+(String) main.lang.get("loginUsage"));
            }
        }else if(CommandEquals("register",cmd) && onlyPlayer(uuid)){
            if (args.length >= 2){
                if(main.authenticator.CacheRegistered(uuid)){
                    msg(uuid,(String) main.config.getValue("prefix")+(String) main.lang.get("alreadyRegistered"));
                    return;
                }
                if(main.GetPlayerStatus(uuid) == AuthState.UNREGISTERED){
                    String pass1 = args[0];
                    String pass2 = args[1];
                    if(pass1.equals(pass2)){
                        main.RunAsync(new Runnable() {
                            @Override
                            public void run() {
                                if(main.authenticator.MySQLConnected()) {
                                    String ip = main.minecraftBridge.GetAddress(uuid);
                                    String name = main.minecraftBridge.GetPlayerName(uuid);
                                    GoAuthenticator.PlayerData data = new GoAuthenticator.PlayerData();
                                    data.LastServer = (String)main.config.getValue("server");
                                    data.RegIP = ip;
                                    data.LastIP = ip;
                                    data.Username = name;
                                    data.UUID = uuid;
                                    data.PasswordHash = main.authenticator.HashPassword(pass1);
                                    if (main.authenticator.Register(data)) {
                                        msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("successRegister"));
                                        log(main.config.getValue("prefix") + main.lang.get("registerConsole", main.minecraftBridge.GetPlayerName(uuid)));
                                    } else {
                                        msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("error"));
                                    }
                                    main.CheckStatusSync(uuid, main.minecraftBridge.GetAddress(uuid));
                                }else{
                                    msg(uuid,(String) main.config.getValue("prefix") + (String) main.lang.get("mysqlConnectionError"));
                                }
                            }
                        });
                    }else{
                        msg(uuid,(String) main.config.getValue("prefix")+(String) main.lang.get("passwordsNMatch"));
                    }
                }else{
                    main.canPlayMsg(uuid);
                }
            }else{
                msg(uuid,(String) main.config.getValue("prefix")+(String) main.lang.get("registerUsage"));
            }
        }else if(CommandEquals("2fa",cmd) && onlyPlayer(uuid)){
            if (args.length >= 1){
                main.RunAsync(new Runnable() {
                    @Override
                    public void run() {
                        if(main.authenticator.MySQLConnected()) {
                            if (main.authenticator.TwoFactorAuthed(uuid, main.minecraftBridge.GetAddress(uuid))) {
                                msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("alreadyLoggedIn"));
                                return;
                            }
                            if (main.authenticator.Authorize2FA(uuid, args[0], main.minecraftBridge.GetAddress(uuid))) {
                                msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("2faAccepted"));
                                log((String) main.config.getValue("prefix") + (String) main.lang.get("2faConsole", main.minecraftBridge.GetPlayerName(uuid)));
                            } else {
                                msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("2faDenied"));
                            }
                        }else{
                            msg(uuid,(String) main.config.getValue("prefix") + (String) main.lang.get("mysqlConnectionError"));
                        }
                    }
                });
            }else{
                main.minecraftBridge.SendChat(uuid,(String) main.config.getValue("prefix")+(String) main.lang.get("twofactorUsage"));
            }
        }else if(CommandEquals("changepassword",cmd) && onlyPlayer(uuid)){
            if (args.length >= 3){
                if (args[1].equals(args[2])) {
                    main.RunAsync(new Runnable() {
                        @Override
                        public void run() {
                            if (main.authenticator.MySQLConnected()) {
                                if (main.authenticator.CanLogin(uuid, args[0])) {
                                    String pass = args[1];
                                    if (main.authenticator.ChangePassword(uuid, pass)) {
                                        msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("chpassSuccess"));
                                        log(main.config.getValue("prefix") + main.lang.get("chpassConsole", main.minecraftBridge.GetPlayerName(uuid)));
                                    } else {
                                        msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("error"));
                                    }
                                } else {
                                    main.RunSync(new Runnable() {
                                        @Override
                                        public void run() {
                                            msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("incorrectPassword"));
                                        }
                                    });
                                }
                            }else{
                                msg(uuid,(String) main.config.getValue("prefix") + (String) main.lang.get("mysqlConnectionError"));
                            }
                        }
                    });
                } else {
                    msg(uuid, (String) main.config.getValue("prefix") + (String) main.lang.get("passwordsNMatch"));
                }
            }else{
                msg(uuid,(String) main.config.getValue("prefix")+(String) main.lang.get("chpassUsage"));
            }
        }else if(CommandEquals("goauth",cmd)){
            if (args.length >= 1){
                if(!isPlayer(uuid) || main.minecraftBridge.HasPermission(uuid,"goauth.admin")){
                    switch (args[0].toLowerCase()) {
                        case "register":
                            if (args.length >= 3) {
                                String username = args[1];
                                String password = args[2];
                                GoAuthenticator.PlayerData pp = main.authenticator.GetPlayerByName(username);
                                if (pp == null) {
                                    GoAuthenticator.PlayerData p = new GoAuthenticator.PlayerData();
                                    p.Username = username;
                                    p.PasswordHash = main.authenticator.HashPassword(password);
                                    p.UUID = null;
                                    if (main.minecraftBridge.GetUUID(username) != null){
                                        p.UUID = main.minecraftBridge.GetUUID(username);
                                        main.authenticator.SavePlayer(p);
                                        main.SetPlayerStatus(p.UUID,AuthState.LOGIN);
                                        lmsg(p.UUID,"adminRegistered");
                                    }else{
                                        main.authenticator.SavePlayer(p);
                                    }
                                    lmsg(uuid,"forceRegistered");
                                } else {
                                    lmsg(uuid, "playerAlreadyRegistered");
                                }
                            } else {
                                lmsg(uuid, "badUsage");
                            }
                            break;
                        case "unregister":
                            if (args.length >= 2) {
                                String username = args[1];
                                GoAuthenticator.PlayerData pp = main.authenticator.GetPlayerByName(username);
                                if (pp != null) {
                                    if (main.authenticator.DeletePlayerByName(pp.Username)) {
                                        lmsg(uuid, "adminPlayerDeleteSuccess");
                                        if (main.minecraftBridge.GetUUID(username) != null) {
                                            main.authenticator.ClearPlayer(pp.UUID);
                                            main.SetPlayerStatus(pp.UUID,AuthState.UNREGISTERED);
                                            lmsg(pp.UUID, "adminUnregistered");
                                        }
                                    } else {
                                        lmsg(uuid, "error");
                                    }
                                } else {
                                    lmsg(uuid, "playerNotFound");
                                }
                            } else {
                                lmsg(uuid, "badUsage");
                            }
                            break;
                        case "forcelogin":
                            if (args.length >= 2) {
                                String puuid = main.minecraftBridge.GetUUID(args[1]);
                                if (puuid != null) {
                                    main.SetPlayerStatus(puuid, AuthState.LOGGEDIN);
                                    lmsg(uuid, "forceLoggedIn");
                                    lmsg(puuid, "forceLoginNotify");
                                    main.RunAsync(new Runnable() {
                                        @Override
                                        public void run() {
                                            GoAuthenticator.PlayerData pd = main.authenticator.GetPlayer(puuid);
                                            String ip = main.minecraftBridge.GetAddress(puuid);
                                            pd.LastServer = (String) main.config.getValue("server");
                                            pd.LastIP = ip;
                                            pd.LastLogin = System.currentTimeMillis() / 1000;
                                            main.authenticator.SavePlayer(pd);
                                        }
                                    });
                                } else {
                                    lmsg(uuid, "playerIsOffline");
                                }
                            } else {
                                lmsg(uuid, "badUsage");
                            }
                            break;
                        case "info":
                            if (args.length >= 2) {
                                String username = args[1];
                                GoAuthenticator.PlayerData p = main.authenticator.GetPlayerByName(username);
                                if (p != null) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat(main.lang.get("dateFormat"));
                                    String regIP = p.RegIP;
                                    String lastIP = p.LastIP;
                                    String lastServer = p.LastServer;
                                    Long regTimeN = p.RegTime;
                                    Long logTimeN = p.LastLogin;
                                    String regTime = dateFormat.format(new Date(regTimeN * 1000));
                                    String logTime = dateFormat.format(new Date(logTimeN * 1000));
                                    msg(uuid, main.lang.get("playerInfo", p.Username, regTime, regIP, logTime, lastIP, lastServer));
                                } else {
                                    lmsg(uuid, "playerNotFound");
                                }
                            }
                            break;
                        default:
                            msg(uuid, getHelp(uuid));
                            break;
                    }
                }else{
                    lmsg(uuid,"noPerm");
                }
            }else{
                msg(uuid,getHelp(uuid));
            }
        }
    }
}
