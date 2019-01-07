package cn.shock.orderclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener{


    String [] dishes;//菜名
    private String responsebody ="";
    int dining_id;
    private int[] numbers;//菜数目
    int[] dishes_id;//菜ID
    int[] price;//菜价格
    int[] instock;//库存
    Button commit ;
    Button paymoney;
    int money = 0;//消费价钱
    String payflag = "";
    String commitflag = "";
    long sleep = 1200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        dining_id = bundle.getInt("postId");
        Log.i("shockc dining_id ",""+dining_id);
        paymoney = findViewById(R.id.paymoney);
        commit = findViewById(R.id.menu_commit);

        paymoney.setOnClickListener(this);
        commit.setOnClickListener(this);
        getdata();

    }

    public void getdata(){
        //获取菜谱
        getdishes();

        while(true){
            Log.i("shockc","is sleepping ");
            try {
                new Thread().sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!responsebody.equals("")&&responsebody != null){
                Log.i("shockc","rsponsebody is not null");
                break;
            }
        }
        jsonarray();

    }

    //菜品点击响应
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        //点击事件
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            //增加
            TextView textView = (TextView)view.findViewById(android.R.id.text1);
            if(instock[position]>=numbers[position]+1){
                numbers[position]+=1;
                instock[position]-=1;
                money = money + price[position]*numbers[position];

                String text0 = dishes[position]+"   "+price[position]+"元"+"  "+numbers[position]+"份";
                textView.setText(text0);
                textView.invalidate();
                paymoney.setText("结账\n"+money+"元");
                paymoney.invalidate();
            }else {
                Toast.makeText(MenuActivity.this,dishes[position]+"的库存不够了",Toast.LENGTH_SHORT).show();
            }



        }

    };

    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            //减少
            TextView textView = (TextView)view.findViewById(android.R.id.text1);
            if(numbers[i]-1>=0){
                instock[i]+=1;
                numbers[i]-=1;
                String text0 = dishes[i]+"   "+price[i]+"元"+"  "+numbers[i]+"份";
                textView.setText(text0);
                textView.invalidate();
                paymoney.setText("结账\n"+money+"元");
                paymoney.invalidate();
            }


            return true;
        }
    };
    //菜品数量点击响应

    //列表方法
    public void listview(String[] str){
        ListView listview = (ListView) findViewById(R.id.listview);

        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                str);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(onClickListView);//注册监听
        listview.setOnItemLongClickListener(onItemLongClickListener);//注册监听;
        paymoney.setText("结账\n"+money+"元");
        paymoney.invalidate();
    }


    public void jsonarray(){
        try {
            List<String> list = new ArrayList<String>();
            JSONArray jsonArray = new JSONArray(responsebody);

            responsebody = "";
            numbers = new int[(jsonArray.length())];
            dishes_id = new int[(jsonArray.length())];
            dishes = new String [(jsonArray.length())];
            price = new int[(jsonArray.length())];
            instock = new int[(jsonArray.length())];
            getnumbers();
            new Thread().sleep(sleep);
            for(int i=0;i<jsonArray.length();i++ ){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                dishes_id[i]=jsonObject.getInt("id");
                dishes[i] = jsonObject.getString("name");
                price[i] = jsonObject.getInt("price");
                instock[i] = jsonObject.getInt("instock");
                money = money+price[i]*numbers[i];
                String str = jsonObject.getString("name")+"   "+jsonObject.getInt("price")+"元"+"  "+numbers[i]+"份";
                list.add(str);
            }

            Log.i("shockc",list.toString());
            listview((String [])list.toArray(new String[(list.size())]));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getnumbers(){
        String weatherUrl = "http://"+getResources().getString(R.string.localhost)+":8080/OrderWeb/getdishesnum?menu_id="+dining_id;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONArray intarray = new JSONArray(body);
                    for(int q=0; q<intarray.length();q++){
                        numbers[q] = intarray.getJSONObject(q).getInt("dishes_num");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("shockc",body);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.i("shockc","网络出错");

            }
        });
    }

    public  void getdishes(){

        String weatherUrl = "http://"+getResources().getString(R.string.localhost)+":8080/OrderWeb/SettingDishes";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responsebody = response.body().string();
                Log.i("shockc",responsebody);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.i("shockc","网络出错");

            }
        });

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.paymoney :
                if(money<=0){
                    return;
                }
                paymoney();
                try {
                    new Thread().sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                if(payflag.equals("true")){
                    onBackPressed();
                }else {
                    Toast.makeText(MenuActivity.this,"未成功结账",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_commit :

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            commitdishes();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                try {
                    new Thread().sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("shockccommitflag",commitflag);
                if(commitflag.equals("true")){
                    Toast.makeText(MenuActivity.this,"菜单已提交",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MenuActivity.this,"网络延迟，请重新提交",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //付钱请求
    public void paymoney(){
        String weatherUrl = "http://"+getResources().getString(R.string.localhost)+":8080/OrderWeb/Paymoney?menu_id="+dining_id;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                 payflag = response.body().string();
                Log.i("shockc payflag",responsebody);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.i("shockc","网络出错");

            }
        });
    }
    //菜单参数设置
    public String setdishes() throws JSONException {
        JSONArray jsonarray = new JSONArray();
        for(int m =0;m<dishes.length;m++){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dishes_id",dishes_id[m]);
            jsonObject.put("menu_id",dining_id);
            jsonObject.put("instock",instock[m]);
            jsonObject.put("dishes_num",numbers[m]);
            jsonObject.put("money",money);
            jsonarray.put(jsonObject);
        }

        return jsonarray.toString();

    }

    //提交菜单
    public void commitdishes() throws JSONException, IOException {
        String Url = "http://"+getResources().getString(R.string.localhost)+":8080/OrderWeb/AddMenu";
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        RequestBody body;
        String str = setdishes();
        if(str.equals("")){
            return;
        }
        body = RequestBody.create(JSON, str);
        Request request = new Request.Builder()
                .url(Url)
                .post(body)
                .build();
        Response responsepost = client.newCall(request).execute();
        commitflag = responsepost.body().string();

    }
}
