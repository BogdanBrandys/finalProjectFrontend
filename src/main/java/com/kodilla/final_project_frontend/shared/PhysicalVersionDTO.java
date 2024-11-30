package com.kodilla.final_project_frontend.shared;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PhysicalVersionDTO {
    private String description;
    private int releaseYear;
    private Boolean steelbook;
    private String details;
    private String movieTitle;
}
