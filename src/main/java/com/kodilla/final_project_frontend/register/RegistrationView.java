package com.kodilla.final_project_frontend.register;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodilla.final_project_frontend.main.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "register", layout = MainLayout.class)
public class RegistrationView extends VerticalLayout {

    private final MainLayout mainLayout;
    private final RestTemplate restTemplate;

    public RegistrationView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        this.restTemplate = new RestTemplate();

        // Registration fields
        TextField firstnameField = new TextField("Imię");
        firstnameField.setRequired(true);
        firstnameField.setRequiredIndicatorVisible(true);
        firstnameField.setErrorMessage("Imię jest wymagane");
        TextField lastnameField = new TextField("Nazwisko");
        lastnameField.setRequired(true);
        lastnameField.setRequiredIndicatorVisible(true);
        lastnameField.setErrorMessage("Nazwisko jest wymagane");
        TextField usernameField = new TextField("Nazwa Użytkownika");
        usernameField.setRequired(true);
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setErrorMessage("Nazwa użytkownika jest wymagane");
        TextField emailField = new TextField("Adres Email");
        emailField.setRequired(true);
        emailField.setRequiredIndicatorVisible(true);
        emailField.setErrorMessage("Email jest wymagane");
        PasswordField passwordField = new PasswordField("Hasło");
        passwordField.setRequired(true);
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setErrorMessage("Hasło jest wymagane");
        PasswordField confirmPasswordField = new PasswordField("Potwierdź hasło");
        confirmPasswordField.setRequired(true);

        // registration button
        Button registerButton = new Button("Zarejestruj się", event -> {
            String firstname = firstnameField.getValue();
            String lastname = lastnameField.getValue();
            String username = usernameField.getValue();
            String email = emailField.getValue();
            String password = passwordField.getValue();
            String confirmPassword = confirmPasswordField.getValue();

            if (firstname.isEmpty() || lastname.isEmpty() || username.isEmpty() ||
                    email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Notification.show("Wszystkie pola są wymagane", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                Notification.show("Podane hasła nie pasują do siebie");
                return;
            }

            List<Map<String, Object>> roles = new ArrayList<>();
            Map<String, Object> role = new HashMap<>();
            role.put("name", null);
            roles.add(role);

            try {
                register(firstname, lastname, username, email, password, roles);
                Notification.show("Zarejestrowałeś się!");
            } catch (HttpClientErrorException.BadRequest ex) {
                try {
                    // read JSON
                    String responseBody = ex.getResponseBodyAsString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> errors = objectMapper.readValue(responseBody, new TypeReference<>() {});

                    if (errors.containsKey("email")) {
                        emailField.setErrorMessage(errors.get("email"));
                        emailField.setInvalid(true);
                    }
                    if (errors.containsKey("password")) {
                        passwordField.setErrorMessage(errors.get("password"));
                        passwordField.setInvalid(true);
                    }
                } catch (Exception parseException) {
                    Notification.show("Nie udało się przetworzyć odpowiedzi z serwera.", 5000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Rejestracja nieudana: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(firstnameField, lastnameField, usernameField, emailField, passwordField, confirmPasswordField, registerButton);
        formLayout.setWidth("400px");
        formLayout.getStyle().set("margin", "auto");
        VerticalLayout formWrapper = new VerticalLayout(formLayout);
        formWrapper.setAlignItems(Alignment.CENTER);
        setAlignItems(Alignment.CENTER);
        setSizeFull();
        add(formWrapper);
    }

    private void register(String firstname, String lastname, String username, String email, String password, List<Map<String, Object>> roles) {
        String backendUrl = "http://localhost:8080/v1/users/register"; // Adres backendu
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("first_name", firstname);
        body.put("last_name", lastname);
        body.put("email", email);
        body.put("username", username);
        body.put("password", password);
        body.put("roles", roles);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(backendUrl, request, String.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to register");
        }
    }
}
