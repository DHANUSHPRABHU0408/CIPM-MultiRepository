package cipm.consistency.vsum.test.java;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cipm.consistency.commitintegration.CommitIntegrationState;
import cipm.consistency.commitintegration.lang.java.JavaModelFacade;
import cipm.consistency.commitintegration.settings.CommitIntegrationSettingsContainer;
import cipm.consistency.vsum.test.appspace.LoggingSetup;
import jamopp.resource.JavaResource2Factory;
import tools.cipm.models.instrumentation.InstrumentationModel.InstrumentationModelPackage;

public class ApacheCommonsTestController 
{
	private CommitIntegrationState<JavaModelFacade> test2State;
    private static final Logger LOGGER =
            Logger.getLogger(ApacheCommonsTestController.class);

    private CommitIntegrationState<JavaModelFacade> state;
    private ApacheCommonsCommitIntegration apacheCommonsController;
    private ApacheCommonsCommitIntegration test2Controller;


    private Path rootPath =
            Paths.get("target", "ApacheCommonsTest");
    private Path test2RootPath =
            Paths.get("target", "ApacheCommonsTest2");

    private Map<String, String> repoIdToCommitId;
    

    /**
     * Setup the commit integration state.
     */
    protected void setup(boolean overwrite) {

        /*
         * Define which commit should be used for each repository.
         *
         * Repository cloning/opening is handled by
         * ApacheCommonsCommitIntegration.
         */
        this.repoIdToCommitId = new LinkedHashMap<>();

        this.repoIdToCommitId.put(
                "commons-cli",
                "e17738b");

        this.repoIdToCommitId.put(
                "commons-csv",
                "e14ef8");

        this.repoIdToCommitId.put(
                "commons-exec",
                "b25039f");

        this.repoIdToCommitId.put(
                "commons-statistics",
                "2937eb");

        /*
         * Create the commit integration controller.
         */
        this.apacheCommonsController =
                new ApacheCommonsCommitIntegration(this.rootPath);

        try {

            CommitIntegrationSettingsContainer.initialize(
                    Paths.get(
                            "apache-commons-exec-files",
                            "settings.properties"));

            this.apacheCommonsController.initialize(
                    this.apacheCommonsController);

            this.state =
                    this.apacheCommonsController.getState();

        } catch (IOException | org.eclipse.jgit.api.errors.GitAPIException e) {

            e.printStackTrace();

            failTest(
                    "Unable to setup commit integration state");
        }
    }

    @BeforeEach
    
    public void setup() {
        LoggingSetup.setMinLogLevel(Level.DEBUG);
    }

    @BeforeAll
    public static void setupStatic() {

        Resource.Factory.Registry.INSTANCE
                .getExtensionToFactoryMap()
                .put(
                        "java",
                        new JavaResource2Factory());

        Resource.Factory.Registry.INSTANCE
                .getExtensionToFactoryMap()
                .put(
                        "javaxmi",
                        new JavaResource2Factory());

        InstrumentationModelPackage.eINSTANCE.eClass();
    }

    @AfterEach
    public void cleanupAfterTest() {

        if (state != null) {
            state.dispose();
        }

        if (test2State != null) {
            test2State.dispose();
        }
    }

    protected void failTest(String msg) {

        LOGGER.error(msg);

        Assert.fail(msg);
    }

    @Test
    public void testApacheCommons() throws Exception {

        setup(false);

        var results =
                this.apacheCommonsController
                        .propagateChanges(repoIdToCommitId);

        Assert.assertEquals(
                "Expected one result for each repository",
                repoIdToCommitId.size(),
                results.size());

        System.out.println(
                "Repositories processed: " + results.size());

        for (int i = 0; i < results.size(); i++) {

            System.out.println(
                    "Propagation "
                    + (i + 1)
                    + ": "
                    + results.get(i));
        }
    }
  
    
    
    private Map<String, String> createTest2Repositories() {

        Map<String, String> repositories =
                new LinkedHashMap<>();

        repositories.put(
                "commons-codec",
                "https://github.com/apache/commons-cli");

        repositories.put(
                "commons-lang",
                "https://github.com/apache/commons-csv");

        repositories.put(
                "commons-io",
                "https://github.com/apache/commons-exec");

        repositories.put(
                "commons-compress",
                "https://github.com/apache/commons-statistics");

        return repositories;
    }
        private Map<String, String> createTest2Commits() {

            Map<String, String> commits =
                    new LinkedHashMap<>();

            commits.put(
                    "commons-codec",
                    "81b5f76");

            commits.put(
                    "commons-lang",
                    "7b2f013");

            commits.put(
                    "commons-io",
                    "461d834");

            commits.put(
                    "commons-compress",
                    "17bf305");

            return commits;
        }
        private void setupTest2() throws Exception {

            Map<String, String> repositories =
                    createTest2Repositories();

            test2Controller =
                    new ApacheCommonsCommitIntegration(
                            test2RootPath,
                            Paths.get(
                                    "target",
                                    "apache-commons-test2"),
                            repositories);

            CommitIntegrationSettingsContainer.initialize(
                    Paths.get(
                            "apache-commons-exec-files",
                            "settings.properties"));

            test2Controller.initialize(
                    test2Controller);

            test2State =
                    test2Controller.getState();
        }
        
        @Test
        public void testApacheCommonsTest2() throws Exception {

            setupTest2();

            var commits =
                    createTest2Commits();

            System.out.println();
            System.out.println("========================================");
            System.out.println("TEST 2 - NEW REPOSITORIES");
            System.out.println("========================================");

            for (var entry : commits.entrySet()) {

                System.out.println(
                        entry.getKey()
                        + " -> "
                        + entry.getValue());
            }

            var results =
                    test2Controller
                            .propagateChanges(commits);

            Assert.assertEquals(
                    "Expected one result for each repository",
                    commits.size(),
                    results.size());

            System.out.println(
                    "Repositories processed: "
                    + results.size());

            for (int i = 0; i < results.size(); i++) {

                System.out.println(
                        "Propagation "
                        + (i + 1)
                        + ": "
                        + results.get(i));
            }
        }
    }
