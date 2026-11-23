package implementations;

import java.util.Arrays;
import java.util.NoSuchElementException;
import utilities.Iterator;
import utilities.ListADT;

/**
 * A dynamic array-based implementation of the {@link ListADT} interface,
 * for use in CPRG 304 at SAIT.
 * <p>
 * This class stores {@link String} elements in an internally managed array
 * that grows automatically when capacity is reached. The growth amount is
 * determined by a configurable growth rate expressed as a decimal percentage
 * of the current array length (e.g., {@code 0.25} means 25% growth).
 * All index parameters are zero-based unless otherwise noted.
 * </p>
 *
 * <p><strong>Design Note – String-Specific Implementation:</strong><br>
 * This class is intentionally designed to store {@link String} elements only.
 * A fully reusable, type-safe design would use Java generics (e.g.,
 * {@code MyArrayList<E>}), allowing any object type to be stored in the list.
 * Generic programming is outside the scope of CPRG 304 and has therefore
 * been omitted. All element fields, parameters, and return types are fixed
 * as {@link String} throughout this class and its implementations.
 * </p>
 */
public class MyArrayList implements ListADT
{
    private String[] data;
    private int size;
    private final double growthRate;

    /** Creates an empty list with room for 10 strings and 25% growth. */
    public MyArrayList() {
        this(10, 0.25);
    }

    /**
     * Creates an empty list with the specified capacity and growth rate.
     *
     * @param capacity initial number of available slots
     * @param growthRate fraction of current capacity added when full
     * @throws IllegalArgumentException if capacity is negative or the
     *         growth rate is not finite and positive
     */
    public MyArrayList(int capacity, double growthRate) {
        if (capacity < 0 || !Double.isFinite(growthRate) || growthRate <= 0) {
            throw new IllegalArgumentException("Invalid capacity or growth rate.");
        }

        data = new String[capacity];
        this.growthRate = growthRate;
    }

    /** Makes room for one additional element when the array is full. */
    private void ensureCapacity() {
        if (size == data.length) {
            int extra = Math.max(1, (int) (data.length * growthRate));
            int capacity = (int) Math.min(Integer.MAX_VALUE, (long) size + extra);
            data = Arrays.copyOf(data, capacity);
        }
    }

    /** Checks that the index refers to an existing element. */
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
    }

    /** Rejects null strings before a list operation. */
    private void checkElement(String element) {
        if (element == null) {
            throw new NullPointerException("List elements cannot be null.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return size;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        while (size > 0) {
            data[--size] = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(String toAdd) {
        return add(size, toAdd);
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(int index, String toAdd) {
        checkElement(toAdd);

        // Inserting at size appends the element.
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(index);
        }

        ensureCapacity();

        // Move backward so each value is copied before its slot is overwritten.
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }

        data[index] = toAdd;
        size++;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(ListADT toAdd) {
        Iterator source = toAdd.iterator();
        int originalSize = size;

        while (source.hasNext()) {
            add(source.next());
        }

        return size != originalSize;
    }

    /** {@inheritDoc} */
    @Override
    public String get(int index) {
        checkIndex(index);
        return data[index];
    }

    /** {@inheritDoc} */
    @Override
    public String set(int index, String toChange) {
        checkElement(toChange);
        String previous = get(index);
        data[index] = toChange;
        return previous;
    }

    /** {@inheritDoc} */
    @Override
    public String remove(int index) {
        String removed = get(index);

        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }

        size--;
        data[size] = null;
        return removed;
    }

    /** {@inheritDoc} */
    @Override
    public String remove(String toRemove) {
        int index = indexOf(toRemove);

        if (index == -1) {
            return null;
        }

        return remove(index);
    }

    /** {@inheritDoc} */
    @Override
    public int indexOf(String toFind) {
        checkElement(toFind);

        for (int i = 0; i < size; i++) {
            if (data[i].equals(toFind)) {
                return i;
            }
        }

        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(String toFind) {
        return indexOf(toFind) != -1;
    }

    /** {@inheritDoc} */
    @Override
    public String[] toArray(String[] toHold) {
        if (toHold.length < size) {
            return Arrays.copyOf(data, size);
        }

        System.arraycopy(data, 0, toHold, 0, size);
        return toHold;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator iterator() {
        return new ListIterator();
    }

    /** Traverses the strings present when this iterator was created. */
    private class ListIterator implements Iterator {
        private final String[] snapshot = Arrays.copyOf(data, size);
        private int position;

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return position < snapshot.length;
        }

        /** {@inheritDoc} */
        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return snapshot[position++];
        }
    }
}