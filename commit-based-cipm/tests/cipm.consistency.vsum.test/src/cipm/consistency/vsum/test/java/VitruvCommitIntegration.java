package cipm.consistency.vsum.test.java;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import cipm.consistency.commitintegration.CommitIntegration;
import cipm.consistency.commitintegration.lang.java.JavaModelFacade;
import cipm.consistency.commitintegration.lang.java.JavaParserAndPropagatorUtils;
import cipm.consistency.commitintegration.lang.java.JavaParserAndPropagatorUtils.Configuration;
import cipm.consistency.cpr.javapcm.CommitIntegrationJavaPCMChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecification;

public class VitruvCommitIntegration extends TEAMMATESCommitIntegration {
	public VitruvCommitIntegration(Path root) {
		super(root);
	}

	@Override
	public void initialize(CommitIntegration<JavaModelFacade> commitIntegration)
			throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		super.initialize(commitIntegration);
		JavaParserAndPropagatorUtils.setConfiguration(new Configuration(true));
	}
	
	@Override
	protected List<ChangePropagationSpecification> getJavaToPCMSpecs() {
		return List.of(new CommitIntegrationJavaPCMChangePropagationSpecification());
	}
}
