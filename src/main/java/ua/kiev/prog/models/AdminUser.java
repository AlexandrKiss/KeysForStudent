package ua.kiev.prog.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.telegram.telegrambots.meta.api.objects.Contact;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class AdminUser extends CustomUser {
    @Lob
    @JsonProperty("access_token")
    private String accessToken;
    @Lob
    @JsonProperty ("refresh_token")
    private String refreshToken;

    private String clientId;
    private String clientSecret;
    @Lob
    private String code;
    private String redirectUri;

    private int step = 0;

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

    public int getStep() {
        return step;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public void setStep(int step) {
        this.step = step;
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
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", code='" + code + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", step=" + step +
                '}';
    }
}
