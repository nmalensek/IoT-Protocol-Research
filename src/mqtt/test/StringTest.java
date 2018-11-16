package mqtt.test;

import java.util.ArrayList;
import java.util.HashMap;

public class StringTest {

    private static HashMap<Integer, String> stringHashMap = new HashMap<>();
    private static ArrayList<String> stringList = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(String.format("%s H:%.2f%%", System.currentTimeMillis(), 50.3));
        for (int i = 0; i < 90000; i++) {
//            stringHashMap.put(i, "this is an example string");
            stringList.add("this is an example string" + i);
        }
        System.out.println(stringHashMap.keySet().size());
        System.out.println(stringList.size());
        System.out.println(stringList.get(89999));
    }
}
