package ua.kiev.prog.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Post implements Serializable {
    private int statusCode;
    private String statusText;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusCode + " " +statusText;
    }

    public void setStatus(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String toString() {
        return "Post{" +
                "statuCode=" + statusCode +
                ", statusText='" + statusText + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
