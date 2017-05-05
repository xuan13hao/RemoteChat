package com.ccit.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.ccit.server.DataPack;
import com.ccit.util.ByteIntSwitch;

public class Login extends JFrame
{
	private static final long serialVersionUID = 7238997905461242892L;
    private boolean remember=true;
	private JLabel top = null;
	
	private JButton login = new JButton("��½");
	private JCheckBox register = new JCheckBox("��ס����");


	private Box bottomBut = Box.createHorizontalBox();
	private JPanel bp = new JPanel();
	

	private JLabel user = new JLabel("�ʺ�:");
	private JLabel pwd = new JLabel("����:");
	private JTextField tuser = new JTextField(15);
	private JPasswordField tpwd = new JPasswordField(15);
	
	private Box b1=Box.createHorizontalBox();
    private Box b2=Box.createHorizontalBox();
    private Box b3=Box.createHorizontalBox();
    private Box b4=Box.createVerticalBox();

    private Font font=new Font("����", Font.BOLD, 14);
    private Color bak=new Color(0XE4,0XF4,0XFF);
    
   //������SocketChannel��Selector����
	private Selector selector = null;
	//���崦�����ͽ�����ַ���
	private Charset charset = Charset.forName("UTF-8");
	//�ͻ���SocketChannel
	private SocketChannel sc = null;
    private String hostName=null;
    private Integer port=9999;
    public Login() {
		super("����Զ�����湲��ͻ���V1.0");
		try {
		init();
		initUserAndPassword();
		
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "������û�п�������ʱ�޷�����!");
			e.printStackTrace();
		}
	}

   
	public void init()throws Exception {
		
		top=new JLabel(new ImageIcon(Login.class.getResource("/com/ccit/res/icon.png")));
		//this.getContentPane().setBackground(new Color(0XE4,0XF4,0XFF));
		
		tuser.setPreferredSize(new Dimension(200,25));
		tpwd.setPreferredSize(new Dimension(200,25));
		tuser.setToolTipText("�������˺�!");
		tpwd.setToolTipText("����������!");
		user.setFont(font);
		pwd.setFont(font);
		register.setFont(font);
		login.setFont(font);
		//�м䲼��
		b1.add(Box.createHorizontalStrut(20));
		b1.add(user);
		b1.add(Box.createHorizontalStrut(10));
		b1.add(tuser);
		
		b2.add(Box.createHorizontalStrut(20));
		b2.add(pwd);
		b2.add(Box.createHorizontalStrut(10));
		b2.add(tpwd);
		
		b3.add(Box.createHorizontalStrut(10));
		
		b4.add(Box.createVerticalStrut(10));
		b4.add(b1);
		b4.add(Box.createVerticalStrut(10));
		b4.add(b2);
		b4.add(Box.createVerticalStrut(10));
		b4.add(b3);

		JPanel jj=new JPanel();
		jj.add(b4);
		jj.setBackground(bak);
		register.setSelected(true);
		register.setBackground(new Color(192,226,250));
		//���õײ�
		bp.setBackground(new Color(192,226,250));
		bottomBut.add(register);
		bottomBut.add(Box.createHorizontalStrut(80));
		bottomBut.add(login);
		bp.add(bottomBut);
		
		this.add(jj,"Center");
		this.add(top,"North");
		this.add(bp,"South");
		this.intiLisener();
		
		this.setSize(306, 220);
		this.setResizable(false);
		this.setLocation(500, 200);
		this.setIconImage(this.getToolkit().getImage(Login.class.getResource("/com/ccit/res/log.png")));
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	

	}
	public void initUserAndPassword()
	{
		File f=new File("redwww.properties");
		if(f.exists())
		{
		    Properties pro=new Properties();
		    try {
				pro.load(new FileInputStream(f));
				tuser.setText(pro.getProperty("username"));
				tpwd.setText(pro.getProperty("password"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void intiLisener()throws Exception
	{
		login.addActionListener(new ActionListener()
		{
                
			@Override
			public void actionPerformed(ActionEvent e) {
				loginServer();	
		}});
		tpwd.addActionListener(new ActionListener()
		{     
			@Override
			public void actionPerformed(ActionEvent e) {
				loginServer();	
		}});
		
	
		register.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(register.isSelected())
				{
					remember=true;
				}else
				{
					remember=false;
				}
				
			}
		});
	}
	public void loginServer()
	{
		String userName = tuser.getText();
		String userPwd = new String(tpwd.getPassword());
		String userpwd=userName+";;"+userPwd;
		login.setEnabled(false);
		tpwd.setEnabled(false);
		tuser.setEditable(false);
		top.setIcon(new ImageIcon(Login.class.getResource("/com/ccit/res/icon2.png")));
		//Login.this.dispose();
		try {
			//��ס����
			if(remember)
			{
				Properties p=new Properties();
				p.put("username", userName);
				p.put("password", userPwd);
				FileOutputStream pos=new FileOutputStream("redwww.properties");
				p.store(pos, "��ס������");
				pos.close();
			}

			initSocketChanel();
			//DataPack.PRO.get("USERPASSWD")+
			
			byte userandpwd[]=userpwd.getBytes();
			byte send[]=new byte[DataPack.HEADLEN+userandpwd.length];
			
			System.arraycopy(DataPack.PRO.get("USERPASSWD").getBytes(), 0, send, 0, 4);
			System.arraycopy(ByteIntSwitch.toByteArray(userandpwd.length, 4), 0, send, 4, 4);
			System.arraycopy(userandpwd, 0, send, DataPack.HEADLEN, userandpwd.length);
			sc.write(ByteBuffer.wrap(send));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	}	

	public void initSocketChanel()throws IOException
	{
	
			selector = Selector.open();
			//������Դ�ļ�conf.properties
			ResourceBundle rb=ResourceBundle.getBundle("conf");
			hostName=rb.getString("hostName");
			port=Integer.parseInt(rb.getString("port"));
			InetSocketAddress isa = new InetSocketAddress(hostName,port);
			//����open��̬�����������ӵ�ָ��������SocketChannel
			sc = SocketChannel.open(isa);
			//���ø�sc�Է�������ʽ����
			sc.configureBlocking(false);
			//��SocketChannel����ע�ᵽָ��Selector
			sc.register(selector, SelectionKey.OP_READ);
			//������ȡ�����������ݵ��߳�
			new ClientThread().start();
			//��������������
		
	}
	
	//�����ȡ���������ݵ��߳�
	private class ClientThread extends Thread
	{
		public void run()
		{
			try
			{
				while (selector.select() > 0) 
				{
					//����ÿ���п���IO����Channel��Ӧ��SelectionKey
					for (SelectionKey sk : selector.selectedKeys())
					{
						//ɾ�����ڴ����SelectionKey
						selector.selectedKeys().remove(sk);
						//�����SelectionKey��Ӧ��Channel���пɶ�������
						if (sk.isReadable())
						{
							//ʹ��NIO��ȡChannel�е�����
							SocketChannel sc = (SocketChannel)sk.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							String content = "";
							while(sc.read(buff) > 0)
							{
								sc.read(buff); 
								buff.flip();
								content += charset.decode(buff);
							}
							System.out.println(content);
							//��ӡ�����ȡ������
							if(content.equals("PASS"))
							{
								Login.this.dispose();
								new Client();
							}else
							{
								JOptionPane.showMessageDialog(Login.this, "�û��������벻�Ϸ�,�����µ�½!");
								Login.this.dispose();
								System.exit(0);
							}
							//Ϊ��һ�ζ�ȡ��׼��
							sk.interestOps(SelectionKey.OP_READ);
						}
					}
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		new Login();

	}

}

