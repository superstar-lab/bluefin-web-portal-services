package com.mcmcg.ico.bluefin.repository.sql;
/**
 * Change interface to class after recommendation of Sonar cube
 * @author ddhiman
 *
 */
public class Queries {
	public static final String FINDALLAPPLICATIONS = "SELECT ApplicationID, ApplicationName FROM Application_Lookup";
	public static final String FINDALLORIGINPAYMENTFREQUENCIES = "SELECT OriginPaymentFrequencyID, Origin, PaymentFrequency, DateCreated, DateModified, ModifiedBy FROM OriginPaymentFrequency_Lookup";
	public static final String FINDPERMISSIONBYPERMISSIONID = "SELECT PermissionID, PermissionName, Description, DateCreated, DatedModified, ModifiedBy FROM Permission_Lookup WHERE PermissionID = ?";
	public static final String FINDPERMISSIONBYPERMISSIONNAME = "SELECT PermissionID, PermissionName, Description, DateCreated, DatedModified, ModifiedBy FROM Permission_Lookup WHERE PermissionName = ?";
	public static final String SAVEPERMISSION = "INSERT INTO Permission_Lookup (PermissionName, Description, DateCreated, DatedModified, ModifiedBy) VALUES (?, ?, ?, ?, ?)";
	public static final String FINDALLROLES = "SELECT RoleID, RoleName, Description, DateCreated, DatedModified, ModifiedBy FROM Role_Lookup";
	public static final String FINDALLROLESBYIDS = "SELECT RoleID, RoleName, Description, DateCreated, DatedModified, ModifiedBy FROM Role_Lookup WHERE RoleID IN (:roleIds)";
	public static final String FINDROLEBYROLEID = "SELECT RoleID, RoleName, Description, DateCreated, DatedModified, ModifiedBy FROM Role_Lookup WHERE RoleID = ?";
	public static final String FINDROLEBYROLENAME = "SELECT RoleID, RoleName, Description, DateCreated, DatedModified, ModifiedBy FROM Role_Lookup WHERE RoleName = ?";
	public static final String SAVEROLE = "INSERT INTO Role_Lookup (RoleName, Description, DateCreated, DatedModified, ModifiedBy) VALUES (?, ?, ?, ?, ?)";
	public static final String DELETEROLEBYROLENAME = "DELETE FROM Role_Lookup WHERE RoleName = ?";
	public static final String FINDROLEPERMISSIONBYROLEPERMISSIONID = "SELECT RolePermissionID, RoleID, PermissionID, DateCreated, DatedModified, ModifiedBy FROM Role_Permission";
	public static final String FINDROLEPERMISSIONBYROLEID = "SELECT RolePermissionID, RoleID, PermissionID, DateCreated, DatedModified, ModifiedBy FROM Role_Permission WHERE RoleID = ?";
	public static final String SAVEROLEPERMISSION = "INSERT INTO Role_Permission (RoleID, PermissionID, DateCreated, DatedModified, ModifiedBy) VALUES (?, ?, ?, ?, ?)";
	public static final String FINDPROPERTYBYNAME = "SELECT ApplicationpropertyID, ApplicationPropertyName, ApplicationPropertyValue, DataType, Description, DateCreated, DateModified, ModifiedBy FROM ApplicationProperty_Lookup WHERE ApplicationPropertyName = ?";
	public static final String FINDSECURITYTOKENBLACKLISTBYTOKENID = "SELECT TokenID, Token, Type, UserID, DateCreated FROM SecurityToken_Backlist WHERE TokenID = ?";
	public static final String FINDSECURITYTOKENBLACKLISTBYTOKEN = "SELECT TokenID, Token, Type, UserID, DateCreated FROM SecurityToken_Backlist WHERE Token = ?";
	public static final String FINDSECURITYTOKENBLACKLISTBYUSERIDANDTOKEN = "SELECT TokenID, Token, Type, UserID, DateCreated FROM SecurityToken_Backlist WHERE UserID = ? AND Token = ?";
	public static final String FINDSECURITYTOKENBLACKLISTBYUSERIDANDTYPE = "SELECT TokenID, Token, Type, UserID, DateCreated FROM SecurityToken_Backlist WHERE UserID = ? AND Type = ?";
	public static final String SAVESECURITYTOKENBLACKLIST = "INSERT INTO SecurityToken_Backlist (Token, Type, UserID, DateCreated) VALUES (?, ?, ?, ?)";
	public static final String SAVEUSERLOGINHISTORY = "INSERT INTO User_Login_History (UserID, LoginDateTime, DateCreated, MessageID, UserName, UserPassword) VALUES (?, ?, ?, ?, ?, ?)";
	public static final String FINDALLTRANSACTIONTYPES = "SELECT TransactionTypeID, TransactionType, Description, DateCreated, DatedModified, ModifiedBy FROM TransactionType_Lookup";
	public static final String FINDTRANSACTIONTYPEBYTRANSACTIONID = "SELECT TransactionTypeID, TransactionType, Description, DateCreated, DatedModified, ModifiedBy FROM TransactionType_Lookup WHERE TransactionTypeId = ?";
	public static final String FINDTRANSACTIONTYPEBYTRANSACTIONTYPE = "SELECT TransactionTypeID, TransactionType, Description, DateCreated, DatedModified, ModifiedBy FROM TransactionType_Lookup WHERE TransactionType = ?";
	public static final String SAVEBASICBATCHUPLOAD = "INSERT INTO BatchUpload (BatchApplication,Name,FileName,DateUploaded,UpLoadedBy,ProcessStart,NumberOfTransactions) VALUES (?,?,?,?,?,?,?)";
	public static final String FINDALLBATCHUPLOADS = "SELECT BatchUploadID,BatchApplication,DateCreated,Name,FileName,DateUploaded,UpLoadedBy,ProcessStart,ProcessEnd,NumberOfTransactions,NumberOfTransactionsProcessed,NumberOfApprovedTransactions,NumberOfDeclinedTransactions,NumberOfErrorTransactions,NumberOfRejected,DateModified FROM BatchUpload";
	public static final String FINDONEBATCHUPLOAD = "SELECT BatchUploadID,BatchApplication,DateCreated,Name,FileName,DateUploaded,UpLoadedBy,ProcessStart,ProcessEnd,NumberOfTransactions,NumberOfTransactionsProcessed,NumberOfApprovedTransactions,NumberOfDeclinedTransactions,NumberOfErrorTransactions,NumberOfRejected,DateModified FROM BatchUpload WHERE BatchUploadID = ?";
	public static final String FINDBYDATEUPLOADEDAFTER = "SELECT BatchUploadID,BatchApplication,DateCreated,Name,FileName,DateUploaded,UpLoadedBy,ProcessStart,ProcessEnd,NumberOfTransactions,NumberOfTransactionsProcessed,NumberOfApprovedTransactions,NumberOfDeclinedTransactions,NumberOfErrorTransactions,NumberOfRejected,DateModified FROM BatchUpload where DateUploaded > ?";
	public static final String FINDALLBATCHUPLOADSBYORDERBYDATEUPLOADEDDESC = "SELECT * FROM (SELECT *, (@row_number:=@row_number + 1) AS num FROM BatchUpload, (SELECT @row_number := 0) as r ORDER BY DateUploaded desc) AS RowConstrainedResult WHERE num >= ? AND num < ? ORDER BY num asc";
	public static final String FINDCOUNTBATCHUPLOAD = "SELECT COUNT(*) FROM BatchUpload";
	public static final String FINDBATCHUPLOADSBYDATEUPLOADEDAFTERORDERBYDATEUPLOADEDDESC = "SELECT * FROM (SELECT *, (@row_number:=@row_number + 1) AS num FROM BatchUpload, (SELECT @row_number := 0) as r WHERE DateUploaded > ? ORDER BY DateUploaded desc) AS RowConstrainedResult WHERE num >= ? AND num < ? ORDER BY num asc";
	public static final String FINDBYLEGALENTITYAPPID = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppID = ?";
	public static final String FINDBYLEGALENTITYAPPNAME = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppName = ?";
	public static final String FINDALLLEGALENTITYAPPS = "SELECT LegalEntityAppID,LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive FROM LegalEntityApp_Lookup";
	public static final String FINDALLLEGALENTITYAPPSBYIDS = "SELECT LegalEntityAppID, LegalEntityAppName, DateCreated, DatedModified, ModifiedBy, IsActive FROM LegalEntityApp_Lookup WHERE LegalEntityAppID IN (:legalEntityAppIds)";
	public static final String DELETELEGALENTITYAPP = "DELETE FROM LegalEntityApp_Lookup WHERE LegalEntityAppID = ?";
	public static final String SAVELEGALENTITYAPP = "INSERT INTO LegalEntityApp_Lookup (LegalEntityAppName,DateCreated,DatedModified,ModifiedBy,IsActive) VALUES (?,?,?,?,?)";
	public static final String UPDATELEGALENTITYAPP = "UPDATE LegalEntityApp_Lookup SET LegalEntityAppName = ?,IsActive = ?, DatedModified = ?, ModifiedBy = ? WHERE LegalEntityAppID = ?";
	public static final String FINDPAYMENTPROCESSORREMITTANCEBYPROCESSORTRANSACTIONID = "SELECT PaymentProcessorRemittanceID, DateCreated, ReconciliationStatusID, ReconciliationDate, PaymentMethod, TransactionAmount, TransactionType, TransactionTime, AccountID, Application, ProcessorTransactionID, MerchantID, TransactionSource, FirstName, LastName, RemittanceCreationDate, PaymentProcessorID, ReProcessStatus, ETL_RUNID FROM PaymentProcessor_Remittance WHERE ProcessorTransactionID = ?";
	public static final String FINDALLRECONCILIATIONSTATUSES = "SELECT ReconciliationStatusID, ReconciliationStatus, Description, DateCreated, DateModified, ModifiedBy FROM ReconciliationStatus_Lookup";
	public static final String FINDRECONCILIATIONSTATUSBYRECONCILIATIONSTATUSID = "SELECT ReconciliationStatusID, ReconciliationStatus, Description, DateCreated, DateModified, ModifiedBy FROM ReconciliationStatus_Lookup WHERE ReconciliationStatusID = ?";
	public static final String FINDRECONCILIATIONSTATUSBYRECONCILIATIONSTATUS = "SELECT ReconciliationStatusID, ReconciliationStatus, Description, DateCreated, DateModified, ModifiedBy FROM ReconciliationStatus_Lookup WHERE ReconciliationStatus = ?";
	public static final String FINDALLSALETRANSACTIONS = "SELECT SaleTransactionID, FirstName, LastName, ProcessUser, TransactionType, Address1, Address2, City, State, PostalCode, Country, CardNumberFirst6Char, CardNumberLast4Char, CardType, ExpiryDate, Token, ChargeAmount, LegalEntityApp, AccountId, ApplicationTransactionID, MerchantID, Processor, Application, Origin, ProcessorTransactionID, TransactionDateTime, TestMode, ApprovalCode, Tokenized, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, PaymentProcessorRuleID, RulePaymentProcessorID, RuleCardType, RuleMaximumMonthlyAmount, RuleNoMaximumMonthlyAmountFlag, RulePriority, AccountPeriod, Desk, InvoiceNumber, UserDefinedField1, UserDefinedField2, UserDefinedField3, ReconciliationStatusID, ReconciliationDate, BatchUploadID, ETL_RUNID FROM Sale_Transaction";
	public static final String FINDSALETRANSACTIONBYAPPLICATIONTRANSACTIONID = "SELECT SaleTransactionID, FirstName, LastName, ProcessUser, TransactionType, Address1, Address2, City, State, PostalCode, Country, CardNumberFirst6Char, CardNumberLast4Char, CardType, ExpiryDate, Token, ChargeAmount, LegalEntityApp, AccountId, ApplicationTransactionID, MerchantID, Processor, Application, Origin, ProcessorTransactionID, TransactionDateTime, TestMode, ApprovalCode, Tokenized, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, PaymentProcessorRuleID, RulePaymentProcessorID, RuleCardType, RuleMaximumMonthlyAmount, RuleNoMaximumMonthlyAmountFlag, RulePriority, AccountPeriod, Desk, InvoiceNumber, UserDefinedField1, UserDefinedField2, UserDefinedField3, ReconciliationStatusID, ReconciliationDate, BatchUploadID, ETL_RUNID FROM Sale_Transaction WHERE ApplicationTransactionID = ?";
	public static final String FINDSALETRANSACTIONBYPROCESSORTRANSACTIONID = "SELECT SaleTransactionID, FirstName, LastName, ProcessUser, TransactionType, Address1, Address2, City, State, PostalCode, Country, CardNumberFirst6Char, CardNumberLast4Char, CardType, ExpiryDate, Token, ChargeAmount, LegalEntityApp, AccountId, ApplicationTransactionID, MerchantID, Processor, Application, Origin, ProcessorTransactionID, TransactionDateTime, TestMode, ApprovalCode, Tokenized, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, PaymentProcessorRuleID, RulePaymentProcessorID, RuleCardType, RuleMaximumMonthlyAmount, RuleNoMaximumMonthlyAmountFlag, RulePriority, AccountPeriod, Desk, InvoiceNumber, UserDefinedField1, UserDefinedField2, UserDefinedField3, ReconciliationStatusID, ReconciliationDate, BatchUploadID, ETL_RUNID FROM Sale_Transaction WHERE ProcessorTransactionID = ?";
	public static final String FINDCOUNTBYPAYMENTPROCESSORRULEID = "SELECT COUNT(*) FROM Sale_Transaction WHERE PaymentProcessorRuleID = ?";
	public static final String FINDSALETRANSACTIONBYBATCHUPLOADID = "SELECT SaleTransactionID, FirstName, LastName, ProcessUser, TransactionType, Address1, Address2, City, State, PostalCode, Country, CardNumberFirst6Char, CardNumberLast4Char, CardType, ExpiryDate, Token, ChargeAmount, LegalEntityApp, AccountId, ApplicationTransactionID, MerchantID, Processor, Application, Origin, ProcessorTransactionID, TransactionDateTime, TestMode, ApprovalCode, Tokenized, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, PaymentProcessorRuleID, RulePaymentProcessorID, RuleCardType, RuleMaximumMonthlyAmount, RuleNoMaximumMonthlyAmountFlag, RulePriority, AccountPeriod, Desk, InvoiceNumber, UserDefinedField1, UserDefinedField2, UserDefinedField3, ReconciliationStatusID, ReconciliationDate, BatchUploadID, ETL_RUNID FROM Sale_Transaction WHERE BatchUploadID = ?";
	public static final String FINDALLREFUNDTRANSACTIONS = "SELECT RefundTransactionID, SaleTransactionID, ApprovalCode, Processor, RefundAmount, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, ReconciliationStatusID, ReconciliationDate, ETL_RUNID FROM Refund_Transaction";
	public static final String FINDREFUNDTRANSACTIONBYAPPLICATIONTRANSACTIONID = "SELECT RefundTransactionID, SaleTransactionID, ApprovalCode, Processor, RefundAmount, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, ReconciliationStatusID, ReconciliationDate, ETL_RUNID FROM Refund_Transaction WHERE ApplicationTransactionID = ?";
	public static final String FINDREFUNDTRANSACTIONBYPROCESSORTRANSACTIONID = "SELECT RefundTransactionID, SaleTransactionID, ApprovalCode, Processor, RefundAmount, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated, ReconciliationStatusID, ReconciliationDate, ETL_RUNID FROM Refund_Transaction WHERE ProcessorTransactionID = ?";
	public static final String FINDALLVOIDTRANSACTIONS = "SELECT VoidTransactionID, SaleTransactionID, ApprovalCode, Processor, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated FROM Void_Transaction";
	public static final String FINDVOIDTRANSACTIONBYAPPLICATIONTRANSACTIONID = "SELECT VoidTransactionID, SaleTransactionID, ApprovalCode, Processor, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated FROM Void_Transaction WHERE ApplicationTransactionID = ?";
	public static final String FINDVOIDTRANSACTIONBYPROCESSORTRANSACTIONID = "SELECT VoidTransactionID, SaleTransactionID, ApprovalCode, Processor, MerchantID, ProcessorTransactionID, TransactionDateTime, ApplicationTransactionID, Application, pUser, OriginalSaleTransactionID, PaymentProcessorStatusCode, PaymentProcessorStatusCodeDescription, PaymentProcessorResponseCode, PaymentProcessorResponseCodeDescription, InternalStatusCode, InternalStatusDescription, InternalResponseCode, InternalResponseDescription, PaymentProcessorInternalStatusCodeID, PaymentProcessorInternalResponseCodeID, DateCreated FROM Void_Transaction WHERE ProcessorTransactionID = ?";
	public static final String FINDALLUSERS = "SELECT  ul.UserID, UserName, FirstName, LastName, ul.IsActive, LastLogin, ul.DateCreated, DateUpdated, Email, UserPassword, ul.DateModified, ul.ModifiedBy, Status  FROM User_Lookup ul";
	public static final String FINDUSERBYUSERID = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE UserID = ?";
	public static final String FINDUSERBYUSERNAME = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE UserName = ?";
	public static final String FINDUSERBYEMAIL = "SELECT UserID, UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status FROM User_Lookup WHERE Email = ?";
	public static final String SAVEUSER = "INSERT INTO User_Lookup (UserName, FirstName, LastName, IsActive, LastLogin, DateCreated, DateUpdated, Email, UserPassword, DateModified, ModifiedBy, Status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String UPDATEUSER = "UPDATE User_Lookup SET UserName = ?, FirstName = ?, LastName = ?, IsActive = ?, LastLogin = ?, DateCreated = ?, DateUpdated = ?, Email = ?, UserPassword = ?, DateModified = ?, ModifiedBy = ?, Status = ? WHERE UserID = ?";
	public static final String DELETEUSERBYUSERNAME = "DELETE FROM User_Lookup WHERE UserName = ?";
	public static final String FINDUSERLEGALENTITYAPPBYUSERID = "SELECT UserLegalEntityAppID, UserID, LegalEntityAppID, DateCreated, DatedModified, ModifiedBy FROM User_LegalEntityApp WHERE UserID = ?";
	public static final String FINDUSERROLEBYUSERID = "SELECT UserRoleID, UserID, RoleID, DateCreated, DatedModified, ModifiedBy FROM User_Role WHERE UserID = ?";
	public static final String FINDUSERROLEBYROLEID = "SELECT UserRoleID, UserID, RoleID, DateCreated, DatedModified, ModifiedBy FROM User_Role WHERE RoleID = ?";
	public static final String SAVEUSERROLE = "INSERT INTO User_Role (UserID,RoleID,DateCreated) VALUES(?,?,?)";
	public static final String SAVEUSERLEGALENTITYAPP = "INSERT INTO User_LegalEntityApp (UserID,LegalEntityAppID,DateCreated) VALUES(?,?,?)";
	public static final String FINDPAYMENTPROCESSORBYID = "SELECT PaymentProcessorID, ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime FROM PaymentProcessor_Lookup WHERE PaymentProcessorID = ?";
	public static final String FINDPAYMENTPROCESSORBYNAME = "SELECT PaymentProcessorID, ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime FROM PaymentProcessor_Lookup WHERE ProcessorName = ?";
	public static final String FINDPAYMENTPROCESSORRULEBYID= "SELECT PaymentProcessorRuleID, PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, CurrentYear,CurrentMonth,DateCreated,ModifiedBy FROM PaymentProcessor_Rule WHERE PaymentProcessorID = ?";
	public static final String FINDPAYMENTPROCESSORRULEBY_ID = "SELECT PaymentProcessorRuleID, PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, CurrentYear, CurrentMonth, DateCreated, ModifiedBy FROM PaymentProcessor_Rule WHERE PaymentProcessorRuleID = ?";
	public static final String FINDPAYMENTPROCESSORMERCHANTSBYID= "SELECT PaymentProcessorMerchantID, LegalEntityAppID, PaymentProcessorID, TestOrProd, MerchantID, DateCreated, DatedModified, ModifiedBy FROM PaymentProcessor_Merchant WHERE PaymentProcessorID = ?";
	public static final String FINDALLPAYMENTPROCESSORS = "SELECT PaymentProcessorID, ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime FROM PaymentProcessor_Lookup";	
	public static final String FINDPAYMENTPROCESSORRESPONSECODEBYCODEID = "SELECT PaymentProcessorResponseCodeID, PaymentProcessorID, PaymentProcessorResponseCode,TransactionType,PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorResponseCode_Lookup WHERE TransactionType=? AND PaymentProcessorResponseCode=? AND PaymentProcessorID=?";
	public static final String FINDPAYMENTPROCESSORRESPONSECODEBYTYPEID = "SELECT PaymentProcessorResponseCodeID, PaymentProcessorID, PaymentProcessorResponseCode,TransactionType,PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorResponseCode_Lookup WHERE TransactionType=? AND PaymentProcessorID=?";
	public static final String FINDPAYMENTPROCESSORSTATUSCODEBYCODEID = "SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE TransactionType=? AND PaymentProcessorStatusCode=? AND PaymentProcessorID=?";
	public static final String FINDPAYMENTPROCESSORSTATUSCODEBYTYPEID = "SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE TransactionType=? AND PaymentProcessorID=?";
	public static final String FINDPAYMENTPROCESSORSTATUSCODEBYID = "SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE PaymentProcessorStatusCodeID =?";
	public static final String FINDPAYMENTPROCESSORSATUSCODESBYPPID = "SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE PaymentProcessorID =?";
	public static final String FINDALLPAYMENTPROCESSORSBYIDS = "SELECT PaymentProcessorID, ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime FROM PaymentProcessor_Lookup WHERE PaymentProcessorID IN (:paymentProcessorIds)";
	public static final String SAVEPAYMENTPROCESSORS = "INSERT INTO PaymentProcessor_Lookup (ProcessorName, DateCreated, DatedModified, ModifiedBy, IsActive, RemitTransactionOpenTime, RemitTransactionCloseTime) VALUES (?, ?, ?, ?, ?, ?, ?)";	
	public static final String SAVEPAYMENTPROCESSORRULES = "INSERT INTO PaymentProcessor_Rule ( PaymentProcessorID, DateCreated,ModifiedBy) VALUES (?,?,?)";
	public static final String DELETEPAYMENTPROCESSORBYID = "DELETE FROM PaymentProcessor_Lookup WHERE PaymentProcessorID = ?";
	public static final String DELETEPAYMENTPROCESSORMERCHANTBYPROCID = "DELETE FROM PaymentProcessor_Merchant WHERE PaymentProcessorID = ?";
	public static final String DELETEPAYMENTPROCESSORRULES = "DELETE FROM PaymentProcessor_Rule WHERE PaymentProcessorID =?";
	public static final String DELETEPAYMENTPROCESSORMERCHANTS = "DELETE FROM PaymentProcessor_Merchant WHERE PaymentProcessorID =?";
	public static final String DELETEPAYMENTPROCESSORSTATUSCODEBYID = "DELETE FROM PaymentProcessorStatusCode_Lookup WHERE PaymentProcessorID =?";
	public static final String DELETEPAYMENTPROCESSORRESPONSECODEBYID = "DELETE FROM PaymentProcessorResponseCode_Lookup WHERE PaymentProcessorID =?";
	public static final String DELETEPAYMENTPROCESSORSTATUSCODES = "DELETE FROM PaymentProcessorStatusCode_Lookup WHERE PaymentProcessorStatusCodeID IN (:paymentProcessorStatusCodes)";
	public static final String SAVEPAYMENTPROCESSORSTATUSCODE = "INSERT INTO PaymentProcessorStatusCode_Lookup (PaymentProcessorID,PaymentProcessorStatusCode,TransactionType,PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy) VALUES (?,?,?,?,?,?,?)"; 
	public static final String UPDATEPAYMENTPROCESSORSTATUSCODE = "UPDATE PaymentProcessorStatusCode_Lookup SET PaymentProcessorID = ?, PaymentProcessorStatusCode =?, TransactionType=?,PaymentProcessorStatusDescription=?, DatedModified=?, ModifiedBy=? WHERE PaymentProcessorStatusCodeID = ?";
	public static final String DELETEPAYMENTPROCESSORINTERNALSTATUSCODE = "DELETE FROM PaymentProcessor_InternalStatusCode where InternalStatusCodeId = ?";
	public static final String DELETEINTERNALSTATUSCODES = "DELETE FROM InternalStatusCode_Lookup where InternalStatusCodeID IN (:ids)";
	public static final String DELETEPAYMENTPROCESSORINTERNALSTATUSCODES = "DELETE FROM PaymentProcessor_InternalStatusCode where PaymentProcessorInternalStatusCodeID IN (:ids)";
	public static final String FINDPAYMENTPROCESSORSTATUSCODEIDS = "SELECT PaymentProcessorStatusCodeID from PaymentProcessor_InternalStatusCode where InternalStatusCodeID = ?";
	public static final String DELETEPAYMENTPROCESSORSTATUSCODEIDS = "DELETE FROM PaymentProcessorStatusCode_Lookup where PaymentProcessorStatusCodeID IN (:ids)";
	public static final String DELETEINTERNALSTATUSCODE = "DELETE FROM InternalStatusCode_Lookup where InternalStatusCodeID = ?";
	public static final String FINDALLINTERNALSTATUSCODE = "SELECT InternalStatusCodeID,InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,DatedModified,TransactionType,DateCreated FROM InternalStatusCode_Lookup";
	public static final String FINDALLPAYMENTPROCESSORINTERNALSTATUSCODEFORINTERNALSTATUSCODEID = "SELECT PaymentProcessorInternalStatusCodeID,InternalStatusCodeID,PaymentProcessorStatusCodeID,DateCreated,ModifiedBy FROM PaymentProcessor_InternalStatusCode where InternalStatusCodeID = ?";
	public static final String FINDINTERNALSTATUSCODEBYID = "SELECT InternalStatusCodeID,InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,DatedModified,TransactionType,DateCreated FROM InternalStatusCode_Lookup WHERE InternalStatusCodeID = ?";
	public static final String SAVEINTERNALSTATUSCODE = "INSERT INTO InternalStatusCode_Lookup (InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,TransactionType,DateCreated,DatedModified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String UPDATEINTERNALSTATUSCODE = "UPDATE InternalStatusCode_Lookup SET InternalStatusCode = ?, InternalStatusCodeDescription = ?, ModifiedBy = ? , InternalStatusCategoryAbbr = ? , InternalStatusCategory = ?, TransactionType = ?, DatedModified = ? WHERE InternalStatusCodeID = ?";
	public static final String SAVEPAYMENTPROCESSORINTERNALSTATUSCODE = "INSERT INTO PaymentProcessor_InternalStatusCode (InternalStatusCodeId,PaymentProcessorStatusCodeId,DateCreated,ModifiedBy) VALUES (?, ?, ?, ?)";
	public static final String FINDBYINTERNALSTATUSCODEANDTRANSACTIONTYPE = "SELECT InternalStatusCodeID,InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,DatedModified,TransactionType,DateCreated FROM InternalStatusCode_Lookup WHERE InternalStatusCode = ? AND TransactionType = ?";
	public static final String FINDALLINTERNALSTATUSCODEBYTRANSACTIONTYPE = "SELECT InternalStatusCodeID,InternalStatusCode,InternalStatusCodeDescription,ModifiedBy,InternalStatusCategoryAbbr,InternalStatusCategory,DatedModified,TransactionType,DateCreated FROM InternalStatusCode_Lookup WHERE TransactionType = ?";
	public static final String FINDPAYMENTPROCESSORRULEBYCARDTYPE = "SELECT PaymentProcessorRuleID, PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, CurrentYear, CurrentMonth, DateCreated, ModifiedBy FROM PaymentProcessor_Rule WHERE CardType = ?";
	public static final String SAVEPAYMENTPROCESSORRULE = "INSERT INTO PaymentProcessor_Rule (PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, DateCreated,ModifiedBy) VALUES (?,?,?,?,?,?,?,?)";
	public static final String DELETEPAYMENTPROCESSORRULEBYID = "DELETE FROM PaymentProcessor_Rule WHERE PaymentProcessorRuleID = ?";
	public static final String FINDPAYMENTPROCESSORRULESBYPAYMENTPROCESSORID = "SELECT PaymentProcessorRuleID, PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, CurrentYear, CurrentMonth, DateCreated, ModifiedBy FROM PaymentProcessor_Rule WHERE PaymentProcessorID = ?";
	public static final String UPDATEPAYMENTPROCESSORRULE = "UPDATE PaymentProcessor_Rule SET PaymentProcessorID= ?, CardType= ?,MaximumMonthlyAmount=?,NoMaximumMonthlyAmountFlag=?,Priority=? WHERE PaymentProcessorRuleID= ?";

	//Below queries has been used for InternalResponseCode/PaymentProcessorInternalResponseCode
	public static final String FINDBYINTERNALRESPONSECODEANDTRANSACTIONTYPENAME ="SELECT * FROM InternalResponseCode_Lookup WHERE InternalResponseCode = ? AND TransactionType = ?";
	public static final String FINDALLINTERNALRESPONSECODE = "SELECT InternalResponseCodeID,InternalResponseCode,InternalResponseCodeDescription,ModifiedBy,DatedModified,TransactionType,DateCreated FROM InternalResponseCode_Lookup";
	public static final String FINDALLINTERNALRESPONSECODEBYTRANSACTIONTYPE = "SELECT InternalResponseCodeID,InternalResponseCode,InternalResponseCodeDescription,ModifiedBy,DatedModified,TransactionType,DateCreated FROM InternalResponseCode_Lookup WHERE TransactionType = ?";
	public static final String SAVEINTERNALRESPONSECODE = "INSERT INTO InternalResponseCode_Lookup (InternalResponseCode, InternalResponseCodeDescription, ModifiedBy, DatedModified, TransactionType,DateCreated) VALUES (?, ?, ?, ?, ?, ?)";
	public static final String FINDONEINTERNALRESPONSECODE = "SELECT * FROM InternalResponseCode_Lookup WHERE InternalResponseCodeID = ?";
	public static final String DELETEINTERNALRESPONSECODE = "DELETE FROM InternalResponseCode_Lookup WHERE InternalResponseCodeID = ?";
	public static final String UPDATEINTERNALRESPONSECODE = "UPDATE InternalResponseCode_Lookup SET InternalResponseCode = ?, InternalResponseCodeDescription = ?, ModifiedBy = ?, TransactionType = ?, DatedModified = ? WHERE InternalResponseCodeID = ?";
	public static final String SAVEPAYMENTPROCESSORRESPONSECODE = "INSERT INTO PaymentProcessorResponseCode_Lookup (PaymentProcessorID,PaymentProcessorResponseCode,TransactionType,PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy) VALUES (?,?,?,?,?,?,?)";
	public static final String FINDPAYMENTPROCESSORRESPONSECODEBYID = "SELECT PaymentProcessorResponseCodeID, PaymentProcessorID, PaymentProcessorResponseCode,TransactionType,PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorResponseCode_Lookup WHERE PaymentProcessorResponseCodeID=?";
	public static final String UPDATEPAYMENTPROCESSORRESPONSECODE = "UPDATE PaymentProcessorResponseCode_Lookup SET PaymentProcessorID = ?, PaymentProcessorResponseCode =?, TransactionType=?,PaymentProcessorResponseCodeDescription=?, DatedModified=?, ModifiedBy=? WHERE PaymentProcessorResponseCodeID = ?";
	public static final String SAVEPAYMENTPROCESSORINTERNALRESPONSECODE = "INSERT INTO PaymentProcessor_InternalResponseCode ( PaymentProcessorResponseCodeID,InternalResponseCodeID,DateCreated) VALUES(?,?,?)";
	public static final String FINDONEPAYMENTPROCESSORINTERNALRESPONSECODE = "SELECT * FROM PaymentProcessor_InternalResponseCode WHERE PaymentProcessorResponseCodeID = ?";
	public static final String FINDALLPAYMENTPROCESSORINTERNALRESPONSECODEBYINTERNALRESPCODEID = "SELECT PaymentProcessorInternalResponseCodeID,PaymentProcessorResponseCodeID,InternalResponseCodeID,DateCreated,ModifiedBy FROM PaymentProcessor_InternalResponseCode WHERE InternalResponseCodeID=?";
	public static final String FINDALLPAYMENTPROCESSORINTERNALRESPONSECODEBYPAYMENTPROCESSORRESPONSECODE = "SELECT PaymentProcessorInternalResponseCodeID,PaymentProcessorResponseCodeID,InternalResponseCodeID,DateCreated,ModifiedBy FROM PaymentProcessor_InternalResponseCode WHERE PaymentProcessorResponseCodeID=?";
	public static final String FINDALLPAYMENTPROCESSORINTERNALRESPONSECODE="SELECT PaymentProcessorInternalResponseCodeID,PaymentProcessorResponseCodeID,InternalResponseCodeID,DateCreated,ModifiedBy FROM PaymentProcessor_InternalResponseCode";
	public static final String DELETEPAYMENTPROCESSORPAYMENTPROCESSORRESPONSECODEID = "DELETE FROM PaymentProcessor_InternalResponseCode WHERE PaymentProcessorResponseCodeID = ?";
	public static final String PAYMENTPROCESSORINTERNALRESPONSECODEID = "SELECT * FROM PaymentProcessor_InternalResponseCode WHERE InternalResponseCodeID = ?";
	public static final String DELETEPAYMENTPROCESSORINTERNALRESPONSECODE = "DELETE FROM PaymentProcessor_InternalResponseCode where InternalResponseCodeId = ?";
	public static final String FINDPAYMENTPROCESSORINTERNALRESPONSECODEIDSBYINTERNALRESPONSECODE = "SELECT PaymentProcessorInternalResponseCodeID FROM PaymentProcessor_InternalResponseCode WHERE InternalResponseCodeID=?";
	public static final String DELETEPAYMENTPROCESSORRESPONSECODEIDS = "DELETE FROM PaymentProcessor_InternalResponseCode where PaymentProcessorInternalResponseCodeID IN (:ids)";
	public static final String SAVEPAYMENTPROCESSORMARCHENT = "INSERT INTO PaymentProcessor_Merchant (paymentProcessorId,TestOrProd,MerchantID,DateCreated,DatedModified,LegalEntityAppID) values (?, ?, ?, ?, ?, ?)";
	public static final String FETCHINTERNALSTATUSCODEUSEDFORPAYMENTPROCESSOR = " select InternalStatusCodeId,PaymentProcessorInternalStatusCodeID from PaymentProcessor_InternalStatusCode where PaymentProcessorStatusCodeID in (  select PaymentProcessorStatusCodeID from PaymentProcessorStatusCode_Lookup where PaymentProcessorID = ? ) ";

	public static final String DELETEPAYMENTPROCESSORINTERNALRESPONSECODES = "DELETE FROM PaymentProcessor_InternalResponseCode where PaymentProcessorInternalResponseCodeID IN (:ids)";
	public static final String DELETEINTERNALRESPONSECODES = "DELETE FROM InternalResponseCode_Lookup where InternalResponseCodeID IN (:ids)";
	public static final String FINDALLPROCESSORRULES = "SELECT * FROM PaymentProcessor_Rule";
	
	public static final String DELETEUSERROLES = "DELETE FROM User_Role where UserRoleID IN (:userRoleIds)";
	public static final String DELETEUSERLEGALENTITIES = "DELETE FROM User_LegalEntityApp WHERE UserLegalEntityAppID IN (:userLegalEntityAppIds)";
	public static final String FINDLEGALENTITIESASSOCIATEDWITHUSERBYLEID  = "SELECT UserLegalEntityAppID FROM User_LegalEntityApp WHERE LegalEntityAppID=?";
	public static final String FINDLEGALENTITIESASSOCIATEDWITHUSERBYUSERID  = "select count(*) from User_LegalEntityApp where UserID=? and LegalEntityAppID not in(:)";
	public static final String UPDATEPAYMENTPROCESSOR = "UPDATE PaymentProcessor_Lookup SET ProcessorName=?,IsActive=?,RemitTransactionOpenTime=?,RemitTransactionCloseTime=?,DatedModified=? WHERE PaymentProcessorID=?";
	public static final String FINDCOUNTUSERLOOKUP = "SELECT COUNT(*) From User_Lookup";
	public static final String FINDALLSALETRANSACTIONSCOUNT = "SELECT COUNT(SaleTransactionID) FROM Sale_Transaction";
	public static final String FINDPREFERENCEIDBYPREFERENCEKEY = "SELECT PreferenceID from Preference_Lookup Where PreferenceKey = ?";
	public static final String FINDPREFERENCEIDBYPREFERENCEID = "SELECT * from User_Preference_Lookup where UserID=? and PreferenceID=?";
	public static final String UPDATEUSERPREFERENCE = "UPDATE User_Preference_Lookup SET PreferenceValue = ? WHERE UserPreferenceID =?"; 
	public static final String SAVEUSERPREFERENCE = "INSERT INTO User_Preference_Lookup (PreferenceID, PreferenceValue, UserID) VALUES (?,?,?)";
	public static final String FINDSELECTEDTIMEZONEBYUSERID = "SELECT PreferenceValue From User_Preference_Lookup WHERE UserID = ? ";
	
	private Queries(){
		// Default Constructor
	}
}