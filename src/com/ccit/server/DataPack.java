package com.ccit.server;

import java.util.HashMap;
import java.util.Map;

import com.ccit.util.ByteIntSwitch;

public class DataPack 
{
   public static final int HEADLEN=8;
   //��ͷЭ��
   public static final String SCREENDATA="~~^^";
   public static final String USERPASSWD="##$$";
   
   public static final Map<String,String> PRO=new HashMap<String,String>();
   static{
	     PRO.put("SCREENDATA","~~^^");//����Ļ��ͼ����
	     PRO.put("USERPASSWD","##$$");//�ͻ����͵��û�������֤����
	     PRO.put("ISCAPTUR", "%%@@"); //������Ļ������֤
	   
   }
   
   //��ͷ����
   public byte[]header=null;//=new byte[HEADLEN];  
   public byte[]data=null;  //����
   private int datalen=0;    //���ݳ���

   public DataPack(){}
   public DataPack(byte []header,byte []data)
   {
	   this.header=header;
	   this.data=data;
   }
   
   public byte[] getHeader() {
	return header;
}
public void setHeader(byte[] header) {
	this.header = header;
	
}
public byte[] getData() {
	return data;
}
public void setData(byte[] data) {
	this.data = data;
	
}
public int getDatalen() {
	return datalen;
}
public void setDatalen(int datalen) {
	this.datalen = datalen;
}

public void initDatalen()
{
	  if(null!=header&&header.length==HEADLEN)
	  {
		  datalen=parseHeaderToInt(header);
		  //System.out.println("��ͷ��"+new String(header,0,4)+"----"+datalen);
	  }
	  if(null!=header&&header.length<HEADLEN)
	  {
		  datalen=0;
	  }
}
   public void destory()
   {
	  this.header=null;
	  this.data=null;
	  this.datalen=0;
   }
   
public int parseHeaderToInt(byte[]header)
{
	 if(null!=header&&header.length==HEADLEN)
	 {
		 
		 
		byte[]tem=new byte[4];
		System.arraycopy(header, 4, tem, 0, 4);
		return ByteIntSwitch.toInt(tem);
	 }else
	 {
		 return 0;
	 }
	 
} 

public boolean checkHeader()
{
	boolean re=false;
	if(null!=header&&header.length==DataPack.HEADLEN)
	{
		String str=new String(header,0,4);
		if(str.equals(SCREENDATA))
		{
			re=true;
		}
	}
  return re;	
}
public String getProtocl()
{
	
    String re=null;
	if(null!=header&&header.length==DataPack.HEADLEN)
	{
		
		re=new String(header,0,4);
	}
	return re;

}
   
}
