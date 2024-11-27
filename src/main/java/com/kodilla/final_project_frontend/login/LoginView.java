package com.kodilla.final_project_frontend.login;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodilla.final_project_frontend.main.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Route(value = "login", layout = MainLayout.class)
public class LoginView extends VerticalLayout {

    private final RestTemplate restTemplate;

    public LoginView() {
        this.restTemplate = new RestTemplate();

        // Username Field
        TextField usernameField = new TextField("Login");
        usernameField.setRequired(true);

        // password field
        PasswordField passwordField = new PasswordField("Hasło");
        passwordField.setRequired(true);

        // Przycisk logowania
        Button loginButton = new Button("Zaloguj się", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            try {
                // catch token
                String token = login(username, password);
                VaadinSession.getCurrent().setAttribute("token", token);
                UI.getCurrent().navigate("search");
            } catch (Exception e) {
                Notification.show("Login failed: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(usernameField, passwordField, loginButton);
        formLayout.setWidth("400px");
        formLayout.getStyle().set("margin", "auto");
        VerticalLayout formWrapper = new VerticalLayout(formLayout);
        formWrapper.setAlignItems(Alignment.CENTER);
        setAlignItems(Alignment.CENTER);
        setSizeFull();
        add(formWrapper);
    }

    private String login(String username, String password) {
        String backendUrl = "http://localhost:8080/v1/login"; // Backend address
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(backendUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String jsonResponse = response.getBody();

            String token = extractTokenFromResponse(jsonResponse);

            LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

            VaadinSession.getCurrent().setAttribute("token", token);
            VaadinSession.getCurrent().setAttribute("tokenExpiry", expiryDate);

            return token;
        } else {
            throw new RuntimeException("Błąd logowania.");
        }
    }
    private String extractTokenFromResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AuthResponse authResponse = objectMapper.readValue(jsonResponse, AuthResponse.class);
            return authResponse.getToken();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}