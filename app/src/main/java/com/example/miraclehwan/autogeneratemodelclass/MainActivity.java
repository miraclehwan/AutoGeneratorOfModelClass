package com.example.miraclehwan.autogeneratemodelclass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    ArrayList<String> resultList = new ArrayList<>();
    String result;

    int T_VALUE = 1;
    int T_OBJECT = 2;
    int T_ARRAY = 3;

    ArrayList<String> AnnotationList = new ArrayList<>();
    ArrayList<String> GetterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView resultTextview = (TextView) findViewById(R.id.result);
        final EditText getJsondata = (EditText) findViewById(R.id.inputJson);
        final EditText getClassname = (EditText) findViewById(R.id.inputClassName);
        Button makeBtn = (Button) findViewById(R.id.makeBtn);
        final Button copyBtn = (Button) findViewById(R.id.resultCopyBtn);

        makeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getJsondata.getText().length()!=0 && getClassname.getText().length()!=0){
                    final String MainClassName = getClassname.getText().toString();
                    final String JsonData = getJsondata.getText().toString();
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            resultList.clear();
                            getModelClassData(JsonData, MainClassName);
                            result = "";
                            for (int i = 0; i < resultList.size(); i++) {
                                result = result + resultList.get(i);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resultTextview.setText(result);
                                }
                            });
                        }
                    });
                }else{
                    Toast.makeText(MainActivity.this, "Check your input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultTextview.getText().length()!=0){
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Result", resultTextview.getText());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Result is Empty", Toast.LENGTH_SHORT).show();
                }
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

    private void getModelClassData(String jsonInput, String className){
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

        resultList.add(getString(R.string.ClassStart, className));

        for (int i = 0; i < AnnotationList.size(); i++) {
            resultList.add("\n" + AnnotationList.get(i));
        }
        for (int i = 0; i < GetterList.size(); i++) {
            resultList.add("\n\n" + GetterList.get(i));
        }

        AnnotationList.clear();
        GetterList.clear();

        for (int i = 0; i <todoList.size() ; i++) {
            String key = todoList.get(i).keySet().toString();
            key = key.substring(1, key.length()-1);
            getModelClassData(todoList.get(i).get(key), key);
        }

        resultList.add(getString(R.string.ClassEnd));

    }

}
