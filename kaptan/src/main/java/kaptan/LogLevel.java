package kaptan;

import java.util.logging.Level;

public enum LogLevel {

	ERROR(Level.SEVERE), WARNING(Level.WARNING), INFO(Level.INFO), DEBUG(Level.FINE), VERBOSE(Level.FINEST);

	public Level level;

	private LogLevel(Level level) {
		this.level = level;
	}

	public Level getLevel() {
		return level;
	}

}