package com.ccit.server;


import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import com.ccit.util.ByteIntSwitch;

public class ReadData
{
    private ByteBuffer buff=ByteBuffer.allocate(1024*100);
    private ClientInfo ci=null;
    private byte header[]=null;
    private byte data[]=null;
    private int datalen=0;
    
	 public ReadData(SelectionKey sk ,SocketChannel sc) throws IOException
	 {
	    	init(sk,sc);
	 }

	public void init(SelectionKey sk ,SocketChannel sc) throws IOException
	{
		//���뻺���е�����
		if(null!=sk.attachment())
		{
			ci=(ClientInfo)sk.attachment();
		}
    //------------------���ϵĶ������е�����----------------------------
		while(sc.read(buff)>0)
		{
			
			  buff.flip();
			  //û�л�������
              if(null==ci||0==ci.getCatchType())
              {  
            	  //��buff�дӱ�ͷ��
            	  readByteBufferFromHeader();  
              }
              if(null!=ci&&1==ci.getCatchType()&&null==ci.getCatchbuff())
              {
            	  System.out.println("------------00000------------------");
            	  header=ci.getHeader();
            	  datalen=ci.getDatalen();
            	  
            	  //������ֻ������һ����ͷ
            	  readByteBufferFromData();
              }
              //�������Ǳ�ͷ
              if(null!=ci&&1==ci.getCatchType()&&null!=ci.getCatchbuff()&&ci.getCatchbuff().length>0)
              {
            	  
            		//��ȡ��ͷ��������
            		  if(ci.getCatchbuff().length+buff.remaining()>=8)
            		  {
            			   byte head[]=new byte[8];
            			   System.arraycopy(ci.getCatchbuff(), 0, head, 0, ci.getCatchbuff().length);
            			   buff.get(head,ci.getCatchbuff().length,8-ci.getCatchbuff().length);
            			   ci.setCatchType(0);
             			  ci.setCatchbuff(null);
             			  ci.setDatalen(0);
             			  ci.setHeader(null);
             			   readByteBufferFromData();
            			   
            		  }else
            		  {
            			  marageBuff();   //���ֺϲ�����
             			 readCatchFromHeader();   
            		  }
            
              }
              //������������
              if(null!=ci&&2==ci.getCatchType()&&ci.getCatchbuff().length>0)
              {
            	      
            	    	  if(buff.remaining()+ci.getCatchbuff().length>=ci.getDatalen())
                		  {
                			  byte [] datas=new byte[ci.getDatalen()]; 
                			  System.arraycopy(ci.getCatchbuff(), 0, datas, 0, ci.getCatchbuff().length);
                			  buff.get(datas,ci.getCatchbuff().length,ci.getDatalen()-ci.getCatchbuff().length);
                			  header=ci.getHeader();
                			  //�������
                			  ci.setCatchType(0);
                			  ci.setCatchbuff(null);
                			  ci.setDatalen(0);
                			  ci.setHeader(null);
                			  

                		  }else
                		  {
                			//��ȡ���ݣ�������
               	    	   marageBuff();
               	    	  readCatchFromData(); 
                			  
                		  }
            	     
            	  
            	 
              }
		}//end whil
	//------------------���������е�����----------------------------			  
				  
		if(null!=ci)
		{
			 sk.attach(ci);
		}
		
		//��sk��Ӧ��Channel���ó�׼����һ�ζ�ȡ
		sk.interestOps(SelectionKey.OP_READ);
	}//init������
	
	/**
	 * ����ͷ�е����ݳ��Ƚ�������
	 * @param header
	 * @return
	 */
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
		/**
		 * 
		 * ��buff�ж���ͷ��
		 * @param buff
		 */
	public void readByteBufferFromHeader()
	    {
		  System.out.println("---��buff�ӱ�ͷ����----------------"+buff.limit());
		  if(buff.remaining()>0)
		  {
			 
			//��һ����ͷ
	   	    if(buff.remaining()>8) 
	   	    {
                readHeader();
	   	    	System.out.println("****��ͷ�ڽ������������ȣ�"+datalen);
	   	    	//�ö�ʣ�µ���������
	   	    	readByteBufferFromData();
	 
	   	    }else if(buff.remaining()==8)
	   	    {
	   	    	//������ֻ�б�ͷ��Ϣ���ȴ���������
	   	        readHeader();
	   	    	if(ci==null)ci=new ClientInfo();
	   	    	ci.setHeader(header);
	   	    	ci.setDatalen(datalen);
	   	    	ci.setCatchType(1);
	   	    	ci.setCatchbuff(null);
	   	    	//��buff�ж�����
	   	    	
	   	    	
	   	    }else
	   	    {
	   	    	//ʣ�µ����ݻ�������
	   	    	byte []tem=new byte[buff.remaining()];
	   	    	buff.get(tem);
	   	    	if(null==ci)ci=new ClientInfo();
	   	    	ci.setCatchbuff(tem);
	   	    	ci.setHeader(null);
	   	    	ci.setDatalen(0);
	   	    	ci.setCatchType(1);
	   	    	//��buff�ж���ͷ
	   	    }
		  }
	    }


	/**
	    * ��buff�ж�������
	    * 
	    * @param buff
	    */
	    public void readByteBufferFromData()
	    {
	    	if(buff.remaining()>0)
	    	{
			    	if(buff.remaining()>datalen)
			    	{
			    		System.out.println("�ɴ��е����ݴ���������-------datalen--"+datalen);
			    		//��buff�ж���������
			    		readPackageFromByteBuff();
			    		//������buff�ж���ͷ
			    		readByteBufferFromHeader();
			    		
			
			    	}else if(buff.remaining()==datalen)
			    	{
			    		//��buff�ж���������,���������
			    		readPackageFromByteBuff();
			    		if(ci!=null)
			    		{
			    			ci.setCatchbuff(null);
			    			ci.setCatchType(0);
			    			ci.setDatalen(0);
			    			ci.setHeader(null);
			    		}
			    	}else
			    	{
			    		System.out.println("��һ��buff����ʣ���ݣ�������������buff.remain--"+buff.remaining());
			    		//���뻺��
			    		byte [] tem=new byte[buff.remaining()];
			    		buff.get(tem);
			    		if(null==ci)ci=new ClientInfo();
			    		ci.setCatchbuff(tem);
			    		ci.setCatchType(2);//��ʾ�����ݻ���
			    		ci.setDatalen(datalen);
			    		ci.setHeader(header);
			    		//���ڻ���������Ӧ�ôӱ�ͷ���𣬵�����ʾ֪
			    		//readCatchFromData();
			    		System.out.println("��һ��buff����ʣ���ݣ�������������buff.remain--"+buff.remaining());
			    	}
	    	}
	    }
		private void readPackageFromByteBuff() 
		{
			data=new byte[datalen];
    		//��������
    		buff.get(data,0,datalen);
    		//������һ����������
    		//&&&&&&&&&&&&
    		readedData(data);
	     }

		/**
		 * �ӻ����ж���ͷ��
		 */
		private void readCatchFromHeader() 
		{
			if(null!=ci&&ci.getCatchType()==1&&null!=ci.getCatchbuff()&&ci.getCatchbuff().length>0)
			{
					if(ci.getCatchbuff().length>8)
			   		{ 
						
						System.out.println("^^^^^^^^^^^ci.getCatchbuff()="+ci.getCatchbuff().length+"^^^^^^^^^^^^^^^^^^^");
			   			//����ͷ����������
						System.arraycopy(ci.getCatchbuff(), 0, header, 0, 8);
			   			datalen=parseHeaderToInt(header);
			   			System.out.println("header:-00----"+new String(header,0,4));
			   	    	
			   			//��������
		   				byte tem[]=new byte[ci.getCatchbuff().length-8];
		   				System.arraycopy(ci.getCatchbuff(), 8, tem, 0, tem.length);
		   
		   				//if(null==ci)ci=new ClientInfo();
			    		ci.setCatchbuff(tem);
			    		ci.setCatchType(2);//��ʾ�����ݻ���
			    		ci.setHeader(header);
			    		ci.setDatalen(datalen);
			    		
			    		readCatchFromData();

			   		}else if(ci.getCatchbuff().length==8)
			   		{
			   			System.out.println("^^^&&&&&&^^^^^^^^^^^^^^^^");
			   		//����ͷ����������
			   			header=new byte[8];
			   			System.arraycopy(ci.getCatchbuff(), 0, header, 0, 8);
			   			datalen=parseHeaderToInt(header);
			   			
			   			//if(ci==null)ci=new ClientInfo();
			   	    	ci.setHeader(header);
			   	    	ci.setDatalen(datalen);
			   	    	ci.setCatchType(1);
			   	    	ci.setCatchbuff(null);
			   			
			   			
			   		}
				
			}
			
		}

		/**
		 * �ӻ����ж�������
		 */
		private void readCatchFromData()
		{
			
		    if(null!=ci&&ci.getCatchType()==2&&null!=ci.getCatchbuff()&&ci.getCatchbuff().length>0)
		    {
		    	System.out.println("^^******^^^^^^^^^^^^^^^^^^^^^^");
		 
		    	if(ci.getCatchbuff().length>ci.getDatalen())
		    	{
		    		System.out.println("- ��ǰ��-"+ci.getCatchbuff().length+"-----ci.len="+ci.getDatalen()+"-------------------------------");
		    		
		    	    //�ӻ����ж���������
		    		readPackageFromCatch();
		    		System.out.println("(((((((((ci.getDatalen()"+ci.getDatalen());
		    		//�����µ����ݻ���Ϊ��ͷ
		    		byte tem[]=new byte[ci.getCatchbuff().length-ci.getDatalen()];
	    			System.arraycopy(ci.getCatchbuff(), ci.getDatalen(), tem, 0, tem.length);
	    			System.out.println("temlllll"+tem.length);
	    			//if(null==ci)ci=new ClientInfo();
	    			ci.setCatchbuff(tem);
	    			ci.setDatalen(0);
	    			ci.setCatchType(1);
	    			ci.setHeader(null);
	    			System.out.println("- ����-"+ci.getCatchbuff().length+"-----ci.len="+ci.getDatalen()+"-------------------------------");
	    			//�ӻ����ڶ���ͷ
		    		readCatchFromHeader();
           
		    	}else  if(ci.getCatchbuff().length==ci.getDatalen())
		    	{
		    		System.out.print("*");
		    		 //�ӻ����ж���������
		    		readPackageFromCatch();
		    		if(null!=ci)
		    		{
		    			ci.setCatchType(0);
		    			ci.setCatchbuff(null);
		    			ci.setHeader(null);
		    			ci.setDatalen(0);
		    		}
		    		//�����µ������ж���ͷ
		    		//readByteBufferFromHeader();
		    	}
		    }
		}	
		
   private void readPackageFromCatch() 
   {
	   
	   System.out.println("-------ci-------"+ci);
		//������
		data=new byte[ci.getDatalen()];
		System.arraycopy(ci.getCatchbuff(), 0, data, 0, ci.getDatalen());
		header=ci.getHeader();
	 	//���������-------------------------
		//&&&&&&&&&&&&
		//System.out.println(ci.getCatchbuff().length+"LLLLLL");
		readedData(data);
   }

public void marageBuff()
   {
 	     byte[]tem=new byte[buff.remaining()];
		 buff.get(tem);
		 byte[]newtem=new byte[tem.length+ci.getCatchbuff().length];
		 System.arraycopy(ci.getCatchbuff(), 0, newtem, 0, ci.getCatchbuff().length);
		 System.arraycopy(tem, 0, newtem, ci.getCatchbuff().length, tem.length);
		 ci.setCatchbuff(newtem);
		
   }
   private void readHeader() 
   {
	    	header=new byte[8];
	    	buff.get(header,0,8);
	    	datalen=parseHeaderToInt(header);
	    	
  }
   
   public void readedData(byte[]data)
   {
	    
	    if(null!=data&&new String(header,0,4).equals("~~^^"))
	     {
	    	System.out.println("-------"+data.length);
	    	  try {
				    //ByteArrayInputStream bin =new ByteArrayInputStream(data);
					FileOutputStream fo=new FileOutputStream("c:\\tt\\aa"+Math.random()+".gif");
					//ImageIO.write(ImageIO.read(bin), "gif", fo);
					BufferedOutputStream bo=new BufferedOutputStream(fo);
					bo.write(data);
					fo.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
			  data=null;
				 
			}
	     }
	   if(null!=data&&new String(header,0,4).equals("##$$"))
	   {
		   System.out.println("##$$:"+new String(data));
		   data =null;
	   }
	    
   }
}