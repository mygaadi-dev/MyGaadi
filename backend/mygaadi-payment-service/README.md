# MyGaadi Payment and Escrow Service

Independent Spring Boot payment service for MyGaadi.com. It owns only payment, escrow and payment-event data. It does not create or store users, admins, cars, bookings, login credentials, OTPs or refresh tokens.

## Purpose

The service provides Razorpay order creation, payment verification, signed webhook processing, duplicate-payment protection, refund initiation, internal escrow-ledger state management and payment audit events.

The escrow implemented here is an **internal application ledger**. `InternalLedgerPayoutService` does not transfer money to a seller bank account. Replace it with a compliant marketplace payout provider before production use.

## Project structure

```text
mygaadi-payment-service
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/mygaadi/payment
    │   │   ├── common
    │   │   ├── config
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── entity
    │   │   ├── enums
    │   │   ├── repository
    │   │   ├── security
    │   │   └── service
    │   └── resources/application.properties
    └── test/java/com/mygaadi/payment
```

## Required software

- Java 17
- Maven 3.9+
- MySQL 8
- Razorpay test account
- Running MyGaadi main application on port `8080`

## Database setup

Run:

```sql
SOURCE C:/path/MyGaadi.com-Clean-Updated/database/payment-service.sql;
```

The script creates the independent database `mygaadi_payment` and only these tables:

- `payments`
- `escrows`
- `payment_events`

There are no foreign keys to the main MyGaadi database. `booking_id`, `buyer_id` and `seller_id` are external reference IDs.

## Environment variables

The JWT secret must be exactly the same as the main MyGaadi application.

### Windows PowerShell

```powershell
$env:PAYMENT_DB_URL="jdbc:mysql://localhost:3306/mygaadi_payment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"
$env:PAYMENT_DB_USERNAME="root"
$env:PAYMENT_DB_PASSWORD="2003"
$env:JWT_SECRET="COPY_THE_EXACT_APP_JWT_SECRET_FROM_MYGAADI"
$env:RAZORPAY_KEY_ID="rzp_test_xxxxxxxxxx"
$env:RAZORPAY_KEY_SECRET="your_test_key_secret"
$env:RAZORPAY_WEBHOOK_SECRET="your_webhook_secret"
$env:RAZORPAY_CURRENCY="INR"
$env:MYGAADI_BASE_URL="http://localhost:8080"
$env:CORS_ORIGINS="http://localhost:5173"
```

### Linux/macOS

```bash
export PAYMENT_DB_URL='jdbc:mysql://localhost:3306/mygaadi_payment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata'
export PAYMENT_DB_USERNAME='root'
export PAYMENT_DB_PASSWORD='2003'
export JWT_SECRET='COPY_THE_EXACT_APP_JWT_SECRET_FROM_MYGAADI'
export RAZORPAY_KEY_ID='rzp_test_xxxxxxxxxx'
export RAZORPAY_KEY_SECRET='your_test_key_secret'
export RAZORPAY_WEBHOOK_SECRET='your_webhook_secret'
export RAZORPAY_CURRENCY='INR'
export MYGAADI_BASE_URL='http://localhost:8080'
export CORS_ORIGINS='http://localhost:5173'
```

Do not commit real secrets.

## Run

```bash
mvn clean test
mvn spring-boot:run
```

The service starts on:

```text
http://localhost:8082
```

Health check:

```text
GET http://localhost:8082/actuator/health
```

## Razorpay test-mode setup

1. Open the Razorpay dashboard and enable Test Mode.
2. Set the test key ID and key secret as environment variables.
3. Create a webhook pointing to:

```text
POST http://your-public-url/api/payments/webhook/razorpay
```

4. Configure the same webhook secret in `RAZORPAY_WEBHOOK_SECRET`.
5. Enable these events:
   - `payment.authorized`
   - `payment.captured`
   - `payment.failed`
   - `order.paid`
   - `refund.processed`
   - `refund.failed`

For local webhook testing, expose port `8082` through a secure tunnel. Do not expose development secrets.

## API list

| Method | URL | Access |
|---|---|---|
| POST | `/api/payments/booking/{bookingId}/create-order` | BUYER owner |
| POST | `/api/payments/booking/{bookingId}/verify` | BUYER owner |
| GET | `/api/payments/booking/{bookingId}` | Matching BUYER, matching SELLER, ADMIN |
| POST | `/api/payments/webhook/razorpay` | Public, valid webhook signature required |
| POST | `/api/escrows/booking/{bookingId}/buyer-confirm` | Matching BUYER |
| POST | `/api/escrows/booking/{bookingId}/seller-confirm` | Matching SELLER |
| GET | `/api/escrows/booking/{bookingId}` | Matching BUYER, matching SELLER, ADMIN |

Only the webhook, CORS preflight and Actuator health endpoint are public.

## JWT compatibility

The service does not have a user table. It validates the Bearer token using the same HMAC secret as the existing MyGaadi application and reads:

```json
{
  "sub": "buyer@mygaadi.com",
  "uid": 10,
  "role": "BUYER"
}
```

Authorities are created as `ROLE_BUYER`, `ROLE_SELLER` or `ROLE_ADMIN`. Role checks are followed by resource-ownership checks against IDs stored with the payment and escrow.

The service contains no login, registration, OTP, refresh-token, user-management or admin-creation API. An ADMIN cannot create another ADMIN through this service.

## Booking integration

The payment service never reads or updates the main booking, car or user tables.

### Temporary adapter for the uploaded MyGaadi project

The current project does not expose a flat internal booking-by-ID endpoint. `DevelopmentBookingClient` therefore forwards the buyer's existing JWT to:

```text
GET {MYGAADI_BASE_URL}/api/bookings/buyer/me
```

It selects the requested booking and uses only:

- booking ID
- buyer ID
- seller ID
- amount
- platform fee
- seller amount
- booking status

This adapter allows create-order testing without modifying the uploaded MyGaadi project.

### Recommended internal contract for later integration

Replace `DevelopmentBookingClient` with a client for:

```http
GET /api/internal/bookings/{bookingId}/payment-snapshot
Authorization: Bearer <trusted service or forwarded user token>
```

Expected response:

```json
{
  "success": true,
  "message": "Booking payment snapshot",
  "data": {
    "bookingId": 7,
    "buyerId": 10,
    "sellerId": 20,
    "amount": 1000000.00,
    "platformFee": 20000.00,
    "sellerAmount": 980000.00,
    "bookingStatus": "ACCEPTED"
  }
}
```

The current MyGaadi project was not changed to add this endpoint.

## Payment flow

1. Buyer creates a booking in the main MyGaadi application.
2. Seller accepts the booking.
3. Buyer calls `create-order` with the MyGaadi JWT.
4. The service validates the trusted booking snapshot and buyer ownership.
5. Razorpay order amount is created from the trusted booking amount, not frontend input.
6. The frontend completes Razorpay Checkout.
7. Buyer calls `verify` with Razorpay order ID, payment ID and signature.
8. The service verifies HMAC, fetches payment details from Razorpay and validates order, amount and currency.
9. Captured payment becomes `SUCCESS` and creates an escrow ledger entry in `HELD` state.
10. Signed webhooks reconcile the authoritative gateway state.

## Idempotency and duplicate protection

- `Idempotency-Key` is optional on create-order.
- One payment row is allowed per booking.
- A repeated order request returns the existing Razorpay order.
- A successful booking payment cannot be paid again.
- Razorpay order IDs, payment IDs and internal transaction IDs are unique.
- Webhook event IDs are unique and inserted with `INSERT IGNORE` before processing.
- Escrow uses both a pessimistic repository lock and JPA `@Version`.

## Escrow flow

```text
Payment captured
    -> HELD
    -> buyer confirms
    -> seller confirms
    -> RELEASED
```

## Refund reconciliation

The service does not expose participant dispute or administrator refund APIs. Signed Razorpay refund webhooks can still reconcile refunds initiated through an authorized external operational process.

## Webhook flow

1. Read the raw request body.
2. Verify `X-Razorpay-Signature` using `RAZORPAY_WEBHOOK_SECRET`.
3. Use `X-Razorpay-Event-Id`, or derive a stable hash when the header is absent.
4. Reserve the unique event ID before applying changes.
5. Validate payment/order ID, amount and currency.
6. Apply only a valid state transition.
7. Store a sanitized event summary, not complete sensitive payloads.

## Postman testing order

1. Start MySQL and run `../database/payment-service.sql`.
2. Start the existing MyGaadi backend on `8080`.
3. Obtain BUYER, SELLER and ADMIN JWTs from the existing MyGaadi authentication APIs.
4. Import `../postman/MyGaadi-Payment-Service.postman_collection.json`.
5. Set collection variables `buyerToken`, `sellerToken`, `adminToken` and `bookingId`.
6. Create a booking and have the seller accept it in the existing application.
7. Run **Create Razorpay Order**.
8. Complete Razorpay Checkout and set `paymentId` and `paymentSignature`.
9. Run **Verify Payment**.
10. Run payment and escrow status requests.

## Security notes

- Never send card numbers, CVV, UPI PIN or banking credentials to this service.
- Never trust an amount sent by the frontend.
- Never log JWTs, Razorpay secrets or complete webhook payloads.
- Use HTTPS in production.
- Rotate leaked credentials immediately.
- Replace the internal payout implementation with a compliant provider before real seller settlement.
- Restrict database and Actuator network access in production.

## Tests included

- Valid and invalid JWT parsing/filtering
- Buyer and seller ownership checks
- ADMIN authorization checks
- Payment signature validation
- Duplicate successful payment prevention
- Idempotent order reuse
- Escrow hold
- Release only after both confirmations
- Duplicate webhook handling
- `@Version` and repository lock concurrency protection
