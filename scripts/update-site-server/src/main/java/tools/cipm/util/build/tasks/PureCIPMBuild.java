package tools.cipm.util.build.tasks;

import static tools.cipm.util.build.common.ErrorUtil.exitAfterError;

import java.nio.file.Path;
import java.nio.file.Paths;

import tools.cipm.util.build.common.MavenWrapperUtil;
import tools.cipm.util.build.common.UpdateSiteServer;

public class PureCIPMBuild implements Runnable {
    public void run() {
        Path root = Paths.get("..", "..");
        
        UpdateSiteServer server = new UpdateSiteServer();
        try {
            server.start();
            server.addDirectoryWithStaticContent("/vitruv", Paths.get("..", "..", "Vitruv", "releng", "cipm.consistency.vitruv.updatesite", "target", "repository"));
        } catch (Exception e) {
            exitAfterError("Could not start update site server:", e);
        }

        Path actualCipmRoot = root.resolve("commit-based-cipm");
        
        MavenWrapperUtil.copyMavenWrapper(root, actualCipmRoot);
        
        MavenWrapperUtil.executeMavenWrapper(actualCipmRoot, "clean verify -P run-one");
        
        try {
            server.addDirectoryWithStaticContent("/cipm", Paths.get("..", "..", "commit-based-cipm", "releng", "cipm.consistency.updatesite.fi", "target", "repository"));
        } catch (Exception e) {
            exitAfterError("Could not add update site from first build round to server:", e);
        }

        MavenWrapperUtil.executeMavenWrapper(actualCipmRoot, "clean verify -P run-two");

        try {
            server.addDirectoryWithStaticContent("/cipm2", Paths.get("..", "..", "commit-based-cipm", "releng", "cipm.consistency.updatesite.si", "target", "repository"));
        } catch (Exception e) {
            exitAfterError("Could not add update site from second build round to server:", e);
        }

        MavenWrapperUtil.executeMavenWrapper(actualCipmRoot, "clean verify -P run-three");

        MavenWrapperUtil.deleteMavenWrapper(actualCipmRoot);

        try {
            server.stop();
        } catch (Exception e) {
            exitAfterError("Could not stop update site server:", e);
        }
    }
}
