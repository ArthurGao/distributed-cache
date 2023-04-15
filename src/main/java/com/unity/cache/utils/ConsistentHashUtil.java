package com.unity.cache.utils;

import com.unity.cache.exceptions.InternalException;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class ConsistentHashUtil {

    /**
     * Binary Search to find the closest hash value in the list
     */
    public static int binarySearch(List<Double> hashValues, double hash) {
        int low = 0;
        int high = hashValues.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midValue = hashValues.get(mid);
            if (Double.compare(midValue, hash) == 0) {
                return mid;
            } else if (midValue < hash) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        // If the hash is not in the list, return the index of the closest value
        if (high < 0) {
            return 0;
        } else if (low >= hashValues.size()) {
            return hashValues.size() - 1;
        } else {
            double lowValue = hashValues.get(high);
            double highValue = hashValues.get(low);
            return (hash - lowValue < highValue - hash) ? high : low;
        }
    }

    /**
     * Get the hash value of the object which is in range of [0, 1)
     */
    public static double myHash(Serializable obj) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(SerializationUtils.serialize(obj));
            BigInteger bigInt = new BigInteger(1, bytes);
            return bigInt.doubleValue() % 1000000 / 1000000.0;
        } catch (NoSuchAlgorithmException e) {
            throw new InternalException("Exception occurs when get hash: ", e);
        }
    }
}
