# Sauce Demo QA Automation Suite – Harvee Technologies Assessment

**Candidate:** Arulmani Kumarasamy  
**Tech Stack:** Java 21, Selenium WebDriver 4.51.0, TestNG 7.12.0, Maven Surefire 3.5.6, ExtentReports 5.1.2

---

## 1. Prerequisites & Setup
- **Java:** JDK 21+
- **Build Tool:** Apache Maven 3.9+
- **Browser:** Google Chrome, Mozilla Firefox, or Microsoft Edge (Selenium Manager automatically resolves the matching driver).

## 2. Configuration & Execution Commands
All base URLs, browser flags, and test user credentials reside in `src/test/resources/config.properties`:

```bash
# Run full suite in headless Chrome (default)
mvn clean test

# Run in headed mode with custom parameters
mvn clean test -Dheadless=false -Dbrowser=chrome
```

## 3. Reports & Failure Screenshots
- **HTML Report:** `target/reports/ExtentReport.html` (embeds Base64 failure screenshots automatically)
- **JUnit XML Report:** `target/surefire-reports/testng-results.xml`
- **Raw Failure PNGs:** `target/screenshots/<TestMethodName>_<Timestamp>.png`

---

## 4. Section A1: Manual Test Suite

### A1.1 Questions for the Product Owner & Test Assumptions
* **Q1 (Input Validation):** Are there specific format, character set, or length rules for First Name, Last Name, and Postal Code? *(Assumption: Any non-empty string is accepted; only blank fields trigger validation errors).*
* **Q2 (Tax Calculation):** Is tax calculated as a flat percentage across all regions? *(Assumption: Flat 8% of item subtotal, rounded to nearest cent).*
* **Q3 (SKU Quantity):** Can a customer order multiple units of the same product? *(Assumption: Limited to 1 unit per order; clicking Add to cart toggles button state to Remove).*
* **Q4 (Empty Cart Checkout):** Should users be prevented from checking out with 0 items? *(Assumption: Checkout button should be disabled, though Sauce Demo leaves it clickable—logged as a UX observation).*
* **Q5 (Sort Tie-Breaking):** What is the secondary sort order for products with identical prices (e.g. $15.99)? *(Assumption: Secondary alphabetical sort A to Z).*
* **Q6 (Session Cart Persistence):** Do cart items persist across refreshes and logouts? *(Assumption: Persists across refresh within session; cleared upon completing order).*

### A1.2 Manual Test Cases (TC01 – TC24)

| ID | Area | Type | Title | Pre-conditions | Steps | Test data | Expected result | Priority | Automate? (Yes / No) and why |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC01** | Login | Positive | Standard user login | On login page | Enter valid credentials & click Login | standard_user / secret_sauce | Redirected to /inventory.html | P1 | Yes – Core smoke test |
| **TC02** | Login | Negative | Locked-out user blocked | On login page | Enter locked-out credentials & click Login | locked_out_user / secret_sauce | Error banner shown | P1 | Yes – Access control check |
| **TC03** | Login | Negative | Invalid password | On login page | Enter valid user & wrong password | standard_user / wrong | Error banner shown | P2 | No – Covered by API/unit test |
| **TC04** | Login | Boundary | Empty credentials | On login page | Leave blank & click Login | Empty strings | Error: Username is required | P2 | No – Form validation check |
| **TC05** | Catalog | Positive | Sort A to Z | On inventory page | Select Name (A to Z) from dropdown | Sort: A to Z | Catalog sorted ascending A-Z | P2 | Yes – Fast deterministic check |
| **TC06** | Catalog | Positive | Sort Z to A | On inventory page | Select Name (Z to A) from dropdown | Sort: Z to A | Catalog sorted descending Z-A | P2 | Yes – Reverse alphabetical check |
| **TC07** | Catalog | Positive | Sort Price low to high | On inventory page | Select Price low to high from dropdown | Sort: Low to High | Prices ordered ($7.99 to $49.99) | P1 | Yes – Full-list numerical sort check |
| **TC08** | Catalog | Positive | Sort Price high to low | On inventory page | Select Price high to low from dropdown | Sort: High to Low | Prices ordered ($49.99 to $7.99) | P2 | Yes – Reverse price sort check |
| **TC09** | Cart | Positive | Add 2 items, remove 1 | On inventory page | Add 2 items, remove 1, check badge | Backpack & Bike Light | Badge updates 2 -> 1, cart synced | P1 | Yes – State sync validation |
| **TC10** | Cart | Positive | Remove from Cart page | On /cart.html | Click Remove next to item | Backpack | Item removed from DOM | P2 | No – Covered in TC09 |
| **TC11** | Cart | Boundary | Add all 6 items | On inventory page | Add all 6 items, check badge | All 6 catalog items | Badge shows 6 | P3 | No – Upper-bound check |
| **TC12** | Cart | Boundary | Empty cart checkout | On /cart.html | Open empty cart, check Checkout button | 0 items | Warns user (Sauce Demo UX gap) | P3 | No – Exploratory observation |
| **TC13** | Checkout | Positive | Full E2E checkout & math | On inventory page | Add 2 items, checkout, verify total, finish | Arulmani / Kumarasamy / 560068 | Subtotal + tax == total; order placed | P1 | Yes – Revenue-critical E2E flow |
| **TC14** | Checkout | Negative | Missing First Name | On step-one | Leave First Name blank, continue | Missing first name | Error: First Name is required | P1 | Yes – Parameterized form check |
| **TC15** | Checkout | Negative | Missing Last Name | On step-one | Leave Last Name blank, continue | Missing last name | Error: Last Name is required | P1 | Yes – Parameterized form check |
| **TC16** | Checkout | Negative | Missing Postal Code | On step-one | Leave Postal Code blank, continue | Missing postal code | Error: Postal Code is required | P1 | Yes – Parameterized form check |
| **TC17** | Checkout | Boundary | Long input strings (255 chars) | On step-one | Enter 255-char strings in fields | 255-char strings | Accepts input without crash | P3 | No – Unit/component level check |
| **TC18** | Checkout | Positive | Cancel from Overview | On overview page | Click Cancel | Overview page | Returns to catalog, preserves cart | P2 | Yes – Business rule check |
| **TC19** | Checkout | Positive | Cancel from Step One | On step-one | Click Cancel | Step One page | Returns to cart | P3 | No – Simple navigation link |
| **TC20** | Security | Security | Direct URL access after logout | Logged out | Navigate to /inventory.html directly | URL: /inventory.html | Blocked, redirected to login | P1 | Yes – Authentication boundary check |
| **TC21** | Security | Security | Direct URL access unauthenticated | Fresh session | Navigate to /checkout-step-two.html | URL: /checkout-step-two.html | Blocked, redirected to login | P2 | No – Covered by router middleware |
| **TC22** | Usability | Usability | Cart persistence on refresh | On inventory page | Add items, refresh browser (F5) | 2 items | Cart badge & button state persist | P2 | No – Implicitly verified |
| **TC23** | Responsive | Responsive | Mobile viewport (375x667) | Viewport set | Test layout at 375x667 | Viewport 375x667 | No overflow, buttons tappable | P2 | No – Manual visual check |
| **TC24** | Accessibility | Accessibility | Keyboard navigation & alt text | Login page | Use Tab/Enter navigation, check alt tags | Keyboard only | Visible focus rings & alt text | P2 | No – Assistive tech check |

### A1.3 Exploratory Testing Charter (`problem_user`) & Real Findings
* **Charter:** Explore catalog browsing, sorting, cart management, and checkout while logged in as `problem_user`.
* **Actual Findings Discovered During Testing:**
  1. All 6 product cards display the exact same dog image (`sl-404.168b1cce.jpg`) instead of product photos.
  2. Clicking **Add to cart** on *Bolt T-Shirt*, *Fleece Jacket*, and *Onesie* fails silently (buttons do not respond).
  3. Clicking **Remove** on items from the inventory page fails; items can only be removed from `/cart.html`.
  4. On `/checkout-step-one.html`, typing into **Last Name** overwrites the last character of **First Name**, triggering a false validation error.

### A1.4 Test Strategy (136 Words)
**UI Layer Automation:** We automate the 11 P1/P2 end-to-end journeys (`TC01–TC02`, `TC05–TC09`, `TC13–TC16`, `TC18`, `TC20`) covering login access control, catalog sorting, cart state transitions, financial total math, checkout form validation, and post-logout route protection.

**API & Unit Layer:** Input boundary rules (`TC03–TC04`, `TC17`), tax calculation rounding formulas, tie-breaking sort algorithms, and route-guard middleware (`TC21`) belong at the unit and API layers. Testing permutations there runs in milliseconds without browser overhead.

**Manual Testing:** Visual responsiveness (`TC23`), accessibility (`TC24`), UX edge cases (`TC12`), and exploratory charters on anomalous accounts (`problem_user`) remain manual, where human judgment spots layout and usability issues best.

**Biggest Remaining Risk:** Sauce Demo processes state purely client-side without backend inventory reservation or payment gateway integration—leaving concurrency and payment failures untested until staging integration.

---

## 5. Traceability Matrix & Scaling
- Mapped 1:1 in `README.md` and automated via `SauceDemoSuiteTest.java`.
- **Scaling to 200+ Tests:** (1) State Injection & API Seeding via cookies/localStorage; (2) TestNG group partitioning (`smoke`, `regression`, `checkout`) running across containerized Docker Grid nodes; (3) Domain Component Decomposition (reusable UI components and strongly typed Java 21 `record` fixtures).
