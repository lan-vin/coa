package cpu.controller;

import cpu.alu.ALU;
import memory.Memory;
import util.DataType;
import util.Transformer;

import java.util.Arrays;


public class Controller {
    // general purpose register
    char[][] GPR = new char[32][32];
    // program counter
    char[] PC = new char[32];
    // instruction register
    char[] IR = new char[32];
    // memory address register
    char[] MAR = new char[32];
    // memory buffer register
    char[] MBR =  new char[32];
    char[] ICC = new char[2];

    // 单例模式
    private static final Controller controller = new Controller();

    private Controller(){
        //规定第0个寄存器为zero寄存器
        GPR[0] = new char[]{'0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0'};
        ICC = new char[]{'0','0'}; // ICC初始化为00
    }

    public static Controller getController(){
        return controller;
    }

    public void reset(){
        PC = new char[32];
        IR = new char[32];
        MAR = new char[32];
        GPR[0] = new char[]{'0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0'};
        ICC = new char[]{'0','0'}; // ICC初始化为00
        interruptController.reset();
    }

    public InterruptController interruptController = new InterruptController();
    public ALU alu = new ALU();

    public void tick(){
        // TODO
        if(ICC[0]=='0'){
            if(ICC[1]=='0'){//'00'，取指
                getInstruct();
                if(String.copyValueOf(IR).startsWith("1101110")){
                    ICC[1]='1';//下一步间接
                }
                else{
                    ICC[0]=ICC[1]='1';//下一步执行
                }
            }
            else{//'01'，间接
                findOperand();
                operate();
                ICC[0]=ICC[1]='0';
            }
        } else{
            if(ICC[1]=='0'){//'10'
                interrupt();
                ICC[0]=ICC[1]='0';
            }
            else{//'11'，执行
                operate();
                if(interruptController.signal){
                    ICC[1]='0';//中断
                }
                else{
                    ICC[0]=ICC[1]='0';
                }
            }
        }

    }
    public String fill32bits(String str){
        StringBuilder strBuilder = new StringBuilder(str);
        while (strBuilder.length()<32){
            strBuilder.insert(0, '0');
        }
        return strBuilder.toString();
    }
    /** 执行取指操作 */
    private void getInstruct(){
        // TODO
        MAR= Arrays.copyOf(PC,32);//将PC加载到MAR
        String marAddr=String.copyValueOf(MAR);
        byte[] ins= Memory.getMemory().read(marAddr,4);
        StringBuilder temp = new StringBuilder();
        for (int i=0;i<4;i++){
            temp.append(Transformer.intToBinary(String.valueOf(ins[i])).substring(24));
        }
        MBR=temp.toString().toCharArray();//把MAR地址对应的内存数据读到MBR
        PC=alu.add(new DataType(marAddr),new DataType(fill32bits("100"))).toString().toCharArray();//PC+1
        IR=Arrays.copyOf(MBR,32);//把MBR加载到IR
    }

    /** 执行间址操作 */
    private void findOperand(){
        // TODO
        int rs2 = Integer.parseInt(Transformer.binaryToInt(String.copyValueOf(IR).substring(20, 25)));
        MAR=Arrays.copyOf(GPR[rs2],32);//将rs2中内容读到MAR
        String marAddr=String.copyValueOf(MAR);
        byte[] data= Memory.getMemory().read(marAddr,4);
        StringBuilder temp = new StringBuilder();
        for (int i=0;i<4;i++){
            temp.append(Transformer.intToBinary(String.valueOf(data[i])).substring(24));
        }
        GPR[rs2]=temp.toString().toCharArray();//将MAR地址对应的内存数据存回rs2
    }

    /** 执行周期 */
    private void operate(){
        // TODO
        String opcode=String.copyValueOf(IR).substring(0,7);
        int destR=Integer.parseInt(Transformer.binaryToInt(String.copyValueOf(IR).substring(7,12)));
        switch (opcode) {
            case "1100110":
            case "1101110": {//add
                int rs1 = Integer.parseInt(Transformer.binaryToInt(String.copyValueOf(IR).substring(15, 20)));
                int rs2 = Integer.parseInt(Transformer.binaryToInt(String.copyValueOf(IR).substring(20, 25)));
                String dest = String.copyValueOf(GPR[rs1]);
                String src = String.copyValueOf(GPR[rs2]);
                GPR[destR] = alu.add(new DataType(src), new DataType(dest)).toString().toCharArray();
                break;
            }
            case "1100100": {//addi
                int rs1 = Integer.parseInt(Transformer.binaryToInt(String.copyValueOf(IR).substring(15, 20)));
                String dest = String.copyValueOf(GPR[rs1]);
                String src = fill32bits(String.copyValueOf(IR).substring(20));
                GPR[destR] = alu.add(new DataType(src), new DataType(dest)).toString().toCharArray();
                break;
            }
            case "1100000": {//lw
                int rs1 = Integer.parseInt(Transformer.binaryToInt(String.copyValueOf(IR).substring(15, 20)));
                String dest = String.copyValueOf(GPR[rs1]);
                String src = fill32bits(String.copyValueOf(IR).substring(20));//偏移量
                String addr = alu.add(new DataType(dest), new DataType(fill32bits(src))).toString();//内存要读出来的数据的地址
                byte[] data= Memory.getMemory().read(addr,4);
                StringBuilder temp = new StringBuilder();
                for (int i=0;i<4;i++){
                    temp.append(Transformer.intToBinary(String.valueOf(data[i])).substring(24));
                }
                GPR[destR] = temp.toString().toCharArray();
                break;
            }
            case "1110110": //lui
                String imm = String.copyValueOf(IR).substring(12);
                GPR[destR] = (imm + "000000000000").toCharArray();
                break;
            case "1110011": {//jalr
                GPR[1] = Arrays.copyOf(PC, 32);//把返回地址保存在第一个寄存器
                GPR[destR]=Arrays.copyOf(PC, 32);
                String offset = String.copyValueOf(IR).substring(20);
                int rs1 = Integer.parseInt(Transformer.binaryToInt(String.copyValueOf(IR).substring(15, 20)));
                String dest = String.copyValueOf(GPR[rs1]);
                String addr = alu.add(new DataType(fill32bits(offset)), new DataType(dest)).toString();//计算要跳转的地址
                PC = addr.toCharArray();
                PC[31] = '0';
                break;
            }
            case "1100111": //ecall
                interruptController.signal=true;
                GPR[1] = Arrays.copyOf(PC, 32);//把返回地址保存在第一个寄存器
                break;
            default:
                break;
        }
    }

    /** 执行中断操作 */
    private void interrupt(){
        // TODO
        interruptController.handleInterrupt();
        interruptController.signal=false;
    }

    public class InterruptController{
        // 中断信号：是否发生中断
        public boolean signal;
        public StringBuffer console = new StringBuffer();
        /** 处理中断 */
        public void handleInterrupt(){
            console.append("ecall ");
        }
        public void reset(){
            signal = false;
            console = new StringBuffer();
        }
    }

    // 以下一系列的get方法用于检查寄存器中的内容进行测试，请勿修改

    // 假定代码程序存储在主存起始位置，忽略系统程序空间
    public void loadPC(){
        PC = new char[]{'0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0'};
    }

    public char[] getRA() {
        //规定第1个寄存器为返回地址寄存器
        return GPR[1];
    }

    public char[] getGPR(int i) {
        return GPR[i];
    }
}
