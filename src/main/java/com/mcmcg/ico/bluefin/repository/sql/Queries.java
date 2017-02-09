package com.mcmcg.ico.bluefin.repository.sql;

public interface Queries {
	String findAllApplications = "SELECT ApplicationID, ApplicationName FROM Application_Lookup";
	String findAllOriginPaymentFrequencies = "SELECT OriginPaymentFrequencyID, Origin, PaymentFrequency, DateCreated, DateModified, ModifiedBy FROM OriginPaymentFrequency_Lookup";
	String findPropertyByName = "SELECT ApplicationpropertyID, ApplicationPropertyName, ApplicationPropertyValue, DataType, Description, DateCreated, DateModified, ModifiedBy FROM ApplicationProperty_Lookup WHERE ApplicationPropertyName = ?";
	String saveUserLoginHistory = "INSERT INTO User_Login_History (UserID, LoginDateTime, DateCreated, MessageID, UserName, UserPassword) VALUES (?, ?, ?, ?, ?, ?)";
}
