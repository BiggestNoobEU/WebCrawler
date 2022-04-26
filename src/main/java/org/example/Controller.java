package org.example;

import org.openqa.selenium.*;

import javax.xml.xpath.XPath;
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
    int transitionsToNewState = 0;

    public Controller() {
        this.crawler = new Crawler();
    }

    public void start() {

    }

    public void startBreadthFirstSearch() {
        String rootUrl = this.url;

        this.crawler.goTo(rootUrl);
        this.crawler.windowMaximize();

        List<DomElement> allClickableElements = this.getPageClickableElementList(rootUrl);
        List<DomElement> links = new ArrayList<>();
        List<DomElement> buttons = new ArrayList<>();

        for (DomElement domElement : allClickableElements) {
            if (this.isLink(domElement)) {
                links.add(domElement);

                continue;
            }

            buttons.add(domElement);
        }

        this.executeClickOnElements(allClickableElements);
//        while (!linkList.isEmpty()) {
//            String currentLink = linkList.iterator().next();
//
//            if (!currentLink.contains(rootUrl)) {
//                // if not current domain, then do not crawl
//                resultLnkList.add(currentLink);
//
//            }
//
//            if (resultLnkList.contains(currentLink)) {
//                // if already crawled, skip current link
//                linkList.remove(currentLink);
//
//                continue;
//            }
//
////            Set<String> newlyFoundLinkSet = this.processPage(currentLink);
//            List<DomElement> clickableElementList = this.getPageClickableElementList(url);
//
//            linkList.addAll(newlyFoundLinkSet);
//            resultLnkList.add(currentLink);
//
//            Set<String> linksAlreadyCrawled = new HashSet<>(resultLnkList);
//
//            linksAlreadyCrawled.retainAll(linkList);
//            linkList.removeAll(linksAlreadyCrawled);
//            this.writeToFile(currentLink, "links.txt");
//        }

        System.out.println(this.transitionsToNewState);
        this.crawler.close();
    }

    protected void executeClickOnElements(List<DomElement> elements) {
        elements.forEach(this::clickElement);
    }

    protected void clickElement(DomElement element) {
        String urlBeforeClick = element.getPageUrl();
        String stateFileName = element
                .getXPath()
                .replaceAll("([/.:-])", "");
        String beforeFileName = String.format("BC%s.html", stateFileName);
        String afterFileName = String.format("AC%s.html", stateFileName);

        this.crawler.savePage(beforeFileName);

        boolean isClickExecuted = this.executeClick(element);

        if (!isClickExecuted) {
            this.crawler.deleteStateFile(beforeFileName);
            this.crawler.goTo(urlBeforeClick);

            return;
        }

        if (this.isUrlChanged(element)) {
            this.crawler.deleteStateFile(beforeFileName);
            this.updateStateCount(1);
            this.crawler.goTo(urlBeforeClick);

            return;
        }

        this.crawler.savePage(afterFileName);

        if(this.isDomChanged()) {
            this.updateStateCount(1);
        }
    }

    protected boolean isDomChanged() {

        return false;
    }

    protected boolean isUrlChanged(DomElement element) {
        return this.crawler
                .getDriver()
                .getCurrentUrl()
                .compareTo(String.format("%s/", element.getPageUrl())) != 0;
    }

    protected boolean executeClick(DomElement domElement) {
        try {
            domElement
                    .getElement()
                    .click();

            return true;
        } catch (StaleElementReferenceException e) {
            return this.executeClick(this.reloadElement(domElement));
        } catch (Exception e1) {
            return false;
        }
    }

    protected void updateStateCount(int count) {
        int currentCount = this.transitionsToNewState;

        this.transitionsToNewState = count + currentCount;
    }

    protected boolean isLink(DomElement domElement) {
        return this.getHrefUrl(domElement).length() > 0;
    }

    public List<DomElement> processPage(String url) {
        try {
            this.crawler.goTo(url);
            this.crawler.windowMaximize();

            List<DomElement> clickableElementList = this.getPageClickableElementList(url);
//            List<DomElement>  = new ArrayList<>();

//            for (DomElement domElement : clickableElementList) {
//                String href = this.getHrefUrl(domElement);
//
//                if(href.length() > 0) {
//                    // it is <a> and 100% invokes redirect on click
//                    urlSet.add(href);
//
//                    continue;
//                }
//
//                String urlAfterClick = this.getUrlAfterClick(domElement);
//
//                if (urlAfterClick.compareTo(domElement.getPageUrl()) != 0) {
//                    // url has changed, so redirect has happened
//                    urlSet.add(urlAfterClick);
//
//                    continue;
//                }
//            }

            return clickableElementList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

//    protected String getUrlAfterClick(DomElement domElement) {
//        String elementPageUrl = domElement.getPageUrl();
//
//        try {
//            domElement
//                    .getElement()
//                    .click();
//        } catch (StaleElementReferenceException e) {
//            try {
//                this.reloadElement(domElement)
//                        .getElement()
//                        .click();
//            } catch (TimeoutException e1) {
//                return elementPageUrl;
//            }
//        } catch (ElementNotInteractableException e2) {
//            return elementPageUrl;
//        }
//
//        String currentUrl = this.crawler
//                .getDriver()
//                .getCurrentUrl();
//
//        if (currentUrl.compareTo(elementPageUrl) == 0) {
//            // the element does not invoke redirect to other page
//            // it might have revealed a new link by opening a popup or something else
//
//            this.crawler.savePage(String.format("%s.html", elementPageUrl.replaceAll("\\/", "_")));
//
//            return elementPageUrl;
//        }
//
//        // go back to the page where redirect element is located
//        this.crawler.goTo(elementPageUrl);
//
//        return currentUrl;
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

        if (href == null) {
            return "";
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
        DomElement rootElement = new DomElement(body, this.buildElementXPath("/html", body, 0), url);
        List<DomElement> elementQueue = new ArrayList<>();
        List<DomElement> clickableElements = new ArrayList<>();
        Map<String, Integer> tagCountMap = new HashMap<>();
        elementQueue.add(rootElement);

        while (!elementQueue.isEmpty()) {
            DomElement parentElement = elementQueue.remove(0);
            boolean isClickable = this.isClickable(parentElement);

            if (this.isClickable(parentElement)) {
                clickableElements.add(parentElement);
                /* assuming <a>, <button> and element that has cursor: pointer css does not
                  have a children that should be clickable as well */
                continue;
            }

            if (!isClickable && parentElement.getElement().getTagName().compareTo("button") == 0) {
                continue;
            }

            for (WebElement child : this.getDirectChildren(parentElement)) {
                int prevSameTagNr = -1;

                if (this.getSameTagSiblings(parentElement, child.getTagName()).size() > 1) {
                    prevSameTagNr = tagCountMap.getOrDefault(child.getTagName(), 1);
                }

                String childXPath = this.buildElementXPath(parentElement.getXPath(), child, prevSameTagNr);
                tagCountMap.put(child.getTagName(), ++prevSameTagNr);
                elementQueue.add(new DomElement(child, childXPath, rootElement.getPageUrl()));
            }

            tagCountMap.clear();
        }

        return clickableElements;
    }

    protected List<WebElement> getSameTagSiblings(DomElement parent, String tag) {
        return parent
                .getElement()
                .findElements(
                        By.xpath(String.format("%s/%s", parent.getXPath(), tag)));
    }

    protected boolean isClickable(DomElement domElement) {
        boolean isVisible = domElement
                .getElement()
                .getCssValue("display")
                .compareTo("none") != 0;

        if (!isVisible) {
            return false;
        }

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
