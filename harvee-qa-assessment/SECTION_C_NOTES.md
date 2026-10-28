# Section C – Issue Analysis & Production Incidents (Live Discussion Prep Notes)

---

## C1. How You Analyze an Issue ("Checkout has been failing for some users since this morning")

Use this structured 5-stage diagnostic walkthrough during the 20-minute live conversation:

### Step 1: Immediate Triage Questions (First 2 Minutes)
Before jumping into code, narrow the blast radius by asking targeted questions:
1. **Scope & Impact:** What percentage of checkout attempts are failing right now, and is total order volume dropping to zero or only failing for a specific segment?
2. **Symptom Details:** At which exact step does checkout fail (clicking *Checkout*, submitting address details, loading tax/shipping on the Overview screen, or clicking *Finish/Pay*)? What error message do users see?
3. **User/Environment Pattern:** Are affected users tied to a specific payment method, region/postal code, device/browser (e.g., Mobile Safari vs. Chrome), or account type (logged-in vs. guest, promo code applied)?
4. **Timeline:** What exact timestamp did the failures start ("this morning" = 6:00 AM UTC or 9:00 AM IST?), and did any deployment, feature-flag toggle, or third-party maintenance happen around that window?

### Step 2: Where to Look (Observability, Logs, Releases & Tests)
1. **Monitoring & APM Dashboards (Datadog / New Relic / CloudWatch):**
   - Check `/checkout` API endpoint error rates (`4xx` vs. `5xx`), latency spikes (`p95`/`p99`), and third-party payment/tax gateway response times starting from the morning timestamp.
2. **Application & Frontend Error Logs (Sentry / Kibana / Splunk):**
   - Group backend exceptions by stack trace and inspect browser console error telemetry (e.g., `TypeError: Cannot read properties of null` on a specific client browser).
3. **Recent Releases & Config Changes:**
   - Check CI/CD deployment history, feature-flag changes (LaunchDarkly/AppConfig), database migrations, or CDN cache purges from the last 24 hours.
4. **Synthetic Monitoring & Scheduled CI Runs:**
   - Check if our automated smoke/checkout monitors passed or failed this morning—and why (e.g., if our test uses a domestic zip code `560068` and passes, while international postal codes fail).

### Step 3: Reproducing & Narrowing Down Safely
1. **Extract a Failing Request Payload (Sanitized):** Pull the correlation ID / trace ID of a failed production checkout from logs to see the exact cart items, shipping country/postal code, user tier, and browser User-Agent.
2. **Reproduce in Staging/Pre-Prod First:** Replay the exact item combination and postal code in Staging. If Staging passes, compare environment variables, API keys, and data state between Staging and Production.
3. **Safe Production Verification (If Needed):** Use a whitelisted test account or dry-run up to the Checkout Overview step (before payment capture) with DevTools Network/Console recording enabled.

### Step 4: Distinguishing Product Defect vs. Data vs. Environment Problem
- **Product (Code) Defect:** Correlates with a recent code deploy or feature flag; deterministic reproduction whenever a specific code branch is triggered (e.g., null-pointer when middle name or apartment line is empty).
- **Data Problem:** Happens only for specific SKUs, user profiles, or database records (e.g., a newly added catalog product has `price = null` or missing tax category in the DB, breaking the checkout total calculation whenever that item is in the cart).
- **Environment / Infrastructure Problem:** Intermittent `502/503/504` timeouts, expired SSL certificates, exhausted DB connection pools, rate-limiting from an external tax/payment API, or Redis session cache eviction.

### Step 5: Client Communication Cadence During Investigation
- **Within 15 Minutes (Acknowledge & Frame):** Confirm receipt, state the current blast radius (e.g., *"Affecting ~12% of checkouts involving international postal codes; domestic orders are processing normally"*), and commit to the next update time.
- **Every 30 Minutes (Timeboxed Status Updates):** Share what has been ruled out, current hypothesis, and mitigation ETA—even if root cause isn't finalized yet.
- **Upon Mitigation & Resolution:** Confirm when the rollback/hotfix is live, verify recovery via live metrics, and follow up with a short RCA summary within 24 hours.

---

## C2. A Recent Production Issue You Identified (STAR + Preventative Controls)

Below is a structured, concrete production incident narrative tailored to your Web + API + Cloud QA Automation background:

### 1. What Happened & How I Found It (Detection & Business Impact)
- **How it was found:** During a post-release verification window for an e-commerce/enterprise portal microservice update, our **Datadog Synthetic API + UI checkout monitor** began alerting with intermittent `500 Internal Server Error` failures on the order summary calculation endpoint, while standard CI regression tests were green.
- **Business Impact:** Roughly **8% of live customer checkouts** were failing at the Order Review screen whenever a user had an active promotional discount combined with a multi-item cart where two items shared the same tax tier. Customers saw a generic *"Unable to calculate order total"* banner and could not complete payment.

### 2. Root Cause: How I Found It & What It Turned Out to Be
- **Investigation:** I pulled the correlation IDs from the failing Datadog synthetic runs and inspected the backend service logs alongside the JSON payloads in Chrome DevTools. I noticed all failing requests contained **2+ items with a percentage discount** that produced a repeating decimal (e.g., `15%` off `$19.99` = `$16.9915`).
- **Root Cause:** A recent backend refactoring changed how item-level discounts were aggregated before tax calculation. The pricing service rounded the grand total to 2 decimal places (`BigDecimal` scale 2), but the downstream payment-intent validation service summed unrounded line items first and threw a `ValidationException` whenever there was a **$0.01 rounding discrepancy** (`$0.01` penny mismatch) between the UI subtotal and the payment payload. Our standard regression suite missed it because our test fixtures used round-number prices (`$10.00`, `$20.00`) that never triggered fractional-cent rounding.

### 3. Explaining It to the Client
- **Who & How Soon:** Within **20 minutes** of the first synthetic alert—after confirming the exact reproduction pattern—I notified the Engineering Lead and Product Manager in the release Slack channel.
- **Non-Technical Explanation:** Instead of talking about floating-point precision or `BigDecimal` rounding modes, I explained:
  > *"When a customer buys multiple items with a percentage coupon, the cart page and the payment service round half-cents slightly differently, causing a 1-cent mismatch. To protect customers from being overcharged, the payment service blocks those orders (~8% of current checkouts). Orders without percentage coupons are unaffected. We have a one-line rounding fix ready to deploy in 15 minutes."*

### 4. Stopping It Happening Again (Preventative Controls & Results)
1. **Parameterized Boundary & Penny-Rounding Test Suite:** Added a `@DataProvider` test suite in REST Assured and Selenium using non-round prices (`$19.99`, `$7.99`, `$14.49`) combined with odd percentage discounts (`7%`, `13%`, `15%`) to explicitly test half-cent rounding boundaries (`Item Subtotal - Discount + Tax == Grand Total`).
2. **Contract / Invariant Assertion in CI:** Added an API assertion verifying that the UI summary total matches the payment gateway payload amount to the exact cent on every build.
3. **Did it catch anything since?** **Yes**—two sprints later, when a new regional GST/tax rule was introduced on staging, our parameterized penny-rounding test caught an identical 1-cent rounding discrepancy during PR validation before it ever reached production.

### 5. Looking Back: What I Would Do Differently Next Time
- **Diverse, Realistic Test Data by Default:** Earlier in the project, we relied too heavily on "clean" synthetic test data (`$10.00`, `$20.00`) in staging. Going forward, I always seed staging catalogs with production-like pricing distributions (`.99`, `.49`, `.95` endings) and multi-item tax/discount combinations from day one, and pair UI E2E checks with fast API boundary fuzzing on financial calculations.
