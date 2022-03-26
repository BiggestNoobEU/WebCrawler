package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.*;

public class Controller {
	static final int MAX_DEPTH = 20;

	//	String url = "https://mvnrepository.com/";
//	String url = "file:///C:/Users/Waldo/Documents/Workspace/JS/CalculatorJS/Calculator.html";
//	String url = "https://mcstaging.buff.com/en_eur/";
	String url = "https://formy-project.herokuapp.com";
	Crawler crawler;
	Set<State> stateQueue;

	public Controller() {
		this.crawler = new Crawler();
		this.stateQueue = new HashSet<>();
	}

	public void start() {
		try {
			this.crawler.goTo(url);

			State rootState = new State(
				this.saveHtmlState("x_x_state.html"),
				0
			);

			this.stateQueue.add(rootState);

//			List<WebElement> htmlChildren = this.getDirectChildren(html.get(0));

//			htmlChildren.forEach((child) -> this.process(child));
		} finally {
			this.crawler.close();
		}
	}

	public void process(WebElement element) {
		do  {
			State currentState = this.stateQueue.iterator().next();

			this.goToLocalUrl(currentState.getFileName());

		} while (!this.stateQueue.isEmpty());
	}

	protected boolean verifyElement(WebElement element) {
		try {
			System.out.println(element.getTagName());
			return element.getTagName() != "a";
		} catch (StaleElementReferenceException e) {
			return false;
		}
	}

	protected void retryClick(WebElement element) {
		int attempts = 0;
		boolean result = false;

		while (attempts < 5) {
			try {
				element.click();

				result = true;

				break;
			} catch (StaleElementReferenceException e) {
				this.sleep(1000);
			} finally {
				attempts++;
			}
		}

		if (!result) {
			throw new ElementNotInteractableException("element is not clickable");
		}
	}

	protected void sleep(long ms) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException f) {
			return;
		}
	}

	protected String saveHtmlState(String fileName) {
		return this.crawler.saveState(fileName);
	}

	protected List<WebElement> getDirectChildren(WebElement parentElement) {
		String tagName = parentElement.getTagName();
		String cssSelector = String.format("%s > *", tagName);

		return this.crawler.findByCss(cssSelector);
	}

	protected void printElement(WebElement element) {
		System.out.printf("el: %s // %s%n", element, element.getTagName());
	}

	protected void goToLocalUrl(String fileName) {
		this.crawler.goTo(String.format(
			"file:///C:/Users/Waldo/Documents/Workspace/JAVA/Web%20Crawler/States/%s",
			fileName
		));
	}
}
