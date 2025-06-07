package DBMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BitmapIndex implements Serializable {
    private Map<String, ArrayList<Integer>> index;

    public BitmapIndex() {
        index = new HashMap<>();
    }

   
    public void add(String key, int bit) {
        index.computeIfAbsent(key, k -> new ArrayList<>()).add(bit);
    }

    public Map<String, ArrayList<Integer>> getIndex() {
        return index;
    }

    public ArrayList<String> getAllValues() {
        return new ArrayList<>(index.keySet());
    }
    public boolean contains(String key) {
        return index.containsKey(key);
    }

    public void createNew(String key, int size) {
        ArrayList<Integer> bitmap = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            bitmap.add(0);
        }
        index.put(key, bitmap);
    }
    public ArrayList<Integer> getBitmapForKey(String key) {
 
        if (index.containsKey(key)) {
            return index.get(key);  
        } else {
            return new ArrayList<>(); 
        }
    }
    public void putBitmap(String key, ArrayList<Integer> bitmap) {
        index.put(key, bitmap);
    }

}
