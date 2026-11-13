package com.tenextakehome.service.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterTest {

    @Test
    void equalFilterComparesNumbersByValue() {
        assertTrue(new EqualFilter().execute(1, 1.0));
    }

    @Test
    void notEqualFilterHandlesNullValues() {
        assertFalse(new NotEqualFilter().execute(null, null));
        assertTrue(new NotEqualFilter().execute(null, "value"));
    }

    @Test
    void lessThanFilterComparesNumbersAndStrings() {
        assertTrue(new LessThanFilter().execute(1, 2L));
        assertTrue(new LessThanFilter().execute("alpha", "beta"));
    }

    @Test
    void greaterThanFilterComparesNumbers() {
        assertTrue(new GreaterThanFilter().execute(3.5, 3));
    }

    @Test
    void orderingFilterRejectsIncompatibleTypes() {
        assertThrows(IllegalArgumentException.class, () -> new LessThanFilter().execute(1, "2"));
    }
}
