package com.mcmcg.ico.bluefin.service.util.querydsl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;

class Predicate {

    private static final Logger LOGGER = LoggerFactory.getLogger(Predicate.class);
    private SearchCriteria criteria;
    private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public Predicate() {
    	// Default Constructor
    }

    public Predicate(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    public BooleanExpression getPredicate(Class<?> entity) {
        String entityQName = Character.toLowerCase(entity.getSimpleName().charAt(0))
                + entity.getSimpleName().substring(1);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        PathBuilder<?> entityPath = new PathBuilder(entity, entityQName);
        Class<?> keyInstance = BeanUtils.findPropertyType(criteria.getKey(), entityPath.getType());
        BooleanExpression result;
        if (keyInstance == Date.class || keyInstance == DateTime.class) {
            result = getDatePredicate(entityPath);
        } else if (keyInstance == BigDecimal.class) {
            result = getNumericPredicate(entityPath);
        } else if (keyInstance == Long.class) {
            result = getLongNumericPredicate(entityPath);
        } else if (keyInstance == String.class) {
            result = getStringPredicate(entityPath);
        } else if (keyInstance == Collection.class) {
            String collectionType = getCollectionType();
            result = getCollectionPredicate(collectionType);
        } else {
            LOGGER.error("Predicate :: getPredicate() : Unable to filter by: {}", criteria.getKey());
            throw new CustomBadRequestException("Unable to filter by: " + criteria.getKey());
        }
        return result;
    }

    private String getCollectionType() {
        try {
            Method method = User.class.getMethod(
                    "set" + Character.toUpperCase(criteria.getKey().charAt(0)) + criteria.getKey().substring(1),
                    Collection.class);
            Type[] genericParameterTypes = method.getGenericParameterTypes();

            for (Type type : genericParameterTypes) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    for (Type t : pt.getActualTypeArguments()) {
                        return t.getTypeName();
                    }
                }
            }
        } catch (NoSuchMethodException | SecurityException e) {
        	if (LOGGER.isDebugEnabled()) {
        		LOGGER.debug("Failed to get collection type ",e);
        	}
        }
        throw new CustomBadRequestException("Predicate :: getCollectionType() : Unable to filter by " + criteria.getKey());
    }

    private BooleanExpression getCollectionPredicate(String collectionType) {
        getListFromCriteria();
        return null;
    }
    
    private List<Long> getListFromCriteria() {
        Matcher matcher = Pattern.compile("\\[(.*?)\\]").matcher(criteria.getValue().toString());
        String criteriaValue = null;
        while (matcher.find()) {
            criteriaValue = matcher.group(1);
        }
        if (criteriaValue == null) {
            LOGGER.error("Predicate :: getListFromCriteria() : Unable to parse value of {}, correct format example [1,2,3]", criteria.getKey());
            throw new CustomBadRequestException(
                    "Unable to parse value of " + criteria.getKey() + ", correct format example [1,2,3]");
        } else if (criteriaValue.isEmpty()) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(criteriaValue.split(",")).stream().map(String::trim).mapToLong(Long::parseLong).boxed()
                    .collect(Collectors.toList());
        }
    }

    private List<String> getStringListFromCriteria() {
        Matcher matcher = Pattern.compile("\\[(.*?)\\]").matcher(criteria.getValue().toString());
        String criteriaValue = null;
        while (matcher.find()) {
            criteriaValue = matcher.group(1);
        }
        if (criteriaValue != null) {
            return Arrays.asList(criteriaValue.split(",")).stream().map(String::trim)
                    .collect(Collectors.toList());
        } else {
            LOGGER.error("Predicate :: getStringListFromCriteria() : Unable to parse value of {}, correct format example [XXXXX,YYYYYY,ZZZZZ]", criteria.getKey());
            throw new CustomBadRequestException(
                    "Unable to parse value of " + criteria.getKey() + ", correct format example [XXXXX,YYYYYY,ZZZZZ]");
        }
    }

    private BooleanExpression getDatePredicate(PathBuilder<?> entityPath) {
        Date date = isValidDate(criteria.getValue().toString());
        if (date != null) {
            DatePath<Date> path = entityPath.getDate(criteria.getKey(), Date.class);
            if (":".equalsIgnoreCase(criteria.getOperation())) {
                return path.eq(date);
            } else if (">".equalsIgnoreCase(criteria.getOperation())) {
                return path.goe(date);
            } else if ("<".equalsIgnoreCase(criteria.getOperation())) {
                return path.loe(date);
            }
        }

        LOGGER.error("Predicate :: getDatePredicate() : Unable to parse date value of {}", criteria.getKey());
        throw new CustomBadRequestException("Unable to parse date value of " + criteria.getKey());
    }

    private BooleanExpression getNumericPredicate(PathBuilder<?> entityPath) {
        if (NumberUtils.isNumber(criteria.getValue().toString())) {
            NumberPath<BigDecimal> path = entityPath.getNumber(criteria.getKey(), BigDecimal.class);
            BigDecimal value = new BigDecimal(criteria.getValue().toString());
            if (":".equalsIgnoreCase(criteria.getOperation())) {
                return path.eq(value);
            } else if (">".equalsIgnoreCase(criteria.getOperation())) {
                return path.goe(value);
            } else if ("<".equalsIgnoreCase(criteria.getOperation())) {
                return path.loe(value);
            }
        }

        LOGGER.error("Predicate :: getNumericPredicate() : Unable to parse numeric value of {}", criteria.getKey());
        throw new CustomBadRequestException("Unable to parse numeric value of " + criteria.getKey());
    }

    private BooleanExpression getLongNumericPredicate(PathBuilder<?> entityPath) {
        if (NumberUtils.isNumber(criteria.getValue().toString())) {
            NumberPath<Long> path = entityPath.getNumber(criteria.getKey(), Long.class);
            Long value = new Long(criteria.getValue().toString());
            if (":".equalsIgnoreCase(criteria.getOperation())) {
                return path.eq(value);
            } else if (">".equalsIgnoreCase(criteria.getOperation())) {
                return path.goe(value);
            } else if ("<".equalsIgnoreCase(criteria.getOperation())) {
                return path.loe(value);
            }
        }

        LOGGER.error("Predicate :: getLongNumericPredicate() :  Unable to parse numeric value of {}", criteria.getKey());
        throw new CustomBadRequestException("Unable to parse numeric value of " + criteria.getKey());
    }

    private BooleanExpression getStringPredicate(PathBuilder<?> entityPath) {
        StringPath path = entityPath.getString(criteria.getKey());
        if (criteria.getValue().toString().matches(QueryDSLUtil.WORD_LIST_REGEX)) {
            List<String> values = getStringListFromCriteria();
            return path.in(values);
        } else {
            if (":".equalsIgnoreCase(criteria.getOperation())) {
                if ("status".equals(criteria.getKey())) {
                    return path.eq(criteria.getValue().toString());
                } else {
                    return path.containsIgnoreCase(criteria.getValue().toString());
                }
            }

            LOGGER.error("Predicate :: getStringPredicate() :  Unable to parse string value of {}", criteria.getKey());
        }
        throw new CustomBadRequestException("Unable to parse string value of " + criteria.getKey());
    }

    private static Date isValidDate(String date) {
        try {
            DateFormat df = new SimpleDateFormat(DATEFORMAT);
            df.setLenient(false);
            return df.parse(date);
        } catch (ParseException e) {
            LOGGER.error("Predicate :: isValidDate() :  Unable to parse date value");
            return null;
        }
    }

}
