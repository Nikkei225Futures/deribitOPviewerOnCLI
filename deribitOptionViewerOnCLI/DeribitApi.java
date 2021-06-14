import org.json.*;
import java.util.*;
import java.math.*;

public class DeribitApi {
    private final String liveEntryPoint = "https://www.deribit.com/api/v2";
    private final String testEntryPoint = "https://test.deribit.com/api/v2";
    private String entryPoint = this.liveEntryPoint;
    private ExeCommand cmd = new ExeCommand();
    private String asset = "BTC";
    private float currentIndex = 0;
    
    //setter asset
    public void setAsset(String assetName){
        if(assetName.equals("BTC") || assetName.equals("ETH")){
            this.asset = assetName;
        }else{
            System.out.println("asset symbol should be BTC or ETH");
        }
    }
    
    //setter api net
    public void setApiNet(String apiNet){
        if(apiNet.equals("test")){
            this.entryPoint = testEntryPoint;
        }else if(apiNet.equals("live")){
            this.entryPoint = liveEntryPoint;
        }else{
            System.out.println("api name should be live or test");
        }
    }

    //getter asset
    public String getCurrentAsset(){
        return this.asset;
    }

    public String getCurrentApiNet(){
        if(this.entryPoint.equals(this.liveEntryPoint)){
            return "liveNet";
        }else{
            return "testNet";
        }
    }


    //インデックス価格の取得
    public float getIndexPrice(){
        String endPoint = "/public/get_index?currency=" + this.asset;
        JSONObject wholeData = this.getAPIResult(endPoint);

        JSONObject result = wholeData.getJSONObject("result");
        float indexPrice = result.getFloat(asset);
        this.currentIndex = indexPrice;
        return indexPrice;
    }

    public String[] getOrderBooks(String instrumentName){
        int depth = 5;
        String endPoint = "/public/get_order_book?depth=5&";
        endPoint += instrumentName;
        JSONOBject wholeData = this.getAPIResult(endPoint);
        JSONOArray bids = wholeData.getJSONArray("bids");
        JSONOArray asks = wholeData.getJSONArray("asks");
        String[] bids = new String[depth-1];
        String[] asks = new String[depth-1];
        String[] results = new String[depth-1];

        for(int i = 0; i < depth; i++){
            bids[i] = bids.getJSONObject(i).toString();
            asks[i] = asks.getJSONObject(i).toString();
        }


        for(int i = 0; i < depth; i++){
            results[i] = bids[i] + "\t" + asks[i];
        }

        return results;

    }
    
    //全てのオプションの限月を表示
    public String[] getAvailableContractMonth(){
        String[] allTickers = getAllAvailableTickers();
        String[] availableTickers = this.getTickersWithoutStrikeType(allTickers);
        return availableTickers;
    }

    //限月内で利用可能なストライクとcall or putを返す
    public String[] getAvailableTickersByContractMonth(String ticker){
        String[] allTickers = getAllAvailableTickers();
        ArrayList<String> tickersMatchedContractMonth = new ArrayList<>();

        for(int i = 0; i < allTickers.length; i++){
            if(allTickers[i].matches(ticker + ".*")){
                tickersMatchedContractMonth.add(allTickers[i]);
            }
        }
        if(tickersMatchedContractMonth == null){
            System.out.println("specified Contract Month is unavailable");
            return null;
        }
        String[] tickers = tickersMatchedContractMonth.toArray(new String[tickersMatchedContractMonth.size()]);
        
        //sort
        while(true){
            boolean isSorted = true;
            for(int i = 0; i < tickers.length-1; i++){
                String[] currentSplitedTicker = tickers[i].split("-", 0);
                String[] nextSplitedTicker = tickers[i+1].split("-", 0);
                int currentStrike = Integer.parseInt(currentSplitedTicker[2]);
                int nextStrike = Integer.parseInt(nextSplitedTicker[2]);
                if(currentStrike > nextStrike){
                    String temp = tickers[i];
                    tickers[i] = tickers[i+1];
                    tickers[i+1] = temp;
                    isSorted = false;
                }
            } 
            if(isSorted){
                break;
            }
        }
        tickers = this.sortTickersByPutCall(tickers);
        return tickers;
    }

    //オプションボードを返す
    public String[] getOpBoard(String contractMonth){
        String[] availableTickers = getAvailableTickersByContractMonth(contractMonth);
        String[] opBoard = new String[availableTickers.length/2];
        int j = 0;
        this.currentIndex = this.getIndexPrice();
        this.sleep(55);

        for(int i = 0; i < opBoard.length; i++){
            this.sleep(55);
            opBoard[i] = getSpecificTickerOpBoard(availableTickers[j]);
            j++;

            String splitedTickerName[] = availableTickers[j].split("-", 0);
            String strike = splitedTickerName[2];
            
            opBoard[i] += "\t" + strike + "\t\t";
            this.sleep(55);
            opBoard[i] += getSpecificTickerOpBoard(availableTickers[j]);
            j++;
        }
        return opBoard;
    }

    //-----------------------------------------------------------------------------------------
    
    //利用可能なすべてのティッカーを返す
    private String[] getAllAvailableTickers() {
        String endPoint = "/public/get_instruments?currency=" + this.asset + "&expired=false&kind=option";
        JSONObject wholeData = this.getAPIResult(endPoint);

        JSONArray results = wholeData.getJSONArray("result");
        ArrayList<String> tickersWithStrikeType = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            tickersWithStrikeType.add(result.getString("instrument_name"));
        }
        String[] allTickers = tickersWithStrikeType.toArray(new String[tickersWithStrikeType.size()]);
        
        return allTickers;
    }

    //APIを実行してJSON形式で返す
    private JSONObject getAPIResult(String endPoint){
        String url = this.entryPoint + endPoint;
        String command = "cmd.exe /c curl -X GET " + "\"" + url + "\"";
        //System.out.println(url);
        String rawText = cmd.connectStrArr(cmd.exeCmd(command));
        JSONObject wholeData = new JSONObject(rawText);
        return wholeData;
    }
    
    //ティッカーからストライクとcall putの記号を削除した文字列の配列を返す
    private String[] getTickersWithoutStrikeType(String[] allTickers){
        ArrayList <String> tickers = new ArrayList<>();
       
        for(int i = 0; i < allTickers.length; i++){
            String[] splitedTicker = allTickers[i].split("-", 0);
            String tickerWithoutStrikeType = splitedTicker[0] + "-" + splitedTicker[1];
            tickers.add(tickerWithoutStrikeType);

            if(i != 0){
                
                String currentTickerName = tickers.get(tickers.size()-1).toString();
                String previousTickerName = tickers.get(tickers.size()-2).toString();

                if(isCollision(tickers, currentTickerName)){
                    tickers.remove(tickers.size()-1);
                }

            }
        }
        String[] availableTickersWithoutStrikeType = tickers.toArray(new String[tickers.size()]);
        return availableTickersWithoutStrikeType;
    }

    private boolean isCollision(ArrayList<String> list, String target){
        for(int i = 0; i < list.size()-1; i++){
            if(list.get(i).toString().equals(target)){
                return true;
            }
        }
        return false;
    }

    private String[] sortTickersByPutCall(String[] tickers){
        for(int i = 0; i < tickers.length; i+=2){
            if(tickers[i].matches(".*-P.*")){
                String temp = tickers[i];
                tickers[i] = tickers[i+1];
                tickers[i+1] = temp;
            }
        }
        return tickers;
    }

    
    //for creating specific OP board
    //tickerName e.g. BTC-JUL20-9250-P
    //ITM-OTM-ATM, bidIV, bidSize, bid, ask, askSize, askIV, OpenInterest => put
    //OpenInterest, bitIV, bidSize, bid, ask, askSize, askIV, ITM-OTM-ATM => call
    //ITM-OTM indicates the status when OP long
    private String getSpecificTickerOpBoard(String tickerName){
        String endPoint = "/public/ticker?instrument_name=" + tickerName;
        JSONObject wholeData = getAPIResult(endPoint);
        JSONObject result = wholeData.getJSONObject("result");
        System.out.println("loading " + tickerName);

        float currentIndexPrice = result.getFloat("index_price");
        float markIv  = result.getFloat("mark_iv");
        float bidSize = result.getFloat("best_bid_amount");
        float askSize = result.getFloat("best_ask_amount");
        float bid = result.getFloat("best_bid_price");
        float ask = result.getFloat("best_ask_price");
        float openInterest = result.getFloat("open_interest");

        //greeks    
        JSONObject greeks = result.getJSONObject("greeks");
        float delta = greeks.getFloat("delta");
        float theta = greeks.getFloat("theta"); 
        float vega  = greeks.getFloat("vega");

        bid = bid * this.currentIndex;
        ask = ask * this.currentIndex;

        int intBid = (int)bid;
        int intAsk = (int)ask;
        bid = this.roundDownFloat(bid, 2);
        ask = this.roundDownFloat(ask, 2);
        delta = this.roundDownFloat(delta, 2);
        theta = this.roundDownFloat(theta, 1);
        vega = this.roundDownFloat(vega, 2);
        openInterest = this.roundDownFloat(openInterest, 1);
        markIv = this.roundDownFloat(markIv, 1);
        
        String[] splitedTickerName = tickerName.split("-", 0);
        float strike = Float.parseFloat(splitedTickerName[2]);
        
        String currentOpStat = "";

        if(strike == currentIndexPrice){
            currentOpStat = "ATM";
        }else if(splitedTickerName[3].equals("C")){
            if(strike < currentIndexPrice){
                currentOpStat = "ITM";
            }else{
                currentOpStat = "OTM";
            }
        }else if(splitedTickerName[3].equals("P")){
            if(strike > currentIndexPrice){
                currentOpStat = "ITM";
            }else{
                currentOpStat = "OTM";
            }
        }
        
        String[] tickerStatus = new String[10];
        if(splitedTickerName[3].equals("C")){
            tickerStatus[0] = String.valueOf(currentOpStat);
            tickerStatus[1] = String.valueOf(delta);
            tickerStatus[2] = String.valueOf(theta);
            tickerStatus[3] = String.valueOf(vega);
            tickerStatus[4] = String.valueOf(bidSize);
            
            tickerStatus[5] = String.valueOf(bid);
            if(tickerStatus[5].length() >= 7){
                tickerStatus[5] = String.valueOf(intBid);
            }

            tickerStatus[6] = String.valueOf(ask);
            if(tickerStatus[6].length() >= 7){
                tickerStatus[6] = String.valueOf(intAsk);
            }

            tickerStatus[7] = String.valueOf(askSize);
            tickerStatus[8] = String.valueOf(markIv) + "%";
            tickerStatus[9] = String.valueOf(openInterest);
            if(tickerStatus[9].equals("0.0")){
                tickerStatus[9] = "---";
            }
        }else if(splitedTickerName[3].equals("P")){
            tickerStatus[0] = String.valueOf(openInterest);
            tickerStatus[1] = String.valueOf(markIv) + "%";
            tickerStatus[2] = String.valueOf(bidSize);
            
            tickerStatus[3] = String.valueOf(bid);
            if(tickerStatus[3].length() >= 7){
                tickerStatus[3] = String.valueOf(intBid);
            }

            tickerStatus[4] = String.valueOf(ask);
            if(tickerStatus[4].length() >= 7){
                tickerStatus[4] = String.valueOf(intAsk);
            }

            tickerStatus[5] = String.valueOf(askSize);
            tickerStatus[6] = String.valueOf(delta);
            tickerStatus[7] = String.valueOf(theta);
            tickerStatus[8] = String.valueOf(vega);
            tickerStatus[9] = String.valueOf(currentOpStat);
            if(tickerStatus[0].equals("0.0")){
                tickerStatus[0] = "---";
            }
        }


        for(int i = 1; i <= 8; i++){
            if(tickerStatus[i].equals("0") || tickerStatus[i].equals("0.0") || tickerStatus[i].equals("0.0%")){
                tickerStatus[i] = "---";
            }
        }

        String tickerString = "";
        for(int i = 0; i < tickerStatus.length; i++){
            tickerString += tickerStatus[i];
            tickerString += "\t";
        }

        return tickerString;
    }

    /* not used */ 
    private String changeStringColor(String target, String color){
        String end = "\u001b[00m";
        String ret = color + target + end;
        return ret;
    }
    /*not used*/

    private float roundDownFloat(float target, int underP){
        BigDecimal bd = new BigDecimal(String.valueOf(target));
        BigDecimal bdRed = bd.setScale(underP, RoundingMode.DOWN);
        return bdRed.floatValue();
    }

    private void sleep(int milliSec){
        try{
            Thread.sleep(milliSec);
        }catch(InterruptedException e){

        }
    }
}