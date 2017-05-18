package zqx.server;

/**
 * Created by adc333 on 2017/4/29.
 * 存储一些共享变量
 */
public class ShareVar {

    //房间容纳人数
    public static final int MAXNUM = 3;

    //当前是否游戏中,1代表游戏中,0代表游戏未开始
    public volatile int isPlaying;

    //当前剩余玩家未发牌
    public volatile int waitPukers;

    //当前剩余玩家还没从这次回合中结束
    public volatile int readyPlayers;

    public ShareVar() {
        this.isPlaying = 0;
        this.waitPukers = this.MAXNUM;
        this.readyPlayers = 0;
    }
}
