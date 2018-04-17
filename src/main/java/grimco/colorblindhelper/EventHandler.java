package grimco.colorblindhelper;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = ColorblindHelper.MODID)
public class EventHandler
{
	@SubscribeEvent
	public static void onItemRenderPost(ItemGUIRenderEvent.Post event)
	{
		ItemOverlayHandler.postRenderStack(Minecraft.getMinecraft().fontRenderer, event.getStack(), event.getxPosition(), event.getyPosition());
	}
}
