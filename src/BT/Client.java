package BT;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;


import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JTextArea;

public class Client extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtIP, txtMess, txtName, txtPort;
	private JButton btnDisconnect, btnConnect, btnSend;
	private JTextArea history;
	private JTabbedPane chats;
	
	MulticastSocket socket = null;
	byte[] bufferS = null;
	byte[] bufferR = null;
	DatagramPacket packet = null;
	InetAddress ip = null;
	private boolean isConnected = false;
	String name = null;	
	HashMap<String, InetAddress> senderMap = new HashMap<String, InetAddress>();

	public static void main(String[] args) {
			Client client = new Client();
			client.setVisible(true);

	}

	public Client() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 486, 343);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		
		JLabel lblNewLabel = new JLabel("IP");
		lblNewLabel.setBounds(10, 11, 48, 14);
		
		JLabel lblNewLabel_1 = new JLabel("Port");
		lblNewLabel_1.setBounds(164, 11, 48, 14);
		
		JLabel lblNewLabel_2 = new JLabel("Name");
		lblNewLabel_2.setBounds(10, 36, 48, 14);
		
		txtIP = new JTextField();
		txtIP.setBounds(45, 8, 96, 20);
		txtIP.setColumns(10);
		
		txtName = new JTextField();
		txtName.setBounds(45, 33, 96, 20);
		txtName.setColumns(10);
		
		txtPort = new JTextField();
		txtPort.setBounds(195, 8, 96, 20);
		txtPort.setColumns(10);
		
		btnConnect = new JButton("Connection");
		btnConnect.setBounds(333, 7, 115, 23);
		btnConnect.addActionListener(this);
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setBounds(333, 32, 115, 23);
		btnDisconnect.addActionListener(this);
		contentPane.setLayout(null);
		contentPane.add(lblNewLabel);
		contentPane.add(lblNewLabel_1);
		contentPane.add(lblNewLabel_2);
		contentPane.add(txtIP);
		contentPane.add(txtName);
		contentPane.add(txtPort);
		contentPane.add(btnConnect);
		contentPane.add(btnDisconnect);
		
		chats = new JTabbedPane();
		chats.setBounds(10, 61, 418, 204);
		setGeneral();
		contentPane.add(chats);

	}
	private void setGeneral() {
		chats = new JTabbedPane();
        chats.setBounds(10, 61, 454, 236);
        newChat("General");
        contentPane.add(chats);
	}
	private void newChat(String Name) {
		JPanel newPanel = new JPanel();
        newPanel.setLayout(null);
        
        txtMess = new JTextField(); 
        txtMess.setBounds(10, 174, 342, 23);
        txtMess.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isConnected == true) {
		        	sendData(txtName.getText()+": "+ txtMess.getText());
				}
			}
		});
        newPanel.add(txtMess);
        
        btnSend = new JButton("Send"); 
        btnSend.setBounds(362, 174, 77, 23);
		btnSend.addActionListener(this); 
		newPanel.add(btnSend);
	
		JScrollPane scroll = new JScrollPane();
		scroll.setBounds(10, 0, 429, 155);
		newPanel.add(scroll);
		
		history = new JTextArea();
		scroll.setViewportView(history);
		history.setEditable(false);
		history.setForeground(Color.BLACK);
		
		chats.addTab(Name, newPanel);
	}
	private void ConnectGroup() {
		try {
			name = txtName.getText().trim();
			isConnected = true;
			socket = new MulticastSocket(Integer.parseInt(txtPort.getText().trim()));
			ip = InetAddress.getByName(txtIP.getText().trim());
			socket.joinGroup(ip);
			isConnected = true;
			newChat("General");
			setTitle(txtName.getText().trim());
			Thread thread = new Thread(new Runnable() {				
			    @Override
			    public void run() {
			    	sendData("#join_group " + name);
			        while (true) {
			        	System.out.println(receiveData());
			        	String newContent = history.getText() + "\n" + receiveData() ;
						history.setText(newContent);
			            try {
			                Thread.sleep(2000);
			            } catch (InterruptedException e) {
			                e.printStackTrace();
			            }
			        }
			    }
			});
			thread.start();
			System.out.println("Client Running ...");	
		} catch (IOException e) {
			System.out.println("initalizeVariable: " + e.toString());
		}
	}
	private void DisconnectGroup() {
		try {
			sendData("#leave_group " +name);
	        if (socket != null) {
	            socket.leaveGroup(ip);
	            isConnected = false;
	            System.out.println("Client disconnected");	
	        } else {
	            System.out.println("Client is not connected to any group");
	        }
	    } catch (IOException e) {
	        System.out.println("DisconnectGroup: " + e.toString());
	    }
	}
	
	private String receiveData() {
	    String receivedData = "";
	    try {
	        bufferR = new byte[1024];
	        packet = new DatagramPacket(bufferR, bufferR.length);
	        socket.receive(packet);
	        receivedData = new String(packet.getData(), 0, packet.getLength());
	        InetAddress senderAddress = packet.getAddress();
	        senderMap.put(receivedData, senderAddress);	        
	        receivedData = processMess(receivedData);
	    } catch (IOException e) {
	        System.out.println("Error receiving data: " + e.toString());
	    }
	    return receivedData;
	}
	
	
	private void sendData(String message) {
		if (!isConnected) {
	        return;
	    }
		try {
			InetAddress ip = InetAddress.getByName(txtIP.getText().trim());
			int port = Integer.parseInt(txtPort.getText().trim());
			bufferS = new byte[1024];
			bufferS = message.getBytes();
			DatagramPacket packetSend = new DatagramPacket(bufferS, bufferS.length, ip, port);
			
			
			socket.send(packetSend);
			message = processMess(message);
			txtMess.setText(null);
			history.setText(history.getText() + "\n" + message);
		} catch (IOException e) {
			e.printStackTrace();
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
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton boton = (JButton) e.getSource();
		if(boton == btnConnect) {
			ConnectGroup();
		}else if(boton == btnSend) {
	        if (isConnected == true) {
	        	sendData(txtName.getText()+": "+ txtMess.getText());
	        }
		}else if (boton == btnDisconnect) {
	        	DisconnectGroup();

	        }
	}

}
