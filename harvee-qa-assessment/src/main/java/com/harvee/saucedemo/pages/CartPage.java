package com.harvee.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class CartPage extends BasePage {
    private static final By CART_ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT_BUTTON = By.cssSelector("[data-test='checkout']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public List<String> getCartItemNames() {
        return driver.findElements(CART_ITEM_NAMES)
                .stream()
                .map(e -> e.getText().trim())
                .toList();
    }

    public CheckoutInfoPage clickCheckout() {
        click(CHECKOUT_BUTTON);
        return new CheckoutInfoPage(driver);
    }
}
