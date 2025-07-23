package tools.cipm.util.build.tasks;

public class FullCIPMBuild implements Runnable {
    public void run() {
        new InitialSetup().run();

        new PureCIPMBuild().run();
    }
}
