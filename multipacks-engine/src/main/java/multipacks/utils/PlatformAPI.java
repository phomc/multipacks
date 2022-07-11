package multipacks.utils;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Mark the API as platform-specific. When using API that is related to platform-specific, you have to be careful
 * at suppling inputs to the method or constructor. 
 * @author nahkd
 *
 */
@Retention(SOURCE)
@Target({ TYPE, METHOD })
public @interface PlatformAPI {
}
