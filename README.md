NhsScraper
=============

This is a simple project to get data from a website, save it as json and render it later using a rest API

Goals
-------

* Writing a service that scrape the nhsChoice website (http://www.nhs.uk/Conditions/Pages/hub.aspx) and cache the condition pages and their sub-pages content in a json file (contain at least url, page content and title).

* As a second part, having a rest enabled service that load this cache and provide an end point to search it and point user to the most appropriate pages for requests like “what are the symptoms of cancer?” “treatments for headaches”

Tech stack
-------

Junit, Jsoup, Gson, Spark
