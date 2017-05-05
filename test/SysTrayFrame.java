

import java.awt.BorderLayout;

import javax.swing.JFrame;

import javax.swing.JPanel;

import javax.swing.JButton;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import javax.swing.*;

import java.awt.event.*;

import java.awt.*;

public class SysTrayFrame extends JFrame {

private static final long serialVersionUID = 3577594605021848256L;

private TrayIcon trayIcon;

   BorderLayout borderLayout1 = new BorderLayout();
   JPanel root = new JPanel();
   JButton exit = new JButton();
    public SysTrayFrame()
    {
       try {
           jbInit();
           pack();
           this.initTrayIcon();
       } catch (Exception exception) 
       {
           exception.printStackTrace();
       }

   }

    private void jbInit() throws Exception
    {

       this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
       this.addWindowListener(new WindowAdapter(){

           //���񴰿ڹر��¼�

           public void windowClosing(WindowEvent e)
           {           
               if(SystemTray.isSupported())
               {
                   setVisible(false);
                   minimizeToTray();
               }

               else{

                   System.exit(0);

               }

           }

           //���񴰿���С���¼�

           public void windowIconified(WindowEvent e){

               if(SystemTray.isSupported()){

                   setVisible(false);

                   minimizeToTray();

               }

               else{

                   System.exit(0);

               }

           }

       });

       getContentPane().setLayout(borderLayout1);

       exit.setText("exit");

       exit.addActionListener(new SysTrayFrame_exit_actionAdapter(this));

       this.getContentPane().add(root, java.awt.BorderLayout.CENTER);

       root.add(exit);

   }  
   private void initTrayIcon(){

       Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/com/ccit/res/icon.png"));
       PopupMenu popup = new PopupMenu();
       MenuItem exitItem = new MenuItem("Show");
       ActionListener listener = new ActionListener(){
           public void actionPerformed(ActionEvent e) {
               setVisible(true);
               setExtendedState(Frame.NORMAL);
               SystemTray.getSystemTray().remove(trayIcon);
            }

       };

       exitItem.addActionListener(listener);

       popup.add(exitItem);

       //����image����ʾ���˵�����TrayIcon

       this.trayIcon = new TrayIcon(image,"MyTray",popup);

       //��TrayIcon����¼�������

       this.trayIcon.addActionListener(listener);       
   }

    public void minimizeToTray()
    {
       SystemTray tray = SystemTray.getSystemTray();
        try {
           tray.add(this.trayIcon);
       } catch (AWTException ex) {
           ex.printStackTrace();
       }
   }

    public static void main(String[] args) 
    {
       SysTrayFrame systrayframe = new SysTrayFrame();
       systrayframe.setTitle("MyTray");
       systrayframe.setVisible(true);
   }

    public void exit_actionPerformed(ActionEvent e) 
    {
       System.exit(0);
   }
}

class SysTrayFrame_exit_actionAdapter implements ActionListener 
{
   private SysTrayFrame adaptee;
   SysTrayFrame_exit_actionAdapter(SysTrayFrame adaptee) 
   {
       this.adaptee = adaptee;
   }

    public void actionPerformed(ActionEvent e) 
    {
       adaptee.exit_actionPerformed(e);
    }

}
 
