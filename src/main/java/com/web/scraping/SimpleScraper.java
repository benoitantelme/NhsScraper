package com.web.scraping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

import com.web.data.ScrapedCondition;
import com.web.json.JsonUtils;

public class SimpleScraper {
	private static final String HREF = "href";
	private static final String PAGES = "pages";
	private static final String CONDITIONS = "conditions";
	private static final String AND = "&";
	private static final String[] indexes = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
			"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0-9" };

	private final Map<String, ScrapedCondition> conditionsMap = new ConcurrentHashMap<String, ScrapedCondition>();
	private final Set<String> scannedPartialUrl = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private final Set<String> failedPartialUrl = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private final ExecutorService pool = Executors.newFixedThreadPool(8);

	public Map<String, List<String>> scrapeNhsPagesToJson(String path) throws InterruptedException {
		Map<String, List<String>> index = new HashMap<String, List<String>>();
		Set<String> pagesToScrap = new HashSet<String>();

		for (int i = 0; i < indexes.length; i++) {
			pagesToScrap.addAll(
					getConditionsUrlsInAPage("http://www.nhs.uk/Conditions/Pages/BodyMap.aspx?Index=" + indexes[i]));
		}

		System.out.println("Number of pages to scrap = " + pagesToScrap.size());

		tryScrapingAllConditions(pagesToScrap);
		System.out.println();

		for (String key : conditionsMap.keySet()) {
			String cleanName = key.replaceAll("[\\\\/:*?\"<>|]", "");
			ScrapedCondition condition = conditionsMap.get(key);

			System.out.println("writing " + key);
			JsonUtils.writeObjectToJsonFile(condition, path, cleanName);

			updateIndex(index, cleanName, condition);
		}

		System.out.println();
		pool.shutdownNow();
		pool.awaitTermination(100, TimeUnit.MILLISECONDS);

		return index;
	}

	protected ScrapedCondition scrapACondition(String partialUrl) throws IOException {
		Document conditionPage = Jsoup.connect("http://www.nhs.uk/" + partialUrl).get();

		Elements titleElement = conditionPage.select("h1");
		String title = titleElement.html();
		if (title.contains(AND))
			title = title.split(AND)[0];

		Elements conditionPageContent = conditionPage.select("div.main-content.healthaz-content.clear");

		ScrapedCondition condition = new ScrapedCondition(conditionPage.baseUri(), conditionPageContent.outerHtml(),
				title);
		return condition;
	}

	/**
	 * @param index
	 * @param cleanName
	 * @param condition
	 * 
	 *            update the index, keeping only words with more than 3 letters
	 *            and put to lower case
	 */
	private void updateIndex(final Map<String, List<String>> index, String cleanName, ScrapedCondition condition) {
		String[] splittedName = cleanName.split(" ");
		for (String part : splittedName) {
			if (part.length() > 3) {
				String lowerCaseName = part.toLowerCase();
				String url = condition.getUrl();
				if (index.containsKey(lowerCaseName)) {
					index.get(lowerCaseName).add(url);
				} else {
					List<String> newList = new ArrayList<String>();
					newList.add(url);
					index.put(lowerCaseName, newList);
				}
			}
		}
	}

	/**
	 * @param address
	 * @return the condition pages urls, going through the nhs website
	 */
	private Set<String> getConditionsUrlsInAPage(String address) {
		Set<String> validAdresses = new HashSet<String>();
		System.out.println("-- Building conditions list for address: " + address + " --");
		Document conditionsPage = null;
		try {
			conditionsPage = Jsoup.connect(address).get();
		} catch (IOException e1) {
			System.out.println("Issue while connecting to  : " + address + " with jsoup");
			e1.printStackTrace();
		}

		Elements links = conditionsPage.select("a[href]");
		for (Element link : links) {
			final String partialUrl = link.attr(HREF);
			String lowerCaseUrl = partialUrl.toLowerCase();

			if (lowerCaseUrl.contains(CONDITIONS) && !lowerCaseUrl.contains(PAGES)) {
				if (!scannedPartialUrl.contains(partialUrl)) {
					validAdresses.add(partialUrl);
				}

			}

		}

		return validAdresses;
	}

	/**
	 * @param pagesToScrap
	 * 
	 *            multiple tries to scrap everything
	 */
	private void tryScrapingAllConditions(Set<String> pagesToScrap) {
		int tries = 5;
		scrapAllPages(pagesToScrap);

		System.out.println("Scraping of NHS conditions done.  Number of conditions scraped = " + conditionsMap.size());
		System.out.println("Number of pages missing = " + failedPartialUrl.size());

		while (!failedPartialUrl.isEmpty() && tries > 0) {
			Set<String> toRetry = new HashSet<>(failedPartialUrl);
			failedPartialUrl.clear();

			scrapAllPages(toRetry);

			tries--;
		}

		System.out.println("Number of conditions scraped after retries = " + conditionsMap.size());
		System.out.println("Number of pages missing = " + failedPartialUrl.size());
	}

	/**
	 * @param addresses
	 * 
	 *            scraping the urls with a pool of callables
	 */
	private void scrapAllPages(Set<String> addresses) {
		List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>();

		for (final String address : addresses) {
			callables.add(new Callable<Boolean>() {
				public Boolean call() {
					Boolean result = true;
					System.out.println("Connecting to link : " + address);
					scannedPartialUrl.add(address);

					ScrapedCondition condition = null;
					try {
						condition = scrapACondition(address);
					} catch (IOException e) {
						System.out.println("Issue while scrapping condition with partial url: " + address);
						e.printStackTrace();
						failedPartialUrl.add(address);
						result = false;
					}

					if (condition != null) {
						conditionsMap.put(condition.getTitle(), condition);
					} else {
						System.out
								.println("Condition for partial url " + address + " could not be scraped successfully");
						failedPartialUrl.add(address);
						result = false;
					}
					return result;
				}
			});
		}

		try {
			pool.invokeAll(callables);
		} catch (InterruptedException e) {
			System.out.println("Issue while scrapping.");
			e.printStackTrace();
		}
	}

}
