package com.ccit.client;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ccit.server.AcceptData;
import com.ccit.server.DataPack;

public class RemoteScreenFrame extends JPanel implements Runnable
{

	
	private static final long serialVersionUID = -7396147656796734875L;
	
	private List<DataPack> dataPacks=null;
	private BufferedImage image;

	//������SocketChannel��Selector����
	private Selector selector = null;
	//���崦�����ͽ�����ַ���
	private Charset charset = Charset.forName("UTF-8");
	//�ͻ���SocketChannel
	private SocketChannel sc = null;
	

	private Client frame;
	private JScrollPane panel;

	public RemoteScreenFrame(Client frame, JScrollPane scrollPane) {
		super();
	
		this.frame = frame;
		this.panel = scrollPane;
		try {
			image=ImageIO.read(this.getClass().getResourceAsStream("/res/ccit.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public void initLisener()
    {
       
    }
	public void init() throws IOException
	{
		selector = Selector.open();
		//������Դ�ļ�conf.properties
		ResourceBundle rb=ResourceBundle.getBundle("conf");
		String hostName=rb.getString("hostName");
		int port=Integer.parseInt(rb.getString("port"));
		InetSocketAddress isa = new InetSocketAddress(hostName,port);
		//����open��̬�����������ӵ�ָ��������SocketChannel
		sc = SocketChannel.open(isa);
		//���ø�sc�Է�������ʽ����
		sc.configureBlocking(false);
		//��SocketChannel����ע�ᵽָ��Selector
		sc.register(selector, SelectionKey.OP_READ);
		//������ȡ�����������ݵ��߳�
		//new ClientThread().start();
		
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		// ִ�и���Ļ��Ʒ���
		int width=this.getWidth();
		int height=this.getHeight();
		int iwidth=image.getWidth();
		int iheight=image.getHeight();
		g.drawImage(image, (width-iwidth)/2, (height/iheight)/2, this);	// ����ȡ����Ļͼ����Ƶ������
		frame.repaint();
	}

	@Override
	public  void run() 
	{
		
		try {
			init();
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
						
						if(sc.isOpen())
						{
							try
							{   
								AcceptData acceptData=new AcceptData();
								dataPacks=acceptData.init(selector, sk, sc);
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
							readDataPacks(sc);
	
						}

					}
				}
			}
		} catch (Exception e) {

			JOptionPane.showMessageDialog(null, "������û�п�������������Ա������������");
			frame.dispose();
			e.printStackTrace();
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
			        	
			        	ByteArrayInputStream bin = new ByteArrayInputStream(dp.getData());
						
							try {
								image=ImageIO.read(bin);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (!frame.isShowing()|| frame.getExtendedState() == JFrame.ICONIFIED)
								continue;
							Dimension preferredSize = new Dimension(image.getWidth(), image.getHeight());		// ����ͼƬ��С���������С
						   setPreferredSize(preferredSize);
							revalidate();
							repaint();
					
			        }
			       
	    		}
	    	}
	    }
	/**
	 * ��ȡBufferedImage��ͼƬ�ֽ�����(һ�㹩�ڴ���)
	 * @param image
	 * @return
	 */
	public static synchronized byte[] getBufferedImageData(BufferedImage image){
		byte[] data = null;
		if(image!=null){
			try {
				ByteArrayOutputStream btout = new ByteArrayOutputStream();
				ImageIO.write(image,"gif",btout);
				data = btout.toByteArray();
				btout.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
}
