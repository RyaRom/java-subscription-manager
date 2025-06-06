package backend.academy.resilience2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fallback {

    /** Fallback method name, Fallback should have same args and return type as parent method */
    String value();
}
