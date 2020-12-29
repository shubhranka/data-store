import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyStore {
    private File file;
    private final Operations operations;

    MyStore() {
        createFile();
        operations = new Operations(file.getAbsolutePath());
    }

    MyStore(String path) {
        file = new File(path);
        operations = new Operations(file.getAbsolutePath());
    }

    private HashMap<String, String> makeHashmap(ArrayList<String> keyss) {
        HashMap<String, String> keys = new HashMap<>();
        for (String key : keyss)
            keys.put(key, "");
        return keys;
    }

    public HashMap<String, String> searchKey(ArrayList<String> keyss) {   // return keys value data
        HashMap<String, String> keys = makeHashmap(keyss);
        keys = operations.searchP(keys);
        if(keys.size() == 0)
            System.out.println("key not found");
        else
            System.out.println((keyss.size() - keys.size()) + " keys are not found");
        return keys;
    }

    public int deleteKey(ArrayList<String> keyss) {  // return number of deleted keys
        HashMap<String, String> keys = makeHashmap(keyss);
        int n = operations.deleteP(keys);
        System.out.println( n + " keys are deleted");
        return n;
    }

    public int addKey(HashMap<String, String> keyss) { // return number of written keys
        int gb = (1024*1024*1024);
        Iterator<Map.Entry<String,String>> iterator = keyss.entrySet().iterator();
        int datasize = 0;
        while(iterator.hasNext()){
            Map.Entry<String,String> fd;
            fd = iterator.next();
            datasize += fd.getKey().length() + fd.getValue().length();
        }
        long filesize = file.length()/gb;
        if(filesize + datasize/gb >= 1){
            System.out.println("limit exceeded");
            return -1;
        }
        int n = operations.writeP(keyss);
        System.out.println(n + " keys are already present");
        return n;
    }

    private void createFile() {
        String currentDir = System.getProperty("user.dir"); // Getting the current directory
        File dir = new File(currentDir + "/stores"); // creating a folder for data-store
        if (!dir.exists()) {  // if folder doesn't exists, create it
            dir.mkdir();
        }
        String fileName = "store";
        String fileExt = ".txt";
        file = new File(dir.getPath() + "/" + fileName + fileExt);
        try {
            int n = 0;
            while (!file.createNewFile()) {
                file = new File(dir.getPath() + "/" + fileName + n + fileExt);
                n++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}