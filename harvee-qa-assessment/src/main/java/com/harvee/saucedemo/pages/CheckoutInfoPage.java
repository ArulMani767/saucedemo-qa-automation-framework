package com.harvee.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutInfoPage extends BasePage {
    private static final By FIRST_NAME_INPUT = By.cssSelector("[data-test='firstName']");
    private static final By LAST_NAME_INPUT = By.cssSelector("[data-test='lastName']");
    private static final By POSTAL_CODE_INPUT = By.cssSelector("[data-test='postalCode']");
    private static final By CONTINUE_BUTTON = By.cssSelector("[data-test='continue']");
    private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");

    public CheckoutInfoPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutOverviewPage submitCustomerInfo(String firstName, String lastName, String postalCode) {
        fillForm(firstName, lastName, postalCode);
        click(CONTINUE_BUTTON);
        return new CheckoutOverviewPage(driver);
    }

    public CheckoutInfoPage submitInvalidCustomerInfo(String firstName, String lastName, String postalCode) {
        fillForm(firstName, lastName, postalCode);
        click(CONTINUE_BUTTON);
        return this;
    }

    private void fillForm(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME_INPUT, firstName);
        type(LAST_NAME_INPUT, lastName);
        type(POSTAL_CODE_INPUT, postalCode);
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }
}
