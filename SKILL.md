# expenseIt Developer Skill Guide 🧠📖

This guide serves as a knowledge base and instruction sheet for any developer or AI assistant working on the **expenseIt** codebase.

---

## 🧮 Core Domain Rules

### 1. Currency Unit System (Minor Units / Paise)
* **Rule**: To avoid floating-point accuracy issues, all financial amounts in the database are stored as **Long integers representing minor currency units (paise)** (i.e., `1 INR = 100 Paise`).
* **Formatting**: Always use `MoneyFormatter.formatINR(minorValue)` to display amounts to the user.
* **Input Parsing**: Convert user-entered double values back to paise using `MoneyFormatter.rupeesToMinor(rupeesString)`.

### 2. Personal Split Tracking
* **Rule**: When a group expense is created where the current user has a share, the app automatically inserts a corresponding personal transaction inside the personal tracker database.
* **Naming Convention**: 
  * The ID of this personal transaction must be prefixed with `"split_"` + the `ExpenseSplitEntity.id`.
  * The merchant name of this personal transaction is set to the name of the group.
  * The description of this personal transaction is prefixed with `"Share: "`.
* **Clean-up Hook**: When deleting or updating a group expense, any personal transaction with an ID starting with `"split_"` referencing that split ID must be automatically cleaned up to maintain synchronization.

### 3. Split Algorithms
* **Equal Split**: 
  * Divide `totalAmount` by `memberCount` using integer division to get the base share.
  * Compute `remainder = totalAmount - (baseShare * memberCount)`.
  * Add the remainder to the first member's share so that the sum of all shares matches the total amount exactly down to the last paise.

---

## 🎨 UI & Styling System

* **Theme**: Strict minimalist black-and-white (monochromatic) theme.
* **Palette**: High-contrast, pure grayscale backgrounds with white card elements and subtle outline borders.
* **Interactive Elements**:
  * Utilize smooth transitions and hover micro-animations.
  * Keep components responsive and clean.
* **Icons**: Standard Material Design icons with primary and error tints.

---

## 🛠️ Code Conventions & File Locations

* **Screens**: Location `feature/*/ui/`
  * Always use Compose state hoisted inside ViewModels.
* **ViewModels**: Location `feature/*/ui/`
  * Interact only with Repositories inside Kotlin Coroutines (`viewModelScope`).
* **Database & DAOs**: Location `core/database/`
* **Models**: Location `core/model/`
