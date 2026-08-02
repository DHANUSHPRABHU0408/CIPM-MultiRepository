package cipm.consistency.vsum.test.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
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

public class ApacheCommonsTestController {
	private static final Logger LOGGER = Logger.getLogger(ApacheCommonsTestController.class);
	private CommitIntegrationState<JavaModelFacade> state;
	private ApacheCommonsCommitIntegration apacheCommonsController;
	
	private Path localRepositoriesDir = Paths.get("target", "apache-commons");
	private Map<String, String> repoIdToRemoteRepository;
	private Map<String, String> repoIdToCommitId;
	private Path rootPath = Paths.get("target", "ApacheCommonsTest");

    /**
     * 
     * @param overwrite
     *            Are existing files (models, etc.) to be deleted before initializing the commit
     *            integration state?
     * @throws GitAPIException
     * @throws IOException
     * @throws org.eclipse.jgit.api.errors.TransportException
     * @throws InvalidRemoteException
     */
    protected void setup(boolean overwrite) {
    	if (this.repoIdToRemoteRepository == null) {
    		// LinkedHashMap preserves insertion order, so repos are always
    		// processed/cloned/logged in the same order across runs.
    		this.repoIdToRemoteRepository = new LinkedHashMap<>();
    		this.repoIdToRemoteRepository.put("commons-cli", "https://github.com/apache/commons-cli");
    		this.repoIdToRemoteRepository.put("commons-csv", "https://github.com/apache/commons-csv");
    		this.repoIdToRemoteRepository.put("commons-exec", "https://github.com/apache/commons-exec");
    		// 
    		this.repoIdToRemoteRepository.put("commons-statistics", "https://github.com/apache/commons-statistics");

    		this.repoIdToCommitId = new LinkedHashMap<>();
    		this.repoIdToCommitId.put("commons-cli", "e17738b");
    		this.repoIdToCommitId.put("commons-csv", "e14ef8");
    		this.repoIdToCommitId.put("commons-exec", "3ee697");
    		this.repoIdToCommitId.put("commons-statistics", "2937eb");
    	}

    	// Clone/update repositories BEFORE initializing the commit integration state.
    	// getMultiRepositoryWrapper() (called during initialize()) checks that each
    	// repository directory already exists and throws otherwise, so this order
    	// is required on a clean machine.
    	prepareAllRepositories();

        // Create new empty state
        this.apacheCommonsController = new ApacheCommonsCommitIntegration(this.rootPath);

        // overwrite existing files?
        try {
        	CommitIntegrationSettingsContainer.initialize(Paths.get("apache-commons-exec-files", "settings.properties"));
        	this.apacheCommonsController.initialize(this.apacheCommonsController);
        	this.state = this.apacheCommonsController.getState();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
            failTest("Unable to setup commit integration state");
        }
    }

    @BeforeEach
    public void setup() {
        LoggingSetup.setMinLogLevel(Level.DEBUG);
        setup(false);
    }

    @BeforeAll
    public static void setupStatic() {
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("java", new JavaResource2Factory());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("javaxmi", new JavaResource2Factory());
    	InstrumentationModelPackage.eINSTANCE.eClass();
    }

    @AfterEach
    public void cleanupAfterTest() {
        state.dispose();
    }

    protected void failTest(String msg) {
        LOGGER.error(msg);
        Assert.fail(msg);
    }
    
    private void prepareAllRepositories() {
    	// The following things should be handled by the multi-repository support.
    	for (Map.Entry<String, String> entry : this.repoIdToRemoteRepository.entrySet()) {
    		var targetDir = this.localRepositoriesDir.resolve(entry.getKey());
    		try {
    			Git git;
    			if (Files.notExists(targetDir)) {
					git = Git
			    		.cloneRepository()
			    		.setDirectory(targetDir.toFile())
			    		.setURI(entry.getValue())
			    		.call();
    			} else {
    				git = Git.open(targetDir.toFile());
    			}
				git.checkout().setName(this.repoIdToCommitId.get(entry.getKey())).call();

		    	System.out.println("====================================");
		    	System.out.println("Repository : " + entry.getKey());
		    	System.out.println("Location   : " + targetDir);
		    	System.out.println("Commit     : " + this.repoIdToCommitId.get(entry.getKey()));
		    	System.out.println("HEAD       : " + git.getRepository().resolve("HEAD").name());
		    	System.out.println("====================================");

		    	git.getRepository().close();
		    	git.close();

			} catch (IOException | GitAPIException e) {
				this.failTest(e.getMessage());
			}
    	}
    }

    @Test
    public void testApacheCommons() {
    	var results = this.apacheCommonsController.propagateAllRepositories();

    	System.out.println("Repositories processed: " + results.size());

    	for (int i = 0; i < results.size(); i++) {
    	    System.out.println("Propagation " + (i + 1) + ": " + results.get(i));
    	}
    }
}