package com.v7878.collections;

class ArraySupport {
    private static int growSize(int size) {
        return Math.max(size + 1, size * 2);
    }

    public static int[] insert(int[] array, int size, int index, int value) {
        assert size <= array.length;
        assert index <= size;
        if (size < array.length) {
            if (index < size) {
                System.arraycopy(array, index, array, index + 1, size - index);
            }
            array[index] = value;
            return array;
        }
        var new_array = new int[growSize(size)];
        System.arraycopy(array, 0, new_array, 0, index);
        if (index < size) {
            System.arraycopy(array, index, new_array, index + 1, size - index);
        }
        new_array[index] = value;
        return new_array;
    }

    public static Object[] insert(Object[] array, int size, int index, Object value) {
        assert size <= array.length;
        assert index <= size;
        if (size < array.length) {
            if (index < size) {
                System.arraycopy(array, index, array, index + 1, size - index);
            }
            array[index] = value;
            return array;
        }
        var new_array = new Object[growSize(size)];
        System.arraycopy(array, 0, new_array, 0, index);
        if (index < size) {
            System.arraycopy(array, index, new_array, index + 1, size - index);
        }
        new_array[index] = value;
        return new_array;
    }

    // Version without range checks
    public static int binarySearch(int[] a, int from, int to, int key) {
        int low = from;
        int high = to - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }
}
