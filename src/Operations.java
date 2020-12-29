import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class Operations{
    protected String filename;
    private HashMap<String, String> presentData;
    private HashMap<Integer, String> searchKeysHelper;
    Operations(String filename){
        this.filename = filename;
    }

    public HashMap<String,String> searchP(HashMap<String,String> keys){
        presentData = new HashMap<>();
        searchKeysHelper = new HashMap<>();
        Searcher s1 = new Searcher(keys);
        try {
            s1.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return presentData;
    }
    public int writeP(HashMap<String,String> keys){
        int n = keys.size();
        presentData = new HashMap<>();
        searchKeysHelper = new HashMap<>();
        Writer s1 = new Writer(keys);
        try {
            s1.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return n - presentData.size();
    }
    public int deleteP(HashMap<String,String> keys){
        presentData = new HashMap<>();
        searchKeysHelper = new HashMap<>();
        Deleter s1 = new Deleter(keys);
        try {
            s1.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return presentData.size();
    }
    private String search(HashMap<String,String> keys,BufferedReader bufferedReader) {
        int c = 0;
        StringBuilder mykey = new StringBuilder();
        StringBuilder startdata = new StringBuilder();
        int count = 0;
        try {
            while (c != '|' && c != -1) {
                c = bufferedReader.read();
                if ((c == ',' || c == '|') && !keys.isEmpty() && keys.containsKey(mykey.toString())) {
                    keys.remove(mykey.toString());
                    searchKeysHelper.put(count,mykey.toString());
                    presentData.put(mykey.toString(),"");
                    mykey = new StringBuilder();
                    count++;
                } else if ((c == ',' || c == '|')) {
                    startdata.append(mykey);
                    startdata.append(",");
                    mykey = new StringBuilder();
                    count++;
                } else
                    mykey.append((char)c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return startdata.toString();
    }
    private String getData(BufferedReader bufferedReader) throws IOException {
        int count = 0;
        StringBuilder remainingdata = new StringBuilder();
        int c = '\0';
        StringBuilder data = new StringBuilder();
        try{
            while (!searchKeysHelper.isEmpty() && c != -1) {
                c = bufferedReader.read();
                if ( (char)c == '~') {
                    if (searchKeysHelper.containsKey(count)){
                        presentData.put(searchKeysHelper.get(count),data.toString());
                        searchKeysHelper.remove(count);
                    }else {
                        remainingdata.append(data);
                        remainingdata.append("~");
                    }
                    data = new StringBuilder();
                    count++;
                }else
                    data.append((char)c);
            }
            while(c != -1){
                c = bufferedReader.read();
                remainingdata.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(remainingdata.length() == 0)
            return "";
        return remainingdata.substring(0,remainingdata.length()-1);
    }


    private class Writer implements Runnable {
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;
        HashMap<String,String> keys;
        Thread t;

        Writer(HashMap<String,String> keys){
            this.keys = keys;
            t = new Thread(this,"WriterThread");
            t.start();
        }

        public void run(){
            String startData;
            try{
                RandomAccessFile fileStream = new RandomAccessFile(filename,"rw");
                FileChannel channel = fileStream.getChannel();
                FileLock fileLock = channel.tryLock();
                bufferedReader = new BufferedReader(Channels.newReader(channel,StandardCharsets.UTF_8));
                startData = search(keys,bufferedReader);
                if(keys.isEmpty()){
                    fileStream.close();
                    return;
                }
                int c = 0;
                channel.truncate(0);

                BufferedWriter bufferedWriter = new BufferedWriter(Channels.newWriter(fileStream.getChannel(),StandardCharsets.UTF_8));
                bufferedWriter.write(startData);
                Iterator<Map.Entry<String, String>> iterator = presentData.entrySet().iterator();
                while(iterator.hasNext()) {
                    bufferedWriter.write(iterator.next().getKey());
                    if(iterator.hasNext())
                        bufferedWriter.write(",");
                }
                if(presentData.size() > 0)
                    bufferedWriter.write(",");
                iterator = keys.entrySet().iterator();
                while(iterator.hasNext()) {
                    bufferedWriter.write(iterator.next().getKey());
                    if(iterator.hasNext())
                        bufferedWriter.write(",");
                }
                bufferedWriter.write("|");
                bufferedWriter.write(getData(bufferedReader));

                for (String value:presentData.values()){
                    bufferedWriter.write(value + "~");
                }
                for (String value:keys.values()){
                    bufferedWriter.write(value + "~");
                }

                bufferedWriter.close();
                fileStream.close();
            } catch (IOException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }

        }

    }

    private class Searcher implements Runnable {
        Thread t;
        private final HashMap<String,String> keys;
        private ArrayList<String> datas;
        FileInputStream fileInputStream;
        BufferedReader bufferedReader;
        FileChannel channel;
        FileLock fileLock;
        Searcher(HashMap<String,String> keys){
            this.keys = keys;
            t = new Thread(this,"ReaderThread");
            try {
                fileInputStream = new FileInputStream(filename);
                channel = fileInputStream.getChannel();
                fileLock = channel.lock(0,Long.MAX_VALUE,true);
                bufferedReader = new BufferedReader(Channels.newReader(channel, StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
            t.start();
        }
        public void run(){
            try {
                search(keys,bufferedReader);
                if(!searchKeysHelper.isEmpty()){
                    getData(bufferedReader);
                }
                fileLock.close();
                channel.close();
                fileInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    private class Deleter implements Runnable{
        Thread t;
        HashMap<String, String> keys;
        Deleter(HashMap<String, String> keys){
            t = new Thread(this,"Y Deleter");
            this.keys = keys;
            t.start();
        }
        public void run(){
            try{
                RandomAccessFile randomAccessFile = new RandomAccessFile(filename,"rw");
                FileChannel channel = randomAccessFile.getChannel();
                FileLock fileLock = channel.lock();
                BufferedReader bufferedReader = new BufferedReader(Channels.newReader(channel,StandardCharsets.UTF_8));
                BufferedWriter bufferedWriter = new BufferedWriter((Channels.newWriter(channel,StandardCharsets.UTF_8)));
                String firstdata = search(keys,bufferedReader);
                if(presentData.size() > 0){
                    String remaingData = getData(bufferedReader);
                    channel.truncate(0);
                    bufferedWriter.write(firstdata);
                    bufferedWriter.write("|");
                    bufferedWriter.write(remaingData);
                }
                fileLock.close();
                bufferedWriter.close();
                channel.close();
                randomAccessFile.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
