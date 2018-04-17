package grimco.colorblindhelper;

import java.awt.Color;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.item.ItemStack;

public class ItemOverlayHandler
{
    public static void postRenderStack(FontRenderer fr, ItemStack stack, int x, int y)
    {
        ItemConfig config = ConfigReader.INSTANCE.getConfig(stack);
        String overlay = config.getOverlay();
        Color color = config.getOverlayColor();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 100);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        int strWidth = fr.getStringWidth(overlay);
        float scale = 1;
        if (strWidth > 16)
        {
            scale = 16f / strWidth;
        }
        if (scale < 1 && scale > 2 / 3f)
        {
            scale = 2 / 3f;
        }
     
        GlStateManager.disableBlend();
        GlStateManager.scale(scale, scale, 0);
        
        fr.drawStringWithShadow(overlay, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color.getRGB());
        
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1);
    }
}
