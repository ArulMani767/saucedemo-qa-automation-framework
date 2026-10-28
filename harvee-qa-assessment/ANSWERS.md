# Section B – Checking AI Output & Client Communication (`ANSWERS.md`)

---

## B1. Review AI-Generated Code

### 1. Every Problem Found in the AI-Generated Test
1. **False-Positive Assertion (`expect(total).toBeTruthy()`):** `textContent()` returns `"Total: $32.39"`. Checking `.toBeTruthy()` only verifies that the string isn't null or empty. Even if the app rendered `$0.00` or `$NaN`, this test would still pass. It never verifies that `Item Total + Tax = Total`.
2. **Hardcoded Fixed Sleep (`await page.waitForTimeout(3000)`):** Slows execution and causes flakiness. Playwright has built-in auto-waiting (`await expect(locator).toBeVisible()`).
3. **Ambiguous Locator (`await page.click('text=Add to cart')`):** Matches 6 buttons on the products page, throwing a Playwright strict-mode error.
4. **Hardcoded URL & Credentials:** Credentials and URLs are hardcoded instead of using environment variables (`process.env`).
5. **No Page Object Model:** Raw CSS selectors scattered in test body.
6. **Brittle CSS Classes:** Uses `.summary_total_label` instead of `data-test` attributes.
7. **Throwaway Test Data (`'A'`, `'B'`, `'1'`):** Reduces test clarity.
8. **Missing Intermediate Verification:** Never checks cart badge updates before checkout.

### 2. Rewritten, Production-Grade Test (Playwright + TypeScript with POM)
```typescript
import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { InventoryPage } from '../pages/InventoryPage';
import { CartPage } from '../pages/CartPage';
import { CheckoutPage } from '../pages/CheckoutPage';

const STANDARD_USER = process.env.SAUCE_USERNAME ?? 'standard_user';
const PASSWORD = process.env.SAUCE_PASSWORD ?? 'secret_sauce';

test('TC13: cart and checkout total equals item subtotal plus tax', async ({ page }) => {
  const loginPage = new LoginPage(page);
  const inventoryPage = new InventoryPage(page);
  const cartPage = new CartPage(page);
  const checkoutPage = new CheckoutPage(page);

  await loginPage.open();
  await loginPage.login(STANDARD_USER, PASSWORD);
  await expect(inventoryPage.pageTitle).toHaveText('Products');

  const item1 = 'Sauce Labs Backpack';   // $29.99
  const item2 = 'Sauce Labs Bike Light'; // $9.99
  const price1 = await inventoryPage.getProductPrice(item1);
  const price2 = await inventoryPage.getProductPrice(item2);

  await inventoryPage.addProductToCart(item1);
  await inventoryPage.addProductToCart(item2);
  await expect(inventoryPage.cartBadge).toHaveText('2');

  await inventoryPage.openCart();
  await expect(cartPage.cartItems).toHaveCount(2);
  await cartPage.proceedToCheckout();

  await checkoutPage.fillCustomerDetails('Arulmani', 'Kumarasamy', '560068');

  const expectedItemSubtotal = Number((price1 + price2).toFixed(2));
  const actualItemSubtotal = await checkoutPage.getSubtotalAmount();
  const actualTax = await checkoutPage.getTaxAmount();
  const actualGrandTotal = await checkoutPage.getTotalAmount();
  const expectedGrandTotal = Number((actualItemSubtotal + actualTax).toFixed(2));

  expect(actualItemSubtotal).toBe(expectedItemSubtotal);
  expect(actualGrandTotal).toBe(expectedGrandTotal);
});
```

---

## B2. Test an AI Feature – Scenario 3: Test Case Generator
* **Check 1 (JSON Schema Validity):** Automated schema check (`Ajv` / `Jackson`) ensuring valid syntax and required keys (`id`, `title`, `steps`, `expectedResult`). *(Scored: Automated Pass/Fail)*.
* **Check 2 (Acceptance Criteria Coverage):** Compare generated cases against user story acceptance criteria. *(Scored: Rubric-based % coverage, target `>= 0.90`)*.
* **Check 3 (Hallucination / Faithfulness Check):** Check that AI didn't invent out-of-scope features (like multi-factor auth). *(Scored: Rubric / Human review audit, target `< 2%` hallucination rate)*.
* **Check 4 (Duplicate Case Detection):** Identify redundant test cases with identical step sequences. *(Scored: Automated uniqueness ratio, target `>= 0.95`)*.

---

## B3. Defect Report (Jira Format)
- **Key:** `SAUCE-402`
- **Title:** `[Catalog Sort] Sorting by "Price (low to high)" orders $29.99 item before $15.99 item on Products page`
- **Environment:** Chrome 130 on Windows 11 / Release 4.2.0 (`standard_user`)
- **Severity:** Major (S2) / Priority: High (P2)
- **Steps:** 1. Log in -> 2. Select sort dropdown -> 3. Choose "Price (low to high)" -> 4. Inspect item prices.
- **Expected:** `[$7.99, $9.99, $15.99, $15.99, $29.99, $49.99]`
- **Actual:** `$29.99` appears before `$15.99`.
- **Evidence:** Attached failure screenshot, TestNG assertion log, and Chrome DevTools HAR file.

---

## B4. Message to the Client (138 Words)
> **Subject:** Friday Release `4.2.0` Readiness – Key Findings & Options
>
> Hi [PM Name],
>
> Ahead of tomorrow’s release, I want to share two findings from today’s regression run:
>
> 1. **Checkout instability:** Our checkout test fails on 1 out of every 10 attempts (passing on retry). If this is a real race condition, up to 10% of customers could hit payment errors.
> 2. **Price sorting bug:** Sorting products by *"Price: low to high"* currently places a `$29.99` item ahead of a `$15.99` item.
>
> **Recommendation:** Hold the release briefly tomorrow morning while we spend 2 hours with development to verify if the 1-in-10 checkout failure affects real users.
>
> **Practical Alternative:** If Friday’s deadline is fixed, revert only the sorting change and ship behind a monitored canary rollout while we patch both items.
>
> Best,  
> Arulmani
