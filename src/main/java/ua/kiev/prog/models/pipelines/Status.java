package ua.kiev.prog.models.pipelines;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Status {
    @JsonProperty("id")
    private int id;
    @JsonProperty("name")
    private String name;


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Statuses{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
