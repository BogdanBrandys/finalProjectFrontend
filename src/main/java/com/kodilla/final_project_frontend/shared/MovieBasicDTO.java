package com.kodilla.final_project_frontend.shared;

import lombok.Data;

@Data
public class MovieBasicDTO {
    private Long id;
    private String title;
    private String release_date;
    private boolean addedToFavorites;
}
