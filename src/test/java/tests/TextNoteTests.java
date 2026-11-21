package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import pages.NotesListPage;
import pages.TextNotePage;

import static org.testng.Assert.*;

public class TextNoteTests extends BaseTest {

    @Test(description = "New text note appears in the list with the right type and preview")
    public void createTextNote_isShownInList() {
        NotesListPage list = home();
        assertEquals(list.count(), 0, "List should start empty");

        list.createTextNote("grocery", "Milk and eggs");

        assertTrue(list.waitForNote("grocery"), "Note should be listed");
        assertEquals(list.count(), 1);
        assertEquals(list.cardField("grocery", "noteTypeTextView"), "TEXT");
        assertEquals(list.cardField("grocery", "noteContentPreviewTextView"), "Milk and eggs");
    }

    @Test(description = "Reopening a note shows what was saved")
    public void createTextNote_contentRestoredOnReopen() {
        TextNotePage note = home().createTextNote("Meeting", "Line 1\nLine 2").openTextNote("Meeting");
        assertEquals(note.title(), "Meeting");
        assertEquals(note.content(), "Line 1\nLine 2");
    }

    @Test(description = "Note is still there after the app restarts")
    public void createTextNote_survivesRestart() {
        home().createTextNote("persisting", "keeping this");
        assertTrue(restartApp().waitForNote("persisting"));
    }
}