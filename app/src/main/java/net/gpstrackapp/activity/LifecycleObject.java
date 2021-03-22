package net.gpstrackapp.activity;

public interface LifecycleObject {
    void onCreate();
    void onStart();
    void onPause();
    void onResume();
    void onStop();
    void onDestroy();
}
