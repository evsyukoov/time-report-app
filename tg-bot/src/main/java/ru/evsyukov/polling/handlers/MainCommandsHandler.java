package ru.evsyukov.polling.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.NotificationRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;
import ru.evsyukov.app.data.repository.ReportDayRepository;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.data.BotDataService;
import ru.evsyukov.polling.exceptions.DateAfterTodayException;
import ru.evsyukov.polling.exceptions.TooLongIntervalException;
import ru.evsyukov.polling.exceptions.ValidationException;
import ru.evsyukov.polling.messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.evsyukov.polling.properties.ButtonsProperties;
import ru.evsyukov.polling.stateMachine.EnumTranslators;
import ru.evsyukov.polling.utils.DateTimeUtils;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.utils.Utils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MainCommandsHandler {

    private final ButtonsProperties buttonsProperties;

    private final BotDataService botDataService;

    @Autowired
    public MainCommandsHandler(ButtonsProperties buttonsProperties,
                               BotDataService botDataService) {
        this.buttonsProperties = buttonsProperties;
        this.botDataService = botDataService;
    }

    public SendMessage handleBackButton(BotContext context, String message, State newState) {
        String command = context.getMessage();
        if (command.equals(Message.BACK)) {
            SendMessage sm = new SendMessage();

            botDataService.updateClientState(context.getClient(), newState);

            if (message.equals(Message.REGISTER_NAME)) {
                SendHelper.setInlineKeyboardOneColumn(sm, botDataService.getAllEmployeeNamesSorted(), null);
            } else if (message.equals(Message.MENU)) {
              SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);
            } else if (message.equals(Message.SELECT_PROJECT)) {
                SendHelper.setInlineKeyboardProjects(sm, botDataService.getAllProjectsSorted());
            } else if (message.equals(Message.CHOOSE_REPORT_TYPE)) {
                SendHelper.setInlineKeyboard(sm, buttonsProperties.getDays(), Message.BACK, 2);
            } else if (message.equals(Message.SELECT_DATE)) {
                SendHelper.setInlineKeyboard(sm, Collections.emptyList(), Message.BACK, 2);
            }
            sm.setText(message);
            return sm;
        }
        return null;
    }

    public SendMessage handleClearVacation(BotContext context) {
        SendMessage sm = new SendMessage();
        if (context.isCallBackQuery() && context.getMessage().equals(Message.CLEAR_VACATION)) {
            sm.setText(Message.VACATION_IS_CLEAR);
            botDataService.clearClientVacation(context.getClient());
            SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);
            return sm;
        }
        return null;
    }

    public SendMessage handleVacationsDate(BotContext context) {
        String command = context.getMessage();
        Date[] res;
        SendMessage sm;
        if ((sm = handleClearVacation(context)) != null) {
            return sm;
        }
        sm = new SendMessage();
        ArrayList<String> buttons = new ArrayList<>();
        boolean isVacationRegister = context.getClient().getStartVacation() != null
                && context.getClient().getEndVacation() != null;
        try {
            buttons.add(Message.BACK);
            if (isVacationRegister) {
                buttons.add(Message.CLEAR_VACATION);
            }
            res = Utils.parseVacationsDate(command);
        }
        catch(Exception e) {
            String finalMessage = null;
            if (e instanceof ValidationException) {
                finalMessage = Utils.generateResultMessage(Message.VACATION_DATES_VALIDATION_ERROR, Message.VACATION);
            } else if (e instanceof ParseException) {
                finalMessage = Utils.generateResultMessage(Message.ERROR_DATE_FORMAT, Message.VACATION);
            } else if (e instanceof DateAfterTodayException) {
                finalMessage = Utils.generateResultMessage(Message.VACATION_ERROR_END_DATE, Message.VACATION);
            } else if (e instanceof TooLongIntervalException) {
                finalMessage = Utils.generateResultMessage(Message.TOO_LONG_INTERVAL, Message.VACATION);
            }
            sm.setText(finalMessage);
            SendHelper.setInlineKeyboard(sm, buttons, null, 2);
            return sm;
        }
        if (isVacationRegister) {
            sm.setText(Message.NOT_ALLOWED_INIT_VACATION);
            SendHelper.setInlineKeyboard(sm, buttons, null, 2);
            return sm;
        }
        boolean onVacation = DateTimeUtils.isBetweenStrict(res[0], res[1], new Date());
        botDataService.updateClientVacation(context.getClient(), State.MENU_CHOICE, res[0], res[1], onVacation);
        sm.setText(Message.VACATION_DATES_SET);
        if (onVacation) {
            sm.setText(Message.YOU_ARE_IN_VACATION_MODE);
            SendHelper.setInlineKeyboard(sm, Collections.emptyList(), Message.CLEAR_VACATION, 1);
        } else {
            SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);
        }
        return sm;
    }

    public SendMessage handleMenuChoice(BotContext context) {
        String command = context.getMessage();
        SendMessage sm = new SendMessage();
        if (command.equals(buttonsProperties.getActionsMenu().get(0))) {
            log.info("Client {} pressed {} button", context.getClient(), buttonsProperties.getActionsMenu().get(0));
            botDataService.updateClientState(context.getClient(), State.CHOOSE_DAY);
            SendHelper.setInlineKeyboard(sm, buttonsProperties.getDays(), Message.BACK, 2);
            sm.setText(Message.CHOOSE_REPORT_TYPE);
            return sm;
        } else if (command.equals(buttonsProperties.getActionsMenu().get(1))) {
            log.info("Client {} pressed {} button", context.getClient(), buttonsProperties.getActionsMenu().get(1));
            botDataService.updateClientState(context.getClient(), State.NOTIFICATION_CHOICE);
            SendHelper.setDateTimeInlineQuery(sm,
                    botDataService.getClientChosenTime(context.getClient()));
            sm.setText(Message.NOTIFICATION_CHOICE);
            return sm;
        } else if (command.equals(buttonsProperties.getActionsMenu().get(2))) {
            log.info("Client {} pressed {} button", context.getClient(), buttonsProperties.getActionsMenu().get(2));
            botDataService.updateClientState(context.getClient(), State.VACATION);
            ArrayList<String> actionButtons = new ArrayList<>();
            actionButtons.add(Message.BACK);
            if (context.getClient().getStartVacation() != null
                    && context.getClient().getEndVacation() != null) {
                actionButtons.add(Message.CLEAR_VACATION);
            }
            SendHelper.setInlineKeyboard(sm, actionButtons, null, 2);
            sm.setText(Message.VACATION);
            return sm;
        }
        return null;
    }

    public SendMessage handleTimeChoice(BotContext context) {
        String command = context.getMessage();
        LocalDateTime resultTime;
        SendMessage sm = null;
        if (!context.isCallBackQuery()) {
            return null;
        }
        if (command.split(" ")[0].matches("\\d+")) {
            refreshTimeBox(context, command);
        } else if (command.equals(Message.DISCHARGE_NOTIFICATION)) {
            log.info("Client {} pressed {} button", context.getClient(), Message.DISCHARGE_NOTIFICATION);
            sm = new SendMessage();
            botDataService.updateNotification(context.getClient(), null);
            sm.setText(Utils.generateResultMessage(Message.DISCHARGE_ACTION_ENABLED, Message.MENU));
            SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);
            botDataService.updateClientState(context.getClient(), State.MENU_CHOICE);
            return sm;
        } else if (command.equals(Message.APPROVE_NOTIFICATION) &&
                (resultTime = getTimeFromClientChoice(context)) != null) {
            log.info("Client {} pressed {} button", context.getClient(), Message.APPROVE_NOTIFICATION);
            sm = new SendMessage();

            botDataService.updateNotification(context.getClient(), resultTime);
            sm.setText(Utils.generateResultMessage(
                    String.format(Message.APPROVE_NOTIFICATION_ENABLED, getTimeStringFromDate(resultTime)),
                    Message.MENU));

            SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);
            botDataService.updateClientState(context.getClient(), State.MENU_CHOICE);
            return sm;
        }
        return null;
    }

    private String getTimeStringFromDate(LocalDateTime dateTime) {
        return String.format("%s:%s по МСК", dateTime.getHour() < 10 ? "0".concat(String.valueOf(dateTime.getHour()))
                        : String.valueOf(dateTime.getHour())
                , dateTime.getMinute() < 10 ?
                "0".concat(String.valueOf(dateTime.getMinute()))
                : String.valueOf(dateTime.getMinute()));
    }

    private InlineKeyboardButton findPressedButton(String payload, InlineKeyboardMarkup markup) {
        return markup.getKeyboard().stream()
                .flatMap(Collection::stream)
                .filter(button -> button.getText().contains(payload)
                        && button.getText().contains(Message.CONFIRM_SYMBOL))
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime getTimeFromClientChoice(BotContext context) {
        InlineKeyboardMarkup markup = context.getUpdate().getCallbackQuery().
                getMessage().getReplyMarkup();
        InlineKeyboardButton pressedHour = findPressedButton("час." , markup);
        InlineKeyboardButton pressedMinutes = findPressedButton("мин.", markup);
        if (pressedHour == null || pressedMinutes == null) {
            return null;
        }
        Integer hour = Integer.parseInt(pressedHour.getCallbackData().split(" ")[0]);
        Integer minutes = Integer.parseInt(pressedMinutes.getCallbackData().split(" ")[0]);
        LocalDateTime nextFireTime = LocalDateTime.now();
        if (botDataService.isReportToday(context.getClient())) {
            nextFireTime = nextFireTime.plusHours(24);
        }
        return LocalDateTime.of(nextFireTime.getYear(), nextFireTime.getMonth(), nextFireTime.getDayOfMonth(),
                hour, minutes);
    }

    public SendMessage parseDate(BotContext context) {
        String command = context.getMessage();
        SendMessage sm = new SendMessage();
        LocalDateTime date;
        try {
            date = Utils.parseDate(command);

        } catch (DateAfterTodayException e) {
            sm.setText(Utils.generateResultMessage(Message.ERROR_DATE_AFTER_TODAY, Message.SELECT_DATE));
            SendHelper.setInlineKeyboard(sm,Collections.emptyList(), Message.BACK, 2);
            return sm;
        } catch (ParseException e) {
            sm.setText(Utils.generateResultMessage(Message.ERROR_DATE_FORMAT, Message.SELECT_DATE));
            SendHelper.setInlineKeyboard(sm,Collections.emptyList(), Message.BACK, 2);
            return sm;
        }
        botDataService.updateClientDateAndState(context.getClient(), State.SELECT_PROJECT, date);
        List<Project> projects = botDataService.getAllProjectsSorted();
        SendHelper.setInlineKeyboardProjects(sm, projects);
        sm.setText(Message.SELECT_PROJECT);
        return sm;
    }

    public SendMessage handleReportChoice(BotContext context) {
        String command = context.getMessage();
        SendMessage message = new SendMessage();
        if (command.equals(buttonsProperties.getDays().get(0))) {
            List<Project> projects = botDataService.getAllProjectsSorted();
            message.setText(EnumTranslators.translate(State.SELECT_PROJECT.ordinal()));
            SendHelper.refreshInlineKeyboard(context);
            SendHelper.setInlineKeyboardProjects(message, projects);
            botDataService.updateClientState(context.getClient(), State.SELECT_PROJECT);
            return message;
        }
        else if (command.equals(buttonsProperties.getDays().get(1))) {
            message.setText(EnumTranslators.translate(State.PARSE_DATE.ordinal()));
            SendHelper.refreshInlineKeyboard(context);
            SendHelper.setInlineKeyboard(message, Collections.emptyList(), Message.BACK, 2);
            botDataService.updateClientState(context.getClient(), State.PARSE_DATE);
            return message;
        }
        return null;
    }

    private boolean isValidTimeBoxChoice(BotContext context, String currentPress) {
        InlineKeyboardMarkup markup = context.getUpdate().getCallbackQuery().
                getMessage().getReplyMarkup();
        if (currentPress.contains("час.")) {
            long countHourChoice = markup.getKeyboard().stream()
                    .flatMap(Collection::stream)
                    .filter(button -> button.getText().contains("час.")
                            && button.getText().contains(Message.CONFIRM_SYMBOL)
                            && !button.getCallbackData().equals(currentPress))
                    .count();
            return countHourChoice == 0;
        } else  if (currentPress.contains("мин.")) {
            long countMinutesChoice = markup.getKeyboard().stream()
                    .flatMap(Collection::stream)
                    .filter(button -> button.getText().contains("мин.")
                            && button.getText().contains(Message.CONFIRM_SYMBOL)
                            && !button.getCallbackData().equals(currentPress))
                    .count();
            return countMinutesChoice == 0;
        }
        return true;
    }

    private void refreshTimeBox(BotContext context, String command) {
        InlineKeyboardMarkup markup = context.getUpdate().getCallbackQuery().
                getMessage().getReplyMarkup();
        if (isValidTimeBoxChoice(context, command)) {
            SendHelper.refreshInlineKeyboard(context);
        }
    }

    public long countFilledBoxes(String telegramSymbol, InlineKeyboardMarkup markup) {
        return markup.getKeyboard().stream()
                .flatMap(Collection::stream)
                .filter(button -> button.getText().contains(telegramSymbol))
                .count();
    }

    public SendMessage handleProjectsChoice(BotContext context) {
        if (!context.isCallBackQuery()) {
            return null;
        } else {
            if (!context.getMessage().equals(Message.APPROVE)) {
                handleProjectBox(context);
                return null;
            } else {
                List<String> result = collectProjectChoice(context);
                if (result.isEmpty()) {
                    return null;
                } else {
                    botDataService.updateClientProjects(context.getClient(), State.FINISH, result);
                }
            }
        }
        SendMessage sm = new SendMessage();
        sm.setText(Utils.generateResultMessage(Message.FINISH, Message.MENU));
        SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);;
        return sm;
    }

    private List<String> collectProjectChoice(BotContext context) {
        InlineKeyboardMarkup markup = context.getUpdate().getCallbackQuery().
                getMessage().getReplyMarkup();
        List<String> projects = new ArrayList<>();
        InlineKeyboardButton mainProjectButton = findConfirmedBoxes(markup, Message.CONFIRM_SYMBOL);
        if (mainProjectButton != null) {
            projects.add(mainProjectButton.getCallbackData());
            List<InlineKeyboardButton> extraButtons = findExtraConfirmedBoxes(markup, Message.EXTRA_CONFIRM_SYMBOL);

            projects.addAll(extraButtons.stream()
                    .map(InlineKeyboardButton::getCallbackData)
                    .collect(Collectors.toList()));

        }
        return projects;
    }

    private InlineKeyboardButton findConfirmedBoxes(InlineKeyboardMarkup markup, String telegramSymbol) {
        return markup.getKeyboard().stream()
                .flatMap(Collection::stream)
                .filter(button -> button.getText().contains(telegramSymbol))
                .findAny()
                .orElse(null);
    }

    private List<InlineKeyboardButton> findExtraConfirmedBoxes(InlineKeyboardMarkup markup, String telegramSymbol) {
        return markup.getKeyboard().stream()
                .flatMap(Collection::stream)
                .filter(button -> button.getText().contains(telegramSymbol))
                .collect(Collectors.toList());
    }

    private InlineKeyboardButton findPressedButton(InlineKeyboardMarkup markup, String text) {
        return markup.getKeyboard().stream()
                .flatMap(Collection::stream)
                .filter(button -> button.getCallbackData().equals(text))
                .findAny()
                .orElse(null);
    }

    private void handleProjectBox(BotContext context) {
        String command = context.getMessage();
        InlineKeyboardMarkup markup = context.getUpdate().getCallbackQuery().
                getMessage().getReplyMarkup();
        int id = context.getUpdate().getCallbackQuery().getMessage().getMessageId();

        InlineKeyboardButton pressedButton = findPressedButton(markup, command);
        if (Objects.isNull(pressedButton)) {
            return;
        }

        boolean isModified = false;
        // главных объектов 1, дополнительных 7
        if (countFilledBoxes(Message.CONFIRM_SYMBOL, markup) == 0) {
            pressedButton.setText(pressedButton.getText().replace(Message.EMPTY_SYMBOL, Message.CONFIRM_SYMBOL));
            isModified = true;
        } else if (countFilledBoxes(Message.CONFIRM_SYMBOL, markup) == 1) {
            if (pressedButton.getText().contains(Message.CONFIRM_SYMBOL)) {
                pressedButton.setText(pressedButton.getText().replace(Message.CONFIRM_SYMBOL, Message.EMPTY_SYMBOL));
                isModified = true;
            } else {
                if (pressedButton.getText().contains(Message.EXTRA_CONFIRM_SYMBOL)) {
                    pressedButton.setText(pressedButton.getText().replace(Message.EXTRA_CONFIRM_SYMBOL, Message.EMPTY_SYMBOL));
                    isModified = true;
                } else if (countFilledBoxes(Message.EXTRA_CONFIRM_SYMBOL, markup) < 7){
                    pressedButton.setText(pressedButton.getText().replace(Message.EMPTY_SYMBOL, Message.EXTRA_CONFIRM_SYMBOL));
                    isModified = true;
                }
            }
        }
        if (isModified) {
            EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                    .messageId(id)
                    .chatId(String.valueOf(context.getClient().getUid()))
                    .replyMarkup(markup)
                    .build();
            try {
                context.getBot().execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                log.error("Error while edit reply markup: ", e);
            }
        }
    }

}
