package com.kodilla.final_project_frontend.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RatingDTO {
    @JsonProperty("Source")
    private String source;
    @JsonProperty("Value")
    private String value;
}
