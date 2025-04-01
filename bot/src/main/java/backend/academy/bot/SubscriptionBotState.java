package backend.academy.bot;

import backend.academy.bot.telegram.sdk.fsm.BotState;

public enum SubscriptionBotState implements BotState {
    WAITING_FOR_LINK,
    WAITING_FOR_TAGS,
    WAITING_FOR_FILTERS,
    WAITING_FOR_LINK_UNSUBSCRIBE
}
