package memory.disk;


public class Scheduler {

    /**
     * 先来先服务算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        //TODO
        double ans=0;
        for (int j : request) {
            ans += Math.abs(j - start);
            start = j;
        }
        return ans /request.length;
    }

    /**
     * 最短寻道时间优先算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        //TODO
        double ans=0;
        boolean[] visit=new boolean[request.length];
        for (int k : request) {
            int temp = 0;
            int min=1000;
            for (int j=0; j < request.length; j++) {
                if (Math.abs(start - request[j]) < min && !visit[j]) {
                    min=start - request[j];
                    temp=j;
                }
            }
            ans += Math.abs(start - request[temp]);
            start= request[temp];
            visit[temp] = true;
        }
        return ans/request.length;
    }

    /**
     * 扫描算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        //TODO
//        int biggest=0;
//        int smallest=512;
//        int ans;
//        for (int j : request) {
//            if (j > biggest) biggest = j;
//            if (j < smallest) smallest = j;
//        }
//        if(direction){
//            if(smallest>=start)ans=biggest-start;
//            else ans=255-smallest+255-start;
//        }
//        else{
//            if(biggest<=start)ans=start-smallest;
//            else ans=biggest+start;
//        }
//        return (double) ans/ request.length;
        double ans=0;
        int max=0;
        int min=1000;
        for (int j:request){
            if(j>max)max=j;
            if(j<min)min=j;
        }
        if(direction){
            boolean bigger=true;
            for (int j : request) {
                if (j < start) {
                    bigger = false;
                    break;
                }//判断是否都大于start
            }
            if(bigger)ans=max-start;
            else ans=255-start+255-min;
        }
        else{
            boolean smaller=true;
            for (int j:request){
                if(j>start){
                    smaller=false;
                    break;
                }
            }
            if(smaller)ans=ans-min;
            else ans=start+max;
        }
        return ans/ request.length;
    }

    /**
     * C-SCAN算法：默认磁头向磁道号增大方向移动
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @return 平均寻道长度
     */
    public double CSCAN(int start,int[] request){
        //TODO
        int small_close=start;
        int ans;
        int len=512;
        int biggest=0;
        for (int j : request) {
            if (j < start && start - j < len) {
                len = start - j;
                small_close = j;
            }
            if(j>biggest)biggest=j;
        }
        if(small_close==start)ans=biggest-start;
        else if(biggest<=start)ans=start+small_close;
        else ans=255+255-start+small_close;
        return (double) ans/ request.length;

    }

    /**
     * LOOK算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double LOOK(int start,int[] request,boolean direction){
        //TODO
        int biggest=0;
        int smallest=512;
        int ans;
        for (int j : request) {
            if (j > biggest) biggest = j;
            if (j < smallest) smallest = j;
        }
        if(direction){
            if(smallest>=start)ans=biggest-start;
            else if(biggest<=start)ans=start-smallest;
            else ans=biggest-smallest+biggest-start;
        }
        else{
            if(biggest<=start)ans=start-smallest;
            else if(smallest>=start)ans=biggest-start;
            else ans=biggest-smallest+start-smallest;
        }
        return (double) ans/ request.length;
    }

    /**
     * C-LOOK算法：默认磁头向磁道号增大方向移动
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @return 平均寻道长度
     */
    public double CLOOK(int start,int[] request){
        //TODO
        int small_close=start;
        int ans;
        int len=512;
        int biggest=0;
        int smallest=512;
        for (int j : request) {
            if (j < start && start - j < len) {
                len = start - j;
                small_close = j;
            }
            if(j>biggest)biggest=j;
            if(j<smallest)smallest=j;
        }
        if(small_close==start)ans=biggest-start;
        else if(biggest<=start)ans=biggest-smallest+start-smallest;
        else ans=biggest-smallest+biggest-start+small_close-smallest;
        return (double) ans/ request.length;
    }

}
