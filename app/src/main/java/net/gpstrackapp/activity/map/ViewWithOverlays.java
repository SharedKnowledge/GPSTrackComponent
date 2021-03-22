package net.gpstrackapp.activity.map;

import org.osmdroid.views.overlay.Overlay;

public interface ViewWithOverlays {
    void addOverlay(Overlay overlay);
    void removeOverlay(Overlay overlay);
}
