package org.example;

import org.openqa.selenium.*;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Controller {
    static final int MAX_DEPTH = 20;

//    String url = "https://mcstaging.buff.com/en_eur/";
    String url = "https://formy-project.herokuapp.com";
//    String url = "https://mcprod.buff.com/";
    Crawler crawler;

    public Controller() {
        this.crawler = new Crawler();
    }

    public void start() {

    }

    public void startBreadthFirstSearch() {
        String rootUrl = this.url;

        Set<String> linkList = new HashSet<>(this.processPage(rootUrl));
        Set<String> resultLnkList = new HashSet<>();

        while (!linkList.isEmpty()) {
            String currentLink = linkList.iterator().next();

            if (!currentLink.contains(rootUrl)) {
                // if not current domain, then do not crawl
                resultLnkList.add(currentLink);

            }

            if (resultLnkList.contains(currentLink)) {
                // if already crawled, skip current link
                linkList.remove(currentLink);

                continue;
            }

            Set<String> newlyFoundLinkList = new HashSet<>(this.processPage(currentLink));

            linkList.addAll(newlyFoundLinkList);
            resultLnkList.add(currentLink);

            Set<String> linksAlreadyCrawled = new HashSet<>(resultLnkList);

            linksAlreadyCrawled.retainAll(linkList);
            linkList.removeAll(linksAlreadyCrawled);
            this.writeToFile(currentLink, "links.txt");
        }

        this.crawler.close();
    }

    public List<String> processPage(String url) {
        try {
            this.crawler.goTo(url);
            this.crawler.windowMaximize();

            WebElement body = this.crawler.findFirstByTagName("body");
            List<Map<String, WebElement>> linkList = this.getPageLinkList(body);
            List<String> urlList = new ArrayList<>();

            for (Map<String, WebElement> elementMap : linkList) {
                urlList.add(this.getHrefUrl(elementMap));
            }

            return urlList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    protected String getHrefUrl(Map<String, WebElement> elementMap) {
        String href;

        try {
            href = this.getElementMapElement(elementMap).getAttribute("href");
        } catch (StaleElementReferenceException e) {
            Map<String, WebElement> reloadedElementMap = this.reloadElement(this.getElementMapXPath(elementMap));
            href = this.getElementMapElement(reloadedElementMap).getAttribute("href");
        }

        boolean isFullHref = href.contains("http://") || href.contains("https://");

        return isFullHref ? href : this.url.concat(href);
    }

    private void writeToFile(String text, String filename) {
        Path filePath = Paths.get(String.format("/home/davis/IdeaProjects/WebCrawler/VariablePrint/%s", filename));
        String textWithNewLine = String.format("%s\n", text);

        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            Files.write(
                    filePath,
                    textWithNewLine.getBytes(),
                    StandardOpenOption.APPEND
            );
        } catch (Exception e) {
        }
    }

    protected List<Map<String, WebElement>> getPageLinkList(WebElement rootElement) {
        List<Map<String, WebElement>> elementQueue = new ArrayList<>();
        List<Map<String, WebElement>> linkList = new ArrayList<>();
        Map<String, Integer> tagCountMap = new HashMap<>();

        elementQueue.add(
                this.buildElementMap(
                        rootElement,
                        this.buildElementXPath("/html", rootElement, 0)));

        while (!elementQueue.isEmpty()) {
            Map<String, WebElement> parentElementMap = elementQueue.remove(0);
            String parentXPath = this.getElementMapXPath(parentElementMap);
            List<WebElement> children = this.getDirectChildren(parentElementMap);

            if (this.isClickable(parentElementMap)) {
                linkList.add(parentElementMap);

                // assuming <a> does not have a children-sibling <a> tag
                continue;
            }

            for (WebElement child : children) {
                int prevSameTagNr = -1;

                if (children.size() > 1) {
                    prevSameTagNr = tagCountMap.getOrDefault(child.getTagName(), 0);
                }

                String elementXPath = this.buildElementXPath(parentXPath, child, prevSameTagNr);

                tagCountMap.put(child.getTagName(), ++prevSameTagNr);
                elementQueue.add(this.buildElementMap(child, elementXPath));
            }

            tagCountMap.clear();
        }

        return linkList;
    }

    protected boolean isClickable(Map<String, WebElement> elementMap) {
        return this.isElementOfType(elementMap, "a")
                || this.isElementOfType(elementMap, "button")
                || this.getElementMapElement(elementMap)
                    .getCssValue("cursor")
                    .compareTo("pointer") == 0;
    }

    protected boolean isElementOfType(Map<String, WebElement> elementMap, String type) {
        try {
            return this.getElementMapElement(elementMap)
                    .getTagName()
                    .compareTo(type) == 0;
        } catch (StaleElementReferenceException e) {
            String elementXPath = this.getElementMapXPath(elementMap);

            try {
                return this.isElementOfType(this.reloadElement(elementXPath), type);
            } catch (TimeoutException e1) {
                return false;
            }
        }
    }

    protected Map<String, WebElement> reloadElement(String xPath) throws TimeoutException {
        WebElement reloadedElement = this.crawler.findFirstByXPath(xPath);

        return this.buildElementMap(reloadedElement, xPath);
    }

    protected String getElementMapXPath(Map<String, WebElement> elementMap) {
        return elementMap
                .keySet()
                .iterator()
                .next();
    }

    protected WebElement getElementMapElement(Map<String, WebElement> elementMap) {
        return elementMap
                .values()
                .iterator()
                .next();
    }

    protected Map<String, WebElement> buildElementMap(WebElement element, String elementXPath) {
        Map<String, WebElement> result = new HashMap<>();

        result.put(elementXPath, element);

        return result;
    }

    protected String buildElementXPath(String parentXPath, WebElement targetElement, int nthTag) {
        String baseString = "%s/%s";

        if (nthTag > 0) {
            baseString = "%s/%s" + String.format("[%s]", nthTag);
        }

        return String.format(baseString, parentXPath, targetElement.getTagName());
    }

    protected List<WebElement> getDirectChildren(Map<String, WebElement> parentElementMap) {
        WebElement parentElement = parentElementMap.entrySet().iterator().next().getValue();

        try {
            return parentElement.findElements(By.xpath("*"));
        } catch (StaleElementReferenceException e) {
            String parentXPath = this.getElementMapXPath(parentElementMap);

            return this.getDirectChildren(this.reloadElement(parentXPath));
        }
    }

    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException f) {
        }
    }
}
