import com.sun.deploy.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseAdapter;
import java.math.BigDecimal;
import java.util.Map;

public class Main {
    //定义组件
    JFrame jf;
    JPanel jp1, jp2, jp3;
    JButton jb1, jb2, jb3, jb4, jb5, jb6;
    JLabel apiLabel, secretLabel, amountLabel,symbolLabel;
    JTextField apiJTextField, secretTextField, amountTextField,symbolTextField;
    JTextArea logArea;
    boolean b = false;          //是否挖矿
    String apiKey = "";
    String sercet = "";
    String symbol = "";
    String amount = "";
    String orderId="";
    public static void main(String[] args) {
        Main main = new Main();
        main.trade();
    }

    public void trade() {

//        String apiKey = "24faa71aabc17ecfc684673a8071100f12a1b107";
//        String sercet = "5b3af0219332758669cc32c6266285f41d390406";

        while (true) {
            logArea.append("开始交易");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!b) {
                continue;
            }
            logArea.append("\n"+"开始交易");
            try {
                AaCoinService tradeService = new AaCoinService();
                OrderInfoBean orderInfoBean = new OrderInfoBean();
                orderInfoBean.setApiKey(apiKey);
                orderInfoBean.setSecret(sercet);
                orderInfoBean.setSymbol(symbol);
                orderInfoBean.setAmount(new BigDecimal(amount));
                //查价格

                Map<String, String> coinPriceMap = tradeService.getSymbolCoinPrice(orderInfoBean);
                if ("00".equals(coinPriceMap.get("code"))) {
                    logArea.append("\n"+"查询价格结束，当前价格为：" + coinPriceMap);
                    orderInfoBean.setPrice(new BigDecimal(coinPriceMap.get("price")).setScale(7, BigDecimal.ROUND_DOWN));
                } else {
                    logArea.append("\n"+"本次失败:" + coinPriceMap.get("msg").toString());
                    continue;
                }
                //下买单
                orderInfoBean.setIndex(System.currentTimeMillis() + "123");
                orderInfoBean.setBuyOrSell("1");
                Map<String, String> tradeMap = tradeService.trade(orderInfoBean);
                if ("00".equals(tradeMap.get("code"))) {
                    orderId = tradeMap.get("orderId");
                    System.out.println(tradeMap);
                    logArea.append("\n"+"买入成功，数量" + amount + "价格为：" + orderInfoBean.getPrice());
                    while(true){
                        orderInfoBean.setOrderId(orderId);
                        Map<String, String> orderpendingMap = tradeService.orderpending(orderInfoBean);
                        Thread.sleep(100);
                        if("98".equals(orderpendingMap.get("code"))){
                            continue;
                        }else if("00".equals(orderpendingMap.get("code"))){
                            break;
                        }
                    }
                    System.out.println("买入成功，数量" + amount + "价格为：" + orderInfoBean.getPrice());
                    //下卖单
                    orderInfoBean.setBuyOrSell("2");
                    Map<String, String> sellTradeMap = tradeService.trade(orderInfoBean);
                    if ("00".equals(sellTradeMap.get("code"))) {
                        logArea.append("\n"+"卖出成功，数量" + amount + "价格为：" + orderInfoBean.getPrice());
                        orderId = "";
                        System.out.println(sellTradeMap);
                    } else {
                        logArea.append("\n"+"卖出失败:" + sellTradeMap.get("msg"));
                        continue;
                    }
                } else {
                    logArea.append("\n"+"下单失败:" + tradeMap.get("msg"));
                    System.out.println("下单失败:" + tradeMap.get("msg"));
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                logArea.append("\n"+"交易失败:{}" + e.getMessage());
                System.out.println("交易失败:{}" + e.getMessage());
            }

        }

    }


    public Main() {
        //创建组件
        //面板组件JPanel布局模式默认的是流式布局FlowLayout\
        jf = new JFrame("我的工具");

        jp1 = new JPanel();
        jp2 = new JPanel();
        jp3 = new JPanel();
        jb1 = new JButton("开始");
        jb2 = new JButton("关闭");
        apiLabel = new JLabel("apiKey:");
        apiJTextField = new JTextField(10);
        secretLabel = new JLabel("secret:");
        secretTextField = new JTextField(10);
        amountLabel = new JLabel("单次数量");
        amountTextField = new JTextField(5);
        symbolLabel = new JLabel("操作币种");
        symbolTextField = new JTextField( 5);
        logArea = new JTextArea(30, 30);
        //设置布局，JPanel默认布局FlowLayout,本案例运用到的刚好是流式布局，所以不用设置了
        //把组件添加JPanel
        jp1.add(apiLabel);
        jp1.add(apiJTextField);
        jp1.add(secretLabel);
        jp1.add(secretTextField);
        jp1.add(amountLabel);
        jp1.add(amountTextField);
        jp1.add(symbolLabel);
        jp1.add(symbolTextField);
        jp2.add(logArea);
        JScrollPane scrollPane_1 = new JScrollPane();
        jp2.add(scrollPane_1);
        jp3.add(jb1);
        jp3.add(jb2);

        //把JPanel加入到JFrame
        jf.add(jp1, BorderLayout.NORTH);
        jf.add(jp2, BorderLayout.CENTER);
        jf.add(jp3, BorderLayout.SOUTH);
        scrollPane_1.setViewportView(logArea);
        event();
        //设置窗口属性
        jf.setSize(600, 400);
        jf.setLocation(700, 500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jf.setResizable(false);
        jf.setVisible(true);
    }

    private void event() {
        jf.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // TODO Auto-generated method stub
                System.exit(0);
            }

        });
        jb1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String apikeyStr = apiJTextField.getText();
                String secretStr = secretTextField.getText();
                String amountStr = amountTextField.getText();
                String symbolStr = symbolTextField.getText();
                if (!isBlack(apikeyStr) && !isBlack(secretStr) && !isBlack(amountStr) && !isBlack(symbolStr)) {
                    System.out.println("点击:" + apiJTextField.getText() + "___" + secretStr + "___" + amountStr);
                    apiKey = apikeyStr;
                    sercet = secretStr;
                    symbol = symbolStr;
                    amount = amountStr;
                    apiJTextField.setEditable(false);
                    amountTextField.setEditable(false);
                    secretTextField.setEditable(false);
                    b = true;
                    logArea.append("\n"+"开始挖矿");

                }else{
                    logArea.append("\n"+"参数不完整，请重新输入");
                }
            }

        });


        jb2.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                apiJTextField.setEditable(true);
                amountTextField.setEditable(true);
                secretTextField.setEditable(true);
                b = false;
                logArea.append("\n"+"关闭挖矿");
            }

        });
    }

    public boolean isBlack(String str) {
        if (str == null || "".equals(str)) {
            return true;
        }
        return false;
    }
}
