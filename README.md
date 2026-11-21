# android-notes-appium-testng

This is my UI test automation project for a simple Android Notes app (`com.example.myapp`). I wrote it with Appium (UiAutomator2), Selenium 4, Java and TestNG, using the Page Object Model. Every run produces an HTML report made with ExtentReports.

The app lets you create text notes and to-do lists, edit them, filter and sort the list. There are **47 test cases** (44 test methods, and 3 of them run twice with different data) that cover:

- creating, editing and deleting text notes and to-do lists
- ticking and unticking to-do items
- input checks, like empty titles and extra spaces
- filtering and sorting
- a few awkward things a real user might do, like rotating the phone or tapping Save twice

While writing the tests I found a few bugs in the app itself. They are listed in [Bugs I found in the app](#bugs-i-found-in-the-app).

---

## What you need

| Tool | Version I used |
|---|---|
| Java (JDK) | 17 or newer (I tested with 23) |
| Maven | 3.9+ |
| Node.js | 18+ |
| Appium server | 2.x |
| UiAutomator2 driver | 3.10.0 |
| Selenium | 4.25.0 (pinned in `pom.xml`) |
| Appium java-client | 9.3.0 |
| TestNG | 7.10.2 |
| ExtentReports | 5.1.2 |
| Android emulator | API 35 (Android 15) |

---

## Setting it up (only once)

1. Install the JDK, Maven, Node.js and Android Studio.
2. Set `ANDROID_HOME` (for me it's `C:\Users\<you>\AppData\Local\Android\Sdk`) and add `%ANDROID_HOME%\platform-tools` to your `PATH`.
3. Install Appium and the Android driver:
   ```
   npm install -g appium@2
   appium driver install uiautomator2@3.10.0
   ```
   The newest UiAutomator2 driver (4.x) only works with Appium 3, so with Appium 2 you have to use 3.10.0.
4. In Android Studio, open **Device Manager** and create an emulator (I used *Medium Phone, API 35*).
5. Clone the repo:
   ```
   git clone https://github.com/siddarth308/android-notes-appium-testng.git
   cd android-notes-appium-testng
   ```
6. Check that the app file is at `app/app-debug.apk`.

---

## Running the tests

You need three terminals open.

**Terminal 1: start the emulator**
```
"%ANDROID_HOME%\emulator\emulator.exe" -avd Medium_Phone_API_35
```
Wait for the home screen, then check it's connected:
```
adb devices
```
You should see `emulator-5554    device`.

**Terminal 2: start Appium** and leave it running
```
appium
```
Wait until it prints `Appium REST http interface listener started on http://0.0.0.0:4723`.

**Terminal 3: run the tests** from the project folder
```
mvn clean test
```

A full run takes roughly 15 to 20 minutes. Keep the emulator and Appium open, and don't let the laptop go to sleep. If it does, the Appium session dies and all the remaining tests get skipped (this happened to me once).

If you don't want to run everything:

| What | Command |
|---|---|
| Whole suite | `mvn clean test` |
| One class | `mvn test -Dtest=TodoListTests` |
| One test | `mvn test -Dtest=TodoListTests#checkItems_showsCheckedState` |
| From VS Code | Click the ▶ next to a `@Test` method |

---

## The report

When a run finishes, open this file in your browser:
```
reports\NotesAppReport.html
```
It lists every test with its result and description. For each failure it shows the error and a screenshot of what was on screen. Each run overwrites the previous report, so copy it somewhere if you want to keep it. TestNG's own results are in `target/surefire-reports/`.

---

## How the project is laid out

```
android-notes-appium-testng
├── app/
│   └── app-debug.apk              the app being tested
├── src/test/java/
│   ├── base/
│   │   └── BaseTest.java          starts Appium and clears the app before every test
│   ├── pages/                     one class per screen (Page Object Model)
│   │   ├── BasePage.java          shared helpers: waits, tap, type, hide keyboard, rotate
│   │   ├── NotesListPage.java     notes list, + button, create dialog, ⋮ menu
│   │   ├── TextNotePage.java      text note editor
│   │   └── TodoListPage.java      to-do editor, items, checkboxes, scrolling
│   ├── tests/                     the tests, only steps and checks
│   └── reporting/
│       └── TestListener.java      builds the HTML report and takes screenshots on failure
├── testng.xml                     which test classes make up the suite
└── pom.xml                        dependencies and Maven setup
```

---

## What the tests cover

| Class | Tests | What it checks |
|---|---|---|
| `TextNoteTests` | 3 | Creating a text note and seeing it in the list; content is the same when reopened; note is still there after restarting the app |
| `TodoListTests` | 8 | Items are added in order and start unticked; list shows as TODO with a preview; ticking and unticking; ticks are saved and survive a restart; preview shows only the first 3 items |
| `EditDeleteTests` | 9 | Editing a note in place (no duplicate); edited note moves to the top; Back throws away changes; clearing content; renaming a list and adding items; changing ticks; deleting a note and a to-do item |
| `ValidationTests` | 14 | Empty or spaces-only titles are rejected with "Title cannot be empty" (notes and lists); fixing the title lets you save; text is trimmed; 200-character title; duplicate titles; blank to-do items are ignored; an empty list can be saved; Back creates nothing |
| `NotesListFeatureTests` | 7 | Empty list on first launch; create dialog; closing the dialog; type labels; filtering by type; sorting by title; filter after adding a note |
| `EdgeCaseTests` | 6 | Rotating while editing; filter after opening a note and coming back; sorting with capital and small letters; double-tapping Save; a 25-item to-do list; sending the app to the background while typing |

---

## Why I did it this way

- **Page Object Model:** all the element locators live in `pages/`, so the tests read like a list of steps.
- **Every test stands alone:** before each test, `BaseTest` wipes the app's data. Each test starts with an empty app, so tests can run alone or in any order.
- **No `Thread.sleep`:** the tests wait for the exact thing they need.
- **Empty text fields:** when a field is empty, UiAutomator gives back its hint ("Note Title") as the text. The page objects turn that back into an empty string.
- **The error message:** "Title cannot be empty" appears in a separate popup, and only while the field is focused. The tests tap the field and turn on Appium's `enableMultiWindows` setting to be able to read it.
- **Keyboard:** I hide it before tapping Save, because it covers the button.

---

## Bugs I found in the app

These tests are tagged `known-defect`. They fail on purpose, because they show something that is missing or broken in the app, not a problem in the tests.

| Test | What goes wrong |
|---|---|
| `deleteTextNote` | There is no way to delete a note. No button, no swipe, no long-press menu. |
| `deleteTodoItem` | There is no way to delete a to-do item. The delete button is commented out in the app. |
| `filter_staysOnAfterAddingNote` | With "Show Todo Lists Only" turned on, a new text note sometimes still shows up. It doesn't happen every time. |
| `rotateWhileEditing_keepsUnsavedText` | If you rotate the phone while editing a saved note, what you typed is lost and the old saved text comes back. |
| `sortByTitle_ignoresCase` | "Sort by Title" treats capital and small letters differently. You get `Mango, Zebra, apple` instead of `apple, Mango, Zebra`. |
| `longTodoList_tickDoesNotJumpToOtherItems` | In a long list (I used 25 items), ticking Item 1 mixes up the rows. Other items show as ticked, and some rows show the wrong text. It only happens when the list is longer than the screen. |

In a full run I expect about  40 passes and 7 failures, and the failures should only be the ones in this table. The exact number can change because `filter_staysOnAfterAddingNote` is intermittent.

---

## If something goes wrong

| Error | What it usually means |
|---|---|
| `SessionNotCreatedException ... app does not exist` | The APK isn't at `app/app-debug.apk` |
| `Connection refused` or `Could not start a new session` | Appium isn't running (Terminal 2) |
| `Could not find a connected Android device` | The emulator isn't started. Check `adb devices` |
| `Error communicating with the remote browser. It may have died` | The emulator or Appium closed during the run, or the laptop went to sleep. Restart both and run again |
| `release version 17 not supported` | Maven is picking a broken or very new JDK. Run `java -version` and point `JAVA_HOME` to JDK 17 to 23 |
| `uiautomator2 cannot be installed ... requires ^4.0.0` | Install the driver that works with Appium 2: `appium driver install uiautomator2@3.10.0` |
| Red errors everywhere in VS Code | Press `Ctrl+Shift+P` and run **Java: Clean Java Language Server Workspace** |

---

## License

The test code is under the [MIT License](LICENSE). The app being tested (`app-debug.apk`) is not covered by it and belongs to its owner.
