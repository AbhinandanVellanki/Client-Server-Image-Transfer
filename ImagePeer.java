import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author abhin
 * to implement a p2p file sharing network
 * the class which creates each individual peer
 */
public class ImagePeer {
	private JFrame j;
	private JPanel im = new JPanel();
	private PrintWriter w;
	private Socket sock;
	private ObjectInputStream oin;
	private BufferedReader rea;
	private Socket c;
	private ObjectInputStream iin;
	private ServerSocket s;

	private static ArrayList<Peers> activepeers = new ArrayList<Peers>();
	private static ArrayList<SubI> images = new ArrayList<SubI>();

	/**
	 * @param args string arguments
	 * main method
	 */
	public static void main(String args[]) {
		ImagePeer obj = new ImagePeer();
		obj.execute();
	}

	@SuppressWarnings("unchecked")
	private void execute() {
		String ip = JOptionPane.showInputDialog("Connect to Server:");
		try {
			sock = new Socket(ip, 9000);
			InputStreamReader ir = new InputStreamReader(sock.getInputStream());
			rea = new BufferedReader(ir);
			w = new PrintWriter(sock.getOutputStream());
			oin = new ObjectInputStream(sock.getInputStream());
			iin = new ObjectInputStream(sock.getInputStream());
			String us = JOptionPane.showInputDialog("Username");
			w.println(us);
			w.flush();
			String pa = JOptionPane.showInputDialog("Password");
			w.println(pa);
			w.flush();
			String auth = rea.readLine();
			if (auth.equals("false")) {
				JOptionPane.showMessageDialog(null, "Login Fail");
			} else {
				creategui();
				im.setSize(700, 700);
				im.setLayout(new GridLayout(10, 10, 0, 0));
				int pc = Integer.parseInt(rea.readLine());
				activepeers = (ArrayList<Peers>) oin.readObject();
				int portno = activepeers.get(pc).getRp();
				//CreateServer server = new CreateServer(portno);
				//server.start();
				for (int i = 0; i < activepeers.size(); i++) {
					if (i == pc)
						continue;
					Peers p = activepeers.get(i);
					Thread t = new Thread(new Peerhandler(p));
					t.start();
				}
			}
		} catch (IOException e) {
			System.exit(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author abhin
	 * Thread to handle input from each peer
	 */
	public class Peerhandler implements Runnable {
		public Peerhandler(Peers p) {
			try {
				if (p.getRa().toString().equals("localhost/127.0.0.1") == false) {
					Socket s = new Socket(p.getRa(), p.getRp());
					iin = new ObjectInputStream(s.getInputStream());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public synchronized void run() {
			try {
				SubI o = (SubI) iin.readObject();
				while (o != null) {
					im.add(o.getLab());
					im.repaint();
					im.revalidate();
					o = (SubI) iin.readObject();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author abhin
	 * class that creates serversocket to write to peers
	 */
	public class CreateServer extends Thread {
		private ServerSocket serversock;
		private ArrayList<Linkedclient> connectedclients = new ArrayList<Linkedclient>();

		/**
		 * @param port create server socket at this port
		 * @throws IOException
		 */
		public CreateServer(int port) throws IOException {
			serversock = new ServerSocket(port);
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			try {
				while (true) {
					Linkedclient lc = new Linkedclient(serversock.accept(), this);
					connectedclients.add(lc);
					lc.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * @return list of linked peers
		 */
		public ArrayList<Linkedclient> getconnectedclients() {
			return connectedclients;
		}

		/**
		 * @param a write to peers
		 */
		public synchronized void write(Linkedclient a) {
			try {
				for (int i = 0; i < images.size(); i++) {
					a.getObjOut().writeObject(images.get(i));
					a.getObjOut().flush();
					TimeUnit.SECONDS.sleep(1);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author abhin
	 * thread to write to peers
	 */
	public class Linkedclient extends Thread {
		private CreateServer server;
		private Socket socket;
		private ObjectOutputStream objo;

		/**
		 * @param socket socket to write to
		 * @param server socket to write from
		 */
		public Linkedclient(Socket socket, CreateServer server) {
			this.server = server;
			this.socket = socket;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			try {
				objo = new ObjectOutputStream(socket.getOutputStream());
				server.write(this);
			} catch (Exception e) {
				server.getconnectedclients().remove(this);
			}
		}

		/**
		 * @return output stream to write to
		 */
		public ObjectOutputStream getObjOut() {
			return objo;
		}
	}

	/**
	 * creates the gui to display image
	 */
	private void creategui() {
		j = new JFrame("Image Peer");
		j.getContentPane().add(BorderLayout.CENTER, im);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		j.setSize(700, 700);
		j.setVisible(true);
	}

}
