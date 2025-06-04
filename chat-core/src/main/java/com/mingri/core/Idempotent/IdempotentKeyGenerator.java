package com.mingri.core.Idempotent;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class IdempotentKeyGenerator {

    public String generateKey(Method method, Object[] args, String keySpel, String[] paramNames) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        Expression expression = parser.parseExpression(keySpel);
        return expression.getValue(context, String.class);
    }
}