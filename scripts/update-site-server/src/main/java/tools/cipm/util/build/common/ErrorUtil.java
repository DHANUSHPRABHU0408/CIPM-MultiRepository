package tools.cipm.util.build.common;

public final class ErrorUtil {
    private ErrorUtil() {}

    public static void exitAfterError(Throwable t) {
        exitAfterError("", t);
    }

    public static void exitAfterError(String message, Throwable t) {
        exitAfterError(message, t, 1);
    }

    public static void exitAfterError(String message, Throwable t, int exitCode) {
        System.out.println(message);
        t.printStackTrace();
        System.exit(exitCode);
    }

    public static void checkForAndExitAfterFailure(String message, int code) {
        if (code != 0) {
            System.out.println(message);
            System.exit(code);
        }
    }
}
