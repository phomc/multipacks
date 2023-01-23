package multipacks.cli.api.annotations;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import multipacks.cli.api.Command;

/**
 * Annotate field with type {@link Command} as subcommand.
 * @author nahkd
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface Subcommand {
	public String value();
}
