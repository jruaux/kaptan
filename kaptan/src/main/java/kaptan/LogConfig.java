package kaptan;

public class LogConfig {

	private LogLevel level;
	private String filePath;
	private Integer fileLimit;
	private int fileCount;
	private boolean fileAppend;
	private LogLevel fileLevel;

	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getFileLimit() {
		return fileLimit;
	}

	public void setFileLimit(Integer fileLimit) {
		this.fileLimit = fileLimit;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public boolean isFileAppend() {
		return fileAppend;
	}

	public void setFileAppend(boolean fileAppend) {
		this.fileAppend = fileAppend;
	}

	public LogLevel getFileLevel() {
		return fileLevel;
	}

	public void setFileLevel(LogLevel fileLevel) {
		this.fileLevel = fileLevel;
	}

}
