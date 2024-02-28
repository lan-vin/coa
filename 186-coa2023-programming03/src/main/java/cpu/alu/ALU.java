package cpu.alu;

import util.DataType;
import util.Transformer;

import java.util.Objects;

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
    public DataType sub(DataType src, DataType dest) {
        // TODO
        String negSrc=negHandle(src.toString());//减数取反
        DataType temp=new DataType(negSrc);
        DataType extra1=new DataType(dest.fill32bit("1"));
        String ans=add(temp,extra1).toString();//取反再加一
        temp=new DataType(ans);
        return new DataType(add(temp,dest).toString());
    }
    /**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType mul(DataType src, DataType dest) {
        //TODO
        DataType P=new DataType(src.fill32bit("0"));//部分乘积p
        String destValue=dest.toString();
        DataType extra1=new DataType(dest.fill32bit("1"));//-x的补码
        DataType negDest=add(new DataType(this.negHandle(destValue)),extra1);//-X
        int cnt=0;
        String extraY="0";//乘数的扩展位Y-1
        while(cnt<32){
            String temp=src.toString();
            if(!temp.substring(temp.length() - 1).equals(extraY)){
                if(extraY.equals("0")){//"10"结尾，-x
                    P=new DataType(add(P,negDest).toString());//复写部分积
                }
                else{//"01"结尾，+x
                    P=new DataType(add(P,dest).toString());
                }
            }
            String mulValue=P.toString();
            extraY=temp.substring(temp.length()-1);//复写扩展位
            src=new DataType(mulValue.charAt(mulValue.length()-1)+temp.substring(0,temp.length()-1));//P的末位给Y的首位
            P=new DataType(mulValue.charAt(0)+mulValue.substring(0,mulValue.length()-1));
            cnt++;
        }
        return src;
    }

    DataType remainderReg = new DataType("00000000000000000000000000000000");

    /**
     * 返回两个二进制整数的除法结果
     * dest ÷ src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType div(DataType src, DataType dest) {
        //TODO
        remainderReg = new DataType("00000000000000000000000000000000");
        if(Objects.equals(dest.toString(), remainderReg.toString()) &&!Objects.equals(src.toString(), remainderReg.toString())){//被除数为0，除数不为0
            DataType ans=new DataType(remainderReg.toString());
            remainderReg=dest;
            return ans;
        }
        else if(Objects.equals(src.toString(), remainderReg.toString())){//除数为0，被除数不为0
            throw new ArithmeticException();
        }
        boolean flag= src.toString().charAt(0) == dest.toString().charAt(0);//判断最后商是否不用求补的标志
        DataType R;
        if(dest.toString().startsWith("0"))R=new DataType("00000000000000000000000000000000");
        else R=new DataType("11111111111111111111111111111111");//R为被除数的高位扩展
        String Qn;//将要上的一位商
        for(int i=0;i<32;i++){
            R=new DataType(R.toString().substring(1)+dest.toString().charAt(0));//R左移
            char temp= R.toString().charAt(0);
            if(temp==src.toString().charAt(0)) R=sub(src,R);//同号，R-Y
            else R=add(R,src);//异号，R+Y
            if((Objects.equals(R.toString(), "00000000000000000000000000000000")&&!dest.toString().substring(1,32-i).contains("1")) ||temp==R.toString().charAt(0))Qn="1";//R操作未变号，商1
            else {
                Qn="0";
                if(temp==src.toString().charAt(0))R=add(src,R);//恢复余数
                else R=sub(src,R);
            }
            dest=new DataType(dest.toString().substring(1)+Qn);//Q左移一位，商在Q的末位
        }
        if(!flag) {
            DataType extra1=new DataType(dest.fill32bit("1"));
            dest=add(new DataType(this.negHandle(dest.toString())),extra1);
        }
        remainderReg=R;
        return dest;
    }

}
