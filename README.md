<p align="center">
  <img src="docs/banner.svg" alt="MngExpense — track every rupee, beautifully" width="100%"/>
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.4-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/></a>
  <a href="https://developer.android.com/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/></a>
  <img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 7.0+"/>
  <img src="https://img.shields.io/badge/Room-SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room"/>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-F7B93E?style=for-the-badge" alt="MIT License"/></a>
</p>

<p align="center">
  <b>A clean, offline-first expense tracker for Android.</b><br/>
  Log spending in seconds, see where your money goes, and export polished reports to <b>PDF</b> or <b>Excel</b>.
</p>

<p align="center">
  <a href="#-screenshots">Screenshots</a> •
  <a href="#-features">Features</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

---

## 📸 Screenshots

<p align="center">
  <img src="docs/screenshots/dashboard.png" width="23%" alt="Dashboard"/>
  &nbsp;
  <img src="docs/screenshots/dashboard_charts.png" width="23%" alt="Charts and recent expenses"/>
  &nbsp;
  <img src="docs/screenshots/add_expense.png" width="23%" alt="Add expense"/>
  &nbsp;
  <img src="docs/screenshots/dark_dashboard.png" width="23%" alt="Dark mode"/>
</p>

<details>
<summary><b>📅 Day · Week · Month · Year views</b></summary>
<br/>
<p align="center">
  <img src="docs/screenshots/week.png" width="23%" alt="Week view"/>
  &nbsp;
  <img src="docs/screenshots/week_bars.png" width="23%" alt="Daily spending chart"/>
  &nbsp;
  <img src="docs/screenshots/year.png" width="23%" alt="Year view"/>
  &nbsp;
  <img src="docs/screenshots/year_bars.png" width="23%" alt="Monthly spending chart"/>
</p>
</details>

<details>
<summary><b>✏️ Add, edit & delete</b></summary>
<br/>
<p align="center">
  <img src="docs/screenshots/add_expense.png" width="23%" alt="Add expense form"/>
  &nbsp;
  <img src="docs/screenshots/date_picker.png" width="23%" alt="Date picker"/>
  &nbsp;
  <img src="docs/screenshots/edit_expense.png" width="23%" alt="Edit expense"/>
  &nbsp;
  <img src="docs/screenshots/delete_expense.png" width="23%" alt="Delete confirmation"/>
</p>
</details>

<h3 align="center">🧩 Home-screen widget</h3>
<p align="center">
  <img src="docs/screenshots/widget_pie_light.png" width="40%" alt="Widget: pie tab (light)"/>
  &nbsp;&nbsp;
  <img src="docs/screenshots/widget_bar_light.png" width="40%" alt="Widget: bar tab (light)"/>
  <br/><br/>
  <img src="docs/screenshots/widget_pie_dark.png" width="40%" alt="Widget: pie tab (dark)"/>
  &nbsp;&nbsp;
  <img src="docs/screenshots/widget_bar_dark.png" width="40%" alt="Widget: bar tab (dark)"/>
</p>

<details>
<summary><b>🏠 Widget on the home screen & one-tap add</b></summary>
<br/>
<p align="center">
  <img src="docs/screenshots/widget_home.png" width="23%" alt="Widget on the home screen"/>
  &nbsp;
  <img src="docs/screenshots/widget_add_dialog.png" width="23%" alt="Add widget dialog"/>
  &nbsp;
  <img src="docs/screenshots/settings.png" width="23%" alt="Settings: Add tile and Add widget"/>
</p>
</details>

<details open>
<summary><b>⚡ Quick add from anywhere: Quick Settings tile & app shortcut</b></summary>
<br/>
<p align="center">
  <img src="docs/screenshots/quick_tile.png" width="23%" alt="Add expense tile in Quick Settings"/>
  &nbsp;
  <img src="docs/screenshots/add_tile_dialog.png" width="23%" alt="One-tap Add tile dialog"/>
  &nbsp;
  <img src="docs/screenshots/app_shortcut.png" width="23%" alt="Long-press the app icon for Add expense"/>
  &nbsp;
  <img src="docs/screenshots/pinned_shortcut.png" width="23%" alt="Add expense pinned to the home screen"/>
</p>
</details>

<details>
<summary><b>📄 Reports: PDF & Excel export</b></summary>
<br/>
<p align="center">
  <img src="docs/screenshots/dashboard_report.png" width="23%" alt="Generate report"/>
  &nbsp;
  <img src="docs/screenshots/export_saved.png" width="23%" alt="Report saved"/>
  &nbsp;
  <img src="docs/screenshots/pdf_report.png" width="36%" alt="Generated PDF report"/>
</p>
</details>

<details>
<summary><b>🌙 Dark mode & ⚙️ Settings</b></summary>
<br/>
<p align="center">
  <img src="docs/screenshots/dark_dashboard.png" width="23%" alt="Dark dashboard"/>
  &nbsp;
  <img src="docs/screenshots/dark_year.png" width="23%" alt="Dark year view"/>
  &nbsp;
  <img src="docs/screenshots/dark_add_expense.png" width="23%" alt="Dark add expense"/>
  &nbsp;
  <img src="docs/screenshots/settings.png" width="23%" alt="Shake settings"/>
</p>
</details>

---

## ✨ Features

<table>
<tr>
<td width="50%" valign="top">

### 💰 Track
- Add, edit and delete expenses in a few taps
- **7 categories**: Food, Transport, Shopping, Bills, Entertainment, Health, Other
- Date picker that blocks future dates
- Optional notes on every expense
- Input checks for title and amount
- 🔁 **Recurring expenses**: rent, EMIs, subscriptions are added automatically

</td>
<td width="50%" valign="top">

### 📊 Understand
- **Day / Week / Month / Year** filters
- Step back and forward through any period
- Animated **donut chart** by category
- Animated **bar chart**: by category, day, week or month
- Total spent, transaction count, daily average
- **% change vs the previous period** ▲▼

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 📄 Export
- **PDF**: A4, multi-page, summary + full table, page numbers
- **Excel (.xlsx)**: *Expenses* sheet with a live `SUM` total + *Summary* sheet by category
- Save anywhere through the system file picker. **No storage permission needed.**
- Open the file right away from the "Report saved" snackbar

</td>
<td width="50%" valign="top">

### 🎨 Delight
- **Material You** dynamic colour on Android 12+
- Full **dark mode**
- Indian number formatting (`₹2,36,750`)
- **100% offline**: your data never leaves the device
- **Quick Settings tile**: tap *Add expense* in the notification shade, with today's total shown on the tile
- **App shortcut**: long-press the app icon → *Add expense*, or pin it as its own home-screen icon
- **Home-screen widget**: today's spending as a **pie or bar chart** (switch with tabs), plus an *Add expense* button
- Optional **shake to open** the app, even from the lock screen

</td>
</tr>
</table>

---

## 🏗 Architecture

MngExpense follows **MVVM** with a single source of truth in Room. The UI observes `StateFlow`s, so every chart and total updates the moment an expense changes.

```mermaid
flowchart LR
    subgraph UI["🎨 UI · Jetpack Compose"]
        D[DashboardScreen]
        E[ExpenseEditScreen]
        S[ShakeSettingsScreen]
    end
    T[⚡ QuickAddTileService] -- opens --> E
    SC[📲 App shortcut] -- opens --> E
    W[🧩 TodayWidget · Glance] -- opens --> E
    R -. "on change: updateAll()" .-> W
    subgraph VM["🧠 ViewModels"]
        DVM[DashboardViewModel]
        EVM[ExpenseEditViewModel]
    end
    subgraph DATA["💾 Data"]
        R[ExpenseRepository]
        DAO[(Room · ExpenseDao)]
        X[ReportExporter<br/>PDF · XLSX]
    end
    P[Period<br/>date-range logic]

    D -- events --> DVM
    DVM -- StateFlow --> D
    E <--> EVM
    DVM --> R
    EVM --> R
    DVM --> X
    X --> R
    R --> DAO
    DVM -. uses .-> P
```

**Key design choices**
- 📆 **Dates are stored as epoch days**, not timestamps, so totals never shift with time zones.
- 🧮 **`Period`** is plain Kotlin (no Android types). It works out the date range, labels and chart buckets for each filter, and it's fully unit-tested.
- 📦 **No heavy libraries for reports.** PDFs use Android's built-in `PdfDocument`, and `.xlsx` files come from a small hand-written Office Open XML writer, which keeps the APK small.
- 🧭 **Navigation 3** with per-screen `ViewModelStore`s, so each screen gets fresh state.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.4 |
| **UI** | Jetpack Compose · Material 3 · Material Icons |
| **Architecture** | MVVM · Repository · `StateFlow` · unidirectional data flow |
| **Navigation** | AndroidX Navigation 3 |
| **Widget** | Jetpack Glance 1.2 (Material 3 dynamic colours) |
| **Database** | Room 2.8 (KSP) with migrations |
| **Background work** | WorkManager (daily recurring-expense job) |
| **Async** | Kotlin Coroutines & Flow |
| **Dates** | `java.time` + core library desugaring (works on API 24+) |
| **Reports** | `android.graphics.pdf.PdfDocument` · custom XLSX writer |
| **Build** | Gradle Kotlin DSL · Version Catalog · AGP 9 · R8 |
| **Testing** | JUnit 4 |

---

## 📂 Project Structure

```text
app/src/main/java/com/guruprasad/mngexpense/
├── MainActivity.kt              # Entry point + Navigation 3 graph
├── MngExpenseApp.kt             # Application: simple dependency container
├── Routes.kt                    # Type-safe navigation keys
│
├── domain/                      # Pure Kotlin, no Android types
│   ├── Expense.kt
│   ├── Category.kt
│   ├── Period.kt                # Date ranges, labels, chart buckets
│   └── RecurringExpense.kt      # Repeat schedules, catch-up, rescheduling
│
├── data/
│   ├── local/                   # Room: entities, DAOs, database + migrations
│   ├── repository/              # ExpenseRepository, RecurringRepository
│   ├── recurring/               # Daily WorkManager job + "added" notification
│   ├── export/                  # PdfWriter, XlsxWriter, ReportExporter
│   ├── widget/                  # Home-screen widget (Glance) + chart bitmaps
│   ├── tile/                    # Quick Settings "Add expense" tile
│   ├── service/                 # Shake detection (foreground service)
│   └── receivers/               # Restart shake service after reboot
│
├── ui/
│   ├── screens/
│   │   ├── dashboard/           # Dashboard UI + ViewModel
│   │   ├── expense/             # Add / Edit UI + ViewModel
│   │   ├── recurring/           # Recurring list + Add / Edit
│   │   └── setting/             # Shake-to-open settings
│   ├── common/                  # Category colours, date picker, relative dates
│   └── theme/                   # Material 3 theme
│
└── util/                        # Currency formatting, preferences
```

---

## 🚀 Getting Started

### Requirements
- **Android Studio**, latest stable
- **JDK 17+**
- A device or emulator running **Android 7.0 (API 24)** or newer

### Build & run
```bash
git clone https://github.com/GuruDev1736/MngExpense.git
cd MngExpense
./gradlew installDebug
```

### Run the tests
```bash
./gradlew testDebugUnitTest
```

Tests cover the period logic (week boundaries, month/year rollover, labels, chart buckets), currency formatting, and the XLSX writer (valid package, XML escaping, Excel date serials, `SUM` formula).

---

## 🧩 Home-screen widget

**Add it:** open the app → ⚙️ **Settings** → **Add widget**, or long-press your home screen → **Widgets** → **MngExpense** → *Today's spending*.

| | |
|---|---|
| **Pie / Bar tabs** | Switch how today's spending is shown. Each widget remembers its own choice. |
| **+ Add expense** | Opens the add form directly |
| **Header or chart** | Opens the full dashboard |
| **Live** | Updates as soon as you add, edit or delete an expense |
| **Resizable** | Charts are re-drawn for the exact size, so they stay sharp |
| **Themed** | Material You colours, light & dark mode |

> Widgets can't run Compose `Canvas`, so the bars and donut are drawn into bitmaps with `android.graphics`. All text (values, labels, legend) is real widget text, so it follows light/dark mode.

---

## ⚡ Quick add: tile & app shortcut

### App shortcut
Long-press the **MngExpense** icon → tap **Add expense**. Tap the **⊕** next to it to pin *Add expense* as its own home-screen icon.

### Quick Settings tile

1. Open the app → ⚙️ **Settings** → **Add tile** (Android 13+), and confirm the system dialog.<br/>
   On older Android versions: swipe down twice, tap ✎ **Edit**, and drag **Add expense** into your tiles.
2. From anywhere, swipe down and tap **Add expense**. The app opens straight on the add form.
3. On the lock screen, the system asks for your PIN or fingerprint first, so your expenses stay private.

The tile's subtitle shows **today's total** (Android 10+) and refreshes every time you open the panel.

---

## 🔁 Recurring expenses

Set up a cost that repeats **once**, and MngExpense adds it for you on the right day.

- **Create one:** tap the 🔁 icon on the dashboard → **Add recurring**, or turn on **Repeat** while adding a normal expense.
- **Schedules:** daily, weekly, monthly or yearly, every *N* days / weeks / months / years, with an optional end date (great for loan EMIs). A rule on the 31st falls on the last day of shorter months.
- **Never misses a payment:** a daily background job adds what is due just after midnight and shows a quiet notification. If the phone was off, everything missed is added the next time the app runs. Each date is added exactly once.
- **Overview:** the list shows each rule's next date and your **fixed costs per month**. The dashboard shows what is **coming up this week**, and auto-added expenses carry a 🔁 mark.
- **Change anytime:** pause and resume (paused months are skipped), edit the amount or the next payment date, or delete a rule. Expenses that were already added always stay in your history.

---

## 🔐 Permissions

Every permission is optional. Apart from notifications, they are all for the **shake-to-open** feature only. Expense tracking, recurring expenses and exports need **none** (recurring expenses still work with notifications off; you just aren't told).

| Permission | Why |
|---|---|
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps shake detection running in the background |
| `POST_NOTIFICATIONS` | Shows the "Shake to open is on" notification, and tells you when recurring expenses were added |
| `SYSTEM_ALERT_WINDOW` | Lets the app open itself after a shake |
| `USE_FULL_SCREEN_INTENT` | Fallback to open over the lock screen |
| `RECEIVE_BOOT_COMPLETED` | Restarts shake detection after a reboot (only if you turned it on) |

> [!NOTE]
> Shake-to-open is **off by default**. Reports are saved through Android's Storage Access Framework, so the app never asks for storage access.

---

## 🗺 Roadmap

- [x] Add / edit / delete expenses
- [x] Day, week, month and year dashboards with charts
- [x] PDF & Excel export
- [x] Dark mode & dynamic colour
- [ ] 🎯 Monthly budgets with alerts
- [ ] 🔍 Search & filter transactions
- [x] ⚡ Quick Settings tile for one-tap entry
- [x] 📲 App shortcut (long-press the icon)
- [x] 🧩 Home-screen widget with Pie / Bar tabs
- [x] 🔁 Recurring expenses
- [ ] ☁️ Backup & restore
- [ ] 💱 Multiple currencies

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m "Add amazing feature"`
4. Push the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

Found a bug or have an idea? [Open an issue](https://github.com/GuruDev1736/MngExpense/issues).

---

## 📄 License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for details.

<p align="center">
  <br/>
  Made with ❤️ and Jetpack Compose<br/>
  <sub>If you find this project useful, please consider giving it a ⭐</sub>
</p>
