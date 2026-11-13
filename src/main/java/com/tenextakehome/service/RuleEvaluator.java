package com.tenextakehome.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tenextakehome.service.filter.EqualFilter;
import com.tenextakehome.service.filter.Filter;
import com.tenextakehome.service.filter.FilterOperator;
import com.tenextakehome.service.filter.GreaterThanFilter;
import com.tenextakehome.service.filter.LessThanFilter;
import com.tenextakehome.service.filter.NotEqualFilter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RuleEvaluator {

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(AND|OR|EQUAL|NOT_EQUAL|LESS_THAN|GREATER_THAN|!=|=|<|>|"
                    + "[A-Za-z_@][A-Za-z0-9_.@-]*|"
                    + "\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|"
                    + "[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)|\\S+)",
            Pattern.CASE_INSENSITIVE);
    private static final Map<FilterOperator, Filter> FILTERS = createFilters();

    private RuleEvaluator() {
    }

    /**
     * Compiles an AND/OR expression into a predicate for parsed JSON log objects.
     * Supported comparisons are =, !=, <, >, and their FilterOperator names.
     */
    public static Predicate<JsonNode> evaluate(String ruleLogic) {
        if (ruleLogic == null || ruleLogic.isBlank()) {
            throw new IllegalArgumentException("Rule logic must not be null or blank");
        }
        return new ExpressionParser(tokenize(ruleLogic)).parse();
    }

    private static Map<FilterOperator, Filter> createFilters() {
        Map<FilterOperator, Filter> filters = new EnumMap<>(FilterOperator.class);
        for (Filter filter : List.of(
                new EqualFilter(),
                new NotEqualFilter(),
                new LessThanFilter(),
                new GreaterThanFilter())) {
            filters.put(filter.getOperator(), filter);
        }
        return Map.copyOf(filters);
    }

    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(expression);
        int position = 0;
        while (position < expression.length()) {
            matcher.region(position, expression.length());
            if (!matcher.lookingAt()) {
                throw new IllegalArgumentException("Invalid rule logic near: " + expression.substring(position));
            }
            tokens.add(matcher.group().trim());
            position = matcher.end();
        }
        return tokens;
    }

    private static Object resolveValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.decimalValue();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isTextual()) {
            return value.textValue();
        }
        return value;
    }

    private static Object parseLiteral(String token) {
        if ((token.startsWith("\"") && token.endsWith("\""))
                || (token.startsWith("'") && token.endsWith("'"))) {
            return token.substring(1, token.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\'", "'")
                    .replace("\\\\", "\\");
        }
        if ("null".equalsIgnoreCase(token)) {
            return null;
        }
        if ("true".equalsIgnoreCase(token) || "false".equalsIgnoreCase(token)) {
            return Boolean.parseBoolean(token);
        }
        try {
            return new BigDecimal(token);
        } catch (NumberFormatException ignored) {
            return token;
        }
    }

    private static FilterOperator parseOperator(String token) {
        return switch (token.toUpperCase(Locale.ROOT)) {
            case "=" -> FilterOperator.EQUAL;
            case "!=" -> FilterOperator.NOT_EQUAL;
            case "<" -> FilterOperator.LESS_THAN;
            case ">" -> FilterOperator.GREATER_THAN;
            default -> {
                try {
                    yield FilterOperator.valueOf(token.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Unsupported filter operator: " + token, exception);
                }
            }
        };
    }

    private static JsonNode getField(JsonNode log, String fieldPath) {
        JsonNode value = log;
        for (String segment : fieldPath.split("\\.")) {
            if (!(value instanceof ObjectNode objectNode)) {
                return null;
            }
            value = objectNode.get(segment);
            if (value == null) {
                return null;
            }
        }
        return value;
    }

    private static final class ExpressionParser {

        private final List<String> tokens;
        private int position;

        private ExpressionParser(List<String> tokens) {
            this.tokens = tokens;
        }

        private Predicate<JsonNode> parse() {
            Predicate<JsonNode> predicate = parseOrExpression();
            if (position != tokens.size()) {
                throw new IllegalArgumentException("Unexpected token in rule logic: " + tokens.get(position));
            }
            return predicate;
        }

        private Predicate<JsonNode> parseOrExpression() {
            Predicate<JsonNode> predicate = parseAndExpression();
            while (match("OR")) {
                Predicate<JsonNode> right = parseAndExpression();
                predicate = predicate.or(right);
            }
            return predicate;
        }

        private Predicate<JsonNode> parseAndExpression() {
            Predicate<JsonNode> predicate = parseCondition();
            while (match("AND")) {
                Predicate<JsonNode> right = parseCondition();
                predicate = predicate.and(right);
            }
            return predicate;
        }

        private Predicate<JsonNode> parseCondition() {
            String field = consume("Expected a field name");
            String operatorToken = consume("Expected a comparison operator after field " + field);
            FilterOperator operator = parseOperator(operatorToken);
            String expectedToken = consume("Expected a comparison value after " + operatorToken);
            Object expectedValue = parseLiteral(expectedToken);
            Filter filter = FILTERS.get(operator);
            return log -> filter.execute(resolveValue(getField(log, field)), expectedValue);
        }

        private boolean match(String expected) {
            if (position < tokens.size() && expected.equalsIgnoreCase(tokens.get(position))) {
                position++;
                return true;
            }
            return false;
        }

        private String consume(String errorMessage) {
            if (position >= tokens.size()) {
                throw new IllegalArgumentException(errorMessage);
            }
            return tokens.get(position++);
        }
    }
}
