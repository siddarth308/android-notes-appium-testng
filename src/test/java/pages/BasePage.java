package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {
    protected final AndroidDriver driver;
    protected final WebDriverWait wait;

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected By id(String resourceId) {
        return AppiumBy.id("com.example.myapp:id/" + resourceId);
    }

    protected By text(String exactText) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + exactText + "\")");
    }

    protected WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected void tap(By by) {
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    protected void type(By by, String value) {
        WebElement field = visible(by);
        field.clear();
        if (!value.isEmpty())
            field.sendKeys(value);
    }

    /**
     * An empty EditText reports its hint ("Note Title") as text, so map that back
     * to "".
     */
    protected String fieldValue(By by, String hint) {
        String t = visible(by).getText();
        return t.equals(hint) ? "" : t;
    }

    protected boolean isPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }

    public void hideKeyboard() {
        try {
            if (driver.isKeyboardShown())
                driver.hideKeyboard();
        } catch (Exception ignored) {
        }
    }

    public void pressBack() {
        driver.navigate().back();
    }

    // phone orientation
    public void rotateTo(ScreenOrientation orientation) {
        driver.rotate(orientation);
    }
}