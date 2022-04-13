package org.example;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

public class DomElement {
    String pageUrl;
    String xPath;
    WebElement element;

    public DomElement(WebElement el, String xp, String url) {
        this.element = el;
        this.xPath = xp;
        this.pageUrl = url;
    }

    public String getPageUrl() {
        return this.pageUrl;
    }

    public String getXPath() {
        return this.xPath;
    }

    public WebElement getElement() {
        return this.element;
    }
}
