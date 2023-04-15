package com.unity.cache.utils;

import com.unity.cache.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases for {@link ConsistentHashUtilTest}
 * <p>
 * Test cases of {@link ConsistentHashUtilTest} are divided into 2 parts:
 * 1. Test binary search given no same input
 * 2. Test binary search given same input
 * 3. Test myHash
 * </p>
 */
class ConsistentHashUtilTest extends AbstractTest {

    @Test
    void testBinarySearch_givenNoSameInput_findNearestNode() {
        List<Double> input = new ArrayList<>();
        input.add(1.01);
        input.add(2.02);
        input.add(3.03);
        input.add(4.04);
        int index = ConsistentHashUtil.binarySearch(input, 2.32);
        assert index == 1;
        index = ConsistentHashUtil.binarySearch(input, 2.92);
        assert index == 2;
        index = ConsistentHashUtil.binarySearch(input, 1.01);
        assert index == 0;
        index = ConsistentHashUtil.binarySearch(input, 4.04);
        assert index == 3;
        index = ConsistentHashUtil.binarySearch(input, 0.01);
        assert index == 0;
        index = ConsistentHashUtil.binarySearch(input, 9);
        assert index == 3;
    }

    @Test
    void testBinarySearch_givenSameInput_findNearestNode() {
        List<Double> input = new ArrayList<>();
        input.add(1.01);
        input.add(1.01);
        input.add(3.03);

        int index = ConsistentHashUtil.binarySearch(input, 1.02);
        assert index == 1;
        index = ConsistentHashUtil.binarySearch(input, 2.92);
        assert index == 2;
    }


    @Test
    void testMyHash() {
        //Hash created should be in range[0, 1)
        double hash = ConsistentHashUtil.myHash(createObject(Integer.class));
        assertThat(hash).isPositive().isLessThan(1);

        hash = ConsistentHashUtil.myHash(createObject(Character.class));
        assertThat(hash).isLessThan(1).isPositive();

        hash = ConsistentHashUtil.myHash(createObject(String.class));
        assertThat(hash).isLessThan(1).isPositive();

        hash = ConsistentHashUtil.myHash(createObject(Double.class));
        assertThat(hash).isLessThan(1).isPositive();

        hash = ConsistentHashUtil.myHash(createObject(Float.class));
        assertThat(hash).isLessThan(1).isPositive();
    }
}
