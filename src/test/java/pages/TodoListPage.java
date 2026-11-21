package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;

import java.util.List;

public class TodoListPage extends BasePage {
    private final By titleField = id("todoNoteNameEditText");
    private final By newItemField = id("newTodoItemEditText");
    private final By addButton = id("addTodoItemButton");
    private final By saveButton = id("saveTodoNoteButton");
    private final By itemTexts = id("todoTextView");
    private final By checkboxes = id("todoCheckBox");

    public TodoListPage(AndroidDriver driver) {
        super(driver);
        visible(addButton);
    }

    public TodoListPage enterTitle(String t) {
        type(titleField, t);
        return this;
    }

    public String title() {
        return fieldValue(titleField, "Todo List Title");
    }

    public String newItemFieldValue() {
        return fieldValue(newItemField, "Add new item");
    }

    public boolean isDisplayed() {
        return isPresent(saveButton);
    }

    public TodoListPage addItem(String text) {
        int before = itemCount();
        type(newItemField, text);
        tap(addButton);
        if (!text.trim().isEmpty()) {
            wait.until(d -> itemCount() == before + 1); // wait for the new row
        }
        return this;
    }

    public List<String> items() {
        return driver.findElements(itemTexts).stream().map(WebElement::getText).toList();
    }

    public int itemCount() {
        return driver.findElements(itemTexts).size();
    }

    private WebElement checkboxFor(String item) {
        return visible(AppiumBy.xpath("//*[@resource-id='com.example.myapp:id/todoTextView' and @text='"
                + item + "']/preceding-sibling::*[@resource-id='com.example.myapp:id/todoCheckBox']"));
    }

    public boolean isChecked(String item) {
        return Boolean.parseBoolean(checkboxFor(item).getAttribute("checked"));
    }

    public TodoListPage check(String item) {
        if (!isChecked(item))
            checkboxFor(item).click();
        return this;
    }

    public TodoListPage uncheck(String item) {
        if (isChecked(item))
            checkboxFor(item).click();
        return this;
    }

    public List<Boolean> checkedStates() {
        return driver.findElements(checkboxes).stream()
                .map(cb -> Boolean.parseBoolean(cb.getAttribute("checked"))).toList();
    }

    public void tapSave() {
        hideKeyboard();
        tap(saveButton);
    }

    public NotesListPage save() {
        tapSave();
        return new NotesListPage(driver).waitUntilLoaded();
    }

    public NotesListPage goBack() {
        hideKeyboard();
        pressBack();
        return new NotesListPage(driver).waitUntilLoaded();
    }

    // For long lists. addItem() waits for the row count to go up by one, but once
    // the list is taller than the screen only the visible rows exist, so the count
    // stops growing. Here we just wait for the "Add new item" box to clear instead.
    public TodoListPage addManyItems(String... items) {
        for (String text : items) {
            type(newItemField, text);
            tap(addButton);
            wait.until(d -> newItemFieldValue().isEmpty());
        }
        return this;
    }

    private void swipeList(String direction) {
        Dimension screen = driver.manage().window().getSize();
        driver.executeScript("mobile: swipeGesture", java.util.Map.of(
                "left", screen.width / 4,
                "top", screen.height / 4,
                "width", screen.width / 2,
                "height", screen.height / 2,
                "direction", direction,
                "percent", 0.75));
    }

    // finger moves up, so the list moves down to show later items
    public void scrollDown() {
        swipeList("up");
    }

    public void scrollUp() {
        swipeList("down");
    }

    // item text - checked, for only the rows that are on screen right now
    public java.util.Map<String, Boolean> checkedStatesOnScreen() {
        java.util.Map<String, Boolean> result = new java.util.LinkedHashMap<>();
        for (WebElement row : driver.findElements(itemTexts)) {
            try {
                WebElement box = row.findElement(AppiumBy.xpath(
                        "preceding-sibling::*[@resource-id='com.example.myapp:id/todoCheckBox']"));
                result.put(row.getText(), Boolean.parseBoolean(box.getAttribute("checked")));
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                // row scrolled away while we were reading it, skip it
            }
        }
        return result;
    }

   
    public boolean hasTitleError() {
        return false;
    }

    /** Swipe a todo row to the left (common "swipe to delete" gesture). */
    public void swipeItemLeft(String item) {
        WebElement row = visible(AppiumBy.xpath(
                "//*[@resource-id='com.example.myapp:id/todoTextView' and @text='" + item + "']"));
        driver.executeScript("mobile: swipeGesture", java.util.Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) row).getId(),
                "direction", "left",
                "percent", 0.9));
    }
}