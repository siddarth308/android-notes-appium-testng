package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import pages.NotesListPage;
import pages.TodoListPage;

import java.util.List;

import static org.testng.Assert.*;

public class TodoListTests extends BaseTest {

    private static final List<String> ITEMS = List.of("Buy maggi", "Call vishal", "Pay rent");

    /**
     * Opens a new todo list, types the title and adds the 3 standard items (not
     * saved yet).
     */
    private TodoListPage newListWithItems(String title) {
        TodoListPage todo = home().newTodoList().enterTitle(title);
        for (String item : ITEMS) {
            todo.addItem(item);
        }
        return todo;
    }

    @Test(description = "Items appear in the order added and start unchecked")
    public void createTodoList_itemsAppearInOrderUnchecked() {
        TodoListPage todo = newListWithItems("Chores");

        assertEquals(todo.items(), ITEMS, "Items should be in the order they were added");
        assertEquals(todo.checkedStates(), List.of(false, false, false), "New items should be unchecked");
        assertEquals(todo.newItemFieldValue(), "", "Input box should be empty after adding");
    }

    @Test(description = "Saved todo list is shown on the home screen as TODO with its items in the preview")
    public void createTodoList_isShownInList() {
        NotesListPage list = home().createTodoList("Weekend", "Buy maggi", "Call vishal", "Pay rent");

        assertTrue(list.waitForNote("Weekend"), "Todo list should be listed");
        assertEquals(list.count(), 1);
        assertEquals(list.cardField("Weekend", "noteTypeTextView"), "TODO");
        assertEquals(list.preview("Weekend"), "Buy maggi\n\nCall vishal\n\nPay rent");
    }

    @Test(description = "Reopening a saved todo list restores the title and every item")
    public void createTodoList_itemsRestoredOnReopen() {
        TodoListPage todo = home().createTodoList("Packing", "Buy maggi", "Call vishal", "Pay rent")
                .openTodoList("Packing");

        assertEquals(todo.title(), "Packing");
        assertEquals(todo.items(), ITEMS);
    }

    @Test(description = "Checking items marks them completed; others stay unchecked")
    public void checkItems_showsCheckedState() {
        TodoListPage todo = newListWithItems("Sprint");

        todo.check("Buy maggi").check("Pay rent");

        assertTrue(todo.isChecked("Buy maggi"));
        assertFalse(todo.isChecked("Call vishal"));
        assertTrue(todo.isChecked("Pay rent"));
    }

    @Test(description = "Checked state is saved: still checked after save and reopen")
    public void checkedState_survivesSaveAndReopen() {
        newListWithItems("Saved checks").check("Call vishal").save();

        TodoListPage reopened = home().openTodoList("Saved checks");

        assertEquals(reopened.checkedStates(), List.of(false, true, false));
    }

    @Test(description = "Checked state survives an app restart")
    public void checkedState_survivesAppRestart() {
        newListWithItems("Restart checks").check("Buy maggi").check("Pay rent").save();

        TodoListPage reopened = restartApp().openTodoList("Restart checks");

        assertEquals(reopened.checkedStates(), List.of(true, false, true));
    }

    @Test(description = "Unchecking a completed item is saved too")
    public void uncheckItem_isSaved() {
        newListWithItems("Toggle").check("Buy maggi").save();

        TodoListPage reopened = home().openTodoList("Toggle");
        assertTrue(reopened.isChecked("Buy maggi"), "Precondition: saved as checked");

        reopened.uncheck("Buy maggi").save();

        assertEquals(home().openTodoList("Toggle").checkedStates(), List.of(false, false, false));
    }

    @Test(description = "Card preview shows only the first 3 items of a longer list")
    public void preview_showsFirstThreeItemsOnly() {
        NotesListPage list = home().createTodoList("Five items", "One", "Two", "Three", "Four", "Five");

        assertEquals(list.preview("Five items").trim(), "One\n\nTwo\n\nThree");
    }
}