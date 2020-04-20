package ua.kiev.prog.models.contatct;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class ContactEmbedded {
    @JsonProperty("items")
    private Contact[] contacts;

    public Contact[] getContacts() {
        return contacts;
    }

    @Override
    public String toString() {
        return "Embedded{" +
                "users=" + Arrays.toString(contacts) +
                '}';
    }
}
