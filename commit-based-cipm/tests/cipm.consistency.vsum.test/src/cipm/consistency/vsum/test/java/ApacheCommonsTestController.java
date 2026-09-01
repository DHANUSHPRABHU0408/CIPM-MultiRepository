package cipm.consistency.vsum.test.java;

import java.io.IOException;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.revwalk.RevCommit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cipm.consistency.commitintegration.PropagationTimingProvider;
import cipm.consistency.commitintegration.CommitIntegrationState;
import cipm.consistency.commitintegration.lang.java.JavaModelFacade;
import cipm.consistency.commitintegration.settings.CommitIntegrationSettingsContainer;
import cipm.consistency.vsum.test.appspace.LoggingSetup;
import jamopp.resource.JavaResource2Factory;
import tools.cipm.models.instrumentation.InstrumentationModel.InstrumentationModelPackage;


public class ApacheCommonsTestController 
{
    private static final Logger LOGGER =
            Logger.getLogger(ApacheCommonsTestController.class);
    
    private CommitIntegrationState<JavaModelFacade> state;
    private ApacheCommonsCommitIntegration apacheCommonsController;

    private static final Path PERFORMANCE_CSV =
            Paths.get("target", "performance-results.csv");
    private static final Path FINEGRAINED_CSV =
            Paths.get("target", "finegrained-propagation-results.csv");
    private Path rootPath =
            Paths.get("target", "ApacheCommonsTest");

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
    }

    protected void failTest(String msg) {

        LOGGER.error(msg);

        Assert.fail(msg);
    }

    @Test
    public void testApacheCommons() throws Exception {

        initializePerformanceCsv();
        initializeFinegrainedCsv();

        Map<String, String> commits =
                new LinkedHashMap<>();

        commits.put(
                "commons-cli",
                "0a68ae0");

        commits.put(
                "commons-csv",
                "d9b9f06");

        commits.put(
                "commons-exec",
                "1f5061b");

        commits.put(
                "commons-statistics",
                "b101916");

        runPerformanceTest(
                "Test Case 1",
                commits,
                createFourRepositories(),
                Paths.get(
                        "target",
                        "ApacheCommonsTest1"));
    }
    
    @Test
    public void testApacheCommonsTest2() throws Exception {

        initializePerformanceCsv();
        initializeFinegrainedCsv();

        Map<String, String> commits =
                new LinkedHashMap<>();

        commits.put(
                "commons-cli",
                "c9e543d");

        commits.put(
                "commons-csv",
                "c3844a2");

        commits.put(
                "commons-exec",
                "92d9943");

        commits.put(
                "commons-statistics",
                "d390942");

        runPerformanceTest(
                "Test Case 2",
                commits,
                createFourRepositories(),
                Paths.get(
                        "target",
                        "ApacheCommonsTest2"));
    }
    
    @Test
    public void testApacheCommonsTest3() throws Exception {

        initializePerformanceCsv();
        initializeFinegrainedCsv();

        Map<String, String> commits =
                new LinkedHashMap<>();

        commits.put(
                "commons-cli",
                "6dadc46");

        commits.put(
                "commons-csv",
                "e0e80b4");

        commits.put(
                "commons-exec",
                "513fa7a");

        commits.put(
                "commons-statistics",
                "6737df3");

        runPerformanceTest(
                "Test Case 3",
                commits,
                createFourRepositories(),
                Paths.get(
                        "target",
                        "ApacheCommonsTest3"));
    }
    
    @Test
    public void testApacheCommonsTest4() throws Exception {

        initializePerformanceCsv();
        initializeFinegrainedCsv();

        Map<String, String> commits =
                new LinkedHashMap<>();

        commits.put(
                "commons-cli",
                "6dadc46");

        commits.put(
                "commons-csv",
                "e0e80b4");

        commits.put(
                "commons-exec",
                "513fa7a");

        commits.put(
                "commons-statistics",
                "6737df3");

        commits.put(
                "commons-bcel",
                "7056283");

        runPerformanceTest(
                "Test Case 4",
                commits,
                createFiveRepositories(),
                Paths.get(
                        "target",
                        "ApacheCommonsTest4"));
    }
    @Test
    public void testApacheCommonsTest5() throws Exception {

        initializePerformanceCsv();
        initializeFinegrainedCsv();

        Map<String, String> commits =
                new LinkedHashMap<>();

        commits.put(
                "commons-cli",
                "6dadc46");

        commits.put(
                "commons-csv",
                "e0e80b4");

        commits.put(
                "commons-exec",
                "513fa7a");

        commits.put(
                "commons-statistics",
                "6737df3");

        commits.put(
                "commons-bcel",
                "1fcbc87");

        runPerformanceTest(
                "Test Case 5",
                commits,
                createFiveRepositories(),
                Paths.get(
                        "target",
                        "ApacheCommonsTest5"));
    }
    @Test
    public void testApacheCommonsTest6() throws Exception {

        initializePerformanceCsv();
        initializeFinegrainedCsv();

        Map<String, String> commits =
                new LinkedHashMap<>();

        commits.put(
                "commons-cli",
                "c9e543d");

        commits.put(
                "commons-csv",
                "c3844a2");

        commits.put(
                "commons-exec",
                "92d9943");

        commits.put(
                "commons-statistics",
                "d390942");

        commits.put(
                "commons-bcel",
                "1fcbc87");

        runPerformanceTest(
                "Test Case 6",
                commits,
                createFiveRepositories(),
                Paths.get(
                        "target",
                        "ApacheCommonsTest6"));
    }
    
    @Test
    public void testApacheCommonsTest7() throws Exception {

        initializePerformanceCsv();
        initializeFinegrainedCsv();

        Map<String, String> commits =
                new LinkedHashMap<>();

        commits.put(
                "commons-cli",
                "0a68ae0");

        commits.put(
                "commons-csv",
                "d9b9f06");

        commits.put(
                "commons-exec",
                "92d9943");

        commits.put(
                "commons-statistics",
                "d390942");

        commits.put(
                "commons-bcel",
                "1fcbc87");

        runPerformanceTest(
                "Test Case 7",
                commits,
                createFiveRepositories(),
                Paths.get(
                        "target",
                        "ApacheCommonsTest7"));
    }
    
    private void initializePerformanceCsv() throws IOException {

        Files.createDirectories(
                PERFORMANCE_CSV.getParent());

        if (Files.exists(PERFORMANCE_CSV)) {
            return;
        }

        try (BufferedWriter writer =
                Files.newBufferedWriter(
                        PERFORMANCE_CSV,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE)) {

        	writer.write(
        	        "Test Case,Repository,Commit ID,Commit Date,"
        	        + "Commit Size (Java LOC),"
        	        + "Total Commit Size (Test Case),"
        	        + "Generate Change Time (ms),"
        	        + "Propagated Changes Time (ms),"
        	        + "Total VSUM Propagation (ms),"
        	        + "Total Test Case Runtime (ms)");

            writer.newLine();
        }
    }

    private void initializeFinegrainedCsv() throws IOException {

        Files.createDirectories(
                FINEGRAINED_CSV.getParent());

        if (Files.exists(FINEGRAINED_CSV)) {
            return;
        }

        try (BufferedWriter writer =
                Files.newBufferedWriter(
                        FINEGRAINED_CSV,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE)) {

            writer.write(
                    "Test Case,Repository,Commit ID,"
                    + "Change Type,Propagation Method,"
                    + "Execution Count,"
                    + "Total Individual Execution Time (ms),"
                    + "Average Individual Execution Time (ms)");

            writer.newLine();
        }
    }

    private void writePerformanceRow(
            String testCaseName,
            String repositoryName,
            String commitId,
            RevCommit commit,
            long commitSize,
            long totalCommitSize,
            long totalGenerateChangeTime,
            long totalPropagatedChangesTime,
            long totalVsumTime,
            long totalTestCaseRuntime)
            throws IOException {

        String commitDate =
                commit.getCommitterIdent()
                        .getWhen()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try (BufferedWriter writer =
                Files.newBufferedWriter(
                        PERFORMANCE_CSV,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {

            writer.write(
                    testCaseName + ","
                    + repositoryName + ","
                    + commitId + ","
                    + commitDate + ","
                    + commitSize + ","
                    + totalCommitSize + ","
                    + totalGenerateChangeTime + ","
                    + totalPropagatedChangesTime + ","
                    + totalVsumTime + ","
                    + totalTestCaseRuntime);

            writer.newLine();
        }
    }

    private void writeFinegrainedRows(
            String testCaseName,
            String repositoryName,
            String commitId,
            List<PropagationTimingProvider.PropagationTiming> timings)
            throws IOException {

        Map<String, FinegrainedAggregate> aggregates =
                new LinkedHashMap<>();

        for (PropagationTimingProvider.PropagationTiming timing : timings) {

            String key =
                    timing.getChangeType()
                    + "||"
                    + timing.getMethodName();

            FinegrainedAggregate aggregate =
                    aggregates.get(key);

            if (aggregate == null) {
                aggregate = new FinegrainedAggregate();
                aggregates.put(key, aggregate);
            }

            aggregate.count++;
            aggregate.totalTimeMs += timing.getExecutionTimeMs();
        }

        try (BufferedWriter writer =
                Files.newBufferedWriter(
                        FINEGRAINED_CSV,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {

            for (Map.Entry<String, FinegrainedAggregate> entry :
                    aggregates.entrySet()) {

            	String[] keyParts = entry.getKey().split(
            	        "\\|\\|",
            	        2);

                String changeType = keyParts[0];
                String methodName = keyParts[1];

                FinegrainedAggregate aggregate =
                        entry.getValue();

                double average =
                        aggregate.totalTimeMs / aggregate.count;

                writer.write(
                        testCaseName + ","
                        + repositoryName + ","
                        + commitId + ","
                        + changeType + ","
                        + methodName + ","
                        + aggregate.count + ","
                        + aggregate.totalTimeMs + ","
                        + average);

                writer.newLine();
            }
        }
    }

    private static class FinegrainedAggregate {

        long count = 0;
        double totalTimeMs = 0.0;
    }

    private void runPerformanceTest(
            String testCaseName,
            Map<String, String> commits,
            Map<String, String> repositories,
            Path testRoot)
            throws Exception {

        initializePerformanceCsv();
        initializeFinegrainedCsv();

        this.repoIdToCommitId = new LinkedHashMap<>(commits);

        this.apacheCommonsController =
                new ApacheCommonsCommitIntegration(
                        testRoot,
                        Paths.get(
                                "target",
                                testCaseName.replace(" ", "")),
                        repositories);
        CommitIntegrationSettingsContainer.initialize(
                Paths.get(
                        "apache-commons-exec-files",
                        "settings.properties"));

        this.apacheCommonsController.initialize(
                this.apacheCommonsController);

        this.state =
                this.apacheCommonsController.getState();

        long testCaseStart = System.nanoTime();

        var results =
                this.apacheCommonsController
                        .propagateChanges(commits);

        long testCaseEnd = System.nanoTime();

        long totalTestCaseRuntime =
                (testCaseEnd - testCaseStart) / 1_000_000;

        Assert.assertEquals(
                "Expected one result for each repository",
                commits.size(),
                results.size());

        var timings =
                this.apacheCommonsController
                        .getLastRepositoryPropagationTimes();
        var detailedTimings =
                this.apacheCommonsController
                        .getLastRepositoryPropagationTimings();

        long totalVsumPropagation = 0;
        long totalGenerateChangeTime = 0;
        long totalPropagatedChangesTime = 0;

        for (long[] timing : timings.values()) {

            totalVsumPropagation += timing[0];

            totalGenerateChangeTime += timing[2];

            totalPropagatedChangesTime += timing[3];
        }
        var repositories1 =
                this.apacheCommonsController
                        .getMultiRepositoryWrapper()
                        .getRepositories();
        Map<String, Long> commitSizes =
                new LinkedHashMap<>();

        long totalCommitSize = 0;

        for (var repository : repositories1) {

            String repositoryName =
                    repository.getWorkTree().getName();

            String commitId =
                    commits.get(repositoryName);

            if (commitId == null) {
                throw new IllegalStateException(
                        "No commit configured for "
                        + repositoryName);
            }

            long commitSize =
                    repository.getJavaSourceLineCount(commitId);

            commitSizes.put(
                    repositoryName,
                    commitSize);

            totalCommitSize += commitSize;
        }

        for (var repository : repositories1) {

            String repositoryName =
                    repository.getWorkTree().getName();

            String commitId =
                    commits.get(repositoryName);

            if (commitId == null) {
                throw new IllegalStateException(
                        "No commit configured for "
                        + repositoryName);
            }

            long[] timing =
                    timings.get(repositoryName);

            if (timing == null) {
                throw new IllegalStateException(
                        "No timing data for "
                        + repositoryName);
            }

            RevCommit commit =
                    repository.getCommitForId(commitId);

            long commitSize =
                    commitSizes.get(repositoryName);

            List<PropagationTimingProvider.PropagationTiming>
            repositoryDetailedTimings =
                    detailedTimings.get(repositoryName);

            if (repositoryDetailedTimings == null) {
                repositoryDetailedTimings = List.of();
            }

            writePerformanceRow(
                    testCaseName,
                    repositoryName,
                    commitId,
                    commit,
                    commitSize,
                    totalCommitSize,
                    totalGenerateChangeTime,
                    totalPropagatedChangesTime,
                    totalVsumPropagation,
                    totalTestCaseRuntime);

            writeFinegrainedRows(
                    testCaseName,
                    repositoryName,
                    commitId,
                    repositoryDetailedTimings);
        }

        System.out.println(
                testCaseName
                + " completed. Repositories processed: "
                + results.size());
    }
    private Map<String, String> createFourRepositories() {

        Map<String, String> repositories =
                new LinkedHashMap<>();

        repositories.put(
                "commons-cli",
                "https://github.com/apache/commons-cli");

        repositories.put(
                "commons-csv",
                "https://github.com/apache/commons-csv");

        repositories.put(
                "commons-exec",
                "https://github.com/apache/commons-exec");

        repositories.put(
                "commons-statistics",
                "https://github.com/apache/commons-statistics");

        return repositories;
    }
    private Map<String, String> createFiveRepositories() {

        Map<String, String> repositories =
                new LinkedHashMap<>(createFourRepositories());

        repositories.put(
                "commons-bcel",
                "https://github.com/apache/commons-bcel");

        return repositories;
    }
}