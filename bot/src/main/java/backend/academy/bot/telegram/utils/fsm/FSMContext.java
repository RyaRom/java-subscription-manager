package backend.academy.bot.telegram.utils.fsm;

import backend.academy.bot.repository.BotStateRepository;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@Log4j2
@RequiredArgsConstructor
public class FSMContext {
    private final BotStateRepository botStateRepository;

    public String getCurrentStateName(Long id) {
        var state = botStateRepository.getState(id);
        if (state.isEmpty()) {
            return "";
        }
        return state.get().toString();
    }

    public Optional<BotState> setState(Message message, BotState botState) {
        return setState(message.chat().id(), botState);
    }

    public Optional<BotState> setState(Long chatId, BotState botState) {
        log.info("state updated {}, id = {}", botState.toString(), chatId);
        return botStateRepository.updateState(chatId, botState);
    }
}
