package pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

public class TextNotePage extends BasePage {
    private final By titleField = id("noteNameEditText");
    private final By contentField = id("noteContentEditText");
    private final By saveButton = id("saveNoteButton");

    public TextNotePage(AndroidDriver driver) {
        super(driver);
        visible(saveButton);
    }

    public TextNotePage enterTitle(String t) {
        type(titleField, t);
        return this;
    }

    public TextNotePage enterContent(String c) {
        type(contentField, c);
        return this;
    }

    public String title() {
        return fieldValue(titleField, "Note Title");
    }

    public String content() {
        return fieldValue(contentField, "Enter your note here...");
    }

    public boolean isDisplayed() {
        return isPresent(saveButton);
    }

    public void tapSave() {
        hideKeyboard();
        tap(saveButton);
    }

    public NotesListPage save() {
        tapSave();
        return new NotesListPage(driver).waitUntilLoaded();
    }

    // Taps SAVE twice, really fast, like an impatient user would.
  
    public NotesListPage doubleTapSave() {
        hideKeyboard();
        WebElement save = visible(saveButton);
        driver.executeScript("mobile: doubleClickGesture", java.util.Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) save).getId()));
        driver.activateApp("com.example.myapp");
        return new NotesListPage(driver).waitUntilLoaded();
    }

    public NotesListPage goBack() {
        hideKeyboard();
        pressBack();
        return new NotesListPage(driver).waitUntilLoaded();
    }

    //
    // The "Title cannot be empty" message is drawn in a separate popup window, and
    // only while the title field is focused.So: tapping the field, let Appium see
    // popup windows, then search the screen for the text.

    public boolean hasTitleError() {
        visible(titleField).click();
        driver.setSetting("enableMultiWindows", true);
        try {
            return wait.until(d -> d.getPageSource().contains("Title cannot be empty"));
        } catch (TimeoutException e) {
            return false;
        } finally {
            driver.setSetting("enableMultiWindows", false);
        }
    }
}