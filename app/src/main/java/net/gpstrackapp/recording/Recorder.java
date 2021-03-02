package net.gpstrackapp.recording;

import net.gpstrackapp.location.ILocationConsumer;

public interface Recorder {
    void registerLocationConsumer(ILocationConsumer consumer);
    void unregisterLocationConsumer(ILocationConsumer consumer);
    void setLocationReceiver();
    void unsetLocationReceiver();
    boolean isRecording();
}
