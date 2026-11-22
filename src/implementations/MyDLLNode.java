package implementations;

/**
 * A single node in a doubly linked list (DLL), used as the building block
 * for {@link MyDLL} in CPRG 304 at SAIT.
 * <p>
 * Each node stores a {@link String} element and maintains two directional
 * references: a predecessor ({@code pred}) pointing to the node immediately
 * before it in the chain, and a successor ({@code succ}) pointing to the
 * node immediately after it. A {@code null} predecessor indicates the node
 * is the head of the list; a {@code null} successor indicates it is the tail.
 * </p>
 *
 * <p><strong>Design Note – String-Specific Implementation:</strong><br>
 * This class is intentionally designed to store {@link String} elements only.
 * A fully reusable, type-safe design would use Java generics (e.g.,
 * {@code DLLNode<E>}), allowing any object type to be stored in the node.
 * Generic programming is outside the scope of CPRG 304 and has therefore
 * been omitted. All element fields, parameters, and return types are fixed
 * as {@link String} throughout this class and its usages.
 * </p>
 */
public class MyDLLNode
{
	/** The string value stored in the node */
	private String element;
	
	/** Previous node in the chain; null if this node is the head */
	private MyDLLNode pred;
	
	/**Next node in the chain , null if this node is the tail */
	private MyDLLNode succ;
	
	/**
     * Creates an unlinked node holding the given element.
     *
     * @param element the String to store
     */
	
	public MyDLLNode(String element)
	{
		this.element = element;
		this.pred = null;
		this.succ = null;
	}
	
	/**
     * Creates a node holding the given element, already linked to its neighbours.
     *
     * @param element the String to store
     * @param pred    the node before this one, or null if this will be the head
     * @param succ    the node after this one, or null if this will be the tail
     */
	 public MyDLLNode(String element, MyDLLNode pred,MyDLLNode succ)
	 {
		 this.element = element;
		 this.pred = pred;
		 this.succ = succ;
	 }
	 
	 /** @return the String stored in this node */
	 
	 public String getElement()
	 {
		 return element;
	 }
	 /** @param element the new String to store in this node */
	 public void setElement(String element)
	 {
		 this.element = element;
	 }
	 
	 /** @return the previous node, or null if this node is the head */
	 
	 public MyDLLNode getPred()
	 {
		 return pred;
	 }
	 
	 /** @param pred the node to link before this one (null makes this the head) */
	 
	 public void setPred(MyDLLNode pred)
	 {
		 this.pred = pred;
	 }
	 /** @return the next node, or null if this node is the tail */
	 public MyDLLNode getSucc()
	 {
		 return succ;
	 }
	 /** @param succ the node to link after this one (null makes this the tail) */
	 
	 public void setSucc(MyDLLNode succ)
	 {
		 this.succ = succ;
	 }

}