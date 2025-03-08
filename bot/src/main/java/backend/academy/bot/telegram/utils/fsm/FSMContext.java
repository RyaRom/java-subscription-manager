package backend.academy.bot.telegram.utils.fsm;

import backend.academy.bot.repository.UserCache;
import backend.academy.bot.repository.UserDataCacheRepository;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Log4j2
@RequiredArgsConstructor
public class FSMContext {
    private final UserDataCacheRepository userDataCacheRepository;

    public String getCurrentStateName(Long id) {
        var user = userDataCacheRepository.getUser(id).block();
        var state = Optional.ofNullable(user).map(UserCache::getBotState).orElse(DefaultStates.NONE);
        return state.toString();
    }

    public Mono<Void> setState(Message message, BotState botState) {
        return setState(message.chat().id(), botState);
    }

    public Mono<Void> setState(Long chatId, BotState botState) {
        log.info("state updated {}, id = {}", botState.toString(), chatId);
        return userDataCacheRepository.updateState(chatId, botState);
    }
}
