package ua.kiev.prog.models.contatct;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContactGet {
    @JsonProperty("_embedded")
    private ContactEmbedded contactEmbedded;

    public ContactEmbedded getContactEmbedded() {
        return contactEmbedded;
    }

    @Override
    public String toString() {
        return "Get{" +
                "embedded=" + contactEmbedded +
                '}';
    }
}