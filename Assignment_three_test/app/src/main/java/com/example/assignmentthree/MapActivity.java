package com.example.assignmentthree;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// 高德地图相关引用
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.AMapException; // 必须导入这个异常类
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements AMap.OnMarkerClickListener, PoiSearch.OnPoiSearchListener {

    private MapView mapView;
    private AMap aMap;
    private EditText etSearch;
    private ImageView btnSearch;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_do_search);
        mapView = findViewById(R.id.map);

        mapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (!TextUtils.isEmpty(keyword)) {
                doSearchDestination(keyword);
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(keyword)) {
                    doSearchDestination(keyword);
                }
                return true;
            }
            return false;
        });
    }

    private void setUpMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
        aMap.setOnMarkerClickListener(this);
    }

    private void enableMyLocation() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        myLocationStyle.interval(2000);
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);

        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ================== 搜索逻辑部分 ==================

    private void doSearchDestination(String keyword) {
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "");
        query.setPageSize(1);
        query.setPageNum(0);

        // ★★★ 修复点 1：添加 try-catch 捕获 AMapException ★★★
        try {
            PoiSearch poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                @Override
                public void onPoiSearched(PoiResult result, int rCode) {
                    if (rCode == 1000) {
                        if (result != null && result.getQuery() != null && result.getPois().size() > 0) {
                            aMap.clear();

                            PoiItem destination = result.getPois().get(0);
                            LatLonPoint point = destination.getLatLonPoint();
                            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());

                            // 添加红色 Marker (目的地)
                            aMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(destination.getTitle())
                                    .snippet(destination.getSnippet())
                                    // ▼▼▼▼▼▼▼▼▼▼ 修改了这里 ▼▼▼▼▼▼▼▼▼▼
                                    // 原来是: .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    // 现在改为加载你的红色图片:
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_marker_location_searched)));
                            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

                            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

                            // 搜索附近的公园
                            searchNearbyParks(point);

                        } else {
                            Toast.makeText(MapActivity.this, "No result found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MapActivity.this, "Search failed: " + rCode, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPoiItemSearched(PoiItem poiItem, int i) {}
            });
            poiSearch.searchPOIAsyn();

        } catch (AMapException e) {
            e.printStackTrace();
            Toast.makeText(this, "Search Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchNearbyParks(LatLonPoint centerPoint) {
        PoiSearch.Query query = new PoiSearch.Query("公园", "风景名胜", "");
        query.setPageSize(10);
        query.setPageNum(0);

        // ★★★ 修复点 2：添加 try-catch 捕获 AMapException ★★★
        try {
            PoiSearch poiSearch = new PoiSearch(this, query);
            poiSearch.setBound(new PoiSearch.SearchBound(centerPoint, 3000));

            poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                @Override
                public void onPoiSearched(PoiResult result, int rCode) {
                    if (rCode == 1000) {
                        if (result != null && result.getPois().size() > 0) {
                            ArrayList<PoiItem> parks = result.getPois();
                            for (PoiItem park : parks) {
                                LatLonPoint pt = park.getLatLonPoint();
                                LatLng ll = new LatLng(pt.getLatitude(), pt.getLongitude());

                                // 添加绿色 Marker (公园)
                                Marker marker = aMap.addMarker(new MarkerOptions()
                                        .position(ll)
                                        .title(park.getTitle())
                                        .snippet(park.getSnippet())
                                        // ▼▼▼▼▼▼▼▼▼▼ 修改了这里 ▼▼▼▼▼▼▼▼▼▼
                                        // 原来是: .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                        // 现在改为加载你的绿色图片:
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_marker_location_park)));

                                marker.setObject(park);
                            }
                        }
                    }
                }

                @Override
                public void onPoiItemSearched(PoiItem poiItem, int i) {}
            });

            poiSearch.searchPOIAsyn();

        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    // ================== 点击事件 ==================

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object obj = marker.getObject();
        if (obj instanceof PoiItem) {
            PoiItem poiItem = (PoiItem) obj;

            Intent intent = new Intent(MapActivity.this, DetailActivity.class);
            intent.putExtra("NAME", poiItem.getTitle());
            intent.putExtra("ADDRESS", poiItem.getSnippet());
            intent.putExtra("LAT", poiItem.getLatLonPoint().getLatitude());
            intent.putExtra("LNG", poiItem.getLatLonPoint().getLongitude());

            startActivity(intent);
            return true;
        }
        return false;
    }

    // 接口必须实现的方法（虽然可能没用到）
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {}
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {}

    // ================== 生命周期 ==================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}