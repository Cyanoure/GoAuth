package ga.cyanoure.goauth.main;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class LanguageManager {
    private GoConfigManager configManager = new GoConfigManager();
    private String pluginPath = "plugins/GoAuth";

    public LanguageManager(String langCode,String _pluginPath){
        pluginPath = _pluginPath;
        LoadLanguage(langCode);
    }

    public void LoadLanguage(String langCode){
        String langFile = "lang/"+langCode+".yml";
        configManager.setPluginPath(pluginPath);
        configManager.LoadFile(langFile);
    }

    public String get(String textID, String... texts){
        String out = (String)configManager.getValue(textID);
        for(int i = 0; i < texts.length; i++){
            String text = texts[i];
            out = out.replace("{"+String.valueOf(i)+"}",text);
        }
        return out;
    }
}
