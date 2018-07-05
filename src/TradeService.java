
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * coinPark 交易所api 信息
 * Created by joel on 2018/6/26.
 */
public class TradeService {
    private String url = "https://api.coinpark.cc/v1/mdata";
    private String tradeURL = "https://api.coinpark.cc/v1/transfer";
    /**
     * 获取单一币种价格
     * @param orderInfoBean
     * @return
     * @throws Exception
     */
    public Map<String,String> getSymbolCoinPrice(OrderInfoBean orderInfoBean) throws Exception {
        //{"result":[{"result":{"id":199,"coin_symbol":"BIX","currency_symbol":"BTC","last":"0.00016109","high":"0.00018504","low":"0.00016057","change":"-0.00001712","percent":"-9.61%","vol24H":"1665","amount":"0.29","last_cny":"6.48","high_cny":"7.44","low_cny":"6.45","last_usd":"0.98","high_usd":"1.13","low_usd":"0.98"},"cmd":"api/market"}]}
        if(null == orderInfoBean){
            return null;
        }
        JSONObject json = new JSONObject();
        JSONObject cmds = new JSONObject();
        cmds.put("cmd","api/market");
        JSONObject body = new JSONObject();
        body.put("pair",orderInfoBean.getSymbol());
        cmds.put("body",body);
        JSONArray jsarray = new JSONArray();
        jsarray.add(cmds);
        json.put("cmds",jsarray.toString());
        json.put("apikey",orderInfoBean.getApiKey());
        String sign = SignUtils.coinParkSign(json.toJSONString(),orderInfoBean.getSecret());
        json.put("sign",sign);

        String response = HttpUtils2.postJson("https://api.coinpark.cc/v1/mdata",json);
        System.out.println(response);
        Map<String,String> maps = new HashMap<String, String>();
        JSONObject respJson = JSONObject.parseObject(response);
        JSONArray jsonArray = respJson.getJSONArray("result");
        if(jsonArray != null && jsonArray.size() > 0){
            JSONObject jsonObject = JSONObject.parseObject(jsonArray.get(0).toString());
            maps.put("code","00");
            maps.put("price",jsonObject.get("last").toString());
        }else{
            maps.put("code", "10");
            maps.put("msg",respJson.toJSONString());
        }
    return maps;
    }




    public Map<String,String> returnMap(String url,JSONObject json) throws Exception {
        String response = HttpUtils2.postJson(url,json);
        System.out.println(response);
        Map<String,String> maps = new HashMap<String, String>();
        JSONObject respJson = JSONObject.parseObject(response);
        for(String key:respJson.keySet()) {
            maps.put(key, respJson.getString(key));
        }
        return maps;
    }


    ////////////////////////////////////////////////用户资产///////////////////////////////////////////////////////////////////


    /**
     * 获取用户资产信息
     * @param orderInfoBean
     * @return
     * @throws Exception
     */
    public Map<String,String> getTransferAssets(OrderInfoBean orderInfoBean) throws Exception {
        if(null == orderInfoBean){
            return null;
        }
        JSONObject json = new JSONObject();
        JSONObject cmds = new JSONObject();
        cmds.put("cmd","transfer/assets");
        JSONObject body = new JSONObject();
        body.put("select","1");
        cmds.put("body",body);
        JSONArray jsarray = new JSONArray();
        jsarray.add(cmds);
        json.put("cmds",jsarray.toString());
        String sign = SignUtils.coinParkSign(json.getString("cmds"),orderInfoBean.getSecret());
        json.put("apikey",orderInfoBean.getApiKey());
        json.put("sign",sign);
        System.out.println(json.toJSONString());
        return returnMap(tradeURL,json);
    }


    ////////////////////////////////////////////////交易///////////////////////////////////////////////////////////////////

    /**
     * 交易下单
     * @param orderInfoBean
     * @return
     * @throws Exception
     */
    public Map<String,String> trade(OrderInfoBean orderInfoBean) throws Exception {
        if(null == orderInfoBean){
            return null;
        }
        JSONObject json = new JSONObject();
        JSONObject cmds = new JSONObject();
        JSONObject body = new JSONObject();
        cmds.put("cmd","orderpending/trade");
        cmds.put("index",orderInfoBean.getOrderId());
        body.put("pair",orderInfoBean.getSymbol());
        body.put("account_type","0");
        body.put("order_type","2");
        body.put("order_side",orderInfoBean.getBuyOrSell());
        body.put("price",orderInfoBean.getPrice());
        body.put("amount",orderInfoBean.getAmount());
        cmds.put("body",body);
        JSONArray jsarray = new JSONArray();
        jsarray.add(cmds);
        json.put("cmds",jsarray.toString());
        json.put("apikey",orderInfoBean.getApiKey());
        String sign = SignUtils.coinParkSign(json.getString("cmds"),orderInfoBean.getSecret());
        json.put("sign",sign);
        System.out.println(json.toJSONString());
        String response = HttpUtils2.postJson("https://api.coinpark.cc/v1/orderpending",json);
        System.out.println(response);
        Map<String,String> maps = new HashMap<String, String>();
        JSONObject respJson = JSONObject.parseObject(response);
        maps.put("code", "00");
        maps.put("orderId",respJson.get("result").toString());
        return maps;
    }


    /**
     * 撤单
     * @param orderInfoBean
     * @return
     * @throws Exception
     */
    public Map<String,String> cancelTrade(OrderInfoBean orderInfoBean) throws Exception {
        if(null == orderInfoBean){
            return null;
        }
        JSONObject json = new JSONObject();
        JSONObject cmds = new JSONObject();
        JSONObject body = new JSONObject();
        cmds.put("cmd","orderpending/cancelTrade");
        cmds.put("index",orderInfoBean.getOrderId());
        body.put("orders_id",orderInfoBean.getIndex());
        cmds.put("body",body);
        JSONArray jsarray = new JSONArray();
        jsarray.add(cmds);
        json.put("cmds",jsarray.toString());
        String sign = SignUtils.coinParkSign(json.getString("cmds"),orderInfoBean.getSecret());
        json.put("apikey",orderInfoBean.getApiKey());
        json.put("sign",sign);
        System.out.println(json.toJSONString());
        String response = HttpUtils2.postJson("https://api.coinpark.cc/v1/orderpending",json);
        System.out.println(response);
        Map<String,String> maps = new HashMap<String, String>();
        JSONObject respJson = JSONObject.parseObject(response);
        maps.put("code", "00");
        maps.put("orderId",respJson.get("result").toString());
        return maps;
    }


    /**
     * 查询委托单
     * @param orderInfoBean
     * @return
     * @throws Exception
     */
    public Map<String,String> orderpending(OrderInfoBean orderInfoBean) throws Exception {
        if(null == orderInfoBean){
            return null;
        }
        JSONObject json = new JSONObject();
        JSONObject cmds = new JSONObject();
        JSONObject body = new JSONObject();
        cmds.put("cmd","orderpending/orderPendingList");
        cmds.put("index",orderInfoBean.getOrderId());
        body.put("pair",orderInfoBean.getSymbol());
        body.put("account_type","0");
        body.put("page","1");
        body.put("size",orderInfoBean.getSymbol());
        String[] symbol = orderInfoBean.getSymbol().split("_");
        body.put("coin_symbol",symbol[0]);
        body.put("currency_symbol",symbol[1]);
        body.put("order_side",orderInfoBean.getBuyOrSell());
        cmds.put("body",body);
        JSONArray jsarray = new JSONArray();
        jsarray.add(cmds);
        json.put("cmds",jsarray.toString());
        String sign = SignUtils.coinParkSign(json.getString("cmds"),orderInfoBean.getSecret());
        json.put("apikey",orderInfoBean.getApiKey());
        json.put("sign",sign);
        System.out.println(json.toJSONString());
        return returnMap(tradeURL,json);
    }


    public static void main(String[] args) throws Exception {
        OrderInfoBean orderInfoBean = new OrderInfoBean("24faa71aabc17ecfc684673a8071100f12a1b107","5b3af0219332758669cc32c6266285f41d390406","BIX_BTC","","1",null,null);
        TradeService tradeService = new TradeService();
        orderInfoBean.setSymbol("CP_ETH");
        tradeService.getSymbolCoinPrice(orderInfoBean);
//       Map<String,String> maps = coinParkService.getTransferAssets(orderInfoBean);
//       Map<String,String> maps = coinParkService.orderpending(orderInfoBean);
//       Map<String,String> maps = coinParkService.pendingHistoryList(orderInfoBean);
//        JSONArray jsonArray = JSONArray.parseArray(maps.get("result"));
//        System.out.println(jsonArray.toString());
//        JSONObject jsonObject = jsonArray.getJSONObject(0);
//        System.out.println(jsonObject.toJSONString());

    }
}
