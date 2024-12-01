package com.kodilla.final_project_frontend.stats;

import com.kodilla.final_project_frontend.main.MainLayout;
import com.kodilla.final_project_frontend.shared.StatsDTO;
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

@Route("stats")
public class StatsView extends VerticalLayout {

    private VerticalLayout contentLayout = new VerticalLayout();
    private StatsDTO stats;

    public StatsView() {
        // Header
        H1 title = new H1("Statystyki Mojej Kolekcji Filmów");
        title.getStyle().set("text-align", "center");
        title.getStyle().set("margin-top", "20px");

        // Return button
        Button backToSearchButton = new Button("Wróć do wyszukiwania filmów", event -> {
            UI.getCurrent().navigate("search");
        });
        backToSearchButton.getStyle().set("margin-bottom", "20px");

        // Header layout
        VerticalLayout headerLayout = new VerticalLayout(title, backToSearchButton);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setWidthFull();
        headerLayout.setHeight("20%");

        // Statistics layout
        contentLayout.setSizeFull();
        contentLayout.setSpacing(true);
        contentLayout.setAlignItems(Alignment.CENTER);

        add(headerLayout);

        add(contentLayout);

        // Show a loading indicator until the stats data is loaded
        Text loadingText = new Text("Ładowanie danych statystyk...");
        contentLayout.add(loadingText);

        loadStats();
    }

    private void loadStats() {
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
            String url = "http://localhost:8080/v1/movies/stats";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<StatsDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, StatsDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                stats = response.getBody();
                // Now that stats data is loaded, display it
                displayStats();
            } else {
                Notification.show("Błąd podczas ładowania danych statystyk.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd podczas ładowania danych statystyk: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void displayStats() {
        contentLayout.removeAll();

        if (stats != null) {
            // Layout to display the stats
            VerticalLayout statsLayout = new VerticalLayout();
            statsLayout.setSpacing(false);
            statsLayout.getStyle().set("gap", "3px");
            statsLayout.setAlignItems(Alignment.CENTER);

            // Stats details
            Div totalMoviesDiv = new Div(new Text("Liczba filmów: " + stats.getTotalMovies()));
            totalMoviesDiv.getStyle().set("margin-bottom", "2px");

            Div mostCommonGenreDiv = new Div(new Text("Najczęstszy gatunek: " + (stats.getMostCommonGenre() != null ? stats.getMostCommonGenre() : "Brak gatunku")));
            mostCommonGenreDiv.getStyle().set("margin-bottom", "2px");

            Div oldestMovieYearDiv = new Div(new Text("Najstarszy film: " + (stats.getOldestMovieYear() != null ? stats.getOldestMovieYear() : "Brak danych")));
            oldestMovieYearDiv.getStyle().set("margin-bottom", "2px");

            Div newestMovieYearDiv = new Div(new Text("Najmłodszy film: " + (stats.getNewestMovieYear() != null ? stats.getNewestMovieYear() : "Brak danych")));
            newestMovieYearDiv.getStyle().set("margin-bottom", "2px");

            statsLayout.add(totalMoviesDiv, mostCommonGenreDiv, oldestMovieYearDiv, newestMovieYearDiv);

            // Add stats layout to the main content
            contentLayout.add(statsLayout);
        } else {
            contentLayout.add(new Text("Brak danych statystyk."));
        }
    }
}