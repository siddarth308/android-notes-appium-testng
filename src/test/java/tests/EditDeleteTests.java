package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import pages.NotesListPage;
import pages.TextNotePage;
import pages.TodoListPage;

import java.util.List;

import static org.testng.Assert.*;

public class EditDeleteTests extends BaseTest {

    // EDIT

    @Test(description = "Editing a text note updates it in place (no duplicate)")
    public void editTextNote_updatesTitleAndContent() {
        NotesListPage list = home().createTextNote("Draft", "old body");

        list.openTextNote("Draft")
                .enterTitle("Final")
                .enterContent("new body")
                .save();

        assertTrue(list.waitForNote("Final"), "New title should be listed");
        assertFalse(list.hasNote("Draft"), "Old title should be gone");
        assertEquals(list.count(), 1, "Edit must update the note, not create a second one");
        assertEquals(list.preview("Final"), "new body");

        TextNotePage reopened = list.openTextNote("Final");
        assertEquals(reopened.title(), "Final");
        assertEquals(reopened.content(), "new body");
    }

    @Test(description = "An edited note moves to the top of the list (newest first)")
    public void editTextNote_movesToTop() {
        NotesListPage list = home().createTextNote("Older", "a").createTextNote("Newer", "b");
        assertEquals(list.titles(), List.of("Newer", "Older"), "Precondition: newest on top");

        list.openTextNote("Older").enterContent("a - edited").save();

        assertEquals(list.titles(), List.of("Older", "Newer"), "Edited note should now be on top");
    }

    @Test(description = "Back without saving throws away the changes")
    public void editTextNote_backDiscardsChanges() {
        NotesListPage list = home().createTextNote("Keep me", "original");

        list.openTextNote("Keep me").enterTitle("Not saved").enterContent("changed").goBack();

        assertTrue(list.waitForNote("Keep me"), "Original title should still be there");
        assertFalse(list.hasNote("Not saved"));
        assertEquals(list.preview("Keep me"), "original");
    }

    @Test(description = "Clearing a note's content and saving stores an empty body")
    public void editTextNote_clearContent() {
        NotesListPage list = home().createTextNote("Clear body", "to be removed");

        list.openTextNote("Clear body").enterContent("").save();

        assertEquals(list.preview("Clear body"), "");
        assertEquals(list.openTextNote("Clear body").content(), "");
    }

    @Test(description = "Rename a todo list and add items: same list is updated")
    public void editTodoList_renameAndAddItems() {
        NotesListPage list = home().createTodoList("Errands", "Post office");

        list.openTodoList("Errands")
                .enterTitle("Errands v2")
                .addItem("Bank")
                .addItem("Pharmacy")
                .save();

        assertTrue(list.waitForNote("Errands v2"));
        assertFalse(list.hasNote("Errands"));
        assertEquals(list.count(), 1, "Edit must not duplicate the list");
        assertEquals(list.openTodoList("Errands v2").items(), List.of("Post office", "Bank", "Pharmacy"));
    }

    @Test(description = "Changing checked states of a saved list is saved")
    public void editTodoList_changeCheckedStates() {
        home().newTodoList().enterTitle("Review").addItem("A").addItem("B").check("A").save();

        home().openTodoList("Review").uncheck("A").check("B").save();

        assertEquals(home().openTodoList("Review").checkedStates(), List.of(false, true));
    }

    @Test(description = "Back from a todo list without saving discards new items")
    public void editTodoList_backDiscardsNewItems() {
        NotesListPage list = home().createTodoList("Unsaved items", "Saved item");

        list.openTodoList("Unsaved items").addItem("Never saved").goBack();

        assertEquals(list.openTodoList("Unsaved items").items(), List.of("Saved item"));
    }

    // DELETE
    // The app has NO delete feature. These tests are EXPECTED TO FAIL-

    @Test(groups = "known-defect", description = "A note can be deleted. KNOWN GAP: the app has no delete option")
    public void deleteTextNote() {
        NotesListPage list = home().createTextNote("Delete me", "temporary");

        // Try the usual Android delete gestures: swipe left, then long-press
        list.swipeNoteLeft("Delete me");
        backToList(list);
        if (list.hasNote("Delete me")) {
            list.longPressNote("Delete me");
            backToList(list);
        }

        assertFalse(list.hasNote("Delete me"),
                "Expected a way to delete the note (swipe, long-press menu or Delete button), but none exists");
    }

    @Test(groups = "known-defect", description = "A single todo item can be deleted. KNOWN GAP: delete button is commented out in the app")
    public void deleteTodoItem() {
        TodoListPage todo = home().newTodoList().enterTitle("Delete item").addItem("Keep").addItem("Remove");

        todo.swipeItemLeft("Remove");

        assertEquals(todo.items(), List.of("Keep"),
                "Expected 'Remove' to be deletable, but there is no delete control");
    }

    /**
     * TRAP: on this app a swipe or long-press counts as a TAP and OPENS the note.
     * The editor screen has 0 note cards, so "note is gone" would wrongly PASS.
     * Always return to the list before checking.
     */
    private void backToList(NotesListPage list) {
        if (!list.isDisplayed()) {
            list.pressBack();
        }
        list.waitUntilLoaded();
    }
}