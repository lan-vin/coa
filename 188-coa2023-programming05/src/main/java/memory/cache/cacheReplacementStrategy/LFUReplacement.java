package memory.cache.cacheReplacementStrategy;


import memory.Memory;
import memory.cache.Cache;
import util.Transformer;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        //TODO
        Cache.getCache().addVisited(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        //TODO
        Cache sample=Cache.getCache();
        long max=sample.getVisited(start);
        int maxLine=start;
        for (int i=start;i<end;i++){
            long temp=sample.getVisited(i);
            if(!sample.isValid(i)){
                maxLine=i;
                sample.update(maxLine,addrTag,input);
                sample.addVisited(maxLine);
                return maxLine;
            }
            if(temp<max){
                max=temp;
                maxLine=i;//找到访问次数最小的那一行并替换
            }
        }
        if(Cache.isWriteBack){
            if(sample.isDirty(maxLine)){
                String len=Transformer.intToBinary(String.valueOf(sample.getSETS()));
                while (len.startsWith("0"))len=len.substring(1);
                String block=String.copyValueOf(sample.getTag(maxLine))+ Transformer.intToBinary(String.valueOf(maxLine/ sample.getSetSize())).substring(32-(len.length()-1))+"000000";
                while (block.length()>32)block=block.substring(1);
                Memory.getMemory().write(block,Cache.LINE_SIZE_B, sample.getData(maxLine));
            }
        }
        sample.update(maxLine,addrTag,input);
        sample.resetVisited(maxLine);
        return maxLine;
    }

}
