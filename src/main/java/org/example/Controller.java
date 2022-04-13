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

            Set<String> newlyFoundLinkSet = this.processPage(currentLink);

            linkList.addAll(newlyFoundLinkSet);
            resultLnkList.add(currentLink);

            Set<String> linksAlreadyCrawled = new HashSet<>(resultLnkList);

            linksAlreadyCrawled.retainAll(linkList);
            linkList.removeAll(linksAlreadyCrawled);
            this.writeToFile(currentLink, "links.txt");
        }

        this.crawler.close();
    }

    public Set<String> processPage(String url) {
        try {
            this.crawler.goTo(url);
            this.crawler.windowMaximize();

            List<DomElement> clickableElementList = this.getPageClickableElementList(url);
            Set<String> urlSet = new HashSet<>();

            for (DomElement domElement : clickableElementList) {
                urlSet.add(this.getHrefUrl(domElement));
            }

            return urlSet;
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

//    protected boolean isNewPageState(DomElement rootElement, String currentUrl) {
//        boolean isTheSamePage = this.crawler
//                .getDriver()
//                .getCurrentUrl()
//                .compareTo(currentUrl) == 0;
//
//        if (isTheSamePage) {
//
//        }
//
//        try {
//            this.getElementMapElement(elementMap)
//                    .click();
//        } catch (StaleElementReferenceException e) {
//            this.isNewPageState(this.reloadElement(this.getElementMapXPath(elementMap)), currentUrl);
//        }
//
//        return false;
//    }

    protected String getHrefUrl(DomElement domElement) {
        String href;

        try {
            href = domElement.getElement().getAttribute("href");
        } catch (StaleElementReferenceException e) {
            try {
                DomElement reloadedElement = this.reloadElement(domElement);
                href = reloadedElement.getElement().getAttribute("href");
            } catch (TimeoutException e1) {
                href = "";
            }
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

    protected List<DomElement> getPageClickableElementList(String url) {
        WebElement body = this.crawler.findFirstByTagName("body");
        DomElement rootElement = new DomElement(
                body,
                this.buildElementXPath("/html", body, 0),
                url);
        List<DomElement> elementQueue = new ArrayList<>();
        List<DomElement> linkList = new ArrayList<>();
        Map<String, Integer> tagCountMap = new HashMap<>();

        elementQueue.add(rootElement);

        while (!elementQueue.isEmpty()) {
            DomElement parentElement = elementQueue.remove(0);
            List<WebElement> children = this.getDirectChildren(parentElement);

            if (this.isClickable(parentElement)) {
                linkList.add(parentElement);

                /* assuming <a>, <button> and element that has cursor: pointer css does not
                  have a children that should be clickable as well */
                continue;
            }

            for (WebElement child : children) {
                int prevSameTagNr = -1;

                if (children.size() > 1) {
                    prevSameTagNr = tagCountMap.getOrDefault(child.getTagName(), 0);
                }

                String childXPath = this.buildElementXPath(parentElement.getXPath(), child, prevSameTagNr);

                tagCountMap.put(child.getTagName(), ++prevSameTagNr);
                elementQueue.add(new DomElement(child, childXPath, rootElement.getPageUrl()));
            }

            tagCountMap.clear();
        }

        return linkList;
    }

    protected boolean isClickable(DomElement domElement) {
        return this.isElementOfType(domElement, "a")
                || this.isElementOfType(domElement, "button")
                || domElement
                    .getElement()
                    .getCssValue("cursor")
                    .compareTo("pointer") == 0;
    }

    protected boolean isElementOfType(DomElement domElement, String type) {
        try {
            return domElement.getElement()
                    .getTagName()
                    .compareTo(type) == 0;
        } catch (StaleElementReferenceException e) {
            try {
                return this.isElementOfType(this.reloadElement(domElement), type);
            } catch (TimeoutException e1) {
                return false;
            }
        }
    }

    protected DomElement reloadElement(DomElement parentElement) throws TimeoutException {
        String xPath = parentElement.getXPath();
        WebElement reloadedElement = this.crawler.findFirstByXPath(xPath);

        return new DomElement(reloadedElement, xPath, parentElement.getPageUrl());
    }

//    protected String getElementMapXPath(Map<String, WebElement> elementMap) {
//        return elementMap
//                .keySet()
//                .iterator()
//                .next();
//    }
//
//    protected WebElement getElementMapElement(Map<String, WebElement> elementMap) {
//        return elementMap
//                .values()
//                .iterator()
//                .next();
//    }

//    protected Map<String, WebElement> buildElementMap(WebElement element, String elementXPath) {
//        Map<String, WebElement> result = new HashMap<>();
//
//        result.put(elementXPath, element);
//
//        return result;
//    }

    protected String buildElementXPath(String parentXPath, WebElement targetElement, int nthTag) {
        String baseString = "%s/%s";

        if (nthTag > 0) {
            baseString = "%s/%s" + String.format("[%s]", nthTag);
        }

        return String.format(baseString, parentXPath, targetElement.getTagName());
    }

    protected List<WebElement> getDirectChildren(DomElement parentElement) {
        try {
            return parentElement
                    .getElement()
                    .findElements(By.xpath("*"));
        } catch (StaleElementReferenceException e) {
            try {
                return this.getDirectChildren(this.reloadElement(parentElement));
            } catch (TimeoutException e1) {
                return new ArrayList<>();
            }
        }
    }

    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException f) {
        }
    }
}
