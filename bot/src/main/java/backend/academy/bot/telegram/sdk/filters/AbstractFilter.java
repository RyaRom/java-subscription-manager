package backend.academy.bot.telegram.sdk.filters;

import java.util.Map;
import java.util.function.Predicate;

@FunctionalInterface
public interface AbstractFilter<T> {
    Predicate<T> filter(Map<FilterParameter, Object> kwargs);
}
