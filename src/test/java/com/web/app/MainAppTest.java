package com.web.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Test;

import spark.Spark;

import static org.junit.Assert.*;

public class MainAppTest {

	@AfterClass
	public static void afterClass() {
		Spark.stop();
	}

	@Test
	public void testMainApp() throws Exception {
		Map<String, List<String>> index = new HashMap<>();
		index.put("test", Arrays.asList(new String[] { "a", "b", "c" }));

		MainApp app = new MainApp();
		app.setServer(index);

		String response = doAGet();
		assertEquals("Pages to consult:<br />a<br />b<br />c", response);
	}

	private String doAGet() throws Exception {
		StringBuilder response = new StringBuilder();
		URL obj = new URL("http://localhost:9090/request/test");
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} else {
			response.append("GET request not worked");
		}

		return response.toString();
	}

}
