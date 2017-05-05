package com.ccit.recoder;

import java.awt.AWTException;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import com.ccit.util.MouseHook;
import com.ccit.util.Mousexy;

public class Capture extends JFrame {

	private static final long serialVersionUID = -1051401344209478733L;
	private JButton recode=new JButton("��������");
	private JButton stop=new JButton("ֹͣ����");
	public RecodeScreen recodescreen;
	private boolean isshare=false;
	private SystemTray systemTray=null;
	private Image icon=null;
    public Capture()
    {
    	super("����Զ�����湲���V1.0");
    	recodescreen=new RecodeScreen(this);
    	
    	this.setSize(300, 80);
    	this.setLocation(660, 40);
    	this.setResizable(false);
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        this.add(recode);
        this.add(stop);
        //���ð�Ŧ״̬
        this.setButtonStatus();
        URL url = getClass().getResource("/com/ccit/res/tryicon.png");
        icon=Toolkit.getDefaultToolkit().createImage(url);
        this.setIconImage(icon);
        initSystemTray();
        recode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String pwd=JOptionPane.showInputDialog(Capture.this, "�����빲�����룡");
				if(null!=pwd&&"xaccitpc".equals(pwd))
				{
				Capture.this.setVisible(false);
				isshare=true;
				setButtonStatus();
				recodescreen.start();
				}else
				{
					JOptionPane.showMessageDialog(Capture.this, "���벻��ȷ������������");
				}
			}
		});
        stop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				isshare=false;
				setButtonStatus();
				recodescreen.interrupt();
				
			}
		});
        
        this.setVisible(true);
       // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

    }
   
    public void setButtonStatus()
    {
    	if(isshare)
    	{
    		recode.setEnabled(false);
    	}else
    	{
    		stop.setEnabled(true);
    	}
    	
    }
    /**
	 * ��ʼ��ϵͳ���̵ķ���
	 */
	private void initSystemTray() {
		if (SystemTray.isSupported())
			systemTray = SystemTray.getSystemTray();
		
		TrayIcon trayIcon = new TrayIcon(icon);
		trayIcon.setToolTip("����Զ�����湲���V1.0");
		trayIcon.setImageAutoSize(true);
		
		PopupMenu popupMenu = new PopupMenu("���̲˵�");

		// ������ʾ������˵���
		MenuItem showMenuItem = new MenuItem("��ʾ������");
		showMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Capture.this.setExtendedState(JFrame.NORMAL);
				Capture.this.setVisible(true);
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

    
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
	   new Capture();

	}

}
