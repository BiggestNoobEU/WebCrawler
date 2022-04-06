package org.example;

import org.openqa.selenium.*;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Controller {
    static final int MAX_DEPTH = 20;

    //	String url = "https://mvnrepository.com/";
//	String url = "file:///C:/Users/Waldo/Documents/Workspace/JS/CalculatorJS/Calculator.html";
//    String url = "https://mcstaging.buff.com/en_eur/";
    String url = "https://formy-project.herokuapp.com";
    Crawler crawler;

    public Controller() {
        this.crawler = new Crawler();
    }

    public void start() {
        String rootUrl = this.url;

//        List<Map<String, WebElement>> linkList = this.processPage(rootUrl);
        Set<String> linkList = new HashSet<>(this.processPage(rootUrl));
        Set<String> resultLnkList = new HashSet<>();

        try {
            File file = new File("/home/davis/IdeaProjects/WebCrawler/VariablePrint/linkListXPath.txt");
            PrintStream stream = new PrintStream(file);

            System.setOut(stream);
        } catch (FileNotFoundException e) {
        }


        while (linkList.size() != resultLnkList.size()) {
            String currentLink = linkList.iterator().next();

            if (resultLnkList.contains(currentLink)) {
                linkList.remove(currentLink);

                continue;
            }

            Set<String> newLinkList = new HashSet<>(this.processPage(currentLink));

            linkList.addAll(newLinkList);
            resultLnkList.add(currentLink);
            linkList.remove(currentLink);

            Set<String> linksCrawled = new HashSet<>(resultLnkList);
            linksCrawled.retainAll(linkList);
            linkList.removeAll(linksCrawled);
        }

        System.out.println(resultLnkList);
        this.crawler.close();
    }

    //    public List<Map<String, WebElement>> processPage(String url) {
    public List<String> processPage(String url) {
        try {
            this.crawler.goTo(url);
            this.crawler.windowMaximize();

            WebElement body = this.crawler.findFirstByTagName("body");
            List<Map<String, WebElement>> linkList = this.getPageLinkList(body);
            List<String> hrefList = new ArrayList<>();

            for (Map<String, WebElement> elementMap : linkList) {
                String linkXPath = this.getElementMapXPath(elementMap);
                WebElement linkElement = this.getElementMapElement(elementMap);
                String href = "";

                try {
                    href = linkElement.getAttribute("href");
                } catch (Exception e) {
                    Map<String, WebElement> reloadedElementMap = this.reloadElement(linkXPath);
                    href = this.getElementMapElement(reloadedElementMap).getAttribute("href");
                }

                Boolean isFullHref = href.contains("http://") || href.contains("https://");
                String resultHref = isFullHref ? href : this.url.concat(href);

                hrefList.add(resultHref);
            }

            this.printLinksToFile(linkList);
//            this.crawler.close();

//            return linkList;
            return hrefList;
        } catch (Exception e) {
//            this.crawler.close();

            return new ArrayList<>();
        }
    }

    private void printLinksToFile(List<Map<String, WebElement>> links) {
        try {
            File file = new File("/home/davis/IdeaProjects/WebCrawler/VariablePrint/linkListXPath.txt");
            PrintStream stream = new PrintStream(file);

            System.setOut(stream);
        } catch (Exception e) {
        }

        Consumer<String> xPathConsumer = System.out::println;

        links.forEach(elementMap -> xPathConsumer.accept(this.getElementMapXPath(elementMap)));
    }

//    private void printLinksToFileFromSet(String link) {
//        try {
//            File file = new File("/home/davis/IdeaProjects/WebCrawler/VariablePrint/linkListHrefs.txt");
//            PrintStream stream = new PrintStream(file);
//
//            System.setOut(stream);
//        } catch (Exception e) {
//        }
//
//        System.out.println(link);
//    }

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

            if (this.isLink(parentElementMap)) {
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

    protected boolean isLink(Map<String, WebElement> elementMap) {
        try {
            return this.getElementMapElement(elementMap)
                    .getTagName()
                    .compareTo("a") == 0;
        } catch (Exception e) {
            String elementXPath = this.getElementMapXPath(elementMap);

            try {
                return this.isLink(this.reloadElement(elementXPath));
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
