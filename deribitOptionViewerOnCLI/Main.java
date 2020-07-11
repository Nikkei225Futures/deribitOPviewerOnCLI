public class Main{
    public static void main(String[] args){
        DeribitApi api = new DeribitApi();
        CommandExecuter exe = new CommandExecuter(api);
        while(true){
            System.out.println();
            exe.selectCommand();
        }
    }
}