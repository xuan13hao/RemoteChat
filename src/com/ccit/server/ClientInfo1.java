package com.ccit.server;

public class ClientInfo1 
{
	//0��ʾ���ͷ�1��ʾ���ܷ�
   private int clientType=0; 
   //0��ʾû�л���1��ͷ����2ͼƬ���ݻ���
   private DataPack dp=null;
   public ClientInfo1(){}
   public ClientInfo1(int clientType,DataPack dp)
   {
	   this.clientType=clientType;
	   this.dp=dp;
   }
	public int getClientType() {
		return clientType;
	}
	public void setClientType(int clientType) {
		this.clientType = clientType;
	}
	public DataPack getDp() {
		return dp;
	}
	public void setDp(DataPack dp) {
		this.dp = dp;
	}
   
}
