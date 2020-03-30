package ua.kiev.prog.models;

import javax.persistence.*;

@Entity
public class CustomMessage {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CustomUser customUser;
    private String message;
    private boolean botMessage;

    public CustomMessage() { }

    public CustomMessage(CustomUser customUser, String message, boolean botMessage) {
        this.customUser = customUser;
        this.message = message;
        this.botMessage = botMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomUser getCustomUser() {
        return customUser;
    }

    public void setCustomUser(CustomUser customUser) {
        this.customUser = customUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getBotMessage() {
        return botMessage;
    }

    public void setBotMessage(boolean botMessage) {
        this.botMessage = botMessage;
    }

    @Override
    public String toString() {
        return "CustomMessage{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", botMessage=" + botMessage +
                '}';
    }
}
