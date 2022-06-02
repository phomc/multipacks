package multipacks.utils.logging;

public abstract class AbstractMPLogger {
	public abstract void info(Object... objs);
	public abstract void warning(Object... objs);
	public abstract void error(Object... objs);
	public abstract void critical(Object... objs);
}
