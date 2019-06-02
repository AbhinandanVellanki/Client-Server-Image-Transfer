import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * @author abhin
 * To create a p2p netwrok for sile sharing
 * This is the server class that initializes the network
 * 
 */
public class ImageServer {
	private JFrame jf;
	private JPanel ji;
	private JPanel jb;
	private JLabel temp;
	private static ArrayList<User> Database = new ArrayList<User>();
	private static ArrayList<SubI> imags = new ArrayList<SubI>();
	private static ArrayList<Peers> peer = new ArrayList<Peers>();
	private BufferedImage imgs[];
	private Socket a;
	private ServerSocket s;
	private int peercount=0;

	/**
	 * @param args string arguments
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String args[]) throws IOException, ParseException {
		ImageServer obj = new ImageServer();
		obj.execute();
	}

	private void execute() throws IOException, ParseException {
		jf = new JFrame("Image Server");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setim();
		jf.add(BorderLayout.NORTH, ji);
		setbut();
		jf.add(BorderLayout.CENTER, jb);
		jf.pack();
		jf.setVisible(true);
		start();
	}

	private void start() throws IOException, ParseException {
		File file = new File("./User.txt");
		try {
			JSONParser parser = new JSONParser();
			JSONObject o = (JSONObject) parser.parse(new FileReader(file));
			JSONArray jar = (JSONArray) o.get("user_array");
			Iterator<Object> iterator = jar.iterator();
			while (iterator.hasNext()) {
				Object obj = iterator.next();
				if (obj instanceof JSONObject) {
					String u;
					String p1;
					String na;
					String ea;
					Long ph;
					int w;
					String d;
					boolean l;
					JSONObject json = (JSONObject) obj;
					u = json.get("username").toString();
					p1 = json.get("hash_password").toString();
					na = json.get("Full Name").toString();
					ea = json.get("Email").toString();
					ph = Long.parseLong(json.get("Phone Number").toString());
					w = Integer.parseInt(json.get("Fail Count").toString());
					d = json.get("Last Login Date").toString();
					l = Boolean.parseBoolean(json.get("Account Locked").toString());
					User n = new User(u, p1, na, ea, ph, w, l, d);
					Database.add(n);
				}
			}
			go();
		} catch (FileNotFoundException e) {
			System.out.println("File does not exist");
			System.exit(0);
		}
	}

	private void go() {
		try {
			s = new ServerSocket(9000);
			InetAddress ra = InetAddress.getByName("localhost");
			int rp = s.getLocalPort();
			Peers p = new Peers(ra, rp);
			peer.add(p);
			while (true) {
				a = s.accept();
				peercount++;
				ra = a.getInetAddress();
				rp = a.getPort();
				p = new Peers(ra, rp);
				peer.add(p);
				Thread t = new Thread(new PeerHandler(a));
				t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	

	/**
	 * @author abhin
	 *	Thread to handle each individual peer
	 */
	public class PeerHandler implements Runnable {
		private BufferedReader r;
		private Socket so;
		private String username;
		private String password;
		private PrintWriter w;
		private ObjectOutputStream oout;
		private ObjectOutputStream iout;

		/**
		 * @param peer the socket to which the peer connects to the server on
		 * Parameterized constructor
		 */
		public PeerHandler(Socket peer) {
			try {
				so = peer;
				w = new PrintWriter(so.getOutputStream());
				oout = new ObjectOutputStream(so.getOutputStream());
				iout = new ObjectOutputStream(so.getOutputStream());
				InputStreamReader i = new InputStreamReader(so.getInputStream());
				r = new BufferedReader(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				username = r.readLine();
				password = r.readLine();
				boolean au = authenti(Finduser(username));
				if (!au) {
					w.println("false");
					w.flush();
				} else {
					w.println("true");
					w.flush();
					perform();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * @throws IOException
		 * writes the image block by block to the peer
		 */
		public void perform() throws IOException {
			try {
				w.println(Integer.toString(peercount));
				w.flush();
				oout.writeObject(peer);
				oout.flush();
				for (int i = 0; i < imags.size(); i++) {
					iout.writeObject(imags.get(i));
					iout.flush();
					TimeUnit.SECONDS.sleep(1);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private User Finduser(String u) {
			for (int i = 0; i < Database.size(); i++) {
				User a = Database.get(i);
				if (a.getUsername().equals(u)) {
					return a;
				}
			}
			User c = new User("notfound", "", "", "", 0);
			return c;
		}

		private boolean authenti(User a) throws NoSuchAlgorithmException {
			int rc = 0;// stores number of incorrect attempts
			if (!a.isLocked()) {
				String sb = a.hashthis(password);
				String pa = a.getHpass();
				if (pa.equals(sb)) {// successful
					String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
					a.setLalgin(date);
					a.setFailli(0);
					return true;
				} else {// failed attempt
					rc++;
					a.setFailli(rc);
					if (a.getFailli() >= 3)
						a.setLocked(true);
					return false;
				}
			} else if (a.isLocked()) {
				return false;
			}
			return false;
		}
	}

	private String chooseim() {
		JFileChooser f = new JFileChooser();
		int result = f.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			File fi = f.getSelectedFile();
			return fi.getAbsolutePath();
		} else {
			return null;
		}
	}

	private BufferedImage resize(BufferedImage img, int newW, int newH) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
		g.dispose();
		return dimg;
	}

	private void setim() {
		File file = new File(chooseim());
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("Failed to load image");
			System.exit(0);
		}
		BufferedImage image = null;
		try {
			image = ImageIO.read(fi);
		} catch (IOException e) {
			System.out.println("Failed to load image");
			System.exit(0);
		}
		image = resize(image, 700, 700);
		splitim(image);
	}

	private void splitim(BufferedImage image) {
		int c = 0;
		BufferedImage[] imgs = new BufferedImage[100];
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				imgs[c] = new BufferedImage(70, 70, image.getType());
				Graphics2D g = imgs[c++].createGraphics();
				g.drawImage(image, 0, 0, 70, 70, 70 * j, 70 * i, 70 * j + 70, 70 * i + 70, null);
				g.dispose();
				SubI a = new SubI();
				a.setLoc((10 * i) + j);
				imags.add(a);
			}
		}
		ji = new JPanel();
		ji.setLayout(new GridLayout(10, 10, 0, 0));
		for (int i = 0; i < imgs.length; i++) {
			temp = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().createImage(imgs[i].getSource())));
			imags.get(i).setLab(temp);
			temp.addMouseListener(new DragDropListener());
			temp.addMouseMotionListener(new DragDropListener());
			ji.add(temp);
		}
	}
	
	/**
	 * @author abhin The user defined class which implements the overridden mouse
	 *         listener methods
	 */
	class DragDropListener extends MouseAdapter implements MouseListener, MouseMotionListener {
		int initx;
		int inity;
		JLabel j;
		JLabel u;

		/**
		 * Constructor
		 */
		DragDropListener() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 * 
		 * @override
		 */
		public void mousePressed(MouseEvent e) {
			j = (JLabel) e.getSource();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
		 * 
		 * @override
		 */
		public void mouseDragged(MouseEvent e) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 * 
		 * @override
		 */
		public void mouseReleased(MouseEvent e) {
			initx = (j.getLocation().x);
			inity = (j.getLocation().y);
			int finx = e.getXOnScreen() - (e.getXOnScreen() % 80);
			int finy = e.getYOnScreen() - (e.getYOnScreen() % 80) - 80;
			int inloc = 10 * (inity / 80) + (initx / 80);
			int finloc = (10 * (finy / 80) + (finx / 80));
			if (finloc != imags.get(finloc).getLoc()) {
				u = imags.get(finloc).getLab();
				u.setLocation(initx, inity);
				j.setLocation(finx, finy);
				Collections.swap(imags, finloc, inloc);
			} else
				System.out.println("\nImage Block in Correct Position");

		}
	}


	private void setbut() {
		jb = new JPanel();
		JButton b = new JButton("Load another image");
		b.addActionListener(new LoadListener());
		jb.add(b);
	}

	/**
	 * @author abhin
	 * to load the new image
	 */
	class LoadListener implements ActionListener {
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent a) {
			try {
				jf.setVisible(false);
				jf.remove(ji);
				setim();
			} catch (Exception e) {
				System.out.println("Failed to load new image, old image retained");
			} finally {
				jf.add(BorderLayout.NORTH, ji);
				jf.setVisible(true);
			}
		}
	}

}
