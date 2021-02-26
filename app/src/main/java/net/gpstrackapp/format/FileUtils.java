package net.gpstrackapp.format;

public class FileUtils {
    private static final char[] invalidFileNameChars = "|\\?*<\":>+[]/'".toCharArray();

    public static char[] getInvalidChars() {
        return invalidFileNameChars;
    }

    public static boolean isValidFileName(String fileName) {
        for (char c : invalidFileNameChars) {
            if (fileName.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }
}
