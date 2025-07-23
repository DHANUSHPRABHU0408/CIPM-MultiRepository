package tools.cipm.util.build.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.Git;

public class InitialSetup implements Runnable {
    public void run() {
        Path root = Paths.get("..", "..");

        checkoutGitSubmodules(root);
        collectCIPMPipelineDependencies(root);
        copyCIPMPipelinePlugins(root);
        
        Path vitruvDir = root.resolve("Vitruv");
        copyMavenWrapper(root, vitruvDir);
        buildVitruv(vitruvDir);
        deleteMavenWrapper(vitruvDir);
    }

    private void exitAfterError(Throwable t) {
        exitAfterError("", t);
    }

    private void exitAfterError(String message, Throwable t) {
        exitAfterError(message, t, 1);
    }

    private void exitAfterError(String message, Throwable t, int exitCode) {
        System.out.println(message);
        t.printStackTrace();
        System.exit(exitCode);
    }

    private void checkForAndExitAfterFailure(String message, int code) {
        if (code != 0) {
            System.out.println(message);
            System.exit(code);
        }
    }

    private void checkoutGitSubmodules(Path root) {
        try {
            Git git = Git.open(root.resolve(".git").toFile());
            git.submoduleInit().call();
            git.submoduleUpdate().call();
            git.getRepository().close();
            git.close();
        } catch (Exception e) {
            exitAfterError("Could not initialize the submodules:", e);
        }
    }

    private void collectCIPMPipelineDependencies(Path root) {
        int executionResult = 0;
        try {
            executionResult = DefaultExecutor
                .builder()
                .setWorkingDirectory(root.resolve(Paths.get("CIPM-Pipeline", "cipm.consistency.bridge.eclipse", "cipm.consistency.base.shared", "dep-generator")))
                .get()
                .execute(CommandLine.parse(getGradleWrapperCommand() + " bundle copyBundles"));
        } catch (IOException e) {
            exitAfterError("Could not copy dependencies for CIPM-Pipeline:", e);
        }
        checkForAndExitAfterFailure("Setting up the dependencies failed.", executionResult);
    }

    private String getGradleWrapperCommand() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "cmd.exe gradlew.bat";
        } else {
            return "bash gradlew";
        }
    }

    private void copyCIPMPipelinePlugins(Path root) {
        try {
            List<Path> subDirectories = Files
                .list(root.resolve(Paths.get("CIPM-Pipeline", "cipm.consistency.bridge.eclipse")))
                .filter((path) -> Files.isDirectory(path) && Files.exists(path))
                .collect(Collectors.toList());
            for (Path subDir : subDirectories) {
                FileUtils.copyDirectoryToDirectory(
                    subDir.toFile(),
                    root.resolve(Paths.get("commit-based-cipm", "bundles", "fi")).toFile()
                );
            }
        } catch (IOException e) {
            exitAfterError("Could not copy CIPM-Pipeline plugins:", e);
        }
    }

    private void buildVitruv(Path root) {
        int executionResult = 0;
        try {
            executionResult = DefaultExecutor
                .builder()
                .setWorkingDirectory(root)
                .get()
                .execute(CommandLine.parse(getMavenWrapperCommand() + " clean verify"));
        } catch (Exception e) {
            exitAfterError("Could not build Vitruv:", e);
        }
        checkForAndExitAfterFailure("Vitruv build was not successful.", executionResult);
    }

    private static final String MAVEN_WRAPPER_EXECUTABLE = "mvnw";
    private static final String MAVEN_WRAPPER_EXECUTABLE_WINDOWS = MAVEN_WRAPPER_EXECUTABLE + ".cmd";

    private void copyMavenWrapper(Path root, Path target) {
        try {
            FileUtils.copyDirectory(root.resolve(".mvn").toFile(), target.resolve(".mvn").toFile());
            FileUtils.copyFile(root.resolve(MAVEN_WRAPPER_EXECUTABLE).toFile(), target.resolve(MAVEN_WRAPPER_EXECUTABLE).toFile());
            FileUtils.copyFile(root.resolve(MAVEN_WRAPPER_EXECUTABLE_WINDOWS).toFile(), target.resolve(MAVEN_WRAPPER_EXECUTABLE_WINDOWS).toFile());
        } catch (IOException e) {
            exitAfterError("Could not copy Maven wrapper files:", e);
        }
    }

    private String getMavenWrapperCommand() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "cmd.exe " + MAVEN_WRAPPER_EXECUTABLE_WINDOWS;
        } else {
            return "bash " + MAVEN_WRAPPER_EXECUTABLE;
        }
    }

    private void deleteMavenWrapper(Path root) {
        try {
            FileUtils.deleteDirectory(root.resolve(".mvn").toFile());
            FileUtils.delete(root.resolve(MAVEN_WRAPPER_EXECUTABLE).toFile());
            FileUtils.delete(root.resolve(MAVEN_WRAPPER_EXECUTABLE_WINDOWS).toFile());
        } catch (IOException e) {
            exitAfterError("Could not delete Maven wrapper files:", e);
        }
    }
}
