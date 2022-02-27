package utils;

import bot.BotContext;
import hibernate.entities.Project;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import stateMachine.RegisterName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SendHelper {

    private static final Logger logger = Logger.getLogger(RegisterName.class.getName());

    public static void sendMessage(SendMessage sendMessage, BotContext context){
        sendMessage.setChatId(String.valueOf(context.getClient().getUid()));
        try {
            context.getBot().execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.severe(e.getCause() != null ? e.getCause().getMessage() : Message.ERROR_SEND_MESSAGE);
        }
    }

    public static synchronized void setInlineKeyboard(SendMessage sm, List<String> buttons, String message, int buttonsInRow) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = null;
        for (int i = 0; i < buttons.size(); i++) {
            if (i % buttonsInRow == 0) {
                row = new ArrayList<>();
                rows.add(row);
            }
            row.add(newButton(buttons.get(i)));
        }
        if (message != null) {
            row = new ArrayList<>();
            rows.add(row);
            row.add(newButton(message));
        }
        inlineKeyboard.setKeyboard(rows);
        sm.setReplyMarkup(inlineKeyboard);
    }

    private static InlineKeyboardButton newButton(String message) {
        return newButton(message, message);
    }

    private static InlineKeyboardButton newButton(String message, String callbackData) {
        InlineKeyboardButton button= new InlineKeyboardButton();
        button.setText(message);
        button.setCallbackData(callbackData);
        return button;
    }

    // названия проектов не умещаются в CallBackData из-за размера, в callBack будем сетить id
    // и на следующем шаге получать по id название проекта
    public static synchronized void setInlineKeyboardProjects(SendMessage sm, List<Project> buttons) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = null;
        for (int i = 0; i < buttons.size(); i++) {
            row = new ArrayList<>();
            rows.add(row);
            String msg = Message.EMPTY_SYMBOL.concat(buttons.get(i).getProjectName());
            row.add(newButton(msg, String.valueOf(buttons.get(i).getId())));
        }
        row = new ArrayList<>();
        rows.add(row);
        row.add(newButton(Message.BACK));
        row.add(newButton(Message.APPROVE));

        inlineKeyboard.setKeyboard(rows);
        sm.setReplyMarkup(inlineKeyboard);
    }

    public static synchronized void setInlineKeyboardOneColumn(SendMessage sm, List<String> buttons, String message) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = null;
        for (int i = 0; i < buttons.size(); i++) {
            row = new ArrayList<>();
            rows.add(row);
            String msg = Message.EMPTY_SYMBOL.concat(buttons.get(i));
            row.add(newButton(msg, buttons.get(i)));
        }
        if (message != null) {
            row = new ArrayList<>();
            rows.add(row);
            row.add(newButton(message));
        }
        inlineKeyboard.setKeyboard(rows);
        sm.setReplyMarkup(inlineKeyboard);
    }

    public static synchronized void refreshInlineKeyboard(BotContext context) {
        int id = context.getUpdate().getCallbackQuery().getMessage().getMessageId();
        String text = context.getMessage();
        InlineKeyboardMarkup markup = context.getUpdate().getCallbackQuery().
                getMessage().getReplyMarkup();

        AtomicBoolean isChanged = new AtomicBoolean(false);
        markup.getKeyboard()
                .stream().flatMap(k -> k.stream())
                .filter(item -> item.getCallbackData().equals(text))
                .findAny()
                .ifPresent(item -> {
                    if (item.getText().startsWith(Message.EMPTY_SYMBOL)) {
                        item.setText(item.getText().replace(Message.EMPTY_SYMBOL, Message.CONFIRM_SYMBOL));
                        isChanged.set(true);
                    } else {
                        item.setText(item.getText().replace(Message.CONFIRM_SYMBOL, Message.EMPTY_SYMBOL));
                        isChanged.set(true);
                    }
                });
        if (isChanged.get()) {
            EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                    .messageId(id)
                    .chatId(String.valueOf(context.getClient().getUid()))
                    .replyMarkup(markup)
                    .build();
            try {
                context.getBot().execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void setDateTimeInlineQuery(SendMessage sm) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = null;
        for (int i = 0; i < 24; i++) {
            if (i % 2 == 0) {
                row = new ArrayList<>();
                rows.add(row);
            }
            String time = getTime(i, "час.");
            String msg = Message.EMPTY_SYMBOL.concat(time);
            row.add(newButton(msg, time));
        }
        for (int i = 0; i < 60; i += 5) {
            if (i % 15 == 0) {
                row = new ArrayList<>();
                rows.add(row);
            }
            String time = getTime(i, "мин.");
            String msg = Message.EMPTY_SYMBOL.concat(time);
            row.add(newButton(msg, time));
        }
        row = new ArrayList<>();
        rows.add(row);
        row.add(newButton(Message.DISCHARGE_NOTIFICATION));
        row.add(newButton(Message.APPROVE_NOTIFICATION));
        inlineKeyboard.setKeyboard(rows);
        sm.setReplyMarkup(inlineKeyboard);
    }

    private static String getTime(int i, String type) {
        return i < 10 ? "0".concat(String.valueOf(i)).concat(" ").concat(type)
                : String.valueOf(i).concat(" ").concat(type);
    }

}
