import java.io.Serializable;
import java.net.InetAddress;

/**
 * @author abhin
 * class to store the peers as objects
 */
public class Peers implements Serializable {
	InetAddress ra;
	int rp;

	/**
	 * @param ra the address of the peer
	 * @param rp the port to which the peer is connected
	 */
	public Peers(InetAddress ra, int rp) {
		super();
		this.ra = ra;
		this.rp = rp;
	}

	/**
	 * @return the address of the peer
	 */
	public InetAddress getRa() {
		return ra;
	}

	/**
	 * @param ra set the address of the peer
	 */
	public void setRa(InetAddress ra) {
		this.ra = ra;
	}

	/**
	 * @return the port number of the peer
	 */
	public int getRp() {
		return rp;
	}

	/**
	 * @param rp set the port number of the peer
	 */
	public void setRp(int rp) {
		this.rp = rp;
	}
}