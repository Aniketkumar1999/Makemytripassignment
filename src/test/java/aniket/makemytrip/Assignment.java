package aniket.makemytrip;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.Test;

import aniket.makemytrip.pages.MakeMyTripFlightPage;
import aniket.makemytrip.pages.MakeMyTripFlightPage.FlightDateSelection;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Assignment {

	@Test
	public void test() throws InterruptedException {
		WebDriverManager.chromedriver().setup();
		WebDriver driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

		try {
			driver.get("https://www.makemytrip.com/");
			driver.manage().window().maximize();

			MakeMyTripFlightPage flights = new MakeMyTripFlightPage(driver, Duration.ofSeconds(5));
			flights.dismissInitialModals();
			flights.dismissGstOverlay();
			flights.selectRoundTrip();
			flights.enterFromCity("Kolkata", "Kolkata, India");
			flights.enterToCity("delhi", "New Delhi, India");
			System.out.println();

			FlightDateSelection selection = flights.selectCheapestDepartureAndReturnAcrossMonths(8);
			int lowest = selection.lowestOutboundFare;
			String departureDate = selection.departureDate;
			String returnDate = selection.returnDate;

			System.out.println("Lowest outbound fare: " + lowest);
			System.out.println("Departure date: " + departureDate);
			System.out.println("Return date: " + returnDate);
			flights.clickSearchFlights();
		} finally {
			driver.quit();
		}
	}

}
