package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class Crawler {
	static final int SECONDS_TO_FIND_ELEMENT = 5;

	static WebDriver driver;

	Wait<WebDriver> wait;

	Crawler() {
		Crawler.driver = new ChromeDriver();
		this.wait = new FluentWait<WebDriver>(Crawler.driver)
			.withTimeout(Duration.ofSeconds(Crawler.SECONDS_TO_FIND_ELEMENT))
			.ignoring(NoSuchElementException.class);

		System.setProperty("webdriver.chrome.driver", "C:\\Users\\Waldo\\Downloads");
	}

	public void goTo(String url) {
		Crawler.driver.get(url);
	}

	public void close() {
		Crawler.driver.quit();
	}

	public String saveState(String fileName) {
		String folderPath = "C:\\Users\\Waldo\\Documents\\Workspace\\JAVA\\Web Crawler\\States\\";
		String content = Crawler.driver.getPageSource();
		Path path = Paths.get(folderPath + fileName);
		byte[] byteContent = content.getBytes();

		try {
			return Files
				.write(path, byteContent)
				.toString();
		} catch (IOException e1) {
			this.printError(e1.getMessage());

			return "";
		}
	}

	public String getPageHtml() {
		return Crawler.driver.getPageSource();
	}

	protected void printError(String error) {
		System.out.print("error: ");
		System.out.println(error);
	}

	public void moveToElement(WebElement $targetElement) {
		Actions action = new Actions(Crawler.driver);

		action.moveToElement($targetElement);
	}

	public List<WebElement> findByCss(String cssSelector) {
		try {
			return this.wait.until(
				ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(cssSelector))
			);
		} catch (TimeoutException e) {
			return Collections.emptyList();
		}
	}

	public List<WebElement> findById(String id) {
		return this.wait.until(
			ExpectedConditions.presenceOfAllElementsLocatedBy(By.id(id))
		);
	}

	public List<WebElement> findByTagName(String tagName) {
		return this.wait.until(
			ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName(tagName))
		);
	}

	public List<WebElement> findByXPath(String xPath) {
		return this.wait.until(
			ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xPath))
		);
	}
}
