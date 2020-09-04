package com.mcmcg.ico.bluefin.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomSaleTransactionDAOTest {

	private CustomSaleTransactionDAOImpl customSaleTransactionDAOImpl;
	
	 @Before
	 public void setup() throws Exception {
		 customSaleTransactionDAOImpl = new CustomSaleTransactionDAOImpl();
	 }

	@Test
	public void testgetappendPPRAndSaleQuerySelect() throws Exception{
		String expectedSelect = "SELECT ppr.ReconciliationStatusID, ppr.TransactionAmount";
		
		Method method = CustomSaleTransactionDAOImpl.class.getDeclaredMethod("getappendPPRAndSaleQuery");
		method.setAccessible(true);
		String returnValue = (String) method.invoke(customSaleTransactionDAOImpl);
		boolean isFound = returnValue.contains(expectedSelect);
		
		assertTrue(isFound);
	}
	
	@Test
	public void testgetappendPPRAndSaleQueryFrom() throws Exception{
		String expectedSelect = "FROM PaymentProcessor_Remittance";
		
		Method method = CustomSaleTransactionDAOImpl.class.getDeclaredMethod("getappendPPRAndSaleQuery");
		method.setAccessible(true);
		String returnValue = (String) method.invoke(customSaleTransactionDAOImpl);
		boolean isFound = returnValue.contains(expectedSelect);
		
		assertTrue(isFound);
	}
	
	@Test
	public void testgetappendPPRAndSaleQueryWhere() throws Exception{
		String expectedSelect = "WHERE ppr2.RemittanceCreationDate >= :remittanceCreationDateBegin";
		
		Method method = CustomSaleTransactionDAOImpl.class.getDeclaredMethod("getappendPPRAndSaleQuery");
		method.setAccessible(true);
		String returnValue = (String) method.invoke(customSaleTransactionDAOImpl);
		boolean isFound = returnValue.contains(expectedSelect);
				
		assertTrue(isFound);
	}
	
	@Test
	public void testgetappendPPRAndRefundQuerySelect() throws Exception{
		String expectedSelect = "SELECT ppr.ReconciliationStatusID, ppr.TransactionAmount";
		
		Method method = CustomSaleTransactionDAOImpl.class.getDeclaredMethod("getappendPPRAndRefundQuery");
		method.setAccessible(true);
		String returnValue = (String) method.invoke(customSaleTransactionDAOImpl);
		boolean isFound = returnValue.contains(expectedSelect);

		assertTrue(isFound);
	}
	
	@Test
	public void testgetappendPPRAndRefundQueryFrom() throws Exception{
		String expectedSelect = "FROM PaymentProcessor_Remittance";
		
		Method method = CustomSaleTransactionDAOImpl.class.getDeclaredMethod("getappendPPRAndRefundQuery");
		method.setAccessible(true);
		String returnValue = (String) method.invoke(customSaleTransactionDAOImpl);
		boolean isFound = returnValue.contains(expectedSelect);
		
		assertTrue(isFound);
	}
	
	@Test
	public void testgetappendPPRAndRefundQueryWhere() throws Exception{
		String expectedSelect = "WHERE ppr2.RemittanceCreationDate >= :remittanceCreationDateBegin";
		
		Method method = CustomSaleTransactionDAOImpl.class.getDeclaredMethod("getappendPPRAndRefundQuery");
		method.setAccessible(true);
		String returnValue = (String) method.invoke(customSaleTransactionDAOImpl);
		boolean isFound = returnValue.contains(expectedSelect);
		
		assertTrue(isFound);
	}

	@Test
	public void evaluateSearchParamCountTest() throws Exception {
		String searchParam = "merchantId:[ccprocessorb,ccprocessora]$$processorName:REPAY"
				+ "$$remittanceCreationDate>2020-07-01 00:00:00$$remittanceCreationDate<2020-07-01 23:59:59";

		Map<String, String> returnValue = callEvaluateSearchParamMethod(searchParam);

		assertTrue(returnValue.size() == 4);
	}

	@Test
	public void evaluateSearchParamMerchantTest() throws Exception {
		String searchParam = "merchantId:[ccprocessorb,ccprocessora]$$processorName:REPAY"
				+ "$$remittanceCreationDate>2020-07-01 00:00:00$$remittanceCreationDate<2020-07-01 23:59:59";

		Map<String, String> returnValue = callEvaluateSearchParamMethod(searchParam);
		String merchantIds = returnValue.get(BluefinWebPortalConstants.MERCHANTIDPARAM);
		boolean checkValue = merchantIds.contains("[") && merchantIds.contains("]");

		assertFalse(checkValue);
	}

	@Test
	public void evaluateSearchParamCountDatesTest() throws Exception {
		String searchParam = "remittanceCreationDate>2020-07-01 00:00:00$$remittanceCreationDate<2020-07-01 23:59:59";

		Map<String, String> returnValue = callEvaluateSearchParamMethod(searchParam);

		assertTrue(returnValue.size() == 2);
	}

	@Test
	public void appendWhereConditionsToMainSelectQueryParamsTest() throws Exception {
		Map<String, String> queryParameters = new HashMap<>();
		String searchParam = "merchantId:[ccprocessorb,ccprocessora]$$processorName:REPAY"
				+ "$$remittanceCreationDate>2020-07-01 00:00:00$$remittanceCreationDate<2020-07-01 23:59:59";

		Map<String, String> searchValues = callEvaluateSearchParamMethod(searchParam);
		boolean dataFiltered = !CollectionUtils.isEmpty(searchValues);
		callAppendWhereConditionsToMainSelectMethod(dataFiltered, searchValues, queryParameters);

		assertTrue(queryParameters.size() == 3);
	}

	@Test
	public void appendWhereConditionsToMainSelectTest() throws Exception {
		Map<String, String> queryParameters = new HashMap<>();
		String searchParam = "remittanceCreationDate>2020-07-01 00:00:00$$remittanceCreationDate<2020-07-01 23:59:59";

		Map<String, String> searchValues = callEvaluateSearchParamMethod(searchParam);
		boolean dataFiltered = !CollectionUtils.isEmpty(searchValues);
		StringBuilder sb = callAppendWhereConditionsToMainSelectMethod(dataFiltered, searchValues, queryParameters);

		assertTrue(sb.toString().contains("ORDER BY"));
	}

	private Map<String, String> callEvaluateSearchParamMethod(String searchParam) throws Exception {
		Method method = CustomSaleTransactionDAOImpl.class.getDeclaredMethod("evaluateSearchParam", String.class);
		method.setAccessible(true);
		Map<String, String> returnValue = (Map<String, String>) method.invoke(customSaleTransactionDAOImpl, searchParam);
		return returnValue;
	}

	private StringBuilder callAppendWhereConditionsToMainSelectMethod(boolean dataFiltered,
			Map<String, String> evaluatedValues, Map<String, String> queryParameters) throws Exception {
		Method method = CustomSaleTransactionDAOImpl.class.getDeclaredMethod("appendWhereConditionsToMainSelect",
				boolean.class, Map.class, Map.class);
		method.setAccessible(true);
		StringBuilder sb = (StringBuilder) method.invoke(customSaleTransactionDAOImpl, dataFiltered, evaluatedValues,
				queryParameters);
		return sb;
	}

}
