package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;

public class BotStateFactory {

    public static AbstractBotState createBotState(State state, BotContext context) {
        if (state == State.REGISTER_NAME) {
            return new RegisterName(context);
        } else if (state == State.CHECK_NAME) {
            return new CheckName(context);
        } else if (state == State.MENU) {
            return new Menu(context);
        } else if (state == State.MENU_CHOICE) {
            return new MenuChoice(context);
        } else if (state == State.VACATION) {
            return new Vacation(context);
        } else if (state == State.NOTIFICATION_CHOICE) {
            return new NotificationChoice(context);
        }  else if (state == State.REPORT_TYPE) {
            return new ReportType(context);
        } else if (state == State.CHOOSE_DAY) {
            return new ChooseDay(context);
        } else if (state == State.PARSE_DATE) {
            return new ParseDate(context);
        } else if (state == State.SELECT_PROJECT) {
            return new SelectProject(context);
        }
        return null;
    }
}
