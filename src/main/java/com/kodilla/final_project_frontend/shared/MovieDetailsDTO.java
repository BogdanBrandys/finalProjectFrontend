package com.kodilla.final_project_frontend.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MovieDetailsDTO {
    private String title;
    private String genre;
    private String year;
    private String director;
    private String plot;
    private List<RatingDTO> ratings;
}
