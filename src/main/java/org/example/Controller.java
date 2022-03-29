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
        List<WebElement> elementQueue = new ArrayList<>();
        List<WebElement> linkList = new ArrayList<>();

        elementQueue.add(rootElement);

        while (!elementQueue.isEmpty()) {
            WebElement parent = elementQueue.remove(0);
            String tagName = parent.getTagName();
            List<WebElement> children = this.getDirectChildren(parent);

            if (tagName.compareTo("a") == 0) {
                linkList.add(parent);
            }

            elementQueue.addAll(children);
        }

        return linkList;
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
