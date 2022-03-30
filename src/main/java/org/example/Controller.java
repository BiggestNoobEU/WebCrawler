package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

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
            List<WebElement> linkList = this.getLinkElementList(body);
            System.out.printf("THE BIG RESULT: %s\n", linkList.size());

        } finally {
            this.crawler.close();
        }
    }

    protected List<WebElement> getLinkElementList(WebElement rootElement) {
        List<Map<String, WebElement>> elementQueue = new ArrayList<>();
        List<WebElement> linkList = new ArrayList<>();
        Map<String, Integer> tagCountMap = new HashMap<>();

        elementQueue.add(
                this.getElementMap(
                        rootElement,
                        this.getElementXPath("/html", rootElement, 0)));

        while (!elementQueue.isEmpty()) {
            Map<String, WebElement> parentElementMap = elementQueue.remove(0);
            String parentXPath = this.getElementMapXPath(parentElementMap);
            WebElement parent = parentElementMap.get(parentXPath);
            List<WebElement> children = this.getDirectChildren(parent);

            if (parent.getTagName().compareTo("a") == 0) {
                linkList.add(parent);
            }

            for (WebElement child : children ) {
                int prevTagNr = tagCountMap.getOrDefault(child.getTagName(), -1);

                tagCountMap.put(child.getTagName(), ++prevTagNr);

                String elementXPath = this.getElementXPath(parentXPath, child, prevTagNr);

                elementQueue.add(this.getElementMap(child, elementXPath));
            }
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

    protected List<WebElement> getDirectChildren(WebElement parent) {
        return parent.findElements(By.xpath("*"));
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
