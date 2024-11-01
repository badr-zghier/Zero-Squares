package util;

public class Time {
    // static variables are initialized at the start of the program
    public static float timeStarted = System.nanoTime();
    public static float getTime() {
        // get elapsed time in seconds
        return (float) ( (System.nanoTime() - timeStarted) * 1E-9);

    }
}
