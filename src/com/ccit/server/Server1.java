package com.ccit.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import com.ccit.util.ByteIntSwitch;

public class Server1
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
    	//���sk��Ӧ��ͨ����������Ҫ��ȡ
		if (sk.isReadable())
		{
			//��ȡ��SelectionKey��Ӧ��Channel����Channel���пɶ�������
			SocketChannel sc = (SocketChannel)sk.channel();
			//����׼��ִ�ж�ȡ���ݵ�ByteBuffer
			ByteBuffer buff = ByteBuffer.allocate(512);
			
			//��ʼ��ȡ����
			try
			{   
				byte catchbuff[]=null;
				
				while(sc.read(buff) > 0)
				{
                      buff.flip();
                      catchbuff=new byte[buff.limit()];
                     buff.get(catchbuff);
                     
                /*      while(buff.remaining()>0)
                      {
                        byte data[]=PackData.getPack(sc, buff, 8);
                        if(null!=data)
                        	System.out.println(new String(data));
                      }*/
                      
                      buff.clear();
                      readData(catchbuff,sc);
				}//end while
				//��ӡ�Ӹ�sk��Ӧ��Channel���ȡ��������
				
				//��sk��Ӧ��Channel���ó�׼����һ�ζ�ȡ
				sk.interestOps(SelectionKey.OP_READ);
			}//end try
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

		}
    }
    /**
     * ��ȡ����
     * @param catchbuff
     * @param sc
     * @throws IOException 
     */
  public void readData(byte[] catchbuff,SocketChannel sc) throws IOException
    {
   
    		//Ҫ����ͷ
    		byte[]header=readHeader(catchbuff,sc);
        	//��ͷ�����
        	if(parseHeader(header))
        	{
        	   readPicData(catchbuff,sc,parseHeaderToInt(header));
        	}else
        	{
        		
        		System.out.println("��ͷ���Ϸ�......");
        	}
    		//System.out.println(new String(header));
 }
  public void readPicData(byte[] catchbuff,SocketChannel sc,int picdatalen) throws IOException
  {
	  ByteBuffer buff=ByteBuffer.allocate(1024*20);
	  byte[]picdata=new byte[picdatalen];
	  int offset=0;
	  while(picdatalen>offset)
	  {
		  if(null!=catchbuff&&catchbuff.length>0)
		  {
			  //�л���
			  if(catchbuff.length>picdatalen)
			  {
				System.arraycopy(catchbuff, 0, picdata, 0, picdatalen);
				offset=picdatalen;
				//��������
				byte []lesscatch=new byte[catchbuff.length-picdatalen];
				System.arraycopy(catchbuff, picdatalen, lesscatch, 0, catchbuff.length-picdatalen);
				catchbuff=lesscatch;
				readData(catchbuff,sc);
				
			  }else if(catchbuff.length==picdatalen)
			  {
				  System.arraycopy(catchbuff, 0, picdata, 0, picdatalen);
				  offset=picdatalen;
				  catchbuff=null;
				  break;
			  }else
			  {
				  while(sc.read(buff)>0)
				  {
					  buff.flip();
					  if(catchbuff.length+buff.limit()>picdatalen)
					  {
						  //�Ƚ������е����ݷ���ͼƬ
						  System.arraycopy(catchbuff, 0, picdata, 0, catchbuff.length);
						  offset+= catchbuff.length;
						 
						 //�����ж������ݷ���һ��������
						  byte newcatch[]=new byte[buff.limit()];

						  buff.get(newcatch);
						  
						  System.arraycopy(newcatch, 0, picdata, offset, picdatalen-catchbuff.length);
						  
						  byte []newlimi=new byte[newcatch.length-picdatalen-catchbuff.length];
						 
						  System.arraycopy(newcatch, picdatalen-catchbuff.length, newlimi, 0, newlimi.length);
						 
						  catchbuff=newlimi;
						  readData(catchbuff,sc);

					  }else if(catchbuff.length+buff.limit()==picdatalen)
					  {
						//�Ƚ������е����ݷ���ͼƬ
						  System.arraycopy(catchbuff, 0, picdata, 0, catchbuff.length);
						  offset+= catchbuff.length;
						 
						 //�����ж������ݷ���һ��������
						  byte newcatch[]=new byte[buff.limit()];
						  buff.get(newcatch); 
						  System.arraycopy(newcatch, 0, picdata, offset, picdatalen-catchbuff.length);
						  catchbuff=null;
						  readData(catchbuff,sc);
					  }else
					  {
						byte []newca=new byte[buff.limit()];
						buff.get(newca);
						byte [] newcatchbuff=new byte[newca.length+catchbuff.length];
						System.arraycopy(catchbuff, 0, newcatchbuff, 0, catchbuff.length);
						System.arraycopy(newca, 0, newcatchbuff, catchbuff.length, newcatchbuff.length);
						catchbuff=newcatchbuff;
					  }
						 
				
				  }
			  }
				  
			  
		  }else
		  {
			  //û�л���
			   while(sc.read(buff)>0)
			   {
				   buff.flip();
				   if(buff.limit()>picdatalen)
				   {
					     byte []limitt=new byte[buff.limit()];
					     buff.get(limitt);
					     System.arraycopy(limitt, 0, picdata, 0, picdatalen);      
						offset=picdatalen;
						
						//��������
						byte []lesscatch=new byte[limitt.length-picdatalen];
						System.arraycopy(limitt, picdatalen, lesscatch, 0,lesscatch.length);
						catchbuff=lesscatch;
						readData(catchbuff,sc);
					   
				   }else if(buff.limit()==picdatalen)
				   {

					     buff.get(picdata);
					        
						 offset=picdatalen;

						catchbuff=null;
						readData(catchbuff,sc);
				   }else
				   {
					   byte []lesss=new byte[buff.limit()];
				       buff.get(lesss);;
				       catchbuff=lesss;
				       break;
				   }
			   }
		  }
		  
		  
	  }
	 
	  
	  
	  
  }
  
 public byte[] readHeader(byte[] catchbuff,SocketChannel sc) throws IOException
  {
	    
	    byte header[]=new byte[8];
	    ByteBuffer buff=ByteBuffer.allocate(512);
		//�Ƿ��л���
	  	if(null!=catchbuff&&catchbuff.length>0)
	  	{
	  		//�ӻ����ж���ͷ
	  		if(catchbuff.length>8)
	  		{
	  			System.arraycopy(catchbuff, 0, header, 0, 8);
	  			//��������
	  			byte tem[]=new byte[catchbuff.length-8];
	  			System.arraycopy(catchbuff, 8, tem, 0, tem.length);
	  			catchbuff=tem;
	  			//������
	  		}else if(catchbuff.length==8)
	  		{
	  			System.arraycopy(catchbuff, 0, header, 0, 8);
	  			//�������
	  			catchbuff=null;
	  		}else
	  		{  
	  			//�л��浫С�ڱ�ͷ
		  		while(sc.read(buff)>0)
		  		{
		  			buff.flip();
		  			if(buff.limit()>0)
		  			{
                      
				  			  if(catchbuff.length+buff.limit()>8)
				  			  {
				  				  //����ͷ������
				  				  byte tem[]=new byte[buff.limit()];
				  				  buff.get(tem);
					  			  System.arraycopy(catchbuff, 0, header, 0, catchbuff.length);
		                          //��װ��header 
					  			  System.arraycopy(tem, 0, header,catchbuff.length,8-catchbuff.length);
					  			  //ʣ������
		                          byte[]newcath=new byte[catchbuff.length+tem.length-8];
		                          
		                          System.arraycopy(tem, 8-catchbuff.length, newcath, 0, newcath.length);
		                          break;
		                          //������
				  			  }else if(catchbuff.length+buff.limit()==8)
				  			  {
				  				  
				  				  byte tem[]=new byte[buff.limit()];
				  				  buff.get(tem);
					  			  System.arraycopy(catchbuff, 0, header, 0, catchbuff.length);
		                         //��װ��header 
					  			  System.arraycopy(tem, 0, header,catchbuff.length,8-catchbuff.length);
					  			  catchbuff=null;
					  			  break;
					  			  //������  
				  			  }else
				  			  {
				  				  byte tem[]=new byte[buff.limit()];
				  				  buff.get(tem);
				  				  byte[]newcatch=new byte[tem.length+catchbuff.length];
				  				  System.arraycopy(catchbuff, 0, newcatch, 0, catchbuff.length);
				  				  System.arraycopy(tem, 0, newcatch,catchbuff.length,tem.length);
				  				 //û�ж�������while������
				  				  
				  			  }
		  			}//end if 
		  		}//end while
	  			
	  		}
	  		
	  	}else if(buff.limit()>0&&null==catchbuff)
	  	{
	  		//û�л�������
	  		if(sc.read(buff)>0)
	  		{
	  			buff.flip();
	  			if(buff.limit()>0)
	  			{
	  				catchbuff=new byte[buff.limit()];
	  				buff.get(catchbuff);
	  				buff.clear();
	  				readHeader(catchbuff,sc);
	  			}
	  		}
 			 //û�л�������
	  		
	  	}
	return header; 
  }
  public boolean parseHeader(byte[]header)
  {
	   //������ͷ
  	   if(parseHeaderToString(header).startsWith("~~^^"))
		{
			
			return true;
		}else
		{
			return false;
		}
  }
  public int parseHeaderToInt(byte[]header)
  {
	 if(null!=header&&header.length==8)
	 {
		byte[]tem=new byte[4];
		System.arraycopy(header, 4, tem, 0, 4);
		return ByteIntSwitch.toInt(tem);
	 }else
	 {
		 return 0;
	 }
	 
  }
  public String parseHeaderToString(byte[]header)
  {
	  String re="";
		 if(null!=header&&header.length==8)
		 {
			 byte[] tem=new byte[4];
			 System.arraycopy(header, 0, tem, 0, 4);
			try {
				re= new String(tem,"ISO-8859-1");
				System.out.println("��ͷ:"+re);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		 } 
		 return re;
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
    public void back()
    {
/*    	
		String content = "";
		byte header[]=new byte[8];
		buff.flip();
		if(null==catchbuff&&buff.limit()<8)
		{
			//������ͷ����
			if(buff.limit()>0)
			{
					catchbuff=new byte[buff.limit()];
					buff.get(catchbuff);
			}
			continue; //������
		}
		if(null!=catchbuff&&catchbuff.length+buff.limit()<8)
		{  
			if(buff.limit()>0)
			{
				byte [] secondcatch=new byte[buff.limit()];
				buff.get(secondcatch);
				byte[] newcatch=new byte[catchbuff.length+secondcatch.length];
				System.arraycopy(catchbuff, 0, newcatch, 0, catchbuff.length);
				System.arraycopy(secondcatch, 0, newcatch, 0, secondcatch.length);
			    catchbuff=newcatch;   
			}
			//�ϲ�����
			continue;
		}
		if(null!=catchbuff&&catchbuff.length+buff.limit()>=8)
		{
			if(catchbuff.length>8)
			{
				System.arraycopy(catchbuff, 0, header, 0, 8);
			}else{
			
			//����ͷ
			System.arraycopy(catchbuff, 0, header, 0, catchbuff.length);
			buff.get(header, catchbuff.length,8- catchbuff.length);
			catchbuff=null;	
			}
		}
		if(null==catchbuff&&buff.limit()>=8)
		{
			//��������ͷ
			buff.get(header, 0, 8);
		}
		//������ͷ
		content+=new String(header,"ISO-8859-1");
		if(content.startsWith("~~^^"))
		{
			byte toint[]=new byte[4];
			System.arraycopy(header, 4, toint, 0, 4);
			int picdatalen=ByteIntSwitch.toInt(toint);
			System.out.println("-------��ͷ�����ݳ�---"+picdatalen);
			//��ָ��ͼƬ����
			int offset=0;
			byte[]picdata=new byte[picdatalen];
			if(null!=catchbuff&&catchbuff.length>8)
			{
				System.arraycopy(catchbuff, 8, picdata, 0, catchbuff.length-8);
			    offset+= catchbuff.length-8;
			    catchbuff=null;
			}
			int remain=buff.remaining();
			 byte[]lastdata=null;
			if(remain>0)
			{
		      lastdata=new byte[remain];
		       buff.get(lastdata);
			}
			while(picdatalen>offset&&sc.read(buff)>0)
			{
					
					if(remain>0)
					{
						if(remain==picdatalen)
						{
							System.arraycopy(lastdata, 0, picdata, 0, picdatalen);
						  // buff.get(picdata, 0, picdatalen);
						   offset=picdatalen;
						   break;
						}
						
                        	���ǻ����ͼƬ��Ҫ��								
                        if(remain>picdatalen)
						{
							   buff.get(picdata, 0, picdatalen);
							   offset=picdatalen;
							   nextpack=new next
							   
							   break;
						}
						//������
						
						if(remain<picdatalen)
						{
							System.arraycopy(lastdata, 0, picdata, 0, lastdata.length);
							//buff.get(picdata,offset,remain);
							offset+=lastdata.length;
							continue;
						}
					}
			}
			
			
		}//end if
		
		
		//��ȡ��ͷ
		//content += charset.decode(buff);
		*/
    }
    public static void main(String[] args) throws IOException
	{
	   new Server1().init();

	}

}
