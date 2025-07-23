package tools.cipm.util.build.common;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

public final class MavenWrapperUtil {
    private MavenWrapperUtil() {}

    private static final String MAVEN_WRAPPER_EXECUTABLE = "mvnw";
    private static final String MAVEN_WRAPPER_EXECUTABLE_WINDOWS = MAVEN_WRAPPER_EXECUTABLE + ".cmd";

    public static void copyMavenWrapper(Path root, Path target) {
        try {
            FileUtils.copyDirectory(root.resolve(".mvn").toFile(), target.resolve(".mvn").toFile());
            FileUtils.copyFile(root.resolve(MAVEN_WRAPPER_EXECUTABLE).toFile(), target.resolve(MAVEN_WRAPPER_EXECUTABLE).toFile());
            FileUtils.copyFile(root.resolve(MAVEN_WRAPPER_EXECUTABLE_WINDOWS).toFile(), target.resolve(MAVEN_WRAPPER_EXECUTABLE_WINDOWS).toFile());
        } catch (IOException e) {
            ErrorUtil.exitAfterError("Could not copy Maven wrapper files:", e);
        }
    }

    public static String getMavenWrapperCommand() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "cmd.exe " + MAVEN_WRAPPER_EXECUTABLE_WINDOWS;
        } else {
            return "bash " + MAVEN_WRAPPER_EXECUTABLE;
        }
    }

    public static void executeMavenWrapper(Path root, String commands) {
        int executionResult = 0;
        try {
            executionResult = DefaultExecutor
                .builder()
                .setWorkingDirectory(root)
                .get()
                .execute(CommandLine.parse(MavenWrapperUtil.getMavenWrapperCommand() + " " + commands));
        } catch (Exception e) {
            ErrorUtil.exitAfterError("Could not execute Maven:", e);
        }
        ErrorUtil.checkForAndExitAfterFailure("Maven build was not successful.", executionResult);
    }

    public static void deleteMavenWrapper(Path root) {
        try {
            FileUtils.deleteDirectory(root.resolve(".mvn").toFile());
            FileUtils.delete(root.resolve(MAVEN_WRAPPER_EXECUTABLE).toFile());
            FileUtils.delete(root.resolve(MAVEN_WRAPPER_EXECUTABLE_WINDOWS).toFile());
        } catch (IOException e) {
            ErrorUtil.exitAfterError("Could not delete Maven wrapper files:", e);
        }
    }
}
