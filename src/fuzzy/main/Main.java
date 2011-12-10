package fuzzy.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Set;

import fuzzy.service.Client;
import fuzzy.service.FuzzySearch;
import fuzzy.service.Result;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String rmiAddress=null;
		int rmiPort=1099;
		
		// Validate input
		if(!(args.length>1)){
			System.err.println("Arguments: 'RMI registry address' 'RMI registry port'");
			System.exit(-1);
		}
		if(!(args[0].matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}"))){
			System.err.println("Arguments: 'RMI registry address' 'RMI registry port'");
			System.exit(-1);
		}
		try{
			rmiPort=Integer.valueOf(args[1]);
		}catch(NumberFormatException n){
			System.err.println("Arguments: 'RMI registry address' 'RMI registry port'");
			System.exit(-1);
		}

		rmiAddress = args[0];
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line=null;
		Client c = new Client(rmiAddress,rmiPort);
		Thread client = new Thread(c);
		Set<Result> results=null;
		
		client.start();
		
		do{
			System.out.println("\nWhat do you want? (insert Phrase IP | lookup Phrase | exit)");
			
			try {
				line = in.readLine();
			} catch (IOException e1) {
				System.err.println("IOException: "+e1.getMessage());
				e1.printStackTrace(System.err);
			}
			
			if(line.startsWith("insert ")){
				line = line.substring(7);
				
				try {
					FuzzySearch.insert(c.node, line);
				} catch (RemoteException e) {
					System.err.println("Could contact host: "+e.getMessage());
					e.printStackTrace(System.err);
				}
				
			}else if(line.startsWith("lookup ")){
				line = line.substring(7);
				
				try {
					results = FuzzySearch.lookup(c.node, line);
				} catch (RemoteException e) {
					System.err.println("Could contact host: "+e.getMessage());
					e.printStackTrace(System.err);
				}
				
				System.out.println("\nResults: ");
				for(Result r : results)
					System.out.println(r.getIp());	
			}else if(line.startsWith("exit")) System.exit(1);
		}while(true);
	}
	
}
