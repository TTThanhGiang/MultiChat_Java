package BT;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

public class Server extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtPort;
	private JTextField txtIp;
	
	private JTextArea history;
	private JTabbedPane chats;
	private JList<String> JlistClient;
    private DefaultListModel<String> listModel;
	
	MulticastSocket socket = null;
	byte[] bufferS = null;
	byte[] bufferR = null;
	DatagramPacket packet = null;
	InetAddress ip = null;
	HashMap<String, InetAddress> senderMap = new HashMap<String, InetAddress>();
	private JTextField txtName;
	
	List<String> listCli = new ArrayList<String>();
	
	public Server() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 495, 419);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
	
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(358, 37, 115, 228);
		contentPane.add(scrollPane_1);
		
		listModel = new DefaultListModel<>();
		JlistClient = new JList<>(listModel);
		JlistClient.setFixedCellWidth(228);
		scrollPane_1.setRowHeaderView(JlistClient);
		
		JLabel lblNewLabel = new JLabel("IP Group");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel.setBounds(10, 281, 72, 14);
		contentPane.add(lblNewLabel);
		
		txtPort = new JTextField();
		txtPort.setBounds(103, 316, 96, 20);
		contentPane.add(txtPort);
		txtPort.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Port Group");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel_1.setBounds(10, 318, 72, 14);
		contentPane.add(lblNewLabel_1);
		
		txtIp = new JTextField();
		txtIp.setBounds(103, 279, 96, 20);
		contentPane.add(txtIp);
		txtIp.setColumns(10);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.setBounds(259, 276, 89, 29);
		contentPane.add(btnCreate);
		btnCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String grname = txtName.getText().trim();
					ip = InetAddress.getByName(txtIp.getText().trim());
					int port = Integer.parseInt(txtPort.getText().trim()); 
					createGroup(grname, ip, port);
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
			}
		});
		
		JButton btnClear = new JButton("Clear");
		btnClear.setBounds(259, 312, 89, 29);
		contentPane.add(btnClear);
		btnClear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				txtIp.setText(null);
				txtName.setText(null);
				txtPort.setText(null);
				chats.removeAll();
				listCli.clear();
				JlistClient.removeAll();
			}
		});
		
		JLabel lblNewLabel_2 = new JLabel("Group Name");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel_2.setBounds(10, 359, 72, 14);
		contentPane.add(lblNewLabel_2);
		
		txtName = new JTextField();
		txtName.setBounds(103, 357, 96, 20);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		setGeneral("General");
		
		
	}
	private void setGeneral(String nameGr) {
		chats = new JTabbedPane();
        chats.setBounds(10, 13, 340, 257);
        newChat(nameGr);
        contentPane.add(chats);
	}
	private void newChat(String Name) {
		JPanel newPanel = new JPanel();
        newPanel.setLayout(null);
		JScrollPane scroll = new JScrollPane();
		scroll.setBounds(10, 0, 315, 224);
		newPanel.add(scroll);
		
		history = new JTextArea();
		scroll.setViewportView(history);
		history.setEditable(false);
		history.setForeground(Color.BLACK);
		
		chats.addTab(Name, newPanel);
	}
	
	private void createGroup(String nameGroup, InetAddress ip, int port) {
		try {
			newChat(nameGroup);
			socket = new MulticastSocket(port);
            socket.joinGroup(ip);       
            System.out.println("Da tao nhom");
            history.setText(nameGroup + " has been created.");
            Thread receiveThread = new Thread(() -> {
            	            	
                while (true) {
                    String newContent = history.getText() + "\n" + receiveData();
                    history.setText(newContent);
                    
                }
                
            });
            receiveThread.start();
            
		} catch (Exception e) {
			System.out.println("Bi loi");
		}
	}
	private String receiveData() {
		String re = "";
		try {
			bufferR = new byte[1024];
			packet = new DatagramPacket(bufferR, bufferR.length);
			socket.receive(packet);
			re = new String(packet.getData(),0,packet.getLength());
			InetAddress senderAdd = packet.getAddress();
			senderMap.put(re, senderAdd);
			re = processMess(re);
			if (re.startsWith("#join_group ")) {
                String clientName = re.substring(12);
                if (!listCli.contains(clientName)) {
                	listCli.add(clientName);
                    System.out.println(clientName + " joined the group.");
                    updateClientList(listCli.toArray(new String[0]));
                }
            }
			if (re.startsWith("#leave_group ")) {
                String clientName = re.substring(13);
                if (listCli.contains(clientName)) {
                	listCli.remove(clientName);
                    System.out.println(clientName + " left the group.");
                    updateClientList(listCli.toArray(new String[0]));
                }
            }
		} catch (IOException e) {
			System.out.println("initalizeVariable: " + e.toString());
		}
		return re;
	}
	
    private void updateClientList(String[] clients) {
        listModel.clear();
        for (String client : clients) {
            listModel.addElement(client);
        }
    }
	private String processMess(String mess) {
		if(mess != null) {
			
			mess = mess.substring(0);
			mess = mess.replace(":)", "\uD83D\uDE04");
			mess = mess.replace(":D", "\uD83D\uDE03");
			mess = mess.replace(":3", "\uD83D\uDE0A");
			mess = mess.replace(":P", "\uD83D\uDE1C");
			mess = mess.replace(":(", "\uD83D\uDE14");
			mess = mess.replace(":'(", "\uD83D\uDE22");
			mess = mess.replace("D:", "\uD83D\uDE29");
			mess = mess.replace(">:c", "\uD83D\uDE21");
		}
		return mess;
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.setVisible(true);
	}
}
