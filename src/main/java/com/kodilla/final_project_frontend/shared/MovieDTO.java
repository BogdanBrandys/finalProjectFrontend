package com.kodilla.final_project_frontend.shared;

import lombok.Data;

@Data
public class MovieDTO {
    private Long movie_id;
    private Long tmdbId;
    private MovieDetailsDTO details;
    private GroupedProvidersDTO providers;
    private PhysicalVersionDTO physicalVersion;
}
