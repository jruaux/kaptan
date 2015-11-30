package kaptan;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class LogFactory {

	public static Logger configure(LogConfig config) throws SecurityException, IOException {
		Logger logger = getRootLogger(config.getLevel());
		if (config.getFilePath() != null) {
			FileHandler fileHandler = getFileHandler(config.getFilePath(), config.getFileLimit(), config.getFileCount(),
					config.isFileAppend());
			fileHandler.setLevel(config.getFileLevel().getLevel());
			logger.addHandler(fileHandler);
		}
		return logger;
	}

	private static FileHandler getFileHandler(String pattern, Integer limit, int count, Boolean append)
			throws SecurityException, IOException {
		if (pattern == null) {
			return new FileHandler();
		}
		if (limit == null) {
			if (append == null) {
				return new FileHandler(pattern);
			}
			return new FileHandler(pattern, append);
		}
		if (append == null) {
			return new FileHandler(pattern, limit, count);
		}
		return new FileHandler(pattern, limit, count, append);
	}

	public static Logger getLog(Class<?> clazz) {
		return Logger.getLogger(clazz.getName());
	}

	public static Logger getRootLogger(LogLevel level) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tFT%1$tT.%1$tL][%4$s] %5$s %6$s%n");
		Logger rootLogger = getRootLogger();
		for (Handler handler : rootLogger.getHandlers()) {
			handler.setLevel(level.getLevel());
		}
		rootLogger.setLevel(level.getLevel());
		return rootLogger;
	}

	public static Logger getRootLogger() {
		return Logger.getLogger("");
	}

}