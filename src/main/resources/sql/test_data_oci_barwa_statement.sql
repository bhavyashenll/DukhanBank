-- Test Data for Download E-Statement Endpoint
-- This script inserts sample statement records for testing

-- Insert statement for account 100003049861, customer 15450, November 2025
INSERT INTO oci_barwa_statement
(customer_number, path, source_number, source_type, statement_date, statement_type)
VALUES
('15450', '/statements/15450_100003049861_NOV2025.pdf', '100003049861', 'CURRENT', '2025-11-15 00:00:00', 'MONTHLY')
ON CONFLICT DO NOTHING;

-- Insert statement for account 100003049861, customer 15450, October 2025
INSERT INTO oci_barwa_statement
(customer_number, path, source_number, source_type, statement_date, statement_type)
VALUES
('15450', '/statements/15450_100003049861_OCT2025.pdf', '100003049861', 'CURRENT', '2025-10-15 00:00:00', 'MONTHLY')
ON CONFLICT DO NOTHING;

-- Insert statement for account 100003049861, customer 15450, September 2025
INSERT INTO oci_barwa_statement
(customer_number, path, source_number, source_type, statement_date, statement_type)
VALUES
('15450', '/statements/15450_100003049861_SEP2025.pdf', '100003049861', 'CURRENT', '2025-09-15 00:00:00', 'MONTHLY')
ON CONFLICT DO NOTHING;

-- Note: The unique index ensures one statement per customer/account/month combination
-- If you need to test with different statement types, use different statement_type values

