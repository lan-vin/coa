package cpu.alu;

import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {
    public String negHandle(String src){
        StringBuilder ans=new StringBuilder();
        for (int i=0;i<src.length();i++){
            if(src.charAt(i)=='0')ans.append("1");
            else ans.append("0");
        }
        return ans.toString();
    }
    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */

    public DataType add(DataType src, DataType dest) {
        // TODO
        String srcValue=src.toString();
        String destValue=dest.toString();
        StringBuilder re=new StringBuilder();
        int c=0;
        for (int i=31;i>=0;i--){
            String temp=(srcValue.charAt(i)==destValue.charAt(i)?"0":"1");
            re.append(temp.equals(String.valueOf(c)) ?"0":"1");//两次异或求F
            if((destValue.charAt(i)=='0'&&(srcValue.charAt(i)=='0'||c==0))||(srcValue.charAt(i)=='0'&&c==0))c=0;
            else c=1;
        }
        return new DataType(re.reverse().toString());
    }

    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
        String negSrc=negHandle(src.toString());//减数取反
        DataType temp=new DataType(negSrc);
        DataType extra1=new DataType(dest.fill32bit("1"));
        String ans=add(temp,extra1).toString();//取反再加一
        temp=new DataType(ans);
        return new DataType(add(temp,dest).toString());
    }

}
