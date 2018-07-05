import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

public class Main {
    //定义组件
    JFrame jf;
    JPanel jp1,jp2;
    JButton jb1,jb2,jb3,jb4,jb5,jb6;
    public static void main(String[] args) {
       Main main = new Main();

        boolean b = false;
         String amount = "20";
        String apiKey = "24faa71aabc17ecfc684673a8071100f12a1b107";
        String sercet = "5b3af0219332758669cc32c6266285f41d390406";
        String symbol = "CP_ETH";

        while(true){
            System.out.println("开始交易");
            if(b){
                break;
            }
            try {
                Thread.sleep(10000);
                TradeService tradeService = new TradeService();
                OrderInfoBean orderInfoBean = new OrderInfoBean();
                orderInfoBean.setApiKey(apiKey);
                orderInfoBean.setSecret(sercet);
                orderInfoBean.setSymbol(symbol);
                orderInfoBean.setAmount(new BigDecimal(amount));
                //查价格

                Map<String, String> coinPriceMap = tradeService.getSymbolCoinPrice(orderInfoBean);
                if("00".equals(coinPriceMap.get("code"))){
                    System.out.println(coinPriceMap);
                    orderInfoBean.setPrice(new BigDecimal(coinPriceMap.get("price")).setScale(2,BigDecimal.ROUND_DOWN));
                }
                //下买单
                orderInfoBean.setIndex(System.currentTimeMillis()+"123");
                orderInfoBean.setBuyOrSell("1");
                Map<String, String> tradeMap = tradeService.trade(orderInfoBean);
                if("00".equals(tradeMap.get("code"))){
                    System.out.println(tradeMap);
                    System.out.println("买入成功，数量"+amount+"价格为："+orderInfoBean.getPrice());
                    //下卖单
                    orderInfoBean.setBuyOrSell("2");
                    Map<String, String> sellTradeMap = tradeService.trade(orderInfoBean);
                    if("00".equals(sellTradeMap.get("code"))){
                        System.out.println(sellTradeMap);
                        System.out.println("卖出成功，数量"+amount+"价格为："+orderInfoBean.getPrice());
                    }
                }
            }catch(Exception e){
               e.printStackTrace();
                System.out.println("交易失败:{}"+e.getMessage());
            }

        }


    }


   public Main(){
            //创建组件
            //面板组件JPanel布局模式默认的是流式布局FlowLayout
            jp1=new JPanel();
            jp2=new JPanel();

            jb1=new JButton("西瓜");
            jb2=new JButton("苹果");
            jb3=new JButton("荔枝");
            jb4=new JButton("葡萄");
            jb5=new JButton("橘子");
            jb6=new JButton("香蕉");
            //设置布局，JPanel默认布局FlowLayout,本案例运用到的刚好是流式布局，所以不用设置了
            //把组件添加JPanel
            jp1.add(jb1);
            jp1.add(jb2);
            jp2.add(jb3);
            jp2.add(jb4);
            jp2.add(jb5);

            //把JPanel加入到JFrame
            jf.add(jp1, BorderLayout.NORTH);
            jf.add(jb6,BorderLayout.CENTER);
            jf.add(jp2,BorderLayout.SOUTH);

            //设置窗口属性
           jf.setSize(300,200);
           jf.setLocation(700,500);
           jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
           jf.setResizable(false);
           jf.setVisible(true);
    }
}
