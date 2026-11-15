# shopeefarm todo

## 1. current progress

### done
- [x] `Product`, `ProductContainer`, `User` models
- [x] three row-based screens: `ShelfScene`, `InventoryScene`, `DatabaseScene`
- [x] `SceneManager` swaps scenes on one stage, `AppHeader` shows buttons by role
- [x] modal windows: `AddProductWindow`, `MoveProductWindow`, `ProductCreationWindow`, `ProductEditingWindow`
- [x] `UniversalDeleter` removes a product from database, inventory and shelf
- [x] stock moves: database -> inventory -> shelf, and back (shelf -> inventory)
- [x] manual save writes `productsDB.txt`, `inventory.txt`, `shelf.txt`
- [x] 27 starting products plus sample stock
- [x] phase 0 cleanup (see section 4)
- [x] `ui/Styles.java` loads `application.css` into every scene and popup window
- [x] prices shown as `₱30.00` through `Product.getFormattedPrice()`
- [x] add and move windows block amounts below 1
- [x] leftover classes removed (`TestLoginWindow`, `ImportExportHandler`)
- [x] readme layout section matches the real folders

### half done
- [ ] persistence: files are written but never read back (`Main` still hardcodes everything)
- [ ] roles: `User` has a role, but the user is hardcoded in `Main` and there is no login
- [ ] styling: stylesheet is loaded everywhere, but inline `setStyle` colors are still in the scenes and override it

### not started
- [ ] login, sign up, manager approval, customer role
- [ ] sales transactions (request, confirm, void)
- [ ] cash balance
- [ ] sale history and clerk list
- [ ] dashboards
- [ ] product unit, crop, type (harvest or made) and "made from" fields
- [ ] shelf grouping and filter by crop / type

---

## 2. bugs and cleanups found in the current code

### fixed
- [x] `ProductContainer.add` and `AddProductWindow` / `MoveProductWindow` accept amount `0`, which creates 0-stock rows on the shelf
- [x] price shows as raw double (`30.0`), now formatted as `₱30.00`
- [x] `application.css` is never loaded, now loaded through `Styles.apply(scene)`
- [ ] `ImportExportHandler` and `TestLoginWindow` are leftovers and not used, deleted


### still open
- [ ] `Main` hardcodes 27 products and ignores `data/*.txt`, so saved changes are lost on restart
- [ ] `ShelfScene.saveData()` writes to `src/data/` but the app runs from `bin/`, pick one data folder and use it everywhere
- [ ] `AddProductWindow` uses a `HashMap` of names, two products with the same name collide and the list order is random
- [ ] `ProductCreationWindow` does not block duplicate names or the `|` character (it would break the txt format)
- [ ] `ShelfScene` is built once with the user in the constructor, it must be rebuilt after login / logout
- [ ] `SceneManager.showInventory()` and `showDatabase()` do not check permissions
- [ ] no confirm before "delete all instances"
- [ ] `templatedata/` is unused, use it as the "reset to default data" source
- [ ] stock logic lives inside the windows, move it so screens and windows only call it
- [ ] `LabelledPasswordField` is unused for now, keep it for the real login screen in phase 3

---

## 3. updated role rules

changed from the old table so it matches what the code can do now and keeps the plan small

| action | guest | customer | clerk | manager |
|---|---|---|---|---|
| browse shelf | yes | yes | yes | yes |
| request to buy | no | yes | no | no |
| add product to shelf (from inventory) | no | no | yes | yes |
| move shelf stock back to inventory | no | no | no | yes |
| view inventory | no | no | yes | yes |
| add / remove inventory stock | no | no | no | yes |
| product database (create, edit, delete) | no | no | no | yes |
| confirm / void transactions | no | no | yes | yes |
| cash balance | no | no | yes | yes |
| sale history | no | no | own only | any clerk |
| approve sign-ups | no | no | no | yes |

note: right now the code lets clerks add and delete inventory stock and move shelf stock back, phase 1 fixes this using this table

---

## 4. todo by phase

### phase 0: cleanup 
- [x] fix the bugs listed in section 2 that are one-liners (typo, amount 0, price format)

### phase 1: persistence and rules
- [ ] add `data/DataStore.java`: `loadAll()` and `saveAll()` for products, inventory, shelf (and later users, transactions)
- [ ] `Main`: load from files, fall back to `templatedata/` if files are missing
- [ ] save automatically after every change (add, move, edit, delete), keep manual save as a backup
- [ ] add `application/FarmSystem.java`: `addToShelf`, `moveToInventory`, `addToInventory`, `deleteEverywhere`
- [ ] windows call `FarmSystem` instead of touching containers directly
- [ ] tighten role checks in `SceneManager` and the scene buttons (use section 3)

### phase 2: richer products
- [ ] add `unit`, `crop`, `type` (`Harvest` / `Made`) and `madeFrom` to `Product`
- [ ] update `productsDB.txt` format and the create / edit windows
- [ ] show unit, crop and made-from on the shelf rows
- [ ] shelf: group by crop and add a filter (all / harvest / made)

### phase 3: accounts
- [ ] add `status` to `User` and a `users.txt` file with one default clerk and one default manager
- [ ] `LoginScene` and `SignUpScene` (customer active at once, clerk / manager pending)
- [ ] `SceneManager`: hold the current user, rebuild scenes on login / logout, guest by default
- [ ] header: login / logout buttons
- [ ] `SignUpRequestsScene` for managers: approve or reject pending users

### phase 4: sales and cash
- [x] `SaleTransaction` model: id, product, quantity, price, requester, status, confirmer, voider, times
  - created `application/SaleTransaction.java`
- [x] `TransactionContainer`: add, get all, get by id, get by requester
  - created `application/TransactionContainer.java`
- [x] `transactions.txt` save and load
  - created `application/TransactionStore.java`
  - `Main` loads it at start, manual save on the shelf also saves it
- [x] `FarmSystem.confirm()` takes shelf stock and `void()` returns it if it was confirmed
  - created `application/FarmSystem.java` with `requestToBuy`, `confirm` and `voidTransaction`
  - `SceneManager` creates it and shares it with `getFarmSystem()`
- [x] customer "request to buy" button on the shelf (up to shelf stock)
  - edited `ShelfScene`, new request window with a quantity spinner, shown to customers only
  - created `backend/RequestToBuyWindow.java`
- [x] `MyRequestsScene` for customers
  - created `ui/MyRequestsScene.java`, the customer's own requests and their status
  - added `showMyRequests()` to `SceneManager` and a header button for customers
- [x] `TransactionsScene` for clerk / manager: confirm and void with an alert
  - created `ui/TransactionsScene.java`, shows who, what, when, status, confirmer and voider
  - added `showTransactions()` to `SceneManager` and a header button for clerk / manager
- [x] `CashScene`: balance and entries derived from confirmed and voided transactions
  - created `ui/CashScene.java`, no new model needed
  - added `showCash()` to `SceneManager` and a header button for clerk / manager


### phase 5: dashboards and history
- [ ] `DashboardScene` with tiles by role
- [ ] `SaleHistoryScene`: transactions confirmed or voided by one clerk
- [ ] `ClerkListScene` for the manager, opens a clerk's history
- [ ] login lands on the dashboard for the role

### phase 6: polish and demo
- [ ] final css pass
- [ ] update `README.md` (features, layout, how to run)
- [ ] rehearse the demo run below

---

## 5. demo checklist (definition of done)

1. open app as guest and browse the shelf
2. sign up as a customer and log in
3. request to buy a product
4. log in as the default clerk and confirm it
5. check that shelf stock and cash balance changed
6. log in as the manager and void it
7. check that stock came back and cash was reversed
8. close and reopen the app, all data is still there