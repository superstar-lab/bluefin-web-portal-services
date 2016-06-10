package com.mcmcg.ico.bluefin.service.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.mcmcg.ico.bluefin.persistent.TransactionView;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.SearchCriteria;
import com.mcmcg.ico.bluefin.rest.resource.StatusCode;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;

public class TransactionPredicate {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPredicate.class);
    private SearchCriteria criteria;
    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public TransactionPredicate() {
    }

    public TransactionPredicate(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    public BooleanExpression getPredicate() {
        PathBuilder<TransactionView> entityPath = new PathBuilder<TransactionView>(TransactionView.class,
                "transactionView");
        Class<?> keyInstance = BeanUtils.findPropertyType(criteria.getKey(), TransactionView.class);
        BooleanExpression result = null;
        if (keyInstance == Date.class) {
            result = getDatePredicate(entityPath);
        } else if (keyInstance == BigDecimal.class) {
            result = getNumericPredicate(entityPath);
        } else if (keyInstance == String.class) {
            result = getStringPredicate(entityPath);
        } else if (keyInstance == StatusCode.class) {
            result = getStatusCodePredicate(entityPath);
        } else {
            LOGGER.error("Unable to filter by: {}", criteria.getKey());
            throw new CustomBadRequestException("Unable to filter by: " + criteria.getKey());
        }
        return result;
    }

    private BooleanExpression getDatePredicate(PathBuilder<TransactionView> entityPath) {
        Date date = isValidDate(criteria.getValue().toString());
        if (date != null) {
            DatePath<Date> path = entityPath.getDate(criteria.getKey(), Date.class);
            if (criteria.getOperation().equalsIgnoreCase(":")) {
                return path.eq(date);
            } else if (criteria.getOperation().equalsIgnoreCase(">")) {
                return path.goe(date);
            } else if (criteria.getOperation().equalsIgnoreCase("<")) {
                return path.loe(date);
            }
        }

        LOGGER.error("Unable to parse date value of {}", criteria.getKey());
        throw new CustomBadRequestException("Unable to parse date value of " + criteria.getKey());
    }

    private BooleanExpression getNumericPredicate(PathBuilder<TransactionView> entityPath) {
        if (NumberUtils.isNumber(criteria.getValue().toString())) {
            NumberPath<BigDecimal> path = entityPath.getNumber(criteria.getKey(), BigDecimal.class);
            BigDecimal value = new BigDecimal(criteria.getValue().toString());
            if (criteria.getOperation().equalsIgnoreCase(":")) {
                return path.eq(value);
            } else if (criteria.getOperation().equalsIgnoreCase(">")) {
                return path.goe(value);
            } else if (criteria.getOperation().equalsIgnoreCase("<")) {
                return path.loe(value);
            }
        }

        LOGGER.error("Unable to parse numeric value of {}", criteria.getKey());
        throw new CustomBadRequestException("Unable to parse numeric value of " + criteria.getKey());
    }

    private BooleanExpression getStatusCodePredicate(PathBuilder<TransactionView> entityPath) {
        NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
        Integer value = StatusCode.getStatusCodeByString(criteria.getValue().toString());

        if (value != null && criteria.getOperation().equalsIgnoreCase(":")) {
            return path.eq(value);
        }

        LOGGER.error("Unable to parse the value of {}", criteria.getKey());
        throw new CustomBadRequestException("Unable to parse the value of " + criteria.getKey());
    }

    private BooleanExpression getStringPredicate(PathBuilder<TransactionView> entityPath) {
        StringPath path = entityPath.getString(criteria.getKey());

        if (criteria.getOperation().equalsIgnoreCase(":")) {
            return path.containsIgnoreCase(criteria.getValue().toString());
        }

        LOGGER.error("Unable to parse string value of {}", criteria.getKey());
        throw new CustomBadRequestException("Unable to parse string value of " + criteria.getKey());
    }

    private static Date isValidDate(String date) {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            return df.parse(date);
        } catch (ParseException e) {
            LOGGER.error("Unable to parse date value");
            return null;
        }
    }

}
