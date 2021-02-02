package net.gpstrackapp;

import net.gpstrackapp.geomodel.ILocationConsumer;

public interface Recorder {
    void registerLocationConsumer(ILocationConsumer consumer);
    void unregisterLocationConsumer(ILocationConsumer consumer);
    boolean isRecording();
}
