package grimco.colorblindhelper.transform;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(1001)
public class ColorblindHelperPlugin implements IFMLLoadingPlugin
{
	public static boolean isRuntineDeobfEnabled = false;
	
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] {"grimco.colorblindhelper.transform.ColorblindHelperTransformer"};
	}
	
	@Override
	public String getModContainerClass()
	{
		return null;
	}
	
	@Nullable
	@Override
	public String getSetupClass()
	{
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data)
	{
		isRuntineDeobfEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}
	
	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
