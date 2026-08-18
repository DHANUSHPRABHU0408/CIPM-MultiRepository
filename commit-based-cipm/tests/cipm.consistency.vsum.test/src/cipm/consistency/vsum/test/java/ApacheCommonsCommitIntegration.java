package cipm.consistency.vsum.test.java;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import cipm.consistency.commitintegration.git.MultiRepositoryWrapper;
import cipm.consistency.commitintegration.git.GitRepositoryWrapper;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import cipm.consistency.commitintegration.CommitIntegration;
import cipm.consistency.commitintegration.CommitIntegrationFailureMode;
import cipm.consistency.commitintegration.CommitIntegrationState;
import cipm.consistency.commitintegration.lang.detection.strategy.ComponentDetectionStrategy;
import cipm.consistency.commitintegration.lang.java.JavaBuildFileBasedComDetectionStrategy;
import cipm.consistency.commitintegration.lang.java.JavaModelFacade;
import cipm.consistency.commitintegration.lang.java.JavaParserAndPropagatorUtils;
import cipm.consistency.commitintegration.lang.java.JavaParserAndPropagatorUtils.Configuration;
import cipm.consistency.cpr.javapcm.CommitIntegrationJavaPCMChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecification;

public class ApacheCommonsCommitIntegration extends JavaCommitIntegration {

	// repositories are cloned here, NOT under getRootPath() (target/ApacheCommonsTest)
	

	private static final String JAVA_FILE_EXTENSION = "java";
	private static final boolean DETECT_RENAMES = true;
	private CommitIntegrationFailureMode failureMode = CommitIntegrationFailureMode.ABORT;
	private Path rootPath;
	private final Path repositoriesRoot;
	private final Map<String, String> remoteRepositories;
	// cached so repositories aren't rebuilt/reinitialized on every call
	private MultiRepositoryWrapper multiRepositoryWrapper;
	
	public ApacheCommonsCommitIntegration(
	        Path root,
	        Path repositoriesRoot,
	        Map<String, String> remoteRepositories) {

	    this.rootPath = root;
	    this.repositoriesRoot = repositoriesRoot;
	    this.remoteRepositories =
	            new LinkedHashMap<>(remoteRepositories);
	}
	public ApacheCommonsCommitIntegration(Path root) {

	    this(
	        root,
	        Path.of("target", "apache-commons"),
	        Map.of(
	            "commons-cli",
	            "https://github.com/apache/commons-cli",

	            "commons-csv",
	            "https://github.com/apache/commons-csv",

	            "commons-exec",
	            "https://github.com/apache/commons-exec",

	            "commons-statistics",
	            "https://github.com/apache/commons-statistics"
	        )
	    );
	}

	public CommitIntegrationState<JavaModelFacade> getState() {
		return this.state;
	}
	
	@Override
	public Path getRootPath() {
		return this.rootPath;
	}

	@Override
	public CommitIntegrationFailureMode getFailureMode() {
		return this.failureMode;
	}

	@Override
	public void setFailureMode(CommitIntegrationFailureMode failureMode) {
		this.failureMode = failureMode;
	}

	@Override
	protected List<ComponentDetectionStrategy> getComponentDetectionStrategies() {
		return List.of(new JavaBuildFileBasedComDetectionStrategy(false));
	}

	@Override
	protected List<ChangePropagationSpecification> getJavaToPCMSpecs() {
		return List.of(
			new CommitIntegrationJavaPCMChangePropagationSpecification()
		);
	}
	
	@Override
	public void initialize(CommitIntegration<JavaModelFacade> commitIntegration)
			throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		super.initialize(commitIntegration);
		JavaParserAndPropagatorUtils.setConfiguration(new Configuration(false));
	}

	@Override
	public MultiRepositoryWrapper getMultiRepositoryWrapper()
	        throws InvalidRemoteException, TransportException,
	        GitAPIException, IOException {

	    if (multiRepositoryWrapper != null) {
	        return multiRepositoryWrapper;
	    }

	    List<GitRepositoryWrapper> repositories =
	            new ArrayList<>();

	    for (Map.Entry<String, String> entry
	            : remoteRepositories.entrySet()) {

	        String repo = entry.getKey();
	        String remoteUrl = entry.getValue();

	        Path repoLocation =
	                repositoriesRoot.resolve(repo);

	        System.out.println("--------------------------------");
	        System.out.println(
	                "Preparing repository: " + repo);
	        System.out.println(
	                "Location: " + repoLocation);
	        System.out.println("--------------------------------");

	        GitRepositoryWrapper wrapper =
	                new GitRepositoryWrapper(
	                        JAVA_FILE_EXTENSION,
	                        DETECT_RENAMES);

	        if (repoLocation.toFile().exists()) {

	            System.out.println(
	                    "Repository already exists. Opening: "
	                    + repo);

	            wrapper.withLocalDirectory(repoLocation);

	        } else {

	            System.out.println(
	                    "Repository does not exist. Cloning: "
	                    + repo);

	            if (remoteUrl == null) {
	                throw new IOException(
	                        "No remote repository configured for: "
	                        + repo);
	            }

	            wrapper.withRemoteRepositoryCopy(
	                    repoLocation,
	                    remoteUrl);
	        }

	        wrapper.initialize();

	        repositories.add(wrapper);
	    }

	    System.out.println();
	    System.out.println(
	            "Total repositories loaded = "
	            + repositories.size());
	    System.out.println();

	    multiRepositoryWrapper =
	            new MultiRepositoryWrapper(repositories);

	    return multiRepositoryWrapper;
	}
}