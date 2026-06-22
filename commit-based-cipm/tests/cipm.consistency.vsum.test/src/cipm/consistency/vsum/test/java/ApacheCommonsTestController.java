package cipm.consistency.vsum.test.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
    		this.repoIdToRemoteRepository = new HashMap<>();
    		this.repoIdToRemoteRepository.put("commons-csv", "https://github.com/apache/commons-csv");
    		this.repoIdToRemoteRepository.put("commons-exec", "https://github.com/apache/commons-exec");
    		this.repoIdToRemoteRepository.put("commons-cli", "https://github.com/apache/commons-cli");
    		this.repoIdToRemoteRepository.put("commons-statistics", "https://github.com/apache/commons-statistics");
    		this.repoIdToCommitId = new HashMap<>();
    		this.repoIdToCommitId.put("commons-csv", "e14ef8");
    		this.repoIdToCommitId.put("commons-exec", "3ee697");
    		this.repoIdToCommitId.put("commons-cli", "d74613");
    		this.repoIdToCommitId.put("commons-statistics", "2937eb");
    	}
        // Create new empty state
        this.apacheCommonsController = new ApacheCommonsCommitIntegration(this.rootPath);

        // overwrite existing files?
        try {
        	CommitIntegrationSettingsContainer.initialize(Paths.get("apache-commons-exec-files", "settings.properties"));
        	this.apacheCommonsController.initialize(this.apacheCommonsController);
        	this.state = this.apacheCommonsController.getState();        	
            // state.initialize(this.teammatesController, this.teammatesController.getRootPath(), overwrite);
            if (Files.exists(this.localRepositoriesDir)) {
            	// Initialize the repositories within this directory.
            } else {
            	// Initialize the repositories with the remote repository locations.
            }
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
    	this.repoIdToRemoteRepository.entrySet().forEach(entry -> {
    		var targetDir = this.localRepositoriesDir.resolve(entry.getKey());
    		try {
    			Git git;
    			if (Files.notExists(targetDir)) {
					Files.createDirectories(targetDir);
					git = Git
			    		.cloneRepository()
			    		.setDirectory(targetDir.toFile())
			    		.setURI(entry.getValue())
			    		.call();
    			} else {
    				git = Git.open(targetDir.toFile());
    			}
				git.checkout().setName(this.repoIdToCommitId.get(entry.getKey())).call();
		    	git.getRepository().close();
		    	git.close();
		    	
			} catch (IOException | GitAPIException e) {
				this.failTest(e.getMessage());
			}
    	});
    	var targetDir = this.localRepositoriesDir.resolve(this.repoIdToRemoteRepository.keySet().stream().findAny().get());
    	try {
			this.state.getGitRepositoryWrapper().withLocalDirectory(targetDir);
			this.state.getGitRepositoryWrapper().initialize();
		} catch (IOException | GitAPIException e) {
			this.failTest(e.getMessage());
		}
    }

    @Test
    public void testApacheCommons() {
    	this.prepareAllRepositories();
    	var result = this.apacheCommonsController.propagateCurrentCheckout();
    	System.out.println(result.get());
    }
}
