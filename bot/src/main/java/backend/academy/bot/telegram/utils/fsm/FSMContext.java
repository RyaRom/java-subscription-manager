package backend.academy.bot.telegram.utils.fsm;

import backend.academy.bot.repository.UserDataCacheRepository;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class FSMContext {
    private final UserDataCacheRepository userDataCacheRepository;

    public String getCurrentStateName(Long id) {
        var state = userDataCacheRepository.getUser(id).botState();
        if (state == DefaultStates.NONE) {
            return "";
        }
        return state.toString();
    }

    public void setState(Message message, BotState botState) {
        setState(message.chat().id(), botState);
    }

    public void setState(Long chatId, BotState botState) {
        log.info("state updated {}, id = {}", botState.toString(), chatId);
        userDataCacheRepository.updateState(chatId, botState);
    }
}
