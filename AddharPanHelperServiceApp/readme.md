# KYC Verification Server (PAN & Aadhaar)

A robust, scalable ASP.NET Core Web API designed to handle Know Your Customer (KYC) verification processes. This service securely validates PAN and Aadhaar records against a central database and implements a Two-Factor Authentication (2FA) workflow utilizing email-based One-Time Passwords (OTPs) via Gmail SMTP.

Built with clean architecture principles, this project demonstrates secure data handling, relational database design, and collaborative Git workflows.

## Key Features

*   **PAN Verification Flow:** Validates standard 10-character PAN formats and cross-references them with a secure registry.
*   **Email 2FA Integration:** Generates secure, randomized 6-digit OTPs and delivers them to the user's registered email via SMTP.
*   **Time-Sensitive Security:** Implements a strict 5-minute expiration window for all OTPs.
*   **Decoupled Architecture:** Utilizes dedicated Services, Data Transfer Objects (DTOs), and Entity Framework Core for clean separation of concerns.
*   **API Documentation:** Built-in Swagger/OpenAPI support for seamless endpoint testing and client integration.

##  Tech Stack

*   **Framework:** .NET Core Web API (C#)
*   **ORM:** Entity Framework (EF) Core
*   **Database:** SQL Server
*   **External Services:** `System.Net.Mail` (Gmail SMTP Integration)

---

##  API Documentation

### 1. Request OTP for PAN Verification
Initiates the KYC process by verifying the PAN exists and sending an OTP to the associated email.

*   **Endpoint:** `POST /api/pan/verify`
*   **Headers:** `Content-Type: application/json`

**Request Body:**
```json
{
  "panNumber": "ABCDE1234F"
}
```

** Success Response (200 OK): **

```JSON
{
    "success": true,
    "code": "OTP_SENT",
    "message": "OTP sent to registered email address",
    "data": {
        "maskedEmail": "s******@gmail.com",
        "expiresIn": 300
    }
}
```
### 2. Verify OTP & Complete KYC
Validates the user's OTP against the database, checks the expiration time, and returns the verified PAN details.

*    **Endpoint:** `POST /api/pan/verify-otp`

*   **Headers:** `Content-Type: application/json`

**Request Body:**

```JSON
{
  "panNumber": "ABCDE1234F",
  "otp": "628184"
}```

Success Response (200 OK):

```JSON
{
    "success": true,
    "code": "PAN_VERIFIED",
    "message": "PAN verification completed",
    "data": {
        "panNumber": "ABCDE1234F",
        "holderName": "Saitej Shinde",
        "status": "ACTIVE",
        "kycStatus": "COMPLETED"
    }
}
```

``Maintained by: Saitej Shinde``