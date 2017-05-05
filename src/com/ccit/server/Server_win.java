package com.ccit.server;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
public class Server_win extends JFrame 
{
	private static final long serialVersionUID = 6615143869796102066L;
	private Selector selector = null; //���ڼ������Channel״̬��Selector
	ServerSocketChannel server=null;
	InetSocketAddress isa=null;
	AcceptData   acceptData=null;
	private List<DataPack> dataPacks=null;
	private SystemTray systemTray=null;
	private Image icon=null;
	private String hostName=null;
	private Integer port=9999;
	public Server_win()
	{
		super("����Զ�����湲������v1.0");
		this.setSize(300, 60);
		this.setResizable(false);
		this.setLocation(400, 400);
		JLabel jb=new JLabel("������������...");
		this.add(jb);
		this.setVisible(true);
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		 initSystemTray();
	}

    public void init()throws IOException
    {
		selector = Selector.open();
	    server = ServerSocketChannel.open();                //ͨ��open��������һ��δ�󶨵�ServerSocketChannelʵ��
	  //������Դ�ļ�conf.properties
		ResourceBundle rb=ResourceBundle.getBundle("conf");
		hostName=rb.getString("hostName");
		port=Integer.parseInt(rb.getString("port"));
	    isa=new InetSocketAddress(hostName,port);
	    server.socket().bind(isa);                          //����ServerSocketChannel�󶨵�ָ��IP��ַ
		server.configureBlocking(false);            	    //����ServerSocket�Է�������ʽ����
		server.register(selector, SelectionKey.OP_ACCEPT);  //��serverע�ᵽָ��Selector����
		
		while (selector.select() > 0) 
		{
			//��selector�ϵ���ѡ��Key����ɾ�����ڴ����SelectionKey
			for (SelectionKey sk : selector.selectedKeys())
			{
				selector.selectedKeys().remove(sk); //��selector�ϵ���ѡ��Key����ɾ�����ڴ����SelectionKey
				acceptAble(sk);       //���ܿͻ�������
			    readAble(sk);        	//���sk��Ӧ��ͨ����������Ҫ��ȡ
			}
		}
    }
    
    /**
     * ��ȡ�ͻ�����������
     * @param sk
     * @throws IOException
     */
    public void readAble(SelectionKey sk) throws IOException
    {
    	//���sk��Ӧ��ͨ����������Ҫ��ȡ
		if (sk.isReadable())
		{
			//��ȡ��SelectionKey��Ӧ��Channel����Channel���пɶ�������
			SocketChannel sc = (SocketChannel)sk.channel();
		
			//��ʼ��ȡ����
			try
			{   
			  acceptData=new AcceptData(selector,sk,sc);
			  dataPacks=acceptData.init(selector,sk,sc);
			 
			}
			//�����׽����sk��Ӧ��Channel�������쳣����������Channel
			//��Ӧ��Client���������⣬���Դ�Selector��ȡ��sk��ע��
			catch (IOException ex)
			{
				//��Selector��ɾ��ָ����SelectionKey
				sk.cancel();
				if (sk.channel() != null)
				{
					sk.channel().close();
				}
			}
			//��������
			readDataPacks(sc);
			

		}
    }

    public  void readDataPacks(SocketChannel sc)
    {
    	if(null!=dataPacks)
    	{
    		for(DataPack dp:dataPacks)
    		{
    		
		    	if(dp.getProtocl().equals(DataPack.PRO.get("SCREENDATA")))
		        {
		        	//���ͻ��˹㲥����
		        	processBroad(sc,dp);
		        }
		        //��֤�ͻ����û���������
		        if(dp.getProtocl().equals(DataPack.PRO.get("USERPASSWD")))
		        {
		        	processClient(sc,dp);
		        }
		        //��֤���Ͷ��Ƿ�Ϸ�
		        if(dp.getProtocl().equals(DataPack.PRO.get("ISCAPTUR")))
		        {
		        	
		        }
    		}
    	}
    }
   
   
    /**
     * ����ͻ�������
     * @param sk
     * @throws IOException
     */
    public void acceptAble(SelectionKey sk) throws IOException
    {
    	//���sk��Ӧ��ͨ�������ͻ��˵���������
		if (sk.isAcceptable())
		{
			//����accept�����������ӣ������������˶�Ӧ��SocketChannel
			SocketChannel sc = server.accept();
		  
			//���ò��÷�����ģʽ
			sc.configureBlocking(false);
			//����SocketChannelҲע�ᵽselector
			sc.register(selector, SelectionKey.OP_READ);
			//��sk��Ӧ��Channel���ó�׼��������������
			sk.interestOps(SelectionKey.OP_ACCEPT);
		}
    }
    //------------------------------------------------------
	public void processBroad(SocketChannel sc,DataPack dp)
	{
		 //sk.attach(new ClientInfo1());
		//������selector��ע�������SelectKey
		for (SelectionKey key :selector.keys())
		{
		    if(null!=key.attachment())continue;	
			//��ȡ��key��Ӧ��Channel
			Channel targetChannel = key.channel();
			//�����channel��SocketChannel����
			if (targetChannel instanceof SocketChannel)
			{
				//������������д���Channel��
				SocketChannel dest = (SocketChannel)targetChannel;
				byte []datapack=new byte[DataPack.HEADLEN+dp.getData().length];
				System.arraycopy(dp.getHeader(),0 , datapack, 0, DataPack.HEADLEN);
				System.arraycopy(dp.getData(),0 , datapack,DataPack.HEADLEN,dp.getData().length);
				
				try {
					dest.write(ByteBuffer.wrap(datapack));
				} catch (IOException e) {
					System.out.println("�㲥ʧ��.....");
					e.printStackTrace();
				}
			}
		}
	}
	public void processClient(SocketChannel sc,DataPack dp)
	{
		//username&&password
	
			try {
				String str=new String(dp.getData(),"UTF-8");
				if(null!=str)
				{
				 String spli[]=str.split(";;");

						 if("xaccit".equals(spli[0])&&"123".equals(spli[1]))
						 {
							 
								sc.write(ByteBuffer.wrap("PASS".getBytes()));
							
						 }else
						  {
							 sc.write(ByteBuffer.wrap("NOPASS".getBytes()));
						  }
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	/**
	 * ��ʼ��ϵͳ���̵ķ���
	 */
	private void initSystemTray() {
		if (SystemTray.isSupported())
			systemTray = SystemTray.getSystemTray();
		
		URL url = getClass().getResource("/com/ccit/res/log.png");
        icon=Toolkit.getDefaultToolkit().createImage(url);
		TrayIcon trayIcon = new TrayIcon(icon);
		trayIcon.setToolTip("����Զ����������V1.0");
		trayIcon.setImageAutoSize(true);
		
		PopupMenu popupMenu = new PopupMenu("���̲˵�");

		// ������ʾ������˵���
		MenuItem showMenuItem = new MenuItem("��ʾ������");
		showMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Server_win.this.setExtendedState(JFrame.NORMAL);
				Server_win.this.setVisible(true);
			}
		});

		// �����˳��˵���
		MenuItem exitMenuItem = new MenuItem("�˳�");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		popupMenu.add(showMenuItem);
		popupMenu.addSeparator();
		popupMenu.add(exitMenuItem);
		trayIcon.setPopupMenu(popupMenu);
		try {
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
    public static void main(String[] args) throws IOException
	{
    	
	   new Server_win().init();

	}
}
