package it.rockeat.ui;

import it.rockeat.RockCrawler;
import it.rockeat.RockEater;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerCli {

	public static void main(String[] args) throws Exception {
	   String crawlStorageFolder = "/home/lorenzo/RockEat/tmp";
       int numberOfCrawlers = 1;
       CrawlConfig config = new CrawlConfig();
       config.setCrawlStorageFolder(crawlStorageFolder);
       config.setUserAgentString(RockEater.USER_AGENT_STRING);
       config.setPolitenessDelay(10);
       config.setMaxDepthOfCrawling(7);
       config.setMaxPagesToFetch(10000);
       config.setResumableCrawling(false);
       PageFetcher pageFetcher = new PageFetcher(config);
       RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
       RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
       CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
       controller.addSeed("http://www.rockit.it/");
       controller.addSeed("http://www.rockit.it/web/audio.php");
       controller.addSeed("http://www.rockit.it/web/band.php");
       controller.start(RockCrawler.class, numberOfCrawlers);
	}

}
