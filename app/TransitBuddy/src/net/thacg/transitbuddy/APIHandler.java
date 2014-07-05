package net.thacg.transitbuddy;

/*
 *  Copyright (C) 2014 Yudong Yang
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author Yudong Yang (yang.yudong@live.com)
 *
 */

public class APIHandler {

	private static final String LOG_TAG = "APIHandler";
	//Enter the API key here
	private static final String API_KEY = "gw3HKRWGMNK4qAnTgJiswZs3W";
	
	private static final String USER_AGENT = "Mozilla/5.0";
	
	//API URI Lists
	private static final String BUS_ROOT_URI = "http://www.ctabustracker.com/bustime/";
	private static final String GET_DIRECTION_API = "api/v1/getdirections";
	private static final String GET_ROUTES_API = "api/v1/getroutes";
	private static final String GET_STOPS_API = "api/v1/getstops";
	private static final String GET_PREDICTIONS_API = "api/v1/getpredictions";
	
	private static final String GET_TIME_API = "api/v1/gettime";
	
	private static final String TRAIN_ROOT_URI = "http://lapi.transitchicago.com/";
	private static final String TRAIN_ARRIVALS_API = "api/1.0/ttarrivals.aspx";
	

	
	
	/**
	 * Return a Route List from the CTA Server or null if error occurred
	 * @return List<Map<String, String>> Route Map has keys of rt, rtnm and rtclr
	 */
	public static List<Map<String, String>> getRoutes(){
		
		String params = buildParams(null, API_KEY);
		
		String xmlResponseStr = apiConnect(BUS_ROOT_URI, GET_ROUTES_API, params);
		
		if(xmlResponseStr == null){
			Log.d(LOG_TAG, "getRoutes: XML Response is null");
			return null;
		}
		
		Document xmlDoc = parseXML(xmlResponseStr);
		
		NodeList nodeRouteList = xmlDoc.getElementsByTagName("route");
		List<Map<String, String>> routeList = null;
		
		if(nodeRouteList != null && nodeRouteList.getLength() > 0){
			int listLength = nodeRouteList.getLength();
			Node rtNode;
			Node rtnmNode;
			Node rtclrNode;
			routeList = new ArrayList<>();
			
			for(int i = 0; i < listLength; i++){
				rtNode = nodeRouteList.item(i).getChildNodes().item(1);
				rtnmNode = nodeRouteList.item(i).getChildNodes().item(3);
				rtclrNode = nodeRouteList.item(i).getChildNodes().item(5);
				
				Map<String, String> routeInfoMap = new HashMap<>();
				
				if(rtNode != null){
					routeInfoMap.put(rtNode.getNodeName(), rtNode.getTextContent());
				}
				
				if(rtnmNode != null){
					routeInfoMap.put(rtnmNode.getNodeName(), rtnmNode.getTextContent());
				}
				
				if(rtclrNode != null){
					routeInfoMap.put(rtclrNode.getNodeName(), rtclrNode.getTextContent());
				}
				
				if(routeInfoMap.size() > 0){
					routeList.add(routeInfoMap);
				}
			}
			
		}
		
		
		return routeList;
		
	}
	
	
	/**
	 * Get Route Directions API
	 * @param rt
	 * Single route designator 
	 * Alphanumeric designator of a route (ex. �g20�h or 
	 * �gX20�h) for which a list of available directions is to 
	 * be returned. 
	 * @return Map object contains all responses or null if no direction
	 * Error Child element of the root element. Message if the processing of the 
	 * request resulted in an error. 
	 * Dir Child element of the root element. Direction that is valid for the specified 
	 * route designator. For example, �gEast Bound�h. 

	 */
	public static List<String> getDirections(String rt){
		
		if(rt == null){
			return null;
		}
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("rt", rt);
		
		String params = buildParams(paramMap, API_KEY);
		
		String xmlResponseStr = apiConnect(BUS_ROOT_URI, GET_DIRECTION_API, params);
		
		if(xmlResponseStr == null){
			Log.d(LOG_TAG, "getDirecions: XML Response is null");
			return null;
		}
		
		Document xmlDoc = parseXML(xmlResponseStr);
		
		//Obtain dir elements
		NodeList nodeDirList = xmlDoc.getElementsByTagName("dir");
		Node dirNode;
		List<String> dirList = new ArrayList<>();
		
		int listLength = nodeDirList.getLength();
		
		if(listLength > 0){
		
			for(int i = 0; i < listLength; i++){
				dirNode = nodeDirList.item(i);
				String nodeValue = dirNode.getChildNodes().item(0).getNodeValue();
				if(nodeValue != null){
					dirList.add(nodeValue);
				}
			}
			
		}
		
		return dirList;
	}
	
	/**
	 * Get current CTA server time
	 * @return CTA server current time or null if error occurred
	 */
	public static Date getTime(){
		String params = buildParams(null, API_KEY);
		
		String xmlResponseStr = apiConnect(BUS_ROOT_URI, GET_TIME_API, params);
		
		if(xmlResponseStr == null){
			Log.d(LOG_TAG, "getTime: XML Response is null");
			return null;
		}
		
		Document xmlDoc = parseXML(xmlResponseStr);
		
		Node node = xmlDoc.getElementsByTagName("tm").item(0);
		
		String errorMsg = getXmlResponseError(xmlDoc);
		
		if(errorMsg != null){
			Log.d(LOG_TAG, "getTime: Error: " + errorMsg);
		}
		
		if(node != null){
			String tmStr = node.getChildNodes().item(0).getNodeValue();
			SimpleDateFormat df = new SimpleDateFormat("yyyymmdd hh:mm:ss", Locale.US);
			 if(tmStr != null && !tmStr.equals("")){
				try {
					Date tmDate = df.parse(tmStr);
					Log.d(LOG_TAG,"getTime: Current Time " + tmStr);
					return tmDate;
				} catch (ParseException e) {
					Log.d(LOG_TAG, "getTime: Time String Parse Exception \"" + tmStr + "\"");
					e.printStackTrace();
				}
			}
		}
		Log.d(LOG_TAG, "getTime: Get Time Failed");
		return null;
	}

	/**
	 * Obtain network availability
	 * @param context
	 * @return boolean of network availability
	 */
	public boolean isNetworkAvaliable(Context context){
		if(context == null){
			Log.d(LOG_TAG, "isNetworkAvailable: Context cannot be a null value");
			return false;
		}
		
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		Log.d(LOG_TAG, "isNetworkAvailable: Network Unavailable");
		return false;
	}
	
	
	private static String getXmlResponseError(Document xmlDoc){
		Node errorNode = xmlDoc.getElementsByTagName("error").item(0);
		
		if(errorNode != null){
			Node childNode = errorNode.getFirstChild();
			if(childNode != null){
				String errorMsg = childNode.getNodeValue();
				return errorMsg;
			}
		}
		
		return null;
		
	}
	
	/**
	 * Parse a XML String and return a document object
	 * @param xmlStr
	 * @return parsed XML Document
	 */
	private static Document parseXML(String xmlStr){
		if(xmlStr == null){
			return null;
		}
		
		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlStr));
			Document doc = xmlBuilder.parse(is);
			//printElements(doc);
			return doc;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void printElements(Document doc)
	{
	   NodeList nl = doc.getElementsByTagName("route");
	   Node n;
	   
	   for (int i=0; i<nl.getLength(); i++)
	   {
		  
	      n = nl.item(i);
	      //System.out.print(n.getNodeName() + " ");
	      NodeList nl2 = n.getChildNodes();
	      System.out.print(nl2.item(1).getNodeName() + "  ");
	      System.out.println(nl2.item(1).getTextContent());

	      System.out.print(nl2.item(3).getNodeName() + "  ");
	      System.out.println(nl2.item(3).getTextContent());
	      
	      System.out.print(nl2.item(5).getNodeName() + "  ");
	      System.out.println(nl2.item(5).getTextContent());
	      //for(int j = 0; j < nl2.getLength(); j++){
	    	  //System.out.print(nl2.item(j).getNodeName() + " ");
	    	  //System.out.println(nl2.item(j).getTextContent() + " ");
	      //}
	      //System.out.println(n.getNodeValue() +"\n");
	   }
	 
	   System.out.println();
	}
	
	/**
	 * Build parameters from a Map object
	 * @param paramMap
	 * @return parameters string
	 */
	private static String buildParams(Map<String, String> paramMap, String apiKey){
		StringBuilder sb = new StringBuilder();
		sb.append("?");
		
		if(apiKey != null){
			sb.append("key");
			sb.append("=");
			sb.append(apiKey);
			
			if(paramMap != null && paramMap.size() >= 1){
				sb.append("&");
			}
		}
		
		if(paramMap != null){
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
		}
		
		return sb.toString();
		
	}
	
	/**
	 * A utility function to connect to the URI of APIs
	 * @param apiPath
	 * @param parameters
	 * @return XML Response
	 */
	private static String apiConnect(String rootUri, String apiPath, String parameters){
		URL url;
		if(rootUri == null || apiPath == null || parameters == null){
			return null;
		}
		
		StringBuilder uriSb = new StringBuilder();
		uriSb.append(rootUri);
		uriSb.append(apiPath);
		uriSb.append(parameters);
		
		try{
			Log.d(LOG_TAG, "Start connecting to the api: " + apiPath);
			url = new URL(uriSb.toString());
			HttpURLConnection apiConn = (HttpURLConnection)url.openConnection();
			apiConn.setReadTimeout(10000);
			apiConn.setConnectTimeout(10000);
			apiConn.setRequestMethod("GET");
			apiConn.setRequestProperty("User-Agent", USER_AGENT);
			apiConn.setDoInput(true);
			
			apiConn.connect();
			
			if (apiConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				  Log.d(LOG_TAG, "http response code: " + apiConn.getResponseCode());
				  return null;
			}
			
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
