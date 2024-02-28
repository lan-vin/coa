package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;
import util.Transformer;

/**
 * TODO 最近最少用算法
 */
public class LRUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {//命中了把其他有效行增加
        //TODO
        Cache sample=Cache.getCache();
        sample.resetStamp(rowNO);
        for (int i=0;i<Cache.CACHE_SIZE_B/Cache.LINE_SIZE_B;i++){
            if(!sample.isValid(i)||i==rowNO){
                continue;
            }
            sample.setTimeStamp(i);
        }
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        //TODO
        Cache sample=Cache.getCache();
        long max=sample.getTimeStamp(start);
        int maxLine=start;
        for (int i=start;i<end;i++){
            long temp=sample.getTimeStamp(i);
            if(!sample.isValid(i)){
                maxLine=i;
                sample.update(maxLine,addrTag,input);
                sample.setTimeStampFIFO(maxLine);
                return maxLine;
            }
            if(temp>max){
                maxLine=i;//找到时间戳最大的那一行并替换
                max=temp;
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
        hit(maxLine);
        return maxLine;
    }

}





























