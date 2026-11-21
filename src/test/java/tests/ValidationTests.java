package tests;

import base.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.NotesListPage;
import pages.TextNotePage;
import pages.TodoListPage;

import java.util.List;

import static org.testng.Assert.*;

public class ValidationTests extends BaseTest {

    /** Each row runs the test once: an empty title, then a title of only spaces. */
    @DataProvider(name = "blankTitles")
    public Object[][] blankTitles() {
        return new Object[][] {
                { "" },
                { "     " }
        };
    }

    // text note

    @Test(dataProvider = "blankTitles", description = "Blank title is rejected: error shown, stays on editor, nothing saved")
    public void textNote_blankTitle_isRejected(String title) {
        TextNotePage note = home().newTextNote().enterTitle(title).enterContent("Body without a title");

        note.tapSave();

        assertTrue(note.isDisplayed(), "Should stay on the editor");
        assertTrue(note.hasTitleError(), "'Title cannot be empty' should be shown");
        assertEquals(note.content(), "Body without a title", "Typed content must not be lost");
        assertEquals(note.goBack().count(), 0, "No note should be created");
    }

    @Test(description = "After the error, typing a title lets you save")
    public void textNote_fixTitleAfterError_saves() {
        TextNotePage note = home().newTextNote().enterContent("content");
        note.tapSave();
        assertTrue(note.isDisplayed(), "Precondition: save was rejected");

        NotesListPage list = note.enterTitle("Recovered").save();

        assertTrue(list.waitForNote("Recovered"));
        assertEquals(list.count(), 1);
    }

    @Test(description = "Title and content are trimmed when saved")
    public void textNote_whitespaceIsTrimmed() {
        NotesListPage list = home().createTextNote("   Trimmed   ", "  padded body  ");

        assertTrue(list.waitForNote("Trimmed"), "Title should be saved without spaces");
        assertEquals(list.preview("Trimmed"), "padded body");
    }

    @Test(description = "A very long title (200 characters) is accepted")
    public void textNote_longTitle_isAccepted() {
        String longTitle = "s".repeat(200);

        NotesListPage list = home().createTextNote(longTitle, "boundary");

        assertTrue(list.waitForNote(longTitle));
        assertEquals(list.openTextNote(longTitle).title(), longTitle);
    }

    @Test(description = "Two notes with the same title are both kept")
    public void textNote_duplicateTitles_areAllowed() {
        NotesListPage list = home().createTextNote("Same", "first").createTextNote("Same", "second");

        assertEquals(list.titles(), List.of("Same", "Same"));
    }

    @Test(description = "Leaving a new note or list with Back creates nothing")
    public void backWithoutSaving_createsNothing() {
        NotesListPage list = home().newTextNote().enterTitle("Abandoned").enterContent("x").goBack();
        assertEquals(list.count(), 0);

        list.newTodoList().enterTitle("Abandoned list").addItem("x").goBack();
        assertEquals(list.count(), 0);
    }

    @Test(description = "Empty editors show empty fields and the SAVE button")
    public void editors_startEmpty() {
        TextNotePage note = home().newTextNote();
        assertEquals(note.title(), "");
        assertEquals(note.content(), "");
        note.goBack();

        TodoListPage todo = home().newTodoList();
        assertEquals(todo.title(), "");
        assertEquals(todo.newItemFieldValue(), "");
        assertEquals(todo.itemCount(), 0);
    }

    // todo list

    @Test(dataProvider = "blankTitles", description = "Todo list with a blank title is rejected; items are kept")
    public void todoList_blankTitle_isRejected(String title) {
        TodoListPage todo = home().newTodoList().enterTitle(title).addItem("Item 1");

        todo.tapSave();

        assertTrue(todo.isDisplayed(), "Should stay on the todo editor");
        assertTrue(todo.hasTitleError(), "'Title cannot be empty' should be shown");
        assertEquals(todo.items(), List.of("Item 1"), "Added items must not be lost");
        assertEquals(todo.goBack().count(), 0, "No list should be created");
    }

    @Test(dataProvider = "blankTitles", description = "Blank todo item is ignored by ADD")
    public void todoItem_blank_isNotAdded(String text) {
        TodoListPage todo = home().newTodoList().enterTitle("Blank items").addItem("Real item");

        todo.addItem(text);

        assertEquals(todo.items(), List.of("Real item"), "Blank item must not be added");
    }

    @Test(description = "Todo item text is trimmed")
    public void todoItem_isTrimmed() {
        TodoListPage todo = home().newTodoList().enterTitle("Trim items");

        todo.addItem("   padded item   ");

        assertEquals(todo.items(), List.of("padded item"));
    }

    @Test(description = "A todo list with no items can be saved; preview says [Empty Todo List]")
    public void todoList_withoutItems_canBeSaved() {
        NotesListPage list = home().newTodoList().enterTitle("Empty list").save();

        assertTrue(list.waitForNote("Empty list"));
        assertEquals(list.cardField("Empty list", "noteTypeTextView"), "TODO");
        assertEquals(list.preview("Empty list"), "[Empty Todo List]");
    }
}