package cipm.consistency.vsum.test.java;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cipm.consistency.cpr.javapcm.CommitIntegrationJavaPCMChangePropagationSpecification;
import cipm.consistency.tools.evaluation.data.EvaluationDataContainer;
import cipm.consistency.tools.evaluation.data.EvaluationDataContainerReaderWriter;

public class VitruvTest {
	@Test
	public void testVitruv() {
		var resultContainer = new EvaluationDataContainer();
		//resultContainer.setNumberOfPropagation(0);
		EvaluationDataContainer.set(resultContainer);
		var controller = new VitruvCommitIntegration(Paths.get("target", "Vitruv"));
		var millis = System.currentTimeMillis();
		// controller.propagateChanges(null);
		millis = System.currentTimeMillis() - millis;
		resultContainer.getExecutionTimes().setOverallTime(millis);
		EvaluationDataContainerReaderWriter.write(resultContainer, Paths.get("target", "eval.json"));
	}
}
