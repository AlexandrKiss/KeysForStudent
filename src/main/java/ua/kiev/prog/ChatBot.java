package ua.kiev.prog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.kiev.prog.models.AdminUser;
import ua.kiev.prog.models.CustomUser;
import ua.kiev.prog.service.AdminService;
import ua.kiev.prog.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ChatBot.class);

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Value("${crm.client.id}")
    private String crmClientID;

    private final UserService userService;
    private final AdminService adminService;

    public ChatBot(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatID = update.getMessage().getChatId();

        if (userService.findByUserID(chatID) == null) { //authorization == null
            if (!update.getMessage().hasContact()) {
                sendMessage(chatID, "Что-бы продолжить, нажмите на кнопку \"Авторизация\"", true);
            } else {
                if (userService.countByAdmin(true) == 0) {
                    AdminUser adminUser = new AdminUser(update.getMessage().getContact());
                    adminUser.setAdmin(true);
                    adminService.addUser(adminUser);
                    sendMessage(adminUser.getUserID(), "Вы авторизованы как Администратор", false);
                    sendInlineButtons(adminUser.getUserID(),
                            new InlineKeyboardButton()
                                .setText("AmoCRM")
                                .setUrl("https://www.amocrm.ru/oauth?client_id="+crmClientID+"&state="+adminUser.getUserID()+"&mode=post_message"),
                            "Авторизуйтесь в системе AmoCRM"
                    );
                } else {
                    CustomUser user = new CustomUser(update.getMessage().getContact());
                    userService.addUser(user);
                    sendMessage(user.getUserID(), "Проверяем данные...", false);
                    serviceUser(user);
                }
            }
        } else if(adminService.findByUserID(chatID)!=null) { //admin panel
            AdminUser adminUser = adminService.findByUserID(chatID);
        } else { //user panel
            if(update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                sendMessage(chatID, text, false);
            } else if (update.getMessage().hasContact()) { //удалить после тестов
                CustomUser user = userService.findByUserID(chatID);
                logger.info(user.toString());
                sendMessage(user.getUserID(), "Проверяем данные...", false);
                serviceUser(user);
            }
        }
    }

    private void serviceUser(CustomUser user){
        String numString = user.getPhoneNumber().toString();
        String customNum = numString.substring(numString.length()-9);
        if (userService.searchUser(customNum)) {
            sendMessage(user.getUserID(), "Вы авторизованы. Ваш ключ: 1380-1310-8760-1543-0173-7899", false);
        } else {
            sendMessage(user.getUserID(), "Вы не авторизованы. Обратитесь к администрации.", false);
        }
    }

    public ReplyKeyboardMarkup setButtons(boolean requestContact) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup()
                .setSelective(true)
                .setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопку в первую строчку клавиатуры
        keyboardFirstRow.add(new KeyboardButton("Авторизация").setRequestContact(requestContact));
        keyboard.add(keyboardFirstRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    void sendMessage(Long chatID, String text, boolean requestContact) {
        SendChatAction sendChatAction = new SendChatAction()
                .setChatId(chatID)
                .setAction(ActionType.get("typing"));
        SendMessage message = new SendMessage()
                .setChatId(chatID)
                .setText(text)
                .setParseMode("html");
//        message.setReplyMarkup(setButtons(true));
        if(requestContact) {
            message.setReplyMarkup(setButtons(true));
        } else {
            message.setReplyMarkup(new ReplyKeyboardRemove());
        }
        try {
            execute(sendChatAction);
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendInlineButtons(Long chatID, InlineKeyboardButton button, String text) {
        InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();

        keyboardButtonsRow.add(button);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);
        try {
            execute(new SendMessage()
                    .setChatId(chatID)
                    .setText(text)
                    .setReplyMarkup(inlineKeyboardMarkup)
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}