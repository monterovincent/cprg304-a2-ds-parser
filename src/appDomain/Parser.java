package appDomain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import exceptions.EmptyQueueException;
import implementations.MyArrayList;
import implementations.MyQueue;
import implementations.MyStack;
import utilities.Iterator;
import utilities.ListADT;
import utilities.QueueADT;
import utilities.StackADT;


public class Parser
{
	/** Opening brackets, in the same order as CLOSERS so they pair up by index. */
	private static final String OPENERS = "({[<";

	/** Closing brackets, in the same order as OPENERS so they pair up by index. */
	private static final String CLOSERS = ")}]>";

	private StackADT stack;

	private QueueADT errorQ;

	private QueueADT extrasQ;

	private ListADT errors;

	private ListADT reported;

	/**
	 * Entry point. Expects the path of the file to check as the first argument.
	 */
	public static void main( String[] args )
	{
		if( args.length == 0 )
		{
			System.out.println( "Usage: java -jar Parser.jar <file>" );
			System.out.println( "Please give the name of the file to check." );
			return;
		}

		File file = new File( args[0] );
		if( !file.isFile() )
		{
			System.out.println( "File not found: " + args[0] );
			return;
		}

		Parser parser = new Parser();
		try
		{
			parser.parse( file );
		}
		catch( IOException e )
		{
			System.out.println( "Could not read file " + args[0] + ": " + e.getMessage() );
			return;
		}
		catch( EmptyQueueException e )
		{
			System.out.println( "Internal error while parsing " + args[0] + ": " + e.getMessage() );
			return;
		}
		parser.printReport( args[0] );
	}

	/**
	 * Creates a parser with an empty stack, empty queues and no errors.
	 */
	public Parser()
	{
		stack = new MyStack();
		errorQ = new MyQueue();
		extrasQ = new MyQueue();
		errors = new MyArrayList();
		reported = new MyArrayList();
	}

	/**
	 * Reads the file line by line and runs algorithm, then runs the end-of-file checks.
	 */
	public void parse( File file ) throws IOException, EmptyQueueException
	{
		try( BufferedReader reader = new BufferedReader( new FileReader( file ) ) )
		{
			String line;
			int lineNumber = 0;
			while( ( line = reader.readLine() ) != null )
			{
				lineNumber++;
				for( int i = 0; i < line.length(); i++ )
				{
					char c = line.charAt( i );
					if( isOpener( c ) )
					{
						stack.push( makeBracket( c, lineNumber ) );
					}
					else if( isCloser( c ) )
					{
						processCloser( makeBracket( c, lineNumber ) );
					}
				}
			}
		}
		finishDocument();
	}

	/**
	 * Handles one closing bracket using Kitty's algorithm.
	 *
	 * @param closer the closing bracket entry, e.g. ")12"
	 */
	private void processCloser( String closer ) throws EmptyQueueException
	{
		char symbol = symbolOf( closer );

		// Matches the top of the stack: a normal, correct pair.
		if( !stack.isEmpty() && matches( symbolOf( stack.peek() ), symbol ) )
		{
			stack.pop();
		}
		// Matches the head of errorQ: closes a bracket already set aside, so ignore it.
		else if( !errorQ.isEmpty() && matches( symbolOf( errorQ.peek() ), symbol ) )
		{
			// It has left errorQ, so it no longer needs guarding against a second report.
			reported.remove( errorQ.dequeue() );
		}
		// Nothing open at all.
		else if( stack.isEmpty() )
		{
			errorQ.enqueue( closer );
		}
		else
		{
			int matchIndex = findMatchInStack( symbol );
			if( matchIndex != -1 )
			{
				// Pop everything above the match into errorQ and report it as an
				// error, then pop the match itself.
				int toMove = stack.size() - 1 - matchIndex;
				for( int i = 0; i < toMove; i++ )
				{
					String popped = stack.pop();
					addError( popped );
					reported.add( popped );
					errorQ.enqueue( popped );
				}
				stack.pop();
			}
			else
			{
				extrasQ.enqueue( closer );
			}
		}
	}

	/**
	 * Searches the stack for the opening bracket that matches the given closing symbol.
	 */
	private int findMatchInStack( char closeSymbol )
	{
		int found = -1;
		int index = 0;
		Iterator it = stack.iterator();
		while( it.hasNext() )
		{
			if( matches( symbolOf( it.next() ), closeSymbol ) )
			{
				found = index;
			}
			index++;
		}
		return found;
	}

	/**
	 * Anything still on the stack goes
	 * to errorQ, then errorQ and extrasQ are compared: a pair that matches
	 * cancels out, everything else is an error.
	 */
	private void finishDocument() throws EmptyQueueException
	{
		while( !stack.isEmpty() )
		{
			errorQ.enqueue( stack.pop() );
		}

		while( !errorQ.isEmpty() || !extrasQ.isEmpty() )
		{
			if( errorQ.isEmpty() )
			{
				reportAtEnd( extrasQ.dequeue() );
			}
			else if( extrasQ.isEmpty() )
			{
				reportAtEnd( errorQ.dequeue() );
			}
			else if( matches( symbolOf( errorQ.peek() ), symbolOf( extrasQ.peek() ) ) )
			{
				errorQ.dequeue();
				extrasQ.dequeue();
			}
			else
			{
				reportAtEnd( errorQ.dequeue() );
			}
		}
	}

	/**
	 * Reports a bracket during the end-of-file step, unless it was already
	 * reported when it was popped into errorQ.
	 */
	private void reportAtEnd( String bracket )
	{
		if( reported.contains( bracket ) )
		{
			reported.remove( bracket );
			return;
		}
		addError( bracket );
	}

	/**
	 * Adds a bracket to the error list, keeping the list sorted by line number.
	 * Errors on the same line keep the order they were reported in.
	 */
	private void addError( String bracket )
	{
		int line = lineOf( bracket );
		int i = errors.size();
		while( i > 0 && lineOf( errors.get( i - 1 ) ) > line )
		{
			i--;
		}
		errors.add( i, bracket );
	}

	/**
	 * Prints the result: either a no-errors message or one line per error.
	 */
	public void printReport( String fileName )
	{
		if( errors.isEmpty() )
		{
			System.out.println( "File: " + fileName + " has no errors." );
			return;
		}

		System.out.println( "Errors in file " + fileName + ":" );
		Iterator it = errors.iterator();
		while( it.hasNext() )
		{
			String bracket = it.next();
			System.out.println( "Line " + lineOf( bracket ) + ": Syntax error on \"" + symbolOf( bracket ) + "\"." );
		}
	}

	/**
	 * Builds the String stored for a bracket: the symbol followed by its line number.
	 */
	private static String makeBracket( char symbol, int line )
	{
		return symbol + "" + line;
	}

	/**
	 * Reads the bracket symbol back out of an entry.
	 */
	private static char symbolOf( String bracket )
	{
		return bracket.charAt( 0 );
	}

	/**
	 * Reads the line number back out of an entry.
	 */
	private static int lineOf( String bracket )
	{
		return Integer.parseInt( bracket.substring( 1 ) );
	}

	
	private static boolean isOpener( char c )
	{
		return OPENERS.indexOf( c ) != -1;
	}

	
	private static boolean isCloser( char c )
	{
		return CLOSERS.indexOf( c ) != -1;
	}

	/**
	 * Checks whether an opening and a closing bracket form a pair.
	 */
	private static boolean matches( char open, char close )
	{
		int i = OPENERS.indexOf( open );
		return i != -1 && i == CLOSERS.indexOf( close );
	}
}
