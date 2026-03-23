package aniket.makemytrip.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MakeMyTripFlightPage extends BasePage {

	public static final class FlightDateSelection {
		public final int lowestOutboundFare;
		public final String departureDate;
		public final String returnDate;

		public FlightDateSelection(int lowestOutboundFare, String departureDate, String returnDate) {
			this.lowestOutboundFare = lowestOutboundFare;
			this.departureDate = departureDate;
			this.returnDate = returnDate;
		}
	}

	private static final By MODAL_CLOSE = By.xpath("//span[@class='commonModal__close']");
	private static final By CHAT_MINIMIZE = By.xpath("//img[@alt='minimize']");
	private static final By BODY = By.tagName("body");
	private static final By ROUND_TRIP = By.xpath("//li[@data-cy='roundTrip']");
	private static final By FROM_CITY = By.id("fromCity");
	private static final By FROM_INPUT = By.xpath("//input[@placeholder='From']");
	private static final By TO_CITY = By.id("toCity");
	private static final By TO_INPUT = By.xpath("//input[@placeholder='To']");
	private static final By PRICE_CELLS = By.xpath("//p[contains(@class, 'todayPrice')]");
	private static final By NEXT_MONTH = By.xpath("//span[@aria-label='Next Month']");
	private static final By PREV_MONTH = By.xpath("//span[@aria-label='Previous Month']");
	private static final By SEARCH_FLIGHTS = By.xpath("//a[contains(@class, 'widgetSearchBtn')]");

	public MakeMyTripFlightPage(WebDriver driver, Duration waitTimeout) {
		super(driver, waitTimeout);
	}

	public void dismissInitialModals() throws InterruptedException {
		Thread.sleep(3000);
		driver.findElement(MODAL_CLOSE).click();
		Thread.sleep(3000);
		driver.findElement(CHAT_MINIMIZE).click();
	}

	public void dismissGstOverlay() {
		WebElement body = driver.findElement(BODY);
		new Actions(driver).moveToElement(body, 250, 100).click().perform();
	}

	public void selectRoundTrip() throws InterruptedException {
		Thread.sleep(3000);
		driver.findElement(ROUND_TRIP).click();
	}

	public void enterFromCity(String typeText, String suggestionExactText) throws InterruptedException {
		driver.findElement(FROM_CITY).click();
		driver.findElement(FROM_INPUT).sendKeys(typeText);
		Thread.sleep(2000);
		driver.findElement(By.xpath("//span[text()='" + suggestionExactText + "']")).click();
	}

	public void enterToCity(String typeText, String suggestionExactText) throws InterruptedException {
		driver.findElement(TO_CITY).click();
		driver.findElement(TO_INPUT).sendKeys(typeText);
		Thread.sleep(2000);
		driver.findElement(By.xpath("//span[text()='" + suggestionExactText + "']")).click();
	}

	public FlightDateSelection selectCheapestDepartureAndReturnAcrossMonths(int maxMonths) throws InterruptedException {
		int absoluteMinPrice = Integer.MAX_VALUE;
		int monthIndex = 0;
		int bestMonthIndex = 0;
		String bestPriceLabel = "";

		for (int i = 0; i < maxMonths; i++) {
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(PRICE_CELLS));
			List<WebElement> priceElements = driver.findElements(PRICE_CELLS);

			for (WebElement p : priceElements) {
				try {
					String digits = p.getText().replaceAll("[^0-9]", "");
					if (!digits.isEmpty()) {
						int currentPrice = Integer.parseInt(digits);
						if (currentPrice < absoluteMinPrice) {
							absoluteMinPrice = currentPrice;
							bestMonthIndex = monthIndex;
							bestPriceLabel = p.getText().trim();
						}
					}
				} catch (Exception ignored) {
					// element may detach while iterating
				}
			}

			try {
				driver.findElement(NEXT_MONTH).click();
				monthIndex++;
				Thread.sleep(500);
			} catch (Exception e) {
				break;
			}
		}

		int stepsBack = monthIndex - bestMonthIndex;
		for (int s = 0; s < stepsBack; s++) {
			driver.findElement(PREV_MONTH).click();
			Thread.sleep(500);
		}

		By cheapestDayLocator = By.xpath("//p[contains(@class,'todayPrice')][normalize-space()='" + bestPriceLabel
				+ "']/ancestor::div[contains(@class, 'DayPicker-Day')]");
		wait.until(ExpectedConditions.elementToBeClickable(cheapestDayLocator));
		WebElement outboundDay = driver.findElement(cheapestDayLocator);
		String departureDate = readDateFromDayCell(outboundDay);
		System.out.println("[DEBUG] Departure date (from calendar cell): " + departureDate);
		jsClick(outboundDay);

		Thread.sleep(2000);
		String returnDate = selectFirstPricedReturnDaySkippingSelected();
		System.out.println("[DEBUG] Return date (from calendar cell): " + returnDate);

		return new FlightDateSelection(absoluteMinPrice, departureDate, returnDate);
	}

	private static String readDateFromDayCell(WebElement dayPickerDayDiv) {
		String aria = dayPickerDayDiv.getAttribute("aria-label");
		if (aria != null && !aria.isBlank()) {
			return aria.trim();
		}
		String dataDay = dayPickerDayDiv.getDomAttribute("data-day");
		if (dataDay != null && !dataDay.isBlank()) {
			return dataDay.trim();
		}
		return dayPickerDayDiv.getText().replaceAll("\\s+", " ").trim();
	}

	private String selectFirstPricedReturnDaySkippingSelected() throws InterruptedException {
		wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_CELLS));
		List<WebElement> priceElements = driver.findElements(PRICE_CELLS);
		for (WebElement p : priceElements) {
			try {
				WebElement day = p.findElement(By.xpath("./ancestor::div[contains(@class,'DayPicker-Day')]"));
				String cls = day.getAttribute("class");
				if (cls != null && (cls.contains("disabled") || cls.contains("blocked"))) {
					continue;
				}
				if (cls != null && cls.contains("selected")) {
					continue;
				}
				String label = readDateFromDayCell(day);
				jsClick(day);
				return label;
			} catch (Exception ignored) {
				// try next
			}
		}
		return "(no return date clicked — no priced day found)";
	}

	public void clickSearchFlights() {
		jsClick(driver.findElement(SEARCH_FLIGHTS));
	}

}
