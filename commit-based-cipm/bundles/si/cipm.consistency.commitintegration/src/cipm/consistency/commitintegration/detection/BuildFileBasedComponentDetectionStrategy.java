package cipm.consistency.commitintegration.detection;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * A component discovery strategy which considers build and deployment
 * configuration files.
 * 
 * @author Martin Armbruster
 */
public class BuildFileBasedComponentDetectionStrategy implements ComponentDetectionStrategy {
	private static final String MAVEN_POM_FILE_NAME = "pom.xml";
	private static final String GRADLE_BUILD_FILE_NAME = "build.gradle";
	private static final String DOCKERFILE_FILE_NAME = "Dockerfile";
	private boolean checkForDockerfiles;
	
	public BuildFileBasedComponentDetectionStrategy() {
		this(true);
	}
	
	public BuildFileBasedComponentDetectionStrategy(boolean checkForDockerfiles) {
		this.checkForDockerfiles = checkForDockerfiles;
	}

	@Override
	public void detectComponent(Resource res, Path file, Path container, ModuleCandidates candidate) {
		Path parent = file.getParent();
		// Beginning with the Java file, the file system hierarchy is searched upwards
		// until the container directory is reached.
		while (container.compareTo(parent) != 0) {
			boolean buildFileExistence = checkSiblingExistence(parent, MAVEN_POM_FILE_NAME)
					|| checkSiblingExistence(parent, GRADLE_BUILD_FILE_NAME);
			boolean dockerFileExistence = checkSiblingExistence(parent, DOCKERFILE_FILE_NAME);
			if (buildFileExistence) {
				var relative = container.relativize(parent.getParent());
				String modName = relative.toString().replaceAll("/", ".");
				if (dockerFileExistence && this.checkForDockerfiles) {
					candidate.addModuleClassifier(ModuleState.MICROSERVICE_COMPONENT, modName, res);
				} else if (!this.checkForDockerfiles) {
					candidate.addModuleClassifier(ModuleState.REGULAR_COMPONENT, modName, res);
				} else {
					candidate.addModuleClassifier(ModuleState.COMPONENT_CANDIDATE, modName, res);
				}
				return;
			}
			parent = parent.getParent();
		}
	}

	private boolean checkSiblingExistence(Path file, String siblingName) {
		Path sibling = file.resolveSibling(siblingName);
		return Files.exists(sibling);
	}
}
