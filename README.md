# android-notes-appium-testng
Mobile UI test automation for an Android Notes app using Appium (UiAutomator2), Selenium 4, Java and TestNG, with the Page Object Model, independent tests and ExtentReports HTML reporting.
## License
The test automation code is licensed under the [MIT License](LICENSE).
The application under test (`app-debug.apk`) is not covered by this license and belongs to its owner.


# android-notes-appium-testng

Mobile UI test automation for an Android Notes app (`com.example.myapp`) using
**Appium (UiAutomator2), Selenium 4, Java and TestNG**, with the Page Object Model,
independent tests and an ExtentReports HTML report.

**41 test cases** (38 test methods, 3 of them data-driven) covering creating, editing and
deleting text notes and to-do lists, checklist states, input validation, filtering and sorting.

---

## Tech stack

| Tool | Version |
|---|---|
| Java (JDK) | 17 or newer (tested with 23) |
| Maven | 3.9+ |
| Node.js | 18+ |
| Appium server | 2.x |
| UiAutomator2 driver | 3.10.0 |
| Selenium | 4.25.0 (pinned in `pom.xml`) |
| Appium java-client | 9.3.0 |
| TestNG | 7.10.2 |
| ExtentReports | 5.1.2 |
| Android SDK / emulator | API 35 (Android 15) |

---

## One-time setup

1. Install **JDK 17+**, **Maven**, **Node.js** and **Android Studio**.
2. Set `ANDROID_HOME` (e.g. `C:\Users\<you>\AppData\Local\Android\Sdk`) and add
   `%ANDROID_HOME%\platform-tools` to `PATH`.
3. Install Appium and the Android driver:
   ```
   npm install -g appium@2
   appium driver install uiautomator2@3.10.0
   ```
   > The newest UiAutomator2 driver (4.x) needs Appium 3. With Appium 2, use 3.10.0.
4. In Android Studio → **Device Manager**, create an emulator (e.g. *Medium Phone, API 35*).
5. Clone this repo:
   ```
   git clone https://github.com/siddarth308/android-notes-appium-testng.git
   cd android-notes-appium-testng
   ```
6. Make sure the app is at `app/app-debug.apk`.

---

## How to run

You need **three terminals**.

**Terminal 1: start the emulator**
```
"%ANDROID_HOME%\emulator\emulator.exe" -avd Medium_Phone_API_35
```
Wait until the home screen appears, then check it's connected:
```
adb devices
```
It must show `emulator-5554    device`.

**Terminal 2: start Appium** (leave it running)
```
appium
```
Wait for `Appium REST http interface listener started on http://0.0.0.0:4723`.

**Terminal 3: run the tests** (from the project folder)
```
mvn clean test
```
The full suite takes about 10 minutes. Keep the emulator and Appium open and don't let
the PC sleep, or the session dies and the remaining tests are skipped.

### Other ways to run

| What | Command |
|---|---|
| Whole suite (`testng.xml`) | `mvn clean test` |
| One class | `mvn test -Dtest=TodoListTests` |
| One test | `mvn test -Dtest=TodoListTests#checkItems_showsCheckedState` |
| From VS Code | Click ▶ next to a `@Test` method |

---

## Test report

After a run, open:
```
reports\NotesAppReport.html
```
It shows every test with pass/fail status, description and category, plus the error and a
**screenshot of the screen** for each failure. TestNG's own results are in `target/surefire-reports/`.

---

## Project structure

```
android-notes-appium-testng
├── app/
│   └── app-debug.apk              App under test
├── src/test/java/
│   ├── base/
│   │   └── BaseTest.java          Starts the Appium session; clears app data before every test
│   ├── pages/                     Page Object Model: one class per screen
│   │   ├── BasePage.java          Shared helpers: waits, tap, type, hide keyboard
│   │   ├── NotesListPage.java     Notes list, + button, create dialog, ⋮ menu
│   │   ├── TextNotePage.java      Text note editor
│   │   └── TodoListPage.java      To-do list editor, items, checkboxes
│   ├── tests/                     Test classes: only steps and assertions
│   └── reporting/
│       └── TestListener.java      ExtentReports HTML + screenshot on failure
├── testng.xml                     Test suite definition
└── pom.xml                        Dependencies and Maven config
```

---

## Test coverage (41 test cases)

| Class | Tests | What it checks |
|---|---|---|
| `TextNoteTests` | 3 | Create a text note; title, type and preview shown in list; content restored on reopen; note survives app restart |
| `TodoListTests` | 8 | Multiple items added in order and unchecked; list shown as TODO with preview; items restored on reopen; check / uncheck items; checked state saved and survives app restart; preview shows first 3 items only |
| `EditDeleteTests` | 9 | Edit note in place (no duplicate); edited note moves to top; Back discards changes; clear content; rename list and add items; change checked states; delete note; delete to-do item |
| `ValidationTests` | 14 | Empty / spaces-only title rejected with **"Title cannot be empty"** for notes and lists (data-driven); fixing the title lets you save; trimming; 200-char title; duplicate titles; blank to-do items ignored; empty list saved as `[Empty Todo List]`; Back creates nothing |
| `NotesListFeatureTests` | 7 | Empty list on first launch; create dialog options; dismiss dialog; type labels; filter by type; sort by title; filter after adding a note |

---

## Design decisions

- **Page Object Model:** locators (resource-ids) live only in `pages/`; tests read like steps.
- **Independent tests:** `BaseTest` runs `mobile: clearApp` before every test, so each test starts
  with an empty app and can run alone or in any order.
- **Explicit waits only:** no `Thread.sleep`; tests wait for the exact element or state they expect.
- **Empty text fields:** UiAutomator reports a field's *hint* as its text when empty
  ("Note Title"), so page objects convert that back to `""`.
- **Error message popup:** Android shows "Title cannot be empty" in a separate popup only while
  the field is focused. Tests tap the field and enable Appium's `enableMultiWindows` setting to read it.
- **Keyboard:** hidden before tapping Save, because it covers the Save button.

---

## Known defects found in the app

These tests are tagged `known-defect` and **fail on purpose**: they document missing or broken app features.

| Test | Defect |
|---|---|
| `deleteTextNote` | There is no way to delete a note (no button, swipe or long-press menu). |
| `deleteTodoItem` | There is no way to delete a to-do item (the delete button is commented out in the app). |
| `filter_staysOnAfterAddingNote` | With "Show Todo Lists Only" on, a newly added text note sometimes still appears. Intermittent. |

**Expected result of a full run:** about 38–39 passed, 2–3 failed (only the known defects above).

---

## Troubleshooting

| Error | Cause / fix |
|---|---|
| `SessionNotCreatedException ... app does not exist` | APK not at `app/app-debug.apk` |
| `Connection refused` / `Could not start a new session` | Appium isn't running (Terminal 2) |
| `Could not find a connected Android device` | Emulator not started; check `adb devices` |
| `Error communicating with the remote browser. It may have died` | Emulator or Appium closed mid-run, or the PC slept; restart both and run again |
| `release version 17 not supported` | Maven is using a broken or very new JDK; run `java -version` and point `JAVA_HOME` to a JDK 17–23 |
| `uiautomator2 cannot be installed ... requires ^4.0.0` | Install the Appium 2 compatible driver: `appium driver install uiautomator2@3.10.0` |
| Red errors everywhere in VS Code | `Ctrl+Shift+P` → **Java: Clean Java Language Server Workspace** |

---

## License

The test automation code is licensed under the [MIT License](LICENSE).
The application under test (`app-debug.apk`) is not covered by this license and belongs to its owner.
