package ua.kiev.prog.models.pipelines;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pipeline {
    @JsonProperty("id")
    private int id;

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Pipeline{" +
                "id=" + id +
                '}';
    }
}