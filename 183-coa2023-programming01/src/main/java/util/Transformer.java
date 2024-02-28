package util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.pow;

public class Transformer {
    public static String[] twist(String[] bits){
        boolean flag=false;
        for (int i=bits.length-1;i>=0;i--){
            bits[i]= Objects.equals(bits[i], "0") ?"1":"0";
            if(flag){
                if(Objects.equals(bits[i], "0"))flag=false;
                bits[i]= Objects.equals(bits[i], "0") ?"1":"0";
            }
            if(i==bits.length-1){
                if(Objects.equals(bits[i], "0"))bits[i]="1";
                else{
                    bits[i]="0";
                    flag=true;
                }
            }
        }
        return bits;
    }
    public static String intToBinary(String numStr) {
        // TODO:
        StringBuilder ans= new StringBuilder();
        int target=Integer.parseInt(numStr);//转成十进制数
        if(target<0){
            String con=intToBinary(String.valueOf(-target-1));//找刚好按位取反的二进制
            String[] bit=con.split("");
            for (int i=0;i<con.length();i++){
                bit[i]= Objects.equals(bit[i], "0") ?"1":"0";
            }
            for(int i=0;i<con.length();i++){
                ans.append(bit[i]);
            }//按位取反即可
        }
        else{
            String temp= Integer.toString(target,2);
            for(int i=0;i<32-temp.length();i++){
                ans.append("0");
            }
            ans.append(temp);
            if(!numStr.contains("-"))return ans.toString();
            LinkedList arr=new LinkedList<>();

        }
        return ans.toString();
    }


    public static String binToUncInt(String binStr){
        return Integer.toString( Integer.parseInt(binStr,2),10);
    }
    public static String binaryToInt(String binStr) {
        // TODO:
        if(binStr.startsWith("1")){
            StringBuilder ans= new StringBuilder();
            ans.append("-");
            String[] bin=binStr.split("");
            bin=twist(bin);
            for (String s : bin) {
                ans.append(s);
            }
            return binToUncInt(ans.toString());
        }
        else return binToUncInt(binStr);
    }

    public static String decimalToNBCD(String decimalStr) {
        // TODO:
        StringBuilder ans= new StringBuilder();
        if(decimalStr.contains("-")) ans.append("1101");
        else ans.append("1100");
        for (int i=4*decimalStr.length();i<28;i++){
            ans.append("0");
        }
        String[] temp=decimalStr.split("");//按位获取
        for(int i=0;i<decimalStr.length();i++){
            int target=Integer.parseInt(temp[i]);
            ans.append(String.format("%04d",Integer.parseInt(Integer.toString(target,2))));
        }
        return ans.toString();
    }

    public static String NBCDToDecimal(String NBCDStr) {
        // TODO:
        StringBuilder ans=new StringBuilder();
        if(NBCDStr.startsWith("1101"))ans.append("-");
        int temp=0;
        for (int i=4;i<32;i+=4){
            if(!NBCDStr.startsWith("0000", i))
                temp+=pow(10,(7-i/4))*Integer.parseInt(NBCDStr.substring(i,i+4),2);
        }
        ans.append(temp);
        if(ans.toString().equals("-0"))return "0";
        return ans.toString();
    }
    public static float restToInt(String res){//处理尾数部分转为十进制
        res=res.replaceAll("0+$","");//去掉前导0
        String[]temp=res.split("");
        float ans=0;
        for(int i=0;i<res.length();i++){
            ans+=pow(2,-1-i)*Integer.parseInt(temp[i]);
        }
        return ans;
    }
    public static int add(int a, int b) {
        while(b != 0){
            int c = (a & b) << 1;
            a = a ^ b;
            b = c;
        }
        return a;
    }
    public static String floatToBinary(String floatStr) {
        // TODO:
        StringBuilder ans=new StringBuilder();
        try {
            Float.parseFloat(floatStr);
        }catch (Exception e){
            return "NaN";
        }
        int index=127;
        //符号位
        if(Math.abs(Double.parseDouble(floatStr))>=Float.MAX_VALUE){
            if(Float.parseFloat(floatStr)<0)return "-Inf";
            else return "+Inf";
        }//超过float的范围
        else if(Float.parseFloat(floatStr)==0){//0
            if(floatStr.contains("-"))return "10000000000000000000000000000000";
            else return "00000000000000000000000000000000";
        }
        else if(Float.parseFloat(floatStr)<0){
            ans.append("1");//负数
            floatStr=floatStr.substring(1);
        }
        else ans.append("0");
        //指数位

        if(Float.isNaN(Float.parseFloat(floatStr))){
            return "01111111111111111111111111111111";
        }
        else{
            float int_num= (float) Math.floor(Float.parseFloat(floatStr));//获取整数和小数部分
            float dec_num=Float.parseFloat(floatStr)-int_num;
            StringBuilder int_part=new StringBuilder();
           // String temp=Integer.toString(Integer.parseInt(String.valueOf(int_num).substring(0,1)),2);//每一位的二进制
            while (int_num>=1){
                if(int_num%2.0==0.0) int_part.append("0");
                else int_part.append("1");
                int_num=(float) Math.floor(int_num/2.0);
            }
            int_part= int_part.reverse();
            StringBuilder rest= new StringBuilder();
            for(int i=0;i<256;i++){
                if((dec_num*=2)>=1){
                    rest.append("1");
                    dec_num-=(int)dec_num;
                }
                else rest.append("0");//小数转二进制
            }
            if(Math.abs(Float.parseFloat(floatStr))<Float.MIN_NORMAL)
                rest= new StringBuilder(rest.substring(126));
            if(int_part.toString().contains("1")){//向前进位
                index+=int_part.length()-1;//指数域
                int target= Integer.parseInt(Integer.toString(index,2));
                String tem=String.format("%08d",target);//8位指数
                ans.append(tem).append(int_part.substring(1)).append(rest);
                ans= new StringBuilder(ans.substring(0, 32));
            }
            else if(Math.abs(Float.parseFloat(floatStr))<Float.MIN_NORMAL){//非规格化小数
                ans.append("00000000").append(rest.substring(0,23));
            }
            else {//向后退位
                int len=rest.toString().split("1")[0].length();
                index-=len+1;//退到第一个1后
                String tem=String.format("%08d",Integer.parseInt(Integer.toString(index,2)));
                ans.append(tem).append(rest.substring(len+1));
                ans= new StringBuilder(ans.substring(0, 32));
            }

        }
        return ans.toString();
    }

    public static String binaryToFloat(String binStr) {
        // TODO:
        StringBuilder ans=new StringBuilder();
        int index;
        if(binStr.startsWith("1")) ans.append("-");
        binStr=binStr.substring(1);//去掉符号位
        String exp=binStr.substring(0,8);
        String rest=binStr.substring(8);
        if(!exp.contains("0")){//阶码全为1
            if(rest.contains("1"))ans.append("NaN");//非数
            else ans.append("Inf");//无穷
        }
        else if(!exp.contains("1")){//阶码全为0
            if(rest.contains("1")){//非格式化小数
                index=-126;
                ans.append(restToInt(rest) *(float) pow(2,index));
            }
            else ans.append("0.0");
        }
        else{
            index= Integer.parseInt(binToUncInt(exp))-127;
            float temp= (1+restToInt(rest))*(float) pow(2,index);
            ans.append(temp);
        }
        return ans.toString();
    }

}
