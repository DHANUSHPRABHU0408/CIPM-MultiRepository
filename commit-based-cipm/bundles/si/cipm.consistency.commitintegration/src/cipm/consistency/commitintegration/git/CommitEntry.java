package cipm.consistency.commitintegration.git;

import org.eclipse.jgit.revwalk.RevCommit;

public class CommitEntry {

    private final String repositoryId;
    private final RevCommit commit;

    public CommitEntry(String repositoryId, RevCommit commit) {
        this.repositoryId = repositoryId;
        this.commit = commit;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public RevCommit getCommit() {
        return commit;
    }

    public String getCommitHash() {
        return commit.getId().getName();
    }
}