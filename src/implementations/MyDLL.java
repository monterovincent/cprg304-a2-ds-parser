package implementations;

import java.util.Arrays;
import java.util.NoSuchElementException;
import utilities.Iterator;
import utilities.ListADT;

/**
 * A doubly linked list (DLL) implementation of the {@link ListADT} interface,
 * for use in CPRG 304 at SAIT.
 * <p>
 * This class stores {@link String} elements in a chain of {@link MyDLLNode}
 * objects, each of which holds a reference to both its predecessor ({@code pred})
 * and its successor ({@code succ}). The list maintains explicit {@code head}
 * and {@code tail} references for O(1) access to both ends of the chain.
 * Insertions and removals at arbitrary positions require O(n) traversal.
 * All index parameters are zero-based unless otherwise noted.
 * </p>
 *
 * <p><strong>Design Note – String-Specific Implementation:</strong><br>
 * This class is intentionally designed to store {@link String} elements only.
 * A fully reusable, type-safe design would use Java generics (e.g.,
 * {@code MyDLL<E>}), allowing any object type to be stored in the list.
 * Generic programming is outside the scope of CPRG 304 and has therefore
 * been omitted. All element fields, parameters, and return types are fixed
 * as {@link String} throughout this class and its implementations.
 * </p>
 */
public class MyDLL implements ListADT
{
    /** First node in the list; null when the list is empty. */
    private MyDLLNode head;

    /** Last node in the list; null when the list is empty. */
    private MyDLLNode tail;

    /** Number of elements currently in the list. */
    private int size;

    /** Creates an empty list. */
    public MyDLL()
    {
        head = null;
        tail = null;
        size = 0;
    }

    // ---------------------------------------------------------------
    // Step 1: basics
    // ---------------------------------------------------------------

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public boolean isEmpty()
    {
        return size == 0;
    }

    @Override
    public void clear()
    {
        // no nodes are reachable once head and tail are gone; Java frees them
        head = null;
        tail = null;
        size = 0;
    }

    // ---------------------------------------------------------------
    // Step 2: add to the end, read by index
    // ---------------------------------------------------------------

    @Override
    public boolean add( String toAdd ) throws NullPointerException
    {
        if( toAdd == null )
        {
            throw new NullPointerException( "Cannot add a null element." );
        }

        // new node points back at the current tail; nothing after it
        MyDLLNode newNode = new MyDLLNode( toAdd, tail, null );

        if( tail == null )
        {
            head = newNode;            // list was empty: new node is also the head
        }
        else
        {
            tail.setSucc( newNode );   // old tail now points forward to it
        }

        tail = newNode;
        size++;
        return true;
    }

    @Override
    public String get( int index ) throws IndexOutOfBoundsException
    {
        checkIndex( index );
        return getNode( index ).getElement();
    }

    // ---------------------------------------------------------------
    // Step 3: insert at any position
    // ---------------------------------------------------------------

    @Override
    public boolean add( int index, String toAdd ) throws NullPointerException, IndexOutOfBoundsException
    {
        if( toAdd == null )
        {
            throw new NullPointerException( "Cannot add a null element." );
        }
        if( index < 0 || index > size )   // size itself is valid: it means append
        {
            throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
        }

        // at the end (includes an empty list): same as add(String)
        if( index == size )
        {
            return add( toAdd );
        }

        // at the front: new node becomes the head
        if( index == 0 )
        {
            MyDLLNode newNode = new MyDLLNode( toAdd, null, head );
            head.setPred( newNode );
            head = newNode;
            size++;
            return true;
        }

        // middle: slot the new node in front of the node currently at index
        MyDLLNode next = getNode( index );
        MyDLLNode prev = next.getPred();
        MyDLLNode newNode = new MyDLLNode( toAdd, prev, next );

        prev.setSucc( newNode );   // rewire both neighbours to point at it
        next.setPred( newNode );
        size++;
        return true;
    }

    // ---------------------------------------------------------------
    // Step 4: replace and remove by index
    // ---------------------------------------------------------------

    @Override
    public String set( int index, String toChange ) throws NullPointerException, IndexOutOfBoundsException
    {
        if( toChange == null )
        {
            throw new NullPointerException( "Cannot set a null element." );
        }
        checkIndex( index );

        // only the value changes; links stay as they are
        MyDLLNode node = getNode( index );
        String old = node.getElement();
        node.setElement( toChange );
        return old;
    }

    @Override
    public String remove( int index ) throws IndexOutOfBoundsException
    {
        checkIndex( index );
        return unlink( getNode( index ) );
    }

    // ---------------------------------------------------------------
    // Step 5: search by value
    // ---------------------------------------------------------------

    @Override
    public int indexOf( String toFind ) throws NullPointerException
    {
        if( toFind == null )
        {
            throw new NullPointerException( "Cannot search for a null element." );
        }

        // walk from the head; first match wins
        MyDLLNode current = head;
        int index = 0;
        while( current != null )
        {
            if( current.getElement().equals( toFind ) )   // compare text, not references
            {
                return index;
            }
            current = current.getSucc();
            index++;
        }
        return -1;
    }

    @Override
    public boolean contains( String toFind ) throws NullPointerException
    {
        return indexOf( toFind ) != -1;   // indexOf handles the null check
    }

    @Override
    public String remove( String toRemove ) throws NullPointerException
    {
        if( toRemove == null )
        {
            throw new NullPointerException( "Cannot remove a null element." );
        }

        // walk to the first match and unlink that node directly
        MyDLLNode current = head;
        while( current != null )
        {
            if( current.getElement().equals( toRemove ) )
            {
                return unlink( current );
            }
            current = current.getSucc();
        }
        return null;   // not found
    }

    // ---------------------------------------------------------------
    // Step 6: bulk add, copy out
    // ---------------------------------------------------------------

    @Override
    public boolean addAll( ListADT toAdd ) throws NullPointerException
    {
        if( toAdd == null )
        {
            throw new NullPointerException( "Cannot add from a null list." );
        }

        // the other list's iterator works on a copy, so addAll(this) can't loop forever
        Iterator it = toAdd.iterator();
        boolean changed = false;
        while( it.hasNext() )
        {
            add( it.next() );
            changed = true;
        }
        return changed;
    }

    @Override
    public String[] toArray( String[] toHold ) throws NullPointerException
    {
        if( toHold == null )
        {
            throw new NullPointerException( "Holder array cannot be null." );
        }

        // reuse the holder if it fits, otherwise make a new array of the right length
        String[] result = ( toHold.length >= size ) ? toHold : Arrays.copyOf( toHold, size );

        MyDLLNode current = head;
        for( int i = 0; i < size; i++ )
        {
            result[i] = current.getElement();
            current = current.getSucc();
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Step 7: iterator
    // ---------------------------------------------------------------

    @Override
    public Iterator iterator()
    {
        return new DLLIterator();
    }

    /**
     * Iterator over a snapshot of the list taken when the iterator is created.
     * Later changes to the list do not affect it, as the Iterator contract requires.
     */
    private class DLLIterator implements Iterator
    {
        /** This iterator's own copy of the elements, first to last. */
        private final String[] copy;

        /** Index of the next element to return. */
        private int position;

        private DLLIterator()
        {
            copy = toArray( new String[size] );   // exact-size array, filled from the nodes
            position = 0;
        }

        @Override
        public boolean hasNext()
        {
            return position < copy.length;
        }

        @Override
        public String next() throws NoSuchElementException
        {
            if( !hasNext() )
            {
                throw new NoSuchElementException( "No more elements." );
            }
            return copy[position++];
        }
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Throws if index isn't a valid element position (0 to size - 1).
     *
     * @param index the index to check
     * @throws IndexOutOfBoundsException if index is out of range
     */
    private void checkIndex( int index )
    {
        if( index < 0 || index >= size )
        {
            throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
        }
    }

    /**
     * Returns the node at index, walking from whichever end is closer.
     * The index must already be valid.
     *
     * @param index the position of the node
     * @return the node at that position
     */
    private MyDLLNode getNode( int index )
    {
        MyDLLNode current;

        if( index < size / 2 )
        {
            current = head;   // front half: walk forward
            for( int i = 0; i < index; i++ )
            {
                current = current.getSucc();
            }
        }
        else
        {
            current = tail;   // back half: walk backward
            for( int i = size - 1; i > index; i-- )
            {
                current = current.getPred();
            }
        }
        return current;
    }

    /**
     * Removes a node from the chain and returns its element.
     * Handles head, tail, middle and single-node cases with the same code.
     *
     * @param node the node to remove; must be in this list
     * @return the removed element
     */
    private String unlink( MyDLLNode node )
    {
        MyDLLNode prev = node.getPred();
        MyDLLNode next = node.getSucc();

        if( prev == null )
        {
            head = next;            // removing the head
        }
        else
        {
            prev.setSucc( next );   // skip over the node going forward
        }

        if( next == null )
        {
            tail = prev;            // removing the tail
        }
        else
        {
            next.setPred( prev );   // skip over the node going backward
        }

        // detach the removed node so it holds no stale links
        node.setPred( null );
        node.setSucc( null );

        size--;
        return node.getElement();
    }
}
