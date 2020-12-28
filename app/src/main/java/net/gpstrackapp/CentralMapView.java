package net.gpstrackapp;

import android.content.Context;
import android.util.Log;

import org.osmdroid.views.MapView;

public class CentralMapView extends MapView {
    private static CentralMapView instance = null;

    private CentralMapView(Context context) {
        super(context);
    }

    public static CentralMapView init(Context context) {
        if (CentralMapView.instance == null)
            CentralMapView.instance = new CentralMapView(context);
        Log.d("ApplicationContext:", String.valueOf(context));
        return CentralMapView.instance;
    }

    public static CentralMapView getInstance() {
        return CentralMapView.instance;
    }
}
