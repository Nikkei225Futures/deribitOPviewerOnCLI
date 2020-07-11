import java.util.*;

public class CommandExecuter{
    DeribitApi api;

    CommandExecuter(DeribitApi api){
        this.api = api;
    }

    public void selectCommand() {
        String input = this.inputCommand(api.getCurrentApiNet() + "-" + api.getCurrentAsset());
        if (input.equals("help")) {
            System.out.println(">setAsset");
            System.out.println(">setNet");
            System.out.println(">getIndex");
            System.out.println(">getContractMonthes");
            System.out.println(">getTickers");
            System.out.println(">getBoard");
            System.out.println(">exit");

        }else if(input.equals("exit")){
            System.exit(0);
       
        }else if(input.equals("setAsset")){
            String selectedAsset = inputCommand("BTC or ETH");
            this.api.setAsset(selectedAsset);
        
        }else if(input.equals("setNet")){
            String selectedAsset = inputCommand("live or test");
            this.api.setApiNet(selectedAsset);

        }else if(input.equals("getIndex")){
            System.out.println(this.api.getCurrentAsset() + " index: " + this.api.getIndexPrice());
        
        }else if(input.equals("getContractMonthes")){
            String[] contractMonthes = api.getAvailableContractMonth();
            for(int i = 0; i < contractMonthes.length; i++){
                System.out.println(contractMonthes[i]);
            }

        }else if(input.equals("getTickers")){
            String selectedCM = this.inputCommand("Contract Month");
            String[] tickers = api.getAvailableTickersByContractMonth(selectedCM);
            for(int i = 0; i < tickers.length; i+=2){
                System.out.println(tickers[i] + "\t" + tickers[i+1]);
            }

        }else if(input.equals("getBoard")){
            String selectedContractMonth = inputCommand("Contract Month");
            String[] boardLines = api.getOpBoard(selectedContractMonth);

            this.showDividingLine();
            System.out.println("\t\t\t\t\tCALL\t\t\t\t\t\t\t\tPUT\t\t\t\t\t");
            System.out.print("stat" + "\t" + "bidIV" + "\t" + "size" + "\t" + "bid" + "\t" + "ask" + "\t" + "size" + "\t" + "askIV" + "\t" + "OI");
            System.out.print("\t\t" + "strike" + "\t\t"); 
            System.out.print("OI" + "\t" + "bidIV" + "\t" + "size" + "\t" + "bid" + "\t" + "ask" + "\t" + "size" + "\t" + "askIV" + "\t" + "stat");
            System.out.println();
            this.showDividingLine();
            for(int i = 0; i < boardLines.length; i++){
                System.out.println(boardLines[i]);
                this.showDividingLine();
            }
        }

        else{
            System.out.println("type command, type \"help\" to see all commands");
        }


    }
    
    private void showDividingLine(){
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------");
    }


    private String inputCommand(String str){
        System.out.print(str + "> ");
        Scanner scan = new Scanner(System.in);
        return scan.next();
    }

}