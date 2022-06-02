package multipacks.spigot;

import java.util.logging.Logger;
import java.util.stream.Stream;

import multipacks.utils.logging.AbstractMPLogger;

class JavaMPLogger extends AbstractMPLogger {
	private Logger logger;

	public JavaMPLogger(Logger logger) {
		this.logger = logger;
	}

	private String concat(Object... objs) {
		return String.join("", Stream.of(objs).map(v -> v.toString()).toArray(String[]::new));
	}

	@Override
	public void info(Object... objs) {
		logger.info(concat(objs));
	}

	@Override
	public void warning(Object... objs) {
		logger.warning(concat(objs));
	}

	@Override
	public void error(Object... objs) {
		logger.severe(concat(objs));
	}

	@Override
	public void critical(Object... objs) {
		logger.severe(concat(objs));
	}
}
