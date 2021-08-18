package ga.cyanoure.goauth.main;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.warrenstrange.googleauth.GoogleAuthenticator;

public class GoAuthenticator {
	public String db_host;
	public String db_database;
	public String db_user;
	public String db_pass;
	public String db_table = "goauth";
	public int SessionLength = 5;
	public String HashPrefix = "";
	
	public Connection db_conn = null;
	public Statement db_stmt = null;
	public ResultSet db_res = null;
	
	public List<String> RegisteredPlayers = new ArrayList<String>();
	public List<String> LoggedInPlayers = new ArrayList<String>();
	public List<String> Authed2FAPlayers = new ArrayList<String>();
	public List<TwoFactorKeyGen> TwoFactorGeneratedKeys = new ArrayList<TwoFactorKeyGen>();
	
	public boolean DebugMode = false;
	
	public GoogleAuthenticator gAuth = null; // https://github.com/wstrange/GoogleAuth
	
	//TABLE -> id, uuid, username, password, regip, lastip, regtime, lastlogin, lastseen, lastserver
	
	public GoAuthenticator(String db_host, String db_database, String db_user, String db_pass, String db_table) {
		Init(db_host,db_database,db_user,db_pass,db_table);
	}

	public GoAuthenticator(String db_host, String db_database, String db_user, String db_pass){
		Init(db_host,db_database,db_user,db_pass,"goauth");
	}

	private void Init(String db_host, String db_database, String db_user, String db_pass, String db_table){
		this.db_host = db_host;
		this.db_database = db_database;
		this.db_user = db_user;
		this.db_pass = db_pass;
		this.db_table = db_table;

		ConnectMySQL();
		ExecMySQL("CREATE TABLE IF NOT EXISTS "+db_table+" (id int(11) NOT NULL AUTO_INCREMENT,uuid varchar(40) DEFAULT NULL,username varchar(100) DEFAULT NULL,password varchar(100) DEFAULT NULL,regip varchar(16) NOT NULL DEFAULT '0.0.0.0',lastip varchar(16) NOT NULL DEFAULT '0.0.0.0',regtime int(11) NOT NULL DEFAULT 0,lastlogin int(11) NOT NULL DEFAULT 0,lastseen int(11) NOT NULL DEFAULT 0,lastserver varchar(100) NOT NULL DEFAULT 'default', 2fa_ip varchar(16) NOT NULL DEFAULT '127.0.0.1', 2fa_key varchar(32) DEFAULT NULL,PRIMARY KEY (id));");

		gAuth = new GoogleAuthenticator();
	}
	
	private String getConURL() {
		return "jdbc:mysql://"+db_host+"/"+db_database;
	}
	
	private void ConnectMySQL() {
		try {
			db_conn = DriverManager.getConnection(getConURL(),db_user,db_pass);
			db_stmt = db_conn.createStatement();
		}catch(Exception exc) {
			if(DebugMode) {
				exc.printStackTrace();
			}
		}
	}
	
	private void CloseMySQL() {
		try { db_res.close(); } catch (Exception e) { /* ignored */ }
	    try { db_stmt.close(); } catch (Exception e) { /* ignored */ }
	    try { db_conn.close(); } catch (Exception e) { /* ignored */ }
	}
	
	public boolean MySQLConnected() {
		if(db_conn == null) return false;
		try{
			boolean connected = !db_conn.isClosed() && !db_stmt.isClosed();
			db_stmt.executeQuery("SELECT null;");
			return connected;
		}catch(Exception exc) {
			if(DebugMode) {
				exc.printStackTrace();
			}
		}
		return false;
	}
	
	private boolean QueryMySQL(String sql) {
		try{
			if(!MySQLConnected()) {
				CloseMySQL();
				ConnectMySQL();
			}
			if(MySQLConnected()) {
				db_res = db_stmt.executeQuery(sql);
				return true;
			}
		}catch(Exception exc) {
			exc.printStackTrace();
		}
		return false;
	}
	
	private boolean ExecMySQL(String sql) {
		if(db_conn == null) return false;
		Boolean out = false;
		try{
			if(db_conn.isClosed() || db_stmt.isClosed()) {
				CloseMySQL();
				ConnectMySQL();
			}
			db_stmt.execute(sql);
			out = true;
		}catch(Exception exc) {
			exc.printStackTrace();
		}
		return out;
	}
	
	public void Stop() {
		CloseMySQL();
	}
	
	public static class PlayerData{
		public String Username = "Player";
		public String UUID = null;
		public String PasswordHash = null;
		public String RegIP = "0.0.0.0";
		public String LastIP = "0.0.0.0";
		public long RegTime = System.currentTimeMillis()/1000;
		public long LastLogin = System.currentTimeMillis()/1000;
		public long LastSeen = System.currentTimeMillis()/1000;
		public String LastServer = "default";
		public String TwoFactorIP = "0.0.0.0";
		public String TwoFactorKey = null;
		public String GeneratedTwoFactorKey = null;
	}

	private PlayerData resultToPlayerData(ResultSet db_res){
		try {
			PlayerData p = new PlayerData();
			p.UUID = db_res.getString("uuid");
			p.Username = db_res.getString("username");
			p.PasswordHash = db_res.getString("password");
			p.RegIP = db_res.getString("regip");
			p.LastIP = db_res.getString("lastip");
			p.RegTime = Integer.parseInt(db_res.getString("regtime"));
			p.LastLogin = Integer.parseInt(db_res.getString("lastlogin"));
			p.LastSeen = Integer.parseInt(db_res.getString("lastseen"));
			p.LastServer = db_res.getString("lastserver");
			p.TwoFactorIP = db_res.getString("2fa_ip");
			p.TwoFactorKey = db_res.getString("2fa_key");

			if (p.UUID != null) {
				if (p.UUID.equalsIgnoreCase("null") || p.UUID.equalsIgnoreCase("")) {
					p.UUID = null;
				}
				if (Is2FASecretGenerated(p.UUID)) {
					p.GeneratedTwoFactorKey = New2FASecret(p.UUID);
				}
			}
			return p;
		}catch (Exception exc){
			exc.printStackTrace();
		}
		return null;
	}
	
	public PlayerData GetPlayer(String uuid) {
		if(!QueryMySQL("SELECT * FROM "+db_table+" WHERE uuid='"+uuid+"'")) return null;
		try {
			if(db_res.next()) {
				return resultToPlayerData(db_res);
			}
		}catch(Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	public PlayerData GetPlayerByName(String name){
		if(!QueryMySQL("SELECT * FROM "+db_table+" WHERE username='"+name+"'")) return null;
		try {
			if(db_res.next()) {
				return resultToPlayerData(db_res);
			}
		}catch(Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}
	
	public boolean SavePlayer(PlayerData p, Boolean byName) {
		if(p.Username != null) p.Username = p.Username.replace("'","").replace("\"","");
		if(p.UUID != null && !byName) {
			p.UUID = p.UUID.replace("'","").replace("\"","");
			if (GetPlayer(p.UUID) != null) {
				return ExecMySQL(("UPDATE " + db_table + " SET username='" + p.Username + "', password='" + p.PasswordHash + "', regip='" + p.RegIP + "', lastip='" + p.LastIP + "', regtime=" + String.valueOf(p.RegTime) + ", lastlogin=" + String.valueOf(p.LastLogin) + ", lastseen=" + String.valueOf(p.LastSeen) + ", lastserver='" + p.LastServer + "', 2fa_ip='" + p.TwoFactorIP + "', 2fa_key='" + p.TwoFactorKey + "' WHERE uuid='" + p.UUID + "'").replace("'null'", "null"));
			} else {
				return ExecMySQL(("INSERT INTO " + db_table + " (uuid,username,password,regip,lastip,regtime,lastlogin,lastseen,lastserver,2fa_ip,2fa_key) VALUES ('" + p.UUID + "','" + p.Username + "','" + p.PasswordHash + "','" + p.RegIP + "','" + p.LastIP + "'," + String.valueOf(p.RegTime) + "," + String.valueOf(p.LastLogin) + "," + String.valueOf(p.LastSeen) + ",'" + p.LastServer + "','" + p.TwoFactorIP + "','" + p.TwoFactorKey + "')").replace("'null'", "null"));
			}
		}else if(p.Username != null){
			if (GetPlayerByName(p.Username) != null) {
				return ExecMySQL(("UPDATE " + db_table + " SET uuid='"+p.UUID+"', username='" + p.Username + "', password='" + p.PasswordHash + "', regip='" + p.RegIP + "', lastip='" + p.LastIP + "', regtime=" + String.valueOf(p.RegTime) + ", lastlogin=" + String.valueOf(p.LastLogin) + ", lastseen=" + String.valueOf(p.LastSeen) + ", lastserver='" + p.LastServer + "', 2fa_ip='" + p.TwoFactorIP + "', 2fa_key='" + p.TwoFactorKey + "' WHERE username='" + p.Username + "'").replace("'null'", "null"));
			} else {
				return ExecMySQL(("INSERT INTO " + db_table + " (uuid,username,password,regip,lastip,regtime,lastlogin,lastseen,lastserver,2fa_ip,2fa_key) VALUES (null,'" + p.Username + "','" + p.PasswordHash + "','" + p.RegIP + "','" + p.LastIP + "'," + String.valueOf(p.RegTime) + "," + String.valueOf(p.LastLogin) + "," + String.valueOf(p.LastSeen) + ",'" + p.LastServer + "','" + p.TwoFactorIP + "','" + p.TwoFactorKey + "')").replace("'null'", "null"));
			}
		}else{
			return false;
		}
	}

	public boolean SavePlayer(PlayerData p){
		return SavePlayer(p,false);
	}

	public boolean SavePlayerByName(PlayerData p){
		return SavePlayer(p,true);
	}
	
	public boolean CanLogin(String uuid, String pass) {
		PlayerData p = GetPlayer(uuid);
		if(p != null) {
			if(p.PasswordHash.equalsIgnoreCase(HashPassword(pass))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean IsRegistered(String uuid) {
		if(GetPlayer(uuid) == null) {
			return false;
		}else {
			RegisteredPlayers.add(uuid);
			return true;
		}
	}
	
	public String HashPassword(String password) {
		return Sha256(HashPrefix+password);
	}
	
	public String Sha256(String text) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(hash);
		}catch(Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public boolean CacheLoggedIn(String uuid) {
		return LoggedInPlayers.contains(uuid);
	}
	
	public boolean LoggedIn(String uuid, String ip) {
		if(CacheLoggedIn(uuid)) {
			return true;
		}else {
			if(IsRegistered(uuid)) {
				PlayerData data = GetPlayer(uuid);
				long CurrentSecs = System.currentTimeMillis()/1000;
				if(CurrentSecs - data.LastSeen < SessionLength && data.LastIP.equals(ip)) {
					LoggedInPlayers.add(uuid);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean CacheRegistered(String uuid) {
		return RegisteredPlayers.contains(uuid);
	}
	
	public boolean Registered(String uuid) {
		if(CacheRegistered(uuid)) {
			return true;
		}else if(GetPlayer(uuid) != null) {
			RegisteredPlayers.add(uuid);
			return true;
		}
		return false;
	}
	
	public void ClearPlayer(String uuid) {
		while(LoggedInPlayers.contains(uuid)) {
			LoggedInPlayers.remove(uuid);
		}
		while(RegisteredPlayers.contains(uuid)) {
			RegisteredPlayers.remove(uuid);
		}
		while(Authed2FAPlayers.contains(uuid)) {
			Authed2FAPlayers.remove(uuid);
		}
	}
	
	public void UpdateSession(String uuid) {
		if(CacheLoggedIn(uuid)) {
			PlayerData data = GetPlayer(uuid);
			if(data != null) {
				data.LastSeen = System.currentTimeMillis()/1000;
				SavePlayer(data);
			}
		}
	}
	
	public boolean LoginProcess(String uuid,String password,String username,String ip,String servername) {
		if(CanLogin(uuid,password)) {
			PlayerData data = GetPlayer(uuid);
			data.LastServer = servername;
			data.LastLogin = System.currentTimeMillis()/1000;
			data.LastSeen = System.currentTimeMillis()/1000;
			data.LastIP = ip;
			data.Username = username;
			SavePlayer(data);
			LoggedInPlayers.add(data.UUID);
			return true;
		}
		return false;
	}

	public boolean LoginProcess(String uuid,String password,String username,String ip){
		return LoginProcess(uuid,password,username,ip,"default");
	}
	
	public boolean Register(PlayerData data) {
		if(GetPlayer(data.UUID) == null) {
			data.LastLogin = System.currentTimeMillis()/1000;
			data.LastSeen = System.currentTimeMillis()/1000;
			data.RegTime = System.currentTimeMillis()/1000;
			if(SavePlayer(data)) {
				LoggedInPlayers.add(data.UUID);
				return true;
			}
		}
		return false;
	}
	
	public boolean ChangePassword(String uuid, String newPass) {
		if(Registered(uuid)) {
			PlayerData plr = GetPlayer(uuid);
			plr.PasswordHash = HashPassword(newPass);
			//return SavePlayer(plr);
			SavePlayer(plr);
			return true;
		}
		return false;
	}
	
	public boolean Authorize2FA(String uuid, int pwd) {
		PlayerData plr = this.GetPlayer(uuid);
		String secret = plr.TwoFactorKey;
		boolean success = secret != null && gAuth.authorize(secret, pwd);
		if(success) {
			return true;
		}else if(plr.GeneratedTwoFactorKey != null) {
			if(gAuth.authorize(plr.GeneratedTwoFactorKey, pwd)) {
				plr.TwoFactorKey = plr.GeneratedTwoFactorKey;
				SavePlayer(plr);
				Delete2FAGenerator(uuid);
				return true;
			}else {
				return false;
			}
		}
		return false;
	}
	
	public boolean Authorize2FA(String uuid, String pwd) {
		try {
			return Authorize2FA(uuid, Integer.parseInt(pwd));
		}catch(Exception e) {
			
		}
		return false;
	}
	
	public boolean Authorize2FA(String uuid, int pwd, String ip) {
		if(Authorize2FA(uuid,pwd)) {
			Authed2FAPlayers.add(uuid);
			PlayerData plr = GetPlayer(uuid);
			plr.TwoFactorIP = ip.trim();
			SavePlayer(plr);
			return true;
		}else {
			return false;
		}
	}
	
	public boolean Authorize2FA(String uuid, String pwd, String ip) {
		try {
			return Authorize2FA(uuid,Integer.parseInt(pwd),ip);
		}catch(Exception e) {
			
		}
		return false;
	}
	
	public boolean TwoFactorAuthedCache(String uuid) {
		if(!CacheLoggedIn(uuid)) {
			Authed2FAPlayers.remove(Authed2FAPlayers.indexOf(uuid));
			return false;
		}
		return Authed2FAPlayers.contains(uuid);
	}
	
	public boolean TwoFactorAuthed(String uuid, String ip) {
		if(!LoggedIn(uuid,ip)) {
			Authed2FAPlayers.remove(Authed2FAPlayers.indexOf(uuid));
			return false;
		}
		if(TwoFactorAuthedCache(uuid)) {
			return true;
		}else {
			PlayerData plr = GetPlayer(uuid);
			boolean authed = !plr.TwoFactorIP.equals("127.0.0.1") && plr.TwoFactorIP.trim().equals(ip.trim());
			if(authed) {
				Authed2FAPlayers.add(uuid);
			}
			return authed;
		}
	} // a cache torlodjon, ha a jelszavas belepes nincs bejelentkezve, mert egyszer beirjuk a 2fa-t, utana semmilyen ip-vel nem fogja kerni
	
	public boolean TwoFactorActive(String uuid) {
		PlayerData plr = GetPlayer(uuid);
		return plr.TwoFactorKey != null && plr.TwoFactorKey != "null";
	}
	
	public class TwoFactorKeyGen {
		public String uuid = null;
		public String secret = null;
		public TwoFactorKeyGen(String _uuid) {
			uuid = _uuid;
			secret = gAuth.createCredentials().getKey();
			TwoFactorGeneratedKeys.add(this);
		}
	}
	
	public boolean Is2FASecretGenerated(String uuid) {
		for(int i = 0; i < TwoFactorGeneratedKeys.size(); i++) {
			TwoFactorKeyGen kg = TwoFactorGeneratedKeys.get(i);
			if(kg.uuid.equals(uuid)) {
				return true;
			}
		}
		return false;
	}
	
	public String New2FASecret(String uuid) {
		for(int i = 0; i < TwoFactorGeneratedKeys.size(); i++) {
			TwoFactorKeyGen kg = TwoFactorGeneratedKeys.get(i);
			if(kg.uuid.equals(uuid)) {
				return kg.secret;
			}
		}
		return new TwoFactorKeyGen(uuid).secret;
	}
	
	public void Delete2FAGenerator(String uuid) {
		int index = -1;
		for(int i = 0; i < TwoFactorGeneratedKeys.size(); i++) {
			TwoFactorKeyGen kg = TwoFactorGeneratedKeys.get(i);
			if(kg.uuid.equals(uuid)) {
				index = i;
			}
		}
		if(index > -1) {
			TwoFactorGeneratedKeys.remove(index);
		}
	}

	public Boolean DeletePlayer(String uuid){
		uuid = uuid.replace("'","").replace("\"","");
		return ExecMySQL("DELETE FROM  " + db_table + " WHERE uuid='"+uuid+"'");
	}

	public Boolean DeletePlayerByName(String name){
		name = name.replace("'","").replace("\"","");
		return ExecMySQL("DELETE FROM  " + db_table + " WHERE username='"+name+"'");
	}
}
