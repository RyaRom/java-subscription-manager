package backend.academy.resilience2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// trigger after everything is done
@Order(Ordered.LOWEST_PRECEDENCE)
public @interface Fallback {
    /** Fallback method name */
    String value();
}
