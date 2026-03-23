package aniket.makemytrip.pages;

import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {

	protected final WebDriver driver;
	protected final WebDriverWait wait;
	protected final JavascriptExecutor js;

	protected BasePage(WebDriver driver, Duration waitTimeout) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, waitTimeout);
		this.js = (JavascriptExecutor) driver;
	}

	protected void jsClick(WebElement element) {
		js.executeScript("arguments[0].click();", element);
	}

}
