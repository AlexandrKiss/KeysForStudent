package ua.kiev.prog.models.lead;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class LeadEmbedded {
    @JsonProperty("items")
    private Lead[] leads;

    public Lead[] getLeads() {
        return leads;
    }

    @Override
    public String toString() {
        return "LeadEmbedded{" +
                "leads=" + Arrays.toString(leads) +
                '}';
    }
}
