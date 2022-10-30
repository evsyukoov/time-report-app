package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.utils.SendHelper;

public interface BotState {

    State getState();
    // хендлим ответ на вопрос

    void handleMessage(BotContext context);

    //задаем новый вопрос
    default void question(SendMessage sm, BotContext context) {
        SendHelper.sendMessage(sm, context);
    }

}
