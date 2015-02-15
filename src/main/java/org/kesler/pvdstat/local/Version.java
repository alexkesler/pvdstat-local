package org.kesler.pvdstat.local;

/**
 * Класс для хранения версии приложения
 */
public abstract class Version {

    private static String version = "0.1.0.0";
    private static String releaseDate = "16.02.2015";

    public static String getVersion() {
        return version;
    }

    public static String getReleaseDate() {
        return releaseDate;
    }
}
