package implementations;

import exceptions.EmptyQueueException;
import utilities.Iterator;
import utilities.QueueADT;

/**
 * A doubly linked list-backed implementation of the {@link QueueADT} interface,
 * for use in CPRG 304 at SAIT.
 * <p>
 * This class delegates all storage to an internal {@link MyDLL}, treating
 * index {@code 0} as the front of the queue and the last index as the back.
 * {@link #enqueue(String)} appends to the back; {@link #dequeue()} and
 * {@link #peek()} access the front. A queue follows a
 * <strong>First-In, First-Out (FIFO)</strong> ordering policy: the first
 * element enqueued is the first one returned.
 * </p>
 *
 * <p><strong>Design Note – String-Specific Implementation:</strong><br>
 * This class is intentionally designed to store {@link String} elements only.
 * A fully reusable, type-safe design would use Java generics (e.g.,
 * {@code MyQueue<E>}), allowing any object type to be stored in the queue.
 * Generic programming is outside the scope of CPRG 304 and has therefore
 * been omitted. All element fields, parameters, and return types are fixed
 * as {@link String} throughout this class and its implementations.
 * </p>
 */
public class MyQueue implements QueueADT
{
    /** Storage for the queue: index 0 is the front, the last index is the back. */
    private MyDLL list;

    /** Creates an empty queue. */
    public MyQueue()
    {
        list = new MyDLL();
    }

    // ---------------------------------------------------------------
    // Core queue operations
    // ---------------------------------------------------------------

    @Override
    public void enqueue( String toAdd ) throws NullPointerException
    {
        if( toAdd == null )
        {
            throw new NullPointerException( "Cannot enqueue a null element." );
        }
        list.add( toAdd );   // join at the back (MyDLL's tail, O(1))
    }

    @Override
    public String dequeue() throws EmptyQueueException
    {
        // check first, so MyDLL's IndexOutOfBoundsException never leaks out
        if( list.isEmpty() )
        {
            throw new EmptyQueueException( "Cannot dequeue from an empty queue." );
        }
        return list.remove( 0 );   // leave from the front (MyDLL's head, O(1))
    }

    @Override
    public String peek() throws EmptyQueueException
    {
        if( list.isEmpty() )
        {
            throw new EmptyQueueException( "Cannot peek at an empty queue." );
        }
        return list.get( 0 );   // read the front without removing it
    }

    @Override
    public void dequeueAll()
    {
        list.clear();
    }

    // ---------------------------------------------------------------
    // State and searching
    // ---------------------------------------------------------------

    @Override
    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    @Override
    public int size()
    {
        return list.size();
    }

    @Override
    public boolean contains( String toFind ) throws NullPointerException
    {
        return list.contains( toFind );   // MyDLL throws NullPointerException for null
    }

    @Override
    public int search( String toFind )
    {
        // QueueADT declares no exception for null, so treat it as "not found"
        if( toFind == null )
        {
            return -1;
        }

        int index = list.indexOf( toFind );   // 0-based, nearest the front
        if( index == -1 )
        {
            return -1;   // check first: -1 + 1 would wrongly give 0
        }
        return index + 1;   // QueueADT positions are 1-based (front = 1)
    }

    // ---------------------------------------------------------------
    // Traversal, comparison, copying
    // ---------------------------------------------------------------

    @Override
    public Iterator iterator()
    {
        return list.iterator();   // front to back is the list's own order
    }

    @Override
    public boolean equals( QueueADT that )
    {
        if( that == null )
        {
            return false;
        }
        if( this == that )
        {
            return true;   // same object
        }
        if( this.size() != that.size() )
        {
            return false;   // different sizes can't match
        }

        // same size: compare element by element, front to back
        Iterator mine = this.iterator();
        Iterator theirs = that.iterator();
        while( mine.hasNext() )
        {
            if( !mine.next().equals( theirs.next() ) )
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String[] toArray( String[] holder ) throws NullPointerException
    {
        return list.toArray( holder );   // MyDLL handles the null check and sizing
    }
}
