# Test Cases - Notes App

These are the functional test cases I wrote for the Notes app. All of them are automated with Appium + Java + TestNG, and the code is in `src/test/java/tests/`.

There are 47 test cases in total. Three of the validation tests run twice (once with an empty value and once with only spaces), which is why the count is 47 even though there are 44 test methods.

Before every test the app data is cleared and the app is opened fresh, so each test starts with an empty list and doesn't depend on any other test.


## Scenarios from the assignment

The assignment asked for these five scenarios. I've marked which test cases cover each one:

| Scenario | Test cases | Class |
|---|---|---|
| **1. Create a new text note and verify that the content is saved and displayed correctly** | TC-01 to TC-03 | TextNoteTests |
| **2. Create a new to-do list with multiple checklist items** | TC-04 to TC-06, TC-11 | TodoListTests |
| **3. Mark to-do list items as completed and validate their checked state** | TC-07 to TC-10 | TodoListTests |
| **4. Edit and delete existing notes and to-do list items** | TC-12 to TC-20 | EditDeleteTests |
| **5. Validate input restrictions, empty fields and error messages** | TC-21 to TC-34 | ValidationTests |

I also added a few more cases on my own: list features like filter and sort (TC-35 to TC-41), and some edge cases like rotating the phone (TC-42 to TC-47).

Status column: **Pass** means the app works as expected. **Fail (bug)** means the test found a real problem in the app, so it fails on purpose. Those tests are tagged `known-defect`.


## Scenario 1 - Create a text note (required)

Class: `TextNoteTests`

| ID | Test case | Steps | Data | Expected result | Status |
|---|---|---|---|---|---|
| TC-01 | New note shows up in the list | Check list is empty, tap + > New Text Note, enter title and content, tap Save | grocery / Milk and eggs | Note is in the list, only 1 note, type shows TEXT, preview shows the content | Pass |
| TC-02 | Note content is the same when reopened | Create a note with two lines of content, open it again | Meeting / Line 1, Line 2 | Title and both lines are exactly as typed | Pass |
| TC-03 | Note is still there after restarting the app | Create a note, close the app, open it again | persisting / keeping this | Note is still in the list | Pass |


## Scenario 2 - Create a to-do list with multiple items (required)

Class: `TodoListTests`. Most tests use the same three items: Buy maggi, Call vishal, Pay rent.

| ID | Test case | Steps | Data | Expected result | Status |
|---|---|---|---|---|---|
| TC-04 | Items show in the order they were added | Tap + > New Todo List, enter title, add the 3 items one by one | Chores | Items in the same order, none ticked, input box clears after each add | Pass |
| TC-05 | Saved list shows on the home screen | Create list with 3 items, tap Save List | Weekend | List is shown, type shows TODO, preview shows the 3 items | Pass |
| TC-06 | All items are there when the list is reopened | Create and save a list, open it again | Packing | Same title and same 3 items in order | Pass |
| TC-11 | Preview only shows the first 3 items | Save a list with 5 items, look at the card on the home screen | One to Five | Preview shows One, Two, Three only | Pass |


## Scenario 3 - Mark items as completed (required)

Class: `TodoListTests`

| ID | Test case | Steps | Data | Expected result | Status |
|---|---|---|---|---|---|
| TC-07 | Ticking items marks them as done | Create list with 3 items, tick Buy maggi and Pay rent | Sprint | Those two are ticked, Call vishal is not | Pass |
| TC-08 | Ticks are saved | Tick Call vishal, save, open the list again | Saved checks | Only Call vishal is ticked | Pass |
| TC-09 | Ticks are still there after restarting the app | Tick Buy maggi and Pay rent, save, restart app, open list | Restart checks | Buy maggi and Pay rent ticked, Call vishal not | Pass |
| TC-10 | Unticking an item is saved too | Tick Buy maggi and save, reopen, untick it and save, reopen | Toggle | Nothing is ticked at the end | Pass |


## Scenario 4 - Edit and delete (required)

Class: `EditDeleteTests`

**Edit**

| ID | Test case | Steps | Data | Expected result | Status |
|---|---|---|---|---|---|
| TC-12 | Editing a note updates the same note | Create a note, open it, change title and content, save | Draft / old body changed to Final / new body | Final is listed, Draft is gone, still only 1 note, new content shows | Pass |
| TC-13 | Edited note moves to the top | Create Older then Newer, edit Older and save | Older, Newer | Order changes from Newer, Older to Older, Newer | Pass |
| TC-14 | Going back without saving keeps the old note | Open a note, change it, press Back | Keep me / original | Note still has the old title and content | Pass |
| TC-15 | Content can be cleared | Open a note, delete all content, save | Clear body | Content is empty in the list and in the note | Pass |
| TC-16 | Rename a list and add items | Create list with 1 item, rename it, add 2 more items, save | Errands changed to Errands v2, items Bank and Pharmacy added | New name is listed, old one is gone, still 1 list, 3 items | Pass |
| TC-17 | Changing ticks on a saved list | Save list with A ticked, reopen, untick A and tick B, save | Review / A, B | Only B is ticked | Pass |
| TC-18 | Going back without saving drops new items | Open a saved list, add an item, press Back, reopen | Unsaved items | Only the originally saved item is there | Pass |

**Delete**

| ID | Test case | Steps | Data | Expected result | Status |
|---|---|---|---|---|---|
| TC-19 | Delete a note | Create a note, try swiping it left, then try long pressing it | Delete me | Note is removed | Fail (bug) - there is no way to delete a note in the app |
| TC-20 | Delete a to-do item | Create list with 2 items, swipe one left | Keep, Remove | Only Keep is left | Fail (bug) - there is no way to delete an item, the delete button is commented out in the app code |

Note: in this app swiping or long pressing a note just opens it, so TC-19 goes back to the list before checking. Otherwise it would look like the note was deleted because the note editor doesn't show the list.


## Scenario 5 - Validation and error messages (required)

Class: `ValidationTests`

**Empty fields**

| ID | Test case | Steps | Data | Expected result | Status |
|---|---|---|---|---|---|
| TC-21 | Note with empty title can't be saved | New text note, leave title empty, add content, tap Save | title "" | Stays on the same screen, shows "Title cannot be empty", content is not lost, no note created | Pass |
| TC-22 | Note with only spaces as title can't be saved | Same as TC-21 | title "     " | Same as TC-21 | Pass |
| TC-23 | Note can be saved after fixing the title | Tap Save with no title (error), then type a title and save | Recovered | Note is saved, 1 note in the list | Pass |
| TC-29 | To-do list with empty title can't be saved | New list, leave title empty, add an item, tap Save List | title "" | Stays on the same screen, shows "Title cannot be empty", item is still there, nothing created | Pass |
| TC-30 | To-do list with only spaces as title can't be saved | Same as TC-29 | title "     " | Same as TC-29 | Pass |
| TC-31 | Empty item is not added | Add one real item, then tap Add with an empty box | item "" | Only the real item is in the list | Pass |
| TC-32 | Item with only spaces is not added | Same as TC-31 | item "     " | Only the real item is in the list | Pass |
| TC-27 | Back on a new note or list saves nothing | Type something in a new note and press Back, same for a new list | Abandoned | List is still empty | Pass |
| TC-28 | New note and new list open empty | Open a new note and a new list | - | All fields empty, no items | Pass |
| TC-34 | List with no items can be saved | New list, only enter a title, save | Empty list | List is saved and preview says [Empty Todo List] | Pass |

**Input restrictions**

| ID | Test case | Steps | Data | Expected result | Status |
|---|---|---|---|---|---|
| TC-24 | Extra spaces are removed from title and content | Save a note with spaces before and after the text | "   Trimmed   " / "  padded body  " | Saved as Trimmed / padded body | Pass |
| TC-25 | Long title is accepted | Save a note with a 200 character title | 200 x "s" | Full title is saved | Pass |
| TC-26 | Two notes can have the same title | Save two notes with the same title | Same | Both notes are in the list | Pass |
| TC-33 | Extra spaces are removed from items | Add an item with spaces around it | "   padded item   " | Item is saved as padded item | Pass |


## Extra - List features (not required)

Class: `NotesListFeatureTests`

| ID | Test case | Steps | Expected result | Status |
|---|---|---|---|---|
| TC-35 | App starts with an empty list | Open the app | No notes | Pass |
| TC-36 | + button shows both options | Tap + | Dialog "Create New Note" with New Text Note and New Todo List | Pass |
| TC-37 | Closing the dialog does nothing | Tap +, press Back | Dialog closes, no note created | Pass |
| TC-38 | Each card shows the right type | Create one note and one list | Note shows TEXT, list shows TODO | Pass |
| TC-39 | Filter by type | Create both types, use Show Text Notes Only, Show Todo Lists Only, Show All Notes | Only the matching notes show each time, then both again | Pass |
| TC-40 | Sort by title | Create Banana, Cherry, Apple, use Sort by Title twice | First time A to Z, second time back to newest first | Pass |
| TC-41 | Filter stays on after adding a note | Turn on Show Todo Lists Only, then add a text note | New text note should not show | Fail (bug) - happens sometimes, the filter resets and the note shows |


## Extra - Edge cases (not required)

Class: `EdgeCaseTests`. After each of these tests the phone is turned back to portrait.

| ID | Test case | Steps | Expected result | Status |
|---|---|---|---|---|
| TC-42 | Rotate the phone while editing | Open a saved note, change the text without saving, rotate to landscape | Changed text is still there | Fail (bug) - the typed changes are lost after rotating |
| TC-43 | Filter after opening a note and coming back | Filter to Todo lists only, open a list, press Back | Filter is still on | Pass |
| TC-44 | Sort by title with small and capital letters | Create Zebra, apple, Mango and sort by title | apple, Mango, Zebra | Fail (bug) - app shows Mango, Zebra, apple because sorting is case sensitive |
| TC-45 | Tapping Save twice quickly | New note, tap Save two times fast | Only one note is created | Pass |
| TC-46 | Ticking an item in a long list | Make a list with 25 items, tick Item 1, scroll down and back up | Only Item 1 is ticked | Fail (bug) - ticks show up on other rows after scrolling |
| TC-47 | App goes to background while typing | Type in a new note, send app to background for 5 seconds, come back | Text is still there | Pass |


## Bugs found

1. No way to delete a note (TC-19)
2. No way to delete a to-do item (TC-20)
3. Typed changes are lost when the phone is rotated while editing a note (TC-42)
4. Sort by title is case sensitive, so "apple" comes after "Zebra" (TC-44)
5. In a long to-do list, ticks move to other rows after scrolling (TC-46)
6. The type filter sometimes resets after adding a note - not every time (TC-41)


## Summary

| Area | Total | Pass | Fail (bug) |
|---|---|---|---|
| 1. Create text note | 3 | 3 | 0 |
| 2. Create to-do list | 4 | 4 | 0 |
| 3. Mark items completed | 4 | 4 | 0 |
| 4. Edit and delete | 9 | 7 | 2 |
| 5. Validation | 14 | 14 | 0 |
| List features | 7 | 6 | 1 |
| Edge cases | 6 | 3 | 3 |
| **Total** | **47** | **41** | **6** |

To run everything: start the emulator and Appium, then run `mvn clean test`. The report is created at `reports/NotesAppReport.html`.

To run just one test case, for example TC-07: `mvn test -Dtest=TodoListTests#checkItems_showsCheckedState`
