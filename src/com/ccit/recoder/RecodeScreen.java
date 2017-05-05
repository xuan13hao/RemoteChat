package com.ccit.recoder;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import com.ccit.util.ByteIntSwitch;
import com.ccit.util.MouseHook;
import com.ccit.util.Mousexy;
public class RecodeScreen extends Thread 
{

    private Mousexy xy=new Mousexy();  
    private Dimension dimension ;     //��������
    private Rectangle rectangel;      //��ͼ���δ�С
    private Robot robot ;             //��ͼ������
    private Image icon;               //���
	private Selector selector = null;  //������SocketChannel��Selector����
	private SocketChannel sc = null; //�ͻ���SocketChannel
	private JFrame jf=null;
    private String hostName=null;
    private Integer port=9999;
	public RecodeScreen(JFrame jf)
	{
		this.jf=jf;
	}
	public void init()
	{
		//����һ���߳��������������
		MouseHook mouseHook=new MouseHook(xy); 
		mouseHook.start();
		
		try {
			selector = Selector.open();
			//������Դ�ļ�conf.properties
			ResourceBundle rb=ResourceBundle.getBundle("conf");
			hostName=rb.getString("hostName");
			port=Integer.parseInt(rb.getString("port"));
			InetSocketAddress isa = new InetSocketAddress(hostName,port);
			sc = SocketChannel.open(isa);//����open��̬�����������ӵ�ָ��������SocketChannel	
			sc.configureBlocking(false);//���ø�sc�Է�������ʽ����
			sc.register(selector, SelectionKey.OP_READ);//��SocketChannel����ע�ᵽָ��Selector
			
			icon=ImageIO.read(this.getClass().getResourceAsStream("/res/cursor1.png"));
			dimension = Toolkit.getDefaultToolkit().getScreenSize();//��ȡ��Ļ��С
			rectangel=new Rectangle(0,0,(int)dimension.getWidth(),(int)dimension.getHeight());
			robot = new Robot();

		} catch (IOException e) {
			System.out.println("��ʼ��ʱ����IO�쳣");
			e.printStackTrace();
		} catch (AWTException e) {
			System.out.println("���ɻ����˳���");
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public void run() 
	{
	
	     init();
	     while(true)
	     {
	    	
	    	 try {
	         if(sc.isOpen())
	         {
	        	 //ͼƬ����
	        	 byte []sendimage=captureScreenToByte();
		    	 
		    	
		    	 byte []header=new byte[8]; //���屨ͷ
		    	 //��װ��ͷ
				 System.arraycopy("~~^^".getBytes("ISO-8859-1"), 0, header, 0, 4);
				 System.arraycopy(ByteIntSwitch.toByteArray(sendimage.length, 4),0,header,4,4);
	
				byte [] pack=new byte[sendimage.length+8];
				System.arraycopy(header,0, pack, 0, 8);
				System.arraycopy(sendimage,0, pack, 8, sendimage.length);
				
	    	    sc.write(ByteBuffer.wrap(pack));
				 //sc.write(ByteBuffer.wrap(header));
	    	   //  sc.write(ByteBuffer.wrap(sendimage));  
	    	 
	         }else
	         {
	        	 return ;
	         }
				Thread.sleep(200);
			}  catch (Exception e) {
			    JOptionPane.showMessageDialog(null, "������û�п������ȿ�������������������");
				System.exit(0);
			    jf.dispose(); 
			    try {
					if(null!=sc)sc.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
	     }	
	}
	
   public  synchronized byte[] captureScreenToByte()
   {
	   //����Ļͼ
	   BufferedImage image=robot.createScreenCapture(rectangel);
	     Graphics g=image.getGraphics();
	    g.drawImage(icon, xy.getX(), xy.getY(), null);
	   return getBufferedImageData(image);
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
				ImageIO.write(image,"jpg",btout);
				data = btout.toByteArray();
				btout.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
	}
	

}
