package com.web.scraping;

import static org.junit.Assert.*;

import org.junit.Test;

import com.web.data.ScrapedCondition;

public class SimpleScraperTest
{
	private static final String MAIN_ADDRESS = "http://www.nhs.uk/";
	
	@Test
	public void testSimpleConditionScraping() throws Exception {
		SimpleScraper scraper = new SimpleScraper();

		ScrapedCondition condition = scraper.scrapACondition("Conditions/Arthritis");

		assertNotNull(condition);
		assertNotNull(condition.getContent());
		assertEquals(MAIN_ADDRESS + "Conditions/Arthritis" + "/Pages/Introduction.aspx", condition.getUrl());
		assertEquals("Arthritis", condition.getTitle());
	}


}
