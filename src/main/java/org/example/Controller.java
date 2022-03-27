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
//	String url = "https://mcstaging.buff.com/en_eur/";
    String url = "https://formy-project.herokuapp.com";
    Crawler crawler;
    List<WebElement> elementQueue;
    List<WebElement> linkList;

    public Controller() {
        this.crawler = new Crawler();
        this.elementQueue = new ArrayList<>();
        this.linkList = new ArrayList<>();
    }

    public void start() {
        try {
            this.crawler.goTo(url);

            WebElement body = this.crawler.findFirstByTagName("body");
            List<WebElement> linkList = this.getLinkElementList(body);
            System.out.printf("THE BIG RESULT: %s", linkList.size());

            linkList.get(6).click(); // just testing how clicking a link works
            this.sleep(5000);
        } finally {
            this.crawler.close();
        }
    }

    protected String saveHtmlState(String fileName) {
        return this.crawler.saveState(fileName);
    }

    /**
     * Searched for <a> elements in breadth-first manner
     *
     * @param rootElement
     * @return
     */
    protected List<WebElement> getLinkElementList(WebElement rootElement) {
        this.elementQueue.add(rootElement);

        while (!this.elementQueue.isEmpty()) {
            WebElement parent = this.elementQueue.remove(0);

            if (parent.getTagName().compareTo("a") == 0) {
                this.linkList.add(parent);
            }

            List<WebElement> children = this.getDirectChildren(parent);

            this.elementQueue.addAll(children);
        }

        return this.linkList;
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

//	public void process(WebElement element) {
//		do  {
//			State currentState = this.stateQueue.iterator().next();
//
//			this.goToLocalUrl(currentState.getFileName());
//
//		} while (!this.stateQueue.isEmpty());
//	}
//
//	protected boolean verifyElement(WebElement element) {
//		try {
//			System.out.println(element.getTagName());
//			return element.getTagName() != "a";
//		} catch (StaleElementReferenceException e) {
//			return false;
//		}
//	}
//
//	protected void retryClick(WebElement element) {
//		int attempts = 0;
//		boolean result = false;
//
//		while (attempts < 5) {
//			try {
//				element.click();
//
//				result = true;
//
//				break;
//			} catch (StaleElementReferenceException e) {
//				this.sleep(1000);
//			} finally {
//				attempts++;
//			}
//		}
//
//		if (!result) {
//			throw new ElementNotInteractableException("element is not clickable");
//		}
//	}
//
//
//
//	protected List<WebElement> getDirectChildren(WebElement parentElement) {
//		String tagName = parentElement.getTagName();
//		String cssSelector = String.format("%s > *", tagName);
//
//		return this.crawler.findByCss(cssSelector);
//	}
//
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
