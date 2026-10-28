package com.harvee.saucedemo.tests;

import com.harvee.saucedemo.base.BaseTest;
import com.harvee.saucedemo.config.ConfigLoader;
import com.harvee.saucedemo.pages.*;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SauceDemoSuiteTest extends BaseTest {

    @Test(description = "TC01: Standard user logs in successfully and sees the Products page")
    public void TC01_standardUserLogsInSuccessfully() {
        InventoryPage inventoryPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"));

        Assert.assertTrue(inventoryPage.isLoaded(), "Products page should be displayed for standard_user.");
    }

    @Test(description = "TC02: Locked-out user receives an explicit lockout error message")
    public void TC02_lockedOutUserShowsError() {
        LoginPage loginPage = new LoginPage(driver())
                .open()
                .attemptLoginExpectingError(ConfigLoader.get("user.locked"), ConfigLoader.get("user.password"));

        Assert.assertEquals(
                loginPage.getErrorMessage(),
                "Epic sadface: Sorry, this user has been locked out.",
                "Locked-out user must be blocked with the expected error banner."
        );
    }

    @Test(description = "TC05: Sorting products by Name (A to Z) orders the entire catalog list ascending")
    public void TC05_sortByNameAscendingChecksWholeList() {
        InventoryPage inventoryPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .sortByVisibleText("Name (A to Z)");

        List<String> actualNames = inventoryPage.getProductNames();
        List<String> expectedSortedNames = new ArrayList<>(actualNames);
        expectedSortedNames.sort(Comparator.naturalOrder());

        Assert.assertEquals(actualNames.size(), 6, "All 6 catalog products must be present.");
        Assert.assertEquals(actualNames, expectedSortedNames,
                "Entire product name list must be sorted alphabetically A to Z.");
    }

    @Test(description = "TC06: Sorting products by Name (Z to A) orders the entire catalog list descending")
    public void TC06_sortByNameDescendingChecksWholeList() {
        InventoryPage inventoryPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .sortByVisibleText("Name (Z to A)");

        List<String> actualNames = inventoryPage.getProductNames();
        List<String> expectedSortedNames = new ArrayList<>(actualNames);
        expectedSortedNames.sort(Comparator.reverseOrder());

        Assert.assertEquals(actualNames.size(), 6, "All 6 catalog products must be present.");
        Assert.assertEquals(actualNames, expectedSortedNames,
                "Entire product name list must be sorted alphabetically Z to A.");
    }

    @Test(description = "TC07: Sorting by Price (low to high) sorts the entire product list ascending")
    public void TC07_sortByPriceLowToHighChecksWholeList() {
        InventoryPage inventoryPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .sortByVisibleText("Price (low to high)");

        List<Double> actualPrices = inventoryPage.getProductPrices();
        List<Double> expectedSortedPrices = new ArrayList<>(actualPrices);
        expectedSortedPrices.sort(Comparator.naturalOrder());

        Assert.assertEquals(actualPrices.size(), 6, "Catalog must contain 6 priced items.");
        Assert.assertEquals(actualPrices, expectedSortedPrices,
                "Entire product price list must be in ascending order from lowest to highest.");
    }

    @Test(description = "TC08: Sorting by Price (high to low) sorts the entire product list descending")
    public void TC08_sortByPriceHighToLowChecksWholeList() {
        InventoryPage inventoryPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .sortByVisibleText("Price (high to low)");

        List<Double> actualPrices = inventoryPage.getProductPrices();
        List<Double> expectedSortedPrices = new ArrayList<>(actualPrices);
        expectedSortedPrices.sort(Comparator.reverseOrder());

        Assert.assertEquals(actualPrices.size(), 6, "Catalog must contain 6 priced items.");
        Assert.assertEquals(actualPrices, expectedSortedPrices,
                "Entire product price list must be in descending order from highest to lowest.");
    }

    @Test(description = "TC09: Adding two items and removing one updates the cart badge and cart contents")
    public void TC09_addTwoItemsRemoveOneVerifiesBadgeAndCartContents() {
        String itemToKeep = "Sauce Labs Backpack";
        String itemToRemove = "Sauce Labs Bike Light";

        InventoryPage inventoryPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .addProductByName(itemToKeep)
                .addProductByName(itemToRemove);

        Assert.assertEquals(inventoryPage.getCartBadgeCount(), 2, "Cart badge should show 2 after adding two items.");

        inventoryPage.removeProductByName(itemToRemove);
        Assert.assertEquals(inventoryPage.getCartBadgeCount(), 1, "Cart badge should show 1 after removing one item.");

        CartPage cartPage = inventoryPage.openCart();
        List<String> cartItems = cartPage.getCartItemNames();
        Assert.assertEquals(cartItems, List.of(itemToKeep), "Cart should contain only the remaining product.");
    }

    @Test(description = "TC13: Full checkout verifies Item Total + Tax == Total and empties the cart afterwards")
    public void TC13_fullCheckoutVerifiesTotalMathAndEmptiesCart() {
        CheckoutOverviewPage overviewPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .addProductByName("Sauce Labs Backpack")     // $29.99
                .addProductByName("Sauce Labs Onesie")       // $7.99
                .openCart()
                .clickCheckout()
                .submitCustomerInfo("Arulmani", "Kumarasamy", "560068");

        double itemSubtotal = overviewPage.getItemSubtotal();
        double tax = overviewPage.getTax();
        double total = overviewPage.getTotal();

        // Human touch: Use BigDecimal for financial rounding to avoid IEEE 754 float inaccuracies
        double expectedItemSum = BigDecimal.valueOf(29.99)
                .add(BigDecimal.valueOf(7.99))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        double expectedGrandTotal = BigDecimal.valueOf(itemSubtotal)
                .add(BigDecimal.valueOf(tax))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        Assert.assertEquals(itemSubtotal, expectedItemSum, "Overview item subtotal must equal sum of added item prices.");
        Assert.assertEquals(total, expectedGrandTotal, "Grand total must equal item subtotal plus tax.");

        CheckoutCompletePage completePage = overviewPage.clickFinish();
        Assert.assertEquals(completePage.getConfirmationHeader(), "Thank you for your order!");
        Assert.assertEquals(completePage.getCartBadgeCount(), 0, "Cart badge must be empty (0) after placing the order.");
    }

    @DataProvider(name = "missingCheckoutFields")
    public Object[][] missingCheckoutFieldsData() {
        return new Object[][]{
                {"TC14", "", "Kumarasamy", "560068", "Error: First Name is required"},
                {"TC15", "Arulmani", "", "560068", "Error: Last Name is required"},
                {"TC16", "Arulmani", "Kumarasamy", "", "Error: Postal Code is required"}
        };
    }

    @Test(dataProvider = "missingCheckoutFields",
          description = "TC14, TC15, TC16: Parameterized check for missing First Name, Last Name, and Postal Code")
    public void TC14_TC15_TC16_checkoutMissingFieldErrors(
            String tcId, String firstName, String lastName, String postalCode, String expectedError) {

        CheckoutInfoPage infoPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .addProductByName("Sauce Labs Backpack")
                .openCart()
                .clickCheckout()
                .submitInvalidCustomerInfo(firstName, lastName, postalCode);

        Assert.assertEquals(infoPage.getErrorMessage(), expectedError,
                tcId + " failed: Unexpected validation error message.");
    }

    @Test(description = "TC18: Clicking Cancel on Checkout Overview returns to Products without placing order")
    public void TC18_cancelCheckoutOverviewPreservesCartWithoutPlacingOrder() {
        InventoryPage inventoryPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .addProductByName("Sauce Labs Backpack")
                .openCart()
                .clickCheckout()
                .submitCustomerInfo("Arulmani", "Kumarasamy", "560068")
                .clickCancel();

        Assert.assertTrue(inventoryPage.isLoaded(), "Cancel should navigate back to the Products page.");
        Assert.assertEquals(inventoryPage.getCartBadgeCount(), 1, "Item must remain in the cart after cancelling checkout.");
    }

    @Test(description = "TC20: Logged-out user cannot access /inventory.html directly by typing its URL")
    public void TC20_cannotOpenProductsPageByUrlAfterLoggingOut() {
        LoginPage loginPage = new LoginPage(driver())
                .open()
                .loginAsValidUser(ConfigLoader.get("user.standard"), ConfigLoader.get("user.password"))
                .logout();

        loginPage.openPath("/inventory.html");

        Assert.assertEquals(
                loginPage.getErrorMessage(),
                "Epic sadface: You can only access '/inventory.html' when you are logged in.",
                "Unauthenticated navigation to /inventory.html must be blocked."
        );
    }
}
