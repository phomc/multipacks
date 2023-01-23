package multipacks.cli.api.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate this field as option or method as option consumer.
 *
 * <p><b>Default values: </b>Values that's assigned in constructors are default values.</p>
 * @author nahkd
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD, METHOD })
public @interface Option {
	public String[] value();
}
