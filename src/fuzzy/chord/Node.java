package fuzzy.chord;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import fuzzy.interfaces.NodeInterface;

public class Node implements NodeInterface, Serializable{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 4267268495928532566L;
	private NodeInterface successor;
	private NodeInterface predecessor;
	private BigInteger id;
	private NodeInterface[] fingers;
	private int next;
	private Map<BigInteger,Set<String>> hashes;

	public Node(String ip){
		// Create hash for the given IP address
		try {
			id = HashFunction.hash(ip.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			//System.err.println("Error creating new node for IP address:"+ip+" \n");
			e.printStackTrace(System.err);
		}

		// Allocate memory for the finger table
		fingers = new NodeInterface[HashFunction._m];
		
		// set index of the next finger to fix to 0 (the first in the array)
		next = 0;
		
		// Init hash table
		hashes = new HashMap<BigInteger,Set<String>>();
	}
	
	// ----- LOOKUPS
	
	public synchronized Set<String> lookup(BigInteger hash) throws RemoteException{
		
		if(idIsInInterval(hash,predecessor.getID(),id) || hash.compareTo(id)==0){
			if(hashes.containsKey(hash)) return(hashes.get(hash));
			else return null;
		}
		else return((find_successor(hash)).lookup(hash));
	}
	
	public synchronized void insert(BigInteger hash, String ip) throws RemoteException{
		
		if(idIsInInterval(hash,predecessor.getID(),id)){
			if(hashes.containsKey(hash))
				hashes.get(hash).add(ip);

			else{
				Set<String> ips = new TreeSet<String>();
				ips.add(ip);
				hashes.put(hash,ips);
			}
		}else{
			NodeInterface node = find_successor(hash);
			if(node.getID().equals(id)){
				if(hashes.containsKey(hash)){
					hashes.get(hash).add(ip);
				}
				else{
					Set<String> ips = new TreeSet<String>();
					ips.add(ip);
					hashes.put(hash,ips);
				}
			}else node.insert(hash, ip);
		}
	}
	
	// ----- CHORD

	// ask node n to find the successor of id 
	/* n.find_successor(id)
	 *  if (id Pertence (n, successor]) 
	 *		return successor;
	 *  else
	 *	 	n' = closest_preceding_node(id); 
	 *	 	return n'.find successor(id);
	 */
	@Override
	public synchronized NodeInterface find_successor(BigInteger id) throws RemoteException{
		//System.err.println("ID: "+id+" This.ID: "+this.id+" Succ.ID: "+successor.getID());
		if(idIsInInterval(id,this.id,successor.getID()) || id.compareTo(successor.getID())==0){
			//System.err.println("Est‡ no intervalo!");
			return(successor);
		}else{
			NodeInterface nLinha = closest_preceding_node(id);
			//System.err.println("Encontrar finger...");
			if(nLinha==this){
				//System.err.println("Oh shit...");
				if(successor.getID().equals(id)) return(this);
				else return(successor.find_successor(id));
			}
			else return(nLinha.find_successor(id));
		}
	}
	
	// search the local table for the highest predecessor of id
	/* n.closest_preceding_node(id)
	 *	for i = m downto 1
	 *		if (finger[i] Pertence (n, id))
	 *			return finger[i];
	 *	return n;
	 */
	private synchronized NodeInterface closest_preceding_node(BigInteger id) throws RemoteException{
		
		for(int i=HashFunction._m-1; i>-1; i--){
			if(fingers[i]==null) continue;
			if(idIsInInterval(fingers[i].getID(),this.id,id))
				return fingers[i];
		}
		//System.err.println("Returning this.");
		return(this);
	}
	
	// join a Chord ring containing node n'.
	/* n.join(n')
	/* 	predecessor = nil;
	 * 	successor = n'.find successor(n);
	 */
	@Override
	public synchronized void join(NodeInterface nLinha) throws RemoteException{
		setPredecessor(null);
		successor = nLinha.find_successor(id);
	}
	
	// create a new Chord ring.
	/* n.create()
	 *	predecessor = nil;
	 *	successor = n;
	 */
	@Override
	public synchronized void create(){
		setPredecessor(null);
		successor=this;
	}
	
	// called periodically. verifies nÕs immediate 
	// successor, and tells the successor about n.
	/* n.stabilize()
	 *	x = successor.predecessor;
	 *	if (x Pertence (n, successor))
	 *		successor = x;
	 *	successor.notify(n);
	 */
	@Override
	public synchronized void stabilize() throws RemoteException{
		NodeInterface x = successor.getPredecessor();
	
		if(x==null){
			//System.err.println("x==null");
			successor.notifySuccessor(this);
			return;
		}
		else if(idIsInInterval(x.getID(),this.id,successor.getID())){
			successor = x;
			//System.err.println("new sucessor of "+id+" is "+x.getID());
		}
		successor.notifySuccessor(this);
		//System.err.println("stabilize()");
	}
	
	// n' thinks it might be our predecessor.
	/* n.notify(n')
	 *	if (predecessor is nil or n' Pertence (predecessor, n)) 
	 *		predecessor = n';
	 */
	@Override
	public void notifySuccessor(NodeInterface nLinha) throws RemoteException{
		if(getPredecessor()==null || ( idIsInInterval(nLinha.getID(),getPredecessor().getID(),this.id) )){
			setPredecessor(nLinha);
			//System.err.println("new predecessor of "+id+" is "+predecessor.getID());
			//System.err.println("FODASSE o memaddress e: "+this.toString());
		}
	}
	
	// periodically refresh finger table entries.
	// next stores the index of the next finger to fix.
	/* n.fix_fingers()
	 * 	next = next + 1;
	 *  if (next > m)
	 * 		next = 1;
	 * 	finger[next] = find_successor(n + 2^(next-1) );
	 */
	@Override
	public synchronized void fix_fingers() throws RemoteException{
		BigInteger k = new BigInteger((new Integer(2)).toString().getBytes());
		BigInteger k2 = new BigInteger((new Integer(2)).toString().getBytes());
		
		if(next>=HashFunction._m) next=0;
		k = k.pow(next);
		k2  = k2.pow(HashFunction._m);
		fingers[next++] = find_successor(id.add(k).mod(k2));
		//System.err.println("fix_fingers()");
	}
	
	// called periodically. 
	// checks whether predecessor has failed.
	/*	n.check_predecessor()
	 *		if (predecessor has failed)
	 * 			predecessor = nil;
	 */
	@Override
	public synchronized void check_predecessor() throws RemoteException{
		//System.err.println("check_predecessor()");
		//TODO CRITICAL como saber se o predecessor falhou?!
		// Estabelecido em assembleia geral que os nossos nodos n‹o falham
	}
	
	// Checks if id is in the interval ]fromID,toID[
	// TODO tirar este synchronized rid’culo.
	private synchronized boolean idIsInInterval(BigInteger id, BigInteger fromID, BigInteger toID){
		
		if(id==null || fromID==null || toID == null) return(false);
		
		// both interval bounds are equal -> calculate out of equals
		if (fromID.equals(toID)) {
			// every ID is contained in the interval except of the two bounds
			//System.err.println("Limites do intervalo iguais!");
			return (!id.equals(fromID));
		}

		// interval does not cross zero -> compare with both bounds
		if (fromID.compareTo(toID) < 0) {
			//System.err.println("N‹o passa pelo 0.");
			//System.err.println("id.compareTo(fromID) > 0 && id.compareTo(toID) < 0 = "+(id.compareTo(fromID) > 0 && id.compareTo(toID) < 0));
			return (id.compareTo(fromID) > 0 && id.compareTo(toID) < 0);
		}

		// the hard part.
		// interval crosses zero -> split interval at zero
		// calculate min and max IDs
		BigInteger minID = new BigInteger(new Integer(0).toString().getBytes());
		BigInteger minusOne = new BigInteger(new Integer(-1).toString().getBytes());
		BigInteger maxID = new BigInteger(new Integer(2).toString().getBytes());
		maxID = maxID.pow(HashFunction._m).add(minusOne);
		
		// check both splitted intervals
				// first interval: ]fromID, maxID]
		return ((!fromID.equals(maxID) && id.compareTo(fromID) > 0 && id.compareTo(maxID) <= 0) ||
				// second interval: [minID, toID[
				(!minID.equals(toID) && id.compareTo(minID) >= 0 && id.compareTo(toID) < 0));
	}
	
	@Override
	public BigInteger getID() {
		return id;
	}

	@Override
	public NodeInterface getSuccessor() {
		return successor;
	}

	@Override
	public NodeInterface getPredecessor() {
		return predecessor;
	}
	
	@Override
	public synchronized void setPredecessor(NodeInterface pred){
		predecessor=pred;
	}
	
}