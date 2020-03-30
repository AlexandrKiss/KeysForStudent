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
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.kiev.prog.models.CustomUser;

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

    private final UserService userService;

    public ChatBot(UserService userService) {
        this.userService = userService;
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

        if (userService.findByUserID(chatID) == null) {
            if (!update.getMessage().hasContact()) {
                sendMessage(chatID, "Что-бы получить ключ, нажмите на кнопку \"Авторизация\"", true);
                return;
            } else {
                CustomUser user = new CustomUser(update.getMessage().getContact());
                userService.addUser(user);
                sendMessage(user.getUserID(),"Вы авторизованы", false);
                sendMessage(user.getUserID(),user.toString(), false);
                logger.info(user.toString());
                return;
            }
        }
        if(update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            sendMessage(chatID, text,false);
            logger.info(text);
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

    private void sendMessage(Long chatID, String text, boolean requestContact) {
        SendChatAction sendChatAction = new SendChatAction()
                .setChatId(chatID)
                .setAction(ActionType.get("typing"));
        SendMessage message = new SendMessage()
                .setChatId(chatID)
                .setText(text)
                .setParseMode("html");
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
}