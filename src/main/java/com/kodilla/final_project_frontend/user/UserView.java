package com.kodilla.final_project_frontend.user;

import com.kodilla.final_project_frontend.main.MainLayout;
import com.kodilla.final_project_frontend.shared.UserDTO;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Route(value = "user", layout = MainLayout.class)
public class UserView extends VerticalLayout {

    private final VerticalLayout contentLayout = new VerticalLayout();
    private Button backToSearchButton;
    private Button updateUserButton;
    private Button deleteUserButton;
    private UserDTO user;

    public UserView() {
        // Header
        H1 title = new H1("Moje dane");
        title.getStyle().set("text-align", "center");
        title.getStyle().set("margin-top", "20px");

        backToSearchButton = new Button("Wróć do wyszukiwania filmów", event -> {
            UI.getCurrent().navigate("search");
        });
        backToSearchButton.getStyle().set("margin-bottom", "20px");

        // Header layout
        VerticalLayout headerLayout = new VerticalLayout(title, backToSearchButton);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setWidthFull();
        headerLayout.setHeight("20%");

        // Main layout
        contentLayout.setSizeFull();
        contentLayout.setSpacing(true);
        contentLayout.setAlignItems(Alignment.CENTER);

        add(headerLayout);

        add(contentLayout);

        // Show a loading indicator until the user data is loaded
        Text loadingText = new Text("Ładowanie danych użytkownika...");
        contentLayout.add(loadingText);

        loadUser();
    }

    private void loadUser() {
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
            String url = "http://localhost:8080/v1/users/user";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, UserDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                user = response.getBody();
                displayUser();
            } else {
                Notification.show("Błąd podczas ładowania danych użytkownika.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd podczas ładowania danych użytkownika: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void displayUser() {
        contentLayout.removeAll();

        if (user != null) {
            VerticalLayout userLayout = new VerticalLayout();
            userLayout.setSpacing(false);
            userLayout.getStyle().set("gap", "3px");
            userLayout.setAlignItems(Alignment.CENTER);

            // User details
            Div firstNameDiv = new Div(new Text("Imię: " + (user.getFirst_name() != null ? user.getFirst_name() : "Brak imienia")));
            firstNameDiv.getStyle().set("margin-bottom", "2px");

            Div lastNameDiv = new Div(new Text("Nazwisko: " + (user.getLast_name() != null ? user.getLast_name() : "Brak nazwiska")));
            lastNameDiv.getStyle().set("margin-bottom", "2px");

            Div emailDiv = new Div(new Text("Email: " + (user.getEmail() != null ? user.getEmail() : "Brak emaila")));
            emailDiv.getStyle().set("margin-bottom", "2px");

            Div usernameDiv = new Div(new Text("Nazwa użytkownika: " + (user.getUsername() != null ? user.getUsername() : "Brak nazwy użytkownika")));
            usernameDiv.getStyle().set("margin-bottom", "2px");

            Div rolesDiv = new Div(new Text("Role: " + user.getRoles().stream().map(role -> role.getName()).collect(Collectors.joining(", "))));
            rolesDiv.getStyle().set("margin-bottom", "2px");

            userLayout.add(firstNameDiv, lastNameDiv, emailDiv, usernameDiv, rolesDiv);

            // Update buttons
            updateUserButton = new Button("Zaktualizuj dane", event -> showUpdateUserDialog(user));
            updateUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            deleteUserButton = new Button("Usuń użytkownika", event -> showDeleteConfirmationDialog());
            deleteUserButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            HorizontalLayout buttonLayout = new HorizontalLayout(updateUserButton, deleteUserButton);
            buttonLayout.setSpacing(true);
            buttonLayout.getStyle().set("margin-top", "20px");

            userLayout.add(buttonLayout);

            // Add user layout to the main content
            contentLayout.add(userLayout);
        } else {
            contentLayout.add(new Text("Brak danych użytkownika."));
        }
    }

    private void showUpdateUserDialog(UserDTO user) {
        // Dialog window
        Dialog dialog = new Dialog();

        dialog.add(new H2("Aktualizuj dane użytkownika"));

        // Dialog form
        TextField firstNameField = new TextField("Imię", user.getFirst_name());
        firstNameField.setValue(user.getFirst_name()); //actual data

        TextField lastNameField = new TextField("Nazwisko", user.getLast_name());
        lastNameField.setValue(user.getLast_name());

        TextField usernameField = new TextField("Nazwa użytkownika");
        usernameField.setValue(user.getUsername());
        usernameField.setEnabled(false); // Cant change username

        TextField emailField = new TextField("Adres e-mail");
        emailField.setValue(user.getEmail());

        // Save changes
        Button saveButton = new Button("Zapisz", event -> {
            user.setFirst_name(firstNameField.getValue());
            user.setLast_name(lastNameField.getValue());
            user.setUsername(usernameField.getValue());
            user.setEmail(emailField.getValue());

            updateUser(user);

            dialog.close(); //close dialog
        });

        Button cancelButton = new Button("Anuluj", event -> dialog.close());

        dialog.getFooter().add(saveButton, cancelButton);

        dialog.add(firstNameField, lastNameField, usernameField, emailField);

        dialog.open();
    }

    private void updateUser(UserDTO user) {
        String token = (String) VaadinSession.getCurrent().getAttribute("token");

        if (token == null) {
            Notification.show("Nie jesteś zalogowany!", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            String url = "http://localhost:8080/v1/users/update";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UserDTO> request = new HttpEntity<>(user, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, Void.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Notification.show("Dane użytkownika zostały zaktualizowane.", 3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Błąd podczas aktualizacji danych użytkownika.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Błąd podczas łączenia z serwerem: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void showDeleteConfirmationDialog() {
        // New dialog
        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setHeaderTitle("Potwierdzenie");

        Paragraph message = new Paragraph("Czy na pewno chcesz usunąć swoje konto?");
        confirmationDialog.add(message);

        Button confirmButton = new Button("Potwierdź", event -> {
            String token = (String) VaadinSession.getCurrent().getAttribute("token");
            if (token != null) {
                try {
                    String url = "http://localhost:8080/v1/users/delete";
                    RestTemplate restTemplate = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(token);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Void> request = new HttpEntity<>(headers);
                    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

                    if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                        Notification.show("Twoje konto zostało usunięte.", 3000, Notification.Position.MIDDLE);
                        UI.getCurrent().navigate("");
                    } else {
                        Notification.show("Błąd podczas usuwania konta.", 3000, Notification.Position.MIDDLE);
                    }
                } catch (Exception e) {
                    Notification.show("Błąd podczas usuwania konta: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            }
            confirmationDialog.close();
        });

        Button cancelButton = new Button("Anuluj", event -> confirmationDialog.close());

        confirmationDialog.getFooter().add(confirmButton, cancelButton);

        confirmationDialog.open();
    }
}