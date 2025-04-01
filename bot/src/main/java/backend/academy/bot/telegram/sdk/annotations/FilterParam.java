package backend.academy.bot.telegram.sdk.annotations;

import backend.academy.bot.telegram.sdk.filters.FilterParameter;

public @interface FilterParam {
    FilterParameter key();

    String[] value();
}
