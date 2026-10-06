/* ============================================================================
   Web Based Tour Guide  -  Database Schema (Microsoft SQL Server)
   Group: 2026-Y2-S1-MLB-WEB3G1-03
   Derived directly from the group ER diagram.

   HOW TO RUN
     1. Open SQL Server Management Studio (SSMS) or Azure Data Studio
     2. Connect to your SQL Server instance
     3. Open this file and press Execute (F5)
     4. Then run 02_seed.sql the same way
   ========================================================================== */

IF DB_ID('TourGuideDB') IS NULL
    CREATE DATABASE TourGuideDB;
GO

USE TourGuideDB;
GO

/* ---------- drop in reverse dependency order (safe re-run) ---------------- */
IF OBJECT_ID('dbo.OfferPackages','U')      IS NOT NULL DROP TABLE dbo.OfferPackages;
IF OBJECT_ID('dbo.Reviews','U')            IS NOT NULL DROP TABLE dbo.Reviews;
IF OBJECT_ID('dbo.BookingDetails','U')     IS NOT NULL DROP TABLE dbo.BookingDetails;
IF OBJECT_ID('dbo.Bookings','U')           IS NOT NULL DROP TABLE dbo.Bookings;
IF OBJECT_ID('dbo.SpecialOffers','U')      IS NOT NULL DROP TABLE dbo.SpecialOffers;
IF OBJECT_ID('dbo.ItineraryActivities','U')IS NOT NULL DROP TABLE dbo.ItineraryActivities;
IF OBJECT_ID('dbo.TourItineraries','U')    IS NOT NULL DROP TABLE dbo.TourItineraries;
IF OBJECT_ID('dbo.Availability','U')       IS NOT NULL DROP TABLE dbo.Availability;
IF OBJECT_ID('dbo.TourPackages','U')       IS NOT NULL DROP TABLE dbo.TourPackages;
IF OBJECT_ID('dbo.Documents','U')          IS NOT NULL DROP TABLE dbo.Documents;
IF OBJECT_ID('dbo.Administrators','U')     IS NOT NULL DROP TABLE dbo.Administrators;
IF OBJECT_ID('dbo.TourGuides','U')         IS NOT NULL DROP TABLE dbo.TourGuides;
IF OBJECT_ID('dbo.Tourists','U')           IS NOT NULL DROP TABLE dbo.Tourists;
IF OBJECT_ID('dbo.Users','U')              IS NOT NULL DROP TABLE dbo.Users;
GO

/* ========================================================================== */
/*  SHARED  -  User hierarchy (the ISA relationship in the ER diagram)        */
/* ========================================================================== */

CREATE TABLE dbo.Users (
    user_id        INT IDENTITY(1,1) PRIMARY KEY,
    full_name      NVARCHAR(120)  NOT NULL,
    email          NVARCHAR(160)  NOT NULL UNIQUE,
    password_hash  NVARCHAR(255)  NOT NULL,
    phone          NVARCHAR(20)   NULL,
    role           VARCHAR(10)    NOT NULL
                   CONSTRAINT CK_Users_role CHECK (role IN ('TOURIST','GUIDE','ADMIN')),
    is_active      BIT            NOT NULL DEFAULT 1,
    date_created   DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE TABLE dbo.Tourists (
    tourist_id         INT PRIMARY KEY,
    nationality        NVARCHAR(60)  NULL,
    preferred_language NVARCHAR(60)  NULL,
    CONSTRAINT FK_Tourists_Users FOREIGN KEY (tourist_id)
        REFERENCES dbo.Users(user_id) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.TourGuides (
    guide_id            INT PRIMARY KEY,
    experience_years    INT           NOT NULL DEFAULT 0,
    verification_status VARCHAR(10)   NOT NULL DEFAULT 'PENDING'
                        CONSTRAINT CK_Guides_status
                        CHECK (verification_status IN ('PENDING','VERIFIED','REJECTED')),
    bio                 NVARCHAR(1000) NULL,
    languages           NVARCHAR(200)  NULL,
    base_location       NVARCHAR(100)  NULL,
    CONSTRAINT FK_Guides_Users FOREIGN KEY (guide_id)
        REFERENCES dbo.Users(user_id) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.Administrators (
    admin_id INT PRIMARY KEY,
    CONSTRAINT FK_Admins_Users FOREIGN KEY (admin_id)
        REFERENCES dbo.Users(user_id) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.Documents (
    document_id       INT IDENTITY(1,1) PRIMARY KEY,
    guide_id          INT           NOT NULL,
    doc_type          NVARCHAR(60)  NOT NULL,
    file_path         NVARCHAR(300) NOT NULL,
    submitted_date    DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    status            VARCHAR(10)   NOT NULL DEFAULT 'PENDING'
                      CONSTRAINT CK_Doc_status CHECK (status IN ('PENDING','VERIFIED','REJECTED')),
    verified_by       INT           NULL,
    verification_date DATETIME2     NULL,
    CONSTRAINT FK_Doc_Guide FOREIGN KEY (guide_id)
        REFERENCES dbo.TourGuides(guide_id) ON DELETE CASCADE,
    CONSTRAINT FK_Doc_Admin FOREIGN KEY (verified_by)
        REFERENCES dbo.Administrators(admin_id)
);
GO

/* ========================================================================== */
/*  FR-01  TOUR PACKAGE MANAGEMENT            (Nanayakkara K.U.  IT25103494)  */
/* ========================================================================== */

CREATE TABLE dbo.TourPackages (
    package_id    INT IDENTITY(1,1) PRIMARY KEY,
    guide_id      INT            NOT NULL,
    title         NVARCHAR(150)  NOT NULL,
    description   NVARCHAR(2000) NULL,
    destination   NVARCHAR(120)  NOT NULL,
    duration_days INT            NOT NULL CHECK (duration_days BETWEEN 1 AND 60),
    base_price    DECIMAL(10,2)  NOT NULL CHECK (base_price >= 0),
    image_url     NVARCHAR(300)  NULL,
    status        VARCHAR(12)    NOT NULL DEFAULT 'DRAFT'
                  CONSTRAINT CK_Pkg_status CHECK (status IN ('DRAFT','PUBLISHED','UNAVAILABLE')),
    created_at    DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at    DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Pkg_Guide FOREIGN KEY (guide_id)
        REFERENCES dbo.TourGuides(guide_id)
);
GO
CREATE INDEX IX_Pkg_Guide        ON dbo.TourPackages(guide_id);
CREATE INDEX IX_Pkg_Destination  ON dbo.TourPackages(destination);
GO

/* ========================================================================== */
/*  FR-02  AVAILABILITY CALENDAR MANAGEMENT   (Ashfak M.A.M.    IT25103511)  */
/*  package_id is NULL  ->  the window applies to ALL packages of the guide   */
/* ========================================================================== */

CREATE TABLE dbo.Availability (
    availability_id INT IDENTITY(1,1) PRIMARY KEY,
    guide_id        INT           NOT NULL,
    package_id      INT           NULL,
    start_date      DATE          NOT NULL,
    end_date        DATE          NOT NULL,
    max_group_size  INT           NOT NULL DEFAULT 10 CHECK (max_group_size > 0),
    price_override  DECIMAL(10,2) NULL,
    status          VARCHAR(10)   NOT NULL DEFAULT 'AVAILABLE'
                    CONSTRAINT CK_Avail_status CHECK (status IN ('AVAILABLE','BLOCKED')),
    note            NVARCHAR(300) NULL,
    created_at      DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_Avail_range CHECK (end_date >= start_date),
    CONSTRAINT FK_Avail_Guide FOREIGN KEY (guide_id)
        REFERENCES dbo.TourGuides(guide_id) ON DELETE CASCADE,
    CONSTRAINT FK_Avail_Pkg FOREIGN KEY (package_id)
        REFERENCES dbo.TourPackages(package_id)
);
GO
CREATE INDEX IX_Avail_Guide_Dates ON dbo.Availability(guide_id, start_date, end_date);
GO

/* ========================================================================== */
/*  FR-03  TOUR ITINERARY MANAGEMENT          (Wijesinghe W.M.P.N. IT25103517)*/
/* ========================================================================== */

CREATE TABLE dbo.TourItineraries (
    itinerary_id INT IDENTITY(1,1) PRIMARY KEY,
    package_id   INT            NOT NULL,
    day_number   INT            NOT NULL CHECK (day_number >= 1),
    day_title    NVARCHAR(150)  NOT NULL,
    description  NVARCHAR(1500) NULL,
    created_at   DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Itin_Pkg FOREIGN KEY (package_id)
        REFERENCES dbo.TourPackages(package_id) ON DELETE CASCADE,
    CONSTRAINT UQ_Itin_Pkg_Day UNIQUE (package_id, day_number)
);
GO

CREATE TABLE dbo.ItineraryActivities (
    activity_id   INT IDENTITY(1,1) PRIMARY KEY,
    itinerary_id  INT            NOT NULL,
    activity_name NVARCHAR(150)  NOT NULL,
    location      NVARCHAR(150)  NULL,
    start_time    TIME(0)        NULL,
    end_time      TIME(0)        NULL,
    sort_order    INT            NOT NULL DEFAULT 1,
    CONSTRAINT FK_Act_Itin FOREIGN KEY (itinerary_id)
        REFERENCES dbo.TourItineraries(itinerary_id) ON DELETE CASCADE
);
GO
CREATE INDEX IX_Act_Itin ON dbo.ItineraryActivities(itinerary_id);
GO

/* ========================================================================== */
/*  FR-04  BOOKING MANAGEMENT                 (Dharmapriya R.T.S. IT25103521) */
/* ========================================================================== */

CREATE TABLE dbo.Bookings (
    booking_id     INT IDENTITY(1,1) PRIMARY KEY,
    tourist_id     INT           NOT NULL,
    guide_id       INT           NOT NULL,
    package_id     INT           NOT NULL,
    offer_id       INT           NULL,          -- FK added after SpecialOffers exists
    booking_date   DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    tour_date      DATE          NOT NULL,
    total_amount   DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    status         VARCHAR(12)   NOT NULL DEFAULT 'PENDING'
                   CONSTRAINT CK_Book_status
                   CHECK (status IN ('PENDING','CONFIRMED','REJECTED','COMPLETED','CANCELLED')),
    is_deleted     BIT           NOT NULL DEFAULT 0,   -- soft delete, keeps history
    updated_at     DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Book_Tourist FOREIGN KEY (tourist_id) REFERENCES dbo.Tourists(tourist_id),
    CONSTRAINT FK_Book_Guide   FOREIGN KEY (guide_id)   REFERENCES dbo.TourGuides(guide_id),
    CONSTRAINT FK_Book_Pkg     FOREIGN KEY (package_id) REFERENCES dbo.TourPackages(package_id)
);
GO

/* Business rule from the ER diagram:
   "A tour guide handles only one booking per day."
   A filtered unique index enforces it for live bookings only.            */
CREATE UNIQUE INDEX UX_Guide_OneBookingPerDay
    ON dbo.Bookings(guide_id, tour_date)
    WHERE status IN ('PENDING','CONFIRMED') AND is_deleted = 0;
GO

CREATE TABLE dbo.BookingDetails (
    detail_id   INT IDENTITY(1,1) PRIMARY KEY,
    booking_id  INT            NOT NULL,
    description NVARCHAR(200)  NOT NULL DEFAULT N'Tour package',
    group_size  INT            NOT NULL CHECK (group_size > 0),
    price       DECIMAL(10,2)  NOT NULL CHECK (price >= 0),
    CONSTRAINT FK_Detail_Booking FOREIGN KEY (booking_id)
        REFERENCES dbo.Bookings(booking_id) ON DELETE CASCADE
);
GO

/* ========================================================================== */
/*  FR-05  SPECIAL OFFERS & PROMOTIONS        (Hunaif A.A.      IT25103523)  */
/* ========================================================================== */

CREATE TABLE dbo.SpecialOffers (
    offer_id       INT IDENTITY(1,1) PRIMARY KEY,
    guide_id       INT            NOT NULL,
    title          NVARCHAR(150)  NOT NULL,
    description    NVARCHAR(1000) NULL,
    discount_type  VARCHAR(10)    NOT NULL
                   CONSTRAINT CK_Offer_type CHECK (discount_type IN ('PERCENT','FIXED')),
    discount_value DECIMAL(10,2)  NOT NULL CHECK (discount_value > 0),
    start_date     DATE           NOT NULL,
    end_date       DATE           NOT NULL,
    status         VARCHAR(10)    NOT NULL DEFAULT 'ACTIVE'
                   CONSTRAINT CK_Offer_status CHECK (status IN ('ACTIVE','INACTIVE','EXPIRED')),
    created_at     DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_Offer_range CHECK (end_date >= start_date),
    CONSTRAINT FK_Offer_Guide FOREIGN KEY (guide_id)
        REFERENCES dbo.TourGuides(guide_id) ON DELETE CASCADE
);
GO

/* "Applies to" relationship: one offer covers one or more packages */
CREATE TABLE dbo.OfferPackages (
    offer_id   INT NOT NULL,
    package_id INT NOT NULL,
    CONSTRAINT PK_OfferPackages PRIMARY KEY (offer_id, package_id),
    CONSTRAINT FK_OP_Offer FOREIGN KEY (offer_id)
        REFERENCES dbo.SpecialOffers(offer_id) ON DELETE CASCADE,
    CONSTRAINT FK_OP_Pkg FOREIGN KEY (package_id)
        REFERENCES dbo.TourPackages(package_id) ON DELETE CASCADE
);
GO

ALTER TABLE dbo.Bookings
    ADD CONSTRAINT FK_Book_Offer FOREIGN KEY (offer_id) REFERENCES dbo.SpecialOffers(offer_id);
GO

/* ========================================================================== */
/*  FR-06  RATINGS AND REVIEWS                (Sajini S.B.      IT25103526)  */
/* ========================================================================== */

CREATE TABLE dbo.Reviews (
    review_id   INT IDENTITY(1,1) PRIMARY KEY,
    booking_id  INT            NOT NULL,
    tourist_id  INT            NOT NULL,
    guide_id    INT            NOT NULL,
    rating      INT            NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     NVARCHAR(1000) NULL,
    review_date DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    is_hidden   BIT            NOT NULL DEFAULT 0,   -- admin moderation
    CONSTRAINT UQ_Review_Booking UNIQUE (booking_id),  -- one review per booking
    CONSTRAINT FK_Rev_Booking FOREIGN KEY (booking_id) REFERENCES dbo.Bookings(booking_id),
    CONSTRAINT FK_Rev_Tourist FOREIGN KEY (tourist_id) REFERENCES dbo.Tourists(tourist_id),
    CONSTRAINT FK_Rev_Guide   FOREIGN KEY (guide_id)   REFERENCES dbo.TourGuides(guide_id)
);
GO
CREATE INDEX IX_Rev_Guide ON dbo.Reviews(guide_id);
GO

PRINT 'TourGuideDB schema created successfully.';
GO
