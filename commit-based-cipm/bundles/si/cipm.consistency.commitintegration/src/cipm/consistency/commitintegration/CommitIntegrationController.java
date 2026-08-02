package cipm.consistency.commitintegration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;

import cipm.consistency.commitintegration.git.GitRepositoryWrapper;
import cipm.consistency.commitintegration.git.MultiRepositoryWrapper;
// import cipm.consistency.commitintegration.lang.lua.runtimedata.ChangedResources; // TODO: Check if can be imported again after adding Lua model.
import cipm.consistency.models.code.CodeModelFacade;
import cipm.consistency.tools.evaluation.data.EvaluationDataContainer;
import cipm.consistency.vsum.Propagation;
import tools.vitruv.change.composite.description.PropagatedChange;

/**
 * This class is responsible for controlling the complete change propagation and adaptive
 * instrumentation.
 * 
 * @param <CM>
 *            The code model class that is used for the integration
 * 
 * @author Martin Armbruster
 * @author Lukas Burgey
 */
public abstract class CommitIntegrationController<CM extends CodeModelFacade> {
    private static final Logger LOGGER = Logger.getLogger(CommitIntegrationController.class.getName());
    protected CommitIntegrationState<CM> state;

    public void initialize(CommitIntegration<CM> commitIntegration)
            throws InvalidRemoteException, TransportException, IOException, GitAPIException {
        state = new CommitIntegrationState<CM>();
        state.initialize(commitIntegration);
    }

    /**
     * Disposes the integration state if it is not fresh
     * 
     * @throws InvalidRemoteException
     * @throws TransportException
     * @throws IOException
     * @throws GitAPIException
     */
    protected void reset() throws InvalidRemoteException, TransportException, IOException, GitAPIException {
        if (!state.isFresh()) {
            LOGGER.info("Resetting commitintegration");
            var ci = state.getCommitIntegration();
            state.getDirLayout()
                .delete();
            state.dispose();
            state.initialize(ci, ci.getRootPath(), true);
        }
    }

    /**
     * Reload the current integration state from disk
     * 
     * @throws GitAPIException
     * @throws IOException
     * @throws TransportException
     * @throws InvalidRemoteException
     */
    protected void reload() {
        var ci = state.getCommitIntegration();
        state.dispose();
        try {
            state.initialize(ci);
        } catch (IOException | GitAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Propagate the work tree that is currently checked out by the git repo wrapper.
     * 
     * @return The Propagation instance including the used model paths
     */
    public Optional<Propagation> propagateCurrentCheckout() {
        long totalStart = System.nanoTime();

        // run possible hooks
        long preHookStart = System.nanoTime();

        prePropagationHook();

        long preHookEnd = System.nanoTime();

        System.out.println(
            "Pre Hook : "
            + (preHookEnd-preHookStart)/1_000_000
            + " ms");

        LOGGER.info(String.format("\n\tPropagating commit #%d: %s", state.getSnapshotCount() + 1,
                state.getGitRepositoryWrapper()
                    .getCurrentCommitHash()));

        var previousParsedModelPath = state.getCurrentParsedModelPath();

        long workTreeStart = System.nanoTime();

        var workTree = state.getGitRepositoryWrapper()
            .getWorkTree()
            .toPath();

        long workTreeEnd = System.nanoTime();

        System.out.println(
            "Get WorkTree : "
            + (workTreeEnd-workTreeStart)/1_000_000
            + " ms");

        LOGGER.info("WorkTree = " + workTree);
        LOGGER.info("Parent   = " + workTree.getParent());

        // Parses the active repository's own work tree only. This intentionally feeds into
        // a single, SHARED VsumFacade/PCM model across all repositories (Option A / shared-VSUM
        // design) - each repository's parsed code model is propagated into the same VSUM in
        // sequence, rather than each repository getting its own isolated VSUM.
        long parseStart = System.nanoTime();

        var resource = state.getCodeModelFacade()
                .parseSourceCodeDir(workTree);

        long parseEnd = System.nanoTime();

        System.out.println(
            "Java parsing: "
            + (parseEnd - parseStart) / 1_000_000
            + " ms");
        if (resource == null) {
            LOGGER.error("Error parsing code model, not running propagation");

            long totalEnd = System.nanoTime();

            System.out.println(
                "TOTAL PROPAGATION : "
                + (totalEnd-totalStart)/1_000_000
                + " ms");

            return Optional.empty();
        }

        // this informs the ComponentSetInfoRegistry singleton that we changed resources which it
        // had mapped infos for
        // ChangedResources.setResourcesWereChanged(); // TODO: Check if can be activated again after adding Lua model.
        
        // reset evaluation data regarding the im update
        EvaluationDataContainer.get().resetImUpdateEval();

        long repoSnapshotStart = System.nanoTime();

        var previousRepositoryPath = state.createRepositorySnapshot();

        long repoSnapshotEnd = System.nanoTime();

        System.out.println(
            "Repository snapshot: "
            + (repoSnapshotEnd - repoSnapshotStart)/1_000_000
            + " ms");
        long parsedSnapshotStart = System.nanoTime();

        var parsedModelPath = state.createParsedCodeModelSnapshot();

        long parsedSnapshotEnd = System.nanoTime();

        System.out.println(
            "Parsed model snapshot: "
            + (parsedSnapshotEnd - parsedSnapshotStart)/1_000_000
            + " ms");
        state.setCurrentParsedModelPath(parsedModelPath);

        // DIAGNOSTIC: verify the shared PCM model is accumulating content across repositories
        // rather than being overwritten. If this design is working correctly, the snapshot
        // file size should generally grow (or at least not shrink to near-zero) after each
        // successive repository is propagated.
        if (previousRepositoryPath != null) {
            long snapshotSizeBytes = FileUtils.sizeOf(previousRepositoryPath.toFile());
            LOGGER.info(String.format("PCM repository snapshot size after this repo: %d bytes (%s)",
                    snapshotSizeBytes, previousRepositoryPath));
        }

        long propagationStart = System.nanoTime();

        // the actual propagation is done here
        var propagation = state.getVsumFacade()
            .propagateResource(resource, state.getDirLayout()
                .getVsumCodeModelURI());

        long propagationEnd = System.nanoTime();

        long propagationTime = (propagationEnd-propagationStart)/1_000_000;

        System.out.println(
            "VSUM propagation : "
            + propagationTime
            + " ms");
        EvaluationDataContainer.get()
            .getExecutionTimes()
            .setChangePropagationTime(propagationTime);

        var exception = propagation.getException();
        if (exception != null) {
            if (getFailureMode() == CommitIntegrationFailureMode.ABORT) {
                throw exception;
            }
        } else {
            // successful propagation
            state.setLastSuccessfulPropagation(propagation);
        }

        // DIAGNOSTIC: how many changes did this repository's propagation contribute to the
        // shared VSUM? Comparing this across repositories confirms each one is actually
        // being processed distinctly, not just re-propagating the same/previous model.
        LOGGER.info(String.format("Propagation for %s produced %d PropagatedChange(s)",
                state.getGitRepositoryWrapper()
                    .getCurrentCommitHash(),
                propagation.getChanges()
                    .size()));

        long evalStart = System.nanoTime();

        addChangeNumbersToEvaluationData(propagation.getChanges());

        long evalEnd = System.nanoTime();

        System.out.println(
            "Evaluation Data : "
            + (evalEnd-evalStart)/1_000_000
            + " ms");

        long snapshotStart = System.nanoTime();

        var snapshotPath = state.createSnapshot();

        long snapshotEnd = System.nanoTime();

        System.out.println(
            "Final snapshot: "
            + (snapshotEnd - snapshotStart)/1_000_000
            + " ms");

        // add some information needed for the evaluation to the propagation object
        propagation.setCommitIntegrationStateSnapshotPath(snapshotPath);
        propagation.setCommitIntegrationStateOriginalPath(state.getDirLayout()
            .getRootDirPath());
        propagation.setPreviousParsedCodeModelPath(previousParsedModelPath);
        propagation.setParsedCodeModelPath(parsedModelPath);
        propagation.setPreviousPcmRepositoryPath(previousRepositoryPath);

        // trigger some post propagation hooks
        long postStart = System.nanoTime();

        postPropagationHook();

        long postEnd = System.nanoTime();

        System.out.println(
            "Post Hook : "
            + (postEnd-postStart)/1_000_000
            + " ms");

        if (exception != null) {
            switch (getFailureMode()) {
            case BACKUP:
                var lastSuccessfulPropagation = state.getLastSuccessfulPropagation();
                if (lastSuccessfulPropagation != null) {
                    // overwrite the current state with a backup, as models may have been corrupted
                    // by the broken propagation
                    var backupPath = lastSuccessfulPropagation.getCommitIntegrationStateCopyPath();
                    var currentPath = state.getDirLayout()
                        .getRootDirPath();
                    LOGGER.info("Loading snapshot from last successful propagation: " + backupPath);
                    try {
                        FileUtils.copyDirectory(backupPath.toFile(), currentPath.toFile());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } // intentional fall through
            case RELOAD:
                LOGGER.info("Reloading commit integration state");
                long reloadStart = System.nanoTime();

                reload();

                long reloadEnd = System.nanoTime();

                System.out.println(
                    "Reload : "
                    + (reloadEnd-reloadStart)/1_000_000
                    + " ms");
                break;
            case CLEAN:
                try {
                    LOGGER.info("Resetting commit integration state");
                    var ci = state.getCommitIntegration();
                    state.getDirLayout()
                        .delete();
                    state.dispose();
                    long cleanStart = System.nanoTime();

                    state.initialize(ci, ci.getRootPath(), true);

                    long cleanEnd = System.nanoTime();

                    System.out.println(
                        "Clean Initialize : "
                        + (cleanEnd-cleanStart)/1_000_000
                        + " ms");
                } catch (IOException | GitAPIException e) {
                    e.printStackTrace();
                }
                break;
            default:
            }
        }

        long totalEnd = System.nanoTime();

        System.out.println();
        System.out.println("======================================");
        System.out.println("TOTAL PROPAGATION : "
                + (totalEnd-totalStart)/1_000_000
                + " ms");
        System.out.println("======================================");

        return Optional.of(propagation);
    }

    protected Optional<Propagation> propagateChanges(String firstCommitId, String secondCommitId)
            throws IncorrectObjectTypeException, IOException {
        if (!prePropagationChecks(firstCommitId, secondCommitId)) {
            LOGGER.info("Prechecks indicate no propagation is needed.");
            return Optional.empty();
        }

        var cs = EvaluationDataContainer.get()
            .resetChangeStatistic();
        cs.setOldCommit(firstCommitId);
        cs.setNewCommit(secondCommitId);
        cs.setNumberCommits(state.getGitRepositoryWrapper()
            .getAllCommitsBetweenTwoCommits(firstCommitId, secondCommitId)
            .size());

        // this computes diff data and puts it into the evaluation data
        state.getGitRepositoryWrapper()
            .computeDiffsBetweenTwoCommits(firstCommitId, secondCommitId);

        if (checkout(secondCommitId)) {
            var propagation = propagateCurrentCheckout();
            if (propagation.isPresent()) {
                propagation.get()
                    .setCommitId(secondCommitId);
            }
            return propagation;
        }

        return Optional.empty();
    }

    /**
     * Propagates changes for a given list of commitsIds. If no commitIds are given, the current
     * checkout of the git repo will be propagated. If there is one commitIds are given, it is
     * checked out and propagated to the state. If the first commitId is null, a fresh commit
     * integration state will be used for the commit integration. If the first commitId is not null,
     * it is expected that this commitId was the last propagated commitId of the commit integration
     * state
     * 
     * @param commitIds
     *            ids of the commits.
     * @throws GitAPIException
     *             if there is an exception within the Git usage.
     * @throws IOException
     *             if the repository cannot be read.
     */
    public List<Optional<Propagation>> propagateChanges(String... commitIds) throws GitAPIException, IOException {
        if (commitIds.length == 0) {
            return List.of();
        } else if (commitIds.length == 1 && commitIds[0] != null) {
            return List.of(propagateChanges(null, commitIds[0]));
        }

        // make sure the state is clean if the first id is null
        if (commitIds[0] == null) {
            reset();
        }

        var numberOfPropagations = commitIds.length - 1;
        List<Optional<Propagation>> allPropagations = new ArrayList<>(numberOfPropagations);

        for (var i = 0; i < numberOfPropagations; i++) {
            var propagation = propagateChanges(commitIds[i], commitIds[i + 1]);
            allPropagations.add(propagation);
        }
        return allPropagations;
    }

    /**
     * Propagates the current checkout for every repository managed by the commit integration
     * state. For each repository, the state's active {@link GitRepositoryWrapper} is switched to
     * that repository before propagating.
     * <p>
     * All repositories are propagated into the SAME shared {@code VsumFacade} / PCM model
     * (Option A). This matches the project's intended design: {@link MultiRepositoryWrapper}
     * and {@link GitRepositoryWrapper} stay separate git-level concerns, while the single
     * {@link CommitIntegrationState} continues to own one VSUM for the whole multi-repository
     * system.
     *
     * @return a list of {@link Propagation} results, one per repository, in the order the
     *         repositories were processed.
     */
    public List<Optional<Propagation>> propagateAllRepositories() {

        List<Optional<Propagation>> propagations = new ArrayList<>();

        for (GitRepositoryWrapper repository : state.getGitRepositoryWrappers()) {

            state.setGitRepositoryWrapper(repository);

            LOGGER.info("Processing repository: "
                    + repository.getRepository()
                            .getDirectory()
                            .getParentFile()
                            .getName());

            LOGGER.info("Repository: "
                    + repository.getRepository()
                            .getWorkTree()
                            .getAbsolutePath());

            System.out.println();
            System.out.println("==========================================");
            System.out.println("Repository : "
                    + repository.getRepository()
                            .getDirectory()
                            .getParentFile()
                            .getName());
            System.out.println("==========================================");

            long repoStart = System.nanoTime();

            Optional<Propagation> result = propagateCurrentCheckout();

            long repoEnd = System.nanoTime();

            System.out.println("Repository Total : "
                    + (repoEnd-repoStart)/1_000_000
                    + " ms");
            System.out.println("==========================================");
            System.out.println();

            propagations.add(result);
        }

        return propagations;
    }

    protected boolean prePropagationChecks(String firstCommitId, String secondCommitId) {
        if (firstCommitId != null) {
            return true;
        }
        LOGGER.debug("Obtaining all differences.");
        List<DiffEntry> diffs;
        try {
            diffs = state.getGitRepositoryWrapper()
                .computeDiffsBetweenTwoCommits(firstCommitId, secondCommitId);
        } catch (RevisionSyntaxException | IOException e) {
            e.printStackTrace();
            return false;
        }
        if (diffs.isEmpty()) {
            LOGGER.info("No source files changed between " + firstCommitId + " and " + secondCommitId + ".");
            return false;
        }
        return true;
    }

    private void addChangeNumbersToEvaluationData(List<PropagatedChange> changes) {
        var cs = EvaluationDataContainer.get()
            .getChangeStatistic();
        var totalChanges = 0;

        long loopStart = System.nanoTime();

        for (var change : changes) {
            var changeCount = change.getOriginalChange()
                .getEChanges()
                .size();
            totalChanges += changeCount;
            for (var modelDescriptor : change.getOriginalChange()
                .getAffectedEObjectsMetamodelDescriptors()) {
                for (var uri : modelDescriptor.getNsUris()) {
                    cs.setNumberVitruvChangesPerModel(uri, changeCount);
                }
            }
        }

        long loopEnd = System.nanoTime();

        System.out.println(
            "Evaluation Loop : "
            + (loopEnd-loopStart)/1_000_000
            + " ms");

        cs.setNumberVitruvChanges(totalChanges);
    }

    /**
     * Can be overwritten to do processing after every checkout
     * 
     * @return
     */
    protected boolean preprocessCheckout() {
        return true;
    }

    protected void prePropagationHook() {
        LOGGER.debug("Running Pre Propagation Hook");

    }

    protected void postPropagationHook() {
        LOGGER.debug("Running Post Propagation Hook");
        // reload models which may have changed
//        state.getPcmFacade().reload();
//        state.getImFacade().reload();
//        state.getVsumFacade().forceReload();
    }

    protected boolean checkout(String commitId) {
        LOGGER.debug("Checkout of " + commitId);
        try {
            state.getGitRepositoryWrapper()
                .checkout(commitId);
            if (!preprocessCheckout()) {
                LOGGER.debug("The preprocessing failed. Aborting.");
                return false;
            }
            return true;
        } catch (GitAPIException e) {
            LOGGER.error("Unable to checkout", e);
        }
        return false;
    }

    private CommitIntegrationFailureMode getFailureMode() {
        return state.getCommitIntegration()
            .getFailureMode();
    }
}