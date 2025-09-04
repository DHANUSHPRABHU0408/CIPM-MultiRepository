package cipm.consistency.vsum.test;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cipm.consistency.commitintegration.JavaParserAndPropagatorUtils;
import cipm.consistency.commitintegration.JavaParserAndPropagatorUtils.Configuration;
import cipm.consistency.commitintegration.detection.BuildFileBasedComponentDetectionStrategy;
import cipm.consistency.cpr.javapcm.CommitIntegrationJavaPCMChangePropagationSpecification;
import cipm.consistency.tools.evaluation.data.EvaluationDataContainer;
import cipm.consistency.tools.evaluation.data.EvaluationDataContainerReaderWriter;
import tools.vitruv.framework.propagation.ChangePropagationSpecification;

public class VitruvTest extends AbstractCITest {
	@Override
	protected String getTestPath() {
		return "target" + File.separator + "vitruv";
	}

	@Override
	protected String getRepositoryPath() {
		return "";
	}

	@Override
	protected String getSettingsPath() {
		return "vitruv-exec-files" + File.separator + "settings.properties";
	}

	@Override
	protected ChangePropagationSpecification getJavaPCMSpecification() {
		return new CommitIntegrationJavaPCMChangePropagationSpecification();
	}

	@Override
	protected String getReferenceRepositoryModelDirectoryName() {
		return null;
	}
	
	@BeforeAll
	public static void setEverythingUp() {
		JavaParserAndPropagatorUtils.setConfiguration(new Configuration(false, new BuildFileBasedComponentDetectionStrategy(false)));
	}
	
	@Test
	public void testVitruv() {
		var resultContainer = new EvaluationDataContainer();
		resultContainer.setNumberOfPropagation(0);
		EvaluationDataContainer.setGlobalContainer(resultContainer);
		var millis = System.currentTimeMillis();
		this.controller.getCommitChangePropagator().propagateCurrentState();
		millis = System.currentTimeMillis() - millis;
		resultContainer.getExecutionTimes().setOverallTime(millis);
		EvaluationDataContainerReaderWriter.write(resultContainer, Paths.get("target", "eval.json"));
	}
}
