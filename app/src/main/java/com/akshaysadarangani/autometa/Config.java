package com.akshaysadarangani.autometa;

public class Config {

    private static final String PACKAGE_NAME = "com.akshaysadarangani.autometa";

    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "autometa_firebase";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 8888;
}