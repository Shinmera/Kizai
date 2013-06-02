package org.tymoonnext.bot.module.misc;

import NexT.data.ConfigLoader;
import NexT.data.required;
import NexT.util.Toolkit;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Weather extends Module implements CommandListener{
    private static final String weatherAPI = "https://api.forecast.io/forecast/$APIKEY/$LATITUDE,$LONGITUDE,$TIMESTAMP?units=si&exclude=hourly,daily,flags";
    private static final String locationAPI = "https://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=$ADDRESS";
    
    
    class C extends ConfigLoader{
        @required public String weatherApiKey;
    };
    private C conf = new C();

    public Weather(Kizai bot){
        super(bot);
        
        CommandModule.register(bot, "weather", "location".split(" "), "Get weather info about a certain location.", this);
        conf.load(bot.getConfig().get("modules").get("misc.Weather"));
    }
    
    public void shutdown(){
        bot.unregisterAllCommands(this);
    }
    
    public void onCommand(CommandEvent cmd){
        try{
            double[] coords = getCoordinates(cmd.getArgs());
            WeatherData data = getWeather(coords[0], coords[1]);
            
            cmd.getStream().send("Weather data for "+cmd.getArgs()+": "+data.summary+", "+
                                                                        data.temperature+"°C "+
                                                                        data.windSpeed+"m/s "+
                                                                        data.humidity+"hum "+
                                                                        data.pressure+"hPa", cmd.getChannel());
        }catch(Exception ex){
            cmd.getStream().send("Failed to retrieve weather data: "+ex.getMessage(), cmd.getChannel());
        }
    }
    
    public WeatherData getWeather(double latitude, double longitude) throws Exception{
        return getWeather(latitude, longitude, System.currentTimeMillis()/1000);
    }
    public WeatherData getWeather(double latitude, double longitude, long time) throws Exception{
        try{
            String response = Toolkit.downloadFileToString(new URL(weatherAPI.replace("$APIKEY", conf.weatherApiKey)
                                                                             .replace("$LATITUDE", latitude+"")
                                                                             .replace("$LONGITUDE", longitude+"")
                                                                             .replace("$TIMESTAMP", time+"")));
            Commons.log.finest(toString()+" Received response for "+latitude+","+longitude+","+time+": "+response);
            
            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
            JsonObject currently = root.getAsJsonObject("currently");
            return new Gson().fromJson(currently, WeatherData.class);
        }catch(MalformedURLException ex){
            Commons.log.log(Level.WARNING, toString()+" Failed to build URL request for "+latitude+","+longitude+","+time+".", ex);
            throw new Exception("Internal error.", ex);
        }
    }

    public double[] getCoordinates(String place) throws UnknownLocationException{
        try{
            String response = Toolkit.downloadFileToString(new URL(locationAPI.replace("$ADDRESS", place.replaceAll(" ", "%20"))));
            Commons.log.finest(toString()+" Received response for '"+place+"': "+response);
            
            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");
            if(results.size() == 0) throw new UnknownLocationException(place);
            
            JsonObject result = results.get(0).getAsJsonObject();
            JsonObject geometry = result.getAsJsonObject("geometry");
            JsonObject location = geometry.getAsJsonObject("location");
            double lat = location.get("lat").getAsDouble();
            double lng = location.get("lng").getAsDouble();
            Commons.log.finest(toString()+" Recv latitude: "+lat+" longitude: "+lng);
            
            double[] coords = new double[2];
            coords[0] = lat;coords[1] = lng;
            return coords;
        }catch(MalformedURLException ex){
            Commons.log.log(Level.WARNING, toString()+" Failed to retrieve coordinates for '"+place+"'.", ex);
            throw new UnknownLocationException(place);
        }
    }
}

class UnknownLocationException extends Exception{
    public UnknownLocationException(String place){super("Location '"+place+"' not found.");}
}

class WeatherData{
    public long time;
    public String summary;
    public String icon;
    public double precipIntensity;
    public double precipProbability;
    public String precipType;
    public double temperature;
    public double dewPoint;
    public double windSpeed;
    public double windBearing;
    public double cloudCover;
    public double humidity;
    public double pressure;
    public double ozone;
}