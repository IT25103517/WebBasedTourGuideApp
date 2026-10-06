/* ============================================================================
   Web Based Tour Guide  -  Seed / demo data
   Run AFTER 01_schema.sql

   Every seeded account uses the password:   Password@123
   ========================================================================== */

USE TourGuideDB;
GO

DECLARE @pw NVARCHAR(255) = N'$2b$10$QjtfG/sHO0gruCuXuU7V0uzUQ3uZ2OWlaHUXL2RZxGl/1OmTeK3a.';   -- bcrypt hash of 'Password@123'

/* ---------------- Users ---------------- */
INSERT INTO dbo.Users (full_name, email, password_hash, phone, role) VALUES
 (N'System Administrator', N'admin@tourguide.lk',  @pw, N'0112000000', 'ADMIN'),
 (N'Kasun Perera',         N'kasun.guide@mail.com', @pw, N'0771234567', 'GUIDE'),
 (N'Nimali Fernando',      N'nimali.guide@mail.com',@pw, N'0779876543', 'GUIDE'),
 (N'Emma Watson',          N'emma.tourist@mail.com',@pw, N'0761112222', 'TOURIST'),
 (N'Liam Schmidt',         N'liam.tourist@mail.com',@pw, N'0763334444', 'TOURIST');

DECLARE @admin INT = (SELECT user_id FROM dbo.Users WHERE email = N'admin@tourguide.lk');
DECLARE @g1    INT = (SELECT user_id FROM dbo.Users WHERE email = N'kasun.guide@mail.com');
DECLARE @g2    INT = (SELECT user_id FROM dbo.Users WHERE email = N'nimali.guide@mail.com');
DECLARE @t1    INT = (SELECT user_id FROM dbo.Users WHERE email = N'emma.tourist@mail.com');
DECLARE @t2    INT = (SELECT user_id FROM dbo.Users WHERE email = N'liam.tourist@mail.com');

INSERT INTO dbo.Administrators (admin_id) VALUES (@admin);

INSERT INTO dbo.TourGuides (guide_id, experience_years, verification_status, bio, languages, base_location) VALUES
 (@g1, 8, 'VERIFIED', N'Licensed national guide specialising in the Cultural Triangle.', N'English, Sinhala, German', N'Kandy'),
 (@g2, 4, 'VERIFIED', N'Wildlife and beach tours across the southern province.',          N'English, Sinhala, Tamil', N'Galle');

INSERT INTO dbo.Tourists (tourist_id, nationality, preferred_language) VALUES
 (@t1, N'United Kingdom', N'English'),
 (@t2, N'Germany',        N'German');

/* ---------------- FR-01  Tour Packages ---------------- */
INSERT INTO dbo.TourPackages (guide_id, title, description, destination, duration_days, base_price, status) VALUES
 (@g1, N'Cultural Triangle Explorer', N'Sigiriya, Dambulla and Polonnaruwa over three unforgettable days.', N'Sigiriya', 3, 45000.00, 'PUBLISHED'),
 (@g1, N'Kandy Heritage Day Tour',    N'Temple of the Tooth, botanical gardens and a cultural show.',       N'Kandy',    1, 12000.00, 'PUBLISHED'),
 (@g2, N'Yala Safari & Southern Coast', N'Two days of leopard spotting followed by the Mirissa coastline.', N'Yala',     2, 38000.00, 'PUBLISHED'),
 (@g2, N'Galle Fort Walking Tour',    N'Half day walk through the Dutch fort and its ramparts.',            N'Galle',    1,  8000.00, 'DRAFT');

DECLARE @p1 INT = (SELECT package_id FROM dbo.TourPackages WHERE title = N'Cultural Triangle Explorer');
DECLARE @p2 INT = (SELECT package_id FROM dbo.TourPackages WHERE title = N'Kandy Heritage Day Tour');
DECLARE @p3 INT = (SELECT package_id FROM dbo.TourPackages WHERE title = N'Yala Safari & Southern Coast');

/* ---------------- FR-02  Availability ---------------- */
INSERT INTO dbo.Availability (guide_id, package_id, start_date, end_date, max_group_size, price_override, status, note) VALUES
 (@g1, NULL, DATEADD(day, 1,  CAST(GETDATE() AS DATE)), DATEADD(day, 45, CAST(GETDATE() AS DATE)), 12, NULL,     'AVAILABLE', N'Open season'),
 (@g1, NULL, DATEADD(day, 10, CAST(GETDATE() AS DATE)), DATEADD(day, 12, CAST(GETDATE() AS DATE)),  1, NULL,     'BLOCKED',   N'Personal leave'),
 (@g2, @p3,  DATEADD(day, 2,  CAST(GETDATE() AS DATE)), DATEADD(day, 60, CAST(GETDATE() AS DATE)),  8, 35000.00, 'AVAILABLE', N'Safari season rate');

/* ---------------- FR-03  Itineraries ---------------- */
INSERT INTO dbo.TourItineraries (package_id, day_number, day_title, description) VALUES
 (@p1, 1, N'Arrival & Dambulla Cave Temple', N'Pick up from Colombo, lunch en route, evening cave temple visit.'),
 (@p1, 2, N'Sigiriya Rock Fortress',         N'Early climb to beat the heat, museum visit, village tour.'),
 (@p1, 3, N'Polonnaruwa Ancient City',       N'Cycle through the ruins, then transfer back.'),
 (@p2, 1, N'Kandy in a Day',                 N'Temple of the Tooth, gardens, cultural dance show.');

DECLARE @i1 INT = (SELECT itinerary_id FROM dbo.TourItineraries WHERE package_id = @p1 AND day_number = 1);
DECLARE @i2 INT = (SELECT itinerary_id FROM dbo.TourItineraries WHERE package_id = @p1 AND day_number = 2);

INSERT INTO dbo.ItineraryActivities (itinerary_id, activity_name, location, start_time, end_time, sort_order) VALUES
 (@i1, N'Hotel pickup',        N'Colombo',   '07:00', '07:30', 1),
 (@i1, N'Lunch stop',          N'Kurunegala','12:30', '13:30', 2),
 (@i1, N'Cave temple visit',   N'Dambulla',  '16:00', '18:00', 3),
 (@i2, N'Sigiriya rock climb', N'Sigiriya',  '06:30', '10:00', 1),
 (@i2, N'Village boat ride',   N'Sigiriya',  '15:00', '17:00', 2);

/* ---------------- FR-05  Special Offers ---------------- */
INSERT INTO dbo.SpecialOffers (guide_id, title, description, discount_type, discount_value, start_date, end_date, status) VALUES
 (@g1, N'Monsoon Season Deal', N'15% off all cultural tours during the off season.', 'PERCENT', 15.00, DATEADD(day,-5, CAST(GETDATE() AS DATE)), DATEADD(day, 40, CAST(GETDATE() AS DATE)), 'ACTIVE'),
 (@g2, N'Early Bird Safari',   N'LKR 5000 off when booked 30 days ahead.',           'FIXED',  5000.00, DATEADD(day,-2, CAST(GETDATE() AS DATE)), DATEADD(day, 90, CAST(GETDATE() AS DATE)), 'ACTIVE'),
 (@g1, N'Avurudu Promotion',   N'Expired new year promotion kept for reporting.',    'PERCENT', 10.00, DATEADD(day,-90,CAST(GETDATE() AS DATE)), DATEADD(day,-60, CAST(GETDATE() AS DATE)), 'EXPIRED');

DECLARE @o1 INT = (SELECT offer_id FROM dbo.SpecialOffers WHERE title = N'Monsoon Season Deal');
DECLARE @o2 INT = (SELECT offer_id FROM dbo.SpecialOffers WHERE title = N'Early Bird Safari');

INSERT INTO dbo.OfferPackages (offer_id, package_id) VALUES (@o1, @p1), (@o1, @p2), (@o2, @p3);

/* ---------------- FR-04  Bookings ---------------- */
INSERT INTO dbo.Bookings (tourist_id, guide_id, package_id, offer_id, tour_date, total_amount, status) VALUES
 (@t1, @g1, @p1, @o1, DATEADD(day, 20, CAST(GETDATE() AS DATE)), 38250.00, 'CONFIRMED'),
 (@t2, @g2, @p3, NULL, DATEADD(day, 25, CAST(GETDATE() AS DATE)), 38000.00, 'PENDING'),
 (@t1, @g1, @p2, NULL, DATEADD(day,-15, CAST(GETDATE() AS DATE)), 12000.00, 'COMPLETED');

DECLARE @b1 INT = (SELECT MIN(booking_id) FROM dbo.Bookings);
DECLARE @b3 INT = (SELECT MAX(booking_id) FROM dbo.Bookings);

INSERT INTO dbo.BookingDetails (booking_id, description, group_size, price) VALUES
 (@b1, N'Cultural Triangle Explorer - 2 adults', 2, 38250.00),
 (@b3, N'Kandy Heritage Day Tour - 2 adults',    2, 12000.00);

/* ---------------- FR-06  Reviews ---------------- */
INSERT INTO dbo.Reviews (booking_id, tourist_id, guide_id, rating, comment) VALUES
 (@b3, @t1, @g1, 5, N'Kasun was punctual, knowledgeable and great with our children. Highly recommended.');

PRINT 'Seed data inserted. All demo accounts use the password Password@123';
GO
