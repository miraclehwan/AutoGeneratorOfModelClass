package com.example.miraclehwan.autogeneratemodelclass;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    String URL = "http://samples.openweathermap.org/data/2.5/weather?q=London,uk&appid=b6907d289e10d714a6e88b30761fae22";

    String result;
    ArrayList<String> resultList = new ArrayList<>();

    int T_VALUE = 1;
    int T_OBJECT = 2;
    int T_ARRAY = 3;

    String MainClassName;
    ArrayList<String> AnnotationList = new ArrayList<>();
    ArrayList<String> GetterList = new ArrayList<>();

    String[] list = {"id", "main", "description", "icon"};

    String json = "{\"coord\":{\"lon\":-0.13,\"lat\":51.51},\"weather\":[{\"id\":300,\"main\":\"Drizzle\",\"description\":\"light intensity drizzle\",\"icon\":\"09d\"}],\"base\":\"stations\",\"main\":{\"temp\":280.32,\"pressure\":1012,\"humidity\":81,\"temp_min\":279.15,\"temp_max\":281.15},\"visibility\":10000,\"wind\":{\"speed\":4.1,\"deg\":80},\"clouds\":{\"all\":90},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"GB\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2643743,\"name\":\"London\",\"cod\":200}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainClassName = "WeatherRepo";
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                getModelClassData(json, MainClassName);
                String result = "";
                for (int i = 0; i < resultList.size(); i++) {
                    result = result + resultList.get(i);
                }
                Log.e("daehwanlog", result);
            }
        });




    }

    private void makeNormalAnnoAndGetter(String jsonKey, String variableType){
        String VariableName = "m" + jsonKey.substring(0, 1).toUpperCase() + jsonKey.substring(1);
        AnnotationList.add(getString(R.string.annotationSerializedName, jsonKey, variableType, VariableName));
        GetterList.add(getString(R.string.getterItem, variableType, VariableName, VariableName));
    }

    private void makeListAnnoAndGetter(String jsonKey){
        String reNaming_jsonKey = "List<" + jsonKey + ">";
        String VariableName = "m" + jsonKey.substring(0, 1).toUpperCase() + jsonKey.substring(1) + "List";
        AnnotationList.add(getString(R.string.annotationSerializedName, jsonKey, reNaming_jsonKey, VariableName));
        GetterList.add(getString(R.string.getterItem, reNaming_jsonKey, VariableName, VariableName));
    }

    private int checkType(String s){
        if (s.equals("{")){
            return T_OBJECT;
        }else if (s.equals("[")){
            return T_ARRAY;
        }else{
            return T_VALUE;
        }
    }

    private String getModelClassData(String jsonInput, String className){
        HashMap<String, String> todoItem;
        ArrayList<HashMap<String, String>> todoList = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject jsonObject = new JSONObject(jsonInput);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()){
                String currentkey = keys.next();
                switch (checkType(jsonObject.getString(currentkey).substring(0,1))){
                    case 1:
                        if (jsonObject.get(currentkey) instanceof String){
                            makeNormalAnnoAndGetter(currentkey, "String");
                        }else if (jsonObject.get(currentkey) instanceof Integer){
                            makeNormalAnnoAndGetter(currentkey, "int");
                        }else if (jsonObject.get(currentkey) instanceof Long){
                            makeNormalAnnoAndGetter(currentkey, "long");
                        }else if (jsonObject.get(currentkey) instanceof Double){
                            makeNormalAnnoAndGetter(currentkey, "double");
                        }else if (jsonObject.get(currentkey) instanceof Boolean){
                            makeNormalAnnoAndGetter(currentkey, "boolean");
                        }
                        break;
                    case 2:
                        todoItem = new HashMap<String, String>();
                        todoItem.put(currentkey, jsonObject.getString(currentkey));
                        todoList.add(todoItem);
                        makeNormalAnnoAndGetter(currentkey, currentkey);
                        break;
                    case 3:
                        todoItem = new HashMap<String, String>();
                        todoItem.put(currentkey, jsonObject.getJSONArray(currentkey).get(0).toString());
                        todoList.add(todoItem);
                        makeListAnnoAndGetter(currentkey);
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String result = null;

        result = getString(R.string.ClassStart, className);
        resultList.add(getString(R.string.ClassStart, className));

        for (int i = 0; i < AnnotationList.size(); i++) {
            result = result + "\n" + AnnotationList.get(i);
            resultList.add("\n" + AnnotationList.get(i));
        }
//        for (int i = 0; i < GetterList.size(); i++) {
//            result = result + "\n" + GetterList.get(i);
//        }

        AnnotationList.clear();
        GetterList.clear();

        for (int i = 0; i <todoList.size() ; i++) {
            String key = todoList.get(i).keySet().toString();
            key = key.substring(1, key.length()-1);
            getModelClassData(todoList.get(i).get(key), key);
        }

        result = result + getString(R.string.ClassEnd);
        resultList.add(getString(R.string.ClassEnd));

//        Log.e("daehwanlog/" + className, result);
        return result;
    }

}
