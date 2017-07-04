package com.mcmcg.ico.bluefin.service.util.querydsl;

import static com.mcmcg.ico.bluefin.service.util.QueryUtil.SEARCH_DELIMITER_CHAR;
import static com.mcmcg.ico.bluefin.service.util.QueryUtil.SEARCH_REGEX;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.service.util.QueryUtil;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;

public class QueryDSLUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDSLUtil.class);

    private QueryDSLUtil() {
		// default constructor
	}
    
    public static BooleanExpression createExpression(String search, Class<?> entity) {
        PredicatesBuilder builder = new PredicatesBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + SEARCH_DELIMITER_CHAR);
            while (matcher.find()) {
                builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }
        }
        return builder.build(entity);
    }

    public static PageRequest getPageRequest(int page, int size, String sort) {
        return QueryUtil.getPageRequest(page, size, sort);
    }

    /**
     * Checks the validity of the search criteria by checking the current legal
     * entities owned by the consultant user and the ones provided. This
     * verification will be executed by using LE id If the user do not have one
     * LE, an access denied exception will be raised
     * 
     * @param userLE
     * @param search
     * @return String with the valid search criteria
     */
    public static String getValidSearchBasedOnLegalEntitiesById(List<LegalEntityApp> userLE, String search) {
        return QueryUtil.getValidSearchBasedOnLegalEntitiesById(userLE, search);
    }

    /**
     * Checks the validity of the search criteria by checking the current legal
     * entities owned by the consultant user and the ones provided. This
     * verification will be executed by using LE names
     * 
     * @param userLE
     * @param search
     * @return String with the valid search criteria
     */
    public static String getValidSearchBasedOnLegalEntities(List<LegalEntityApp> userLE, String search) {
        return QueryUtil.getValidSearchBasedOnLegalEntities(userLE, search);
    }

    /**
     * Checks if the search criteria has an string with the transactionId and
     * returns it. If it can not parse it a bad request exception will rise
     * 
     * @param search
     * @param filter
     * @return String with the transactionId value
     */
    public static String getTransactionIdValue(String search, String filter) {
       return QueryUtil.getTransactionIdValue(search, filter);
    }

    /**
     * Generate the filter by processorTransactionId or processorTransactionId
     * 
     * @param search
     * @param value
     * @return Boolean Expression with the filter
     */
    public static BooleanExpression getTransactionIdFilter(String value) {
        PathBuilder<SaleTransaction> entityPath = new PathBuilder<>(SaleTransaction.class,
                "saleTransaction");
        StringPath pathApplicationTransactionId = entityPath.getString("applicationTransactionId");
        StringPath pathProcessorTransactionId = entityPath.getString("processorTransactionId");
        return pathApplicationTransactionId.containsIgnoreCase(value)
                .or(pathProcessorTransactionId.containsIgnoreCase(value));
    }

    /**
     * Creates a list that with the LE that are given in the search criteria. It
     * takes the search criteria and pulls out the parameter with the LE and
     * split it into a list to be analyzed with the own legal entities of the
     * consultant user
     * 
     * @param value
     * @return return a list of strings
     */
    private static List<String> getLEListFilterValue(String value) {
        return QueryUtil.getLEListFilterValue(value);
    }
}
