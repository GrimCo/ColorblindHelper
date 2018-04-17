package grimco.colorblindhelper;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ColorblindHelper.MODID, name = ColorblindHelper.MOD_NAME, version = ColorblindHelper.VERSION, certificateFingerprint = ColorblindHelper.FINGERPRINT, clientSideOnly = true)
public class ColorblindHelper
{
    public static final String MODID = "colorblindhelper";
    public static final String MOD_NAME = "Colorblind Helper - GrimCo Edition";
    public static final String VERSION = "@VERSION@";
    public static final String FINGERPRINT = "@FINGER@";
    
    public static final Logger logger = LogManager.getLogger(MODID);
    
    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event)
    {
        if (!event.getSide().isClient())
        {
            throw new RuntimeException("Colorblind Helper is client-only!");
        }

        ConfigReader.INSTANCE.preInit(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        ConfigReader.INSTANCE.refresh();
    }
    
    @EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        logger.warn("Invalid fingerprint detected!");
    }
}
