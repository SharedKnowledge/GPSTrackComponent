package net.gpstrackapp;

import org.osmdroid.views.overlay.Overlay;

import java.util.List;

public interface MapObject {
    List<Overlay> getComponentsToDisplay();
}
