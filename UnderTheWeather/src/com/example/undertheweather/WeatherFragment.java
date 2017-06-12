package com.example.undertheweather;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;



public class WeatherFragment extends Fragment {
    Typeface weatherFont;
     
    TextView cityField;
    TextView updatedField;
    TextView condition;
    TextView humidity;
    TextView pressure;
    TextView currentTemperatureField_mxm;
    TextView currentTemperatureField_min;
    TextView weatherIcon;
     
    Handler handler;
 
    public WeatherFragment(){   
        handler = new Handler();
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        cityField = (TextView)rootView.findViewById(R.id.current_location);
        updatedField = (TextView)rootView.findViewById(R.id.current_date);
        condition = (TextView)rootView.findViewById(R.id.condtion);
        humidity = (TextView)rootView.findViewById(R.id.humidity);
        pressure = (TextView)rootView.findViewById(R.id.pressure);
        currentTemperatureField_mxm = (TextView)rootView.findViewById(R.id.current_weather_high);
        currentTemperatureField_min = (TextView)rootView.findViewById(R.id.current_weather_low);
        weatherIcon = (TextView)rootView.findViewById(R.id.weather_condition);
         
        weatherIcon.setTypeface(weatherFont);
        return rootView; 
    }
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);  
	    //weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");     
	    weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "weather.ttf");     
	    updateWeatherData(new CityPreference(getActivity()).getCity());
	}
     
	
	private void updateWeatherData(final String city){
	    new Thread(){
	        public void run(){
	            final JSONObject json = RemoteFetch.getJSON(city);
	            if(json == null){
	                handler.post(new Runnable(){
	                    public void run(){
	                        Toast.makeText(getActivity(), 
	                                getActivity().getString(R.string.place_not_found), 
	                                Toast.LENGTH_LONG).show(); 
	                    }
	                });
	            } else {
	                handler.post(new Runnable(){
	                    public void run(){
	                        renderWeather(json);
	                    }
	                });
	            }               
	        }
	    }.start();
	}
	String country = "";
	private void renderWeather(JSONObject json){
	    try {
	    	
	    	country =((json.getJSONObject("sys").getString("country").toString().equals("ZA"))? "South Africa":json.getJSONObject("sys").getString("country"));
	    	
	        cityField.setText(json.getString("name") + ", " + country);
	         
	        JSONObject details = json.getJSONArray("weather").getJSONObject(0);
	        JSONObject main = json.getJSONObject("main");
	        condition.setText(details.getString("description").toUpperCase(Locale.US));
	        humidity.setText("Humidity: " +"\n" +  main.getString("humidity") + "%");
	        pressure.setText("Pressure: " +"\n" +  main.getString("pressure") + " hPa");
	         
	        double max = main.getDouble("temp_max");
	        double min = main.getDouble("temp_min");
	         
	        currentTemperatureField_mxm.setText("max: "+
	                    String.format("%.2f", max) + " ℃");
	        currentTemperatureField_min.setText("min: "+
                    String.format("%.2f", min)+" ℃");
	 
	        DateFormat df = DateFormat.getDateInstance();
	        String date = df.format(new Date(json.getLong("dt")*1000));
	        updatedField.setText(date);
	 
	        setWeatherIcon(details.getInt("id"),
	                json.getJSONObject("sys").getLong("sunrise") * 1000,
	                json.getJSONObject("sys").getLong("sunset") * 1000);
	         
	    }catch(Exception e){
	        Log.e("SimpleWeather", "One or more fields not found in the JSON data");
	    }
	}
	
	private void setWeatherIcon(int actualId, long sunrise, long sunset){
	    int id = actualId / 100;
	    String icon = "";
	    if(actualId == 800){
	        long currentTime = new Date().getTime();
	        if(currentTime>=sunrise && currentTime<sunset) {
	            icon = getActivity().getString(R.string.weather_sunny);
	        } else {
	            icon = getActivity().getString(R.string.weather_clear_night);
	        }
	    } else {
	        switch(id) {
	        case 2 : icon = getActivity().getString(R.string.weather_thunder);
	                 break;         
	        case 3 : icon = getActivity().getString(R.string.weather_drizzle);
	                 break;     
	        case 7 : icon = getActivity().getString(R.string.weather_foggy);
	                 break;
	        case 8 : icon = getActivity().getString(R.string.weather_cloudy);
	                 break;
	        case 6 : icon = getActivity().getString(R.string.weather_snowy);
	                 break;
	        case 5 : icon = getActivity().getString(R.string.weather_rainy);
	                 break;
	        }
	    }
	    weatherIcon.setText(icon);
	}
	
	public void changeCity(String city){
	    updateWeatherData(city);
	}
	
}