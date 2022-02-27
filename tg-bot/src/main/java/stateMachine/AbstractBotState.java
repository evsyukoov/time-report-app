package stateMachine;

import bot.BotContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import utils.SendHelper;

public abstract class AbstractBotState {

    protected SendMessage sm;

    protected BotContext context;

    public AbstractBotState(BotContext context) {
        this.context = context;
    }

    // хендлим ответ на вопрос
    public abstract void handleMessage();

    //задаем новый вопрос
    public void question() {
        SendHelper.sendMessage(sm, context);
    }

}
