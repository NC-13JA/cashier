# Cashier — Offline Android POS

A lightweight Android cashier app built in Java with local storage and an automatic GitHub Actions APK build.

## Current features
- Touch-friendly sales screen, dynamic product categories, product search, and quick-add tiles
- Arabic (RTL), French, English, and Japanese language switching; dark mode
- Offline product, sales, cash movement, and stock movement history
- Configurable low-stock threshold and duplicate-barcode validation
- Camera barcode scanner (front/rear camera) and external keyboard barcode scanners
- Cart quantities, percentage discounts, cash/card checkout, receipt sharing and Android print dialog
- Cash deposits/withdrawals, opening float, shift cash reconciliation, and corrected cash-refund accounting
- Daily/7-day reports, best-selling products, full-sale refunds, optional purchase cost and gross-profit reporting for sales with known costs
- Offline Excel `.xlsx` inventory import with preview, plus inventory and sales CSV exports
- JSON backup and restore for products, sales, cash movements, settings, and stock history

## Build
Every push to `main` builds a debug APK. Open **Actions → latest successful run → Artifacts → cashier-debug-apk**. Download the ZIP and extract `app-debug.apk`.

## Data and privacy
The app stores data locally and works offline. Back up data before uninstalling or changing devices. Restoring a backup replaces the current local dataset after confirmation. Cloud synchronization and authenticated staff accounts are not included.
