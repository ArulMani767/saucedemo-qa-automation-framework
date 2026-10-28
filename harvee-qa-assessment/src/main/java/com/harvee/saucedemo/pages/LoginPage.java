package com.harvee.saucedemo.pages;

import com.harvee.saucedemo.config.ConfigLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private static final By USERNAME_INPUT = By.cssSelector("[data-test='username']");
    private static final By PASSWORD_INPUT = By.cssSelector("[data-test='password']");
    private static final By LOGIN_BUTTON = By.cssSelector("[data-test='login-button']");
    private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(ConfigLoader.get("base.url"));
        waitForVisible(LOGIN_BUTTON);
        return this;
    }

    public LoginPage openPath(String relativePath) {
        String baseUrl = ConfigLoader.get("base.url").replaceAll("/+$", "");
        driver.get(baseUrl + relativePath);
        return this;
    }

    public InventoryPage loginAsValidUser(String username, String password) {
        type(USERNAME_INPUT, username);
        type(PASSWORD_INPUT, password);
        click(LOGIN_BUTTON);
        return new InventoryPage(driver);
    }

    public LoginPage attemptLoginExpectingError(String username, String password) {
        type(USERNAME_INPUT, username);
        type(PASSWORD_INPUT, password);
        click(LOGIN_BUTTON);
        return this;
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }
}
