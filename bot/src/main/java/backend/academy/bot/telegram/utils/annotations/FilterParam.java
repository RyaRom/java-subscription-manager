package backend.academy.bot.telegram.utils.annotations;

import backend.academy.bot.telegram.utils.filters.FilterParameter;

public @interface FilterParam {
    FilterParameter key();

    String[] value();
}
