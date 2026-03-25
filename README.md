# Rental Profitability Calculator

A Spring Boot REST API designed to estimate the profitability of rental 
properties, both short-term(airbnb and booking.com) and long-term(via OpenAI)

The application combines real market data via BrightData scraping with 
AI-based estimates via OpenAI to provide revenue projections and ROI 
calculations, built around a real personal use case.

---

## Overview

- Calculate short-term rental profitability using real listing data
- Estimate long-term rental returns using AI-generated insights
- Compare both strategies side by side
- Designed with asynchronous data fetching for performance

---

## Tech Stack

- Java 21 + Spring Boot 4.x
- MySQL + Spring Data JPA
- BrightData API (Airbnb + Booking.com scraping)
- OpenAI API (GPT-4, long-term rental estimates via prompt engineering)
- JUnit 5 + Mockito

---

## Architecture
```
Controller → Service → External APIs (BrightData / OpenAI)
                ↓
        ProfitabilityService
        (revenue, ROI, occupancy assumptions)
```

### Key design decisions

- Concurrent data fetching using `CompletableFuture.allOf()` — Airbnb 
  and Booking.com scraped simultaneously, reducing wait time by ~50%
- Asynchronous BrightData scraping with polling until results are ready
- City-based occupancy rates loaded from a JSON config file at startup 
  via `@PostConstruct` — separates market data from code
- Centralized error handling via `GlobalExceptionHandler` — maps domain 
  exceptions to meaningful HTTP status codes (404, 502, 500)
- Java 21 Records for all DTOs — immutable, concise, purpose-built 
  for data transfer
- ROI calculated on cash invested (down payment + costs), not total 
  property value — reflects real investor behaviour (Cash-on-Cash Return)

---

## Prerequisites

- Java 21
- MySQL running locally
- BrightData account with dataset IDs (Airbnb + Booking.com)
- OpenAI API key

---

## Setup
```bash
git clone https://github.com/YOUR_USERNAME/rental-profitability
cd rental-profitability

cp .env.example .env
# Add your API keys and database configuration
```

Fill in `.env.example`:
```
DATABASE_URL=jdbc:mysql://localhost:3306/rentalProfitability?createDatabaseIfNotExist=true
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
OPENAI_API_KEY=your_openai_key
OPENAI_API_URL=https://api.openai.com/v1/chat/completions
BRIGHTDATA_API_KEY=your_brightdata_key
BRIGHTDATA_API_URL=https://api.brightdata.com/datasets/v3/scrape
BRIGHTDATA_AIRBNB_DATASET_ID=your_airbnb_dataset_id
BRIGHTDATA_BOOKING_DATASET_ID=your_booking_dataset_id
```

Then run:
```bash
./mvnw spring-boot:run
```

The application will start on port 8080 and automatically create the 
required database schema.

---

## API Documentation

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/properties` | Create a property |
| GET | `/api/properties` | List all properties |
| GET | `/api/properties/{id}` | Get property by ID |
| PUT | `/api/properties/{id}` | Update a property |
| DELETE | `/api/properties/{id}` | Delete a property |
| POST | `/api/profitability/short` | Calculate short-term ROI |
| POST | `/api/profitability/long` | Calculate long-term ROI |
| POST | `/api/profitability/compare` | Compare both strategies |

### Example Request
```json
POST /api/profitability/short
{
  "propertyID": 1,
  "rentaltype": "SHORT",
  "platform": "AIRBNB",
  "propertyManagementFee": 16,
  "currency": "EUR"
}
```

### Example Response
```json
{
  "propertyId": 1,
  "rentalType": "SHORT",
  "estimatedMonthlyRevenue": 2052.0,
  "estimatedYearlyRevenue": 24624.0,
  "estimatedNetMonthlyProfit": 2480.37856,
  "estimatedNetYearlyProfit": 29764.54272,
  "ROI": 13.26,
  "result": "Based on a cash investment of €70000, with an estimated 
             monthly revenue of €2052, your annual ROI is 13.3%."
}
```

---

## Important Notes

- BrightData scraping is asynchronous and may take 1–5 minutes per request
- Long-term estimates are AI-generated via GPT-4 and not sourced from 
  real-time listings
- ROI is calculated before taxes and additional costs
- Occupancy rates are based on curated market research per city — 
  100+ cities covered across Europe, Asia, and the Americas

---

## Tests
```bash
./mvnw test
```

7 unit tests covering both successful scenarios and key error paths 
(BrightData timeout, OpenAI returning non-numeric responses).

---

## What I'd improve in production

- Replace polling with BrightData webhooks for async notification 
  instead of blocking the HTTP thread
- Store scraping results in the database to reduce API costs on 
  repeated requests
- Add JWT authentication to protect the endpoints
- Replace hardcoded occupancy rates with a database-driven approach 
  maintained by a data team