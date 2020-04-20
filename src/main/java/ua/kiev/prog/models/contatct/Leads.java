package ua.kiev.prog.models.contatct;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class Leads {
    @JsonProperty("id")
    private int[] leads;

    public int[] getLeads() {
        return leads;
    }

    @Override
    public String toString() {
        return "Leads{" +
                "lead=" + Arrays.toString(leads) +
                '}';
    }
}
