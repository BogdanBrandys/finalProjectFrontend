package com.kodilla.final_project_frontend.collection;

import com.kodilla.final_project_frontend.main.MainLayout;
import com.kodilla.final_project_frontend.shared.MovieBasicDTO;
import com.kodilla.final_project_frontend.shared.MovieDetailsDTO;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "collection", layout = MainLayout.class)
public class CollectionView extends VerticalLayout {

    private final Grid<MovieDetailsDTO> movieGrid = new Grid<>();

    public CollectionView() {
        // Nagłówek
        H1 title = new H1("Przeglądaj swoją kolekcję");
        title.getStyle().set("text-align", "center");

        // Przycisk powrotu do wyszukiwania filmów
        Button backToSearchButton = new Button("Wróć do wyszukiwania filmów", event -> {
            UI.getCurrent().navigate("search");
        });
    }
}