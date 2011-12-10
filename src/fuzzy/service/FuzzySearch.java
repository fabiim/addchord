package fuzzy.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import fuzzy.chord.HashFunction;
import fuzzy.interfaces.NodeInterface;

public abstract class FuzzySearch {
	
	public static void insert(NodeInterface node, String phrase) throws RemoteException{
		String[] splited = phrase.split(" ");
		List<String> sequences = new ArrayList<String>();
		for(int x=0; x<splited.length; x++){
			if(splited[x].length()<3) continue;
			for(int c=splited[x].length(); c>2; c--)
				sequences.add(splited[x].substring(0, c));
		}
		
		String localAddress = null;
		
		try {
			localAddress = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			System.err.println("Failed to get local address: "+e.getMessage());
			e.printStackTrace(System.err);
			return;
		}
		
		for(String seq : sequences)
			node.insert(HashFunction.hash(seq.getBytes()), localAddress);
			//node.insert(HashFunction.hash(seq.getBytes()), splited[splited.length-1]);
	}
	
	public static Set<Result> lookup(NodeInterface node, String phrase) throws RemoteException{
		String[] splited = phrase.split(" ");
		Set<String> ips;
		Map<String,Set<String>> resultSet = null;
		Map<String,Set<String>> wordAndSequences = new TreeMap<String,Set<String>>();
		Set<String> sequences=null;
		Map<String,Map<String,Set<String>>> megaMap = new TreeMap<String,Map<String,Set<String>>>();
		
		for(int x=0; x<splited.length; x++){
			if(splited[x].length()<3) continue;
			sequences = new TreeSet<String>();
			for(int c=splited[x].length(); c>2; c--){
				sequences.add(splited[x].substring(0, c));
			}
			wordAndSequences.put(splited[x], sequences);
		}
		
		for(Map.Entry<String,Set<String>> e : wordAndSequences.entrySet()){
			resultSet = new TreeMap<String,Set<String>>();
			for(String seq : e.getValue()){
				ips = node.lookup(HashFunction.hash(seq.getBytes()));
				resultSet.put(seq,ips);
			}
			megaMap.put(e.getKey(), resultSet);
		}
		
		// Tratamento da resposta

		Map<String,Integer> ranks = new TreeMap<String,Integer>();
		Set<Result> sortedRanks = new TreeSet<Result>();
			
		for(Map.Entry<String, Map<String,Set<String>>> e : megaMap.entrySet()){
			
			for(Map.Entry<String, Set<String>> e1 : e.getValue().entrySet()){
				if(e1.getValue()==null || e1.getValue().isEmpty())
					continue;
				
				for(String s : e1.getValue()){
					if(ranks.containsKey(s)){
						Integer rank = ranks.get(s);
						ranks.remove(s);
						rank += e1.getKey().length();
						ranks.put(s, rank);
					}else
						ranks.put(s, e1.getKey().length());
				}
			}
		}
		
		for(Map.Entry<String, Integer> e : ranks.entrySet())
			sortedRanks.add(new Result(e.getValue(),e.getKey()));
		
		return(sortedRanks);
	}
	
}
