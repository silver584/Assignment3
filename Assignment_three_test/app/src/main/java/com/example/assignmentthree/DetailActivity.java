package com.example.assignmentthree;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    // ==========================================
    // ★★★ 请在这里填入你的 API KEY ★★★
    // ==========================================
    private static final String WEATHER_API_KEY = "458e0fd8b2a6444083884022251612";

    // 如果你有 Google API Key 用于街景图片，填在这里
    // 如果没有，图片可能无法显示，或者你可以换成 picsum 的随机图链接
    private static final String GOOGLE_STREET_VIEW_KEY = "623fcb00d8192d7c395af5d43faacebe";
    // ==========================================

    private TextView txtParkName, txtAddress, txtHours, txtWeather, txtReviewInfo;
    private ImageView imgPark;
    private RatingBar ratingBar;
    private Button btnExit; // 新增的退出按钮变量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. 初始化 UI 控件
        txtParkName = findViewById(R.id.txtParkName);
        txtAddress = findViewById(R.id.txtAddress);
        txtHours = findViewById(R.id.txtHours);
        txtWeather = findViewById(R.id.txtWeather);
        txtReviewInfo = findViewById(R.id.txtReviewInfo);
        imgPark = findViewById(R.id.imgPark);
        ratingBar = findViewById(R.id.ratingBar);
        btnExit = findViewById(R.id.btnExit); // 绑定退出按钮

        // 2. 获取 MapActivity 传递过来的数据
        String name = getIntent().getStringExtra("NAME");
        String address = getIntent().getStringExtra("ADDRESS");
        double lat = getIntent().getDoubleExtra("LAT", 0);
        double lng = getIntent().getDoubleExtra("LNG", 0);

        // 3. 显示基本信息
        txtParkName.setText(name);
        txtAddress.setText(address);

        // 模拟营业时间和评分数据 (因为高德基础搜索不包含这些)
        txtHours.setText("09:00 AM - 06:00 PM (Open)");
        ratingBar.setRating(4.5f);
        txtReviewInfo.setText("4.5 stars (128 reviews)");

        // 4. 设置退出按钮的点击事件
        // 点击后调用 finish()，关闭当前页面，自动返回到地图页
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 5. 加载天气数据
        loadWeatherData(lat, lng);

        // 6. 加载街景图片
        loadStreetViewImage(lat, lng);
    }

    /**
     * 使用 Volley 获取天气
     */
    private void loadWeatherData(double lat, double lng) {
        String url = "https://api.weatherapi.com/v1/current.json?key=" + WEATHER_API_KEY + "&q=" + lat + "," + lng;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject current = response.getJSONObject("current");
                            double tempC = current.getDouble("temp_c");

                            JSONObject condition = current.getJSONObject("condition");
                            String conditionText = condition.getString("text");

                            String weatherInfo = String.format(Locale.getDefault(), "Temp: %.1f°C, %s", tempC, conditionText);
                            txtWeather.setText(weatherInfo);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            txtWeather.setText("Data parsing error");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WeatherAPI", "Error: " + error.toString());
                        txtWeather.setText("Failed to load weather");
                    }
                });

        queue.add(jsonObjectRequest);
    }

    /**
     * 使用 Picasso 加载图片
     */
    private void loadStreetViewImage(double lat, double lng) {
        // 如果你有 Google Key，用下面这行：
        String imageUrl = "https://maps.googleapis.com/maps/api/streetview?size=600x300&location=" + lat + "," + lng + "&fov=90&heading=235&pitch=10&key=" + GOOGLE_STREET_VIEW_KEY;

        // ★如果你没有 Google Key，不想看空白，可以用下面这行随机风景图代替测试★
        // String imageUrl = "https://picsum.photos/600/300";

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imgPark);
    }
}