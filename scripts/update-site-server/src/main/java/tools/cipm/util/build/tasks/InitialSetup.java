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
        collectBaseSharedDependencies(root);
        
        //Path vitruvDir = root.resolve(Paths.get("commit-based-cipm", "bundles", "Vitruv"));
        //MavenWrapperUtil.copyMavenWrapper(Paths.get("."), vitruvDir);
        //MavenWrapperUtil.executeMavenWrapper(vitruvDir, "clean verify");
        //MavenWrapperUtil.deleteMavenWrapper(vitruvDir);
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

    private void collectBaseSharedDependencies(Path root) {
        Path depGeneratorDir = root.resolve(Paths.get("scripts", "dep-generator"));
        MavenWrapperUtil.copyMavenWrapper(Paths.get("."), depGeneratorDir);
        MavenWrapperUtil.executeMavenWrapper(depGeneratorDir, "package");
        MavenWrapperUtil.deleteMavenWrapper(depGeneratorDir);
    }
}
