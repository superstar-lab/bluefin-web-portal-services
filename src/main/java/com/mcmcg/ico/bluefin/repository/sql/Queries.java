package com.mcmcg.ico.bluefin.repository.sql;

public interface Queries {
	String findAllApplications = "SELECT ApplicationID, ApplicationName FROM Application_Lookup";
	String findAllOriginPaymentFrequencies = "SELECT OriginPaymentFrequencyID, Origin, PaymentFrequency, DateCreated, DateModified, ModifiedBy FROM OriginPaymentFrequency_Lookup";
	String findPermissionByPermissionId = "SELECT PermissionID, PermissionName, Description, DateCreated, DatedModified, ModifiedBy FROM Permission_Lookup WHERE PermissionID = ?";
	String findPermissionByPermissionName = "SELECT PermissionID, PermissionName, Description, DateCreated, DatedModified, ModifiedBy FROM Permission_Lookup WHERE PermissionName = ?";
	String savePermission = "INSERT INTO Permission_Lookup (PermissionName, Description, DateCreated, DatedModified, ModifiedBy) VALUES (?, ?, ?, ?, ?)";
	String findAllRoles = "SELECT RoleID, RoleName, Description, DateCreated, DatedModified, ModifiedBy FROM Role_Lookup";
	String findAllRolesByIds = "SELECT RoleID, RoleName, Description, DateCreated, DatedModified, ModifiedBy FROM Role_Lookup WHERE RoleID IN (:roleIds)";
	String findRoleByRoleId = "SELECT RoleID, RoleName, Description, DateCreated, DatedModified, ModifiedBy FROM Role_Lookup WHERE RoleID = ?";
	String findRoleByRoleName = "SELECT RoleID, RoleName, Description, DateCreated, DatedModified, ModifiedBy FROM Role_Lookup WHERE RoleName = ?";
	String saveRole = "INSERT INTO Role_Lookup (RoleName, Description, DateCreated, DatedModified, ModifiedBy) VALUES (?, ?, ?, ?, ?)";
	String deleteRoleByRoleName = "DELETE FROM Role_Lookup WHERE RoleName = ?";
	String findRolePermissionByRolePermissionId = "SELECT RolePermissionID, RoleID, PermissionID, DateCreated, DatedModified, ModifiedBy FROM Role_Permission";
	String findRolePermissionByRoleId = "SELECT RolePermissionID, RoleID, PermissionID, DateCreated, DatedModified, ModifiedBy FROM Role_Permission WHERE RoleID = ?";
	String saveRolePermission = "INSERT INTO Role_Permission (RoleID, PermissionID, DateCreated, DatedModified, ModifiedBy) VALUES (?, ?, ?, ?, ?)";
	String findPropertyByName = "SELECT ApplicationpropertyID, ApplicationPropertyName, ApplicationPropertyValue, DataType, Description, DateCreated, DateModified, ModifiedBy FROM ApplicationProperty_Lookup WHERE ApplicationPropertyName = ?";
	String findSecurityTokenBlacklistByTokenId = "SELECT TokenID, Token, Type, UserID, DateCreated FROM SecurityToken_Backlist WHERE TokenID = ?";
	String findSecurityTokenBlacklistByToken = "SELECT TokenID, Token, Type, UserID, DateCreated FROM SecurityToken_Backlist WHERE Token = ?";
	String findSecurityTokenBlacklistByUserIdAndToken = "SELECT TokenID, Token, Type, UserID, DateCreated FROM SecurityToken_Backlist WHERE UserID = ? AND Token = ?";
	String findSecurityTokenBlacklistByUserIdAndType = "SELECT TokenID, Token, Type, UserID, DateCreated FROM SecurityToken_Backlist WHERE UserID = ? AND Type = ?";
	String saveSecurityTokenBlacklist = "INSERT INTO SecurityToken_Backlist (Token, Type, UserID, DateCreated) VALUES (?, ?, ?, ?)";
	String saveUserLoginHistory = "INSERT INTO User_Login_History (UserID, LoginDateTime, DateCreated, MessageID, UserName, UserPassword) VALUES (?, ?, ?, ?, ?, ?)";
	String findAllTransactionTypes = "SELECT TransactionTypeID, TransactionType, Description, DateCreated, DatedModified, ModifiedBy FROM TransactionType_Lookup";
	String findTransactionTypeByTransactionId = "SELECT TransactionTypeID, TransactionType, Description, DateCreated, DatedModified, ModifiedBy FROM TransactionType_Lookup WHERE TransactionTypeId = ?";
	String findTransactionTypeByTransactionType = "SELECT TransactionTypeID, TransactionType, Description, DateCreated, DatedModified, ModifiedBy FROM TransactionType_Lookup WHERE TransactionType = ?";
	String saveBasicBatchUpload = "INSERT INTO BatchUpload (BatchApplication,Name,FileName,DateUploaded,UpLoadedBy,ProcessStart,NumberOfTransactions) VALUES (?,?,?,?,?,?,?)";
	String findAllBatchUploads = "SELECT BatchUploadID,BatchApplication,DateCreated,Name,FileName,DateUploaded,UpLoadedBy,ProcessStart,ProcessEnd,NumberOfTransactions,NumberOfTransactionsProcessed,NumberOfApprovedTransactions,NumberOfDeclinedTransactions,NumberOfErrorTransactions,NumberOfRejected,DateModified FROM BatchUpload";
	String findOneBatchUpload = "SELECT BatchUploadID,BatchApplication,DateCreated,Name,FileName,DateUploaded,UpLoadedBy,ProcessStart,ProcessEnd,NumberOfTransactions,NumberOfTransactionsProcessed,NumberOfApprovedTransactions,NumberOfDeclinedTransactions,NumberOfErrorTransactions,NumberOfRejected,DateModified FROM BatchUpload WHERE BatchUploadID = ?";
	String findByDateUploadedAfter = "SELECT BatchUploadID,BatchApplication,DateCreated,Name,FileName,DateUploaded,UpLoadedBy,ProcessStart,ProcessEnd,NumberOfTransactions,NumberOfTransactionsProcessed,NumberOfApprovedTransactions,NumberOfDeclinedTransactions,NumberOfErrorTransactions,NumberOfRejected,DateModified FROM BatchUpload where DateUploaded > ?";
	String findAllBatchUploadsByOrderByDateUploadedDesc = "SELECT * FROM (SELECT *, (@row_number:=@row_number + 1) AS num FROM BatchUpload ORDER BY DateUploaded desc) AS RowConstrainedResult,(SELECT @row_number := 0) as r WHERE num >= ? AND num < ? ORDER BY num";
	String findCountBatchUpload = "SELECT COUNT(*) FROM BatchUpload";
	String findBatchUploadsByDateUploadedAfterOrderByDateUploadedDesc = "SELECT * FROM (SELECT *, (@row_number:=@row_number + 1) AS num FROM BatchUpload WHERE DateUploaded > ? ORDER BY DateUploaded desc) AS RowConstrainedResult,(SELECT @row_number := 0) as r WHERE num >= ? AND num < ? ORDER BY num";
	String findByLegalEntityAppId = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppID = ?";
	String findByLegalEntityAppName = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppName = ?";
	String findAllLegalEntityApps = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup";
	String findAllLegalEntityAppsByIds = "SELECT LegalEntityAppID, LegalEntityAppName, DateCreated, DatedModified, ModifiedBy, IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppID IN (:legalEntityAppIds)";
	String deleteLegalEntityApp = "DELETE FROM LegalEntityApp_Lookup WHERE LegalEntityAppID = ?";
	String saveLegalEntityApp = "INSERT INTO LegalEntityApp_Lookup (LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive) VALUES (?,?,?,?,?)";
	String updateLegalEntityApp = "UPDATE LegalEntityApp_Lookup SET LegalEntityAppName = ?,IsActive = ?, DatedModified = ?, ModifiedBy = ? WHERE LegalEntityAppID = ?";
	String findPaymentProcessorRemittanceByProcessorTransactionId = "SELECT PaymentProcessorRemittanceID, DateCreated, ReconciliationStatusID, ReconciliationDate, PaymentMethod, TransactionAmount, TransactionType, TransactionTime, AccountID, Application, ProcessorTransactionID, MerchantID, TransactionSource, FirstName, LastName, RemittanceCreationDate, PaymentProcessorID, ReProcessStatus, ETL_RUNID FROM PaymentProcessor_Remittance WHERE ProcessorTransactionID = ?";
	String findAllReconciliationStatuses = "SELECT ReconciliationStatusID, ReconciliationStatus, Description, DateCreated, DateModified, ModifiedBy FROM ReconciliationStatus_Lookup";
	String findReconciliationStatusByReconciliationStatusId = "SELECT ReconciliationStatusID, ReconciliationStatus, Description, DateCreated, DateModified, ModifiedBy FROM ReconciliationStatus_Lookup WHERE ReconciliationStatusID = ?";
	String findReconciliationStatusByReconciliationStatus = "SELECT ReconciliationStatusID, ReconciliationStatus, Description, DateCreated, DateModified, ModifiedBy FROM ReconciliationStatus_Lookup WHERE ReconciliationStatus = ?";
	String findAllSaleTransactions = "SELECT SaleTransactionID, FirstName, LastName, ProcessUser, TransactionType, Address1, Address2, City, State, PostalCode, Country, CardNumberFirst6Char, CardNumberLast4Char, CardType, ExpiryDate, Token, ChargeAmount, LegalEntityApp, AccountId, ApplicationTransactionID, MerchantID, Processor, Application, Origin, ProcessorTransactionID, TransactionDateTime, TestMode, ApprovalCode, Tokenized, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, PaymentProcessorRuleID, RulePaymentProcessorID, RuleCardType, RuleMaximumMonthlyAmount, RuleNoMaximumMonthlyAmountFlag, RulePriority, AccountPeriod, Desk, InvoiceNumber, UserDefinedField1, UserDefinedField2, UserDefinedField3, ReconciliationStatusID, ReconciliationDate, BatchUploadID, ETL_RUNID FROM Sale_Transaction";
	String findSaleTransactionByApplicationTransactionId = "SELECT SaleTransactionID, FirstName, LastName, ProcessUser, TransactionType, Address1, Address2, City, State, PostalCode, Country, CardNumberFirst6Char, CardNumberLast4Char, CardType, ExpiryDate, Token, ChargeAmount, LegalEntityApp, AccountId, ApplicationTransactionID, MerchantID, Processor, Application, Origin, ProcessorTransactionID, TransactionDateTime, TestMode, ApprovalCode, Tokenized, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, PaymentProcessorRuleID, RulePaymentProcessorID, RuleCardType, RuleMaximumMonthlyAmount, RuleNoMaximumMonthlyAmountFlag, RulePriority, AccountPeriod, Desk, InvoiceNumber, UserDefinedField1, UserDefinedField2, UserDefinedField3, ReconciliationStatusID, ReconciliationDate, BatchUploadID, ETL_RUNID FROM Sale_Transaction WHERE ApplicationTransactionID = ?";
	String findSaleTransactionByProcessorTransactionId = "SELECT SaleTransactionID, FirstName, LastName, ProcessUser, TransactionType, Address1, Address2, City, State, PostalCode, Country, CardNumberFirst6Char, CardNumberLast4Char, CardType, ExpiryDate, Token, ChargeAmount, LegalEntityApp, AccountId, ApplicationTransactionID, MerchantID, Processor, Application, Origin, ProcessorTransactionID, TransactionDateTime, TestMode, ApprovalCode, Tokenized, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, PaymentProcessorRuleID, RulePaymentProcessorID, RuleCardType, RuleMaximumMonthlyAmount, RuleNoMaximumMonthlyAmountFlag, RulePriority, AccountPeriod, Desk, InvoiceNumber, UserDefinedField1, UserDefinedField2, UserDefinedField3, ReconciliationStatusID, ReconciliationDate, BatchUploadID, ETL_RUNID FROM Sale_Transaction WHERE ProcessorTransactionID = ?";
	String findCountByPaymentProcessorRuleId = "SELECT COUNT(*) FROM Sale_Transaction WHERE PaymentProcessorRuleID = ?";
	String findSaleTransactionByBatchUploadId = "SELECT SaleTransactionID, FirstName, LastName, ProcessUser, TransactionType, Address1, Address2, City, State, PostalCode, Country, CardNumberFirst6Char, CardNumberLast4Char, CardType, ExpiryDate, Token, ChargeAmount, LegalEntityApp, AccountId, ApplicationTransactionID, MerchantID, Processor, Application, Origin, ProcessorTransactionID, TransactionDateTime, TestMode, ApprovalCode, Tokenized, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, PaymentProcessorRuleID, RulePaymentProcessorID, RuleCardType, RuleMaximumMonthlyAmount, RuleNoMaximumMonthlyAmountFlag, RulePriority, AccountPeriod, Desk, InvoiceNumber, UserDefinedField1, UserDefinedField2, UserDefinedField3, ReconciliationStatusID, ReconciliationDate, BatchUploadID, ETL_RUNID FROM Sale_Transaction WHERE BatchUploadID = ?";
	String findAllRefundTransactions = "SELECT RefundTransactionID, SaleTransactionID, ApprovalCode, Processor, RefundAmount, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, ReconciliationStatusID, ReconciliationDate, ETL_RUNID FROM Refund_Transaction";
	String findRefundTransactionByApplicationTransactionId = "SELECT RefundTransactionID, SaleTransactionID, ApprovalCode, Processor, RefundAmount, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, ReconciliationStatusID, ReconciliationDate, ETL_RUNID FROM Refund_Transaction WHERE ApplicationTransactionID = ?";
	String findRefundTransactionByProcessorTransactionId = "SELECT RefundTransactionID, SaleTransactionID, ApprovalCode, Processor, RefundAmount, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, ReconciliationStatusID, ReconciliationDate, ETL_RUNID FROM Refund_Transaction WHERE ProcessorTransactionID = ?";
	String findAllVoidTransactions = "SELECT VoidTransactionID, SaleTransactionID, ApprovalCode, Processor, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated FROM Void_Transaction";
	String findVoidTransactionByApplicationTransactionId = "SELECT VoidTransactionID, SaleTransactionID, ApprovalCode, Processor, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated FROM Void_Transaction WHERE ApplicationTransactionID = ?";
	String findVoidTransactionByProcessorTransactionId = "SELECT VoidTransactionID, SaleTransactionID, ApprovalCode, Processor, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated FROM Void_Transaction WHERE ProcessorTransactionID = ?";
	String findAllUsers = "SELECT  ul.UserID, UserName, FirstName, LastName, ul.IsActive, LastLogin, ul.DateCreated, DateUpdated, Email, UserPassword, ul.DateModified, ul.ModifiedBy, Status  FROM User_Lookup ul";
	String findUserByUserId = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE UserID = ?";
	String findUserByUsername = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE UserName = ?";
	String findUserByEmail = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE Email = ?";
	String saveUser = "INSERT INTO User_Lookup (UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	String updateUser = "UPDATE User_Lookup SET UserName = ?, FirstName = ?, LastName = ?, IsActive = ?, LastLogin = ?, DateCreated = ?, DateUpdated = ?, Email = ?, UserPassword = ?, DateModified = ?, ModifiedBy = ?, Status = ? WHERE UserID = ?";
	String deleteUserByUsername = "DELETE FROM User_Lookup WHERE UserName = ?";
	String findUserLegalEntityAppByUserId = "SELECT UserLegalEntityAppID, UserID, LegalEntityAppID, DateCreated, DatedModified, ModifiedBy FROM User_LegalEntityApp WHERE UserID = ?";
	String findUserRoleByUserId = "SELECT UserRoleID, UserID, RoleID, DateCreated, DatedModified, ModifiedBy FROM User_Role WHERE UserID = ?";
	String findUserRoleByRoleId = "SELECT UserRoleID, UserID, RoleID, DateCreated, DatedModified, ModifiedBy FROM User_Role WHERE RoleID = ?";
	String saveUserRole = "INSERT INTO User_Role (UserID,RoleID,DateCreated) VALUES(?,?,?)";
	String saveUserLegalEntityApp = "INSERT INTO User_LegalEntityApp (UserID,LegalEntityAppID,DateCreated) VALUES(?,?,?)";
	String remittanceSaleTransaction = new StringBuilder().append("SELECT * ").append("FROM ")
			.append("(SELECT ppr.PaymentProcessorRemittanceID,").append("ppr.DateCreated,")
			.append("ppr.ReconciliationStatusID,").append("ppr.ReconciliationDate,").append("ppr.PaymentMethod,")
			.append("ppr.TransactionAmount,").append("ppr.TransactionType,").append("ppr.TransactionTime,")
			.append("ppr.AccountID,").append("ppr.Application AS Application,").append("ppr.ProcessorTransactionID,")
			.append("ppr.MerchantID,").append("ppr.TransactionSource,").append("ppr.FirstName,").append("ppr.LastName,")
			.append("ppr.RemittanceCreationDate,").append("ppr.PaymentProcessorID,")
			.append("ppl.ProcessorName AS ProcessorName,").append("st.SaleTransactionID AS SaleTransactionID,")
			.append("st.FirstName AS SaleFirstName,").append("st.LastName AS SaleLastName,")
			.append("st.ProcessUser AS SaleProcessUser,").append("st.TransactionType AS SaleTransactionType,")
			.append("st.Address1 AS SaleAddress1,").append("st.Addrbhaiess2 AS SaleAddress2,")
			.append("st.City AS SaleCity,").append("st.State AS SaleState,").append("st.PostalCode AS SalePostalCode,")
			.append("st.Country AS SaleCountry,").append("st.CardNumberFirst6Char AS SaleCardNumberFirst6Char,")
			.append("st.CardNumberLast4Char AS SaleCardNumberLast4Char,").append("st.CardType AS SaleCardType,")
			.append("st.ExpiryDate AS SaleExpiryDate,").append("st.Token AS SaleToken,")
			.append("st.ChargeAmount AS SaleChargeAmount,").append("st.LegalEntityApp AS SaleLegalEntityApp,")
			.append("st.AccountId AS SaleAccountId,")
			.append("st.ApplicationTransactionID AS SaleApplicationTransactionID,")
			.append("st.MerchantID AS SaleMerchantID,").append("st.Processor AS SaleProcessor,")
			.append("st.Application AS SaleApplication,").append("st.Origin AS SaleOrigin,")
			.append("st.ProcessorTransactionID AS SaleProcessorTransactionID,")
			.append("st.TransactionDateTime AS SaleTransactionDateTime,").append("st.TestMode AS SaleTestMode,")
			.append("st.ApprovalCode AS SaleApprovalCode,").append("st.Tokenized AS SaleTokenized,")
			.append("st.PaymentProcessorStatusCode AS SalePaymentProcessorStatusCode,")
			.append("st.PaymentProcessorStatusCodeDescription AS SalePaymentProcessorStatusCodeDescription,")
			.append("st.PaymentProcessorResponseCode AS SalePaymentProcessorResponseCode,")
			.append("st.PaymentProcessorResponseCodeDescription AS SalePaymentProcessorResponseCodeDescription,")
			.append("st.InternalStatusCode AS SaleInternalStatusCode,")
			.append("st.InternalStatusDescription AS SaleInternalStatusDescription,")
			.append("st.InternalResponseCode AS SaleInternalResponseCode,")
			.append("st.InternalResponseDescription AS SaleInternalResponseDescription,")
			.append("st.PaymentProcessorInternalStatusCodeID AS SalePaymentProcessorInternalStatusCodeID,")
			.append("st.PaymentProcessorInternalResponseCodeID AS SalePaymentProcessorInternalResponseCodeID,")
			.append("st.DateCreated AS SaleDateCreated,")
			.append("st.PaymentProcessorRuleID AS SalePaymentProcessorRuleID,")
			.append("st.RulePaymentProcessorID AS SaleRulePaymentProcessorID,")
			.append("st.RuleCardType AS SaleRuleCardType,")
			.append("st.RuleMaximumMonthlyAmount AS SaleRuleMaximumMonthlyAmount,")
			.append("st.RuleNoMaximumMonthlyAmountFlag AS SaleRuleNoMaximumMonthlyAmountFlag,")
			.append("st.RulePriority AS SaleRulePriority,").append("st.AccountPeriod AS SaleAccountPeriod,")
			.append("st.Desk AS SaleDesk,").append("st.InvoiceNumber AS SaleInvoiceNumber,")
			.append("st.UserDefinedField1 AS SaleUserDefinedField1,")
			.append("st.UserDefinedField2 AS SaleUserDefinedField2,")
			.append("st.UserDefinedField3 AS SaleUserDefinedField3,")
			.append("st.ReconciliationStatusID AS SaleReconciliationStatusID,")
			.append("st.ReconciliationDate AS SaleReconciliationDate,").append("st.BatchUploadID AS SaleBatchUploadID,")
			.append("0 AS SaleIsVoided,").append("0 AS SaleIsRefunded,").append("ppr.MerchantID AS MID,")
			.append("ppl.ProcessorName AS Processor_Name,")
			.append("ppr.ReconciliationStatusID AS ReconciliationStatus_ID ")
			.append("FROM PaymentProcessor_Remittance ppr ")
			.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ")
			.append("LEFT JOIN Sale_Transaction st ON (ppr.ProcessorTransactionID = st.ProcessorTransactionID) ")
			.append("WHERE ppr.RemittanceCreationDate >= ':remittanceCreationDateParam1' ")
			.append("AND ppr.RemittanceCreationDate <= ':remittanceCreationDateParam2' ")
			.append("AND (Upper(ppr.TransactionType) = 'SALE') ")
			.append("AND (st.TestMode = 1 or st.TestMode is null) ")
			.append("UNION SELECT ppr.PaymentProcessorRemittanceID,").append("ppr.DateCreated,")
			.append("ppr.ReconciliationStatusID,").append("ppr.ReconciliationDate,").append("ppr.PaymentMethod,")
			.append("ppr.TransactionAmount,").append("ppr.TransactionType,").append("ppr.TransactionTime,")
			.append("ppr.AccountID,").append("ppr.Application AS Application,").append("ppr.ProcessorTransactionID,")
			.append("ppr.MerchantID,").append("ppr.TransactionSource,").append("ppr.FirstName,").append("ppr.LastName,")
			.append("ppr.RemittanceCreationDate,").append("ppr.PaymentProcessorID,")
			.append("ppl.ProcessorName AS ProcessorName,").append("rt.SaleTransactionID AS SaleTransactionID,")
			.append("NULL AS SaleFirstName,").append("NULL AS SaleLastName,").append("NULL AS SaleProcessUser,")
			.append("'REFUND' AS SaleTransactionType,").append("NULL AS SaleAddress1,").append("NULL AS SaleAddress2,")
			.append("NULL AS SaleCity,").append("NULL AS SaleState,").append("NULL AS SalePostalCode,")
			.append("NULL AS SaleCountry,").append("NULL AS SaleCardNumberFirst6Char,")
			.append("st1.CardNumberLast4Char AS SaleCardNumberLast4Char,").append("st1.CardType AS SaleCardType,")
			.append("CAST(NULL AS DATETIME) AS SaleExpiryDate,").append("NULL AS SaleToken,")
			.append("st1.ChargeAmount AS SaleChargeAmount,").append("st1.LegalEntityApp AS SaleLegalEntityApp,")
			.append("st1.AccountId AS SaleAccountId,")
			.append("rt.ApplicationTransactionID AS SaleApplicationTransactionID,")
			.append("rt.MerchantID AS SaleMerchantID,").append("rt.Processor AS SaleProcessor,")
			.append("rt.Application AS SaleApplication,").append("NULL AS SaleOrigin,")
			.append("rt.ProcessorTransactionID AS SaleProcessorTransactionID,")
			.append("rt.TransactionDateTime AS SaleTransactionDateTime,").append("NULL AS SaleTestMode,")
			.append("rt.ApprovalCode AS SaleApprovalCode,").append("NULL AS SaleTokenized,")
			.append("rt.PaymentProcessorStatusCode AS SalePaymentProcessorStatusCode,")
			.append("rt.PaymentProcessorStatusCodeDescription AS SalePaymentProcessorStatusCodeDescription,")
			.append("rt.PaymentProcessorResponseCode AS SalePaymentProcessorResponseCode,")
			.append("rt.PaymentProcessorResponseCodeDescription AS SalePaymentProcessorResponseCodeDescription,")
			.append("rt.InternalStatusCode AS SaleInternalStatusCode,")
			.append("rt.InternalStatusDescription AS SaleInternalStatusDescription,")
			.append("rt.InternalResponseCode AS SaleInternalResponseCode,")
			.append("rt.InternalResponseDescription AS SaleInternalResponseDescription,")
			.append("rt.PaymentProcessorInternalStatusCodeID AS SalePaymentProcessorInternalStatusCodeID,")
			.append("rt.PaymentProcessorInternalResponseCodeID AS SalePaymentProcessorInternalResponseCodeID,")
			.append("rt.DateCreated AS SaleDateCreated,").append("NULL AS SalePaymentProcessorRuleID,")
			.append("NULL AS SaleRulePaymentProcessorID,").append("NULL AS SaleRuleCardType,")
			.append("NULL AS SaleRuleMaximumMonthlyAmount,").append("NULL AS SaleRuleNoMaximumMonthlyAmountFlag,")
			.append("NULL AS SaleRulePriority,").append("NULL AS SaleAccountPeriod,").append("NULL AS SaleDesk,")
			.append("NULL AS SaleInvoiceNumber,").append("NULL AS SaleUserDefinedField1,")
			.append("NULL AS SaleUserDefinedField2,").append("NULL AS SaleUserDefinedField3,")
			.append("rt.ReconciliationStatusID AS SaleReconciliationStatusID,")
			.append("rt.ReconciliationDate AS SaleReconciliationDate,").append("NULL AS SaleBatchUploadID,")
			.append("0 AS SaleIsVoided,").append("0 AS SaleIsRefunded,").append("ppr.MerchantID AS MID,")
			.append("ppl.ProcessorName AS Processor_Name,")
			.append("ppr.ReconciliationStatusID AS ReconciliationStatus_ID ")
			.append("FROM PaymentProcessor_Remittance ppr ")
			.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ")
			.append("LEFT JOIN Refund_Transaction rt ON (ppr.ProcessorTransactionID = rt.ProcessorTransactionID) ")
			.append("LEFT JOIN Sale_Transaction st1 ON (rt.SaleTransactionId = st1.SaleTransactionId) ")
			.append("WHERE ppr.RemittanceCreationDate >= ':remittanceCreationDateParam1' ")
			.append("AND ppr.RemittanceCreationDate <= ':remittanceCreationDateParam2' ")
			.append("AND (Upper(ppr.TransactionType) = 'REFUND') ")
			.append("AND (st1.TestMode = 1 or st1.TestMode is null) ")
			.append("UNION SELECT NULL AS PaymentProcessorRemittanceID,").append("NULL AS DateCreated,")
			.append("NULL AS ReconciliationStatusID,").append("NULL AS ReconciliationDate,")
			.append("NULL AS PaymentMethod,").append("NULL AS TransactionAmount,").append("NULL AS TransactionType,")
			.append("NULL AS TransactionTime,").append("NULL AS AccountID,").append("NULL AS Application,")
			.append("NULL AS ProcessorTransactionID,").append("NULL AS MerchantID,")
			.append("NULL AS TransactionSource,").append("NULL AS FirstName,").append("NULL AS LastName,")
			.append("NULL AS RemittanceCreationDate,").append("NULL AS PaymentProcessorID,")
			.append("NULL AS ProcessorName,").append("SALE.SaleTransactionID,").append("SALE.FirstName,")
			.append("SALE.LastName,").append("SALE.ProcessUser,").append("SALE.TransactionType,")
			.append("SALE.Address1,").append("SALE.Address2,").append("SALE.City,").append("SALE.State,")
			.append("SALE.PostalCode,").append("SALE.Country,").append("SALE.CardNumberFirst6Char,")
			.append("SALE.CardNumberLast4Char,").append("SALE.CardType,").append("SALE.ExpiryDate,")
			.append("SALE.Token,").append("SALE.ChargeAmount,").append("SALE.LegalEntityApp,").append("SALE.AccountId,")
			.append("SALE.ApplicationTransactionID,").append("SALE.MerchantID,")
			.append("SALE.Processor AS SaleProcessor,").append("SALE.Application AS Application,")
			.append("SALE.Origin,").append("SALE.ProcessorTransactionID,").append("SALE.TransactionDateTime,")
			.append("SALE.TestMode,").append("SALE.ApprovalCode,").append("SALE.Tokenized,")
			.append("SALE.PaymentProcessorStatusCode,").append("SALE.PaymentProcessorStatusCodeDescription,")
			.append("SALE.PaymentProcessorResponseCode,").append("SALE.PaymentProcessorResponseCodeDescription,")
			.append("SALE.InternalStatusCode,").append("SALE.InternalStatusDescription,")
			.append("SALE.InternalResponseCode,").append("SALE.InternalResponseDescription,")
			.append("SALE.PaymentProcessorInternalStatusCodeID,").append("SALE.PaymentProcessorInternalResponseCodeID,")
			.append("SALE.DateCreated,").append("SALE.PaymentProcessorRuleID,").append("SALE.RulePaymentProcessorID,")
			.append("SALE.RuleCardType,").append("SALE.RuleMaximumMonthlyAmount,")
			.append("SALE.RuleNoMaximumMonthlyAmountFlag,").append("SALE.RulePriority,").append("SALE.AccountPeriod,")
			.append("SALE.Desk,").append("SALE.InvoiceNumber,").append("SALE.UserDefinedField1,")
			.append("SALE.UserDefinedField2,").append("SALE.UserDefinedField3,").append("SALE.ReconciliationStatusID,")
			.append("SALE.ReconciliationDate,").append("SALE.BatchUploadID,").append("0 AS SaleIsVoided,")
			.append("0 AS SaleIsRefunded,").append("SALE.MerchantID AS MID,")
			.append("SALE.Processor AS Processor_Name,")
			.append("SALE.ReconciliationStatusID AS ReconciliationStatus_ID ").append("FROM Sale_Transaction SALE ")
			.append("JOIN PaymentProcessor_Lookup ppl ON (SALE.Processor = ppl.ProcessorName) ")
			.append("WHERE SALE.TransactionDateTime >= DATE_ADD(CAST(':remittanceCreationDateParam1' AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -2 DAY) ")
			.append("AND SALE.TransactionDateTime <= DATE_ADD(CAST(':remittanceCreationDateParam1' AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -1 DAY) ")
			.append("AND SALE.InternalStatusCode = 1 ").append("AND (Upper(SALE.TransactionType) = 'SALE') ")
			.append("AND SALE.ReconciliationStatusID = 3 ").append("UNION SELECT NULL AS PaymentProcessorRemittanceID,")
			.append("NULL AS DateCreated,").append("NULL AS ReconciliationStatusID,")
			.append("NULL AS ReconciliationDate,").append("NULL AS PaymentMethod,").append("NULL AS TransactionAmount,")
			.append("NULL AS TransactionType,").append("NULL AS TransactionTime,").append("NULL AS AccountID,")
			.append("NULL AS Application,").append("NULL AS ProcessorTransactionID,").append("NULL AS MerchantID,")
			.append("NULL AS TransactionSource,").append("NULL AS FirstName,").append("NULL AS LastName,")
			.append("NULL AS RemittanceCreationDate,").append("NULL AS PaymentProcessorID,")
			.append("NULL AS ProcessorName,").append("REFUND.SaleTransactionID,").append("NULL AS RefundFirstName,")
			.append("NULL AS RefundLastName,").append("NULL AS RefundProcessUser,")
			.append("'REFUND' AS RefundTransactionType,").append("NULL AS RefundAddress1,")
			.append("NULL AS RefundAddress2,").append("NULL AS RefundCity,").append("NULL AS RefundState,")
			.append("NULL AS RefundPostalCode,").append("NULL AS RefundCountry,")
			.append("NULL AS RefundCardNumberFirst6Char,")
			.append("st2.CardNumberLast4Char AS RefundCardNumberLast4Char,").append("st2.CardType AS RefundCardType,")
			.append("NULL AS RefundExpiryDate,").append("NULL AS RefundToken,")
			.append("st2.ChargeAmount AS RefundChargeAmount,").append("st2.LegalEntityApp AS RefundLegalEntityApp,")
			.append("st2.AccountId AS RefundAccountId,").append("REFUND.ApplicationTransactionID,")
			.append("REFUND.MerchantID,").append("REFUND.Processor AS SaleProcessor,")
			.append("REFUND.Application AS Application,").append("NULL AS RefundOrigin,")
			.append("REFUND.ProcessorTransactionID,").append("REFUND.TransactionDateTime,")
			.append("NULL AS RefundTestMode,").append("REFUND.ApprovalCode,").append("NULL AS RefundTokenized,")
			.append("REFUND.PaymentProcessorStatusCode,").append("REFUND.PaymentProcessorStatusCodeDescription,")
			.append("REFUND.PaymentProcessorResponseCode,").append("REFUND.PaymentProcessorResponseCodeDescription,")
			.append("REFUND.InternalStatusCode,").append("REFUND.InternalStatusDescription,")
			.append("REFUND.InternalResponseCode,").append("REFUND.InternalResponseDescription,")
			.append("REFUND.PaymentProcessorInternalStatusCodeID,")
			.append("REFUND.PaymentProcessorInternalResponseCodeID,").append("REFUND.DateCreated,")
			.append("NULL AS RefundPaymentProcessorRuleID,").append("NULL AS RefundRulePaymentProcessorID,")
			.append("NULL AS RefundRuleCardType,").append("NULL AS RefundRuleMaximumMonthlyAmount,")
			.append("NULL AS RefundRuleNoMaximumMonthlyAmountFlag,").append("NULL AS RefundRulePriority,")
			.append("NULL AS RefundAccountPeriod,").append("NULL AS RefundDesk,").append("NULL AS RefundInvoiceNumber,")
			.append("NULL AS RefundUserDefinedField1,").append("NULL AS RefundUserDefinedField2,")
			.append("NULL AS RefundUserDefinedField3,").append("REFUND.ReconciliationStatusID,")
			.append("REFUND.ReconciliationDate,").append("NULL AS RefundBatchUploadID,").append("0 AS REFUNDIsVoided,")
			.append("0 AS REFUNDIsRefunded,").append("REFUND.MerchantID AS MID,")
			.append("REFUND.Processor AS Processor_Name,")
			.append("REFUND.ReconciliationStatusID AS ReconciliationStatus_ID ")
			.append("FROM Refund_Transaction REFUND ")
			.append("JOIN Sale_Transaction st2 ON (REFUND.SaleTransactionId = st2.SaleTransactionId) ")
			.append("JOIN PaymentProcessor_Lookup ppl ON (REFUND.Processor = ppl.ProcessorName) ")
			.append("WHERE REFUND.TransactionDateTime >= DATE_ADD(CAST(':remittanceCreationDateParam1' AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -2 DAY) ")
			.append("AND REFUND.TransactionDateTime <= DATE_ADD(CAST(':remittanceCreationDateParam1' AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -1 DAY) ")
			.append("AND REFUND.InternalStatusCode = 1 ").append("AND REFUND.ReconciliationStatusID = 3 ) ReconDate ")
			.append("WHERE ReconDate.Processor_Name = ':processorNameParam' ")
			.append("AND (ReconDate.MID IN (':merchantIdParam')) ")
			.append("AND ReconDate.ReconciliationStatus_ID = :reconciliationStatusIdParam ")
			.append("ORDER BY Processor_Name ASC, ").append("MID ASC, ").append("ReconciliationStatus_ID ASC")
			.toString();
	String findPaymentProcessorById = "SELECT PaymentProcessorID, ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime FROM PaymentProcessor_Lookup WHERE PaymentProcessorID = ?";
	String findPaymentProcessorByName = "SELECT PaymentProcessorID, ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime FROM PaymentProcessor_Lookup WHERE ProcessorName = ?";
	String findPaymentProcessorRuleById= "SELECT PaymentProcessorRuleID, PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, CurrentYear,CurrentMonth,DateCreated,ModifiedBy FROM PaymentProcessor_Rule WHERE PaymentProcessorID = ?";
	String findPaymentProcessorMerchantsById= "SELECT PaymentProcessorMerchantID, LegalEntityAppID, PaymentProcessorID, TestOrProd, MerchantID, DateCreated, DatedModified, ModifiedBy FROM PaymentProcessor_Merchant WHERE PaymentProcessorID = ?";
	String findAllPaymentProcessors = "SELECT PaymentProcessorID, ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime FROM PaymentProcessor_Lookup";	
	String findPaymentProcessorResponseCodeByCodeId = "SELECT PaymentProcessorResponseCodeID, PaymentProcessorID, PaymentProcessorResponseCode,TransactionType,PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorResponseCode_Lookup WHERE TransactionType=? AND PaymentProcessorResponseCode=? AND PaymentProcessorID=?";
	String findPaymentProcessorResponseCodeByTypeId = "SELECT PaymentProcessorResponseCodeID, PaymentProcessorID, PaymentProcessorResponseCode,TransactionType,PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorResponseCode_Lookup WHERE TransactionType=? AND PaymentProcessorID=?";
	String findPaymentProcessorStatusCodeByCodeId = "SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE TransactionType=? AND PaymentProcessorStatusCode=? AND PaymentProcessorID=?";
	String findPaymentProcessorStatusCodeByTypeId = "SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE TransactionType=? AND PaymentProcessorID=?";
	String findPaymentProcessorStatusCodeById = "SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE PaymentProcessorStatusCodeID =?";
	String findPaymentProcessorSatusCodesByPPId = "SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE PaymentProcessorID =?";
	String findAllPaymentProcessorsByIds = "SELECT PaymentProcessorID, ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime FROM PaymentProcessor_Lookup WHERE PaymentProcessorID IN (:paymentProcessorIds)";
	String savePaymentProcessors = "INSERT INTO PaymentProcessor_Lookup (ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime) VALUES (?, ?, ?, ?, ?, ?, ?)";	
	String savePaymentProcessorRules = "INSERT INTO PaymentProcessor_Rule ( PaymentProcessorID, DateCreated,ModifiedBy) VALUES (?,?,?)";
	String deletePaymentProcessorByID = "DELETE FROM PaymentProcessor_Lookup WHERE PaymentProcessorID = ?";
	String deletePaymentProcessorMerchantByProcId = "DELETE FROM PaymentProcessor_Merchant WHERE PaymentProcessorID = ?";
	String deletePaymentProcessorRules = "DELETE FROM PaymentProcessor_Rule WHERE PaymentProcessorID =?";
	String deletePaymentProcessorMerchants = "DELETE FROM PaymentProcessor_Merchant WHERE PaymentProcessorID =?";
	String deletePaymentProcessorStatusCodeByID = "DELETE FROM PaymentProcessorStatusCode_Lookup WHERE PaymentProcessorID =?";
	String deletePaymentProcessorResponseCodeByID = "DELETE FROM PaymentProcessorResponseCode_Lookup WHERE PaymentProcessorID =?";
	String deletePaymentProcessorStatusCodes = "DELETE FROM PaymentProcessorStatusCode_Lookup WHERE PaymentProcessorStatusCodeID IN (:paymentProcessorStatusCodes)";
	String savePaymentProcessorStatusCode = "INSERT INTO PaymentProcessorStatusCode_Lookup (PaymentProcessorID,PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy) VALUES (?,?,?,?,?,?,?)"; 
	String updatePaymentProcessorStatusCode = "UPDATE PaymentProcessorStatusCode_Lookup SET PaymentProcessorID = ?, PaymentProcessorStatusCode =?, TransactionType=?,PaymentProcessorStatusDescription=?, DatedModified=?, ModifiedBy=? WHERE PaymentProcessorStatusCodeID = ?";
	String deletePaymentProcessorInternalStatusCode = "DELETE FROM PaymentProcessor_InternalStatusCode where InternalStatusCodeId = ?";
	String deleteInternalStatusCodes = "DELETE FROM InternalStatusCode_Lookup where InternalStatusCodeID IN (:ids)";
	String deletePaymentProcessorInternalStatusCodes = "DELETE FROM PaymentProcessor_InternalStatusCode where PaymentProcessorInternalStatusCodeID IN (:ids)";
	String findPaymentProcessorStatusCodeIds = "SELECT PaymentProcessorStatusCodeID from PaymentProcessor_InternalStatusCode where InternalStatusCodeID = ?";
	String deletePaymentProcessorStatusCodeIds = "DELETE FROM PaymentProcessorStatusCode_Lookup where PaymentProcessorStatusCodeID IN (:ids)";
	String deleteInternalStatusCode = "DELETE FROM InternalStatusCode_Lookup where InternalStatusCodeID = ?";
	String findAllInternalStatusCode = "SELECT InternalStatusCodeID,InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,DatedModified,TransactionType,DateCreated FROM InternalStatusCode_Lookup";
	String findAllPaymentProcessorInternalStatusCodeForInternalStatusCodeId = "SELECT PaymentProcessorInternalStatusCodeID,InternalStatusCodeID,PaymentProcessorStatusCodeID,DateCreated,ModifiedBy FROM PaymentProcessor_InternalStatusCode where InternalStatusCodeID = ?";
	String findInternalStatusCodeById = "SELECT InternalStatusCodeID,InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,DatedModified,TransactionType,DateCreated FROM InternalStatusCode_Lookup WHERE InternalStatusCodeID = ?";
	String saveInternalStatusCode = "INSERT INTO InternalStatusCode_Lookup (InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,TransactionType,DateCreated,DatedModified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	String updateInternalStatusCode = "UPDATE InternalStatusCode_Lookup SET InternalStatusCode = ?, InternalStatusCodeDescription = ?, ModifiedBy = ? , InternalStatusCategoryAbbr = ? , InternalStatusCategory = ?, TransactionType = ?, DatedModified = ? WHERE InternalStatusCodeID = ?";
	String savePaymentProcessorInternalStatusCode = "INSERT INTO PaymentProcessor_InternalStatusCode (InternalStatusCodeId,PaymentProcessorStatusCodeId,DateCreated,ModifiedBy) VALUES (?, ?, ?, ?)";
	String findByInternalStatusCodeAndTransactionType = "SELECT InternalStatusCodeID,InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,DatedModified,TransactionType,DateCreated FROM InternalStatusCode_Lookup WHERE InternalStatusCode = ? AND TransactionType = ?";
	String findAllInternalStatusCodeByTransactionType = "SELECT InternalStatusCodeID,InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,DatedModified,TransactionType,DateCreated FROM InternalStatusCode_Lookup WHERE TransactionType = ?";
	String findPaymentProcessorRuleByCardType = "SELECT PaymentProcessorRuleID, PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, CurrentYear, CurrentMonth, DateCreated, ModifiedBy FROM PaymentProcessor_Rule WHERE CardType = ?";
	String savePaymentProcessorRule = "INSERT INTO PaymentProcessor_Rule (PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, DateCreated,ModifiedBy) VALUES (?,?,?,?,?,?,?,?)";
	String findPaymentProcessorRuleByID = "SELECT PaymentProcessorRuleID, PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, CurrentYear, CurrentMonth, DateCreated, ModifiedBy FROM PaymentProcessor_Rule WHERE PaymentProcessorRuleID = ?";
	String deletePaymentProcessorRuleByID = "DELETE FROM PaymentProcessor_Rule WHERE PaymentProcessorRuleID = ?";
	String findPaymentProcessorRulesByPaymentProcessorID = "SELECT PaymentProcessorRuleID, PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, CurrentYear, CurrentMonth, DateCreated, ModifiedBy FROM PaymentProcessor_Rule WHERE PaymentProcessorID = ?";
	String updatePaymentProcessorRule = "UPDATE PaymentProcessor_Rule SET PaymentProcessorID= ?, CardType= ?,MaximumMonthlyAmount=?,NoMaximumMonthlyAmountFlag=?,Priority=? WHERE PaymentProcessorRuleID= ?";

	//Below queries has been used for InternalResponseCode/PaymentProcessorInternalResponseCode
	String findByInternalResponseCodeAndTransactionTypeName ="SELECT * FROM InternalResponseCode_Lookup WHERE InternalResponseCode = ? AND TransactionType = ?";
	String findAllInternalResponseCode = "SELECT InternalResponseCodeID,InternalResponseCode,InternalResponseCodeDescription,ModifiedBy,DatedModified,TransactionType,DateCreated FROM InternalResponseCode_Lookup";
	String findAllInternalResponseCodeByTransactionType = "SELECT InternalResponseCodeID,InternalResponseCode,InternalResponseCodeDescription,ModifiedBy,DatedModified,TransactionType,DateCreated FROM InternalResponseCode_Lookup WHERE TransactionType = ?";
	String saveInternalResponseCode = "INSERT INTO InternalResponseCode_Lookup (InternalResponseCode, InternalResponseCodeDescription, ModifiedBy, DatedModified, TransactionType,DateCreated) VALUES (?, ?, ?, ?, ?, ?)";
	String findOneInternalResponseCode = "SELECT * FROM InternalResponseCode_Lookup WHERE InternalResponseCodeID = ?";
	String deleteInternalResponseCode = "DELETE FROM InternalResponseCode_Lookup WHERE InternalResponseCodeID = ?";
	String updateInternalResponseCode = "UPDATE InternalResponseCode_Lookup SET InternalResponseCode = ?, InternalResponseCodeDescription = ?, ModifiedBy = ?, TransactionType = ?, DatedModified = ? WHERE InternalResponseCodeID = ?";
	String savePaymentProcessorResponseCode = "INSERT INTO PaymentProcessorResponseCode_Lookup (PaymentProcessorID,PaymentProcessorResponseCode,TransactionType,PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy) VALUES (?,?,?,?,?,?,?)";
	String findPaymentProcessorResponseCodeByID = "SELECT PaymentProcessorResponseCodeID, PaymentProcessorID, PaymentProcessorResponseCode,TransactionType,PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorResponseCode_Lookup WHERE PaymentProcessorResponseCodeID=?";
	String updatePaymentProcessorResponseCode = "UPDATE PaymentProcessorResponseCode_Lookup SET PaymentProcessorID = ?, PaymentProcessorResponseCode =?, TransactionType=?,PaymentProcessorResponseCodeDescription=?, DatedModified=?, ModifiedBy=? WHERE PaymentProcessorResponseCodeID = ?";
	String savePaymentProcessorInternalResponseCode = "INSERT INTO PaymentProcessor_InternalResponseCode ( PaymentProcessorResponseCodeID,InternalResponseCodeID,DateCreated) VALUES(?,?,?)";
	String findOnePaymentProcessorInternalResponseCode = "SELECT * FROM PaymentProcessor_InternalResponseCode WHERE PaymentProcessorResponseCodeID = ?";
	String findAllPaymentProcessorInternalResponseCode = "SELECT PaymentProcessorInternalResponseCodeID,PaymentProcessorResponseCodeID,InternalResponseCodeID,DateCreated,ModifiedBy FROM PaymentProcessor_InternalResponseCode WHERE InternalResponseCodeID=?";
	String deletePaymentProcessorPaymentProcessorResponseCodeId = "DELETE FROM PaymentProcessor_InternalResponseCode WHERE PaymentProcessorResponseCodeID = ?";
	String paymentProcessorInternalResponseCodeId = "SELECT * FROM PaymentProcessor_InternalResponseCode WHERE InternalResponseCodeID = ?";
	String deletePaymentProcessorInternalResponseCode = "DELETE FROM PaymentProcessor_InternalResponseCode where InternalResponseCodeId = ?";
	String findPaymentProcessorInternalResponseCodeIdsByInternalResponseCode = "SELECT PaymentProcessorInternalResponseCodeID FROM PaymentProcessor_InternalResponseCode WHERE InternalResponseCodeID=?";
	String deletePaymentProcessorResponseCodeIds = "DELETE FROM PaymentProcessor_InternalResponseCode where PaymentProcessorInternalResponseCodeID IN (:ids)";
	String savePaymentProcessorMarchent = "INSERT INTO PaymentProcessor_Merchant (paymentProcessorId,TestOrProd,MerchantID,DateCreated,DatedModified,LegalEntityAppID) values (?, ?, ?, ?, ?, ?)";
	String fetchInternalStatusCodeUsedForPaymentProcessor = " select InternalStatusCodeId,PaymentProcessorInternalStatusCodeID from PaymentProcessor_InternalStatusCode where PaymentProcessorStatusCodeID in (  select PaymentProcessorStatusCodeID from PaymentProcessorStatusCode_Lookup where PaymentProcessorID = ? ) ";

	String deletePaymentProcessorInternalResponseCodes = "DELETE FROM PaymentProcessor_InternalResponseCode where PaymentProcessorInternalResponseCodeID IN (:ids)";
	String deleteInternalResponseCodes = "DELETE FROM InternalResponseCode_Lookup where InternalResponseCodeID IN (:ids)";
	String findAllProcessorRules = "SELECT * FROM PaymentProcessor_Rule";
	
	String deleteUserRoles = "DELETE FROM User_Role where UserRoleID IN (:userRoleIds)";
	String deleteUserLegalEntities = "DELETE FROM User_LegalEntityApp WHERE UserLegalEntityAppID IN (:userLegalEntityAppIds)";
	String findLegalEntitiesAssociatedWithUserByLEId  = "SELECT UserLegalEntityAppID FROM User_LegalEntityApp WHERE LegalEntityAppID=?";
	String findLegalEntitiesAssociatedWithUserByUserId  = "select count(*) from User_LegalEntityApp where UserID=? and LegalEntityAppID not in(:)";
	String updatePaymentProcessor = "UPDATE PaymentProcessor_Lookup SET ProcessorName=?,IsActive=?,RemitTransactionOpenTime=?,RemitTransactionCloseTime=?,DatedModified=? WHERE PaymentProcessorID=?";
	String findCountUserLookup = "SELECT COUNT(*) From User_Lookup";
	String findAllSaleTransactions_Count = "SELECT COUNT(SaleTransactionID) FROM Sale_Transaction";
}