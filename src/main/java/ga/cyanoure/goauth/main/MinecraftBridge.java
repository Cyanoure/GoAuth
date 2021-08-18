package ga.cyanoure.goauth.main;

public interface MinecraftBridge {
    public void SendChat(String uuid, String message);
    public void KickPlayer(String uuid, String reason);
    public boolean HasPermission(String uuid, String permission);
    public String GetPlayerName(String uuid);
    public String GetPluginDir();
    public void SendConsole(String msg);
    public boolean IsOnline(String uuid);
    public boolean IsOnlineName(String name);
    public String GetAddress(String uuid);
    public String GetUUID(String name);
    public void LockPlayer(String uuid);
    public void UnlockPlayer(String uuid);
    public String GetServerType();
}
