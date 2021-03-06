
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.deploy.util.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class AaCoinService   {
    

     
    public   Map<String,String> getSymbolCoinPrice(OrderInfoBean orderInfoBean) throws Exception {
        final String url = "https://api.aacoin.com//market/tickers";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        Map<String,String> responseMap = new HashMap<>();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("symbol", orderInfoBean.getSymbol()));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpClient.execute(httpPost);
        String responseStr = "";
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // 正常返回
//            System.out.println(EntityUtils.toString(response.getEntity()));
            responseStr = EntityUtils.toString(response.getEntity());
            //{"status":"1000","data":{"date":"2018-07-01 12:19:54","ticker":[{"symbol":"BCH_ETH","high":"1.738",
            // "low":"1.420131","last":"1.420133","vol":"8.49","sell":"1.700000","buy":"1.420134"}]}}
            System.out.println(responseStr);
            httpPost.releaseConnection();
        } else {
            System.err.println(response.getStatusLine().getStatusCode());
            httpPost.releaseConnection();
            responseMap.put("code","98");
            responseMap.put("msg","请求失败");
            return responseMap;

        }
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        responseMap.put("code","00");
        JSONObject jsonData = jsonObject.getJSONObject("data");
        JSONArray jsonArray = jsonData.getJSONArray("ticker");
        if(jsonArray.size() == 0){
            responseMap.put("code","98");
            responseMap.put("msg","结果为空"+jsonData);
            return responseMap;
        }
        JSONObject arr0 = JSONObject.parseObject(jsonArray.getString(0));
        responseMap.put("symbol",arr0.getString("symbol"));
        responseMap.put("price",arr0.getString("buy"));
        responseMap.put("code","00");
        System.out.println("查询结束，币种信息为:"+responseMap);
        return responseMap;

    }


    public   Map<String,String> getTransferAssets(OrderInfoBean orderInfoBean) throws Exception {
        final String url = "https://api.aacoin.com/v1/account/accounts";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("accessKey", orderInfoBean.getApiKey()));

        //对参数进行排序
        params.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        List<String> paramStringList = params.stream().map(e -> e.getName() + "=" + e.getValue()).collect(Collectors.toList());

        String paramString = "";
        StringBuffer var2 = new StringBuffer();
        for(String param:paramStringList){
            var2.append(param);
            var2.append("&");
        }
        paramString = var2.substring(0,var2.length()-1);
//        String paramString = StringUtils.join(paramStringList, "&");
        //进行签名
        String actualSignature = HmacSHAUtils.encodeHmacSHA256(paramString, orderInfoBean.getSecret());
        params.add(new BasicNameValuePair("sign", actualSignature));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpClient.execute(httpPost);
        String responseStr = "";
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // 正常返回
            responseStr = EntityUtils.toString(response.getEntity());
        } else {
            responseStr = EntityUtils.toString(response.getEntity());
        }
        httpPost.releaseConnection();

        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("data"));
        String symbol = orderInfoBean.getSymbol();
        Map<String,String> returnMaps = new HashMap<>();
        for(int i =0; i < jsonArray.size(); i++){
            JSONObject symbolJson = jsonArray.getJSONObject(i);
            if(symbol.startsWith(symbolJson.getString("currencyCode"))){
                JSONArray accountsJsonArray = symbolJson.getJSONArray("accounts");
                JSONObject accountJson = accountsJsonArray.getJSONObject(0);
                if(accountJson.getString("type").equals("frozen")){
                    accountJson = accountsJsonArray.getJSONObject(1);
                }
                returnMaps.put(symbolJson.getString("currencyCode"),accountJson.getString("balance"));
            }
            if(symbol.endsWith(symbolJson.getString("currencyCode"))){
                JSONArray accountsJsonArray = symbolJson.getJSONArray("accounts");
                JSONObject accountJson = accountsJsonArray.getJSONObject(0);
                if(accountJson.getString("type").equals("frozen")){
                    accountJson = accountsJsonArray.getJSONObject(1);
                }
                returnMaps.put(symbolJson.getString("currencyCode"),accountJson.getString("balance"));
            }
        }
        System.out.println("查询结束，用户资产为:"+returnMaps);
        return returnMaps;
    }

     
    public   Map<String,String> trade(OrderInfoBean orderInfoBean) throws Exception {

        final String url = "https://api.aacoin.com/v1/order/place";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("accessKey", orderInfoBean.getApiKey()));
        params.add(new BasicNameValuePair("symbol", orderInfoBean.getSymbol()));
        String type = "";
        if("1".equals(orderInfoBean.getBuyOrSell())){
            type = "buy-limit";
        }else{
            type = "sell-limit";
        }
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("quantity", orderInfoBean.getAmount().toString()));
        params.add(new BasicNameValuePair("price", orderInfoBean.getPrice().toString()));

        //对参数进行排序
        params.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        Map<String,String> returnMap = new HashMap<>();
        List<String> paramStringList = params.stream().map(e -> e.getName() + "=" + e.getValue()).collect(Collectors.toList());
        String paramString = "";
        StringBuffer var2 = new StringBuffer();
        for(String param:paramStringList){
            var2.append(param);
            var2.append("&");
        }
        paramString = var2.substring(0,var2.length()-1);
//        String paramString = StringUtils.join(paramStringList, "&");
        String actualSignature = HmacSHAUtils.encodeHmacSHA256(paramString, orderInfoBean.getSecret());
        params.add(new BasicNameValuePair("sign", actualSignature));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpClient.execute(httpPost);
        String  responseStr = "";
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // 正常返回
            responseStr = EntityUtils.toString(response.getEntity());
        } else {
            responseStr = EntityUtils.toString(response.getEntity());
        }
        System.out.println(responseStr);
        httpPost.releaseConnection();
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        if("1000".equals(jsonObject.getString("status"))){
            returnMap.put("orderId",jsonObject.getString("data"));
            System.out.println("交易结束，交易内容为:"+orderInfoBean);
            returnMap.put("code","00");
            return returnMap;
        }else{
            returnMap.put("code","98");
            returnMap.put("msg",responseStr);
            return returnMap;
        }
    }

     
    public   Map<String,String> cancelTrade(OrderInfoBean orderInfoBean) throws Exception {
        final String url = "https://api.aacoin.com/v1/order/cancel";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("accessKey", orderInfoBean.getApiKey()));
        params.add(new BasicNameValuePair("orderId", orderInfoBean.getOrderId()));
        //对参数进行排序
        params.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        List<String> paramStringList = params.stream().map(e -> e.getName() + "=" + e.getValue()).collect(Collectors.toList());
        String paramString = "";
        StringBuffer var2 = new StringBuffer();
        for(String param:paramStringList){
            var2.append(param);
            var2.append("&");
        }
        paramString = var2.substring(0,var2.length()-1);
//        String paramString = StringUtils.join(paramStringList, "&");
        String actualSignature = HmacSHAUtils.encodeHmacSHA256(paramString, orderInfoBean.getSecret());
        params.add(new BasicNameValuePair("sign", actualSignature));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpClient.execute(httpPost);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // 正常返回
            System.out.println(EntityUtils.toString(response.getEntity()));
        } else {
            System.out.println(EntityUtils.toString(response.getEntity()));
            System.err.println(response.getStatusLine().getStatusCode());
        }
        httpPost.releaseConnection();
        return null;
    }

     
    public   Map<String,String> orderpending(OrderInfoBean orderInfoBean) throws Exception {
        final String url = "https://api.aacoin.com/v1/order/currentOrders";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("accessKey", orderInfoBean.getApiKey()));
        params.add(new BasicNameValuePair("symbol", orderInfoBean.getSymbol()));
        params.add(new BasicNameValuePair("size","100"));
        String type = "";
        if("1".equals(orderInfoBean.getBuyOrSell())){
            type = "buy-limit";
        }else{
            type = "sell-limit";
        }
        params.add(new BasicNameValuePair("type", type));

        //对参数进行排序
        params.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        List<String> paramStringList = params.stream().map(e -> e.getName() + "=" + e.getValue()).collect(Collectors.toList());
        String paramString = "";
        StringBuffer var2 = new StringBuffer();
        for(String param:paramStringList){
            var2.append(param);
            var2.append("&");
        }
        paramString = var2.substring(0,var2.length()-1);
//        String paramString = StringUtils.join(paramStringList, "&");
        String actualSignature = HmacSHAUtils.encodeHmacSHA256(paramString, orderInfoBean.getSecret());
        params.add(new BasicNameValuePair("sign", actualSignature));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpClient.execute(httpPost);
        String responseStr ="";
        //{"status":"1000","data":{"pageNo":1,"pageSize":100,"total":0,"list":[],"prevPage":1,"hasPrevPage":false,"hasNextPage":false,"totalPage":0,"nextPage":0}}
        //{"status":"1000","data":{"pageNo":1,"pageSize":100,"total":1,"list":[{"orderId":34671623,"symbol":"AAT_ETH","price":0.00015000,"type":"buy","priceType":"limit","orderTime":"2018-07-01 16:40:34","filledQuantity":0,"quantity":15,"filledAmount":0}],"prevPage":1,"hasPrevPage":false,"hasNextPage":false,"totalPage":1,"nextPage":1}}
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // 正常返回
            responseStr = EntityUtils.toString(response.getEntity());
            System.out.println(responseStr);
        } else {
            responseStr = EntityUtils.toString(response.getEntity());
        }
        httpPost.releaseConnection();
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        JSONObject jsonData = JSONObject.parseObject(jsonObject.getString("data"));
        Map<String,String> returnMap = new HashMap<String,String>();
        if("0".equals(jsonData.getString("total"))){
            returnMap.put("code","00");
            returnMap.put("msg","当前没有委托单");
            return returnMap;
        }else{
            JSONArray jsonArray = jsonData.getJSONArray("list");
            for(int i = 0;i < jsonArray.size(); i++){
                JSONObject accountJSON = jsonArray.getJSONObject(i);
                if(orderInfoBean.getOrderId().equals(accountJSON.getString("orderId"))){
                    returnMap.put("code","98");
                    returnMap.put("msg","上一笔订单未成交");
//                if(orderInfoBean.getSymbol().equals(accountJSON.getString("symbol"))){
                    //返回该类型的所有委托单
                    returnMap.put("order"+i,accountJSON.toJSONString());
//                    returnMap.put("symbol",orderInfoBean.getSymbol());
//                    returnMap.put("orderId", accountJSON.getString("orderId"));
//                    String buyOrSell = accountJSON.getString("type");
//                    returnMap.put("type",buyOrSell.equals("1")?"1":"2");
//                    returnMap.put("price",accountJSON.getString("price"));
//                    returnMap.put("priceType",accountJSON.getString("priceType"));
                    return returnMap;
                }
            }
        }
        returnMap.put("code","00");
        returnMap.put("msg","当前订单已成交");
        System.out.println("委托单查询结束:"+returnMap);
        return  returnMap;
    }


    public static void main(String[] args) throws Exception {
        final String secretKey = "80a2d314edda4d3f92463aa5dd5ffd14";
        final String accessKey = "697a9a56-34db-4b9b-a0c9-fcfc7f7ddab9";
        AaCoinService aaCoinService = new AaCoinService();
        OrderInfoBean orderInfoBean = new OrderInfoBean();
        orderInfoBean.setSymbol("AAT_ETH");
        orderInfoBean.setAmount(new BigDecimal(15));
        orderInfoBean.setPrice(new BigDecimal("0.00017465"));
        orderInfoBean.setBuyOrSell("1");
        orderInfoBean.setSecret(secretKey);
        orderInfoBean.setApiKey(accessKey);
        System.out.println(orderInfoBean.toString());
         Map<String,String> resp =  aaCoinService.orderpending(orderInfoBean);
        System.out.println(resp.toString());



    }

}
