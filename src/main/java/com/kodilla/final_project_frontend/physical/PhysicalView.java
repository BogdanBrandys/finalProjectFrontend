package com.kodilla.final_project_frontend.physical;

import com.kodilla.final_project_frontend.main.MainLayout;
import com.kodilla.final_project_frontend.shared.PhysicalVersionDTO;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Route(value = "physical-collection", layout = MainLayout.class)
public class PhysicalView extends VerticalLayout {

    private final VerticalLayout contentLayout = new VerticalLayout();
    private final Button backToSearchButton;

    public PhysicalView() {
        // Header
        H1 title = new H1("Twoja fizyczna kolekcja");
        title.getStyle().set("text-align", "center");

        // Return button
        backToSearchButton = new Button("Wróć do wyszukiwania filmów", event -> {
            UI.getCurrent().navigate("search");
        });
        backToSearchButton.getStyle().set("margin-bottom", "20px");

        // Header layout
        VerticalLayout headerLayout = new VerticalLayout();
        headerLayout.add(title, backToSearchButton);
        headerLayout.setAlignItems(Alignment.CENTER);

        contentLayout.setWidthFull();
        contentLayout.setSpacing(true);

        // Adding contentLayout and headerLayout to mainView
        add(headerLayout, contentLayout);

        setAlignItems(Alignment.CENTER);
        setSizeFull();

        loadPhysicalVersions();
    }

    private void loadPhysicalVersions() {
        String token = (String) VaadinSession.getCurrent().getAttribute("token");

        if (token == null) {
            Notification.show("Nie jesteś zalogowany!", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate(MainLayout.class);
            return;
        }

        LocalDateTime tokenExpiry = (LocalDateTime) VaadinSession.getCurrent().getAttribute("tokenExpiry");

        if (tokenExpiry == null || tokenExpiry.isBefore(LocalDateTime.now())) {
            Notification.show("Token wygasł. Zaloguj się ponownie.", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate(MainLayout.class);
            return;
        }

        try {
            String url = "http://localhost:8080/v1/movies/physical"; // Endpoint dla wersji fizycznych
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<PhysicalVersionDTO[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PhysicalVersionDTO[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                displayPhysicalVersions(response.getBody());
            } else {
                Notification.show("Brak fizycznych wersji filmów w Twojej kolekcji.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd podczas ładowania kolekcji wersji fizycznych: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void displayPhysicalVersions(PhysicalVersionDTO[] physicalVersions) {
        removeAll();

        H1 title = new H1("Twoja fizyczna kolekcja");
        title.getStyle().set("text-align", "center");
        add(title);

        // Add return button again
        add(backToSearchButton);

        for (PhysicalVersionDTO physicalVersion : physicalVersions) {
            Div physicalVersionDiv = new Div();
            physicalVersionDiv.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("padding", "5px")
                    .set("margin-bottom", "10px")
                    .set("width", "100%");

            VerticalLayout physicalVersionLayout = new VerticalLayout();
            physicalVersionLayout.setSpacing(false);
            physicalVersionLayout.getStyle().set("gap", "3px");

            Div titleDiv = new Div(new Text("Tytuł: " + (physicalVersion.getMovieTitle() != null ? physicalVersion.getMovieTitle() : "Brak tytułu")));
            titleDiv.getStyle().set("margin-bottom", "2px");

            Div descriptionDiv = new Div(new Text("Opis: " + (physicalVersion.getDescription() != null ? physicalVersion.getDescription() : "Brak opisu")));
            descriptionDiv.getStyle().set("margin-bottom", "2px");

            Div releaseYearDiv = new Div(new Text("Rok: " + physicalVersion.getReleaseYear()));
            releaseYearDiv.getStyle().set("margin-bottom", "2px");

            Div steelbookDiv = new Div(new Text("Steelbook: " + (physicalVersion.getSteelbook() != null ? (physicalVersion.getSteelbook() ? "Tak" : "Nie") : "Brak informacji")));
            steelbookDiv.getStyle().set("margin-bottom", "2px");

            Div detailsDiv = new Div(new Text("Szczegóły: " + (physicalVersion.getDetails() != null ? physicalVersion.getDetails() : "Brak szczegółów")));
            detailsDiv.getStyle().set("margin-bottom", "2px");

            // Add elements to VerticalLayout
            physicalVersionLayout.add(titleDiv, descriptionDiv, releaseYearDiv, steelbookDiv, detailsDiv);

            // physical version to main view
            physicalVersionDiv.add(physicalVersionLayout);
            add(physicalVersionDiv);
        }
    }
}