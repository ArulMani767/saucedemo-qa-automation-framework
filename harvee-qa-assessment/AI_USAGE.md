# AI Usage Log (`AI_USAGE.md`)

As an experienced SDET, I used AI coding assistants as a productivity tool to scaffold boilerplate structures, then applied thorough human code audits and manual testing to refine the output.

---

## 1. Section A1 (Manual Test Suite & Charter)
* **What I asked for:** Drafted initial table structure for 24 test cases and exploratory charter.
* **My Human Edits & Verifications:**
  - Audited all manual cases to ensure strict 1:1 traceability with Section A2 test methods.
  - Performed live exploratory testing on `saucedemo.com` to document real `problem_user` bugs (dog images, broken add-to-cart buttons, and Last Name overwriting First Name).
  - Ensured Test Strategy word count is strictly under 150 words (136 words).

---

## 2. Section A2 (Java 21 + Selenium + TestNG)
* **What I asked for:** Scaffolded Page Object boilerplate, ThreadLocal WebDriver manager, ExtentReports listener, and test class.
* **My Human Edits & Verifications:**
  - **Split TC05 & TC06:** The initial AI output combined A-Z and Z-A sorting into one method. I split them into `TC05_sortByNameAscendingChecksWholeList` and `TC06_sortByNameDescendingChecksWholeList` to satisfy strict 1:1 method prefix naming.
  - **Fixed Floating-Point Math (`TC13`):** Replaced raw `double` addition with `BigDecimal` (`RoundingMode.HALF_UP`, scale 2) to eliminate IEEE 754 float precision errors.
  - **Browser Popup Suppression:** Added `profile.password_manager_leak_detection = false` to `ChromeOptions` in `DriverManager.java` to prevent Chrome's password-leak modal from blocking automated clicks.
  - **Full List Price Verification:** Verified that `getProductPrices()` extracts all 6 prices and compares the entire list against a sorted copy.

---

## 3. Section B (Code Review & Client Comms)
* **What I asked for:** Structured checklist for B1 code review and drafts for B3/B4.
* **My Human Edits & Verifications:**
  - Audited Playwright strict-mode behavior on `page.click('text=Add to cart')` (which matches 6 buttons on Sauce Demo).
  - Trimmed the B4 client message to 138 words, removing technical jargon to ensure clear business impact communication.
