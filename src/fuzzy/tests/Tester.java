package fuzzy.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import fuzzy.service.Client;
import fuzzy.service.FuzzySearch;
import fuzzy.service.Result;

public class Tester {

	public static void main(String[] args) throws IOException, InterruptedException{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<Client> clients = new ArrayList<Client>();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		String line;

		int i=0,x=-1;
		do{
			System.out.println("\nWhat do you want? (add | print X | insert Phrase IP | lookup Phrase | kill X | exit)");
			line = in.readLine();
			if(line.startsWith("add")){
				clients.add(new Client("127.0.0.1",1099));
				threads.add(new Thread(clients.get(i)));
				threads.get(i++).start();
				Thread.sleep(3000);
			}else if(line.startsWith("print ")){
				x = Integer.valueOf(line.substring(6));
				System.out.println("---------- Node "+x+" ----------"+
						"\nKey: "+clients.get(x).node.getID()+
						"\nSuc: "+clients.get(x).node.getSuccessor().getID().toString()+
						"\nPre: "+(clients.get(x).node.getPredecessor()==null?"(null)":clients.get(x).node.getPredecessor().getID().toString())
						);//+"\nNext: "+clients.get(x).node.next+"\n");
			}else if(line.startsWith("printAll")){
				for(x=0; x<clients.size(); x++){
					System.out.println("---------- Node "+x+" ----------"+
							"\nKey: "+clients.get(x).node.getID()+
							"\nSuc: "+clients.get(x).node.getSuccessor().getID().toString()+
							"\nPre: "+(clients.get(x).node.getPredecessor()==null?"(null)":clients.get(x).node.getPredecessor().getID().toString())
							);//+"\nNext: "+clients.get(x).node.next+"\n");
				}
			}else if(line.startsWith("insert ")){
				if(clients.size()==0) continue;
				line = line.substring(7);
				
				FuzzySearch.insert(clients.get(0).node, line);
				
			}else if(line.startsWith("lookup ")){
				if(clients.size()==0) continue;
				line = line.substring(7);
				
				for(Result r : FuzzySearch.lookup(clients.get(0).node, line))
					System.out.println(r.getIp());
				
			}else if(line.startsWith("kill ")){
				x = Integer.valueOf(line.substring(5));
				clients.remove(x);
				threads.get(x).interrupt();
				threads.remove(x);
			}else if(line.startsWith("exit")) System.exit(1);
		}while(true);
	}
	
}