package com.kodilla.final_project_frontend.collection;

import com.kodilla.final_project_frontend.main.MainLayout;
import com.kodilla.final_project_frontend.shared.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
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

    private final Grid<MovieDTO> movieGrid = new Grid<>();

    public CollectionView() {
        // Nagłówek
        H1 title = new H1("Przeglądaj swoją kolekcję");
        title.getStyle().set("text-align", "center");

        // Przycisk powrotu do wyszukiwania filmów
        Button backToSearchButton = new Button("Wróć do wyszukiwania filmów", event -> {
            UI.getCurrent().navigate("search");
        });

        // Konfiguracja kolumn
        movieGrid.addColumn(MovieDTO::getMovie_id).setHeader("ID Filmu").setAutoWidth(true);
        movieGrid.addColumn(MovieDTO::getTmdbId).setHeader("TMDB ID").setAutoWidth(true);
        movieGrid.getElement().getStyle().set("font-size", "12px");

        movieGrid.addColumn(movie -> {
            String movieTitle = movie.getDetails().getTitle();
            return (movieTitle == null || movieTitle.isEmpty()) ? "Brak tytułu" : movieTitle;
        }).setHeader("Tytuł").setAutoWidth(true);

        movieGrid.addColumn(movie -> {
            String genre = movie.getDetails().getGenre();
            return (genre == null || genre.isEmpty()) ? "Brak gatunku" : genre;
        }).setHeader("Gatunek").setAutoWidth(true);

        movieGrid.addColumn(movie -> {
            String year = movie.getDetails().getYear();
            return (year == null || year.isEmpty()) ? "Brak roku" : year;
        }).setHeader("Rok").setAutoWidth(true);

        movieGrid.addColumn(movie -> {
            String director = movie.getDetails().getDirector();
            return (director == null || director.isEmpty()) ? "Brak reżysera" : director;
        }).setHeader("Reżyser").setAutoWidth(true);

        movieGrid.addColumn(movie -> {
            String plot = movie.getDetails().getPlot();
            return (plot == null || plot.isEmpty()) ? "Brak fabuły" : plot;
        }).setHeader("Fabuła").setAutoWidth(true);

        // Kolumna ocen
        movieGrid.addColumn(movie -> {
            List<RatingDTO> ratings = movie.getDetails().getRatings();
            return (ratings == null || ratings.isEmpty())
                    ? "Brak ocen"
                    : ratings.stream()
                    .map(rating -> rating.getSource() + ": " + rating.getValue())
                    .collect(Collectors.joining(", "));
        }).setHeader("Oceny").setAutoWidth(true);

        // Kolumna subskrypcji
        movieGrid.addColumn(movie -> {
            List<MovieProviderDTO> subscriptions = movie.getProviders().getSubscription();
            return (subscriptions == null || subscriptions.isEmpty())
                    ? "Brak dostępnych subskrypcji"
                    : subscriptions.stream()
                    .map(MovieProviderDTO::getProvider_name)
                    .collect(Collectors.joining(", "));
        }).setHeader("Subskrypcja").setAutoWidth(true);

        // Kolumna wypożyczeń
        movieGrid.addColumn(movie -> {
            List<MovieProviderDTO> rentals = movie.getProviders().getRental();
            return (rentals == null || rentals.isEmpty())
                    ? "Brak możliwości wypożyczenia"
                    : rentals.stream()
                    .map(MovieProviderDTO::getProvider_name)
                    .collect(Collectors.joining(", "));
        }).setHeader("Wypożyczenie").setAutoWidth(true);

        // Kolumna zakupów
        movieGrid.addColumn(movie -> {
            List<MovieProviderDTO> purchases = movie.getProviders().getPurchase();
            return (purchases == null || purchases.isEmpty())
                    ? "Brak możliwości zakupu"
                    : purchases.stream()
                    .map(MovieProviderDTO::getProvider_name)
                    .collect(Collectors.joining(", "));
        }).setHeader("Zakup").setAutoWidth(true);

        // Kolumna wersji fizycznych
        movieGrid.addColumn(movie -> {
            PhysicalVersionDTO physicalVersion = movie.getPhysicalVersion();
            return (physicalVersion == null)
                    ? "Brak wersji fizycznej"
                    : String.format("Opis: %s, Rok: %d, Steelbook: %s, Szczegóły: %s",
                    physicalVersion.getDescription(),
                    physicalVersion.getReleaseYear(),
                    physicalVersion.getSteelbook() ? "Tak" : "Nie",
                    physicalVersion.getDetails());
        }).setHeader("Wersja Fizyczna").setAutoWidth(true);

        movieGrid.addComponentColumn(movie -> {
            Button removeButton = new Button("Usuń film", event -> removeMovieFromFavorites(movie.getMovie_id()));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button addPhysicalVersionButton = new Button("Aktualizuj wersję fizyczną", event -> addPhysicalVersion(movie));
            addPhysicalVersionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            HorizontalLayout buttonLayout = new HorizontalLayout(removeButton, addPhysicalVersionButton);
            buttonLayout.setSpacing(true);
            return buttonLayout;
        }).setHeader("Operacje").setAutoWidth(true);

        movieGrid.setSizeFull();

        // Ładowanie filmów
        loadMovies();

        // Dodanie komponentów do widoku
        add(title, backToSearchButton, movieGrid);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setSizeFull();
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
                movieGrid.setItems(response.getBody());
            } else {
                Notification.show("Brak filmów w Twojej kolekcji.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd podczas ładowania kolekcji filmów: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
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
            // URL do Twojego endpointu w backendzie
            String url = "http://localhost:8080/v1/movies/" + movieId;

            // Przygotowanie obiektu RestTemplate i nagłówków z tokenem
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON); // Dodaj Content-Type jeśli potrzebne

            // Tworzymy żądanie z odpowiednimi nagłówkami
            HttpEntity<?> request = new HttpEntity<>(headers);

            // Wywołanie metody DELETE na backendzie
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            // Sprawdzamy odpowiedź
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                Notification.show("Film został usunięty z ulubionych.", 3000, Notification.Position.MIDDLE);
                loadMovies();  // Odświeżenie listy filmów po usunięciu
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

        // Formularz dodawania wersji fizycznej
        TextField descriptionField = new TextField("Opis");
        descriptionField.setRequired(false);  // Ustawienie jako nieobowiązkowe

        TextField releaseYearField = new TextField("Rok wydania");
        releaseYearField.setRequired(false);  // Ustawienie jako nieobowiązkowe

        Checkbox steelbookCheckbox = new Checkbox("Steelbook");
        steelbookCheckbox.setValue(false);  // Ustawienie checkboxa na niezaznaczone (domyślnie)

        TextArea detailsField = new TextArea("Szczegóły");
        detailsField.setRequired(false);  // Ustawienie jako nieobowiązkowe

        // Pole z tytułem filmu, które jest automatycznie ustawiane na tytuł filmu z obiektu MovieDTO
        TextField movieTitleField = new TextField("Tytuł filmu");
        movieTitleField.setValue(movie.getDetails().getTitle());  // Automatycznie ustaw tytuł na tytuł filmu
        movieTitleField.setEnabled(false); // Ustawienie pola jako tylko do odczytu

        Button saveButton = new Button("Zapisz", event -> {
            try {
                // Parsowanie roku wydania jako Integer lub ustawienie domyślnej wartości 0
                int releaseYear = 0;
                if (!releaseYearField.getValue().isEmpty()) {
                    releaseYear = Integer.parseInt(releaseYearField.getValue());
                }

                // Tworzymy obiekt PhysicalVersionDTO, ustawiając wartości na null, jeśli pole jest puste
                PhysicalVersionDTO physicalVersion = new PhysicalVersionDTO(
                        descriptionField.getValue().isEmpty() ? null : descriptionField.getValue(),
                        releaseYear,  // Ustawienie na 0, jeśli pole jest puste
                        steelbookCheckbox.getValue() ? true : null,  // Jeśli nie zaznaczone, ustawiamy null
                        detailsField.getValue().isEmpty() ? null : detailsField.getValue(),
                        movieTitleField.getValue().isEmpty() ? null : movieTitleField.getValue()  // Jeśli pole puste, ustawiamy null
                );

                // Wyślij tylko wtedy, gdy chociaż jedno pole zostało wypełnione
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

            // Ustawienie nagłówka na 'application/json'
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON); // Ważna zmiana

            HttpEntity<PhysicalVersionDTO> request = new HttpEntity<>(physicalVersion, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

            // Sprawdzamy odpowiedź
            if (response.getStatusCode() == HttpStatus.OK) {
                Notification.show("Wersja fizyczna została dodana.", 3000, Notification.Position.MIDDLE);
                loadMovies();  // Przeładuj listę filmów
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