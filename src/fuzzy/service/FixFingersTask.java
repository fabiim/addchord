package fuzzy.service;

import java.rmi.RemoteException;
import java.util.TimerTask;

import fuzzy.interfaces.NodeInterface;

public class FixFingersTask extends TimerTask{

	private NodeInterface node=null;
	
	public FixFingersTask(NodeInterface node){
		this.node = node;
	}
	
	@Override
	public void run() {
		try {
			node.fix_fingers();
		} catch (RemoteException e) {
			System.err.println("RemoteException on 'FixFingersTask': "+e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	
}
