package com.hz.util.okhttp_extend;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 2016/4/13.
 */
public class FileUtil {

    public static void write(Context context, List list){
        ObjectOutputStream oos = null;
      try {
          FileOutputStream fileOutputStream = context.openFileOutput("xxx", context.MODE_PRIVATE);
          oos = new ObjectOutputStream(fileOutputStream);
          oos.writeObject(list);
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          try {
              oos.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
  }

  public static List read(Context context){
      ObjectInputStream ois = null;
      List list = null;
      try {
          FileInputStream fileInputStream = context.openFileInput("xxx");
          ois = new ObjectInputStream(fileInputStream);
          list = (List) ois.readObject();
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (StreamCorruptedException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } catch (ClassNotFoundException e) {
          e.printStackTrace();
      } finally {
          try {
              assert ois != null;
              ois.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      return list;
  }


}
