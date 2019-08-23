package ncsinkbalance;

import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SinkBalance {
	
	static final Map<String, Double> SINK_MAP = new HashMap<>();
	static final Map<String, Integer> SINK_COUNTS = new HashMap<>();
	
	public static void main(String[] args) throws Exception {
		final JSONParser parser = new JSONParser();
		File config = new File("BetaConfig.json");
		if (config.isFile()) {
			JSONObject obj = (JSONObject)((JSONObject)parser.parse(new FileReader(config))).get("HeatSinks");
			
			for (Object o : obj.entrySet()) {
				Entry<?,?> entry = (Entry<?,?>)o;
				addSink((String)entry.getKey(), ((Double)((JSONObject)entry.getValue()).get("HeatPassive")).doubleValue());
			}
		}
		
		File[] files = new File("designs").listFiles();
		final List<String> sinks = new ArrayList<>(SINK_MAP.keySet());
		for (File file : files) {
			if (file.isFile()) {
				String ext = "", filename = file.getName();
				int i = filename.lastIndexOf('.');
				if (i > 0) ext = filename.substring(i + 1);
				
				if (ext.equalsIgnoreCase("json")) {
					JSONObject obj = (JSONObject)((JSONObject)parser.parse(new FileReader(file))).get("HeatSinks");
					
					for (String sink : sinks) {
						JSONArray arr = (JSONArray)obj.get(sink);
						if (arr == null) continue;
						SINK_COUNTS.put(sink, SINK_COUNTS.get(sink) + arr.size());
					}
				}
			}
		}
		
		for (String sink : sinks) {
			SINK_MAP.put(sink, SINK_MAP.get(sink)*SINK_MAP.get(sink)*SINK_COUNTS.get(sink));
		}
		
		List<Entry<String, Double>> list = new ArrayList<>(SINK_MAP.entrySet());
		list.sort(Entry.comparingByValue());
		Collections.reverse(list);
		final double mult = 100D/list.get(0).getValue();
		
		Scanner scanner = new Scanner(System.in);
		
		for (Entry<String, Double> entry : list) {
			System.out.println(entry.getKey() + ": " + decimalPlaces(mult*entry.getValue(), 2));
		}
		
		scanner.nextLine();
		scanner.close();
	}
	
	static void addSink(String name, double cooling_rate) {
		SINK_MAP.put(name, cooling_rate);
		SINK_COUNTS.put(name, 0);
	}
	
	static String decimalPlaces(double number, int places) {
		if (number == (int)number) {
			return (int)number + "";
		}
		char[] arr = new char[Math.max(1, places)];
		Arrays.fill(arr, '#');
		DecimalFormat df = new DecimalFormat("0." + new String(arr));
		return df.format(number);
	}
}
