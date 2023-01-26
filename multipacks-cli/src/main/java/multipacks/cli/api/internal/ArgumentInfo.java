/*
 * Copyright (c) 2022-2023 PhoMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package multipacks.cli.api.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import multipacks.cli.api.Command;
import multipacks.cli.api.annotations.Argument;

public class ArgumentInfo implements Comparable<ArgumentInfo> {
	public final Command command;
	public final Argument declared;
	public final Field field;
	public final Method method;
	public final Class<?> type;

	public ArgumentInfo(Command command, Argument declared, Field field) {
		this.command = command;
		this.declared = declared;
		this.field = field;
		this.method = null;
		this.type = field.getType();
	}

	public ArgumentInfo(Command command, Argument declared, Method method) {
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

	@Override
	public int compareTo(ArgumentInfo o) {
		return declared.value() - o.declared.value();
	}
}
