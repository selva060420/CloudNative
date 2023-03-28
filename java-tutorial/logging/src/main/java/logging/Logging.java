package logging;

import java.util.logging.Logger;

//slf4j--> simple logging facade (need to add dependency for slf4j)  
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class Logging {
	private static final Logger log= Logger.getLogger(Logging.class.getName());
//	private static final Logger logger = LoggerFactory.getLogger(Logging.class);
	public static void testLogger() {
		log.severe("This is Severe logger");
		log.info("This is info logger");
		log.warning("This is warning logger");
	}
//	public void testLoggerForSLF4J() {
//		logger.debug("This is debug log");
//		logger.info("This is info log");
//		logger.error("This is error log");
//		logger.warn("This is warn log");
//		logger.trace("This is trace log");
//	}
	public static void main(String[] args) {
		Logging.testLogger();
	}
}

