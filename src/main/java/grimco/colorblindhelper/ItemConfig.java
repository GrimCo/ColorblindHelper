package grimco.colorblindhelper;

import java.awt.Color;

public class ItemConfig
{
    private Color underlay = Color.WHITE;
    private String overlay = "";
    private Color overlayColor = Color.WHITE;
    
    public ItemConfig(Color underlay, String overlay, Color overlayColor)
    {
        this.underlay = underlay;
        this.overlay = overlay;
        this.overlayColor = overlayColor;
    }
    
    public ItemConfig(Color underlay, String overlay)
    {
        this.underlay = underlay;
        this.overlay = overlay;
    }
    
    public Color getUnderlay()
    {
        return underlay;
    }
    
    public String getOverlay()
    {
        return overlay;
    }
    
    public Color getOverlayColor()
    {
        return overlayColor;
    }
    
}
