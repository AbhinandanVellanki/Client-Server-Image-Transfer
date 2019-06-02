import java.security.NoSuchAlgorithmException;

/**
 * @author abhin This is the hash interface that has been implemented in User
 */
public interface Hash {
	/**
	 * @param a the string to be hashed
	 * @return the hashed form of the string
	 * @throws NoSuchAlgorithmException if the algorithm is not valid
	 */
	abstract String hashthis(String a) throws NoSuchAlgorithmException;
}