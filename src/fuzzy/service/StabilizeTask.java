package fuzzy.service;

import java.rmi.RemoteException;
import java.util.TimerTask;

import fuzzy.interfaces.NodeInterface;

public class StabilizeTask extends TimerTask{

	private NodeInterface node=null;
	
	public StabilizeTask(NodeInterface node){
		this.node = node;
	}
	
	@Override
	public void run() {
		try {
			node.stabilize();
			if(node.getPredecessor()==null){
		//		System.err.println("moment of null "+node.getID());
			}
		} catch (RemoteException e) {
			System.err.println("RemoteException on 'StabilizeTask': "+e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}
