import java.io.*;
import java.util.ArrayList;

public class Backup {
    private File source;
    private File parentDestination;
    private File prevBackupFile;
    private File backupFile;
    private long prevBackupTime;
    private ArrayList<String> markedFiles = new ArrayList<String>();
    private ArrayList<String> correctedMarkedFiles = new ArrayList<String>();
    private Boolean hasMarkedFiles;

    public Backup (String source, String parentDestination) {
        this.source = new File(source);
        this.parentDestination = new File(parentDestination);
        backupFile = new File (parentDestination+"/Backup"+getBackupNumber());  // new directory for backup to go
    }

    public void fullDump () {
        try {
            fullCopy(source, backupFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void incrementalDump () {
        markFiles(source);   // initial mark the of modified files/all directories
        for ( String file : markedFiles) {   //loop through each file in the marked array
            hasMarkedFiles = false;
            if (new File (file).isDirectory()){
                checkMarkedDirectories(new File (file));   //loop through directories and if it has any edited files, has marked files is changed to true
            } else {
                correctedMarkedFiles.add(file);   //adds all of the marked files to the corrected marked files array
            }
            if (hasMarkedFiles){   //if hasMarkedFiles was changed to true, the directory needs to be marked
                correctedMarkedFiles.add(file);   //add it to the corrected array
            }
        }
        if (correctedMarkedFiles.isEmpty()){
            System.out.println("There have been no changes since the previous backup");
        } else {
            try {
                copyMarkedFiles(source, backupFile);   //copy all of the corrected marked files
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for ( String file : correctedMarkedFiles) {
            System.out.println(file + "has been backed up");
        }

    }

    private void fullCopy(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {  //creates the directories in the backup file
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();   //creates an array of all files in the directory
            for (int i=0; i<children.length; i++) {    //for each file call fullCopy again with the new source/target
                fullCopy(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {   //if its a file, copy the data from the source to the target
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            in.transferTo(out);
            in.close();
            out.close();
        }
    }

    //initial mark of modified files and all directories
    private void markFiles(File current) {
        if (current.getName().equals(".DS_Store")){   //mac automatically generates these files so I skip them
            return;
        }
        if (current.isDirectory()) {     //if it is a directory, mark it, get all children and recall
            markedFiles.add(current.toString());
            String[] children = current.list();
            for (int i=0; i<children.length; i++) {
                markFiles(new File(current, children[i]));
            }
        } else {
            if (current.lastModified() > prevBackupTime){
                markedFiles.add(current.toString());   // if it has been modified, mark it
            }
        }
    }

    //second mark of only modified files and the directories that contain them
    private void checkMarkedDirectories(File current) {
        if (current.getName().equals(".DS_Store")){   //mac automatically generates these files so I skip them
            return;
        }
        if (current.isDirectory()) {     //if it is a directory, get all children and recall;
            String[] children = current.list();
            for (int i=0; i<children.length; i++) {
                checkMarkedDirectories(new File(current, children[i]));
            }
        } else {
            if (markedFiles.contains(current.toString())){
                hasMarkedFiles = true;   //if the file was on the previous list, mark it
            }
        }
    }

    // recursively copies all files/directories that have been marked
    private void copyMarkedFiles(File sourceLocation, File targetLocation) throws IOException {
        Boolean pass = false;
        for ( String file : correctedMarkedFiles) {   //loop through all the files on the marked list and if it is on there, give it a pass
            if (file.equals(sourceLocation.toString())){
                pass = true;
            }
        }
        if (!pass){
            return;
        }

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {   //creates the directories in the backup file
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();   //creates an array of all files in the directory
            for (int i=0; i<children.length; i++) {
                copyMarkedFiles(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));   //recursively calls function for all children
            }
        } else {   //if its a file, copy the data from the source to the target
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            in.transferTo(out);
            in.close();
            out.close();
        }
    }

    //checks if there is a previous backup
    public Boolean backupPresent () {
        File tempFile = new File(parentDestination+"/Backup1");
        if (tempFile.exists()){
            setPrevBackupFileAndTime();
            return true;
        } else {
            return false;
        }
    }

    //sets the time of the previous backup (is called in backupPresent function)
    private void setPrevBackupFileAndTime () {
        prevBackupFile = new File(parentDestination+"/Backup"+(getBackupNumber()-1));
        prevBackupTime = prevBackupFile.lastModified();
    }

    //gets the number of the backup
    private int getBackupNumber() {
        int backupNumber = parentDestination.listFiles().length;
        return backupNumber;
    }
}
