package cipm.consistency.commitintegration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

/**
 * A utility class for executing external commands.
 * 
 * @author Martin Armbruster
 */
public final class ExternalCommandExecutionUtils {
	private static final Logger LOGGER = Logger
			.getLogger("cipm." + ExternalCommandExecutionUtils.class.getSimpleName());
	private static final String ENV_NAME_PATH = SystemUtils.IS_OS_WINDOWS ? "Path" : "PATH";
	private static final String SCRIPT_EXTENSION = SystemUtils.IS_OS_WINDOWS ? ".bat" : ".sh";

	private ExternalCommandExecutionUtils() {
	}

	/**
	 * Runs an external script. This method appends an appropriate file extension to the script file
	 * based on the operating system (.bat for Windows and .sh for Linux).
	 * It also checks if the script file exists.
	 * 
	 * @param directory directory in which the script shall run.
	 * @param command   the script to run.
	 * @return true if the script was successfully executed or the script file does not exist. false otherwise.
	 */
	public static boolean runScript(File directory, String command) {
		int result = -1;
		
		Path possibleScriptFile = Paths.get(command + SCRIPT_EXTENSION).toAbsolutePath();
		if (Files.notExists(possibleScriptFile)) {
			LOGGER.debug("Location '" + command + "' for script not found.");
			return true;
		}
		
		LOGGER.debug("Executing " + command);
		if (SystemUtils.IS_OS_WINDOWS) {
			result = internalRunScript(directory, "cmd.exe", "/c", "\"" + possibleScriptFile.toString() + "\"");
		} else {
			result = internalRunScript(directory, "/bin/bash", "-c", "\"" + possibleScriptFile.toString() + "\"");
		}
		return result == 0;
	}

	private static int internalRunScript(File directory, String... command) {
		try {
			ProcessBuilder builder = new ProcessBuilder().directory(directory).inheritIO().command(command);
			var javaBinDir = System.getProperty("java.home") + File.separator + "bin";
			builder.environment().put(ENV_NAME_PATH, javaBinDir + File.pathSeparator + builder.environment().get(ENV_NAME_PATH));
			Process process = builder.start();
			return process.waitFor();
		} catch (IOException | InterruptedException e) {
			return -1;
		}
	}
}
