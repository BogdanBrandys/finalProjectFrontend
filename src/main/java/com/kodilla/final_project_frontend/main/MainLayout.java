package com.kodilla.final_project_frontend.main;

import com.kodilla.final_project_frontend.login.LoginView;
import com.kodilla.final_project_frontend.register.RegistrationView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;

@Route("")
public class MainLayout extends AppLayout {
    Tabs tabs = new Tabs();
    private final VerticalLayout content;

    public MainLayout() {
        // corner title
        Span appTitle = new Span("Movies");
        Span appTitle2 = new Span("Collection");
        appTitle.getStyle()
                .set("font-size", "14px")
                .set("position", "absolute")
                .set("top", "10px")
                .set("left", "10px");
        appTitle2.getStyle()
                .set("font-size", "14px")
                .set("position", "absolute")
                .set("top", "30px")
                .set("left", "10px");

        // Main section
        content = new VerticalLayout();
        content.setSizeFull();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        showWelcomeScreen();

        addToNavbar(appTitle, appTitle2);
        setContent(content);
    }

    private void showWelcomeScreen() {
        content.removeAll();

        // Title
        H1 welcomeTitle = new H1("Witaj w serwisie MoviesCollection!");
        H4 welcomeTitle2 = new H4("Znajdź film, dodaj informacje o fizycznych wersjach jakie posiadasz.");
        H4 welcomeTitle3 = new H4("Stwórz katalog swoich ulubionych filmów.");
        welcomeTitle.getStyle().set("text-align", "center");
        welcomeTitle2.getStyle().set("text-align", "center");
        welcomeTitle3.getStyle().set("text-align", "center");

        // Enter Button
        Button enterButton = new Button("Wejdź", event -> showMainScreen());
        enterButton.getStyle().set("margin-top", "60px");

        content.add(welcomeTitle, welcomeTitle2, welcomeTitle3, enterButton);
    }

    private void showMainScreen() {
        content.removeAll();

        H1 sectionTitle = new H1("MoviesCollection");
        sectionTitle.getStyle().set("text-align", "center");

        Tab loginTab = new Tab("Zaloguj się");
        Tab registerTab = new Tab("Zarejestruj się");

        // Dodanie zakładek do tabs
        tabs.add(loginTab, registerTab);
        tabs.setWidth("300px");
        tabs.setFlexGrowForEnclosedTabs(1);

        VerticalLayout dynamicContent = new VerticalLayout();
        dynamicContent.setSizeFull();
        dynamicContent.setAlignItems(FlexComponent.Alignment.CENTER);
        dynamicContent.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        VerticalLayout loginContent = createCenteredContent(new LoginView());
        VerticalLayout registerContent = createCenteredContent(new RegistrationView(this));

        tabs.addSelectedChangeListener(event -> {
            dynamicContent.removeAll();
            if (tabs.getSelectedTab().equals(loginTab)) {
                dynamicContent.add(loginContent);
            } else if (tabs.getSelectedTab().equals(registerTab)) {
                dynamicContent.add(registerContent);
            }
        });

        // Domyślnie pokaż widok logowania
        dynamicContent.add(loginContent);

        content.add(sectionTitle, tabs, dynamicContent);
    }

    private VerticalLayout createCenteredContent(VerticalLayout view) {
        VerticalLayout centeredContent = new VerticalLayout();
        centeredContent.setSizeFull();
        centeredContent.setAlignItems(FlexComponent.Alignment.CENTER);
        centeredContent.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        centeredContent.add(view);
        return centeredContent;
    }
    public void switchToLoginTab() {
        if (tabs != null) {
            tabs.setSelectedIndex(0); // Przełączenie na zakładkę "Zaloguj się"
        } else {
            System.out.println("Tabs jest null!");
        }
    }
}