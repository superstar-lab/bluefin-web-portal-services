Use BlueFin;

DROP PROCEDURE IF EXISTS spTransactionStatusMetrics;

DELIMITER $$
CREATE PROCEDURE `spTransactionStatusMetrics`(
    IN initialDate VARCHAR(255),
	IN endDate VARCHAR(255),
    IN transactionStatus VARCHAR(32)
)
BEGIN
	DECLARE finished INTEGER DEFAULT 0;
	DECLARE active_Processor varchar(100) DEFAULT "";
	-- declare cursor for getting active processors
	DECLARE curProcessor
		CURSOR FOR
			SELECT ProcessorName FROM PaymentProcessor_Lookup
			Where IsActive = 1;
	-- declare NOT FOUND handler
	DECLARE CONTINUE HANDLER
        FOR NOT FOUND SET finished = 1;

	DROP TEMPORARY TABLE IF EXISTS declineSummary;
	CREATE TEMPORARY  TABLE declineSummary(Processor VARCHAR(50), Declined VARCHAR(50), Approved VARCHAR(50), Rate VARCHAR(50));
	OPEN curProcessor;

	getProcessor: LOOP
		FETCH curProcessor INTO active_Processor;
			SET @declinedTotal = (SELECT count(SaleTransactionID) FROM Sale_Transaction
			where InternalStatusCode = transactionStatus
			AND Processor = active_Processor
			AND TransactionDateTime BETWEEN initialDate AND endDate
			order by 1 desc);

            SET @approvedTotal = (SELECT count(SaleTransactionID) FROM Sale_Transaction
			where InternalStatusCode = '1'
			AND Processor = active_Processor
			AND TransactionDateTime BETWEEN initialDate AND endDate
			ORDER BY 1 DESC);

            IF @declinedTotal <> 0 THEN
				SET @rate = ROUND(((@declinedTotal/(@approvedTotal+@declinedTotal))*100),2); 
			ELSE
				SET @rate = 0;
            END IF;

		IF finished = 1 THEN
			LEAVE getProcessor;
		END IF;
        INSERT INTO declineSummary Values(active_Processor,@declinedTotal,@approvedTotal,@rate);
	END LOOP getProcessor;
	CLOSE curProcessor;
	SELECT * FROM declineSummary;
    DROP TEMPORARY TABLE declineSummary;
END $$
DELIMITER ;