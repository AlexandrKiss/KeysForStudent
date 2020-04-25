package ua.kiev.prog.enums;

public enum UserMessages {
    NO_CHEAT("Так не пойдет. Попробуй без читерства."),
    VERIFICATION("Проверяем данные..."),
    NO_USER("По данному номеру не найдено ни одного совапдения.\n" +
            "Обратитесь к администрации Prog.kiev.ua.\n\n" +
            "<i>Подсказка: используйте тот же номер, который использовали при разговоре с менеджером.</i>"),
    NO_COURSE("Вы не зарегестрированы ни на одном из курсов.\n" +
            "Обратитесь к администрации Prog.kiev.ua."),
    NO_MONEY("Нет оплаты." +
            "Если вы оплачивали курс, но видите это сообщение - " +
            "обратитесь к администрации Prog.kiev.ua."),
    PROFIT("Спасибо, что вы с нами. Сейчас пришлем лицензию.");

    private String message;
    UserMessages(String s) {
        this.message = s;
    }

    public String getMessage() {
        return message;
    }
}
