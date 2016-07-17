package com.mcmcg.ico.bluefin.service.util.querydsl;

import java.util.ArrayList;
import java.util.List;

import com.mysema.query.types.expr.BooleanExpression;

class PredicatesBuilder {
    private List<SearchCriteria> params;

    public PredicatesBuilder() {
        params = new ArrayList<SearchCriteria>();
    }

    public PredicatesBuilder with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public BooleanExpression build(Class<?> entity) {
        if (params.size() == 0) {
            return null;
        }

        List<BooleanExpression> predicates = new ArrayList<BooleanExpression>();
        Predicate predicate;
        for (SearchCriteria param : params) {
            predicate = new Predicate(param);
            BooleanExpression exp = predicate.getPredicate(entity);
            if (exp != null) {
                predicates.add(exp);
            }
        }

        BooleanExpression result = predicates.get(0);
        for (int i = 1; i < predicates.size(); i++) {
            result = result.and(predicates.get(i));
        }
        return result;
    }
}