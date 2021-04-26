Use BlueFin;

DROP PROCEDURE IF EXISTS spGetApprovedTransactionSummary;

DELIMITER $$
CREATE PROCEDURE `spGetApprovedTransactionSummary`(IN dateFrom VARCHAR(25), IN dateTo VARCHAR(25))
BEGIN
	SELECT LegalEntityApp, COUNT(1) as Transactions, TRUNCATE(COALESCE(SUM(ChargeAmount), 0), 2) as TotalAmount, Processor FROM Sale_Transaction WHERE TransactionType = 'SALE'
	AND InternalStatusCode = '1'
	AND TransactionDateTime BETWEEN dateFrom AND dateTo
	AND Processor in ( SELECT ProcessorName FROM PaymentProcessor_Lookup WHERE IsActive = '1' )
	GROUP BY LegalEntityApp, Processor
	ORDER BY Processor, LegalEntityApp;
END$$
DELIMITER ;
