package net.gpstrackapp.overlay;

import org.osmdroid.views.overlay.Overlay;

import java.util.List;

public interface MapOverlay {
    List<Overlay> getComponentsToDisplay();
}
