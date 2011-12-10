package fuzzy.chord;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFunction {
	
	public static final String _hashAlgorithm = "SHA-1"; // Digest algorithm
	public static final int _m = 160; // Size (bits) of a digest

	public static BigInteger hash(byte[] data){
		try {
			MessageDigest m = MessageDigest.getInstance(_hashAlgorithm);
			m.reset();
			m.update(data);
			return(new BigInteger(1,m.digest()));
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Invalid algorithm: "+e.getMessage());
			e.printStackTrace(System.err);
			return null;
		} 
	}

}
