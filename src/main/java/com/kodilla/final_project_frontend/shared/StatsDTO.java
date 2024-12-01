package com.kodilla.final_project_frontend.shared;

import lombok.Data;

@Data
public class StatsDTO {
    private int totalMovies;
    private String mostCommonGenre;
    private String oldestMovieYear;
    private String newestMovieYear;
}
