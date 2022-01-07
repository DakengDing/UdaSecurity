module Security {
    requires miglayout;
    requires java.desktop;
    requires com.google.gson;
    requires java.prefs;
    requires com.google.common;

    opens com.udacity.catpoint.security.data to com.google.gson;
}