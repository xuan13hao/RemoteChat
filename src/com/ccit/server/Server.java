package com.ccit.server;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import com.ccit.util.ByteIntSwitch;

public class Server 
{
	private static final String CAPTUREPRO="##CCFF$$TTAA##";
    private byte []catchremain=null;
  //  public static ByteBuffer buff = ByteBuffer.allocate(1024*10);  //������֤��Ϣ
	private Selector selector = null; //���ڼ������Channel״̬��Selector
	private Charset charset = Charset.forName("UTF-8");	//����ʵ�ֱ��롢������ַ�������
	ServerSocketChannel server=null;
	InetSocketAddress isa=null;
	
    public void init()throws IOException
    {
		selector = Selector.open();
	    server = ServerSocketChannel.open();                //ͨ��open��������һ��δ�󶨵�ServerSocketChannelʵ��
		isa = new InetSocketAddress("localhost", 9999); 
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
    	if (sk.isReadable())
		{
			 SocketChannel sc = (SocketChannel)sk.channel();      //��ȡ��SelectionKey��Ӧ��Channel����Channel���пɶ�������
			 ByteBuffer buff = ByteBuffer.allocate(1024*20);       //����׼��ִ�ж�ȡ���ݵ�ByteBuffer
		     
			 byte []remainbuff=null;//�������ݣ�����ʣ��
			
			 while(sc.read(buff)>0)
		     {
				 buff.flip();//��ʱbuff��remaiΪlimit
				 
				 
				 
				 
				 //��ȡ��ͷ
				 String content="";
				 int picdatalen=0;
				 byte []head=new byte[8];
				 //û�л��棬�ҹ���ͷ����
				 if(buff.limit()>=8&&null==remainbuff)
				 {
					 //buff��һ����ͷʱ
					 buff.get(head, 0, 8);//����position�ڣ�λ��
					 content+=new String(head,"ISO-8859-1");
				 }
                 //�л���,���泤�ȹ���ͷ
				 if(null!=remainbuff&&remainbuff.length>=8)
				 {
					 buff.get(remainbuff, 0, 8);//����position�ڣ�λ��
					 content+=new String(head,"ISO-8859-1");
					 if(remainbuff.length==8)remainbuff=null;
					 if(remainbuff.length>8)
					 {
					 byte[] lastbuff=new byte[remainbuff.length-8];
					 System.arraycopy(remainbuff, 8, lastbuff, 0, remainbuff.length-8);
					 remainbuff=lastbuff;
					 }
				 }
				 //�л��棬�ҳ��Ȳ�����ͷ
                 if(null!=remainbuff&&remainbuff.length<8)
				 {
					 System.arraycopy(remainbuff, 0, head, 0, remainbuff.length);
					 buff.get(head, remainbuff.length, 8-remainbuff.length);
					 remainbuff=null;
				 }
				 
				 //������ͷ
				 if(content.startsWith("~~^^"))
				 {
					//�����ǵ���Ļ���� 
					 byte []piclen=new byte[4];
					 System.arraycopy(head, 4, piclen, 0, 4);
					 picdatalen=ByteIntSwitch.toInt(piclen);
					 System.out.println("------��Ļ���ݴ�С----"+picdatalen);
					
					 //�ӻ����ڶ�ȡָ�����ȵ�ͼƬ����
					 byte []picdata=new byte[picdatalen];
					 int remain=buff.remaining();
					 int offset=0;
					 if(null!=remainbuff&&remainbuff.length<picdatalen)
					 {
						 System.arraycopy(remainbuff, 0, picdatalen, 0, remainbuff.length);
						 offset+=remainbuff.length;
						 remainbuff=null;
					 }
					 if(null!=remainbuff&&remainbuff.length==picdatalen)
					 {
						 System.arraycopy(remainbuff, 0, picdatalen, 0,picdatalen);
						 offset=picdatalen;
						 remainbuff=null;
					 }
					 if(null!=remainbuff&&remainbuff.length>picdatalen)
					 {
						 System.out.println("---------�����ܰ�-------------");
					 }
					 //�����㹻
					 if(remain>=picdatalen)
					 {
						 buff.get(picdata,0,picdatalen);
						 //ʣ������ݻ�������
						 remainbuff=new byte[remain-picdatalen];
						 buff.get(remainbuff,0,remain-picdatalen);
						 offset=picdatalen;
						// continue; 
					 }else if(remain>0)
					 {
					   //���治��ͼƬ����
					   buff.get(picdata,0,remain);
					   offset+=remain;
					   buff.clear();
					 }
					 
					 while(picdatalen>offset&&sc.read(buff)>0)
					 {
						buff.flip();
						if(buff.limit()==picdatalen-offset)
						{
						  buff.get(picdata,offset,picdatalen-offset);
						  offset=picdatalen;
						}else if( buff.limit()>picdatalen-offset)
						{
							buff.get(picdata,offset,picdatalen-offset);
							offset=picdatalen;
							int rema=buff.remaining();
							//��������
							remainbuff=new byte[rema];
						    buff.get(remainbuff,0,rema);
						}else
						{
							buff.get(picdata,offset,buff.limit());
							offset+=buff.limit();
							
						}
						
					 }//end while while(picdatalen>offset&&sc.read(buff)>0)
					 System.out.println("---------"+offset+"---"+picdatalen+"---------------");
				 }//end if  if(content.startsWith("~~^^"))
				
		    	 
		     }//end  while(sc.read(buff)>0)
			
		
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

    
    public void readback()
    {
    	//��ʼ��ȡ����
	       
		 /*   String content="";
		    int len=0;
	        while((len=sc.read(buff))>0)
	        {
	        	buff.flip();
	            if(len>=8)
	            {
	            	
	            	int offset=0;
	            	byte []picdata=null;
	            	int picdatalen=0;
		            	byte []header=new byte[8];
		            	buff.get(header,0,8);
		                if(null!=header)
			            {
			            content+=new String(header,"ISO-8859-1");
			            System.out.println("��ͷ��"+content);
			            }
		                if(content.startsWith("~~^^"))
		                {
				              //���ܵ�����Ļ���ݳ��� 
				            	byte piclen[]=new byte[4];
				            	System.arraycopy(header,4, piclen, 0, 4);
				            	picdatalen=ByteIntSwitch.toInt(piclen);
				            	System.out.println("���ݳ���Ϊ��"+picdatalen);
				            	int remain=buff.remaining();
				            	//int offset=0;
				            	picdata=new byte[picdatalen];
				            	
				            	if(remain>=picdatalen)
					        	   {
					        		   buff.get(picdata,0,picdatalen);
					        		   offset=picdatalen;
					        	   }else if(remain>0)
					        	   {
						        		   buff.get(picdata,offset,remain);//����һ��buffʣ��������
						        		   offset+=remain;
						        		   buff.clear();
						        		   while(offset<picdatalen&&sc.read(buff)>0)
						        		   {
						        			    buff.flip();
						        			    //��������ڹ�ͼƬ����
						        			    if(buff.limit()>=picdatalen-offset)
						        			    {
						        			    	buff.get(picdata,offset,picdatalen-offset);
						        			    	offset=picdatalen;
						        			    }else
						        			    {
						        			    	buff.get(picdata,offset,buff.limit());
						        			    	offset+=buff.limit();
						        			    	buff.clear();
						        			    }
						        			    
						        		   }
					        		 
				        		   
				        	          }else if(remain==0)
				        	          {
				        	        	  while(offset<picdatalen&&sc.read(buff)>0)
						        		   {
						        			    buff.flip();
						        			    //��������ڹ�ͼƬ����
						        			    if(buff.limit()>=picdatalen-offset)
						        			    {
						        			    	buff.get(picdata,offset,picdatalen-offset);
						        			    	offset=picdatalen;
						        			    }else
						        			    {
						        			    	buff.get(picdata,offset,buff.limit());
						        			    	offset+=buff.limit();
						        			    	buff.clear();
						        			    }
						        			    
						        		   }
				        	          }
				            	
		                }
		                System.out.println("--------"+picdatalen+"----------"+offset+"---------------------");
	            	
	            }else
	            {
	            	
	            }
	        
	        	
	        	
	        	sk.interestOps(SelectionKey.OP_READ);
	        }	*/
	//�����׽����sk��Ӧ��Channel�������쳣����������Channel
	//��Ӧ��Client���������⣬���Դ�Selector��ȡ��sk��ע��
    }
	public static void main(String[] args) throws IOException
	{
	   new Server().init();

	}
	
}
