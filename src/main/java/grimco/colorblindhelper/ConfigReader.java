package grimco.colorblindhelper;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import grimco.colorblindhelper.JsonConfigReader.ModToken;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInts;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public enum ConfigReader
{
    INSTANCE;

    private static class JsonData
    {
        public String stack;
        public String underlay = "0";
        public String overlay = "";
        public String overlayColor = "FFFFFFFF";
        
        private JsonData copy()
        {
            JsonData ret = new JsonData();
            ret.stack = stack;
            ret.underlay = underlay;
            ret.overlay = overlay;
            ret.overlayColor = overlayColor;
            return ret;
        }
    }

    private JsonConfigReader<JsonData> reader;
    
    private ArrayList<Object> configs;

    private File configDir;
    private File json;

    public void preInit(FMLPreInitializationEvent event)
    {
        configDir = event.getModConfigurationDirectory();
        
        File grimcoDirectory = new File(configDir, "grimco");
        if(!grimcoDirectory.exists())
        {
            if(!grimcoDirectory.mkdir())
            {
                ColorblindHelper.logger.error("Unabled to Create GrimCo Config Directory");
            }
        }
        
        json = new File(grimcoDirectory, "colorblind.json");
    }
    
    public static final JsonData DEFAULT_DATA = new JsonData();
    
    public void refresh()
    {
        configs = Lists.newArrayList();

        if (reader == null)
        {
            ModToken token = new ModToken(ColorblindHelper.class, ColorblindHelper.MODID + "/config");
            reader = new JsonConfigReader<JsonData>(token, json, JsonData.class);
            
        }

        reader.refresh();
        List<JsonData> custom = reader.getElements("custom");
        
        json.delete();
        FileUtil.copyFromJar(ColorblindHelper.class, ColorblindHelper.MODID + "/config/colorblind.json", json);
        reader.refresh();
        
        try
        {
            JsonElement object = new JsonParser().parse(FileUtils.readFileToString(json));
            JsonArray customData = object.getAsJsonObject().getAsJsonArray("custom");
    
            for (JsonData data : custom)
            {
                JsonData toWrite = data.copy();
                if (toWrite.overlay.equals(DEFAULT_DATA.overlay))
                    toWrite.overlay = null;
                if (toWrite.overlayColor.equals(DEFAULT_DATA.overlayColor))
                    toWrite.overlayColor = null;
                if (toWrite.underlay.equals(DEFAULT_DATA.underlay))
                    toWrite.underlay = null;
                customData.add(new Gson().toJsonTree(toWrite, JsonData.class));
            }
    
            FileUtil.writeToFile(json.getAbsolutePath(), new GsonBuilder().setPrettyPrinting().create().toJson(object));
    
            parseAll(reader.getElements("defaults"));
            parseAll(custom);
            
        }
        catch(IOException ioE)
        {
            ColorblindHelper.logger.warn("Unabled to parse colorblind.json");
        }

    }

    private void parseAll(Iterable<JsonData> entries)
    {
        for (JsonData data : entries)
        {
            try
            {
                ItemStack stack = ItemUtil.parseStringIntoItemStack(data.stack);
                ItemKey key = ItemKey.forStack(stack);

                int underlayColor = UnsignedInts.parseUnsignedInt(data.underlay, 16);
                int overlayColor = UnsignedInts.parseUnsignedInt(data.overlayColor, 16);

                ItemConfig config = new ItemConfig(new Color(underlayColor, true), data.overlay, new Color(overlayColor, true));

                if (configs.contains(key))
                {
                    int idx = configs.indexOf(key);
                    configs.set(idx, key);
                    configs.set(idx + 1, config);
                }
                else
                {
                    configs.add(key);
                    configs.add(config);
                }
            }
            catch (IllegalArgumentException e)
            {
                LogManager.getLogger(ColorblindHelper.MOD_NAME).info(data.stack + " could not be parsed into an ItemStack. Skipping...");
            }
        }
    }
    
    private static final ItemConfig DEFAULT = new ItemConfig(new Color(0, true), "", new Color(0, true));

    public ItemConfig getConfig(ItemStack stack)
    {
        int idx = configs.indexOf(ItemKey.forStack(stack));
        if (idx < 0)
        {
            return DEFAULT;
        }
        return (ItemConfig) configs.get(idx + 1);
    }

}
