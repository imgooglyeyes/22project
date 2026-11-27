# ShopeeFarm

by: shawn, jian, and matt

- a java desktop app for a farm-to-market store with a built-in back office
- built with javafx, data is saved in plain text files
- customers browse and request products, clerks and managers run the sales and stock
- see `TODO.md` for the current status and the plan for what is next

## Roles

- **guest** (not logged in)
  - browse the home page and the marketplace
  - log in or sign up
- **customer**
  - everything a guest can do
  - request to buy a product
  - see their own requests in my requests
- **clerk**
  - add stock from the inventory to the shelf
  - view the inventory
  - confirm and void transactions
  - view the cash balance
  - view their own history
- **manager**
  - everything a clerk can do, except the own history item
  - restock or delete inventory stock
  - move shelf stock back to the inventory
  - create, edit, and delete products
  - reset all product data to the template data
  - approve or reject clerk and manager sign-ups
  - open any staff member's dashboard and history

## Main Features

### 1. Marketplace

- shows the shelf as a grid of product cards
- each card has a picture, price, stock badge, and a harvest or made-from chip
- filter by all, harvest, or made from harvest
- sort by name, price, or stock
- staff can switch between manage shelf and marketplace preview
- staff can save all data by hand with the save button

### 2. Products (managers)

- create, edit, and delete products
- a product is a harvest item or is made from one harvest item
- pick a picture for each product
- a duplicate name or a bad price is rejected with a message
- delete removes the product from the database, inventory, and shelf
- reset puts all product files back to the template data

### 3. Inventory

- clerks and managers can view it
- managers can restock from the database
- managers can delete stock

### 4. Sales

- a customer sends a request, it never changes stock
- a clerk or manager confirms it, stock leaves the shelf
- selling more than the shelf has is blocked
- voiding a confirmed sale returns the stock and takes back the cash
- the status goes: requested, confirmed, voided
- every confirm and void saves who did it and when

### 5. Cash and History

- the cash page shows the balance and every sale and void
- the cash balance comes from the transactions, there is no separate cash file
- history shows the requests a staff member confirmed or voided

### 6. Accounts

- customers are active right away
- clerk and manager sign-ups wait for a manager to approve them
- a pending or rejected account cannot log in
- usernames are not case sensitive and `guest` is reserved

### 7. Dashboards

- staff home shows tiles for pending requests, low stock, cash, requests handled, sales confirmed, and sign-ups
- home for guests and customers shows a welcome, trending products, and a short note about the farm

### 8. Small Helpers

- search box in the top bar filters the open page
- bell in the top bar shows how many requests are waiting
- toasts confirm every action that worked
- dialogs are only used for errors and for things that cannot be undone

## How Stock Moves

- stock flows one way: database -> inventory -> shelf
- restock adds new stock to the inventory
- add to shelf moves stock from the inventory to the shelf
- move to inventory moves stock back from the shelf
- only `FarmSystem` changes stock, so the containers never disagree
- shelf stock at 3 or less counts as low stock

## Project Layout

- `TODO.md`
  - current status, bugs, and the phased plan
- `fxtest2/src/application/`
  - `Main.java` starts the app
  - `FarmSystem.java` holds the business rules
  - models: `Product`, `User`, `SaleTransaction`
  - containers: `ProductContainer`, `UserContainer`, `TransactionContainer`
  - file handlers: `ProductsFileHandler`, `UsersFileHandler`, `TransactionFilesHandler`
  - helpers: `Money`, `ProductImages`
  - `application.css` is the shared stylesheet
- `fxtest2/src/shell/`
  - `SceneManager` owns the stage, role checks, and page switching
  - `AppShell`, `TopBar`, `SideNav`, `UserChip`, `PageToolbar`
- `fxtest2/src/pages/`
  - one class per screen: home, staff home, marketplace, inventory, products, transactions, my requests, history, cash, staff, auth
- `fxtest2/src/popup/`
  - popup windows built on `PopupWindow`
  - add stock, move stock, create product, edit product, request to buy
- `fxtest2/src/tables/`
  - `DataTable` is the base table
  - `ProductTable`, `TransactionTable`, `UserTable`
- `fxtest2/src/ui/`
  - reusable parts: `ProductCard`, `NavItem`, `BellButton`, `Toast`, `EmptyState`, `SegmentedToggle`
  - form parts: `LabelledTextField`, `MadeFromPicker`, `ImagePicker`, `RevealPasswordField`
  - helpers: `Dialogs`, `Styles`, `Fonts`, `Icons`, `Ui`, `Searchable`
- `fxtest2/src/data/`
  - the saved files and product pictures
- `fxtest2/src/templatedata/`
  - the starting files, used when a saved file is missing
- `fxtest2/bin/`
  - compiled class files
- `.vscode/launch.json`
  - the working javafx launch configuration
- `.vscode/settings.json`
  - java source, output, runtime, and javafx library settings

## OOP Design

- **encapsulation**: only `FarmSystem` changes stock and saves product files
- **abstraction**: pages ask `SceneManager` role questions like `canManageInventory()`
- **inheritance**: popups extend `PopupWindow`, tables extend `DataTable`
- **polymorphism**: pages that implement `Searchable` get the top bar search box
- **reusability**: one page is shared by every role, it only hides buttons the role cannot use

## Saved Data

- files are in `fxtest2/src/data/`, one record per line, fields separated by `|`
- if a file is missing, the matching file in `templatedata` is used
- a bad line is skipped and printed, it does not crash the app
- the `|` character is not allowed in any field
- files:
  - `productsDB.txt`: `id|name|description|price|madeFrom|image`
  - `inventory.txt`: `id|stock`
  - `shelf.txt`: `id|stock`
  - `users.txt`: `username|password|firstName|lastName|userType|status`
  - `transactions.txt`: `id|productId|productName|unitPrice|quantity|requester|status|confirmedBy|voidedBy|requestedAt|confirmedAt|voidedAt`
- `transactions.txt` has no template, a missing file means no transactions yet
- product pictures are in `data/images`, then `templatedata/images`
- the logo is loaded from `/application/images/logo.png`
- the fonts are loaded from `/application/fonts/Plus_Jakarta_Sans/static/`
- a missing logo or font only prints a message, the app still starts

## Starting Accounts

- the starting accounts are in `templatedata/users.txt`
- `Jian` is a manager
- `Matt` and `Ana` are clerks
- `shawneee`, `mariaclara`, `juandc`, and `liza` are customers
- `pedro` is a pending clerk and `rico` is a rejected clerk, they are for testing the sign-up flow

## Requirements

- windows
- java development kit 26
- javafx sdk 27.0.1
- visual studio code with the java extension pack is recommended
- the javafx sdk should be at the path already used by the project:
  - `c:\users\shawneee\documents\eclipse-java\eclipse\javafx-27_windows-x64_bin-sdk\javafx-sdk-27.0.1`
- if the javafx sdk is somewhere else, update the local setup carefully before running

## How to Run in Visual Studio Code

1. open the repository folder in visual studio code
2. open the **run and debug** panel
3. choose **launch fxtest2 javafx application**
4. press `f5`
5. do not use the generic **run java** option
   - it may start java without the javafx module path
   - this causes `java.lang.NoClassDefFoundError: javafx/application/Application`

## Basic Application Flow

- the app starts in `application.Main`
- `Main` does these steps:
  - loads the fonts
  - loads the products, inventory, and shelf
  - loads the transactions and the users
  - starts as a guest
  - creates `SceneManager` and opens the home page
- one window and one scene are used for the whole app
- `SceneManager` swaps the page in the middle, the top bar and side nav stay
- logging in or out lands on the home page of the role
- every scene and popup loads `application.css` through `ui.Styles`

## Important Notes

- keep the working javafx launch configuration unchanged
  - it has the main class, class path, javafx module path, module names, and native library path
  - changing it can make the app fail to start
- the data paths start with `fxtest2/`, so run the app from the repository folder
- `application.css` must stay in the `application` package, `ui.Styles` loads it from `/application/application.css`
- role rules live in `SceneManager`, `FarmSystem` does not check roles

## Troubleshooting

- if the terminal says `could not find or load main class`, check that:
  - the class path is set to `bin`
  - `application.Main` is spelled correctly
- if the terminal reports a javafx class error, check that:
  - `--module-path` points to the javafx `lib` folder
  - `--add-modules javafx.controls,javafx.fxml` is included
  - the javafx sdk version matches the installed files
- if the app does not open after a source change:
  - stop any old java process
  - run the visual studio code javafx configuration
  - check the problems panel for compile errors
- if the app crashes with a `NullPointerException` in `Styles.apply`:
  - `application.css` was not copied to `bin/application/`
  - clean and rebuild so the css is copied next to the classes
- if no data shows up or old data keeps loading:
  - check the console, it prints the path of the transactions file it reads
  - delete the file in `fxtest2/src/data/` to fall back to the template data