package com.harvee.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutOverviewPage extends BasePage {
    private static final By SUBTOTAL_LABEL = By.cssSelector("[data-test='subtotal-label']");
    private static final By TAX_LABEL = By.cssSelector("[data-test='tax-label']");
    private static final By TOTAL_LABEL = By.cssSelector("[data-test='total-label']");
    private static final By FINISH_BUTTON = By.cssSelector("[data-test='finish']");
    private static final By CANCEL_BUTTON = By.cssSelector("[data-test='cancel']");

    public CheckoutOverviewPage(WebDriver driver) {
        super(driver);
    }

    public double getItemSubtotal() {
        return parseCurrency(getText(SUBTOTAL_LABEL));
    }

    public double getTax() {
        return parseCurrency(getText(TAX_LABEL));
    }

    public double getTotal() {
        return parseCurrency(getText(TOTAL_LABEL));
    }

    public CheckoutCompletePage clickFinish() {
        click(FINISH_BUTTON);
        return new CheckoutCompletePage(driver);
    }

    public InventoryPage clickCancel() {
        click(CANCEL_BUTTON);
        return new InventoryPage(driver);
    }

    private double parseCurrency(String rawText) {
        String numeric = rawText.replaceAll("[^0-9.]", "");
        return Double.parseDouble(numeric);
    }
}
