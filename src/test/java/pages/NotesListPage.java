package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;

import java.util.List;

public class NotesListPage extends BasePage {

    private final By fab = id("addNoteFab");
    private final By cardTitles = id("noteNameTextView");

    public NotesListPage(AndroidDriver driver) {
        super(driver);
    }

    public NotesListPage waitUntilLoaded() {
        visible(fab);
        return this;
    }

    public boolean isDisplayed() {
        return isPresent(fab);
    }

    public TextNotePage newTextNote() {
        tap(fab);
        tap(text("New Text Note"));
        return new TextNotePage(driver);
    }

    public TodoListPage newTodoList() {
        tap(fab);
        tap(text("New Todo List"));
        return new TodoListPage(driver);
    }

    public NotesListPage createTextNote(String title, String content) {
        return newTextNote().enterTitle(title).enterContent(content).save();
    }

    public List<String> titles() {
        return driver.findElements(cardTitles).stream().map(WebElement::getText).toList();
    }

    public int count() {
        return driver.findElements(cardTitles).size();
    }

    private By card(String title) {
        return AppiumBy.xpath("//*[@resource-id='com.example.myapp:id/noteNameTextView' and @text='" + title + "']");
    }

    /** Waits for a card with this title; returns false instead of throwing. */
    public boolean waitForNote(String title) {
        try {
            visible(card(title));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean hasNote(String title) {
        return isPresent(card(title));
    }

    public String cardField(String title, String fieldId) {
        return visible(AppiumBy.xpath("//*[@resource-id='com.example.myapp:id/noteNameTextView' and @text='"
                + title + "']/../*[@resource-id='com.example.myapp:id/" + fieldId + "']")).getText();
    }

    public TextNotePage openTextNote(String title) {
        tap(card(title));
        return new TextNotePage(driver);
    }

    public TodoListPage openTodoList(String title) {
        tap(card(title));
        return new TodoListPage(driver);
    }

    // creates a todo list with the given items and saves it.
    public NotesListPage createTodoList(String title, String... items) {
        TodoListPage todo = newTodoList().enterTitle(title);
        for (String item : items) {
            todo.addItem(item);
        }
        return todo.save();
    }

    // card preview text wait
    public String preview(String title) {
        wait.until(d -> !cardField(title, "noteContentPreviewTextView").equals("Loading Preview..."));
        return cardField(title, "noteContentPreviewTextView");
    }

    /** Swipe a note card to the left (common "swipe to delete" gesture). */
    public void swipeNoteLeft(String title) {
        WebElement card = visible(card(title));
        driver.executeScript("mobile: swipeGesture", java.util.Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) card).getId(),
                "direction", "left",
                "percent", 0.9));
    }

    /** Press and hold a note card (common way to open a delete menu). */
    public void longPressNote(String title) {
        WebElement card = visible(card(title));
        driver.executeScript("mobile: longClickGesture", java.util.Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) card).getId(),
                "duration", 1200));
    }

    // ---------- create dialog ----------

    /** Tap "+" and wait for the "Create New Note" dialog. */
    public NotesListPage openCreateDialog() {
        tap(fab);
        visible(id("alertTitle"));
        return this;
    }

    public String createDialogTitle() {
        return visible(id("alertTitle")).getText();
    }

    public List<String> createDialogOptions() {
        return driver.findElements(AppiumBy.id("android:id/text1"))
                .stream().map(WebElement::getText).toList();
    }

    public boolean isCreateDialogShown() {
        return isPresent(id("alertTitle"));
    }

    // ---------- overflow menu (⋮) ----------

    /** Open the ⋮ menu at the top right and choose an option by its text. */
    public NotesListPage chooseMenu(String option) {
        tap(AppiumBy.accessibilityId("More options"));
        tap(text(option));
        return this;
    }

    /**
     * The list updates a moment after filtering/sorting: wait for the expected
     * order, then return what is shown.
     */
    public List<String> waitForTitles(List<String> expected) {
        try {
            wait.until(d -> titles().equals(expected));
        } catch (TimeoutException ignored) {
            // return what is really on screen so the assertion shows the difference
        }
        return titles();
    }
}