package ga.cyanoure.goauth.spigot;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Language{
	public String LangCode;
	private File file;
	private FileConfiguration lang;
	
	public Language(File dataFolder, String LangCode) {
		file = new File(dataFolder, "lang/"+LangCode+".yml");
		lang = YamlConfiguration.loadConfiguration(file);
	}
	
	public String getText(String name) {
		return lang.getString(name);
	}
}
