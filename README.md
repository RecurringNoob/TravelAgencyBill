# 🧳Travel Agency — Management System

> A Java Swing desktop application for travel agents to generate professional invoices covering flight bookings and car rentals.

---

## Overview

Tours Management System is a **4-step wizard** that guides a travel agent from entering customer details all the way to a polished, GST-compliant PDF invoice — in minutes.

```
Step 1 → Customer Details
Step 2 → Flight Bookings
Step 3 → Car Rentals
Step 4 → Invoice Preview & PDF Export
```

All booking data lives in a single shared session that can be saved to disk and reloaded at any time. Company branding and the GST rate persist across restarts.

---

## Features

### Core Workflow
- **Customer & Invoice Form** — name, contact, address, auto-generated invoice number
- **Flight Bookings** — add PNR, route, passengers, and fare; edit or delete any row inline
- **Car Rentals** — add vehicle number, route, and fare; edit or delete any row inline
- **Invoice Preview** — formatted text preview with flight subtotal, car subtotal, GST breakdown, and grand total
- **PDF Export** — multi-page A4 PDF with company header, itemised bookings, GST line, and page-numbered footer

### Invoice & Tax
- Configurable GST rate (default 18%)
- Separate subtotals for flights and car rentals
- Grand total = subtotal + GST, all displayed in `Rs.`

### Session Management
- Save the current session to a `.rss` file
- Load any previous session from disk (safe field-by-field copy — no stale panel references)
- Invoice History browser — scan a folder of `.rss` files, preview key details in a table, and reopen any past session directly into the invoice view

### Company Settings
- Set company name, tagline, email, and phone
- Settings persist across restarts via `~/.ridhisidhi.properties`
- Changes apply immediately to the current session

### PDF Quality
- Multi-page A4 layout — content flows to new pages automatically
- Blue header band with company name, tagline, and contact details
- Section headers for flights and car rentals
- Green grand-total highlight band
- Page X of Y numbering in the footer
- Character sanitisation — Unicode symbols (`→`, `₹`, `…`) are converted before PDF rendering, preventing encoding errors

---

## Screenshots / Layout

```
┌──────────────────────────────────────────────────┐
│  NAV BAR   [1] [2] [3] [4]    [Save][Load][⚙][⏱] │
├──────────────────────────────────────────────────┤
│                                                  │
│   STEP PANEL (CustomerForm / FlightForm / …)     │
│                                                  │
│   ┌───────────────────────┐  ┌────────────────┐  │
│   │   Form / Table area   │  │  Sidebar total │  │
│   └───────────────────────┘  └────────────────┘  │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## Architecture

The project follows a simplified **MVC** pattern:

| Role | Class(es) |
|------|-----------|
| **Model** | `BookingSession`, `AppSettings` |
| **View** | `CustomerForm`, `FlightForm`, `CarForm`, `BillView`, `SettingsPanel`, `HistoryPanel` |
| **Controller** | `MainApp` — owns the `CardLayout` and drives navigation |

```
bill/
├── MainApp.java              # JFrame host, CardLayout navigation, session owner
├── BookingSession.java       # Serializable data model — flights, cars, GST
├── AppSettings.java          # Company settings persisted to ~/.ridhisidhi.properties
├── BasePanel.java            # Abstract base — design tokens, layout helpers
├── CustomerForm.java         # Step 1
├── FlightForm.java           # Step 2 — inline edit + delete
├── CarForm.java              # Step 3 — inline edit + delete
├── BillView.java             # Step 4 — preview + PDF export
├── SettingsPanel.java        # Company config UI
├── HistoryPanel.java         # Past session browser
└── PDFGeneratorService.java  # Apache PDFBox — multi-page invoice generation
```

All panels receive the controller (`MainApp`) and the model (`BookingSession`) via **constructor injection**. `AppSettings` is a singleton accessed directly where needed.

---

## Data Model

### `BookingSession` (Serializable)

| Field | Type | Notes |
|-------|------|-------|
| `customerName` | `String` | Trimmed on set |
| `contactNumber` | `String` | |
| `address` | `String` | |
| `invoiceNumber` | `String` | |
| `bookingDate` | `Date` | |
| `gstRatePercent` | `BigDecimal` | Default 18% |
| `flights` | `List<FlightBookingData>` | PNR, route, pax, fare |
| `cars` | `List<CarBookingData>` | Car no., route, fare |

**Validation rules** (enforced on every add/update):
- PNR / car number / source / destination must be non-blank
- Source and destination must differ (case-insensitive)
- Fare must be > 0

**Totals:**
```
Subtotal    = Σ(all flight fares) + Σ(all car fares)
GST Amount  = Subtotal × gstRate / 100  (rounded HALF_UP, 2 dp)
Grand Total = Subtotal + GST Amount
```

---

## Build & Run

### Prerequisites

- Java 11 or later
- Apache Maven 3.6+

### Quick Start

```bash
git clone <repo-url>
cd ridhi-sidhi-tours
chmod +x build.sh
./build.sh
```

`build.sh` runs `mvn clean package` and immediately launches the resulting fat JAR.

### Manual Maven Build

```bash
mvn clean package
java -jar target/RidhiSidhi-Tours.jar
```

### Without Maven

```bash
# Download pdfbox-app-3.0.2.jar from https://pdfbox.apache.org/download.html
javac -cp pdfbox-app-3.0.2.jar -d classes src/bill/*.java
java  -cp classes:pdfbox-app-3.0.2.jar bill.MainApp
```

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Java SE (Swing, AWT) | 11+ | UI framework and serialization |
| Apache PDFBox | 3.0.2 | PDF generation |

No other external libraries required. All PDF fonts are Standard14 built-ins — no font files need to be bundled.

---

## Configuration Files

| File | Location | Format | Purpose |
|------|----------|--------|---------|
| Company settings | `~/.ridhisidhi.properties` | Java `.properties` | Company name, tagline, email, phone, GST rate |
| Session files | Any path you choose | Binary (`.rss`) | Saved `BookingSession` objects |

`~/.ridhisidhi.properties` is created automatically on first save and can be edited manually in a text editor if needed.

---

## Changelog — v2.0

### Bug Fixes

| # | Issue | Fix |
|---|-------|-----|
| 1 | Shared mutable `GridBagConstraints` corrupted panel layouts | `createCard()` now returns a `CardResult` with its own `gbc` per card |
| 2 | `hasCustomerDetails()` and `isValidCustomer()` behaved inconsistently | Removed the former; everything uses `isValidCustomer()` (trims whitespace) |
| 3 | Zero-item invoices could reach the preview screen | `CarForm.advance()` blocks navigation when `session.hasBookings()` is false |
| 4 | Currency symbol mismatch — UI showed `₹`, PDF showed `Rs.` | `BasePanel.CURRENCY = "Rs."` is the single constant used everywhere |
| 5 | Added entries could not be removed | Both tables now have a `🗑 Del` column |
| 6 | `SpringUtilities` class was unused | Removed from the package |
| 7 | Returning to the customer form lost previously entered data | `CustomerForm.populateFromSession()` fills from the live session on every load |
| 8 | Long names broke PDF column alignment | All PDF text goes through `truncate()` with fixed column x-coordinates |

### New Features

| Feature | Classes Affected |
|---------|-----------------|
| Inline row editing — click any table row to edit it | `FlightForm`, `CarForm` |
| Full input validation — empty fields, src == dest, fare ≤ 0 | `FlightForm`, `CarForm`, `BookingSession` |
| GST line item — configurable rate shown in preview and PDF | `BookingSession`, `BillView`, `PDFGeneratorService` |
| Multi-page PDF — content flows to new pages automatically | `PDFGeneratorService` |
| Session save / load — serialize bookings to `.rss` files | `BookingSession`, `MainApp` |
| Invoice history — browse a folder of saved sessions | `HistoryPanel` |
| Company settings — name, contact, GST rate, persisted across restarts | `SettingsPanel`, `AppSettings` |
| Page numbers in PDF footer | `PDFGeneratorService` |
| Minimum window size enforced (900 × 650 px) | `MainApp` |
| Table header reordering disabled | `BasePanel.styleTable()` |

---

## UI Design Tokens

All constants live on `BasePanel` and are inherited by every panel.

| Constant | Hex | Usage |
|----------|-----|-------|
| `PRIMARY_COLOR` | `#2980B9` | Nav bar, primary buttons, PDF headers |
| `SUCCESS_COLOR` | `#27AE60` | Add buttons, grand total highlight |
| `DANGER_COLOR` | `#E74C3C` | Reserved for destructive actions |
| `WARNING_COLOR` | `#F39C12` | "New Booking" button |
| `ACCENT_COLOR` | `#2C3E50` | Page title text |
| `BACKGROUND_COLOR` | `#ECF0F1` | Panel backgrounds |
| `TEXT_COLOR` | `#2C3E50` | Body text |
| `TEXT_MUTED` | `#7F8C8D` | Labels, secondary text, Back buttons |
| `BORDER_COLOR` | `#BDC3C7` | Input outlines, table separators |

To change the currency symbol across the entire application, update one constant:

```java
// BasePanel.java
protected static final String CURRENCY = "Rs.";
```

---


