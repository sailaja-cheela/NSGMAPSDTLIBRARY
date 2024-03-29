package com.nsg.nsgmapslibrary.Classes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.SphericalUtil;
import com.nsg.nsgmapslibrary.Classes.ExpandedMBTilesTileProvider;
import com.nsg.nsgmapslibrary.R;
import com.nsg.nsgmapslibrary.SupportClasses.ETACalclator;
import com.nsg.nsgmapslibrary.SupportClasses.Util;
import com.nsg.nsgmapslibrary.database.db.SqlHandler;
import com.nsg.nsgmapslibrary.database.dto.EdgeDataT;
import com.nsg.nsgmapslibrary.interfaces.ILoadTiles;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
public class NSGLiveTrackingRoutingApiClass2 extends Fragment implements GoogleMap.CancelableCallback{
    LatLng SourcePosition, DestinationPosition;
    //LatLng convertedSrcPosition,convertedDestinationPoisition;
    double sourceLat, sourceLng, destLat, destLng;
    LatLng dubai;
    String SourcePoint;
    String DestinationPoint;
    Marker markerSource, markerDestination,mPositionMarker;
    private Polyline mPolyline;
    private GoogleMap mMap;
    private SqlHandler sqlHandler;
    GoogleMap.CancelableCallback callback;
    ILoadTiles mCallback;
    private double userLocatedLat, userLocatedLongi;
    private List points;
    private List<LatLng> convertedPoints;
    LatLng currentGpsPosition;
    // String distance = "";
    // String duration = "";
    StringBuilder sb = new StringBuilder();
    private List LocationPerpedicularPoints=new ArrayList();
    private ArrayList<LatLng> currentLocationList=new ArrayList<LatLng>();
    private Marker sourceMarker,destinationMarker;
    private List<EdgeDataT> edgeDataList;
    private Handler handler = new Handler();
    // private int index=0;
    // private int next=0;
    private int enteredMode;
    private int routeDeviationDistance;
    List<LatLng> LatLngDataArray=new ArrayList<LatLng>();
    private String currentGpsPoint;
    private Polyline line;
    private List polyLines;
    private Circle mCircle;
    private List<LatLng>lastKnownPosition;
    private LatLng nearestPositionPoint;
    //  BitmapDescriptor mMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.car_icon_32);
    Bitmap mMarkerIcon;
    int mIndexCurrentPoint=0;
    private List<LatLng> edgeDataPointsList ;
    private List AllPointsList ;
    private ProgressDialog dialog;
    private LatLng newCenterLatLng,PointData;
    private List distancesList;
    private List distanceValuesList;
    HashMap<String, String> hash_map;
    private List<LatLng> nearestPointValuesList;
    private Marker gpsMarker;
    private TextView tv,tv1,tv2;
    private String MESSAGE;

    public NSGLiveTrackingRoutingApiClass2() {
        // Required empty public constructor
    }
    @SuppressLint("ValidFragment")
    public NSGLiveTrackingRoutingApiClass2(double v1, double v2, double v3, double v4,int mode,int radius ) {
        //get Cordinates from MainActivity
        SourcePosition = new LatLng(v1, v2);
        DestinationPosition = new LatLng(v4, v3);
        sourceLat = v2;
        sourceLng = v1;
        destLat = v4;
        destLng =v3;
        enteredMode = mode;
        routeDeviationDistance=radius;
        SourcePoint=String.valueOf(v1).concat(" ").concat(String.valueOf(v2));
        DestinationPoint=String.valueOf(v3).concat(" ").concat(String.valueOf(v4));
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqlHandler = new SqlHandler(getContext());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMarkerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.car_icon_32);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.maplite, container, false);
        tv=(TextView)rootView.findViewById(R.id.tv);
        tv1=(TextView)rootView.findViewById(R.id.tv1);
        tv2=(TextView)rootView.findViewById(R.id.tv2);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment1 = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googlemap) {
                mMap = googlemap;
                String BASE_MAP_URL_FORMAT = Environment.getExternalStorageDirectory() + File.separator + "MBTILES" + File.separator + "DubaiBasemap" + ".mbtiles";
                // Environment.getExternalStorageDirectory() + File.separator + "samples"+ File.separator + sectionName+".mbtiles"
                // Log.e("URL FORMAT","URL FORMAT ****************** "+ BASE_MAP_URL_FORMAT);
                TileProvider tileProvider = new ExpandedMBTilesTileProvider(new File(BASE_MAP_URL_FORMAT.toString()), 256, 256);
                TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                        .tileProvider(tileProvider));
                tileOverlay.setTransparency(0.5f - tileOverlay.getTransparency());
                tileOverlay.setVisible(true);

                if (Util.isInternetAvailable(getActivity()) == true && mMap != null && tileOverlay.isVisible()==true) {
                    dialog = new ProgressDialog(getActivity(), R.style.ProgressDialog);
                    dialog.setMessage("Fetching Route");
                    dialog.setMax(100);
                    dialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            GetRouteDetails();
                            if(MESSAGE.equals("Sucess")){
                                getAllEdgesData();
                                addMarkers();
                                dialog.dismiss();
                                // final LatLng position1 = new LatLng(sourceLat, sourceLng);
                                if(enteredMode==1 &&edgeDataList!=null && edgeDataList.size()>0){
                                    MoveWithGpsPointInBetWeenAllPoints();
                                }else if(enteredMode==2){
                                    CalculateNearestViaFakeGPS();
                                }
                            }else{
                                dialog.dismiss();
                                Toast.makeText(getActivity(), "Not Able to get Route from Service", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, 20);
                } else {
                    Toast.makeText(getActivity(), "please turn on wifi/mobiledata", Toast.LENGTH_LONG).show();
                }

            }
        });
        return rootView;
    }
    public void addMarkers(){

        LatLng position1= new LatLng(sourceLat,sourceLng);
        // Log.e("URL FORMAT","Uposition2 T ****************** "+ position1);
        sourceMarker = mMap.addMarker(new MarkerOptions()
                .position(position1)
                .icon(bitmapDescriptorFromVector(getActivity(),R.drawable.source_red)));
        CameraPosition googlePlex = CameraPosition.builder()
                .target(position1)
                .zoom(18)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);


        LatLng position2= new LatLng(destLat,destLng);

        destinationMarker= mMap.addMarker(new MarkerOptions()
                .position(position2)
                .icon(bitmapDescriptorFromVector(getActivity(),R.drawable.destination_green)));
        CameraPosition googlePlex1 = CameraPosition.builder()
                .target(position2)
                .zoom(18)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex1), 1000, null);

    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(10, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private void GetRouteDetails(){
        try{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT > 9) {
                        StrictMode.ThreadPolicy policy =
                                new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        try {
                            String httprequest = "http://202.53.11.74/dtnavigation/api/routing/routenavigate";

                            String FeatureResponse = HttpPost(httprequest,SourcePoint,DestinationPoint);
                            Log.e("RESPONSE", "RESPONSE" + FeatureResponse);
                            JSONObject jsonObject = null;
                            try {
                                if(FeatureResponse!=null){
                                    String delQuery = "DELETE  FROM " + EdgeDataT.TABLE_NAME;
                                    Log.e("DEL QUERY","DEL QUERY " + delQuery);
                                    sqlHandler.executeQuery(delQuery.toString());

                                    jsonObject = new JSONObject(FeatureResponse);
                                    String ID = String.valueOf(jsonObject.get("$id"));
                                    MESSAGE = jsonObject.getString("Message");
                                    String Status = jsonObject.getString("Status");
                                    String TotalDistance = jsonObject.getString("TotalDistance");
                                    JSONArray jSonRoutes = new JSONArray(jsonObject.getString("Route"));
                                    Log.e("jSonRoutes", "jSonRoutes" + jSonRoutes);
                                    for (int i = 0; i < jSonRoutes.length(); i++) {
                                        points=new ArrayList();
                                        convertedPoints=new ArrayList<LatLng>();
                                        Log.e("jSonRoutes", "jSonRoutes" + jSonRoutes.get(i));
                                        // List Routes=new ArrayList();
                                        // Routes.add(jSonRoutes.get(i));
                                        JSONObject Routes = new JSONObject(jSonRoutes.get(i).toString());
                                        String $id = Routes.getString("$id");
                                        String EdgeNo = Routes.getString("EdgeNo");
                                        String GeometryText = Routes.getString("GeometryText");
                                        Log.e("GeometryText", "GeometryText" + GeometryText);
                                        String Geometry = Routes.getString("Geometry");
                                        Log.e("Geometry", "Geometry----" + Geometry);
                                        JSONObject geometryObject = new JSONObject(Routes.getString("Geometry"));
                                        String $id1 = geometryObject.getString("$id");
                                        String type = geometryObject.getString("type");
                                        Log.e("type", "type----" + type);
                                        String coordinates = geometryObject.getString("coordinates");
                                        Log.e("coordinates", "coordinates----" + coordinates);
                                        JSONArray jSonLegs = new JSONArray(geometryObject.getString("coordinates"));
                                        Log.e("jSonLegs", "jSonLegs----" + jSonLegs);
                                        for (int j = 0; j < jSonLegs.length(); j++) {
                                            Log.e("JSON LEGS", "JSON CORDINATES" + jSonLegs.get(j));
                                            points.add(jSonLegs.get(j));
                                            Log.e("JSON LEGS", " LATLNG RESULT------ " + points.size());
                                        }
                                        Log.e("JSON LEGS", " LATLNG RESULT------ " + points.size());
                                        String  stPoint=String.valueOf(jSonLegs.get(0));
                                        // String  endPoint=String.valueOf(jSonLegs.get(jSonLegs.length()-1));

                                        stPoint=stPoint.replace("[","");
                                        stPoint=stPoint.replace("]","");
                                        String [] firstPoint=stPoint.split(",");
                                        Double stPointLat= Double.valueOf(firstPoint[0]);
                                        Double stPointLongi= Double.valueOf(firstPoint[1]);
                                        LatLng stVertex=new LatLng(stPointLongi,stPointLat);
                                        //    endPoint=endPoint.replace("[","");
                                        //    endPoint=endPoint.replace("]","");
                                        //    String [] secondPoint=endPoint.split(",");
                                        //   Double endPointLat= Double.valueOf(secondPoint[0]);
                                        //    Double endPointLongi= Double.valueOf(secondPoint[1]);
                                        //    LatLng endVertex=new LatLng(endPointLongi,endPointLat);

                                        //    double distance=showDistance(stVertex,endVertex);
                                        //    String distanceInKM = String.valueOf(distance/1000);
                                        //    Log.e("Distance -----","Distance in KM-------- "+ distanceInKM);
                                        StringBuilder query = new StringBuilder("INSERT INTO ");
                                        query.append(EdgeDataT.TABLE_NAME).append("(edgeNo,distanceInVertex,startPoint,allPoints,endPoint) values (")
                                                .append("'").append(EdgeNo).append("',")
                                                .append("'").append("distanceInKM").append("',")
                                                .append("'").append(jSonLegs.get(0)).append("',")
                                                .append("'").append(points).append("',")
                                                .append("'").append(jSonLegs.get(jSonLegs.length()-1)).append("')");
                                        sqlHandler.executeQuery(query.toString());
                                        sqlHandler.closeDataBaseConnection();
                                        for (int p = 0; p < points.size(); p++) {
                                            Log.e("JSON LEGS", "JSON POINTS LIST ---- " + points.get(p));
                                            String listItem = points.get(p).toString();
                                            listItem = listItem.replace("[", "");
                                            listItem = listItem.replace("]", "");
                                            Log.e("JSON LEGS", "JSON POINTS LIST ---- " + listItem);
                                            String[] subListItem = listItem.split(",");
                                            Log.e("JSON LEGS", "JSON POINTS LIST ---- " + subListItem.length);
                                            Log.e("JSON LEGS", "JSON POINTS LIST ---- " + subListItem[0]);
                                            Log.e("JSON LEGS", "JSON POINTS LIST ---- " + subListItem[1]);
                                            Double y = Double.valueOf(subListItem[0]);
                                            Double x = Double.valueOf(subListItem[1]);
                                            StringBuilder sb=new StringBuilder();
                                            //  sb.append(x).append(",").append(y).append(":");
                                            //  LocationPerpedicularPoints.add(sb.toString());
                                            LatLng latLng = new LatLng(x, y);
                                            Log.e("JSON LEGS", " LATLNG RESULT------ " + latLng);
                                            convertedPoints.add(latLng);
                                            for (int k = 0; k < convertedPoints.size(); k++) {
                                                MarkerOptions markerOptions = new MarkerOptions();
                                                PolylineOptions polylineOptions = new PolylineOptions();
                                                if(polylineOptions!=null && mMap!=null) {
                                                    markerOptions.position(convertedPoints.get(k));
                                                    markerOptions.title("Position");
                                                    // polylineOptions.color(Color.RED);
                                                    // polylineOptions.width(6);
                                                    polylineOptions.addAll(convertedPoints);
                                                    // polylineOptions.color(Color.GREEN).width(10);
                                                    polylineOptions.color(Color.BLACK).width(12);
                                                    // Polyline polyline =
                                                    mMap.addPolyline(polylineOptions);
                                                    polylineOptions.color(Color.CYAN).width(18);
                                                    mMap.addPolyline(polylineOptions);

                                                }

                                            }

                                        }


                                    }

                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }catch (Exception ex){

                        }
                        dialog.dismiss();

                    }
                }


            });
        }catch(Exception e){
            e.printStackTrace();
        }
        dialog.dismiss();
    }
    private String HttpPost(String myUrl,String latLng1,String latLng2) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        String LoginResponse = "";
        String result = "";
        URL url = new URL(myUrl);
        Log.v("URL ", " URL: " + url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "text/plain");
        JSONObject jsonObject = buidJsonObject(latLng1,latLng2);
        Log.e(" Message", " jsonObject: " + jsonObject);
        setPostRequestContent(conn, jsonObject);
        conn.connect();
        Log.e("Response Code", "ResponseCode: " + conn.getResponseCode());
        result = conn.getResponseMessage();
        Log.e("Response Message", "Response Message: " + result);

        if (conn.getResponseCode() != 200) {

        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output = null;
            //   System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                LoginResponse = sb.append(output).append(" ").toString();
                //      Log.e("Login Response "," From server ############ "+LoginResponse);
            }
        }
        conn.disconnect();
        return LoginResponse;
    }

    private JSONObject buidJsonObject(String latLng1,String latLng2) throws JSONException {
        JSONObject buidJsonObject = new JSONObject();
        buidJsonObject.accumulate("UserData", buidJsonObject1());
        buidJsonObject.accumulate("StartNode", latLng1);
        buidJsonObject.accumulate("EndNode", latLng2);
        return buidJsonObject;
    }

    private JSONObject buidJsonObject1() throws JSONException {
        JSONObject buidJsonObject1 = new JSONObject();
        buidJsonObject1.accumulate("username", "admin");
        buidJsonObject1.accumulate("password", "admin");
        return buidJsonObject1;
    }

    private void setPostRequestContent(HttpURLConnection conn,
                                       JSONObject jsonObject) throws IOException {
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        // Log.i(LoginActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }


    @Override
    public void onFinish() {

    }

    @Override
    public void onCancel() {

    }
    private  List<EdgeDataT> getAllEdgesData() {
        String query = "SELECT * FROM " + EdgeDataT.TABLE_NAME;
        Cursor c1 = sqlHandler.selectQuery(query);
        edgeDataList = (List<EdgeDataT>) SqlHandler.getDataRows(EdgeDataT.MAPPING, EdgeDataT.class, c1);
        sqlHandler.closeDataBaseConnection();
        return edgeDataList;
    }

    public void MoveWithGpsPointInBetWeenAllPoints(){
        getLatLngPoints();
        getAllEdgesData();
        edgeDataPointsList = new ArrayList<LatLng>();

        //LatLng srcP1=new LatLng(sourceLat,sourceLng);
        // edgeDataPointsList.add(SourcePosition);
        if (edgeDataList != null && edgeDataList.size() > 0) {
            AllPointsList=new ArrayList<>();
            for (int i = 0; i < edgeDataList.size(); i++) {
                EdgeDataT edge = new EdgeDataT();
                edge = edgeDataList.get(i);
                edge.getEdgeNo();
                String stPoint = edge.getStartPoint();
                String endPoint = edge.getEndPoint();
                String points = edge.getAllPoints();
                //[[55.07252845510704,24.986485718893903], [55.07252691395126,24.986503080465624], [55.07252858393359,24.9865204314153], [55.072533418545014,24.986537282374343], [55.072541282105426,24.9865531573588]]
                if(points!=null){
                    String AllPoints = points.replace("[", "");
                    AllPoints = AllPoints.replace("]", "");
                    String[] AllPointsArray = AllPoints.split(", ");
                    Log.e("ALL POINTS", "ALL POINTS" + AllPointsArray.length);

                    for (int ap = 0; ap < AllPointsArray.length; ap++) {
                        AllPointsList.add(AllPointsArray[ap]);
                    }
                }
            }
        }
        Log.e("ALL POINTS ", "FROM DATABASE ----- " + AllPointsList.size());
        for (int pntCount = 0; pntCount < AllPointsList.size(); pntCount++) {
            String data = String.valueOf(AllPointsList.get(pntCount));
            String dataStr = data.replace("[", "");
            dataStr = dataStr.replace("]", "");
            String ptData[] = dataStr.split(",");
            double Lat = Double.parseDouble(ptData[0]);
            double Lang = Double.parseDouble(ptData[1]);
            PointData = new LatLng(Lat, Lang);
            edgeDataPointsList.add(PointData);
            Log.e("ALL POINTS ", "FROM DATABASE ----- " + edgeDataPointsList.get(pntCount));
        }
        Log.e("ALL POINTS ", "FROM DATABASE ----- " + AllPointsList.size());
        Log.e("ALL POINTS ", "FROM DATABASE ----- " + edgeDataPointsList.size());
        nearestPointValuesList=new ArrayList<LatLng>();
        for (int j = 0; j < LatLngDataArray.size(); j++) {
            currentGpsPosition = LatLngDataArray.get(j);
            // List<LatLng> EdgeWithoutDuplicates = new ArrayList<>(edgeDataPointsList);
            List<LatLng> EdgeWithoutDuplicates = removeDuplicates(edgeDataPointsList);
            if (EdgeWithoutDuplicates != null && EdgeWithoutDuplicates.size() > 0) {
                Log.e("currentGpsPosition ", "currentGpsPosition POINT----------" + currentGpsPosition);
                String FirstCordinate="",SecondCordinate="";
                distancesList = new ArrayList();
                distanceValuesList = new ArrayList();
                hash_map = new HashMap<String, String>();
                for (int epList = 0; epList < EdgeWithoutDuplicates.size(); epList++) {
                    LatLng PositionMarkingPoint = EdgeWithoutDuplicates.get(epList);
                    Log.e("currentGpsPosition ", "PositionMarking POINT----------" + PositionMarkingPoint);
                    Log.e("currentGpsPosition ", "currentGpsPosition POINT----------" + currentGpsPosition);

                    double distance = distFrom(PositionMarkingPoint.latitude,PositionMarkingPoint.longitude,currentGpsPosition.longitude,currentGpsPosition.latitude);
                    //distanceValuesList.add("A"+" # "+edgeDataPointsList.get(epList));
                    // Mapping string values to int keys
                    // List<LatLng> deduped = list.stream().distinct().collect(Collectors.toList());
                    ;
                    hash_map.put(String.valueOf(distance), String.valueOf(EdgeWithoutDuplicates.get(epList)));
                    // distanceValuesList.add("A"+" ");
                    //  Log.e("Sorted ArrayList ", "in Ascending order : " + distanceValuesList.get(epList));
                    distancesList.add(distance);
                    Collections.sort(distancesList);
                }
                for(int i=0;i<distancesList.size();i++) {
                    Log.e("Sorted ArrayList ", "in Ascending order : " + distancesList.get(i));
                }

                String FirstShortestDistance = String.valueOf(distancesList.get(0));
                String SecondShortestDistance = String.valueOf(distancesList.get(1));
                boolean answerFirst= hash_map.containsKey(FirstShortestDistance);
                if (answerFirst) {
                    System.out.println("The list contains " + FirstShortestDistance);
                    FirstCordinate = (String)hash_map.get(FirstShortestDistance);
                    Log.e("Sorted ArrayList ", "INDEX----- : " + FirstCordinate);
                } else {
                    System.out.println("The list does not contains "+ "FALSE");
                }
                boolean answerSecond= hash_map.containsKey(SecondShortestDistance);
                if (answerSecond) {
                    System.out.println("The list contains " + SecondShortestDistance);
                    SecondCordinate = (String)hash_map.get(SecondShortestDistance);
                    Log.e("Sorted ArrayList ", "INDEX----- : " + SecondCordinate);
                } else {
                    System.out.println("The list does not contains "+ "FALSE");
                }

                //  String[] FirstCordinateArray = FirstCordinate.split("#");
                //  Log.e("Sorted ArrayList ", "in Ascending order ----AT 1---: " + FirstCordinateArray[0]);
                String First= FirstCordinate.replace("lat/lng: (","");
                First= First.replace(")","");
                String[] FirstLatLngsData=First.split(",");
                Double FirstLatitude= Double.valueOf(FirstLatLngsData[0]);
                Double FirstLongitude= Double.valueOf(FirstLatLngsData[1]);
                Log.e("Sorted ArrayList ", "-----FirstLatitude :" + FirstLatitude);
                Log.e("Sorted ArrayList ", "-----FirstLongitude" + FirstLongitude);
                // String[] SecondCordinateArray = SecondCordinate.split("#");
                //  Log.e("Sorted ArrayList ", "in Ascending order ---AT 2--- :" + SecondCordinateArray[0]);
                String Second= SecondCordinate.replace("lat/lng: (","");
                Second= Second.replace(")","");
                String[] SecondLatLngsData=Second.split(",");
                Double SecondLatitude= Double.valueOf(SecondLatLngsData[0]);
                Double SecondLongitude= Double.valueOf(SecondLatLngsData[1]);

                Log.e("Sorted ArrayList ", "-----SecondLatitude :" + SecondLatitude);
                Log.e("Sorted ArrayList ", "-----SecondLongitude" + SecondLongitude);

                LatLng source=new LatLng(FirstLongitude,FirstLatitude);
                LatLng destination=new LatLng(SecondLongitude,SecondLatitude);


                nearestPositionPoint= findNearestPoint(currentGpsPosition,source,destination);

              //  String nearestPoint = GenerateLinePoint(FirstLatitude, FirstLongitude, SecondLatitude, SecondLongitude, currentGpsPosition.longitude, currentGpsPosition.latitude);
               // Log.e("NEAREST POINT", "NEAREST POINT----------" + nearestPoint);
              //  String[] nearestDataStr = nearestPoint.split(",");
              //  double latitude = Double.parseDouble(nearestDataStr[0]);
              //  double longitude = Double.parseDouble(nearestDataStr[1]);
              //  nearestPositionPoint = new LatLng(longitude, latitude);
                nearestPointValuesList.add(nearestPositionPoint);

            }
        }
        Log.e("NEAREST Point", "NEAREST POINT &&&&&&&&&&&&&&&&&&&&&& " + nearestPositionPoint);
        for(int i=0;i<nearestPointValuesList.size();i++) {
            Log.e("Sorted ArrayList ", " NEAREST POINT LIST VALUES : " + nearestPointValuesList.get(i));

        }
        Log.e("EdgeSt Point", "End point" + LatLngDataArray.size());
        // animateLatLngZoom(nearestPositionPoint, 15, 5, 10);
        // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(nearestPositionPoint.latitude,nearestPositionPoint.longitude, 48));
        mPositionMarker = mMap.addMarker(new MarkerOptions()
                .position(nearestPositionPoint)
                .title("currentLocation")
                .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_car_symbol)));
        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(sourceLat,sourceLng))
                .zoom(15)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);
        CameraUpdate center=
                CameraUpdateFactory.newLatLng(new LatLng(nearestPositionPoint.latitude,
                        nearestPositionPoint.longitude));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        // verifyRouteDeviation(routeDeviationDistance);
        //  animateMarkerToFinalDestination(mPositionMarker,DestinationPosition,new LatLngInterpolator.Spherical());
        // animateMarker(mMap,mPositionMarker,LatLngDataArray,false);
        // animateMarkerViaVertex(mPositionMarker,new LatLngInterpolator.Spherical());
        // animateMarkerNew(newCenterLatLng,mPositionMarker);
        animateCarMove(mPositionMarker, nearestPointValuesList.get(0), nearestPointValuesList.get(1), 1000);

    }

    private String GenerateLinePoint(double startPointX, double startPointY, double endPointX, double endPointY, double pointX, double pointY)
    {
        double k = ((endPointY - startPointY) * (pointX - startPointX) - (endPointX - startPointX) * (pointY - startPointY)) / (Math.pow(endPointY - startPointY, 2)
                + Math.pow(endPointX - startPointX, 2));
        double resultX = pointX - k * (endPointY - startPointY);
        double resultY = pointY + k * (endPointX - startPointX);
        StringBuilder sb=new StringBuilder();
        sb.append(resultX).append(",").append(resultY);

        return sb.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void verifyRouteDeviation(int markDistance){
        PolylineOptions polylineOptions = new PolylineOptions();
        //To Verify Route Deviation
        //currentLocationList.add(currentGpsPosition);
        String nearestPoint = GenerateLinePoint( sourceLng,sourceLat,destLng,destLat,currentGpsPosition.longitude,currentGpsPosition.latitude);
        Log.e("NEAREST POINT", "NEAREST POINT----------" + nearestPoint);
        String[] nearestDataStr = nearestPoint.split(",");
        double latitude = Double.parseDouble(nearestDataStr[0]);
        double longitude = Double.parseDouble(nearestDataStr[1]);
        LatLng nearestPosition=new LatLng(longitude,latitude);
        double returnedDistance= showDistance(currentGpsPosition,nearestPosition);
        Log.e("returnedDistance", "returnedDistance --------- "+ returnedDistance);
        drawMarkerWithCircle(nearestPosition,markDistance);
        if(returnedDistance > markDistance){
            Toast toast = Toast.makeText(getContext(), " ROUTE DEVIATED ", Toast.LENGTH_LONG);
            toast.setMargin(100, 100);
            toast.show();
            //drawDeviatedRoute(currentGpsPosition, DestinationPosition);
            String cgpsLat= String.valueOf(currentGpsPosition.latitude);
            String cgpsLongi= String.valueOf(currentGpsPosition.longitude);
            currentGpsPoint=cgpsLongi.concat(" ").concat(cgpsLat);
            Log.e("returnedDistance", "nearest Position--------- "+ nearestPosition);
            Log.e("returnedDistance", "Destination Position --------- "+ DestinationPosition);
            //DestinationPosition=new LatLng(destLat,destLng);
            //Log.e("returnedDistance", "DestinationPosition --------- "+ DestinationPosition);
            //MarkerOptions markerOptions = new MarkerOptions();
            // markerOptions.position(currentGpsPosition);
            // markerOptions.position(DestinationPosition);
            // markerOptions.title("Position");

            // ReRouteFeaturesFromServer download=new ReRouteFeaturesFromServer();
            //  download.execute();
            /*
            polylineOptions.color(Color.RED);
            polylineOptions.width(6);
            points.add(nearestPosition);
            points.add(new LatLng(24.987665, 55.060701));
            points.add(new LatLng(24.988843, 55.062091));
            points.add(new LatLng(24.989472, 55.061488));
            points.add(DestinationPosition);
            if(points.size()>0) {
                polylineOptions.addAll(points);
                line = mMap.addPolyline(polylineOptions);
                if (polylineOptions != null) {
                    if (line != null) {
                        line.remove();
                    }
                    line = mMap.addPolyline(polylineOptions);
                } else
                    Toast.makeText(getContext(), "No route is found", Toast.LENGTH_LONG).show();
            }
            */
            //

        }else{

        }
    }


    private void drawMarkerWithCircle(LatLng gpsPosition,double radius){
        // double radiusInMeters = 400.0;
        CircleOptions circleOptions = new CircleOptions().center(gpsPosition).radius(radius).fillColor(Color.parseColor("#2271cce7")).strokeColor(Color.parseColor("#2271cce7")).strokeWidth(3);
        mCircle = mMap.addCircle(circleOptions);

    }
    private double showDistance(LatLng latlng1,LatLng latLng2) {
        double distance = SphericalUtil.computeDistanceBetween(latlng1,latLng2);
        return distance;
    }
    public int getLatLngPoints(){
        /*Route--1*/
        LatLngDataArray.add(new LatLng(24.988996,55.076971));
        LatLngDataArray.add(new LatLng(24.988944,55.077062));
        LatLngDataArray.add(new LatLng(24.988826,55.077152));
        LatLngDataArray.add(new LatLng(24.988692,55.077325));
        LatLngDataArray.add(new LatLng(24.988523,55.077466));
        LatLngDataArray.add(new LatLng(24.988434,55.077592));
        return LatLngDataArray.size();
    }
    private double getAngle(LatLng beginLatLng, LatLng endLatLng) {
        double f1 = Math.PI * beginLatLng.latitude / 180;
        double f2 = Math.PI * endLatLng.latitude / 180;
        double dl = Math.PI * (endLatLng.longitude - beginLatLng.longitude) / 180;
        return Math.atan2(Math.sin(dl) * Math.cos(f2) , Math.cos(f1) * Math.sin(f2) - Math.sin(f1) * Math.cos(f2) * Math.cos(dl));
    }


    private void animateCarMove(final Marker marker, final LatLng beginLatLng, final LatLng endLatLng, final long duration) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();
        // set car bearing for current part of path
        float angleDeg = (float)(180 * getAngle(beginLatLng, endLatLng) / Math.PI);
        Matrix matrix = new Matrix();
        matrix.postRotate(angleDeg);

        int centreX = mMarkerIcon.getWidth()/2;
        int centreY = mMarkerIcon.getHeight()/2;
        //marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(mMarkerIcon, 0, 0,centreX, centreY, matrix, true)));
      //  marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(mMarkerIcon, 0, 0, mMarkerIcon.getWidth(), mMarkerIcon.getHeight(), matrix, true)));
        Bitmap opBitMap= addPaddingLeftForBitmap(mMarkerIcon,60);
        // marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(mMarkerIcon, 0, 0,centreX, centreY, matrix, true)));
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(opBitMap, 0, 0, opBitMap.getWidth(),opBitMap.getHeight(), matrix, true)));
        handler.post(new Runnable() {
            @Override
            public void run() {
                // calculate phase of animation
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                // calculate new position for marker

                double lat = (endLatLng.latitude - beginLatLng.latitude) * t + beginLatLng.latitude;
                double lngDelta = endLatLng.longitude - beginLatLng.longitude;
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * t + beginLatLng.longitude;
                marker.setPosition(new LatLng(lat, lng));
               // centerMapAt(new LatLng(lat,lng));
                // if not end of line segment of path
                if (t < 1.0) {
                    // call next marker position
                    handler.postDelayed(this, 16);
                } else {
                    // call turn animation
                    nextTurnAnimation();
                }
            }
        });
    }
    private void nextTurnAnimation() {
        mIndexCurrentPoint++;
        Log.e("EdgeListPoints","--------------"+nearestPointValuesList.size());
        if (mIndexCurrentPoint < nearestPointValuesList.size() - 1) {
            LatLng prevLatLng = nearestPointValuesList.get(mIndexCurrentPoint - 1);
            LatLng currLatLng = nearestPointValuesList.get(mIndexCurrentPoint);
            LatLng nextLatLng = nearestPointValuesList.get(mIndexCurrentPoint + 1);

            float beginAngle = (float)(90 * getAngle(prevLatLng, currLatLng) / Math.PI);
            float endAngle = (float)(90 * getAngle(currLatLng, nextLatLng) / Math.PI);

            animateCarTurn(mPositionMarker, beginAngle, endAngle, 10);
        }
    }
    private void animateCarTurn(final Marker marker, final float startAngle, final float endAngle, final long duration) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();

        final float dAndgle = endAngle - startAngle;

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                Matrix m = new Matrix();
                float angle=startAngle + dAndgle * t;
                m.postRotate(angle);
                int width  = Resources.getSystem().getDisplayMetrics().widthPixels;
                int height = Resources.getSystem().getDisplayMetrics().heightPixels;
                Bitmap rotatedBitmap = Bitmap.createBitmap(mMarkerIcon, 0, 0, mMarkerIcon.getWidth(), mMarkerIcon.getHeight(), m, true);

                marker.setIcon(BitmapDescriptorFactory.fromBitmap(rotatedBitmap));


                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    nextMoveAnimation();
                }
            }
        });
    }
    private void nextMoveAnimation() {
        if (mIndexCurrentPoint < nearestPointValuesList.size() - 1) {
            double resultdistance=showDistance(nearestPointValuesList.get(mIndexCurrentPoint),new LatLng(destLat,destLng)); //in km
            double resultMts=resultdistance*1000;
            String finalResultMts=String.format("%.2f", resultMts);
            double speed=10.0; //kmph
            ETACalclator calculator=new ETACalclator();
            double resultTime=calculator.cal_time(resultdistance, speed);
            System.out.println("\n The calculated Time(hr) : "+resultTime);
            tv.setText("Estimated Time : "+ resultTime +" SEC ");
            tv1.setText("DISTANCE : "+ finalResultMts +" Meters ");
            tv2.setText("Speed : "+ speed +"KMPH ");
            animateCarMove(mPositionMarker, nearestPointValuesList.get(mIndexCurrentPoint), nearestPointValuesList.get(mIndexCurrentPoint+1), 10000);
        }
    }
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (float) (earthRadius * c);
        return dist;
    }
    private List<LatLng> removeDuplicates(List<LatLng> EdgeWithoutDuplicates)
    {
        int count = edgeDataPointsList.size();

        for (int i = 0; i < count; i++)
        {
            for (int j = i + 1; j < count; j++)
            {
                if (edgeDataPointsList.get(i).equals(edgeDataPointsList.get(j)))
                {
                    edgeDataPointsList.remove(j--);
                    count--;
                }
            }
        }
        return EdgeWithoutDuplicates;
    }
    public void CalculateNearestViaFakeGPS(){
        Marker gpsMarker=null;
        getLatLngPoints();
        LatLng nearestPosition = null;
        //  nearestPointArray=new ArrayList<LatLng>();
        for(int i=0 ;i<LatLngDataArray.size();i++) {
            currentGpsPosition = LatLngDataArray.get(i);
            gpsMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentGpsPosition)
                    .title("currentLocation")
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.symbol_ht_ss)));
            /*
            String nearestPoint = GenerateLinePoint( sourceLng, sourceLat, destLng, destLat,currentGpsPosition.longitude,currentGpsPosition.latitude);
            Log.e("NEAREST POINT", "NEAREST POINT----------" + nearestPoint);
            String[] nearestDataStr = nearestPoint.split(",");
            double latitude = Double.parseDouble(nearestDataStr[0]);
            double longitude = Double.parseDouble(nearestDataStr[1]);
            nearestPosition=new LatLng(longitude,latitude);
            nearestPointArray.add(nearestPosition);
            mPositionMarker = mMap.addMarker(new MarkerOptions()
                    .position(nearestPosition)
                    .title("currentLocation")
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.circle_pink)));
            */
        }
        animateCarMoveViaFakeGPS(gpsMarker, LatLngDataArray.get(0), LatLngDataArray.get(1), 1000);
        /*
        Log.e("NEAREST POINT", "NEAREST POINT----------" + nearestPointArray.size());
        for(int k=0;k<nearestPointArray.size();k++){
            Log.e("NEAREST POINT", "NEAREST POINT List----------" + nearestPointArray.get(k));
        }
        if(nearestPointArray!=null && nearestPointArray.size()>0) {
            animateCarMoveViaFakeGPS(gpsMarker, nearestPointArray.get(0), nearestPointArray.get(1), 1000);
        }
        */
    }
    public Bitmap addPaddingLeftForBitmap(Bitmap bitmap, int paddingLeft) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth() + paddingLeft, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        //canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, paddingLeft, 0, null);
        return outputBitmap;
    }
    private void animateCarMoveViaFakeGPS(final Marker marker, final LatLng beginLatLng, final LatLng endLatLng, final long duration) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();
        // set car bearing for current part of path
        float angleDeg = (float)(180 * getAngle(beginLatLng, endLatLng) / Math.PI);
        Matrix matrix = new Matrix();
        matrix.postRotate(angleDeg);
        Bitmap opBitMap= addPaddingLeftForBitmap(mMarkerIcon,60);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(opBitMap, 0, 0, opBitMap.getWidth(), opBitMap.getHeight(), matrix, true)));

        handler.post(new Runnable() {
            @Override
            public void run() {
                // calculate phase of animation
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                // calculate new position for marker

                double lat = (endLatLng.latitude - beginLatLng.latitude) * t + beginLatLng.latitude;
                double lngDelta = endLatLng.longitude - beginLatLng.longitude;

                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * t + beginLatLng.longitude;

                marker.setPosition(new LatLng(lat, lng));

                // if not end of line segment of path
                if (t < 1.0) {
                    // call next marker position
                    handler.postDelayed(this, 16);
                } else {
                    // call turn animation
                    nextTurnAnimationViaFakeGPS();
                }
            }
        });
    }
    private void nextTurnAnimationViaFakeGPS() {
        mIndexCurrentPoint++;
        Log.e("EdgeListPoints","--------------"+ LatLngDataArray.size());
        if (mIndexCurrentPoint < LatLngDataArray.size() - 1) {
            LatLng prevLatLng = LatLngDataArray.get(mIndexCurrentPoint - 1);
            LatLng currLatLng = LatLngDataArray.get(mIndexCurrentPoint);
            LatLng nextLatLng = LatLngDataArray.get(mIndexCurrentPoint + 1);

            float beginAngle = (float)(90 * getAngle(prevLatLng, currLatLng) / Math.PI);
            float endAngle = (float)(90 * getAngle(currLatLng, nextLatLng) / Math.PI);

            animateCarTurnViaFakeGPS(mPositionMarker, beginAngle, endAngle, 10);
        }
    }
    private void animateCarTurnViaFakeGPS(final Marker marker, final float startAngle, final float endAngle, final long duration) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();

        final float dAndgle = endAngle - startAngle;

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                Matrix m = new Matrix();
                float angle=startAngle + dAndgle * t;
                m.postRotate(angle);
                int width  = Resources.getSystem().getDisplayMetrics().widthPixels;
                int height = Resources.getSystem().getDisplayMetrics().heightPixels;
                Bitmap rotatedBitmap = Bitmap.createBitmap(mMarkerIcon, 0, 0, mMarkerIcon.getWidth(), mMarkerIcon.getHeight(), m, true);

                marker.setIcon(BitmapDescriptorFactory.fromBitmap(rotatedBitmap));


                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    nextMoveAnimationViaFakeGPS();
                }
            }
        });
    }
    private void nextMoveAnimationViaFakeGPS() {
        if (mIndexCurrentPoint < LatLngDataArray.size() - 1) {
            animateCarMove(mPositionMarker, LatLngDataArray.get(mIndexCurrentPoint), LatLngDataArray.get(mIndexCurrentPoint+1), 10000);
        }
    }

    public void centerMapAt(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }
    private LatLng findNearestPoint(final LatLng p, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return start;
        }

        final double s0lat = Math.toRadians(p.latitude);
        final double s0lng = Math.toRadians(p.longitude);
        final double s1lat = Math.toRadians(start.latitude);
        final double s1lng = Math.toRadians(start.longitude);
        final double s2lat = Math.toRadians(end.latitude);
        final double s2lng = Math.toRadians(end.longitude);

        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
        if (u <= 0) {
            return start;
        }
        if (u >= 1) {
            return end;
        }

        return new LatLng(start.latitude + (u * (end.latitude - start.latitude)),
                start.longitude + (u * (end.longitude - start.longitude)));


    }

}
