package net.gpstrackapp;

import android.content.Context;

import net.gpstrackapp.geomodel.track.TrackManager;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.android.apps.ASAPApplication;
import net.sharksystem.asap.android.apps.ASAPApplicationComponent;
import net.sharksystem.asap.android.apps.ASAPApplicationComponentHelper;
import net.sharksystem.asap.android.apps.ASAPComponentNotYetInitializedException;

public class GPSComponent implements ASAPApplicationComponent {
    private final ASAPApplicationComponentHelper asapComponentHelper;
    private static GPSComponent instance = null;
    //private final TrackManager trackManager;

    private GPSComponent(ASAPApplication asapApplication) {
        this.asapComponentHelper = new ASAPApplicationComponentHelper();
        this.asapComponentHelper.setASAPApplication(asapApplication);

        //this.trackManager = new TrackManager(asapApplication.getActivity().getApplicationContext());
        //this.trackManager = new TrackManager();
    }

    //TODO evtl. add AndroidASAPKeyStorage
    public static GPSComponent initialize(ASAPApplication asapApplication) {
        return GPSComponent.instance = new GPSComponent(asapApplication);
    }

    public static GPSComponent getGPSComponent() throws ASAPComponentNotYetInitializedException {
        if (instance == null) {
            throw new ASAPComponentNotYetInitializedException("GPSComponent not yet initialized");
        }
        return instance;
    }

    @Override
    public Context getContext() throws ASAPException {
        return this.asapComponentHelper.getContext();
    }

    @Override
    public ASAPApplication getASAPApplication() {
        return this.asapComponentHelper.getASAPApplication();
    }

    /*public TrackManager getTrackManager() {
        return trackManager;
    }*/

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
