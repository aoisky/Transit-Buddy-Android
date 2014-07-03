package net.thacg.transitbuddy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class APIHandler {

	private static final String LOG_TAG = "APIHandler";
	private static final String API_KEY = "";
	
	//API URI Lists
	private static final String BUS_ROOT_URI = "http://www.ctabustracker.com/bustime/";
	private static final String GET_DIRECTION_API = "api/v1/getdirections";
	private static final String GET_ROUTES_API = "api/v1/getroutes";
	private static final String GET_STOPS_API = "api/v1/getstops";
	private static final String GET_PREDICTIONS_API = "api/v1/getpredictions";
	
	private static final String TRAIN_ROOT_URI = "http://lapi.transitchicago.com/";
	private static final String TRAIN_ARRIVALS_API = "api/1.0/ttarrivals.aspx";
	
	
	
	/**
	 * Utility function
	 * Check the network availability of this device
	 * @return network availability
	 */
	public static boolean isNetworkAvaliable(Context context){
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		Log.d(LOG_TAG, "Network Unavailable");
		return false;
	}
	
	
	private static Map<String, Object> parseXML(String xmlStr){
		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlStr));
			Document doc = xmlBuilder.parse(is);
			
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * Build parameters from a Map
	 * @param paramMap
	 * @return parameters string
	 */
	private static String buildParams(Map<String, String> paramMap){
		StringBuilder sb = new StringBuilder();
		sb.append("?");
		
		int length = paramMap.size();
		int count = 0;
		
		for(String param : paramMap.keySet()){
			count++;
			sb.append(param);
			sb.append("=");
			sb.append(paramMap.get(param));
			if(count != length){
				sb.append("&");
			}
		}
		
		return sb.toString();
		
	}
	
	/**
	 * A utility function to connect to the API URI
	 * @param apiPath
	 * @param parameters
	 * @return XML Response
	 */
	private static String apiConnect(String apiPath, String parameters){
		URL url;
		try{
			Log.d(LOG_TAG, "Start connecting to the api: " + apiPath);
			url = new URL(apiPath + parameters);
			HttpURLConnection apiConn = (HttpURLConnection)url.openConnection();
			apiConn.setReadTimeout(10000);
			apiConn.setConnectTimeout(10000);
			apiConn.setRequestMethod("GET");
			apiConn.setDoInput(true);
			apiConn.setDoOutput(true);
			
			apiConn.connect();
			
			InputStream xmlResponseStream = apiConn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(xmlResponseStream));
			StringBuilder sb = new StringBuilder();
			String line;
			
			while((line = reader.readLine()) != null){
				sb.append(line);
				sb.append("\n");
			}
			
			reader.close();
			apiConn.disconnect();
			String xmlResponseStr = sb.toString();
			
			Log.d(LOG_TAG, "XML Response: " + xmlResponseStr);
			
			return xmlResponseStr;
			
		}catch(IOException ex){
			Log.d(LOG_TAG, "API Connection Exception");
			ex.printStackTrace();
		}
		
		return null;
	}
}
