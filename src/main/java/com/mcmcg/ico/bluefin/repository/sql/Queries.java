package com.mcmcg.ico.bluefin.repository.sql;

public interface Queries {
	String findAllApplications = "SELECT ApplicationID, ApplicationName FROM Application_Lookup";
	String findAllOriginPaymentFrequencies = "SELECT OriginPaymentFrequencyID, Origin, PaymentFrequency, DateCreated, DateModified, ModifiedBy FROM OriginPaymentFrequency_Lookup";
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
}
