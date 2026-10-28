package com.harvee.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class InventoryPage extends BasePage {
    private static final By PAGE_TITLE = By.cssSelector("[data-test='title']");
    private static final By SORT_DROPDOWN = By.cssSelector("[data-test='product-sort-container']");
    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ITEM_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By INVENTORY_ITEMS = By.cssSelector("[data-test='inventory-item']");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return waitForVisible(PAGE_TITLE).getText().equalsIgnoreCase("Products")
                && driver.getCurrentUrl().contains("/inventory.html");
    }

    public InventoryPage sortByVisibleText(String sortOptionText) {
        Select select = new Select(waitForVisible(SORT_DROPDOWN));
        select.selectByVisibleText(sortOptionText);
        return this;
    }

    public List<String> getProductNames() {
        waitForVisible(ITEM_NAMES);
        return driver.findElements(ITEM_NAMES)
                .stream()
                .map(e -> e.getText().trim())
                .toList();
    }

    public List<Double> getProductPrices() {
        waitForVisible(ITEM_PRICES);
        return driver.findElements(ITEM_PRICES)
                .stream()
                .map(e -> Double.parseDouble(e.getText().replace("$", "").trim()))
                .toList();
    }

    public InventoryPage addProductByName(String productName) {
        WebElement card = findProductCard(productName);
        card.findElement(By.cssSelector("button[id^='add-to-cart']")).click();
        return this;
    }

    public InventoryPage removeProductByName(String productName) {
        WebElement card = findProductCard(productName);
        card.findElement(By.cssSelector("button[id^='remove']")).click();
        return this;
    }

    private WebElement findProductCard(String productName) {
        waitForVisible(INVENTORY_ITEMS);
        return driver.findElements(INVENTORY_ITEMS)
                .stream()
                .filter(item -> item.findElement(ITEM_NAMES).getText().trim().equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found in catalog: " + productName));
    }
}
