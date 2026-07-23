package cipm.consistency.commitintegration;

import cipm.consistency.commitintegration.git.GitRepositoryWrapper;
import cipm.consistency.commitintegration.git.MultiRepositoryWrapper;
import cipm.consistency.models.code.CodeModelFacade;
import com.google.common.base.Supplier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.views.changederivation.StateBasedChangeResolutionStrategy;

/**
 * Instances of this interface are used to initialize a
 * {@link CommitIntegrationState}.
 *
 * @param <CM>
 *            The code model class that is used for the integration
 *
 * @author Lukas Burgey
 */
public interface CommitIntegration<CM extends CodeModelFacade> {

    /**
     * @return The root path of this commit integration.
     */
    public Path getRootPath();

    /**
     * @return All change propagation specifications.
     */
    public List<ChangePropagationSpecification> getChangeSpecs();

    public StateBasedChangeResolutionStrategy getStateBasedChangeResolutionStrategy();

    /**
     * Legacy single repository support.
     */
    public GitRepositoryWrapper getGitRepositoryWrapper()
            throws InvalidRemoteException, TransportException, GitAPIException, IOException;

    /**
     * Returns all Git repositories participating in the commit integration.
     *
     * The default implementation wraps the legacy single
     * GitRepositoryWrapper inside a MultiRepositoryWrapper to
     * preserve backward compatibility.
     */
    public default MultiRepositoryWrapper getMultiRepositoryWrapper()
            throws InvalidRemoteException, TransportException, GitAPIException, IOException {
        GitRepositoryWrapper repository = getGitRepositoryWrapper();
        return new MultiRepositoryWrapper(List.of(repository));
    }

    /**
     * @return A supplier to instantiate the generic code model.
     */
    public Supplier<CM> getCodeModelFacadeSupplier();

    /**
     * Returns the failure mode for failing propagations.
     */
    public CommitIntegrationFailureMode getFailureMode();

    /**
     * Set the failure mode for failing propagations.
     */
    public void setFailureMode(CommitIntegrationFailureMode failureMode);
}