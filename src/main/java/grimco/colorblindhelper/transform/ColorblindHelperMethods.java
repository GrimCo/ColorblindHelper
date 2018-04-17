package grimco.colorblindhelper.transform;

import grimco.colorblindhelper.ItemGUIRenderEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

public class ColorblindHelperMethods
{
	public static void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
		if (!stack.isEmpty()) {
			MinecraftForge.EVENT_BUS.post(new ItemGUIRenderEvent.Post(stack, xPosition, yPosition));
		}
	}
}
