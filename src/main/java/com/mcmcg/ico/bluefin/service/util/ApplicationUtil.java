package com.mcmcg.ico.bluefin.service.util;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mcmcg.ico.bluefin.model.SaleTransaction;

public class ApplicationUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUtil.class);
	private ApplicationUtil(){
		// Default Constructor 
	}
	/**
	 * Get the value of parameter in search string.
	 * 
	 * @param search
	 *            string
	 * @param parameter
	 *            in search string
	 * 
	 * @return value of parameter
	 */
	public static String getValueFromParameter(String search, String parameter) {

		String value = null;
		String[] array1 = search.split("\\$\\$");

		for (String pair : array1) {
			if (pair.startsWith(parameter)) {
				String[] array2 = pair.split(":");
				value = array2[1];
				break;
			}
		}

		return value;
	}
	
	public static SaleTransaction getDateTimeFormat(SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String timeZone) {
		LOGGER.info("Entering to set date and time for batch return file");
		// TransactionDateTime needs to be split into two parts.
		DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MM/dd/yyyy");
		DateTimeFormatter fmt2 = DateTimeFormat.forPattern("hh:mm:ss.SSa");

		// Batch Upload Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (saleTransaction.getTransactionDateTime() == null) {
			saleTransactionDataRecord.add("");
		} else {
			DateTime dateTimeUTC = saleTransaction.getTransactionDateTime().toDateTime(DateTimeZone.UTC);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
			saleTransactionDataRecord.add(fmt1.print(dateTimeUser));
		}

		// Batch Upload Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (saleTransaction.getTransactionDateTime() == null) {
			saleTransactionDataRecord.add("");
		} else {
			DateTime dateTimeUTC = saleTransaction.getTransactionDateTime().toDateTime(DateTimeZone.UTC);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
			saleTransactionDataRecord.add(fmt2.print(dateTimeUser));
		}
		LOGGER.info("Exiting after setting date and time for batch return file");
		return saleTransaction;
	}
}
