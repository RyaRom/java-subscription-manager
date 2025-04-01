package backend.academy.bot.repository;

import backend.academy.bot.telegram.sdk.fsm.BotState;
import backend.academy.bot.telegram.sdk.fsm.DefaultStates;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class UserCache {
    @Builder.Default
    private BotState botState = DefaultStates.NONE;

    @Builder.Default
    private String link = "";

    @Builder.Default
    private String tags = "";

    @Builder.Default
    private String filters = "";
}
