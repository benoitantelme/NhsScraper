package com.web.data;

public class ScrapedCondition
{
	public ScrapedCondition(String url, String content, String title) {
		super();
		this.url = url;
		this.content = content;
		this.title = title;
	}

	protected String url;
	protected String content;
	protected String title;

	public String getUrl() {
		return url;
	}

	public String getContent() {
		return content;
	}

	public String getTitle() {
		return title;
	}

}
