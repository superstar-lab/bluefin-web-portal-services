Use BlueFin;

DROP PROCEDURE IF EXISTS spTopTransactionStatus;

DELIMITER $$
CREATE PROCEDURE `spTopTransactionStatus`(
	IN top INTEGER,
    IN statusCode VARCHAR(32),
    IN initialDate VARCHAR(255),
	IN endDate VARCHAR(255)
)
BEGIN
	DECLARE pName varchar(100) DEFAULT "" ;
	DECLARE finished INTEGER DEFAULT 0;
	DECLARE curProcessor
		CURSOR FOR
			SELECT ProcessorName FROM PaymentProcessor_Lookup WHERE IsActive = 1;
	DECLARE CONTINUE HANDLER
    FOR NOT FOUND SET finished = 1;

    CREATE TEMPORARY TABLE tmpTblTopTransactionStatus(Processor VARCHAR(100),DeclineReason VARCHAR(100),TotalTransactions VARCHAR(100),Rate VARCHAR(100));

    OPEN curProcessor;
		getProcessor: LOOP
			FETCH curProcessor INTO pName;
            IF finished = 1 THEN
				LEAVE getProcessor;
			END IF;
            SET @total =  (select count(SaleTransactionID) from Sale_Transaction WHERE InternalStatusCode = statusCode AND TransactionDateTime BETWEEN initialDate AND endDate AND Processor = pName);
				INSERT INTO tmpTblTopTransactionStatus
					SELECT Processor,PaymentProcessorResponseCodeDescription AS DeclineReason
					,COUNT(SaleTransactionID) AS TotalTransactions
					,CONCAT(ROUND(((count(SaleTransactionID) / @total)* 100),2),'%')  AS Rate
					FROM Sale_Transaction
					WHERE InternalStatusCode = statusCode AND TransactionDateTime BETWEEN initialDate AND endDate AND Processor = pName
					GROUP BY PaymentProcessorResponseCodeDescription
					ORDER BY TotalTransactions DESC LIMIT top;
        END LOOP getProcessor;
	CLOSE curProcessor;

    SELECT * FROM tmpTblTopTransactionStatus;
    DROP TEMPORARY TABLE tmpTblTopTransactionStatus;

    END$$
DELIMITER ;
