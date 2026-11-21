package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import pages.NotesListPage;

import java.util.List;

import static org.testng.Assert.*;

public class NotesListFeatureTests extends BaseTest {

    @Test(description = "Fresh app shows an empty list")
    public void freshApp_showsEmptyList() {
        assertEquals(home().count(), 0);
    }

    @Test(description = "'+' opens 'Create New Note' with Text and Todo options")
    public void createDialog_showsBothOptions() {
        NotesListPage list = home().openCreateDialog();

        assertEquals(list.createDialogTitle(), "Create New Note");
        assertEquals(list.createDialogOptions(), List.of("New Text Note", "New Todo List"));
    }

    @Test(description = "Closing the dialog with Back creates nothing")
    public void createDialog_back_createsNothing() {
        NotesListPage list = home().openCreateDialog();

        list.pressBack();

        assertFalse(list.isCreateDialogShown(), "Dialog should be closed");
        assertTrue(list.isDisplayed(), "Should still be on the list");
        assertEquals(list.count(), 0);
    }

    @Test(description = "Each card shows the right type label")
    public void mixedList_showsCorrectTypeLabels() {
        NotesListPage list = home().createTextNote("My text", "x").createTodoList("My todo", "y");

        assertEquals(list.cardField("My text", "noteTypeTextView"), "TEXT");
        assertEquals(list.cardField("My todo", "noteTypeTextView"), "TODO");
    }

    @Test(description = "Filter: text only, todo only, then show all")
    public void filter_byType() {
        NotesListPage list = home().createTextNote("Text A", "t").createTodoList("Todo B", "item");
        assertEquals(list.waitForTitles(List.of("Todo B", "Text A")), List.of("Todo B", "Text A"));

        list.chooseMenu("Show Text Notes Only");
        assertEquals(list.waitForTitles(List.of("Text A")), List.of("Text A"));

        list.chooseMenu("Show Todo Lists Only");
        assertEquals(list.waitForTitles(List.of("Todo B")), List.of("Todo B"));

        list.chooseMenu("Show All Notes");
        assertEquals(list.waitForTitles(List.of("Todo B", "Text A")), List.of("Todo B", "Text A"));
    }

    @Test(description = "Sort by Title: A-Z, and selecting it again goes back to newest first")
    public void sort_byTitle_toggles() {
        NotesListPage list = home()
                .createTextNote("Banana", "2")
                .createTextNote("Cherry", "3")
                .createTextNote("Apple", "1");
        List<String> newestFirst = List.of("Apple", "Cherry", "Banana");
        List<String> alphabetical = List.of("Apple", "Banana", "Cherry");
        assertEquals(list.waitForTitles(newestFirst), newestFirst, "Precondition: newest first");

        list.chooseMenu("Sort by Title");
        assertEquals(list.waitForTitles(alphabetical), alphabetical);

        list.chooseMenu("Sort by Title");
        assertEquals(list.waitForTitles(newestFirst), newestFirst);
    }

    @Test(groups = "known-defect", description = "Filter should stay on after adding a note. KNOWN DEFECT: it sometimes resets")
    public void filter_staysOnAfterAddingNote() {
        NotesListPage list = home().createTodoList("Todo keep", "x");
        list.chooseMenu("Show Todo Lists Only");

        list.createTextNote("Text hidden", "should be filtered out");

        assertFalse(list.hasNote("Text hidden"),
                "'Show Todo Lists Only' is on, so the new TEXT note should not be listed");
    }
}