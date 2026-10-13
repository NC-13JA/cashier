# Cashier — Offline Android POS

A lightweight Android cashier app built in Java with local storage and an automatic GitHub Actions APK build.

## Current features
- Touch-friendly sales screen with product categories and quick-add tiles
- Dark mode
- Local/offline product, sales, and cash movement storage
- Stock editing and low-stock warnings
- Camera barcode scanner and external keyboard barcode scanners
- Cart quantities, percentage discounts, cash/card checkout, receipt summary
- Cash deposits/withdrawals, opening float, shift cash reconciliation
- Daily/7-day reports, top-selling products, and sale refunds

## Build
Every push to `main` builds a debug APK. Open **Actions → Build Android APK → latest successful run → Artifacts → cashier-debug-apk**. Download the ZIP and extract `app-debug.apk`.

Data is stored locally on the device. This first version does not sync data across devices or provide authenticated staff accounts.
