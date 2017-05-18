package zqx.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

/**
 * Created by adc333 on 2017/4/29.
 */
public class Gaming implements Runnable {
    //当前客户端socket
    Socket s;
    //当前客户端name
    String name;
    //当前socket集合
    Map sockets;
    //当前回合剩余的卡牌
    LinkedList currentPukers;
    //当前回合玩家拥有的卡牌
    Map currentSituations;
    //初始化共享变量:当前游戏状态
    ShareVar shareVar;

    public Gaming(Socket s, String name, Map sockets, LinkedList currentPukers, Map currentSituations, ShareVar shareVar) {
        this.s = s;
        this.name = name;
        this.sockets = sockets;
        this.currentPukers = currentPukers;
        this.currentSituations = currentSituations;
        this.shareVar = shareVar;
    }

    @Override
    public void run() {
        try {
            //获取新连接的输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
            //获取该连接上的输出流
            PrintWriter out = new PrintWriter(this.s.getOutputStream());

            //每一次循环代表一回合游戏
            while(true) {
                //System.out.println("yes");
                /*
                if(this.sockets.size() < this.shareVar.MAXNUM) {
                    out.println("Text:请耐心等待其他玩家进入房间.");
                    out.flush();
                }
                */
                while(this.sockets.size() < this.shareVar.MAXNUM) {
                    out.println("Text:请耐心等待其他玩家进入房间.");
                    out.flush();
                    Thread.sleep(10000);
                }
                //游戏开始,每一回合游戏只能开始一次,所以上锁
                synchronized (this.currentPukers) {
                    //如果还没开始游戏,则初始化游戏,反之则不初始化游戏
                    if(this.shareVar.isPlaying == 0) {
                        //设置当前游戏中
                        this.shareVar.isPlaying = 1;
                        //设置当前游戏需询问3个人
                        this.shareVar.waitPukers = this.sockets.size();
                        //设置当前0玩家还没从这回合游戏结束
                        this.shareVar.readyPlayers = 0;
                        //设置每个玩家卡牌为空
                        this.currentSituations.clear();
                        //顺序初始化卡牌
                        for(int i = 1; i <= 13; i++) {
                            for(int j = 1; j <= 4; j++) {
                                this.currentPukers.add(i);
                            }
                        }
                        //洗牌
                        Collections.shuffle(this.currentPukers);
                        //发牌
                        for(Object object : sockets.entrySet()) {
                            Map.Entry entry = (Map.Entry) object;
                            //获取所有在线玩家的昵称name
                            String name1 = (String) entry.getKey();
                            //获取所有在线玩家的连接socket
                            Socket s1 = (Socket) entry.getValue();

                            //获取该连接上的输出流
                            PrintWriter out1 = new PrintWriter(s1.getOutputStream());
                            //提醒所有玩家游戏开始
                            out1.println("Text:游戏开始!每位玩家将得到2张首发牌.");
                            out1.flush();
                            //该玩家手上的卡牌
                            String p = "";
                            for(int i = 1; i <= 2; i++) {
                                //得到的卡牌号
                                int pn = (Integer) currentPukers.poll();
                                //卡牌号转字符串:1->A, 10->0, 11->J...
                                String pns;
                                if(pn == 1) pns = "A";
                                else if(pn == 10) pns = "0";
                                else if(pn == 11) pns = "J";
                                else if(pn == 12) pns = "Q";
                                else if(pn == 13) pns = "K";
                                else pns = String.valueOf(pn);
                                //提醒玩家获得卡牌
                                out1.println("Text:你获得卡牌"+(pns.equals("0") ? "10" : pns));
                                out1.flush();
                                out1.println("Puker:"+(pns.equals("0") ? "10" : pns));
                                out1.flush();
                                //将卡牌加入玩家卡牌组
                                p += pns;
                            }
                            //更新玩家卡牌组
                            this.currentSituations.put(name1, p);
                        }
                    }
                }
                //不断询问玩家,知道玩家不需要牌,每次只能询问一个玩家是否发牌,所以上锁
                synchronized (this.currentPukers) {
                    while(true) {
                        out.println("Que:是否需要继续发牌?(Yes/No)");
                        out.flush();
                        //得到玩家的回答
                        String text = in.readLine();
                        //根据客户端发送时指定的分隔符":",分割该信息
                        StringTokenizer st = new StringTokenizer(text, ":");
                        //得到协议的类型
                        String type = st.nextToken();
                        //
                        if(type.equals("Accepted")) {
                            //得到玩家的回应:Yes/No
                            String answer = st.nextToken();

                            if(answer.equals("Yes")) {
                                //获得该玩家的卡牌
                                String p = (String) this.currentSituations.get(this.name);
                                //得到的卡牌号
                                int pn = (Integer) this.currentPukers.poll();
                                //卡牌号转字符串:1->A, 10->0, 11->J...
                                String pns;
                                if(pn == 1) pns = "A";
                                else if(pn == 10) pns = "0";
                                else if(pn == 11) pns = "J";
                                else if(pn == 12) pns = "Q";
                                else if(pn == 13) pns = "K";
                                else pns = String.valueOf(pn);
                                //提醒玩家获得卡牌
                                out.println("Text:你获得卡牌"+(pns.equals("0") ? "10" : pns));
                                out.flush();
                                out.println("Puker:"+(pns.equals("0") ? "10" : pns));
                                out.flush();
                                //将卡牌加入玩家卡牌组
                                p += pns;
                                //更新该玩家的卡牌
                                this.currentSituations.put(this.name, p);
                            } else {
                                //提示玩家发牌已结束
                                out.println("Text:您的发牌回合已结束!请耐心等待其他玩家发牌回合!");
                                out.flush();
                                this.shareVar.waitPukers = this.shareVar.waitPukers - 1;
                                break;
                            }

                        } else {
                            out.println("Text:指令不对!请重新输入!");
                            out.flush();
                        }
                    }
                }

                //等待其他玩家发牌回合结束
                while (this.shareVar.waitPukers > 0) {
                    out.println("Text:请耐心等待游戏最终结果!");
                    out.flush();
                    Thread.sleep(10000);
                    //System.out.println(this.shareVar.waitPukers);
                }

                //玩家发牌回合结束,开始计算分数决出胜负,结果只能计算一次,所以加锁
                synchronized (this.currentSituations) {
                    //如果游戏还没结束,则计算
                    if(this.shareVar.isPlaying == 1) {
                        //游戏结束
                        this.shareVar.isPlaying = 0;
                        //记录最高分的玩家昵称,默认平局
                        String resultName = "NONE";
                        //记录最高分
                        int resultScores = 0;
                        //各玩家的得分
                        HashMap<String, Integer> allPlayersScores = new HashMap<String, Integer>();
                        //游戏结果
                        String finalResult = "Text:各玩家最终得分为";
                        //遍历所有玩家计算分数
                        for(Object object : this.sockets.entrySet()) {
                            Map.Entry entry2 = (Map.Entry) object;
                            //获取所有玩家的昵称name
                            String name2 = (String) entry2.getKey();
                            //得到该玩家的卡牌组
                            String pukers2 = (String) this.currentSituations.get(name2);
                            //得到该玩家卡牌组的卡牌数量
                            int pukersNum2 = pukers2.length();
                            //卡牌A的个数
                            int numOfA2 = 0;
                            //该玩家除了卡牌A的得分
                            int score2 = 0;
                            //计算玩家的得分
                            for(int i = 0; i < pukersNum2; i++) {
                                if(pukers2.charAt(i) == 'A') numOfA2++;
                                else {
                                    if(pukers2.charAt(i) == '0') score2 += 10;
                                    else if(pukers2.charAt(i) == 'J') score2 += 10;
                                    else if(pukers2.charAt(i) == 'Q') score2 += 10;
                                    else if(pukers2.charAt(i) == 'K') score2 += 10;
                                    else score2 += (pukers2.charAt(i)-'0');
                                }
                            }
                            System.out.println(name2+", "+score2);
                            //该玩家的最终得分
                            int finalScore2 = 0;
                            //遍历卡牌A的所有情况,选择最优的,通过二进制代表所有情况,0代表1,1代表11
                            for(int i = 0; i < (1<<numOfA2); i++) {
                                int temp = 0;
                                for(int j = 0; j < numOfA2; j++) {
                                    if(((i>>j)&1) == 1) temp += 11;
                                    else temp += 1;
                                }
                                if(score2+temp <= 21) finalScore2 = Math.max(finalScore2, score2+temp);
                            }
                            //更新最高分玩家
                            if(resultScores < finalScore2) {
                                resultScores = finalScore2;
                                resultName = name2;
                            }
                            //插入玩家最终得分
                            allPlayersScores.put(name2, finalScore2);
                            finalResult += (name2+"得分为"+finalScore2+", ");
                        }

                        //通知所有玩家游戏结果
                        for(Object object : this.sockets.entrySet()) {
                            Map.Entry entry3 = (Map.Entry) object;
                            //获取所有玩家的socket
                            Socket s3 = (Socket) entry3.getValue();
                            //获取该连接上的输出流
                            PrintWriter out3 = new PrintWriter(s3.getOutputStream());
                            out3.println(finalResult);
                            out3.flush();
                            if(!resultName.equals("NONE"))out3.println("Text:恭喜"+resultName+"赢得游戏,"+"得分为"+resultScores);
                            else out3.println("Text:平局");
                            out3.flush();
                            out3.println("Game:over");
                            out3.flush();
                        }

                    }
                    this.shareVar.readyPlayers = this.shareVar.readyPlayers + 1;
                }

                //等待所有玩家这回合游戏结束
                while (this.shareVar.readyPlayers != this.sockets.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(this.shareVar.readyPlayers != 0) this.shareVar.readyPlayers = this.shareVar.readyPlayers - 1;
                this.shareVar.waitPukers = this.shareVar.waitPukers - 1;

                //当登录的玩家退出后,连接socket便断开,以后代码会被执行
                //从容器中移除该连接socket
                this.sockets.remove(name);

                //获取容器中其他在线用户的活动连接
                Collection value = sockets.values();
                Iterator it = value.iterator();

                while(it.hasNext()) {
                    //获取连接socket
                    Socket s2 = (Socket) it.next();

                    //获取该连接的输出流
                    PrintWriter pw = new PrintWriter(s2.getOutputStream());

                    //发送数据,并指定为"Del"类型
                    pw.println("Del:"+name);
                    pw.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
