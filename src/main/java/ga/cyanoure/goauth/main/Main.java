package ga.cyanoure.goauth.main;

import ga.cyanoure.goauth.main.enums.AuthState;

import java.util.*;

public class Main {
    public GoAuthenticator authenticator;
    public CommandReceiver commandReceiver;
    public GoConfigManager config;
    public String pluginDir = "plugins/GoAuth";
    public MinecraftBridge minecraftBridge;
    public LanguageManager lang;

    private Map<String, AuthState> playerStatus = new HashMap<String,AuthState>();
    // unchecked, unregistered, 2fa, loggedin, login, bungeecord
    // UNCHECKED, UNREGISTERED, TWOFACTOR, LOGGEDIN, LOGIN, BUNGEECORD

    private List<Runnable> syncTasks = new ArrayList<Runnable>();
    private List<Runnable> asyncTasks = new ArrayList<Runnable>();

    private Map<String, Long> lastSeens = new HashMap<String, Long>();

    public Boolean bungeeMode(){
        return (Boolean) config.getValue("bungeecord") && minecraftBridge.GetServerType() != "bungeecord";
    }

    public String getSecret(){
        return (String) config.getValue("secret");
    }

    private void seenPlayer(String uuid){
        if(bungeeMode()) return;
        long time = System.currentTimeMillis()/1000;
        lastSeens.put(uuid,time);
        RunSync(new Runnable() {
            @Override
            public void run() {
                GoAuthenticator.PlayerData pd = authenticator.GetPlayer(uuid);
                pd.LastSeen = time;
                authenticator.SavePlayer(pd);
            }
        });
    }

    public void updatePlayerStatus(String uuid){
        if(bungeeMode()) return;
        Boolean online = minecraftBridge.IsOnline(uuid);
        if(!online){
            int timeout = (int)config.getValue("session-length");
            long time = System.currentTimeMillis();
            long lastSeen = lastSeens.get(uuid);
            if (lastSeen+time < time){
                lastSeens.remove(uuid);
            }
            return;
        };
        AuthState status = GetPlayerStatus(uuid);
        if (status == AuthState.LOGGEDIN){
            seenPlayer(uuid);
        }
    }

    public Boolean sessionActive(String uuid){
        if(bungeeMode()) return false;
        if(!minecraftBridge.IsOnline(uuid)) return false;
        int timeout = (int)config.getValue("session-length");
        if(!lastSeens.containsKey(uuid) || timeout <= 0) return false;
        GoAuthenticator.PlayerData pd = authenticator.GetPlayer(uuid);
        if (pd.LastIP != minecraftBridge.GetAddress(uuid)) return false;
        long time = System.currentTimeMillis();
        long lastSeen = lastSeens.get(uuid);
        if (lastSeen+timeout >= time){
            return true;
        }
        return false;
    }

    private void log(String message){
        RunSync(new Runnable() {
            @Override
            public void run() {
                minecraftBridge.SendConsole(message);
            }
        });
    }

    private void msg(String uuid, String msg){
        RunSync(new Runnable() {
            @Override
            public void run() {
                minecraftBridge.SendChat(uuid,msg);
            }
        });
    }

    private void kick(String uuid, String msg){
        RunSync(new Runnable() {
            @Override
            public void run() {
                minecraftBridge.KickPlayer(uuid,msg);
            }
        });
    }

    public AuthState GetPlayerStatus(String uuid){
        if(!playerStatus.containsKey(uuid)) return AuthState.UNCHECKED;
        return playerStatus.get(uuid);
    }

    public void SetPlayerStatus(String uuid, AuthState status){
        if(uuid == null) return;
        if (status == AuthState.UNCHECKED && bungeeMode()) status = AuthState.BUNGEECORD;
        playerStatus.put(uuid,status);
        if (status == AuthState.LOGGEDIN){
            minecraftBridge.UnlockPlayer(uuid);
        }else{
            minecraftBridge.LockPlayer(uuid);
        }
    }

    public void RunAsync(Runnable runnable){
        asyncTasks.add(runnable);
    }

    public void RunSync(Runnable runnable){
        syncTasks.add(runnable);
    }

    public String getPrefix(){
        return (String) config.getValue("prefix");
    }

    public void UsernameCheck(String uuid){
        String username = minecraftBridge.GetPlayerName(uuid);
        RunAsync(new Runnable() {
            @Override
            public void run() {
                GoAuthenticator.PlayerData p = authenticator.GetPlayerByName(username);
                if (p == null){
                    return;
                }else{
                    if (!p.Username.equals(username)) {
                        if(!bungeeMode() && (Boolean) config.getValue("username-case-protection")) {
                            RunSync(new Runnable() {
                                @Override
                                public void run() {
                                    minecraftBridge.KickPlayer(uuid, lang.get("usernameCaseKick", p.Username));
                                }
                            });
                        }
                    }else{
                        if(p.UUID == null){
                            p.UUID = uuid;
                            authenticator.SavePlayerByName(p);
                            CheckStatusSync(uuid);
                        }
                    }
                }
            }
        });
    }

    public void JoinPlayer(String uuid){
        if(bungeeMode()){
            playerStatus.put(uuid,AuthState.BUNGEECORD);
            return;
        }
        playerStatus.put(uuid,AuthState.UNCHECKED);
        asyncTasks.add(new Runnable() {
            @Override
            public void run() {
                AuthState status = CheckStatusSync(uuid,minecraftBridge.GetAddress(uuid));
                if(canPlay(uuid)){
                    msg(uuid,(String) config.getValue("prefix")+lang.get("autoLogin"));
                    log((String) config.getValue("prefix")+lang.get("autologinConsole",minecraftBridge.GetPlayerName(uuid)));
                }else {
                    RunSync(new Runnable() {
                        @Override
                        public void run() {
                            minecraftBridge.LockPlayer(uuid);
                        }
                    });
                    Runnable loginRunnable = new Runnable(){
                        private int countdown = (int)config.getValue("login-timeout")*20;
                        @Override
                        public void run() {
                            if(GetPlayerStatus(uuid) != AuthState.LOGGEDIN) {
                                //minecraftBridge.SendChat(uuid,String.valueOf(countdown));
                                if (countdown > 0) {
                                    if (countdown % (10*20) == 0){
                                        canPlayMsg(uuid);
                                    }
                                    countdown--;
                                    RunAsync(this);
                                } else {
                                    RunSync(new Runnable() {
                                        @Override
                                        public void run() {
                                            kick(uuid,lang.get("loginTimeout"));
                                        }
                                    });
                                }
                            }
                        }
                    };
                    RunAsync(loginRunnable);
                }
            }
        });
    }

    public AuthState CheckStatusSync(String uuid,String ip){
        AuthState prevStatus = playerStatus.get(uuid);
        AuthState status = AuthState.UNCHECKED;
        if(authenticator.Registered(uuid)){
            if(authenticator.LoggedIn(uuid,ip)){
                if((Boolean) config.getValue("enable-2fa") && minecraftBridge.HasPermission(uuid,"goauth.twofactor") && !authenticator.TwoFactorAuthed(uuid,ip)){
                    //status = "2fa";
                    if(authenticator.TwoFactorActive(uuid)){
                        status = AuthState.TWOFACTOR;
                    }else{
                        status = AuthState.TWOFACTOR_KEY;
                    }
                }else{
                    status = AuthState.LOGGEDIN;
                }
            }else{
                status = AuthState.LOGIN;
            }
        }else{
            status = AuthState.UNREGISTERED;
        }
        playerStatus.put(uuid,status);
        if(prevStatus != status && status == AuthState.LOGGEDIN){
            RunSync(new Runnable() {
                @Override
                public void run() {
                    minecraftBridge.UnlockPlayer(uuid);
                }
            });
        }else if(prevStatus != status){
            RunSync(new Runnable() {
                @Override
                public void run() {
                    minecraftBridge.LockPlayer(uuid);
                }
            });
        }
        return status;
    }

    public AuthState CheckStatusSync(String uuid){
        return CheckStatusSync(uuid,"127.0.0."+Math.round(Math.random()*254+1));
    }

    public void CheckStatus(String uuid,String ip){
        new Thread(new Runnable() {
            @Override
            public void run() {
                CheckStatusSync(uuid,ip);
            }
        }).start();
    }

    public void CheckStatus(String uuid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                CheckStatusSync(uuid);
            }
        }).start();
    }

    public void LeavePlayer(String uuid){
        playerStatus.remove(uuid);
        authenticator.ClearPlayer(uuid);
    }

    public void runSyncTasks(){
        List<Runnable> taskList = new ArrayList<Runnable>();
        taskList.addAll(syncTasks);
        syncTasks.clear();
        for(int i = 0; i < taskList.size(); i++){
            try {
                Runnable runnable = taskList.get(i);
                runnable.run();
            }catch (Exception e) { }
        }
    }

    public void runAsyncTasks(){
        List<Runnable> taskList = new ArrayList<Runnable>();
        taskList.addAll(asyncTasks);
        asyncTasks.clear();
        for(int i = 0; i < taskList.size(); i++){
            try {
                Runnable runnable = taskList.get(i);
                runnable.run();
            }catch (Exception e) { }
        }
    }

    class AsyncScheduler extends TimerTask{
        public void run(){
            try{
                runAsyncTasks();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class SecondScheduler extends TimerTask{
        public void run(){
            List<String> uuids = new ArrayList<String>();
            for (Map.Entry<String,AuthState> entry : playerStatus.entrySet()){
                String uuid = entry.getKey();
                uuids.add(uuid);
            }
            for (Map.Entry<String,Long> entry : lastSeens.entrySet()){
                String uuid = entry.getKey();
                if (!uuids.contains(uuid)){
                    uuids.add(uuid);
                }
            }
            for (String uuid : uuids){
                RunSync(new Runnable() {
                    @Override
                    public void run() {
                        updatePlayerStatus(uuid);
                    }
                });
            }
        }
    }

    public Main(MinecraftBridge _minecraftBridge){
        minecraftBridge = _minecraftBridge;
        pluginDir = minecraftBridge.GetPluginDir();
        config = new GoConfigManager();
        config.setPluginPath(pluginDir);
        config.LoadFile("config.yml");

        lang = new LanguageManager((String) config.getValue("language"),pluginDir);

        commandReceiver = new CommandReceiver(this);

        Map<String, String> mysqlConnData = (Map<String, String>) config.getValue("mysql");
        authenticator = new GoAuthenticator(mysqlConnData.get("host"), mysqlConnData.get("database"), mysqlConnData.get("user"), mysqlConnData.get("password"), mysqlConnData.get("registered-players-table"));
        authenticator.SessionLength = (int)config.getValue("session-length");
        authenticator.HashPrefix = (String) config.getValue("password-prefix");

        log((String) config.getValue("prefix")+lang.get("pluginLoaded"));

        Timer t = new Timer();
        AsyncScheduler asyncTimer = new AsyncScheduler();
        t.scheduleAtFixedRate(asyncTimer,0,50);

        Timer t2 = new Timer();
        SecondScheduler ssch = new SecondScheduler();
        t2.scheduleAtFixedRate(ssch,0,1000);
    }

    public boolean canPlay(String uuid){
        if(playerStatus.get(uuid) == AuthState.LOGGEDIN){
            return true;
        }
        return false;
    }

    public boolean canPlayMsg(String uuid){
        boolean can = canPlay(uuid);
        if(!can && !bungeeMode()){
            AuthState status = playerStatus.get(uuid);
            if(status == AuthState.UNREGISTERED){
                msg(uuid,(String) config.getValue("prefix")+lang.get("registerMessage"));
            }else if(status == AuthState.LOGIN){
                msg(uuid,(String) config.getValue("prefix")+lang.get("loginMessage"));
            }else if(status == AuthState.TWOFACTOR){
                msg(uuid,(String) config.getValue("prefix")+lang.get("googleAuthCode"));
            }else if(status == AuthState.TWOFACTOR_KEY){
                send2FACode(uuid);
            }
        }
        return can;
    }

    public void send2FACode(String uuid){
        msg(uuid,(String) config.getValue("prefix")+lang.get("googleAuthSetup",authenticator.New2FASecret(uuid)));
    }
}
