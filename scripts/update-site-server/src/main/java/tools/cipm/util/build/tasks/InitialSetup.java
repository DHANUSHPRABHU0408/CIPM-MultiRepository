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

import tools.cipm.util.build.common.MavenWrapperUtil;

import static tools.cipm.util.build.common.ErrorUtil.exitAfterError;
import static tools.cipm.util.build.common.ErrorUtil.checkForAndExitAfterFailure;

public class InitialSetup implements Runnable {
    public void run() {
        Path root = Paths.get("..", "..");

        checkoutGitSubmodules(root);
        collectCIPMPipelineDependencies(root);
        copyCIPMPipelinePlugins(root);
        
        Path vitruvDir = root.resolve("Vitruv");
        MavenWrapperUtil.copyMavenWrapper(Paths.get("."), vitruvDir);
        MavenWrapperUtil.executeMavenWrapper(vitruvDir, "clean verify");
        MavenWrapperUtil.deleteMavenWrapper(vitruvDir);
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
}
