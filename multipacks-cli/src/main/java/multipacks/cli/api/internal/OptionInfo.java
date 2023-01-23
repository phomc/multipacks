package multipacks.cli.api.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import multipacks.cli.api.Command;
import multipacks.cli.api.annotations.Option;

public class OptionInfo {
	public final Command command;
	public final Option declared;
	public final Field field;
	public final Method method;
	public final Class<?> type;

	public OptionInfo(Command command, Option declared, Field field) {
		this.command = command;
		this.declared = declared;
		this.field = field;
		this.method = null;
		this.type = field.getType();
	}

	public OptionInfo(Command command, Option declared, Method method) {
		this.command = command;
		this.declared = declared;
		this.field = null;
		this.method = method;
		this.type = method.getParameterTypes()[0];
	}

	public void set(String data) {
		try {
			Object val = Command.parse(data, type);
			if (field != null) field.set(command, val);
			if (method != null) method.invoke(command, val);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
