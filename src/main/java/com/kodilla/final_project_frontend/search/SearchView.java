package com.kodilla.final_project_frontend.search;

import com.kodilla.final_project_frontend.collection.CollectionView;
import com.kodilla.final_project_frontend.main.MainLayout;
import com.kodilla.final_project_frontend.physical.PhysicalView;
import com.kodilla.final_project_frontend.shared.MovieBasicDTO;
import com.kodilla.final_project_frontend.shared.MovieDTO;
import com.kodilla.final_project_frontend.stats.StatsView;
import com.kodilla.final_project_frontend.user.UserView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Route(value = "search", layout = MainLayout.class)
public class SearchView extends VerticalLayout {

    private final Grid<MovieBasicDTO> movieGrid = new Grid<>();

    public SearchView() {
        // Nav bar after login
        HorizontalLayout navigationBar = createNavigationBar();
        add(navigationBar);

        // Header
        H1 title = new H1("Wyszukaj film");
        title.getStyle().set("text-align", "center");

        // Search field
        TextField titleField = new TextField("Tytuł filmu");

        // Search button
        Button searchButton = new Button("Szukaj", event -> {
            String movieTitle = titleField.getValue();
            if (movieTitle.isEmpty()) {
                Notification.show("Proszę podać tytuł filmu!", 3000, Notification.Position.MIDDLE);
            } else {
                searchMovies(movieTitle);
            }
        });

        movieGrid.addColumn(MovieBasicDTO::getId).setHeader("Numer ID").setAutoWidth(true);
        movieGrid.addColumn(MovieBasicDTO::getTitle).setHeader("Tytuł").setAutoWidth(true);
        movieGrid.addColumn(MovieBasicDTO::getRelease_date).setHeader("Data wydania").setAutoWidth(true);

        movieGrid.addComponentColumn(movie -> {
            Button addButton = new Button("Dodaj do bazy", event -> {
                addMovieToDatabase(movie);
            });

            updateAddButtonState(addButton, movie);

            return addButton;
        }).setAutoWidth(true);

        movieGrid.setSizeFull();
        movieGrid.setVisible(false);

        add(title, titleField, searchButton, movieGrid);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setSizeFull();
    }

    private HorizontalLayout createNavigationBar() {
        Button myCollectionButton = new Button("Moja kolekcja", event -> navigateToCollection());
        Button editProfileButton = new Button("Dane użytkownika", event -> navigateToEditProfile());
        Button physicalCollection = new Button("Moja fizyczna kolekcja", event -> navigateToPhysicalCollection());
        Button statsButton = new Button("Statystyka kolekcji", event -> navigateToStats());

        HorizontalLayout navBar = new HorizontalLayout(myCollectionButton, physicalCollection, statsButton, editProfileButton);
        navBar.setSpacing(true);
        navBar.setJustifyContentMode(JustifyContentMode.START);
        return navBar;
    }

    private void navigateToCollection() {
        UI.getCurrent().navigate(CollectionView.class);
    }

    private void navigateToEditProfile() {
        UI.getCurrent().navigate(UserView.class);
    }

    private void navigateToPhysicalCollection() {
        UI.getCurrent().navigate(PhysicalView.class);
    }

    private void navigateToStats() {
        UI.getCurrent().navigate(StatsView.class);
    }

    private void updateAddButtonState(Button addButton, MovieBasicDTO movie) {
        if (movie.isAddedToFavorites()) {
            addButton.setText("Film już dodany");
            addButton.setEnabled(false);
            addButton.getElement().getStyle()
                    .set("background-color", "gray")
                    .set("color", "white");
        } else {
            addButton.setText("Dodaj do bazy");
            addButton.setEnabled(true);
            addButton.getElement().getStyle()
                    .set("background-color", "#0074E8")
                    .set("color", "white")
                    .set("border", "none")
                    .set("border-radius", "5px");
        }
    }

    private void searchMovies(String movieTitle) {
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
            String encodedTitle = URLEncoder.encode(movieTitle, StandardCharsets.UTF_8);
            String url = "http://localhost:8080/v1/tmdb/search?title=" + encodedTitle;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<MovieBasicDTO[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, MovieBasicDTO[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                movieGrid.setItems(response.getBody());
                movieGrid.setVisible(true);
            } else {
                Notification.show("Brak wyników dla podanego tytułu.", 3000, Notification.Position.MIDDLE);
            }
        } catch (HttpClientErrorException e) {
            Notification.show("Błąd klienta: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (HttpServerErrorException e) {
            Notification.show("Błąd serwera: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Nieoczekiwany błąd: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void addMovieToDatabase(MovieBasicDTO movie) {
        String token = (String) VaadinSession.getCurrent().getAttribute("token");

        if (token == null) {
            Notification.show("Nie jesteś zalogowany!", 3000, Notification.Position.MIDDLE);
            return;
        }

        MovieBasicDTO movieBasicDTO = new MovieBasicDTO();
        movieBasicDTO.setId(Long.valueOf(movie.getId()));
        movieBasicDTO.setTitle(movie.getTitle());
        movieBasicDTO.setRelease_date(movie.getRelease_date());

        try {
            String url = "http://localhost:8080/v1/movies/add";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MovieBasicDTO> request = new HttpEntity<>(movieBasicDTO, headers);

            ResponseEntity<MovieDTO> response = restTemplate.exchange(url, HttpMethod.POST, request, MovieDTO.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Notification.show("Film dodany do bazy!", 3000, Notification.Position.MIDDLE);

                movie.setAddedToFavorites(true);

                movieGrid.getDataProvider().refreshItem(movie);

                movieGrid.getDataProvider().refreshAll();
            } else {
                Notification.show("Nie udało się dodać filmu.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd podczas dodawania filmu: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}