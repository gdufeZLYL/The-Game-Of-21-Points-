package zqx.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

public class GameClient extends JFrame implements Runnable {

    //容器板
    JPanel contentPane;
    //信息发送框
    JTextField InfoTxf;
    //玩家列表滚动板
    JScrollPane sp1;
    //信息滚动板
    JScrollPane sp2;
    //卡牌组标签
    JLabel lb1;
    //发送标签
    JLabel lb2;
    //卡牌显示区
    JTextArea pukersTxa;
    //在线玩家显示区
    JList userTxa;
    //系统通知显示区
    JTextArea InfoTxa;
    //玩家模型，存储当前在线玩家
    DefaultListModel userModel;
    JScrollBar scrollBar1;
    JScrollBar scrollBar2;

    //玩家名称
    String name;
    //与服务端的连接socket
    Socket s;
    //输入输出流
    BufferedReader in;
    PrintWriter out;
    //是否已经发送回应
    volatile boolean isSended;


    /**
     * Launch the application.
     */
    //主方法,初始化登录界面
    public static void main(String[] args) {
        //初始化登录界面jf
        final JFrame jf = new JFrame("选择服务器");

        //设置界面的相应属性
        jf.setLocation(400, 200);
        jf.setSize(250, 210);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //添加界面上的组件
        JPanel jp = new JPanel();
        final JTextField ip = new JTextField(20);
        ip.setText("localhost");
        JButton jb = new JButton("确定");
        JLabel j1 = new JLabel("服务器地址");
        JLabel j2 = new JLabel("玩家昵称");
        final JTextField nicheng = new JTextField(20);
        jp.add(j1);
        jp.add(ip);
        jp.add(j2);
        jp.add(nicheng);
        jp.add(jb);
        jf.add(jp, BorderLayout.CENTER);
        jf.setVisible(true);

        //在"确定"按钮上添加事件侦听器,匿名内部类实现
        jb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                String ipaddr = ip.getText();

                //启动线程
                Thread trc = new Thread(new GameClient(nicheng.getText(), ipaddr));
                trc.start();
                jf.dispose();
            }
        });
    }

    /**
     * Create the frame.
     */
    public GameClient(String n, String ipad) {

        this.name = n;
        String ip = ipad;

        try {
            //使用指定的ip和端口9000，与服务端建立连接socket
            s = new Socket(ip, 9000);
            //获取连接上的输入,输出流
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream());
            //发送玩家昵称
            out.println(name);
            out.flush();
            //收到响应,Connection:True/False
            String ack = in.readLine();
            StringTokenizer st = new StringTokenizer(ack,":");
            //获取信息类型
            String type = st.nextToken();
            //获取信息True/False
            String accepted = st.nextToken();
            if(accepted.equals("False")) dispose();
            //System.out.println(accepted);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //初始化玩家模型
        userModel = new DefaultListModel();

        setTitle("\u7A97\u53E3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 610, 529);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        sp2 = new JScrollPane();

        sp1 = new JScrollPane();

        lb1 = new JLabel("\u5361 \u724C \u7EC4");
        lb1.setFont(new Font("宋体", Font.PLAIN, 23));

        lb2 = new JLabel("\u53D1    \u9001");
        lb2.setFont(new Font("宋体", Font.PLAIN, 23));

        pukersTxa = new JTextArea();
        pukersTxa.setEditable(false);
        pukersTxa.setRows(3);

        InfoTxf = new JTextField();
        InfoTxf.setColumns(10);
        //给信息发送框添加侦听器,使用匿名内部类实现
        InfoTxf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //获取玩家输入信息
                String text = InfoTxf.getText();
                InfoTxf.setText("");
                if(text.length() == 0 || (!text.equals("Yes") && !text.equals("No"))) new JOptionPane().showMessageDialog(null, "消息格式有误!");
                else {
                    //向服务器端发送信息
                    out.println("Accepted:"+text);
                    out.flush();
                    isSended = true;
                }
            }
        });

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(lb2)
                                                .addGap(18))
                                        .addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
                                                .addComponent(sp1, GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                                                .addGap(4))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(lb1, GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)))
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(pukersTxa, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                                        .addComponent(sp2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                                        .addComponent(InfoTxf, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                                .addGap(22))
        );
        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(sp2, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                        .addComponent(sp1))
                                .addGap(36)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lb1)
                                        .addComponent(pukersTxa, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lb2)
                                        .addComponent(InfoTxf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
        scrollBar2 = sp2.getVerticalScrollBar();
        sp2.setRowHeaderView(scrollBar2);

        InfoTxa = new JTextArea();
        InfoTxa.setEditable(false);
        sp2.setViewportView(InfoTxa);

        scrollBar1 = sp1.getVerticalScrollBar();
        sp1.setRowHeaderView(scrollBar1);

        userTxa = new JList();
        userTxa.setModel(userModel);
        sp1.setViewportView(userTxa);

        /*
        JScrollBar scrollBar1 = new JScrollBar();
        sp1.setRowHeaderView(scrollBar1);

        JTextArea userTxa = new JTextArea();
        userTxa.setEditable(false);
        sp1.setViewportView(userTxa);
        */
        contentPane.setLayout(gl_contentPane);
        setTitle(this.name+"的窗口");
        setVisible(true);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            while(true) {
                //获取服务器发送过来的信息
                String text = in.readLine();
                //解析信息,以分隔符":"分割字符串
                StringTokenizer st = new StringTokenizer(text,":");
                //获取信息的类型
                String type = st.nextToken();
                //获取信息
                String tx = st.nextToken();
                //根据信息类型的不同做出不同的处理
                //增加在线玩家
                if(type.equals("Add")) {
                    System.out.println(tx);
                    StringTokenizer ipadr = new StringTokenizer(tx, "/");
                    String user = ipadr.nextToken();
                    //如果添加的不是自己,则将其加入玩家在线列表
                    if(!(this.name.equals(user))) {
                        this.userModel.addElement(user);
                    }
                    //在消息框中显示玩家上线通知
                    this.InfoTxa.append(user+" "+ipadr.nextToken()+"进入房间!\n");
                    scrollBar2.setValue(InfoTxa.getHeight());
                }
                //删除在线玩家
                if(type.equals("Del")) {
                    System.out.println(tx);
                    //StringTokenizer ipadr = new StringTokenizer(tx, "/");
                    //String user = ipadr.nextToken();
                    this.userModel.removeElement(tx);
                    //在消息框中显示玩家上线通知
                    this.InfoTxa.append(tx+"离开房间!\n");
                    scrollBar2.setValue(InfoTxa.getHeight());
                }
                //普通消息
                if(type.equals("Text")) {
                    this.InfoTxa.append(tx+"\n");
                    scrollBar2.setValue(InfoTxa.getHeight());
                }
                //扑克牌消息
                if(type.equals("Puker")) {
                    this.pukersTxa.append(tx+" ");
                    scrollBar2.setValue(InfoTxa.getHeight());
                }
                //询问是否需要叫牌
                if(type.equals("Que")) {
                    this.InfoTxa.append(tx+"\n");
                    scrollBar2.setValue(InfoTxa.getHeight());
                    isSended = false;
                    while(isSended == false);
                }
                //游戏是否结束
                if(type.equals("Game")) {
                    pukersTxa.setText("");
                    InfoTxa.append("--------------分割线-----------------\n");
                    scrollBar2.setValue(InfoTxa.getHeight());
                }
                scrollBar2.setValue(InfoTxa.getHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
