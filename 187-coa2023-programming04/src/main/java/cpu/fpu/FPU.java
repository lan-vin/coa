package cpu.fpu;

import cpu.alu.ALU;
import util.BinaryIntegers;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

import java.util.Objects;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };

    /**
     * compute the float add of (dest + src)
     */
    public DataType add(DataType src, DataType dest) {
        //TODO
        String a = dest.toString();
        String b = src.toString();
        //边界情况检查
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        else if(cornerCheck(addCorner,b,a)!=null)return new DataType(Objects.requireNonNull(cornerCheck(addCorner, b, a)));
        //提取符号、阶码、尾数
        String srcSign=b.substring(0,1);
        String destSign=a.substring(0,1);
        String srcE=b.substring(1,9);
        String destE=a.substring(1,9);
        String srcM=b.substring(9);
        String destM=a.substring(9);
        if(srcE.equals("11111111"))return src;
        else if(destE.equals("11111111"))return dest;//指数全为1（无穷）返回自身

        char ansSign = '0';//判断结果符号
        if(srcSign.equals(destSign))ansSign= destSign.equals("0") ?'0':'1';
        else{
            for (int i=1;i<32;i++){
                if(a.charAt(i)>b.charAt(i)){
                    ansSign= destSign.equals("0") ?'0':'1';
                    break;
                }
                else if(a.charAt(i)<b.charAt(i)) {
                    ansSign= srcSign.equals("0") ?'0':'1';
                    break;
                }
            }
        }
        if(srcE.equals("00000000")&&destE.equals("00000000")){
            srcE="00000001";
            destE="00000001";//指数全为0（非规格化）加一
            destM="0"+destM;
            srcM="0"+srcM;
        }
        else if(destE.equals("00000000")){
            destE="00000001";//指数全为0（非规格化）加一
            destM="0"+destM;
            srcM="1"+srcM;
        }
        else if(srcE.equals("00000000")){
            srcE="00000001";
            srcM="0"+srcM;
            destM="1"+destM;//尾数前加上隐藏位
        }
        else{
            srcM="1"+srcM;
            destM="1"+destM;
        }
        srcM+="000";
        destM+="000";
        //进入运算
        String ansE = null;
        String shiftE;
        for (int i=0;i<8;i++){//找大阶,求差值,尾数右移
            if(srcE.charAt(i)>destE.charAt(i)){
                ansE=srcE;
                shiftE=sub(new DataType(src.fill32bit(destE)),new DataType(dest.fill32bit(srcE))).toString();
                destM=rightShift(destM, Integer.parseInt(Transformer.binaryToInt(shiftE)));
                break;
            }
            else if(srcE.charAt(i)<destE.charAt(i)){
                ansE=destE;
                shiftE= ALU.sub(new DataType(src.fill32bit(srcE)),new DataType(dest.fill32bit(destE))).toString();
                srcM=rightShift(srcM,Integer.parseInt(Transformer.binaryToInt(shiftE)));
                break;
            }
        }
        if(ansE==null){
            ansE=srcE;//初始已经同阶
        }
        while(destM.length()<32){
            destM="0"+destM;
        }
        while (srcM.length()<32){
            srcM="0"+srcM;
        }
        String ansM;
        if(srcSign.equals(destSign))ansM=ALU.add(new DataType(destM),new DataType(srcM)).toString();//完成尾数的加法
        else if(ansSign==destSign.charAt(0))ansM=ALU.sub(new DataType(srcM),new DataType(destM)).toString();
        else ansM=ALU.sub(new DataType(destM),new DataType(srcM)).toString();
        //判断溢出，规格化尾数
        if(ansM.charAt(4)=='1'){//判断结果是否溢出
            ansM=rightShift(ansM.substring(5),1);
            ansE=oneAdder(ansE).substring(1);//尾数右移一位，指数加一
            if (ansE.equals("11111111")){//阶数溢出
                if(ansSign=='0')return new DataType(IEEE754Float.P_INF);
                else return new DataType(IEEE754Float.N_INF);
            }
        }
        else {
            ansM=ansM.substring(5);
            while(ansM.charAt(0)=='0'){//遍历前导0
                if(Objects.equals(ansE, "00000000"))break;
                ansE=ALU.sub(new DataType(src.fill32bit("1")),new DataType(ansE)).toString();
                ansE=ansE.substring(ansE.length()-8);
                if(Objects.equals(ansE, "00000000"))break;
                ansM=ansM.substring(1)+"0";
            }
        }
        String ans=round(ansSign,ansE,ansM);
        return new DataType(ans);
    }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src, DataType dest) {
        //TODO
        if(src.toString().startsWith("1"))return add(new DataType("0"+src.toString().substring(1)),dest);
        else return add(new DataType("1"+src.toString().substring(1)),dest);
    }

    /**
     * compute the float mul of (dest * src)
     */
    public DataType mul(DataType src,DataType dest){
        //TODO
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        else if(cornerCheck(mulCorner,b,a)!=null)return new DataType(Objects.requireNonNull(cornerCheck(mulCorner, b, a)));
        //提取符号、阶码、尾数
        String srcSign=b.substring(0,1);
        String destSign=a.substring(0,1);
        String srcE=b.substring(1,9);
        String destE=a.substring(1,9);
        String srcM=b.substring(9);
        String destM=a.substring(9);
        char ansSign= destSign.equals(srcSign) ?'0':'1';//同号得到正数，异号得到负数
        if(srcE.equals("11111111")) {
            if(ansSign=='1')return new DataType(IEEE754Float.N_INF);
            else return new DataType(IEEE754Float.P_INF);
        }
        else if(destE.equals("11111111")) {
            if(ansSign=='1')return new DataType(IEEE754Float.N_INF);
            else return new DataType(IEEE754Float.P_INF);
        }//指数全为1（无穷）返回无穷
        if(srcE.equals("00000000")&&destE.equals("00000000")){
            srcE="00000001";
            destE="00000001";//指数全为0（非规格化）加一
            destM="0"+destM;
            srcM="0"+srcM;
        }
        else if(destE.equals("00000000")){
            destE="00000001";//指数全为0（非规格化）加一
            destM="0"+destM;
            srcM="1"+srcM;
        }
        else if(srcE.equals("00000000")){
            srcE="00000001";
            srcM="0"+srcM;
            destM="1"+destM;//尾数前加上隐藏位
        }
        else{
            srcM="1"+srcM;
            destM="1"+destM;
        }
        srcM+="000";
        destM+="000";
        //进入运算
        String ansE;
        String ansM ;
        int E= Integer.parseInt(Transformer.binaryToInt(srcE))+Integer.parseInt(Transformer.binaryToInt(destE))-127;
        DataType P=new DataType(src.fill32bit("0"));//部分乘积p
        DataType X=new DataType(src.fill32bit(destM));
        DataType Y=new DataType(src.fill32bit(srcM));
        int cnt=0;
        while(cnt<32){
            String temp=Y.toString();//乘数
            if(temp.endsWith("1")){//部分积要加上被乘数
                if(P.toString().charAt(4) == '1' ){
                    P=new DataType("00001"+P.toString().substring(5));
                }//加法有进位
                else if(P.toString().charAt(3)=='1'){
                    P=new DataType("00010"+P.toString().substring(5));
                }
                P=ALU.add(P,X);
            }
            String mulValue=P.toString();
            Y=new DataType(mulValue.charAt(mulValue.length()-1)+temp.substring(0,temp.length()-1));//P的末位给Y的首位
            P=new DataType(mulValue.charAt(0)+mulValue.substring(0,mulValue.length()-1));
            cnt++;
        }
        ansM=P.toString().substring(10)+ Y;//54位尾数乘积
        E+=1;//抵消掉前一位隐藏位的影响
        //规格化处理
        while(E>0&&ansM.startsWith("0")){
            //System.out.println(Transformer.binaryToInt(ansE));
            ansM=ansM.substring(1)+"0";//尾数左移
            E-=1;//指数减一
        }
        while(ansM.substring(0,28).contains("1")&&E<0){
            ansM=rightShift(ansM,1);//尾数右移
            E+=1;//指数加一
        }
        if(E>=255){//阶码上溢
            return ansSign=='0'?new DataType(IEEE754Float.P_INF):new DataType(IEEE754Float.N_INF);
        }
        else if(E<0){//阶码下溢
            return ansSign=='0'?new DataType(IEEE754Float.P_ZERO):new DataType(IEEE754Float.N_ZERO);
        }
        else if(E==0){
            ansM=rightShift(ansM,1);//非规格数额外右移一次
        }
        ansE=Transformer.intToBinary(String.valueOf(E)).substring(24);
        String ans=round(ansSign,ansE,ansM);
        return new DataType(ans);
    }
    public boolean isBiggerThan(String a,String b){//if A is bigger than B
        while(a.length()<32)a+="0";
        while(b.length()<32)b+="0";
        for (int i=0;i<32;i++){
            if(a.charAt(i)>b.charAt(i))return true;
            else if(a.charAt(i)<b.charAt(i))return false;
        }
        return true;
    }
    /**
     * compute the float mul of (dest / src)
     */
    public DataType div(DataType src,DataType dest){
        //TODO
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        else if(cornerCheck(divCorner,b,a)!=null)return new DataType(Objects.requireNonNull(cornerCheck(divCorner, b, a)));
        if(Objects.equals(dest.toString(), BinaryIntegers.ZERO) &&!Objects.equals(src.toString(), BinaryIntegers.ZERO)){//被除数为0，除数不为0
            return new DataType(BinaryIntegers.ZERO);
        }
        else if(Objects.equals(src.toString(), BinaryIntegers.ZERO)){//除数为0，被除数不为0
            throw new ArithmeticException();
        }
        //提取符号、阶码、尾数
        String srcSign=b.substring(0,1);
        String destSign=a.substring(0,1);
        String srcE=b.substring(1,9);
        String destE=a.substring(1,9);
        String srcM=b.substring(9);
        String destM=a.substring(9);
        char ansSign= destSign.equals(srcSign) ?'0':'1';//同号得到正数，异号得到负数
        if(srcE.equals("11111111")) {
            if(ansSign=='1')return new DataType(IEEE754Float.N_INF);
            else return new DataType(IEEE754Float.P_INF);
        }
        else if(destE.equals("11111111")) {
            if(ansSign=='1')return new DataType(IEEE754Float.N_INF);
            else return new DataType(IEEE754Float.P_INF);
        }//指数全为1（无穷）返回无穷
        if(srcE.equals("00000000")&&destE.equals("00000000")){
            srcE="00000001";
            destE="00000001";//指数全为0（非规格化）加一
            destM="0"+destM;
            srcM="0"+srcM;
        }
        else if(destE.equals("00000000")){
            destE="00000001";//指数全为0（非规格化）加一
            destM="0"+destM;
            srcM="1"+srcM;
        }
        else if(srcE.equals("00000000")){
            srcE="00000001";
            srcM="0"+srcM;
            destM="1"+destM;//尾数前加上隐藏位
        }
        else{
            srcM="1"+srcM;
            destM="1"+destM;
        }
        srcM+="000";
        destM+="000";
        //进入运算
        String ansE;
        String ansM ;
        int E= Integer.parseInt(Transformer.binaryToInt(destE))-Integer.parseInt(Transformer.binaryToInt(srcE))+127;
        DataType Q=new DataType(src.fill32bit("0"));//商
        while (destM.length()<32)destM+="0";//小数部分在末尾补0补成32位
        DataType R=new DataType(destM);//余数
        while(srcM.length()<32)srcM+="0";
        DataType Y=new DataType(srcM);//被除数
        int cnt=0;
        while(cnt++<32){
            char temp;
            R=ALU.sub(Y,R);
            if(R.toString().startsWith("1")){//不够减
                R=ALU.add(Y,R);
                temp='0';
            }
            else temp='1';
            R=new DataType(R.toString().substring(1)+Q.toString().charAt(0));//R左移一位
            Q=new DataType(Q.toString().substring(1)+temp);//Q左移一位，上商
        }
        ansM=Q.toString().substring(0,27);

        //规格化处理
        while(E>0&&ansM.startsWith("0")){
            ansM=ansM.substring(1)+"0";//尾数左移
            E-=1;//指数减一
        }
        while(ansM.contains("1")&&E<0){
            ansM=rightShift(ansM,1);//尾数右移
            E+=1;//指数加一
        }
        if(E>=255){//阶码上溢
            return ansSign=='0'?new DataType(IEEE754Float.P_INF):new DataType(IEEE754Float.N_INF);
        }
        else if(E<0){//阶码下溢
            return ansSign=='0'?new DataType(IEEE754Float.P_ZERO):new DataType(IEEE754Float.N_ZERO);
        }
        else if(E==0){
            ansM=rightShift(ansM,1);//非规格数额外右移一次
        }
        ansE=Transformer.intToBinary(String.valueOf(E)).substring(24);
        String ans=round(ansSign,ansE,ansM);
        return new DataType(ans);
    }

    /**
     * check corner cases of mul and div
     *
     * @param cornerMatrix corner cases pre-stored
     * @param oprA first operand (String)
     * @param oprB second operand (String)
     * @return the result of the corner case (String)
     */
    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return sign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF;
        }

        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carry to the next)
     *         and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }

}
