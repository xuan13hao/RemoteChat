package com.ccit.util;


public final class ByteIntSwitch {

    public static void main(String args[] ) {
        int i = 212123;
        byte[] b = toByteArray(i, 4);   //���͵��ֽڣ�

       System.out.println( "212123 bin: " + Integer.toBinaryString(212123));//212123�Ķ����Ʊ�ʾ
       System.out.println( "212123 hex: " + Integer.toHexString(212123));  //212123��ʮ�����Ʊ�ʾ  

        for(int j=0;j<4;j++){
              System.out.println("b["+j+"]="+b[j]);//�ӵ�λ����λ���,java��byte��Χ��-128��127
        }
       
        int k=toInt(b);//�ֽڵ����ͣ�ת������
        System.out.println("byte to int:"+k); 
      
    }

    
    // ��iSourceתΪ����ΪiArrayLen��byte���飬�ֽ�����ĵ�λ�����͵ĵ��ֽ�λ
    public static byte[] toByteArray(int iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for ( int i = 0; (i < 4) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte)( iSource>>8*i & 0xFF );
          
        }
        return bLocalArr;
    }   

     // ��byte����bRefArrתΪһ������,�ֽ�����ĵ�λ�����͵ĵ��ֽ�λ
    public static int toInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;
        
        for ( int i =0; i<4 ; i++) {
            bLoop = bRefArr[i];
            iOutcome+= (bLoop & 0xFF) << (8 * i);
          
        }  
        
        return iOutcome;
    }   
    
}  
