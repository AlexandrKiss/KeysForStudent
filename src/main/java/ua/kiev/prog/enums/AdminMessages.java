package ua.kiev.prog.enums;

public enum AdminMessages {
    NO_LICENSES("Внимание! Закончились лицензии!"),
    CHEATER("У нас завелся читер: ");

    private String message;
    AdminMessages(String s) {
        this.message = s;
    }

    public String getMessage() {
        return message;
    }
}
