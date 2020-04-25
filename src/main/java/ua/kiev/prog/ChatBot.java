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
import ua.kiev.prog.models.contatct.Contact;
import ua.kiev.prog.models.lead.Lead;
import ua.kiev.prog.service.AdminService;
import ua.kiev.prog.service.UserService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static ua.kiev.prog.enums.AdminMessages.CHEATER;
import static ua.kiev.prog.enums.UserMessages.*;

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
            authorization(chatID,
                    update.getMessage().getChat().getUserName(),
                    update.getMessage().hasContact(),
                    update.getMessage().getContact());
        } else if(adminService.findByUserID(chatID)!=null) { //admin panel
            AdminUser adminUser = adminService.findAdmin(true);
            try {
                sendInlineButtons(adminUser.getUserID(),
                        new InlineKeyboardButton()
                                .setText("Google Drive")
                                .setUrl("https://accounts.google.com/o/oauth2/auth?" +
                                        "access_type=offline&" +
                                        "client_id=291052134520-qkpsvkmn11kardqojqe1kcb9bmr4pr1i.apps.googleusercontent.com&" +
                                        "redirect_uri=http://localhost:8889/Callback&" +
                                        "response_type=code&" +
                                        "scope=https://www.googleapis.com/auth/drive"),
                        "Авторизуйтесь в Google Drive"
                );
                AdminService.service();
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        } else { //user panel
            if(update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                sendMessage(chatID, text, false);
            } else if (update.getMessage().hasContact()) { //удалить после тестов
                CustomUser user = userService.findByUserID(chatID);
                sendMessage(user.getUserID(), VERIFICATION.getMessage(), false);
                serviceUser(user);
            }
        }
    }

    private void serviceUser(CustomUser user){
        AdminUser adminUser = adminService.findAdmin(true);
        String numString = user.getPhoneNumber().toString();
        String customNum = numString.substring(numString.length()-9);
        Contact[] contacts = userService.searchUser(adminUser, customNum);

        if (contacts == null ) {
            sendMessage(user.getUserID(), NO_USER.getMessage(),true);
        } else {
            for (Contact contact : contacts) {
                int[] leadsContact = contact.getLeads().getLeads();
                if (leadsContact == null) {
                    sendMessage(user.getUserID(), NO_COURSE.getMessage(),true);
                } else {
                    for (int leadID : leadsContact) {
                        Lead[] leads = userService.viewLeads(adminUser, leadID);
                        for (Lead lead : leads) {
                            if (userService.viewStatuses(adminUser, lead.getPipeline().getId(), lead.getStatus())) {
                                sendMessage(user.getUserID(), PROFIT.getMessage(), false);
                            } else {
                                sendMessage(user.getUserID(), NO_MONEY.getMessage(), true);
                            }
                        }
                    }
                }
            }
        }
    }

    private void authorization(Long chatID, String chatName, boolean hasContact, org.telegram.telegrambots.meta.api.objects.Contact getContact) {
        if (!hasContact) {
            sendMessage(chatID, "Что-бы продолжить, нажмите на кнопку \"Авторизация\"", true);
        } else {
            if (userService.countByAdmin(true) == 0) {
                AdminUser adminUser = new AdminUser(getContact);
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
                CustomUser user = new CustomUser(getContact);
                if (chatID.equals(user.getUserID())) {
                    userService.addUser(user);
                    sendMessage(user.getUserID(), VERIFICATION.getMessage(), false);
                    serviceUser(user);
                } else {
                    sendMessage(chatID, NO_CHEAT.getMessage(), true);
                    sendMessage(adminService.findAdmin(true).getUserID(),
                            CHEATER.getMessage()+chatName,false);
                }
            }
        }
    }

    private ReplyKeyboardMarkup setButtons() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup()
                .setSelective(true)
                .setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопку в первую строчку клавиатуры
        keyboardFirstRow.add(new KeyboardButton("Авторизация").setRequestContact(true));
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
        if(requestContact) {
            message.setReplyMarkup(setButtons());
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