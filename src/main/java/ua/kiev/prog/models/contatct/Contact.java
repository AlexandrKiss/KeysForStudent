package ua.kiev.prog.models.contatct;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contact {
    @JsonProperty("id")
    private int id;
    @JsonProperty("name")
    private String name;

    @JsonProperty("leads")
    private Leads leads;

    public String getName() {
        return name;
    }

    public Leads getLeads() {
        return leads;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", leads=" + leads +
                '}';
    }
}
