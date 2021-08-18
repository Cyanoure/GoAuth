package ga.cyanoure.goauth.main;

import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class GoConfigManager {
    private Yaml yaml;
    private Map<String, Object> data = new HashMap<String, Object>();
    private Map<String, Object> defaultData = new HashMap<String, Object>();
    private String defaultPluginPath = "plugins/GoAuth";
    private String pluginPath = defaultPluginPath;

    public String getPluginPath(){
        //return StringUtils.strip(pluginPath,"/");
        String p = pluginPath;
        //if(p == null) p = defaultPluginPath;
        if (p.startsWith("/")){
            p = p.substring(0,p.length()-2);
        }
        return p;
    }

    public void setPluginPath(String path){
        pluginPath = path;
    }

    public Object getValue(String key){
        return (data.get(key) != null ? data.get(key) : defaultData.get(key));
    }

    public Object getValue(String key, Object defaultValue){
        Object in = getValue(key);
        if (in == null){
            return defaultValue;
        }
        return in;
    }

    public void setValue(String key, Object value){
        data.put(key, value);
    }

    public Object getDefaultValue(String key){
        return defaultData.get(key);
    }

    public void setDefaultValue(String key, Object value){
        defaultData.put(key, value);
    }

    public GoConfigManager(){
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(dumperOptions);
    }

    public void LoadFile(String path){
        Map<String, Object> obj = new HashMap<String, Object>();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
        if(inputStream != null){
            obj = yaml.load(inputStream);
        }
        try{
            File file = new File(getPluginPath()+"/"+path);
            /*Scanner fReader = new Scanner(file);
            List<String> fileInputList = new ArrayList<String>();
            while(fReader.hasNextLine()){
                fileInputList.add(fReader.nextLine());
            }
            fReader.close();
            String fileInputString = String.join("\n",fileInputList);*/
            InputStream fileStream = new DataInputStream(new FileInputStream(file));
            Map<String, Object> obj2 = yaml.load(fileStream);
            obj.putAll(obj2);
        }catch(FileNotFoundException e){}

        try {
            File file = new File(getPluginPath()+"/"+path);
            file.getParentFile().mkdirs();
            //FileWriter writer = new FileWriter(file);
            Writer writer = new OutputStreamWriter(new FileOutputStream(getPluginPath()+"/"+path),"UTF-8");
            yaml.dump(obj,writer);
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        data = obj;
    }

    public void SaveFile(String path){
        try {
            //CopyDefaults();
            File file = new File(getPluginPath()+"/"+path);
            file.getParentFile().mkdirs();
            //FileWriter writer = new FileWriter(file);
            Writer writer = new OutputStreamWriter(new FileOutputStream(getPluginPath()+"/"+path),"UTF-8");
            yaml.dump(data,writer);
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void CopyDefaults(){
        Map oldData = data;
        data = new HashMap<String, Object>();
        data.putAll(defaultData);
        data.putAll(data);
    }
}
