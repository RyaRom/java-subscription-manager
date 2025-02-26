package backend.academy.bot.telegram.utils.fsm;

import backend.academy.bot.repository.BotStateRepository;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class FSMContext {
    private final BotStateRepository botStateRepository;

    public String getCurrentStateName(Long id) {
        var state = botStateRepository.getState(id);
        if (state == DefaultStates.NONE) {
            return "";
        }
        return state.toString();
    }

    public BotState setState(Message message, BotState botState) {
        return setState(message.chat().id(), botState);
    }

    public BotState setState(Long chatId, BotState botState) {
        log.info("state updated {}, id = {}", botState.toString(), chatId);
        return botStateRepository.updateState(chatId, botState);
    }
}
