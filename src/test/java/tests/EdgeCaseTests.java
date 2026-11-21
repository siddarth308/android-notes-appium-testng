package tests;

import base.BaseTest;
import org.openqa.selenium.ScreenOrientation;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import pages.NotesListPage;
import pages.TextNotePage;
import pages.TodoListPage;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * The "what if the user does something awkward" tests. These are not part of the
 * basic requirements, I added them after poking at the app and suspecting it
 * would break in these spots. If one of them fails, that's the app's problem,
 * not the test's.
 */
public class EdgeCaseTests extends BaseTest {

    // A test that rotates the phone must put it back, otherwise the next test
    // starts sideways and everything looks different.
    @AfterMethod(alwaysRun = true)
    public void backToPortrait() {
        driver.rotate(ScreenOrientation.PORTRAIT);
    }

    @Test(groups = {"edge-case", "known-defect"},
            description = "Rotate while editing a saved note: what I typed should still be there")
    public void rotateWhileEditing_keepsUnsavedText() {
        home().createTextNote("Original", "original body");
        TextNotePage editor = home().openTextNote("Original");
        editor.enterTitle("Edited title").enterContent("edited body");

        editor.rotateTo(ScreenOrientation.LANDSCAPE);

        // I suspect the screen re-reads the note from the database after a rotate
        // and wipes whatever was typed but not saved yet.
        assertEquals(editor.title(), "Edited title", "Title typed before rotating was lost");
        assertEquals(editor.content(), "edited body", "Content typed before rotating was lost");
    }

    @Test(groups = "edge-case",
            description = "Filter by type, open a note, come back: the filter should still be on")
    public void filter_staysOnAfterOpeningNoteAndGoingBack() {
        NotesListPage list = home().createTextNote("Text hidden", "t").createTodoList("Todo keep", "item");
        list.chooseMenu("Show Todo Lists Only");
        assertEquals(list.waitForTitles(List.of("Todo keep")), List.of("Todo keep"),
                "Precondition: only the todo list should be shown");

        list.openTodoList("Todo keep").goBack();

        // same kind of problem as filter_staysOnAfterAddingNote: coming back to the
        // list probably loads the full data again and forgets the filter
        assertEquals(list.waitForTitles(List.of("Todo keep")), List.of("Todo keep"),
                "Filter was reset after opening a note and going back");
    }

    @Test(groups = {"edge-case", "known-defect"},
            description = "Sort by title should ignore upper/lower case (apple, Mango, Zebra)")
    public void sortByTitle_ignoresCase() {
        NotesListPage list = home()
                .createTextNote("Zebra", "1")
                .createTextNote("apple", "2")
                .createTextNote("Mango", "3");

        list.chooseMenu("Sort by Title");

        // A plain String sort puts every capital letter before every small one,
        // so "apple" would land after "Zebra". People expect plain A-Z.
        List<String> expected = List.of("apple", "Mango", "Zebra");
        assertEquals(list.waitForTitles(expected), expected);
    }

    @Test(groups = "edge-case",
            description = "Double-tap SAVE on a new note: only one note should be created")
    public void doubleTapSave_createsOnlyOneNote() {
        TextNotePage editor = home().newTextNote().enterTitle("Tapped twice").enterContent("body");

        NotesListPage list = editor.doubleTapSave();

        // Nothing stops a second tap, so I expect either two copies of the note
        // or the app closing because it went "back" twice.
        assertEquals(list.count(), 1, "Expected one note, but the double tap created: " + list.titles());
    }

    @Test(groups = {"edge-case", "known-defect"},
            description = "25-item todo list: tick the first item, scroll down and up, only that one stays ticked")
    public void longTodoList_tickDoesNotJumpToOtherItems() {
        String[] items = new String[25];
        for (int i = 0; i < items.length; i++) {
            items[i] = "Item " + (i + 1);
        }
        TodoListPage todo = home().newTodoList().enterTitle("Long list");
        todo.addManyItems(items);
        scrollToTop(todo);
        todo.check("Item 1");

        // Rows get reused while scrolling, so a tick can show up on the wrong
        // row. Go all the way down, then all the way back up, and note the state of
        // every row we see on the way.
        Map<String, Boolean> seen = new LinkedHashMap<>(todo.checkedStatesOnScreen());
        for (int i = 0; i < 15 && !seen.containsKey("Item 25"); i++) {
            todo.scrollDown();
            seen.putAll(todo.checkedStatesOnScreen());
        }
        assertTrue(seen.containsKey("Item 25"), "Never reached the last item, scrolling didn't work");
        scrollToTop(todo);
        seen.putAll(todo.checkedStatesOnScreen());

        assertEquals(seen.get("Item 1"), Boolean.TRUE, "After ticking Item 1 the list got mixed up (Item 1 is missing or not ticked). Rows seen: " + seen);
        seen.remove("Item 1");
        seen.forEach((item, checked) ->
                assertFalse(checked, item + " is ticked but only Item 1 was ticked by the test"));
    }

    @Test(groups = "edge-case",
            description = "Send the app to the background for 5 seconds while typing: the text should survive")
    public void backgroundWhileTyping_keepsText() {
        TextNotePage editor = home().newTextNote().enterTitle("Half written").enterContent("still typing...");

        driver.runAppInBackground(Duration.ofSeconds(5));

        assertEquals(editor.title(), "Half written", "Title was lost after the app came back");
        assertEquals(editor.content(), "still typing...", "Content was lost after the app came back");
    }

    // swipe down a few times, enough to get back to the top of a 25 item list
    private void scrollToTop(TodoListPage todo) {
        for (int i = 0; i < 6; i++) {
            todo.scrollUp();
        }
    }
}
