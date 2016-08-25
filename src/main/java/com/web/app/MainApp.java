package com.web.app;

import java.io.File;
import static spark.Spark.*;
import java.util.List;
import java.util.Map;

import com.web.scraping.SimpleScraper;

public class MainApp {

	private Map<String, List<String>> index;

	public static void main(String[] args) throws InterruptedException {
		MainApp app = new MainApp();
		SimpleScraper scraper = new SimpleScraper();

		File repository = new File("target", "repo");
		repository.mkdir();

		app.index = scraper.scrapeNhsPagesToJson(repository.getAbsolutePath());

		app.setServer(app.index);
		
		while (true) {
		}
	}

	/**
	 * @param index
	 * 
	 * create a server with a get method to request the pages concerning one conditions.
	 * Example:
	 * http://localhost:9090/request/cancer
	 */
	protected void setServer(Map<String, List<String>> index) {
		port(9090);
		get("/request/:question", (request, response) -> {
			String question = request.params(":question");
			List<String> pages = index.get(question.toLowerCase());
			
			String result = "Pages to consult:";
			for(String s : pages)
				result = result + "<br />" + s;
			return result;
		});
	}

}
