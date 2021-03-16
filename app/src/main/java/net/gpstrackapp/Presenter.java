package net.gpstrackapp;

public interface Presenter {
    void onCreate();
    void onStart();
    void onPause();
    void onResume();
    void onStop();
    void onDestroy();
}
