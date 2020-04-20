package ua.kiev.prog.models.lead;

import com.fasterxml.jackson.annotation.JsonProperty;
import ua.kiev.prog.models.pipelines.Pipeline;

public class Lead {
    @JsonProperty("id")
    private int id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("status_id")
    private long status;
    @JsonProperty("sale")
    private float sale;
    @JsonProperty("pipeline")
    private Pipeline pipeline;

    public long getStatus() {
        return status;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public String toString() {
        return "Lead{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", sale=" + sale +
                ", pipeline=" + pipeline +
                '}';
    }
}
