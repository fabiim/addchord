package fuzzy.service;

import java.rmi.RemoteException;
import java.util.TimerTask;

import fuzzy.interfaces.NodeInterface;

public class CheckPredecessorTask extends TimerTask{

	private NodeInterface node=null;
	
	public CheckPredecessorTask(NodeInterface node){
		this.node = node;
	}
	
	@Override
	public void run() {
		try {
			node.check_predecessor();
		} catch (RemoteException e) {
			System.err.println("RemoteException on 'CheckPredecessorTask': "+e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	
}
