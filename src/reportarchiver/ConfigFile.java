package reportarchiver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javafx.util.Pair;


public class ConfigFile {
  private final ArrayList<KeyValue> mList = new ArrayList<>();

  
  public void clear() {
    mList.clear();
  }
  
  
  private int find (String key) {
    final int n = mList.size();

    if (key != null) {
      for(int i=0; i<n; i++)
        if (key.equals(mList.get(i).getKey())) {
            return i;
        }
    }

    return -1;
  }


  public void write(String key, String value) {
    final int index = find(key);
    final KeyValue newItem = new KeyValue(key, value);
    if(index < 0) {
      mList.add(newItem);
    }
    else {
      mList.set(index, newItem);
    }
  }


  public void write(String key, boolean value) {
    write(key, Boolean.toString(value));
  }


  public void write(String key, int value) {
    write(key, Integer.toString(value));
  }


  public void write(String key, float value) {
    write(key, Float.toString(value));
  }


  public String read(String key, String defValue) {
    final int index = find(key);
    return index < 0 
            ? defValue
            : mList.get(index).getValue();
  }


  public boolean read(String key, boolean defValue) {
    final String valueStr = read(key, null);
    boolean value = defValue;

    if("true".equalsIgnoreCase(valueStr)) {
      value = true;
    }
    else if("false".equalsIgnoreCase(valueStr)) {
      value = false;
    }

    return value;
  }


  public int read(String key, int defValue) {
    int result = defValue;
    try {
      result = Integer.parseInt(read(key, null));
    }
    catch(Exception exception) {
    }
    return result;
  }


  public float read(String key, float defValue) {
    float result = defValue;
    try {
      result = Float.parseFloat(read(key, null));
    }
    catch(Exception exception) {
    }
    return result;
  }


  public boolean load(String fileName) {
    boolean done = false;
    
    try (BufferedReader reader =
            new BufferedReader(
            new InputStreamReader(
            new FileInputStream(fileName), StandardCharsets.UTF_8))) {
      String line;

      while ((line = reader.readLine()) != null) {
        final int pos = line.indexOf('=');
        if (pos > 0) {
          final String key = line.substring(0, pos).trim();
          final String value = line.substring(pos + 1).trim();
          write(key, value);
        }
      }
      done = true;
    }
    catch(Exception exception) {
    }

    return done; 
  }


  public boolean save(String fileName) {
    boolean done = false;
    final String lineSeparator =  System.getProperty("line.separator");
    
    try (BufferedWriter writer =
        new BufferedWriter(
        new OutputStreamWriter (
        new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
      final int n = mList.size();
      
      for (int i=0; i < n; i++) {
        final KeyValue item = mList.get(i);
        final String line = item.getKey() + "=" + item.getValue() + lineSeparator;

        writer.write(line);
      }
      
      done = true;
    }
    catch(Exception exception) {
    }

    return done;
  }


  private static class KeyValue extends Pair<String, String> {
    public KeyValue(String key, String value) {
      super(key, value);  
    }
  }
}
