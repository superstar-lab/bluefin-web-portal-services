package com.mcmcg.ico.bluefin.service.util;

import java.util.ArrayList;
import java.util.List;

import com.mcmcg.ico.bluefin.rest.resource.SearchCriteria;
import com.mysema.query.types.expr.BooleanExpression;

public class TransactionPredicatesBuilder {
    private List<SearchCriteria> params;

    public TransactionPredicatesBuilder() {
        params = new ArrayList<SearchCriteria>();
    }

    public TransactionPredicatesBuilder with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public BooleanExpression build() {
        if (params.size() == 0) {
            return null;
        }

        List<BooleanExpression> predicates = new ArrayList<BooleanExpression>();
        TransactionPredicate predicate;
        for (SearchCriteria param : params) {
            predicate = new TransactionPredicate(param);
            BooleanExpression exp = predicate.getPredicate();
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