package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

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
    String url = "https://mcstaging.buff.com/en_eur/";
    //    String url = "https://formy-project.herokuapp.com";
    Crawler crawler;

    public Controller() {
        this.crawler = new Crawler();
    }

    public void start() {
        try {
            this.crawler.goTo(url);
            this.crawler.windowMaximize();

            WebElement body = this.crawler.findFirstByTagName("body");
            List<Map<String, WebElement>> linkList = this.getPageLinkList(body);

            this.printLinksToFile(linkList);

            System.out.printf("THE BIG RESULT: %s\n", linkList.size());

        } finally {
            this.crawler.close();
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

    protected List<Map<String, WebElement>> getPageLinkList(WebElement rootElement) {
        List<Map<String, WebElement>> elementQueue = new ArrayList<>();
        List<Map<String, WebElement>> linkList = new ArrayList<>();
        Map<String, Integer> tagCountMap = new HashMap<>();

        elementQueue.add(
                this.getElementMap(
                        rootElement,
                        this.getElementXPath("/html", rootElement, 0)));

        while (!elementQueue.isEmpty()) {
            Map<String, WebElement> parentElementMap = elementQueue.remove(0);
            String parentXPath = this.getElementMapXPath(parentElementMap);
            WebElement parent = parentElementMap.get(parentXPath);
            List<WebElement> children = this.getDirectChildren(parentElementMap);

            if (parent.getTagName().compareTo("a") == 0) {
                linkList.add(parentElementMap);

                // assuming <a> does not have a children-sibling <a> tag
                continue;
            }

            for (WebElement child : children) {
                int prevTagNr = -1;

                if (children.size() > 1) {
                    prevTagNr = tagCountMap.getOrDefault(child.getTagName(), 0);
                }

                tagCountMap.put(child.getTagName(), ++prevTagNr);

                String elementXPath = this.getElementXPath(parentXPath, child, prevTagNr);

                elementQueue.add(this.getElementMap(child, elementXPath));
            }

            tagCountMap.clear();
        }

        return linkList;
    }

    protected String getElementMapXPath(Map<String, WebElement> elementMap) {
        return elementMap
                .keySet()
                .iterator()
                .next();
    }

    protected Map<String, WebElement> getElementMap(WebElement element, String elementXPath) {
        Map<String, WebElement> result = new HashMap<>();

        result.put(elementXPath, element);

        return result;
    }

    protected String getElementXPath(String parentXPath, WebElement targetElement, int nthTag) {
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
            String parentXPath = parentElementMap.entrySet().iterator().next().getKey();
            WebElement freshParentElement = this.crawler.findFirstByXPath(parentXPath);

            return this.getDirectChildren(this.getElementMap(freshParentElement, parentXPath));
        }
    }

    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException f) {
        }
    }

    //	protected void printElement(WebElement element) {
//		System.out.printf("el: %s // %s%n", element, element.getTagName());
//	}
//
//	protected void goToLocalUrl(String fileName) {
//		this.crawler.goTo(String.format(
//			"file:///C:/Users/Waldo/Documents/Workspace/JAVA/Web%20Crawler/States/%s",
//			fileName
//		));
//	}
}
