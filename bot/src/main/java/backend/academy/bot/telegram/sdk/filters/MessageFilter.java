package backend.academy.bot.telegram.sdk.filters;

import com.pengrad.telegrambot.model.Message;

@FunctionalInterface
public interface MessageFilter extends AbstractFilter<Message> {}
