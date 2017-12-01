package innards.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Logging of a timestamped text file.
 * 
 * @author cchao
 *
 */
public class TextLogger {

	public static final String DEFAULT_PATH = "log/";
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
	public static final String sep = ":";
	
	protected String path;
	protected String logFilename;
	protected String prefix;
	protected PrintStream dataStream;
	protected boolean isLogging = false;
	protected boolean verbose = false;
	protected boolean filenameHasTimestamp = true;
	
	static Map<String, TextLogger> allLoggers = new HashMap<String, TextLogger>();
	
	public TextLogger(String path, String prefix) {
		this.path = path;
		this.prefix = prefix;
	}
	
	public static TextLogger getLogger(String prefix) {
		TextLogger logger = allLoggers.get(prefix);
		if (logger == null) {
			logger = new TextLogger(DEFAULT_PATH, prefix);
			allLoggers.put(prefix, logger);
		}
		return logger;
	}
	
	/**
	 * Open a data stream to start this log. The first line of the log is the time when the data 
	 * data stream was opened.
	 */
	public void startLogging() {
		if (! isLogging) {
			path = path.replaceFirst("~", System.getProperty("user.home")) + "/";
			path = path.replaceAll("//", "/");
			String ts = filenameHasTimestamp ? "_" + timeStamp() : "";
			new File(path).mkdirs();
			logFilename = path + "log_" + prefix + ts + ".txt"; 
			try {
				FileOutputStream logFile = new FileOutputStream(logFilename);
				dataStream = new PrintStream(logFile);
				isLogging = true;
				System.out.println("Opened log data stream: " + logFilename);
				log("");
			} catch(Exception ex){
				System.out.println("Could not open log data stream: " + logFilename);
				ex.printStackTrace();
			}
		}
	}
	
	public static void startLogging(String prefix) {
		TextLogger logger = allLoggers.get(prefix);
		if (logger == null) {
			logger = new TextLogger(DEFAULT_PATH, prefix);
			allLoggers.put(prefix, logger);
		}
		logger.startLogging();
	}
	
	/**
	 * Set the prefix for the log filename. The naming convention is log_PREFIX_TIMESTAMP.txt
	 * 
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * Set the directory in which the log file will reside.
	 * 
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Log the toString() of these objects with a timestamp.
	 * @param o
	 */
	public void log(Object... objs) {
		String line = timeStamp() + "";
		for (Object o : objs) {
			line += sep + (o == null? null : o.toString());
		}
		println(line);
	}
	
	/**
	 * Log the toString() of these objects for a specified timestamp.
	 * @param objs
	 * @param time
	 */
	public void log(long time, Object... objs) {
		String line = dateFormat.format(time) + "";
		for (Object o : objs) {
			line += sep + (o == null? null : o.toString());
		}
		println(line);
	}
	
	/**
	 * Log the toString() of these objects for a specified timestamp.
	 * @param objs
	 * @param time
	 */
	public void log(Date time, Object... objs) {
		String line = dateFormat.format(time) + "";
		for (Object o : objs) {
			line += sep + (o == null? null : o.toString());
		}
		println(line);
	}
	
	public static String makeString(Object... objs) {
		String line = "";
		for (Object o : objs) {
			line += sep + o;
		}
		return line.substring(1);
	}
	
	public void println(String line) {
		if (isLogging) {
			dataStream.println(line);
		}
		if (verbose) {
			System.out.println(line);
		}
	}
	
	/**
	 * Close the data stream for this log. The last line of the data stream is the time when 
	 * the data stream was closed.
	 */
	public void stopLogging() {
		if (isLogging) {
			log("");
			dataStream.close();
			dataStream = null;
			isLogging = false;
			System.out.println("Closed log data stream: " + logFilename);
		}
	}
	
	/**
	 * Set whether this logger should print log events to the system console.
	 * 
	 * @param bool
	 */
	public void setVerbose(boolean bool) {
		verbose = bool;
	}
	
	public static void setVerbose(String prefix, boolean bool) {
		TextLogger logger = allLoggers.get(prefix);
		if (logger != null) {
			logger.setVerbose(bool);
		}
	}
	
	/**
	 * Whether or not the logger is currently logging. When this is false, calling the log()
	 * function doesn't write to file. 
	 * 
	 * @return
	 */
	public boolean isLogging() {
		return isLogging;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getSeparator() {
		return sep;
	}
	
	public void setFilenameHasTimestamp(boolean bool) {
		filenameHasTimestamp = bool;
	}
	
	public static String timeStamp() {
		return dateFormat.format(new Date());
	}
}
