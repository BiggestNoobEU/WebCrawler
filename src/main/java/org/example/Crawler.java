package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Crawler {
	static final int SECONDS_TO_FIND_ELEMENT = 5;

	static WebDriver driver;
	Path statesDirectoryPath;

	Wait<WebDriver> wait;

	Crawler() {
		System.setProperty("webdriver.chrome.driver", "/home/davis/IdeaProjects/WebCrawler/chromedriver");

		Crawler.driver = new ChromeDriver();
		this.wait = new FluentWait<>(Crawler.driver)
			.withTimeout(Duration.ofSeconds(Crawler.SECONDS_TO_FIND_ELEMENT))
			.ignoring(NoSuchElementException.class);
		this.statesDirectoryPath = Paths.get("/home/davis/IdeaProjects/WebCrawler/Pages/");
	}

	public void goTo(String url) {
		Crawler.driver.get(url);
	}

	public void close() {
		Crawler.driver.quit();
	}

	public void windowMaximize() {
		Crawler.driver.manage().window().maximize();
	}

	public String saveState(String fileName) {
		Path folderPath = Paths.get("/home/davis/IdeaProjects/WebCrawler/States/");

		return this.savePageSource(Paths.get(folderPath + fileName));
	}

	public String savePage(String fileName) {
		Path folderPath = Paths.get("/home/davis/IdeaProjects/WebCrawler/Pages/");
		Path filePath = Path.of(String.format("%s/%s", folderPath, fileName));

		if(Files.exists(folderPath)) {
			return this.savePageSource(filePath);
		}

		try {
			Files.createDirectory(folderPath);
		} catch (IOException e) {
			this.printError(e.getMessage());

			return "";
		}

		return this.savePage(fileName);
	}

	protected String savePageSource(Path savePath) {
		String content = Crawler.driver.getPageSource();

		try {
			return this.writeToFile(content, savePath);
		} catch (IOException e1) {
			this.printError(e1.getMessage());

			return "";
		}
	}

	protected String writeToFile(String content, Path savePath) throws IOException {
		byte[] byteContent = content.getBytes();

		if (Files.exists(savePath)) {
			String newFilePath = this.createNewFile(savePath);

			return this.writeToFile(content, Path.of(newFilePath));
		}

		return Files
				.write(savePath, byteContent)
				.toString();
	}

	private String getFileDirectory(Path path) {
		return path.toString().replace("/" + path.getFileName(), "");
	}

	private String getFileName(Path path) {
		String[] pathArray = path.toString().split("/");

		return pathArray[pathArray.length - 1].split("([.])")[0];
	}

	private String getFileExtension(Path path) {
		String[] pathArray = path.toString().split("/");

		return pathArray[pathArray.length - 1].split("([.])")[1];
	}

	private String createNewFile(Path savePath) {
		int num = 0;
		File file = new File(savePath.toString());

		while(file.exists()) {
			String newFilePath = String.format(
					"%s/%s(%s).%s",
					this.getFileDirectory(savePath),
					this.getFileName(savePath),
					(num++),
					this.getFileExtension(savePath));
			file = new File(newFilePath);
		}

		return file.getAbsolutePath();
	}

	public void deleteStateFile(Path deletePath) {
		try {
			Files.delete(deletePath);
		} catch (IOException e) {
			this.printError(e.getMessage());
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

	public WebElement findFirstByTagName(String tagName) {
		return this.wait.until(
			ExpectedConditions.presenceOfElementLocated(By.tagName(tagName))
		);
	}

	public WebElement findFirstByXPath(String xPath) {
		return this.wait.until(
			ExpectedConditions.presenceOfElementLocated(By.xpath(xPath))
		);
	}

	public List<WebElement> findByXPath(String xPath) {
		return this.wait.until(
			ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xPath))
		);
	}

	public WebDriver getDriver() {
		return Crawler.driver;
	}
}
