package implementations;

import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import utilities.Iterator;
import utilities.StackADT;

/**
 * An array-backed implementation of the {@link StackADT} interface,
 * for use in CPRG 304 at SAIT.
 * <p>
 * This class delegates all storage to an internal {@link MyArrayList},
 * treating the end of the list as the top of the stack. {@link #push(String)}
 * appends to the end; {@link #pop()} and {@link #peek()} access the last
 * element. A stack follows a <strong>Last-In, First-Out (LIFO)</strong>
 * ordering policy: the last element pushed is the first one returned.
 * </p>
 *
 * <p><strong>Design Note – String-Specific Implementation:</strong><br>
 * This class is intentionally designed to store {@link String} elements only.
 * A fully reusable, type-safe design would use Java generics (e.g.,
 * {@code MyStack<E>}), allowing any object type to be stored in the stack.
 * Generic programming is outside the scope of CPRG 304 and has therefore
 * been omitted. All element fields, parameters, and return types are fixed
 * as {@link String} throughout this class and its implementations.
 * </p>
 */
public class MyStack implements StackADT
{
    private final MyArrayList items = new MyArrayList();

    /** {@inheritDoc} */
    @Override
    public void push(String toAdd) {
        items.add(toAdd);
    }

    /** Returns the top index or throws EmptyStackException for an empty stack. */
    private int topIndex() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }

        return size() - 1;
    }

    /** {@inheritDoc} */
    @Override
    public String pop() {
        return items.remove(topIndex());
    }

    /** {@inheritDoc} */
    @Override
    public String peek() {
        return items.get(topIndex());
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        items.clear();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return items.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(String toFind) {
        return items.contains(toFind);
    }

    /** {@inheritDoc} */
    @Override
    public int search(String toFind) {
        // Search from the top so the closest duplicate is found first.
        for (int i = size() - 1; i >= 0; i--) {
            if (items.get(i).equals(toFind)) {
                return size() - i;
            }
        }

        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public String[] toArray(String[] holder) {
        return items.toArray(holder);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator iterator() {
        return items.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(StackADT that) {
        if (that == null || size() != that.size()) {
            return false;
        }

        Iterator left = iterator();
        Iterator right = that.iterator();

        while (left.hasNext()) {
            if (!left.next().equals(right.next())) {
                return false;
            }
        }

        return true;
    }
}