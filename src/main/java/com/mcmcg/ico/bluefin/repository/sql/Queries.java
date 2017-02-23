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
	String findAllBatchUploadsByOrderByDateUploadedDesc = "SELECT * FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY DateUploaded DESC ) AS RowNum, * FROM BatchUpload) AS RowConstrainedResult WHERE RowNum >= ? AND RowNum < ? ORDER BY RowNum";
	String findCountBatchUpload = "SELECT COUNT(*) FROM BatchUpload";
	String findBatchUploadsByDateUploadedAfterOrderByDateUploadedDesc = "SELECT * FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY DateUploaded DESC ) AS RowNum, * FROM BatchUpload WHERE DateUploaded > ?) AS RowConstrainedResult WHERE RowNum >= ? AND RowNum < ? ORDER BY RowNum";
	String findByLegalEntityAppId = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppID = ?";
	String findByLegalEntityAppName = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppName = ?";
	String findAllLegalEntityApps = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup";
	String findAllLegalEntityAppsByIds = "SELECT LegalEntityAppID, LegalEntityAppName, DateCreated, DatedModified, ModifiedBy, IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppID IN (:legalEntityAppIds)";
	String deleteLegalEntityApp = "DELETE FROM LegalEntityApp_Lookup WHERE LegalEntityAppID = ?";
	String saveLegalEntityApp = "INSERT INTO LegalEntityApp_Lookup (LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive) VALUES (?,?,?,?,?)";
	String updateLegalEntityApp = "UPDATE LegalEntityApp_Lookup SET LegalEntityAppName = ?, ModifiedBy = ? WHERE LegalEntityAppID = ?";
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
	String findAllUsers = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup";
	String findUserByUserId = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE UserID = ?";
	String findUserByUsername = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE UserName = ?";
	String findUserByEmail = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE Email = ?";
	String saveUser = "INSERT INTO User_Lookup (UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	String updateUser = "UPDATE User_Lookup SET UserName = ?, FirstName = ?, LastName = ?, IsActive = ?, LastLogin = ?, DateCreated = ?, DateUpdated = ?, Email = ?, UserPassword = ?, DateModified = ?, ModifiedBy = ?, Status = ? WHERE UserID = ?";
	String deleteUserByUsername = "DELETE FROM User_Lookup WHERE UserName = ?";
	String findUserLegalEntityAppByUserId = "SELECT UserLegalEntityAppID, UserID, LegalEntityAppID, DateCreated, DatedModified, ModifiedBy FROM User_LegalEntityApp WHERE UserID = ?";
	String findUserRoleByUserId = "SELECT UserRoleID, UserID, RoleID, DateCreated, DatedModified, ModifiedBy FROM User_Role WHERE UserID = ?";
	String findUserRoleByRoleId = "SELECT UserRoleID, UserID, RoleID, DateCreated, DatedModified, ModifiedBy FROM User_Role WHERE RoleID = ?";
}
