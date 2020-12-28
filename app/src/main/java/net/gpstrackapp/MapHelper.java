package net.gpstrackapp;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.List;

public class MapHelper {
    public static void addToMap(MapView view, Overlay overlay) {
        view.getOverlayManager().add(overlay);
    }

    public static void removeFromMap(MapView view, Overlay overlay) {
        view.getOverlayManager().remove(overlay);
    }

    public static void addAllToMap(MapView view, List<Overlay> overlays) {
        view.getOverlayManager().addAll(overlays);
    }

    public static void removeAllFromMap(MapView view, List<Overlay> overlays) {
        view.getOverlayManager().removeAll(overlays);
    }
}
