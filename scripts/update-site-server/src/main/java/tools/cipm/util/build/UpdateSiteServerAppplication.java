package tools.cipm.util.build;

import java.nio.file.Paths;

public class UpdateSiteServerAppplication {
    public static void main(String[] args) {
        UpdateSiteServer server = new UpdateSiteServer();
        try {
            server.start();
        } catch (Exception e) {
            System.out.println("Could not start update site server:");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            server.addDirectoryWithStaticContent("/vitruv", Paths.get("..", "..", "Vitruv", "releng", "cipm.consistency.vitruv.updatesite", "target", "repository"));
            server.addDirectoryWithStaticContent("/cipm", Paths.get("..", "..", "commit-based-cipm", "releng", "cipm.consistency.updatesite.fi", "target", "repository"));
            server.addDirectoryWithStaticContent("/cipm2", Paths.get("..", "..", "commit-based-cipm", "releng", "cipm.consistency.updatesite.si", "target", "repository"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
