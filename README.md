# ShopeeFarm

by: shawn, jian, and matt
- a small java desktop application for managing products, inventory, and shelf
  stock
- the application uses javafx for its user interface
- the project is focused on simple product movement:
  - products are stored in the main database
  - products can be moved into inventory
  - inventory stock can be moved onto the shelf
  - product details and stock levels can be viewed and updated through the
    application windows
- see `TODO.md` for the current status and the plan for what is next

## Main Features

### 1. Product Database Management:
  - view the available products
  - create new products
  - edit product details
  - remove products when they are no longer needed
  
### 2. Inventory Management:
  - add products from the database
  - increase or decrease product stock
  - move products between the database and inventory

### 3 Shelf Management:
  - move stock from inventory to the shelf
  - view the products currently placed on the shelf
  - save shelf changes when the application provides the save action

### 4. Shared Navigation:
  - move between the database, inventory, and shelf screens
  - use the shared application header to keep navigation consistent

### 5. Starting Data:
  - the application starts with sample coconut, rice, fruit, coffee, sugar,
    and other farm products
  - sample inventory and shelf quantities are created when the application
    starts

## Project Layout

- `TODO.md`
  - current status, bugs, and the phased plan for the project
- `fxtest2/src/application/`
  - contains the main application classes
  - contains product, user, and product container models
  - contains `Main.java`, which starts the javafx application
  - contains `application.css`, the shared stylesheet
- `fxtest2/src/ui/`
  - contains the scenes: shelf, inventory, and database
  - contains the scene manager and shared navigation (`AppHeader`)
  - contains reusable parts: `LabelledTextField`, `LabelledPasswordField`
  - contains `Styles.java`, which loads the shared stylesheet into a scene
- `fxtest2/src/backend/`
  - contains the popup windows: add, move, create, and edit product
  - contains `UniversalDeleter`, which removes a product everywhere
- `fxtest2/src/data/`
  - contains data files used by the project
- `fxtest2/src/templatedata/`
  - contains template data files for the application
- `fxtest2/bin/`
  - contains compiled class files used by the terminal run command
- `.vscode/launch.json`
  - contains the working javafx launch configurations
- `.vscode/settings.json`
  - contains the java source, output, runtime, and javafx library settings

## Requirements

- windows
- java development kit 26
- javafx sdk 27.0.1
- visual studio code with the java extension pack is recommended
- the javafx sdk should be available at the path already used by the project:
  - `c:\users\shawneee\documents\eclipse-java\eclipse\javafx-27_windows-x64_bin-sdk\javafx-sdk-27.0.1`
- if the javafx sdk is installed somewhere else, update the local machine
  setup carefully before trying to run the project

## How to run in Visual Studio Code

1. open the repository folder in visual studio code
2. open the **run and debug** panel
3. choose **launch fxtest2 javafx application**
4. press `f5`
5. do not use the generic **run java** option:
  - it may start java without the javafx module path
  - this causes an error similar to:
    - `java.lang.noclassdeffounderror: javafx/application/application`


## Basic Application Flow

- the application starts in `application.Main`
- the Main class:
  - creates the starting product database
  - creates empty inventory and shelf containers
  - adds starting stock to inventory and shelf
  - creates the current manager user
  - opens the shelf screen
- from the shelf screen, use the application header to open:
  - the inventory screen
  - the product database screen
- use the product windows to create, edit, move, or remove products
- every scene and popup window loads `application.css` through `ui.Styles`

## Important Notes

- keep the working javafx launch configuration unchanged
- the launch configuration contains the required:
  - main class
  - compiled class path
  - javafx module path
  - javafx module names
  - native library path
- changing or removing these settings can make the application fail to start
- `application.css` must stay in the `application` package, `ui.Styles` loads it
  from `/application/application.css` on the class path

## Troubleshooting

- if the terminal reports `could not find or load main class`, check that:
  - the current directory is `fxtest2`
  - the class path is set to `bin`
  - `application.Main` is spelled correctly
- if the terminal reports a javafx class error, check that:
  - `--module-path` points to the javafx `lib` folder
  - `--add-modules javafx.controls,javafx.fxml` is included
  - the javafx sdk version matches the installed files
- if the application does not open after a source change:
  - stop any old java process
  - run the visual studio code javafx configuration
  - review the problems panel for compile errors
- if the application crashes with a `NullPointerException` in `Styles.apply`:
  - `application.css` was not copied to `bin/application/`
  - clean and rebuild the project so the css is copied next to the classes