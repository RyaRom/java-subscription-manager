package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;

public enum SubscriptionBotState implements BotState {
    REGISTRATION,
    SENDING
}
