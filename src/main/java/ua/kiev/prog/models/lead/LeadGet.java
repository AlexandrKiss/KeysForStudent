package ua.kiev.prog.models.lead;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LeadGet {
    @JsonProperty("_embedded")
    private LeadEmbedded leadEmbedded;

    public LeadEmbedded getLeadEmbedded() {
        return leadEmbedded;
    }

    @Override
    public String toString() {
        return "Get{" +
                "leadEmbedded=" + leadEmbedded +
                '}';
    }
}