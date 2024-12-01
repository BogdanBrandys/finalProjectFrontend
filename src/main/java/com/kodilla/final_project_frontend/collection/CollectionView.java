package com.kodilla.final_project_frontend.collection;

import com.kodilla.final_project_frontend.main.MainLayout;
import com.kodilla.final_project_frontend.shared.*;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "collection", layout = MainLayout.class)
public class CollectionView extends VerticalLayout {

    private final VerticalLayout contentLayout = new VerticalLayout();
    private final Button backToSearchButton;

    public CollectionView() {
        // Header
        H1 title = new H1("Przeglądaj swoją kolekcję");
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

        // Movies layout
        contentLayout.setWidthFull();
        contentLayout.setSpacing(true);

        // To main view
        add(headerLayout, contentLayout);

        setAlignItems(Alignment.CENTER);
        setSizeFull();

        // loading movies
        loadMovies();
    }

    private void loadMovies() {
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
            String url = "http://localhost:8080/v1/movies";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<MovieDTO[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, MovieDTO[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                displayMovies(response.getBody());
            } else {
                Notification.show("Brak filmów w Twojej kolekcji.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd podczas ładowania kolekcji filmów: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void displayMovies(MovieDTO[] movies) {
        removeAll();

        H1 title = new H1("Przeglądaj swoją kolekcję");
        title.getStyle().set("text-align", "center");
        add(title);

        // // Return button again
        add(backToSearchButton);

        for (MovieDTO movie : movies) {
            // Div for every movie
            Div movieDiv = new Div();
            movieDiv.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("padding", "5px")
                    .set("margin-bottom", "10px")
                    .set("width", "100%");

            VerticalLayout movieLayout = new VerticalLayout();
            movieLayout.setSpacing(false);
            movieLayout.getStyle().set("gap", "3px");

            //Basic data
            Div titleDiv = new Div(new Text("Tytuł: " + (movie.getDetails().getTitle() != null ? movie.getDetails().getTitle() : "Brak tytułu")));
            titleDiv.getStyle().set("margin-bottom", "2px");

            Div genreDiv = new Div(new Text("Gatunek: " + (movie.getDetails().getGenre() != null ? movie.getDetails().getGenre() : "Brak gatunku")));
            genreDiv.getStyle().set("margin-bottom", "2px");

            Div yearDiv = new Div(new Text("Rok: " + (movie.getDetails().getYear() != null ? movie.getDetails().getYear() : "Brak roku")));
            yearDiv.getStyle().set("margin-bottom", "2px");

            Div directorDiv = new Div(new Text("Reżyser: " + (movie.getDetails().getDirector() != null ? movie.getDetails().getDirector() : "Brak reżysera")));
            directorDiv.getStyle().set("margin-bottom", "2px");

            Div plotDiv = new Div(new Text("Fabuła: " + (movie.getDetails().getPlot() != null ? movie.getDetails().getPlot() : "Brak fabuły")));
            plotDiv.getStyle().set("margin-bottom", "2px");

            // Add elements to VerticalLayout
            movieLayout.add(titleDiv, genreDiv, yearDiv, directorDiv, plotDiv);

            // Ratings
            List<RatingDTO> ratings = movie.getDetails().getRatings();
            String ratingsText = (ratings != null && !ratings.isEmpty())
                    ? ratings.stream().map(rating -> rating.getSource() + ": " + rating.getValue()).collect(Collectors.joining(", "))
                    : "Brak ocen";
            Div ratingsDiv = new Div(new Text("Oceny: " + ratingsText));
            ratingsDiv.getStyle().set("margin-bottom", "2px");
            movieLayout.add(ratingsDiv);

            // Providers
            List<MovieProviderDTO> subscriptions = movie.getProviders().getSubscription();
            String subscriptionsText = (subscriptions != null && !subscriptions.isEmpty())
                    ? subscriptions.stream().map(MovieProviderDTO::getProvider_name).collect(Collectors.joining(", "))
                    : "Brak dostępnych subskrypcji";
            Div subscriptionsDiv = new Div(new Text("Subskrypcje: " + subscriptionsText));
            subscriptionsDiv.getStyle().set("margin-bottom", "2px");
            movieLayout.add(subscriptionsDiv);

            List<MovieProviderDTO> rentals = movie.getProviders().getRental();
            String rentalsText = (rentals != null && !rentals.isEmpty())
                    ? rentals.stream().map(MovieProviderDTO::getProvider_name).collect(Collectors.joining(", "))
                    : "Brak możliwości wypożyczenia";
            Div rentalsDiv = new Div(new Text("Wypożyczenie: " + rentalsText));
            rentalsDiv.getStyle().set("margin-bottom", "2px");
            movieLayout.add(rentalsDiv);

            List<MovieProviderDTO> purchases = movie.getProviders().getPurchase();
            String purchasesText = (purchases != null && !purchases.isEmpty())
                    ? purchases.stream().map(MovieProviderDTO::getProvider_name).collect(Collectors.joining(", "))
                    : "Brak możliwości zakupu";
            Div purchasesDiv = new Div(new Text("Zakup: " + purchasesText));
            purchasesDiv.getStyle().set("margin-bottom", "2px");
            movieLayout.add(purchasesDiv);

            // Physical version
            PhysicalVersionDTO physicalVersion = movie.getPhysicalVersion();
            String physicalVersionText = (physicalVersion != null)
                    ? String.format("Opis: %s, Rok: %d, Steelbook: %s, Szczegóły: %s",
                    physicalVersion.getDescription(),
                    physicalVersion.getReleaseYear(),
                    physicalVersion.getSteelbook() ? "Tak" : "Nie",
                    physicalVersion.getDetails())
                    : "Brak wersji fizycznej";
            Div physicalVersionDiv = new Div(new Text("Wersja fizyczna: " + physicalVersionText));
            physicalVersionDiv.getStyle().set("margin-bottom", "2px");
            movieLayout.add(physicalVersionDiv);

            // Button to delete movie from favourite
            Button removeButton = new Button("Usuń film", event -> removeMovieFromFavorites(movie.getMovie_id()));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            // Button to add/change physical version
            Button addPhysicalVersionButton = new Button("Aktualizuj wersję fizyczną", event -> addPhysicalVersion(movie));
            addPhysicalVersionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            // Add buttons to layout
            HorizontalLayout buttonLayout = new HorizontalLayout(removeButton, addPhysicalVersionButton);
            buttonLayout.setSpacing(true);
            movieLayout.add(buttonLayout);

            // Add movie to main view
            movieDiv.add(movieLayout);
            add(movieDiv);
        }
    }
    private void removeMovieFromFavorites(Long movieId) {
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
            String url = "http://localhost:8080/v1/movies/" + movieId;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            // checking response
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                Notification.show("Film został usunięty z ulubionych.", 3000, Notification.Position.MIDDLE);
                loadMovies();
            } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                Notification.show("Nie znaleziono filmu do usunięcia.", 3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Błąd podczas usuwania filmu. Status: " + response.getStatusCode(), 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }
    private void addPhysicalVersion(MovieDTO movie) {
        Dialog physicalVersionDialog = new Dialog();

        // Form
        TextField descriptionField = new TextField("Opis");
        descriptionField.setRequired(false);

        TextField releaseYearField = new TextField("Rok wydania");
        releaseYearField.setRequired(false);

        Checkbox steelbookCheckbox = new Checkbox("Steelbook");
        steelbookCheckbox.setValue(false);

        TextArea detailsField = new TextArea("Szczegóły");
        detailsField.setRequired(false);

        // Movie title is adding automatically
        TextField movieTitleField = new TextField("Tytuł filmu");
        movieTitleField.setValue(movie.getDetails().getTitle());
        movieTitleField.setEnabled(false); //only read

        Button saveButton = new Button("Zapisz", event -> {
            try {
                // default values
                int releaseYear = 0;
                if (!releaseYearField.getValue().isEmpty()) {
                    releaseYear = Integer.parseInt(releaseYearField.getValue());
                }

                PhysicalVersionDTO physicalVersion = new PhysicalVersionDTO(
                        descriptionField.getValue().isEmpty() ? null : descriptionField.getValue(),
                        releaseYear,
                        steelbookCheckbox.getValue() ? true : null,
                        detailsField.getValue().isEmpty() ? null : detailsField.getValue(),
                        movieTitleField.getValue().isEmpty() ? null : movieTitleField.getValue()
                );

                // Send if one field is entered
                if (physicalVersion.getDescription() != null || physicalVersion.getReleaseYear() != 0 || physicalVersion.getDetails() != null || physicalVersion.getSteelbook() != null) {
                    savePhysicalVersion(movie.getMovie_id(), physicalVersion);
                    physicalVersionDialog.close();
                } else {
                    Notification.show("Proszę wypełnić przynajmniej jedno pole.", 3000, Notification.Position.MIDDLE);
                }
            } catch (NumberFormatException e) {
                Notification.show("Proszę podać poprawny rok wydania.", 3000, Notification.Position.MIDDLE);
            }
        });

        physicalVersionDialog.add(descriptionField, releaseYearField, steelbookCheckbox, detailsField, movieTitleField, saveButton);
        physicalVersionDialog.open();
    }

    private void savePhysicalVersion(Long movieId, PhysicalVersionDTO physicalVersion) {
        String token = (String) VaadinSession.getCurrent().getAttribute("token");
        if (token == null) {
            Notification.show("Nie jesteś zalogowany!", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            String url = "http://localhost:8080/v1/movies/" + movieId + "/physical";
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PhysicalVersionDTO> request = new HttpEntity<>(physicalVersion, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Notification.show("Wersja fizyczna została dodana.", 3000, Notification.Position.MIDDLE);
                loadMovies();
            } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                Notification.show("Nie znaleziono filmu do dodania wersji fizycznej.", 3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Błąd podczas dodawania wersji fizycznej. Status: " + response.getStatusCode(), 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }
}