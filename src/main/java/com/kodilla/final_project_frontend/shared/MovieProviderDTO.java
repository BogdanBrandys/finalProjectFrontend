package com.kodilla.final_project_frontend.shared;

import lombok.Data;

@Data
public class MovieProviderDTO {
    private String provider_name;
    private MovieProvider.AccessType accessType;
}
