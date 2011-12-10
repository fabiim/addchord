package fuzzy.service;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.net.InetAddress;
import java.net.UnknownHostException;

import fuzzy.chord.Node;
import fuzzy.interfaces.NodeInterface;

public class Client implements Runnable{
	
	private String rmiAddress="127.0.0.1";
	private int rmiPort=1099;
	public NodeInterface node=null;
	private Timer timer;
	
	public Client(String rmiAddress, int rmiPort){
		this.rmiAddress=rmiAddress;
		this.rmiPort=rmiPort;
		timer = new Timer();
	}

	@Override
	public void run(){
		
		try{
			// I am a new node
			node = new Node(InetAddress.getLocalHost().toString()+System.currentTimeMillis()+Thread.currentThread()+System.currentTimeMillis()); 

			// Get RMI registry
			Registry registry = LocateRegistry.getRegistry(rmiAddress,rmiPort);

			try {
				// Look for the Adam node
				NodeInterface adam = (NodeInterface)registry.lookup("Adam");
				UnicastRemoteObject.exportObject(node,rmiPort);
				
				// Ask Adam to let me into the party
				node.join(adam);
				//System.out.println("Behold for I am... Ooooops... I am nobody...");
			} catch (NotBoundException e) {
				// If Adam is not present... guess what? I am Adam!
				// Bind my node's stub in the registry
				NodeInterface stub = (NodeInterface)UnicastRemoteObject.exportObject(node,rmiPort);
				registry.rebind("Adam", stub);
				node.create();
				//System.out.println("Behold, for I am Adam! I was formed out of dust, and dust I shall become!");
			}
		}catch(RemoteException e){
			System.err.println("Registry not found, creating my own...");
			try {
				Registry registry = LocateRegistry.createRegistry(rmiPort);
				// Bind my node's stub in the registry
				NodeInterface stub = (NodeInterface)UnicastRemoteObject.exportObject(node,rmiPort);
				registry.rebind("Adam", stub);
				node.create();
				//System.out.println("Behold, for I am Adam! I was formed out of dust, and dust I shall become!");			
			} catch (RemoteException e1) {
				System.err.println("Could not create registry: "+e1.getMessage());
				e1.printStackTrace(System.err);
			}   
		}catch (UnknownHostException e){
			System.err.println("Unknown Host: "+e.getMessage());
			e.printStackTrace(System.err);
		}

		timer.scheduleAtFixedRate(new StabilizeTask(node), 1500, 2000);
		timer.scheduleAtFixedRate(new FixFingersTask(node), 2000, 2000);
		timer.scheduleAtFixedRate(new CheckPredecessorTask(node), 2500, 2000);
	}
	
}