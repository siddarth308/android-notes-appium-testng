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

    /** Filled in at Stage 6. */
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