public class Main {
    public static void main(String[] args){
        Config cfg = new Config();
        String source = cfg.getProperty("o");    //get the source and destination
        String parentDestination = cfg.getProperty("d");

        Backup backup = new Backup (source, parentDestination);  //create backup object giving it source/destination

        //execute full or incremental dump based on whether there is a previous backup
        if (backup.backupPresent()){
            backup.incrementalDump();
        } else {
            backup.fullDump();
        }
    }
}
