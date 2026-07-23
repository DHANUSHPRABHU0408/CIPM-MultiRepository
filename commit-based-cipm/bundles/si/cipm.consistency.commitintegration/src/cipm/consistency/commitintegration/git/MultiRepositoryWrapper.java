package cipm.consistency.commitintegration.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps multiple Git repositories and exposes them as a single logical unit.
 *
 * This class provides a simple abstraction for managing multiple
 * GitRepositoryWrapper instances.
 */
public class MultiRepositoryWrapper {

    private final List<GitRepositoryWrapper> repositories;

    public MultiRepositoryWrapper(List<GitRepositoryWrapper> repositories) {
        this.repositories = repositories != null
                ? new ArrayList<>(repositories)
                : new ArrayList<>();
    }

    /**
     * Returns an unmodifiable view of all repositories.
     */
    public List<GitRepositoryWrapper> getRepositories() {
        return Collections.unmodifiableList(repositories);
    }

    /**
     * Returns the number of repositories.
     */
    public int size() {
        return repositories.size();
    }

    /**
     * Returns true if no repositories exist.
     */
    public boolean isEmpty() {
        return repositories.isEmpty();
    }

    /**
     * Returns the repository at the specified index.
     */
    public GitRepositoryWrapper getRepository(int index) {
        return repositories.get(index);
    }

    /**
     * Adds a repository to this wrapper.
     */
    public void addRepository(GitRepositoryWrapper repository) {
        repositories.add(repository);
    }
}