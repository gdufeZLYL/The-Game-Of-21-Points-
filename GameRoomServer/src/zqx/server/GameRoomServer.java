package zqx.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by adc333 on 2017/4/29.
 * 管理服务器端与客户端的活动连接
 */
public class GameRoomServer {

    public static void main(String[] args) throws Exception {
        //服务器端ServerSocket, 绑定端口9000
        ServerSocket ss = new ServerSocket(9000);

        //容器Sockets用来保存服务器与客户端的活动连接, "键"为name, "值"为socket
        Map sockets = new ConcurrentHashMap();

        //当前回合剩余的卡牌, Integer
        LinkedList currentPukers = new LinkedList();

        //当前回合玩家拥有的卡牌,"键"为name, "值"为卡牌连接成String
        Map currentSituations = new HashMap();

        //初始化共享变量:当前回合数
        ShareVar shareVar = new ShareVar();

        while(true) {
            //响应客户端的连接,accept方式是阻塞式的
            Socket s = ss.accept();

            //获取连接上的输入流in和输出流out, 并包装
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream());
            String name = in.readLine();

            //判断是否拒绝玩家进入
            if(sockets.size() >= shareVar.MAXNUM || shareVar.isPlaying != 0) {
                //当前房间人数已满,或者当前游戏中,拒绝玩家进入房间
                out.println("Connection:False");
                out.flush();
                continue;
            } else {
                //同意玩家进入房间
                out.println("Connection:True");
                out.flush();
                //if(sockets.size() >= shareVar.MAXNUM) sockets.notifyAll();
            }


            //获取客户端的IP地址
            String IP = s.getInetAddress().toString();
            System.out.println(name+":"+IP);

            //获取所有活动连接的socket集合
            Collection values = sockets.values();

            //迭代遍历每一个socket
            Iterator it = values.iterator();
            while(it.hasNext()) {
                //获取socket连接
                Socket s1 = (Socket) it.next();
                //获取该连接上的输入流
                PrintWriter pw = new PrintWriter(s1.getOutputStream());
                //通知该玩家,新玩家上线
                pw.println("Add:"+name+IP);
                pw.flush();
            }

            //将新用户添加到容器sockets中
            sockets.put(name, s);

            //获取容器sockets中所有的"键"集合,以便通知新用户谁在线
            Set names = sockets.keySet();
            it = names.iterator();
            while(it.hasNext()) {
                //获取已登录玩家的昵称
                String loginedUser = (String) it.next();
                //将该登录玩家通知新玩家
                out.println("Add:"+loginedUser+IP);
                out.flush();
            }

            //启动新线程,将新用户名,连接,容器传递过去
            //System.out.println("yes");
            Thread tt = new Thread(new Gaming(s, name, sockets, currentPukers, currentSituations, shareVar));
            tt.start();
        }
    }
}
