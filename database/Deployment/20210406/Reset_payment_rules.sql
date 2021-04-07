Use BlueFin;

START TRANSACTION;

SELECT 
	@repay_credit_total:= IFNULL(sum(ChargeAmount),0) as repay_credit_total FROM Sale_Transaction
where  Processor = 'REPAY' AND CardType = 'Credit'
and InternalStatusDescription = 'Transaction Approved'
AND (cast(TransactionDateTime as datetime) BETWEEN '2021-04-01 03:55:01.157360' AND NOW());

SELECT @tsys_debit_total:= IFNULL(sum(ChargeAmount),0) as tsys_debit_total FROM Sale_Transaction
where  Processor = 'TSYS' AND CardType = 'Debit'
and InternalStatusDescription = 'Transaction Approved'
AND (cast(TransactionDateTime as datetime) BETWEEN '2021-04-01 03:55:01.157360' AND NOW());
		
SELECT @repay_debit_total:= IFNULL(sum(ChargeAmount),0) as repay_debit_total FROM Sale_Transaction
where  Processor = 'REPAY' AND CardType = 'Debit'
and InternalStatusDescription = 'Transaction Approved'
AND (cast(TransactionDateTime as datetime) BETWEEN '2021-04-01 03:55:01.157360' AND NOW());

-- Update REPAY Credit
update PaymentProcessor_Rule set MonthToDateCumulativeAmount = @repay_credit_total
where PaymentProcessorRuleId = 9;

-- Update REPAY - Debit
update PaymentProcessor_Rule set MonthToDateCumulativeAmount = @repay_debit_total, 
ConsumedPercentage = ROUND(((@repay_debit_total/(@tsys_debit_total+@repay_debit_total))*100),2)
where PaymentProcessorRuleId = 8; 

-- Update TSYS - Debit
update PaymentProcessor_Rule set MonthToDateCumulativeAmount = @tsys_debit_total, 
ConsumedPercentage = ROUND(((@tsys_debit_total/(@tsys_debit_total+@repay_debit_total))*100),2)
where PaymentProcessorRuleId = 3; 

COMMIT;

