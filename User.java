import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author abhin
 * The user class to dtore the user records as user objects
 */
public class User implements Hash {
	String username;
	String Hpass;
	String Name;
	String eadd;
	long phno;
	int failli;
	String lalgin;
	boolean locked;
	String Alg = "SHA-1";

	/**
	 * @param u stores username
	 * @param p stores password
	 * @param n stores full name
	 * @param e stores email id
	 * @param ph stores phone number
	 * @param f stores failed login count
	 * @param b stores boolean of whether the account is locked 
	 * @param ll stores the last login date
	 */
	User(String u, String p, String n, String e, long ph, int f, boolean b, String ll) {
		username = u;
		Hpass = p;
		Name = n;
		eadd = e;
		phno = ph;
		failli = f;
		lalgin = ll;
		locked = b;
	}

	/**
	 * @param u stores username
	 * @param p stores password
	 * @param n stores full name
	 * @param e stores email id
	 * @param ph stores phone number
	 */
	User(String u, String p, String n, String e, long ph) {
		username = u;
		Hpass = p;
		Name = n;
		eadd = e;
		phno = ph;
		failli = 0;
		lalgin = "";
		locked = false;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username that is to be set as the current username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password of the user
	 */
	public String getHpass() {
		return Hpass;
	}

	/**
	 * @param hpass password to be set
	 */
	public void setHpass(String hpass) {
		Hpass = hpass;
	}

	/**
	 * @return the full name
	 */
	public String getName() {
		return Name;
	}

	/**
	 * @param name the name that is to be set
	 */
	public void setName(String name) {
		Name = name;
	}

	/**
	 * @return the email address
	 */
	public String getEadd() {
		return eadd;
	}

	/**
	 * @param eadd the email address that is to be set
	 */
	public void setEadd(String eadd) {
		this.eadd = eadd;
	}

	/**
	 * @return the phone number
	 */
	public long getPhno() {
		return phno;
	}

	/**
	 * @param phno the phone number that is to be set
	 */
	public void setPhno(long phno) {
		this.phno = phno;
	}

	/**
	 * @return the failed login count
	 */
	public int getFailli() {
		return failli;
	}

	/**
	 * @param failli the failed login count that is to be set
	 */
	public void setFailli(int failli) {
		this.failli = failli;
	}

	/**
	 * @return the last login date
	 */
	public String getLalgin() {
		return lalgin;
	}

	/**
	 * @param lalgin the last login date to be set
	 */
	public void setLalgin(String lalgin) {
		this.lalgin = lalgin;
	}

	/**
	 * @return boolean whether the user account is locked
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * @param locked boolean to be set
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/* (non-Javadoc)
	 * @see Hash#hashthis(java.lang.String)
	 */
	public String hashthis(String a) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(Alg);
		md.update(a.getBytes());
		byte[] hash = md.digest();
		StringBuilder sb = new StringBuilder();
		for (byte b : hash) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

}
