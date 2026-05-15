package cipm.consistency.vsum.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cipm.consistency.cpr.javapcm.CommitIntegrationJavaPCMChangePropagationSpecification;
import tools.vitruv.framework.propagation.ChangePropagationSpecification;

/**
 * A test class for the TeaStore.
 * 
 * @author Martin Armbruster
 */
public class TeaStoreCITest extends AbstractCITest {
	private static final Logger LOGGER = Logger.getLogger("cipm." + TeaStoreCITest.class.getSimpleName());
	private static final String COMMIT_TAG_1_0 = "b0d046e178dbaab7e045de57c01795ce5d1dac92";
	private static final String COMMIT_TAG_1_1 = "77733d9c6ab6680c6cc460c631cd408a588a595c";
	private static final String COMMIT_TAG_1_2 = "53c6efa1dca64a87e536d8c5a3dcc3c12ad933b5";
	private static final String COMMIT_TAG_1_2_1 = "f8f13f4390f80d3dc8adb0a6167938a688ddb45e";
	private static final String COMMIT_TAG_1_3 = "745469e55fad8a801a92b0be96dc009acbe7e3fb";
	private static final String COMMIT_TAG_1_3_1 = "de69e957597d20d4be17fc7db2a0aa2fb3a414f7";
	private static final List<String> INTERVAL_BORDERS = List.of(COMMIT_TAG_1_0, COMMIT_TAG_1_1, COMMIT_TAG_1_2,
			COMMIT_TAG_1_2_1, COMMIT_TAG_1_3, COMMIT_TAG_1_3_1);
	private Path pcmPool;
	private String interval = "";

	@Override
	protected String getTestPath() {
		return "target" + File.separator + "TeaStoreTest";
	}

	@Override
	protected String getRepositoryPath() {
		return "https://github.com/DescartesResearch/TeaStore";
	}

	@Override
	protected String getSettingsPath() {
		return "teastore-exec-files" + File.separator + "settings.properties";
	}
	
	@Override
	protected String getReferenceRepositoryModelDirectoryName() {
		return "88c4015eef95daf39b60e7c8a8fed1ca4a4f8a57" + File.separator + interval;
	}
	
	@BeforeEach
	public void setUp() {
		// The super.setUp is directly called by the test methods for explicit control. Therefore, nothing should be done.
	}
	
	@AfterEach
	public void tearDown() {
		// The super.tearDown is directly called by the test methods for explicit control. Therefore, nothing should be done.
	}
	
	private List<String> propagateMultipleCommits(String firstCommit, String lastCommit, int start)
			throws Exception {
		super.setUp();
		List<String> successfulCommits = new ArrayList<>();
		var commits = convertToStringList(this.controller.getCommitChangePropagator().getWrapper()
				.getAllCommitsBetweenTwoCommits(firstCommit, lastCommit));
		commits.add(0, firstCommit);
		int startIndex = start;
		var oldCommit = commits.get(startIndex);
		successfulCommits.add(oldCommit);
		super.tearDown();
		for (int idx = startIndex + 1; idx < commits.size(); idx++) {
			var newCommit = commits.get(idx);
			super.setUp();
			boolean result = executePropagationAndEvaluation(oldCommit, newCommit, idx);
			super.tearDown();
			if (result) {
				super.setUp();
				copyPcmRepository(idx, oldCommit + "-" + newCommit);
				performIndependentEvaluation();
				super.tearDown();
				oldCommit = newCommit;
				successfulCommits.add(oldCommit);
			}
			Thread.sleep(5000);
		}
		return successfulCommits;
	}
	
	private List<String> convertToStringList(List<RevCommit> commits) {
		List<String> result = new ArrayList<>();
		for (RevCommit com : commits) {
			result.add(com.getId().getName());
		}
		return result;
	}

	@Test
	public void testTeaStore() throws Exception {
		pcmPool = Paths.get(this.getTestPath(), "pcm-pool");
		Files.createDirectories(this.pcmPool);
		Path modelDir = Paths.get(this.getTestPath(), "model-data");
		Path tempModelDir = Paths.get(this.getTestPath(), "temp-models");
		List<List<String>> commitsInIntervals = new ArrayList<>();
		
		for (int intervalIdx = 0; intervalIdx < INTERVAL_BORDERS.size() - 1; intervalIdx++) {
			String intervalStart = INTERVAL_BORDERS.get(intervalIdx);
			String intervalEnd = INTERVAL_BORDERS.get(intervalIdx + 1);
			this.interval = "I" + intervalIdx;
			
			// Propagate and evaluate initial commit.
			super.setUp();
			executePropagationAndEvaluation(null, intervalStart, 0);
			copyPcmRepository(0, intervalStart);
			super.tearDown();
			super.setUp();
			performIndependentEvaluation();
			super.tearDown();
			FileUtils.copyDirectory(modelDir.toFile(), tempModelDir.toFile());
			
			// Propagate and evaluate all changes between interval borders.
			super.setUp();
			executePropagationAndEvaluation(intervalStart, intervalEnd, 1);
			copyPcmRepository(-1, intervalStart + "-" + intervalEnd);
			super.tearDown();
			super.setUp();
			performIndependentEvaluation();
			super.tearDown();
			FileUtils.deleteDirectory(modelDir.toFile());
			FileUtils.copyDirectory(tempModelDir.toFile(), modelDir.toFile());
			FileUtils.deleteDirectory(tempModelDir.toFile());
			
			// Propagate and evaluate all single commits within the interval borders.
			var successfulCommits = propagateMultipleCommits(intervalStart, intervalEnd, 0);
			commitsInIntervals.add(successfulCommits);
			FileUtils.deleteDirectory(modelDir.toFile());
		}
	}
	
	private void copyPcmRepository(int propagationNo, String commitId) throws IOException {
		FileUtils.copyFile(
			this.controller.getVSUMFacade().getFileLayout().getPcmRepositoryPath().toFile(),
			this.pcmPool.resolve(
				"pmca-"
				+ this.interval
				+ "-"
				+ propagationNo
				+ "-"
				+ commitId
				+ ".repository"
			).toFile()
		);
	}
	
	@Override
	protected ChangePropagationSpecification getJavaPCMSpecification() {
		return new CommitIntegrationJavaPCMChangePropagationSpecification();
	}
}
