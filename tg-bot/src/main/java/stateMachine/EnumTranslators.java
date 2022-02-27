package stateMachine;

import messages.Message;

public class EnumTranslators {

    public static String translate(int state) {
        if (state == State.REGISTER_NAME.ordinal()) {
            return Message.REGISTER_NAME;
        } else if (state == State.MENU.ordinal()) {
            return null;
        } else if (state == State.MENU_CHOICE.ordinal()) {
            return Message.CHOOSE_REPORT_TYPE;
        } else if (state == State.CHOOSE_DAY.ordinal()) {
            return Message.CHOOSE_DAY;
        } else if (state == State.SELECT_PROJECT.ordinal()) {
            return Message.SELECT_PROJECT;
        }else if (state == State.PARSE_DATE.ordinal()) {
            return Message.SELECT_DATE;
        } else if (state == State.FINISH.ordinal()) {
            return Message.FINISH;
        }
        return "";
    }
}
