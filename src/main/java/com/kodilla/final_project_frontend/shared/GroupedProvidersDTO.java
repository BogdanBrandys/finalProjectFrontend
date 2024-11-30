package com.kodilla.final_project_frontend.shared;

import lombok.Data;

import java.util.List;

@Data
public class GroupedProvidersDTO {
    private List<MovieProviderDTO> rental;
    private List<MovieProviderDTO> subscription;
    private List<MovieProviderDTO> purchase;
}
