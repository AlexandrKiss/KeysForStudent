package ua.kiev.prog.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.telegram.telegrambots.meta.api.objects.Contact;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class AdminUser extends CustomUser {
    @Lob
//    @Type(type="org.hibernate.type.BinaryType")
    @JsonProperty("access_token")
    private String accessToken;
    @Lob
//    @Type(type="org.hibernate.type.BinaryType")
    @JsonProperty ("refresh_token")
    private String refreshToken;

    public AdminUser(){};

    public AdminUser(Contact contact) {
        super.setUserID(Long.valueOf(contact.getUserID()));
        super.setPhoneNumber(Long.valueOf(contact.getPhoneNumber()));
        super.setFirstName(contact.getFirstName());
        super.setLastName(contact.getLastName());
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "AdminUser{" +
                "id=" + super.getId() +
                ", userID=" + super.getUserID() +
                ", phoneNumber=" + super.getPhoneNumber() +
                ", firstName='" + super.getFirstName() + '\'' +
                ", lastName='" + super.getLastName() + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
