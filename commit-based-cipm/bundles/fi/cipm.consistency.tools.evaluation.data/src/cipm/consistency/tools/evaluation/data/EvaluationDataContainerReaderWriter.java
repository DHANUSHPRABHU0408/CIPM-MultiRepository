package cipm.consistency.tools.evaluation.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.google.gson.Gson;

/**
 * This class enables the reading and writing of the evaluation data.
 * 
 * @author Martin Armbruster
 */
public final class EvaluationDataContainerReaderWriter {
	private EvaluationDataContainerReaderWriter() {
	}

	/**
	 * Reads evaluation data from a file.
	 * 
	 * @param file the file from which the data is read.
	 * @return the read data.
	 */
	public static EvaluationDataContainer read(Path file) {
		if (Files.notExists(file)) {
			return null;
		}
		
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			var data = readAll(file);
			if (data != null && data.length > 0) {
				return data[data.length - 1];
			}
		} catch (IOException e) {
		}
		
		return null;
	}
	
	public static EvaluationDataContainer[] readAll(Path file) {
		if (Files.notExists(file)) {
			return null;
		}
		
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			return new Gson().fromJson(reader, EvaluationDataContainer[].class);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the evaluation data to a file.
	 * 
	 * @param result the data to write.
	 * @param file   the file in which the data is written.
	 */
	public static void write(EvaluationDataContainer result, Path file) {
		EvaluationDataContainer[] toWrite;
		if (Files.exists(file)) {
			toWrite = readAll(file);
			boolean overwritten = false;
			for (var idx = 0; idx < toWrite.length; idx++) {
				if (toWrite[idx].getChangeStatistic().getOldCommit().equals(result.getChangeStatistic().getOldCommit())
						&& toWrite[idx].getChangeStatistic().getNewCommit().equals(result.getChangeStatistic().getNewCommit())) {
					toWrite[idx] = result;
					overwritten = true;
					break;
				}
			}
			if (!overwritten) {
				toWrite = Arrays.copyOf(toWrite, toWrite.length + 1);
				toWrite[toWrite.length - 1] = result;
			}
		} else {
			toWrite = new EvaluationDataContainer[] { result };
		}
		
		Gson gson = new Gson();
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			gson.toJson(toWrite, EvaluationDataContainer[].class, gson.newJsonWriter(writer));
		} catch (IOException e) {
		}
	}
}
