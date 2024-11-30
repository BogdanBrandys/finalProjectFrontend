package com.kodilla.final_project_frontend.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MovieDetailsDTO {
    @JsonProperty("Title")
    private String title;
    @JsonProperty("Genre")
    private String genre;
    @JsonProperty("Year")
    private String year;
    @JsonProperty("Director")
    private String director;
    @JsonProperty("Plot")
    private String plot;
    @JsonProperty("Ratings")
    private List<RatingDTO> ratings;
}
