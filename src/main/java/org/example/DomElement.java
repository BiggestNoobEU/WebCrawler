package org.example;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DomElement {
    String pageUrl;
    String xPath;
    WebElement element;
    Crawler crawler;

    public DomElement(WebElement el, String xp, String url, Crawler crawler) {
        this.element = el;
        this.xPath = xp;
        this.pageUrl = url;
        this.crawler = crawler;
    }

    public String getPageUrl() {
        return this.pageUrl;
    }

    public String getXPath() {
        return this.xPath;
    }

    public WebElement getElement() {
        try {
            this.element.isDisplayed();

            return this.element;
        } catch (StaleElementReferenceException e) {
            return this.reloadElement(new DomElement(this.element, this.xPath, this.pageUrl, this.crawler));
        }
    }

    public WebElement reloadElement(DomElement domElement) {
        String xPath = domElement.getXPath();

        return this.crawler.findFirstByXPath(xPath);
//        return new DomElement(reloadedElement, xPath, domElement.getPageUrl(), this.crawler);
    }
}
