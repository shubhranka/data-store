import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runners.JUnit4;

import javax.xml.transform.Result;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class MyTestClass {

    HashMap<String, String> ll = new HashMap<>();
    MyStore store;
    File file ;
    ArrayList<String> keys = new ArrayList<>();

    @Before
    public void before() throws IOException {
        file = new File("testFile.txt");
        file.createNewFile();
        store = new MyStore(file.getAbsolutePath());
    }

    @After
    public void after(){
        file.delete();
    }
    @Test
    public void addingKey() {
        ll.put("mykye1", "somedata");
        ll.put("mykye2", "somedata2");
        assertEquals(2, store.addKey(ll));
        ll.put("mykey3","somedata3");
        assertEquals(1,store.addKey(ll));
    }

    @Test
    public void searchingkey(){
        ll.put("mykye1", "somedata");
        keys.add("mykye1");
        assertEquals(1,store.addKey(ll));
        assertEquals(ll.get("mykye1"),store.searchKey(keys).get("mykye1"));
    }

    @Test
    public void deletingKey(){
        keys.add("mykye1");
        ll.put("mykye1", "somedata");
        assertEquals(1,store.addKey(ll));
        assertEquals(1,store.deleteKey(keys));
    }


}
