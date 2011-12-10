package fuzzy.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.math.BigInteger;

public interface NodeInterface extends Remote{
	public BigInteger getID() throws RemoteException;
	public NodeInterface getSuccessor() throws RemoteException;
	public NodeInterface getPredecessor() throws RemoteException;
	public void setPredecessor(NodeInterface pred) throws RemoteException;
	public NodeInterface find_successor(BigInteger id) throws RemoteException;
	public void join(NodeInterface nLinha) throws RemoteException;
	public void stabilize() throws RemoteException;
	public void fix_fingers() throws RemoteException;
	public void check_predecessor() throws RemoteException;
	public void create() throws RemoteException;
	public void notifySuccessor(NodeInterface nLinha) throws RemoteException;
	public Set<String> lookup(BigInteger hash) throws RemoteException;
	public void insert(BigInteger hash, String ip) throws RemoteException;
}
