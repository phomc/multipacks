package multipacks.cli.api.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate this field as argument or method as argument consumer.
 * @author nahkd
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD, METHOD })
public @interface Argument {
	/**
	 * The index of this argument in command line.
	 * @return Index of this argument in command line.
	 */
	public int value();

	/**
	 * Mark this argument as optional.
	 * @return Is this argument optional?
	 */
	public boolean optional() default false;
}
