package base;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.*;
import pages.NotesListPage;

import java.net.URL;
import java.util.Map;

public class BaseTest {
    protected AndroidDriver driver;
    protected static final String PKG = "com.example.myapp";

    @BeforeClass
    public void startSession() throws Exception {
        UiAutomator2Options opts = new UiAutomator2Options()
                .setApp(System.getProperty("user.dir") + "/app/app-debug.apk")
                .setAppPackage(PKG)
                .setAppActivity(PKG + ".MainActivity")
                .setNoReset(true) // we reset ourselves, below
                .setDisableWindowAnimation(true); // faster, fewer flaky waits
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), opts);
    }

    @BeforeMethod
    public void cleanApp() {
        driver.executeScript("mobile: clearApp", Map.of("appId", PKG)); // wipes all notes
        driver.activateApp(PKG);
        new NotesListPage(driver).waitUntilLoaded();
    }

    @AfterClass(alwaysRun = true)
    public void endSession() {
        if (driver != null)
            driver.quit();
    }

    protected NotesListPage home() {
        return new NotesListPage(driver);
    }

    /** Close and reopen the app WITHOUT clearing data, to check notes are saved. */
    protected NotesListPage restartApp() {
        driver.terminateApp(PKG);
        driver.activateApp(PKG);
        return new NotesListPage(driver).waitUntilLoaded();
    }

    // to capture ss
    public AndroidDriver getDriver() {
        return driver;
    }
}