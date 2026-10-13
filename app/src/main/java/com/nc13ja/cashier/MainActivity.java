package com.nc13ja.cashier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.content.Intent;
import android.net.Uri;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebView;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private final ArrayList<Product> products = new ArrayList<>();
    private final ArrayList<CartLine> cart = new ArrayList<>();
    private final ArrayList<Sale> sales = new ArrayList<>();
    private final ArrayList<CashMove> cashMoves = new ArrayList<>();
    private final ArrayList<StockMove> stockMoves = new ArrayList<>();
    private LinearLayout root, body, nav;
    private String page = "Vente", category = "Tout";
    private boolean dark = false;
    private String language = "fr";
    private int lowStockThreshold = 3;
    private String westernDigits(String s) {
        if (s == null) return null;
        return s.replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4')
                .replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9')
                .replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4')
                .replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9');
    }
    private String tr(String s) {
        if (s == null || language.equals("fr")) return s;
        String[][] words = {
            {"Language","اللغة","Language","言語"},
            {"Settings","الإعدادات","Settings","設定"},
            {"Dark mode","الوضع الداكن","Dark mode","ダークモード"},
            {"Light mode","الوضع الفاتح","Light mode","ライトモード"},
            {"Test barcode scanner","اختبار ماسح الباركود","Test barcode scanner","バーコードスキャナーをテスト"},
            {"About Cashier","حول الكاشير","About Cashier","Cashierについて"},
            {"Diagnostic log / share","سجل التشخيص / مشاركة","Diagnostic log / share","診断ログ / 共有"},
            {"Camera permission may be needed","قد يحتاج التطبيق إلى إذن الكاميرا","Camera permission may be needed","カメラの許可が必要な場合があります"},
            {"No barcode was returned","لم يتم إرجاع باركود","No barcode was returned","バーコードが読み取られませんでした"},
            {"Cashier works offline and stores data on this device.","يعمل الكاشير دون إنترنت ويحفظ البيانات على هذا الجهاز.","Cashier works offline and stores data on this device.","Cashierはオフラインで動作し、この端末にデータを保存します。"},
            {"Nouvelle vente","بيع جديد","New sale","新規販売"},
            {"Stock & produits","المخزون والمنتجات","Stock & products","在庫・商品"},
            {"Caisse & clôture","الصندوق والإغلاق","Cash register & closing","レジ・締め処理"},
            {"Rapports & statistiques","التقارير والإحصائيات","Reports & statistics","レポート・統計"},
            {"Vente","بيع","Sale","販売"},{"Stock","المخزون","Stock","在庫"},{"Caisse","الصندوق","Cash","レジ"},{"Rapports","التقارير","Reports","レポート"},
            {"Tout","الكل","All","すべて"},{"Boissons","مشروبات","Drinks","飲料"},{"Snacks","وجبات خفيفة","Snacks","スナック"},{"Épicerie","مواد غذائية","Groceries","食料品"},{"Divers","متنوع","Miscellaneous","その他"},
            {"Produits rapides","المنتجات السريعة","Quick products","クイック商品"},
            {"Nom ou code-barres…","اسم المنتج أو الباركود…","Name or barcode…","商品名またはバーコード…"},
            {"Scanner","مسح","Scan","スキャン"},{"Scan barcode","مسح الباركود","Scan barcode","バーコードをスキャン"},{"Rechercher / ajouter","بحث / إضافة","Search / add","検索 / 追加"},
            {"Scannez le code-barres","امسح الباركود","Scan the barcode","バーコードをスキャン"},
            {"Scanner indisponible","الماسح غير متاح","Scanner unavailable","スキャナーを利用できません"},
            {"Panier","السلة","Cart","カート"},{"Le panier est vide. Touchez un produit pour commencer.","السلة فارغة. اضغط على منتج للبدء.","The cart is empty. Tap a product to begin.","カートは空です。商品をタップして開始します。"},
            {"Remise %","خصم %","Discount %","割引 %"},{"TOTAL  ","الإجمالي  ","TOTAL  ","合計  "},
            {"Encaisser en espèces","الدفع نقدًا","Pay by cash","現金で会計"},{"Encaisser par carte","الدفع بالبطاقة","Pay by card","カードで会計"},
            {"Vider le panier","إفراغ السلة","Clear cart","カートを空にする"},{"Stock insuffisant","المخزون غير كافٍ","Insufficient stock","在庫不足"},
            {"Produit introuvable","المنتج غير موجود","Product not found","商品が見つかりません"},
            {"Rupture de stock","نفد المخزون","Out of stock","在庫切れ"},{"Panier vide","السلة فارغة","Cart is empty","カートは空です"},
            {"Vente enregistrée","تم تسجيل البيع","Sale saved","販売を保存しました"},{"Remise : ","الخصم: ","Discount: ","割引: "},
            {"Paiement : ","طريقة الدفع: ","Payment: ","支払い方法: "},{"Merci !","شكرًا!","Thank you!","ありがとうございます！"},
            {"Remise rapide","خصم سريع","Quick discount","クイック割引"},{"Remise en % (0 à 100)","الخصم بالنسبة المئوية (0 إلى 100)","Discount % (0 to 100)","割引率（0～100）"},
            {"Annuler","إلغاء","Cancel","キャンセル"},{"Appliquer","تطبيق","Apply","適用"},
            {"＋ Ajouter un produit","＋ إضافة منتج","＋ Add product","＋ 商品を追加"},
            {"Scanner un code-barres pour rechercher","امسح باركود للبحث","Scan a barcode to search","バーコードをスキャンして検索"},
            {"Scannez un produit","امسح منتجًا","Scan a product","商品をスキャン"},
            {"produit(s) à stock faible (≤ 3)","منتج بمخزون منخفض (≤ 3)","product(s) low in stock (≤ 3)","在庫が少ない商品（≤ 3）"},
            {"Modifier","تعديل","Edit","編集"},{"Supprimer","حذف","Delete","削除"},
            {"Nouveau produit","منتج جديد","New product","新しい商品"},{"Modifier le produit","تعديل المنتج","Edit product","商品を編集"},
            {"Nom du produit","اسم المنتج","Product name","商品名"},{"Catégorie","الفئة","Category","カテゴリ"},{"Code-barres","الباركود","Barcode","バーコード"},
            {"Prix en DA","السعر بالدينار","Price in DA","価格（DA）"},{"Quantité en stock","الكمية في المخزون","Stock quantity","在庫数"},
            {"Enregistrer","حفظ","Save","保存"},{"Vérifiez le nom, le prix et le stock","تحقق من الاسم والسعر والمخزون","Check the name, price and stock","商品名、価格、在庫を確認してください"},
            {"Fond de caisse initial","الرصيد النقدي الافتتاحي","Opening cash","開始時の現金"},
            {"Ventes du jour","مبيعات اليوم","Today's sales","本日の売上"},{"Ventes espèces","المبيعات النقدية","Cash sales","現金売上"},
            {"Ventes carte","مبيعات البطاقة","Card sales","カード売上"},{"＋ Apport de caisse","＋ إيداع في الصندوق","＋ Cash deposit","＋ 現金入金"},
            {"− Dépense / retrait","− مصروف / سحب","− Expense / withdrawal","− 支出・引き出し"},
            {"Définir le fond de caisse","تحديد الرصيد الافتتاحي","Set opening cash","開始時の現金を設定"},
            {"Clôturer la caisse / compter l'espèce","إغلاق الصندوق / عدّ النقد","Close register / count cash","レジ締め・現金を数える"},
            {"Pièces de caisse récentes","حركات الصندوق الأخيرة","Recent cash movements","最近の現金移動"},
            {"Montant en DA","المبلغ بالدينار","Amount in DA","金額（DA）"},{"Motif / référence","السبب / المرجع","Reason / reference","理由・参照"},
            {"Montant invalide","مبلغ غير صالح","Invalid amount","無効な金額"},{"Fond de caisse en DA","الرصيد الافتتاحي بالدينار","Opening cash in DA","開始現金（DA）"},
            {"Clôture de caisse","إغلاق الصندوق","Register closing","レジ締め"},{"Espèces réellement comptées (DA)","النقد المعدود فعليًا (DA)","Actual cash counted (DA)","実際に数えた現金（DA）"},
            {"Espèces théoriques : ","النقد المتوقع: ","Expected cash: ","想定現金: "},{"Comparer","مقارنة","Compare","比較"},
            {"Résultat de clôture","نتيجة الإغلاق","Closing result","締め結果"},{"Théorique : ","المتوقع: ","Expected: ","想定: "},
            {"Compté : ","المعدود: ","Counted: ","実数: "},{"Écart : ","الفرق: ","Difference: ","差額: "},
            {"Rapports & statistiques","التقارير والإحصائيات","Reports & statistics","レポート・統計"},
            {"Ventes aujourd'hui","مبيعات اليوم","Sales today","本日の売上"},{"Ventes sur 7 jours","مبيعات آخر 7 أيام","Sales over 7 days","過去7日間の売上"},
            {"Transactions du jour","عمليات اليوم","Today's transactions","本日の取引"},{"Unités en stock","الوحدات في المخزون","Units in stock","在庫数"},
            {"Valeur estimée du stock","القيمة التقديرية للمخزون","Estimated stock value","在庫の推定額"},
            {"Produits les plus vendus","المنتجات الأكثر مبيعًا","Best-selling products","売れ筋商品"},
            {"Aucune vente enregistrée pour le moment.","لا توجد مبيعات مسجلة حاليًا.","No sales recorded yet.","まだ販売記録はありません。"},
            {"Dernières transactions","آخر المعاملات","Latest transactions","最近の取引"},{"Rembourser cette vente","استرداد قيمة هذا البيع","Refund this sale","この販売を返金"},
            {"Confirmer le remboursement","تأكيد الاسترداد","Confirm refund","返金を確認"},{"Rembourser ","استرداد ","Refund ","返金 "},
            {" et remettre les produits en stock ?"," وإعادة المنتجات إلى المخزون؟"," and return the products to stock?","して商品を在庫に戻しますか？"},
            {"Remboursement enregistré","تم تسجيل الاسترداد","Refund recorded","返金を記録しました"},
            {"Code-barres scanné","تم مسح الباركود","Barcode scanned","バーコードをスキャンしました"},
            {"Produit inconnu. Voulez-vous créer le produit ?","منتج غير معروف. هل تريد إنشاءه؟","Unknown product. Create it?","不明な商品です。作成しますか？"},
            {"Créer","إنشاء","Create","作成"},{"Produit ","منتج ","Product ","商品 "},
            {"Stock : ","المخزون: ","Stock: ","在庫: "},{"Code : ","الرمز: ","Code: ","コード: "},
            {" · Stock : "," · المخزون: "," · Stock: "," · 在庫: "},
            {"Vérifiez le nom, le prix et le stock","تحقق من الاسم والسعر والمخزون","Check name, price and stock","商品名、価格、在庫を確認してください"},
            {"Espèces","نقدًا","Cash","現金"},{"Carte","بطاقة","Card","カード"},
            {"Apport","إيداع","Deposit","入金"},{"Sortie","سحب","Withdrawal","出金"},
            {"Clôture","إغلاق","Closing","締め"},{"Remboursement","استرداد","Refund","返金"},
            {"Importer un fichier Excel (.xlsx)","استيراد ملف Excel (.xlsx)","Import Excel file (.xlsx)","Excelファイルを読み込む (.xlsx)"},
            {"Choisir le fichier Excel","اختر ملف Excel","Choose Excel file","Excelファイルを選択"},
            {"Aperçu de l'importation","معاينة الاستيراد","Import preview","インポートのプレビュー"},
            {"Importer","استيراد","Import","読み込む"},
            {"Import terminé","اكتمل الاستيراد","Import complete","インポート完了"},
            {"Aucune ligne valide n'a été trouvée.","لم يتم العثور على صفوف صالحة.","No valid rows were found.","有効な行が見つかりません。"},
            {"Fichier Excel illisible ou colonnes manquantes.","تعذر قراءة ملف Excel أو أن الأعمدة المطلوبة غير موجودة.","Excel file unreadable or required columns are missing.","Excelファイルを読み取れないか、必要な列がありません。"},
            {"Export backup","تصدير نسخة احتياطية","Export backup","バックアップをエクスポート"},
            {"Restore backup","استعادة نسخة احتياطية","Restore backup","バックアップを復元"},
            {"Export sales CSV","تصدير المبيعات CSV","Export sales CSV","売上CSVをエクスポート"},
            {"Low stock alert limit","حد تنبيه المخزون المنخفض","Low stock alert limit","在庫不足の警告しきい値"},
            {"Stock movement history","سجل حركة المخزون","Stock movement history","在庫変動履歴"},
            {"Backup created","تم إنشاء النسخة الاحتياطية","Backup created","バックアップを作成しました"},
            {"Restore data from this backup? Existing data will be replaced.","استعادة بيانات هذه النسخة؟ سيتم استبدال البيانات الحالية.","Restore data from this backup? Existing data will be replaced.","このバックアップから復元しますか？現在のデータは置き換えられます。"},
            {"Invalid or incomplete backup file.","ملف النسخة الاحتياطية غير صالح أو غير مكتمل.","Invalid or incomplete backup file.","バックアップファイルが無効または不完全です。"},
            {"Purchase cost in DA (optional)","سعر الشراء بالدينار (اختياري)","Purchase cost in DA (optional)","仕入価格（DA・任意）"},
            {"Gross profit (known costs)","الربح الإجمالي (للتكاليف المعروفة)","Gross profit (known costs)","粗利益（原価登録済み）"},
            {"Sales with cost data","مبيعات ذات تكلفة مسجلة","Sales with cost data","原価登録済みの売上"},
            {"Print receipt","طباعة الفاتورة","Print receipt","レシートを印刷"},
            {"Share receipt","مشاركة الفاتورة","Share receipt","レシートを共有"},
            {"Export inventory CSV","تصدير المخزون CSV","Export inventory CSV","在庫CSVをエクスポート"},
            {"Search product name or barcode","ابحث باسم المنتج أو الباركود","Search product name or barcode","商品名またはバーコードを検索"},
            {"No products yet. Add a product or import your Excel file.","لا توجد منتجات بعد. أضف منتجًا أو استورد ملف Excel.","No products yet. Add a product or import your Excel file.","商品がありません。商品を追加するか、Excelファイルを読み込んでください。"},
            {"Replace stock from file","استبدال الكمية بكمية الملف","Replace stock from file","ファイルの在庫数で置き換え"},
            {"Add quantities to current stock","إضافة الكميات إلى المخزون الحالي","Add quantities to current stock","現在の在庫に数量を加算"},
            {"Duplicate rows skipped","الصفوف المكررة التي تم تخطيها","Duplicate rows skipped","スキップした重複行"},
            {"اختر طريقة التعامل مع الكميات الموجودة: استبدالها بما في الملف أو إضافة كميات الملف إليها.","اختر طريقة التعامل مع الكميات الموجودة: استبدالها بما في الملف أو إضافة كميات الملف إليها.","Choose how to handle existing quantities: replace them with file quantities or add file quantities to them.","既存数量の処理方法を選択してください：ファイルの数量で置換するか、加算します。"},
            {"New products: ","سلع جديدة: ","New products: ","新規商品: "},
            {"Updated products: ","سلع تم تحديثها: ","Updated products: ","更新商品: "},
            {"Duplicate rows skipped: ","الصفوف المكررة المتخطاة: ","Duplicate rows skipped: ","スキップした重複行: "},
            {"Quantities skipped due to overflow: ","الكميات التي تعذر جمعها بسبب تجاوز الحد: ","Quantities skipped due to overflow: ","上限超過で加算できなかった数量: "},
            {"Achat : ","الشراء: ","Cost: ","仕入: "},
            {"Marge : ","الهامش: ","Margin: ","利益幅: "},
            {"Existing barcodes were preserved.","تم الاحتفاظ بالباركودات الموجودة.","Existing barcodes were preserved.","既存のバーコードは保持されました。"},
            {"عدد السلع: ","عدد السلع: ","Products: ","商品数: "},
            {"   الكمية: ","   الكمية: ","   Quantity: ","   数量: "},
            {"  |  السعر: ","  |  السعر: ","  |  Price: ","  |  価格: "},
            {"... و ","... و ","... and ","... 他 "},
            {"سيتم تحديث السعر والكمية للسلع المطابقة بالاسم مع الاحتفاظ بالباركود الحالي. السلع الجديدة ستُضاف دون باركود. لن يتم حذف أي سلعة.","سيتم تحديث السعر والكمية للسلع المطابقة بالاسم مع الاحتفاظ بالباركود الحالي. السلع الجديدة ستُضاف دون باركود. لن يتم حذف أي سلعة.","Matching products will have their price and stock updated while keeping existing barcodes. New products will be added without a barcode. No products will be deleted.","一致する商品の価格と在庫を更新し、既存のバーコードを保持します。新しい商品はバーコードなしで追加されます。商品は削除されません。"}
        };
        int idx = language.equals("ar") ? 1 : language.equals("en") ? 2 : 3;
        String out = s;
        java.util.ArrayList<String[]> ordered = new java.util.ArrayList<>();
        for (String[] w : words) ordered.add(w);
        ordered.sort((a,b)->Integer.compare(b[0].length(),a[0].length()));
        for (String[] w : ordered) if (!w[0].isEmpty()) out = out.replace(w[0], w[idx]);
        return out;
    }
    private AlertDialog.Builder dialog() {
        return new AlertDialog.Builder(this) {
            @Override public AlertDialog.Builder setTitle(CharSequence title) { return super.setTitle(westernDigits(tr(title == null ? null : title.toString()))); }
            @Override public AlertDialog.Builder setMessage(CharSequence message) { return super.setMessage(westernDigits(tr(message == null ? null : message.toString()))); }
            @Override public AlertDialog.Builder setPositiveButton(CharSequence text, android.content.DialogInterface.OnClickListener listener) { return super.setPositiveButton(westernDigits(tr(text == null ? null : text.toString())), listener); }
            @Override public AlertDialog.Builder setNegativeButton(CharSequence text, android.content.DialogInterface.OnClickListener listener) { return super.setNegativeButton(westernDigits(tr(text == null ? null : text.toString())), listener); }
        };
    }
    private void settingsDialog() {
        String[] options = {
            tr("Language") + " / " + (language.equals("ar") ? "العربية" : language.equals("en") ? "English" : language.equals("ja") ? "日本語" : "Français"),
            tr(dark ? "Light mode" : "Dark mode"),
            tr("Test barcode scanner"),
            tr("About Cashier"),
            tr("Diagnostic log / share"),
            tr("Export backup"),
            tr("Restore backup"),
            tr("Export sales CSV"),
            tr("Low stock alert limit"),
            tr("Export inventory CSV")
        };
        dialog().setTitle(tr("Settings")).setItems(options, (d, which) -> {
            if (which == 0) chooseLanguage();
            else if (which == 1) { dark = !dark; saveData(); render(); }
            else if (which == 2) startBarcodeScan(tr("Scannez le code-barres"));
            else if (which == 3) dialog().setTitle(tr("About Cashier")).setMessage(tr("Cashier works offline and stores data on this device.")).setPositiveButton("OK", null).show();
            else if (which == 4) showDiagnosticLog();
            else if (which == 5) createBackupFile();
            else if (which == 6) chooseBackupFile();
            else if (which == 7) createSalesCsvFile();
            else if (which == 8) configureLowStockThreshold();
            else if (which == 9) createInventoryCsvFile();
        }).setNegativeButton(tr("Annuler"), null).show();
    }

    private void showDiagnosticLog() {
        CrashLogger.log("USER_ACTION", "Opened diagnostic log viewer");
        String log = CrashLogger.readLog();
        AlertDialog.Builder builder = dialog().setTitle(tr("Diagnostic log / share"))
                .setMessage(log.isEmpty() ? "No log entries yet." : log)
                .setPositiveButton("Share", (d, which) -> {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_SUBJECT, "Cashier diagnostic log");
                    share.putExtra(Intent.EXTRA_TEXT, CrashLogger.readLog());
                    try { startActivity(Intent.createChooser(share, "Share diagnostic log")); }
                    catch (Exception e) { CrashLogger.crash("Share diagnostic log failed", e); }
                })
                .setNegativeButton(tr("Annuler"), null);
        builder.show();
    }
    private static final int BARCODE_REQUEST = 7101;
    private static final int EXCEL_REQUEST = 7102;
    private static final int BACKUP_EXPORT_REQUEST = 7103;
    private static final int BACKUP_IMPORT_REQUEST = 7104;
    private static final int SALES_EXPORT_REQUEST = 7105;
    private static final int INVENTORY_EXPORT_REQUEST = 7106;
    private static class ImportRow {
        String name; int stock; double price;
        ImportRow(String name, int stock, double price) { this.name=name; this.stock=stock; this.price=price; }
    }
    private boolean scanningForProductBarcode = false;
    private EditText productBarcodeField;
    private void startBarcodeScan(String prompt) {
        CrashLogger.log("SCANNER", "Launching barcode scanner activity");
        try {
            Intent intent = new Intent(this, BarcodeScannerActivity.class);
            intent.putExtra("prompt", prompt);
            intent.putExtra("language", language);
            startActivityForResult(intent, BARCODE_REQUEST);
        } catch (Exception e) {
            CrashLogger.crash("Could not launch barcode scanner", e);
            dialog().setTitle(tr("Scanner indisponible"))
                    .setMessage(tr("Camera permission may be needed") + "\n" + e.getClass().getSimpleName())
                    .setPositiveButton("OK", null).show();
        }
    }

    private void chooseLanguage() {
        String[] names = {"العربية", "Français", "English", "日本語"};
        String[] codes = {"ar", "fr", "en", "ja"};
        int selected = 1;
        for (int i=0;i<codes.length;i++) if (codes[i].equals(language)) selected=i;
        dialog().setTitle(tr("Language")).setSingleChoiceItems(names, selected, (d, which) -> {
            language = codes[which];
            prefs.edit().putString("language", language).apply();
            d.dismiss();
            render();
        }).setNegativeButton(tr("Annuler"), null).show();
    }

    private double discountPercent = 0;
    private double openingCash = 0;
    private final int BLUE = Color.rgb(0, 150, 136);
    private int bg, panel, fg, muted, line;

    static class Product {
        String id, name, category, barcode;
        double price, costPrice;
        int stock;
        Product(String id, String name, String category, String barcode, double price, int stock) {
            this(id, name, category, barcode, price, stock, -1);
        }
        Product(String id, String name, String category, String barcode, double price, int stock, double costPrice) {
            this.id=id; this.name=name; this.category=category; this.barcode=barcode; this.price=price; this.stock=stock; this.costPrice=costPrice;
        }
    }
    static class CartLine {
        Product product; int qty;
        CartLine(Product p, int q) { product=p; qty=q; }
    }
    static class Sale {
        String id, time, payment, items, details;
        double total, discount, costTotal;
        boolean refunded;
        Sale(String id, String time, String payment, String items, double total, double discount, boolean refunded) {
            this(id, time, payment, items, total, discount, refunded, -1, "");
        }
        Sale(String id, String time, String payment, String items, double total, double discount, boolean refunded, double costTotal) {
            this(id, time, payment, items, total, discount, refunded, costTotal, "");
        }
        Sale(String id, String time, String payment, String items, double total, double discount, boolean refunded, double costTotal, String details) {
            this.id=id; this.time=time; this.payment=payment; this.items=items; this.total=total; this.discount=discount; this.refunded=refunded; this.costTotal=costTotal; this.details=details==null?"":details;
        }
    }
    static class CashMove {
        String time, type, note;
        double amount;
        CashMove(String time, String type, String note, double amount) { this.time=time; this.type=type; this.note=note; this.amount=amount; }
    }
    static class StockMove {
        String time, productName, reason;
        int delta;
        StockMove(String time, String productName, int delta, String reason) { this.time=time; this.productName=productName; this.delta=delta; this.reason=reason; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        CrashLogger.log("LIFECYCLE", "MainActivity onCreate");
        prefs = getSharedPreferences("cashier_local_data", MODE_PRIVATE);
        dark = prefs.getBoolean("dark", false);
        language = prefs.getString("language", "fr");
        openingCash = Double.longBitsToDouble(prefs.getLong("openingCash", Double.doubleToLongBits(0)));
        lowStockThreshold = Math.max(0, prefs.getInt("lowStockThreshold", 3));
        loadData();
        render();
    }

    private void colors() {
        bg = dark ? Color.rgb(17,24,39) : Color.rgb(246,248,252);
        panel = dark ? Color.rgb(31,41,55) : Color.WHITE;
        fg = dark ? Color.rgb(243,246,252) : Color.rgb(25,35,55);
        muted = dark ? Color.rgb(175,187,205) : Color.rgb(105,117,138);
        line = dark ? Color.rgb(58,70,88) : Color.rgb(226,231,240);
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private String money(double v) { return westernDigits(String.format(Locale.US, "%,.2f DA", v)); }
    private String now() { return westernDigits(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())); }
    private String today() { return westernDigits(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date())); }
    private String id() { return String.valueOf(System.currentTimeMillis()); }

    private void seedProducts() {
        products.add(new Product(id(),"Eau minérale","Boissons","613000000001",50,30));
        products.add(new Product(id(),"Jus d'orange","Boissons","613000000002",120,18));
        products.add(new Product(id(),"Chips","Snacks","613000000003",80,15));
        products.add(new Product(id(),"Biscuits","Snacks","613000000004",65,20));
        products.add(new Product(id(),"Sucre 1 kg","Épicerie","613000000005",140,12));
        saveData();
    }
    private void loadData() {
        try {
            JSONArray a = new JSONArray(prefs.getString("products","[]"));
            for (int i=0;i<a.length();i++) { JSONObject o=a.getJSONObject(i); products.add(new Product(o.optString("id",id()),o.optString("name"),o.optString("category","Divers"),o.optString("barcode",""),o.optDouble("price"),o.optInt("stock"),o.has("costPrice")?o.optDouble("costPrice",-1):-1)); }
            JSONArray s = new JSONArray(prefs.getString("sales","[]"));
            for (int i=0;i<s.length();i++) { JSONObject o=s.getJSONObject(i); sales.add(new Sale(o.optString("id"),o.optString("time"),o.optString("payment"),o.optString("items"),o.optDouble("total"),o.optDouble("discount"),o.optBoolean("refunded"),o.has("costTotal")?o.optDouble("costTotal",-1):-1,o.optString("details",""))); }
            JSONArray m = new JSONArray(prefs.getString("moves","[]"));
            for (int i=0;i<m.length();i++) { JSONObject o=m.getJSONObject(i); cashMoves.add(new CashMove(o.optString("time"),o.optString("type"),o.optString("note"),o.optDouble("amount"))); }
            JSONArray sm = new JSONArray(prefs.getString("stockMoves","[]"));
            for (int i=0;i<sm.length();i++) { JSONObject o=sm.getJSONObject(i); stockMoves.add(new StockMove(o.optString("time"),o.optString("productName"),o.optInt("delta"),o.optString("reason"))); }
        } catch(Exception ignored) { }
    }
    private void saveData() {
        try {
            JSONArray p=new JSONArray(), s=new JSONArray(), m=new JSONArray(), sm=new JSONArray();
            for(Product x:products){ JSONObject o=new JSONObject(); o.put("id",x.id);o.put("name",x.name);o.put("category",x.category);o.put("barcode",x.barcode);o.put("price",x.price);o.put("stock",x.stock);o.put("costPrice",x.costPrice);p.put(o); }
            for(Sale x:sales){ JSONObject o=new JSONObject();o.put("id",x.id);o.put("time",x.time);o.put("payment",x.payment);o.put("items",x.items);o.put("total",x.total);o.put("discount",x.discount);o.put("refunded",x.refunded);o.put("costTotal",x.costTotal);o.put("details",x.details);s.put(o); }
            for(CashMove x:cashMoves){ JSONObject o=new JSONObject();o.put("time",x.time);o.put("type",x.type);o.put("note",x.note);o.put("amount",x.amount);m.put(o); }
            for(StockMove x:stockMoves){ JSONObject o=new JSONObject();o.put("time",x.time);o.put("productName",x.productName);o.put("delta",x.delta);o.put("reason",x.reason);sm.put(o); }
            prefs.edit().putString("products",p.toString()).putString("sales",s.toString()).putString("moves",m.toString()).putString("stockMoves",sm.toString()).putLong("openingCash",Double.doubleToLongBits(openingCash)).putBoolean("dark",dark).putInt("lowStockThreshold",lowStockThreshold).apply();
        } catch(Exception e) { CrashLogger.crash("Saving local data failed", e); }
    }

    private void render() {
        colors();
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setLayoutDirection(language.equals("ar") ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR); root.setBackgroundColor(bg);
        setContentView(root);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(18),dp(12),dp(18),dp(10));
        LinearLayout brandBlock = new LinearLayout(this); brandBlock.setOrientation(LinearLayout.VERTICAL); brandBlock.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("CASHIER",20,true); brand.setLetterSpacing(0.06f); brandBlock.addView(brand);
        TextView subtitle = text("POINT DE VENTE  •  DA",10,true); subtitle.setTextColor(muted); brandBlock.addView(subtitle);
        header.addView(brandBlock,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView lang = text("🌐",22,true); lang.setGravity(Gravity.CENTER); lang.setBackground(shape(panel,16)); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(44),dp(44)); lp.leftMargin=dp(5); lp.rightMargin=dp(5); header.addView(lang,lp); lang.setOnClickListener(v->chooseLanguage());
        TextView settings = text("⚙",22,true); settings.setGravity(Gravity.CENTER); settings.setBackground(shape(panel,16)); LinearLayout.LayoutParams setp = new LinearLayout.LayoutParams(dp(44),dp(44)); setp.rightMargin=dp(5); header.addView(settings,setp); settings.setOnClickListener(v->settingsDialog());
        TextView mode = text(dark ? "☀" : "☾",24,true); mode.setGravity(Gravity.CENTER); mode.setBackground(shape(panel,16)); header.addView(mode,new LinearLayout.LayoutParams(dp(44),dp(44)));
        mode.setOnClickListener(v->{dark=!dark;saveData();render();}); root.addView(header);
        body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(14),dp(4),dp(14),dp(12));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        nav = new LinearLayout(this);nav.setPadding(dp(5),dp(6),dp(5),dp(6));nav.setBackground(shape(panel,18));
        String[] tabs={"Vente","Stock","Caisse","Rapports"};
        String[] icons={"▣","▦","◉","▤"};
        for(int i=0;i<tabs.length;i++){final String t=tabs[i];LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);item.setPadding(dp(2),dp(7),dp(2),dp(7));item.setBackground(t.equals(page)?shape(dark?Color.rgb(21,70,69):Color.rgb(220,246,242),13):shape(panel,13));
            TextView ico=text(icons[i],18,true);ico.setGravity(Gravity.CENTER);ico.setTextColor(t.equals(page)?BLUE:muted);item.addView(ico);
            TextView label=text(t,11,true);label.setGravity(Gravity.CENTER);label.setTextColor(t.equals(page)?BLUE:muted);item.addView(label);
            LinearLayout.LayoutParams itemParams=new LinearLayout.LayoutParams(0,-2,1);itemParams.setMargins(dp(3),0,dp(3),0);nav.addView(item,itemParams);item.setOnClickListener(v->{page=t;render();});}
        root.addView(nav);
        if(page.equals("Vente")) salePage(); else if(page.equals("Stock")) stockPage(); else if(page.equals("Caisse")) cashPage(); else reportPage();
    }
    private TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(westernDigits(tr(s)));t.setTextSize(size);t.setTextColor(fg);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),line);return d;}
    private LinearLayout card(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(13),dp(12),dp(13),dp(12));l.setBackground(shape(panel,16));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);body.addView(l,p);return l;}
    private TextView button(String label,Runnable action){TextView b=text(label,14,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(12),dp(13),dp(12),dp(13));b.setBackground(shape(BLUE,12));b.setTextColor(Color.WHITE);b.setOnClickListener(v->action.run());return b;}
    private void addButtonRow(LinearLayout parent,String label,Runnable action){TextView b=button(label,action);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(8);parent.addView(b,p);}
    private EditText input(String hint,int type){EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(15);e.setTextColor(fg);e.setHintTextColor(muted);e.setHint(tr(hint));e.setInputType(type);e.setPadding(dp(12),dp(8),dp(12),dp(8));e.setBackground(shape(bg,10));return e;}
    private LinearLayout row(){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setOrientation(LinearLayout.HORIZONTAL);return r;}
    private void title(String s){TextView t=text(s,20,true);t.setPadding(dp(2),dp(8),dp(2),dp(12));body.addView(t);}
    private void small(LinearLayout parent,String s){TextView t=text(s,13,false);t.setTextColor(muted);parent.addView(t);}
    private double cartTotal(){double n=0;for(CartLine c:cart)n+=c.product.price*c.qty;return n*(1-discountPercent/100.0);}
    private void salePage(){
        title("Nouvelle vente");
        LinearLayout search=card();
        LinearLayout sr=row(); EditText find=input("Nom ou code-barres…",InputType.TYPE_CLASS_TEXT);sr.addView(find,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView scan=button("Scanner",()->startBarcodeScan(tr("Scannez le code-barres")));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(105),dp(50));sp.leftMargin=dp(8);sr.addView(scan,sp);search.addView(sr);
        addButtonRow(search,"Rechercher / ajouter",()->addByCode(find.getText().toString()));
        find.setOnEditorActionListener((v,a,event)->{addByCode(find.getText().toString());return true;});
        LinearLayout cats=card(); HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout cr=row();ArrayList<String> cs=new ArrayList<>();cs.add("Tout");for(Product product:products)if(!product.category.trim().isEmpty()&&!cs.contains(product.category.trim()))cs.add(product.category.trim());if(!cs.contains(category))category="Tout";for(String c:cs){TextView b=text(c,12,true);b.setPadding(dp(12),dp(10),dp(12),dp(10));b.setTextColor(category.equals(c)?BLUE:fg);cr.addView(b);b.setOnClickListener(v->{category=c;render();});}hs.addView(cr);cats.addView(hs);
        title("Produits rapides");
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);
        int shown=0;LinearLayout gr=null;
        for(Product p:products){if(!category.equals("Tout")&&!p.category.equals(category))continue;if(shown%2==0){gr=row();grid.addView(gr,new LinearLayout.LayoutParams(-1,-2));}
            LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setPadding(dp(12),dp(12),dp(12),dp(12));tile.setBackground(shape(panel,14));
            TextView name=text(p.name,14,true);tile.addView(name);TextView price=text(money(p.price),14,true);price.setTextColor(BLUE);tile.addView(price);small(tile,"Stock : "+p.stock);
            LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1);tp.setMargins(dp(3),dp(3),dp(3),dp(6));gr.addView(tile,tp);tile.setOnClickListener(v->addToCart(p));shown++;
        }
        body.addView(grid);if(shown==0)small(body,"No products yet. Add a product or import your Excel file.");
        LinearLayout basket=card();LinearLayout r=row();r.addView(text("Panier",18,true),new LinearLayout.LayoutParams(0,-2,1));TextView disc=text("Remise %",13,true);disc.setTextColor(BLUE);r.addView(disc);disc.setOnClickListener(v->discountDialog());basket.addView(r);
        if(cart.isEmpty())small(basket,"Le panier est vide. Touchez un produit pour commencer.");
        for(CartLine c:new ArrayList<>(cart)){LinearLayout crw=row();TextView label=text(c.product.name+" × "+c.qty,14,false);crw.addView(label,new LinearLayout.LayoutParams(0,-2,1));TextView minus=text("−",24,true);minus.setGravity(Gravity.CENTER);minus.setTextColor(BLUE);crw.addView(minus,new LinearLayout.LayoutParams(dp(36),dp(42)));minus.setOnClickListener(v->{c.qty--;if(c.qty<=0)cart.remove(c);render();});TextView plus=text("+",22,true);plus.setGravity(Gravity.CENTER);plus.setTextColor(BLUE);crw.addView(plus,new LinearLayout.LayoutParams(dp(36),dp(42)));plus.setOnClickListener(v->{if(c.qty<c.product.stock)c.qty++;else toast("Stock insuffisant");render();});basket.addView(crw);}
        TextView total=text("TOTAL  "+money(cartTotal()),22,true);total.setPadding(0,dp(12),0,dp(4));basket.addView(total);
        addButtonRow(basket,"Encaisser en espèces",()->checkout("Espèces"));
        addButtonRow(basket,"Encaisser par carte",()->checkout("Carte"));
        addButtonRow(basket,"Vider le panier",()->{cart.clear();discountPercent=0;render();});
    }
    private void addByCode(String q){if(q.trim().isEmpty())return;for(Product p:products)if(p.name.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))||p.barcode.equals(q.trim())){addToCart(p);return;}toast("Produit introuvable");}
    private void addToCart(Product p){if(p.stock<=0){toast("Rupture de stock");return;}for(CartLine c:cart)if(c.product==p){if(c.qty<p.stock)c.qty++;else toast("Stock insuffisant");render();return;}cart.add(new CartLine(p,1));render();}
    private void checkout(String payment){if(cart.isEmpty()){toast("Panier vide");return;}for(CartLine c:cart)if(c.qty>c.product.stock){toast("Stock insuffisant : "+c.product.name);return;}
        double total=cartTotal();double costTotal=0;boolean costKnown=true;for(CartLine line:cart){if(line.product.costPrice<0)costKnown=false;else costTotal+=line.product.costPrice*line.qty;}if(!costKnown)costTotal=-1;StringBuilder items=new StringBuilder();for(CartLine c:cart){if(items.length()>0)items.append(", ");items.append(c.product.name).append(" x").append(c.qty);c.product.stock-=c.qty;recordStockMove(c.product.name,-c.qty,"Vente");}
        Sale s=new Sale(id(),now(),payment,items.toString(),total,discountPercent,false,costTotal,buildCartDetails());sales.add(s);String receipt="CASHIER\n"+now()+"\n"+items+"\nRemise : "+discountPercent+"%\nTOTAL : "+money(total)+"\nPaiement : "+payment+"\nMerci !";
        cart.clear();discountPercent=0;saveData();dialog().setTitle("Vente enregistrée").setMessage(receipt).setNegativeButton(tr("Print receipt"),(d,w)->printReceipt(receipt)).setNeutralButton(tr("Share receipt"),(d,w)->shareReceipt(receipt)).setPositiveButton("OK",(d,w)->{page="Vente";render();}).show();
    }
    private void discountDialog(){EditText e=input("Remise en % (0 à 100)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setText(String.valueOf(discountPercent));dialog().setTitle("Remise rapide").setView(e).setNegativeButton("Annuler",null).setPositiveButton("Appliquer",(d,w)->{try{double value=Double.parseDouble(westernDigits(e.getText().toString().trim()));if(!Double.isFinite(value))throw new Exception();discountPercent=Math.max(0,Math.min(100,value));}catch(Exception ignored){toast("Montant invalide");}render();}).show();}



    private JSONObject buildBackupJson() throws Exception {
        JSONObject backup = new JSONObject();
        backup.put("app", "Cashier");
        backup.put("formatVersion", 1);
        backup.put("exportedAt", now());
        JSONArray p = new JSONArray(), ss = new JSONArray(), m = new JSONArray();
        for (Product x : products) {
            JSONObject o = new JSONObject();
            o.put("id", x.id); o.put("name", x.name); o.put("category", x.category);
            o.put("barcode", x.barcode); o.put("price", x.price); o.put("stock", x.stock); o.put("costPrice",x.costPrice); p.put(o);
        }
        for (Sale x : sales) {
            JSONObject o = new JSONObject();
            o.put("id", x.id); o.put("time", x.time); o.put("payment", x.payment); o.put("items", x.items);
            o.put("total", x.total); o.put("discount", x.discount); o.put("refunded", x.refunded); o.put("costTotal",x.costTotal); o.put("details",x.details); ss.put(o);
        }
        for (CashMove x : cashMoves) {
            JSONObject o = new JSONObject();
            o.put("time", x.time); o.put("type", x.type); o.put("note", x.note); o.put("amount", x.amount); m.put(o);
        }
        JSONArray sm = new JSONArray();
        for (StockMove x : stockMoves) { JSONObject o = new JSONObject(); o.put("time",x.time); o.put("productName",x.productName); o.put("delta",x.delta); o.put("reason",x.reason); sm.put(o); }
        backup.put("products", p); backup.put("sales", ss); backup.put("moves", m); backup.put("stockMoves", sm);
        backup.put("openingCash", openingCash); backup.put("dark", dark);
        backup.put("language", language); backup.put("lowStockThreshold", lowStockThreshold);
        return backup;
    }

    private void createBackupFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "cashier-backup-" + today() + ".json");
            startActivityForResult(intent, BACKUP_EXPORT_REQUEST);
        } catch (Exception e) {
            CrashLogger.crash("Backup file picker failed", e);
            toast("Export failed");
        }
    }

    private void chooseBackupFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, BACKUP_IMPORT_REQUEST);
        } catch (Exception e) {
            CrashLogger.crash("Backup restore picker failed", e);
            toast("Import failed");
        }
    }

    private void writeTextFile(Uri uri, String value) throws Exception {
        try (java.io.OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
            if (out == null) throw new Exception("Cannot open destination");
            out.write(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.flush();
        }
    }

    private String csvCell(String value) {
        String v = value == null ? "" : value;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private void createSalesCsvFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, "cashier-sales-" + today() + ".csv");
            startActivityForResult(intent, SALES_EXPORT_REQUEST);
        } catch (Exception e) {
            CrashLogger.crash("Sales export picker failed", e);
            toast("Export failed");
        }
    }

    private void createInventoryCsvFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, "cashier-inventory-" + today() + ".csv");
            startActivityForResult(intent, INVENTORY_EXPORT_REQUEST);
        } catch (Exception e) { CrashLogger.crash("Inventory export picker failed", e); toast("Export failed"); }
    }

    private String buildInventoryCsv() {
        StringBuilder csv = new StringBuilder("\uFEFFProduit;Catégorie;Code-barres;Prix_vente_DA;Prix_achat_DA;Quantité;Valeur_stock_DA\n");
        for (Product p : products) {
            csv.append(csvCell(p.name)).append(';').append(csvCell(p.category)).append(';')
                    .append(csvCell(p.barcode)).append(';').append(String.format(Locale.US,"%.2f",p.price)).append(';')
                    .append(p.costPrice < 0 ? "" : String.format(Locale.US,"%.2f",p.costPrice)).append(';')
                    .append(p.stock).append(';').append(String.format(Locale.US,"%.2f",p.stock*p.price)).append('\n');
        }
        return csv.toString();
    }

    private String buildSalesCsv() {
        StringBuilder csv = new StringBuilder("\uFEFFDate;ID;Paiement;Articles;Total_DA;Coût_DA;Profit_DA;Remise_pct;Remboursée\n");
        for (Sale sale : sales) {
            csv.append(csvCell(sale.time)).append(';').append(csvCell(sale.id)).append(';')
                    .append(csvCell(sale.payment)).append(';').append(csvCell(sale.items)).append(';')
                    .append(String.format(Locale.US, "%.2f", sale.total)).append(';')
                    .append(sale.costTotal < 0 ? "" : String.format(Locale.US, "%.2f", sale.costTotal)).append(';')
                    .append(sale.costTotal < 0 ? "" : String.format(Locale.US, "%.2f", sale.total - sale.costTotal)).append(';')
                    .append(String.format(Locale.US, "%.2f", sale.discount)).append(';')
                    .append(sale.refunded ? "Oui" : "Non").append('\n');
        }
        return csv.toString();
    }

    private void shareReceipt(String receipt) {
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, "Cashier receipt");
            share.putExtra(Intent.EXTRA_TEXT, receipt);
            startActivity(Intent.createChooser(share, tr("Share receipt")));
        } catch (Exception e) { CrashLogger.crash("Share receipt failed", e); toast("Export failed"); }
    }

    private void printReceipt(String receipt) {
        try {
            PrintManager manager = (PrintManager)getSystemService(PRINT_SERVICE);
            if (manager == null) throw new Exception("Print service unavailable");
            WebView web = new WebView(this);
            String escaped = receipt.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
            String html = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'/><style>body{font-family:monospace;font-size:12pt;width:58mm;margin:3mm;word-wrap:break-word}</style></head><body>" + escaped + "</body></html>";
            web.setWebViewClient(new android.webkit.WebViewClient() {
                @Override public void onPageFinished(WebView view, String url) {
                    manager.print("Cashier receipt", view.createPrintDocumentAdapter("Cashier receipt"),
                            new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build());
                }
            });
            web.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null);
        } catch (Exception e) {
            CrashLogger.crash("Print receipt failed", e);
            toast("Print unavailable");
        }
    }

    private void restoreBackupFromUri(Uri uri) {
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) throw new Exception("empty backup");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = input.read(buffer)) != -1) {
                if (bytes.size() + n > 20 * 1024 * 1024) throw new Exception("backup too large");
                bytes.write(buffer, 0, n);
            }
            JSONObject backup = new JSONObject(new String(bytes.toByteArray(), java.nio.charset.StandardCharsets.UTF_8));
            if (!"Cashier".equals(backup.optString("app")) || backup.optInt("formatVersion", 0) != 1
                    || !(backup.opt("products") instanceof JSONArray)
                    || !(backup.opt("sales") instanceof JSONArray)
                    || !(backup.opt("moves") instanceof JSONArray)) throw new Exception("invalid format");
            JSONArray pa = backup.getJSONArray("products"), sa = backup.getJSONArray("sales"), ma = backup.getJSONArray("moves");
            ArrayList<Product> restoredProducts = new ArrayList<>();
            java.util.HashSet<String> restoredBarcodes = new java.util.HashSet<>();
            ArrayList<Sale> restoredSales = new ArrayList<>();
            ArrayList<CashMove> restoredMoves = new ArrayList<>();
            ArrayList<StockMove> restoredStockMoves = new ArrayList<>();
            for (int i=0;i<pa.length();i++) {
                JSONObject o=pa.getJSONObject(i);
                String name=o.optString("name").trim();
                double price=o.optDouble("price", -1); int stock=o.optInt("stock", -1);
                if(name.isEmpty() || price<0 || stock<0 || !Double.isFinite(price)) throw new Exception("invalid product");
                String restoredBarcode=o.optString("barcode","").trim();
                if(!restoredBarcode.isEmpty()&&!restoredBarcodes.add(restoredBarcode))throw new Exception("duplicate barcode in backup");
                restoredProducts.add(new Product(o.optString("id", id()), name, o.optString("category","Divers"),
                        restoredBarcode, price, stock, o.has("costPrice")?o.optDouble("costPrice",-1):-1));
            }
            for (int i=0;i<sa.length();i++) {
                JSONObject o=sa.getJSONObject(i);
                restoredSales.add(new Sale(o.optString("id"),o.optString("time"),o.optString("payment"),
                        o.optString("items"),o.optDouble("total"),o.optDouble("discount"),o.optBoolean("refunded"),o.has("costTotal")?o.optDouble("costTotal",-1):-1,o.optString("details","")));
            }
            for (int i=0;i<ma.length();i++) {
                JSONObject o=ma.getJSONObject(i);
                restoredMoves.add(new CashMove(o.optString("time"),o.optString("type"),o.optString("note"),o.optDouble("amount")));
            }
            JSONArray sma = backup.optJSONArray("stockMoves");
            if (sma != null) for (int i=0;i<sma.length();i++) { JSONObject o=sma.getJSONObject(i); restoredStockMoves.add(new StockMove(o.optString("time"),o.optString("productName"),o.optInt("delta"),o.optString("reason"))); }
            double restoredOpening = backup.optDouble("openingCash", 0);
            int restoredThreshold = Math.max(0, backup.optInt("lowStockThreshold", 3));
            String restoredLanguage = backup.optString("language", "fr");
            if (!restoredLanguage.equals("fr") && !restoredLanguage.equals("ar")
                    && !restoredLanguage.equals("en") && !restoredLanguage.equals("ja")) restoredLanguage = "fr";
            final String finalLanguage = restoredLanguage;
            final double finalOpening = Math.max(0, restoredOpening);
            final int finalThreshold = restoredThreshold;
            dialog().setTitle(tr("Restore backup"))
                    .setMessage(tr("Restore data from this backup? Existing data will be replaced.")
                            + "\n\nProducts: " + restoredProducts.size() + "\nSales: " + restoredSales.size()
                            + "\nCash movements: " + restoredMoves.size() + "\nBackup date: " + backup.optString("exportedAt", "—"))
                    .setNegativeButton(tr("Annuler"), null)
                    .setPositiveButton(tr("Restore backup"), (d,w) -> {
                        products.clear(); products.addAll(restoredProducts);
                        sales.clear(); sales.addAll(restoredSales);
                        cashMoves.clear(); cashMoves.addAll(restoredMoves);
                        stockMoves.clear(); stockMoves.addAll(restoredStockMoves);
                        openingCash=finalOpening; lowStockThreshold=finalThreshold;
                        dark=backup.optBoolean("dark", false); language=finalLanguage;
                        cart.clear(); discountPercent=0; category="Tout"; page="Vente";
                        saveData(); prefs.edit().putString("language",language).apply();
                        render();
                        toast("Backup restored");
                    }).show();
        } catch (Exception e) {
            CrashLogger.crash("Backup restore failed", e);
            dialog().setTitle(tr("Restore backup")).setMessage(tr("Invalid or incomplete backup file."))
                    .setPositiveButton("OK", null).show();
        }
    }

    private void startExcelImport() {
        try {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(pick, EXCEL_REQUEST);
        } catch (Exception e) {
            CrashLogger.crash("Could not open Excel picker", e);
            dialog().setTitle(tr("Import terminé")).setMessage(tr("Fichier Excel illisible ou colonnes manquantes."))
                    .setPositiveButton("OK", null).show();
        }
    }

    private byte[] readZipEntry(InputStream input) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) out.write(buffer, 0, count);
        return out.toByteArray();
    }

    private Document parseXml(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        try { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Exception ignored) {}
        try { factory.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Exception ignored) {}
        try { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Exception ignored) {}
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }

    private String xmlText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return "";
        Node node = list.item(0);
        return node == null ? "" : node.getTextContent();
    }

    private int excelColumnIndex(String ref) {
        int value = 0;
        for (int i = 0; i < ref.length(); i++) {
            char c = Character.toUpperCase(ref.charAt(i));
            if (c < 'A' || c > 'Z') break;
            value = value * 26 + (c - 'A' + 1);
        }
        return Math.max(0, value - 1);
    }

    private String normalizeHeader(String value) {
        if (value == null) return "";
        return westernDigits(value).toLowerCase(Locale.ROOT)
                .replace("ـ", "").replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
                .replace("ى", "ي").replaceAll("[\\u064B-\\u065F\\u0670]", "")
                .replaceAll("[\\s\\p{Punct}：:()（）]", "");
    }

    private double parseExcelNumber(String raw) throws Exception {
        String value = westernDigits(raw == null ? "" : raw).trim()
                .replace("\u00A0", "").replace("٫", ".").replace("٬", ",");
        value = value.replaceAll("[^0-9,.-]", "");
        if (value.isEmpty() || value.equals("-") || value.equals(".") || value.equals(",")) throw new Exception("number");
        if (value.contains(",") && value.contains(".")) { if (value.lastIndexOf(',') > value.lastIndexOf('.')) value = value.replace(".", "").replace(",", "."); else value = value.replace(",", ""); }
        else if (value.contains(",")) {
            int comma = value.lastIndexOf(',');
            int digitsAfter = value.length() - comma - 1;
            if (digitsAfter == 3 && comma > 0) value = value.replace(",", "");
            else value = value.replace(',', '.');
        }
        return Double.parseDouble(value);
    }

    private ArrayList<ImportRow> readExcelProducts(Uri uri) throws Exception {
        HashMap<String, byte[]> entries = new HashMap<>();
        try (InputStream raw = getContentResolver().openInputStream(uri);
             ZipInputStream zip = new ZipInputStream(raw)) {
            if (raw == null) throw new Exception("empty file");
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals("xl/sharedStrings.xml") || name.equals("xl/worksheets/sheet1.xml")
                        || name.matches("xl/worksheets/sheet[0-9]+\\.xml")) {
                    entries.put(name, readZipEntry(zip));
                }
                zip.closeEntry();
            }
        }
        byte[] sheetBytes = entries.get("xl/worksheets/sheet1.xml");
        if (sheetBytes == null) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                if (entry.getKey().matches("xl/worksheets/sheet[0-9]+\\.xml")) { sheetBytes = entry.getValue(); break; }
            }
        }
        if (sheetBytes == null) throw new Exception("worksheet missing");

        ArrayList<String> shared = new ArrayList<>();
        byte[] sharedBytes = entries.get("xl/sharedStrings.xml");
        if (sharedBytes != null) {
            Document sharedDoc = parseXml(sharedBytes);
            NodeList items = sharedDoc.getElementsByTagName("si");
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                StringBuilder value = new StringBuilder();
                if (item instanceof Element) {
                    NodeList textNodes = ((Element)item).getElementsByTagName("t");
                    for (int j = 0; j < textNodes.getLength(); j++) value.append(textNodes.item(j).getTextContent());
                }
                shared.add(value.toString());
            }
        }

        Document sheet = parseXml(sheetBytes);
        NodeList rowNodes = sheet.getElementsByTagName("row");
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        for (int i = 0; i < rowNodes.getLength(); i++) {
            Node rowNode = rowNodes.item(i);
            if (!(rowNode instanceof Element)) continue;
            NodeList cells = ((Element)rowNode).getElementsByTagName("c");
            ArrayList<String> values = new ArrayList<>();
            for (int j = 0; j < cells.getLength(); j++) {
                Element cell = (Element)cells.item(j);
                int col = excelColumnIndex(cell.getAttribute("r"));
                while (values.size() <= col) values.add("");
                String type = cell.getAttribute("t");
                String value;
                if ("s".equals(type)) {
                    String index = xmlText(cell, "v");
                    int sharedIndex = index.isEmpty() ? -1 : Integer.parseInt(index);
                    value = sharedIndex >= 0 && sharedIndex < shared.size() ? shared.get(sharedIndex) : "";
                } else if ("inlineStr".equals(type)) {
                    NodeList textNodes = cell.getElementsByTagName("t");
                    StringBuilder textValue = new StringBuilder();
                    for (int k = 0; k < textNodes.getLength(); k++) textValue.append(textNodes.item(k).getTextContent());
                    value = textValue.toString();
                } else {
                    value = xmlText(cell, "v");
                }
                values.set(col, value == null ? "" : value.trim());
            }
            rows.add(values);
        }
        if (rows.isEmpty()) throw new Exception("no rows");

        int headerRow = -1, nameCol = -1, qtyCol = -1, priceCol = -1;
        for (int r = 0; r < Math.min(rows.size(), 6); r++) {
            ArrayList<String> values = rows.get(r);
            int n = -1, q = -1, p = -1;
            for (int c = 0; c < values.size(); c++) {
                String h = normalizeHeader(values.get(c));
                if (h.contains("تعيينالسلع") || h.contains("سلع") || h.contains("designation") || h.contains("produit") || h.equals("item") || h.equals("product") || h.contains("nomduproduit")) n = c;
                if (h.contains("الكمية") || h.contains("quantite") || h.contains("quantity") || h.equals("qty") || h.contains("stock")) q = c;
                if (h.contains("ثمنالبيع") || h.contains("prixdevente") || h.contains("sellingprice") || h.equals("price") || h.contains("prixunitaire")) p = c;
            }
            if (n >= 0 && q >= 0 && p >= 0) { headerRow = r; nameCol = n; qtyCol = q; priceCol = p; break; }
        }
        if (headerRow < 0) throw new Exception("required headers missing");

        ArrayList<ImportRow> result = new ArrayList<>();
        for (int r = headerRow + 1; r < rows.size(); r++) {
            ArrayList<String> values = rows.get(r);
            String name = nameCol < values.size() ? values.get(nameCol).trim() : "";
            if (name.isEmpty()) continue;
            try {
                String quantity = qtyCol < values.size() ? values.get(qtyCol) : "";
                String priceText = priceCol < values.size() ? values.get(priceCol) : "";
                double qtyValue = parseExcelNumber(quantity);
                if (qtyValue < 0 || qtyValue > Integer.MAX_VALUE || qtyValue != Math.rint(qtyValue)) continue;
                int qty = (int)qtyValue;
                double price = parseExcelNumber(priceText);
                if (price < 0 || !Double.isFinite(price)) continue;
                result.add(new ImportRow(name, qty, price));
            } catch (Exception ignored) { }
        }
        return result;
    }

    private void previewExcelImport(ArrayList<ImportRow> imported) {
        if (imported.isEmpty()) {
            dialog().setTitle(tr("Aperçu de l'importation")).setMessage(tr("Aucune ligne valide n'a été trouvée."))
                    .setPositiveButton("OK", null).show();
            return;
        }
        StringBuilder preview = new StringBuilder();
        preview.append("عدد السلع: ").append(imported.size()).append("\n\n");
        int limit = Math.min(imported.size(), 8);
        for (int i = 0; i < limit; i++) {
            ImportRow row = imported.get(i);
            preview.append(i + 1).append(". ").append(row.name)
                    .append("\n   الكمية: ").append(row.stock)
                    .append("  |  السعر: ").append(money(row.price)).append("\n");
        }
        if (imported.size() > limit) preview.append("\n... و ").append(imported.size() - limit).append(" سلع أخرى");
        preview.append("\n\nسيتم تحديث السعر والكمية للسلع المطابقة بالاسم مع الاحتفاظ بالباركود الحالي. السلع الجديدة ستُضاف دون باركود. لن يتم حذف أي سلعة.");
        preview.append("\n\nاختر طريقة التعامل مع الكميات الموجودة: استبدالها بما في الملف أو إضافة كميات الملف إليها.");
        dialog().setTitle(tr("Aperçu de l'importation")).setMessage(preview.toString())
                .setNegativeButton(tr("Annuler"), null)
                .setPositiveButton(tr("Replace stock from file"), (d, w) -> applyExcelImport(imported, false))
                .setNeutralButton(tr("Add quantities to current stock"), (d, w) -> applyExcelImport(imported, true)).show();
    }

    private void applyExcelImport(ArrayList<ImportRow> imported, boolean addQuantities) {
        int added = 0, updated = 0, duplicates = 0, skippedOverflow = 0;
        java.util.HashSet<String> seenNames = new java.util.HashSet<>();
        for (ImportRow row : imported) {
            String normalizedName = normalizeHeader(row.name);
            if (!seenNames.add(normalizedName)) { duplicates++; continue; }
            Product found = null;
            for (Product product : products) {
                if (normalizeHeader(product.name).equals(normalizedName)) { found = product; break; }
            }
            if (found == null) {
                products.add(new Product(id(), row.name.trim(), "Divers", "", row.price, row.stock));
                recordStockMove(row.name.trim(), row.stock, "Import Excel");
                added++;
            } else {
                if (addQuantities && row.stock > Integer.MAX_VALUE - found.stock) { skippedOverflow++; continue; }
                int previousStock = found.stock;
                found.price = row.price;
                found.stock = addQuantities ? previousStock + row.stock : row.stock;
                int delta = found.stock - previousStock;
                recordStockMove(found.name, delta, addQuantities ? "Ajout depuis Excel" : "Mise à jour Excel");
                updated++;
            }
        }
        products.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        saveData();
        page = "Stock";
        render();
        dialog().setTitle(tr("Import terminé"))
                .setMessage(tr("Import terminé") + "\n" + tr("New products: ") + added + "\n"
                        + tr("Updated products: ") + updated + "\n" + tr("Duplicate rows skipped: ") + duplicates
                        + "\n" + tr("Quantities skipped due to overflow: ") + skippedOverflow + "\n"
                        + tr("Existing barcodes were preserved."))
                .setPositiveButton("OK", null).show();
    }

    private void recordStockMove(String productName, int delta, String reason) {
        if (delta != 0) stockMoves.add(new StockMove(now(), productName, delta, reason));
    }

    private void showStockHistory() {
        StringBuilder history = new StringBuilder();
        int shown = 0;
        for (int i = stockMoves.size() - 1; i >= 0 && shown < 40; i--, shown++) {
            StockMove move = stockMoves.get(i);
            if (shown > 0) history.append("\n\n");
            history.append(move.time).append("\n").append(move.productName)
                    .append("  ").append(move.delta > 0 ? "+" : "").append(move.delta)
                    .append("\n").append(move.reason);
        }
        if (history.length() == 0) history.append("لا توجد حركات مخزون مسجلة بعد.");
        ScrollView scroll = new ScrollView(this);
        TextView content = text(history.toString(), 14, false);
        content.setPadding(dp(16), dp(12), dp(16), dp(12));
        scroll.addView(content);
        dialog().setTitle(tr("Stock movement history")).setView(scroll)
                .setPositiveButton("OK", null).show();
    }

    private void stockPage(){
        title("Stock & produits");LinearLayout actions=card();addButtonRow(actions,"＋ Ajouter un produit",()->productDialog(null));
        EditText stockSearch=input("Search product name or barcode",InputType.TYPE_CLASS_TEXT);actions.addView(stockSearch,new LinearLayout.LayoutParams(-1,dp(48)));
        addButtonRow(actions,"Importer un fichier Excel (.xlsx)",()->startExcelImport());
        addButtonRow(actions,"Stock movement history",()->showStockHistory());
        addButtonRow(actions,"Scanner un code-barres pour rechercher",()->startBarcodeScan(tr("Scannez un produit")));
        int low=0;for(Product p:products)if(p.stock<=lowStockThreshold)low++;
        TextView warning=text(low+" produit(s) à stock faible (≤ "+lowStockThreshold+")",14,true);warning.setTextColor(low>0?Color.rgb(225,125,35):BLUE);actions.addView(warning);
        ArrayList<LinearLayout> productCards=new ArrayList<>();ArrayList<Product> cardProducts=new ArrayList<>();
        for(Product p:new ArrayList<>(products)){LinearLayout c=card();productCards.add(c);cardProducts.add(p);TextView n=text(p.name,17,true);c.addView(n);small(c,p.category+" · "+money(p.price)+" · Achat : "+(p.costPrice<0?"—":money(p.costPrice))+(p.costPrice<0?"":" · Marge : "+money(p.price-p.costPrice))+" · Code : "+(p.barcode.isEmpty()?"—":p.barcode));TextView st=text("Stock : "+p.stock,14,true);st.setTextColor(p.stock<=lowStockThreshold?Color.rgb(225,80,70):BLUE);c.addView(st);
            LinearLayout rr=row();TextView edit=button("Modifier",()->productDialog(p));rr.addView(edit,new LinearLayout.LayoutParams(0,-2,1));TextView del=button("Supprimer",()->dialog().setMessage("Supprimer "+p.name+" ?").setNegativeButton("Annuler",null).setPositiveButton("Supprimer",(d,w)->{recordStockMove(p.name,-p.stock,"Suppression produit");products.remove(p);saveData();render();}).show());LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(0,-2,1);dp.leftMargin=dp(8);rr.addView(del,dp);c.addView(rr);
        }
        stockSearch.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence a,int st,int count,int after){} public void onTextChanged(CharSequence value,int start,int before,int count){String q=value.toString().trim().toLowerCase(Locale.ROOT);for(int i=0;i<productCards.size();i++){Product p=cardProducts.get(i);boolean match=q.isEmpty()||p.name.toLowerCase(Locale.ROOT).contains(q)||p.barcode.toLowerCase(Locale.ROOT).contains(q);productCards.get(i).setVisibility(match?View.VISIBLE:View.GONE);}}public void afterTextChanged(Editable e){}});
    }
    private void productDialog(Product existing){ productDialog(existing, false); }
    private void productDialog(Product existing, boolean isNewScannedProduct){
        LinearLayout form=new LinearLayout(this);form.setPadding(dp(4),dp(4),dp(4),dp(4));form.setOrientation(LinearLayout.VERTICAL);
        EditText name=input("Nom du produit",InputType.TYPE_CLASS_TEXT);EditText cat=input("Catégorie",InputType.TYPE_CLASS_TEXT);EditText barcode=input("Code-barres",InputType.TYPE_CLASS_TEXT);EditText price=input("Prix en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText cost=input("Purchase cost in DA (optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText stock=input("Quantité en stock",InputType.TYPE_CLASS_NUMBER);
        productBarcodeField = barcode;
        EditText[] beforeBarcode={name,cat};
        for(EditText e:beforeBarcode)form.addView(e,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout barcodeRow=new LinearLayout(this);barcodeRow.setOrientation(LinearLayout.HORIZONTAL);barcodeRow.setGravity(Gravity.CENTER_VERTICAL);
        barcodeRow.addView(barcode,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView scanBarcode=button("Scanner",()->{scanningForProductBarcode=true;productBarcodeField=barcode;startBarcodeScan(tr("Scannez le code-barres"));});
        LinearLayout.LayoutParams scanParams=new LinearLayout.LayoutParams(-2,dp(48));scanParams.leftMargin=dp(8);barcodeRow.addView(scanBarcode,scanParams);form.addView(barcodeRow);
        form.addView(price,new LinearLayout.LayoutParams(-1,dp(52)));form.addView(cost,new LinearLayout.LayoutParams(-1,dp(52)));form.addView(stock,new LinearLayout.LayoutParams(-1,dp(52)));
        if(existing!=null){name.setText(existing.name);cat.setText(existing.category);barcode.setText(existing.barcode);price.setText(String.valueOf(existing.price));if(existing.costPrice>=0)cost.setText(String.valueOf(existing.costPrice));stock.setText(String.valueOf(existing.stock));}
        dialog().setTitle(existing==null?"Nouveau produit":"Modifier le produit").setView(form).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{String n=name.getText().toString().trim();double pr=Double.parseDouble(westernDigits(price.getText().toString()));String costText=westernDigits(cost.getText().toString().trim());double cp=costText.isEmpty()?-1:Double.parseDouble(costText);int qty=Integer.parseInt(westernDigits(stock.getText().toString()));if(n.isEmpty()||pr<0||!Double.isFinite(pr)||cp < -1||(cp>=0&&!Double.isFinite(cp))||qty<0)throw new Exception();String bc=barcode.getText().toString().trim();if(!bc.isEmpty()){for(Product p:products)if(p!=existing&&bc.equals(p.barcode))throw new IllegalArgumentException("duplicate barcode");}if(existing==null){products.add(new Product(id(),n,cat.getText().toString().trim().isEmpty()?"Divers":cat.getText().toString().trim(),bc,pr,qty,cp));recordStockMove(n,qty,"Création produit");}else{int delta=qty-existing.stock;existing.name=n;existing.category=cat.getText().toString().trim().isEmpty()?"Divers":cat.getText().toString().trim();existing.barcode=bc;existing.price=pr;existing.costPrice=cp;existing.stock=qty;if(isNewScannedProduct){products.add(existing);recordStockMove(n,qty,"Création par scan");}else if(delta!=0)recordStockMove(n,delta,"Ajustement manuel");}saveData();render();}catch(Exception ex){toast("Vérifiez le nom, le prix et le stock");}}).show();
    }

    private void cashPage(){
        title("Caisse & clôture");double cashSales=0,cardSales=0,todaySales=0;for(Sale s:sales)if(s.time.startsWith(today())&&!s.refunded){todaySales+=s.total;if(s.payment.equals("Espèces"))cashSales+=s.total;else cardSales+=s.total;}
        LinearLayout c=card();metric(c,"Fond de caisse initial",money(openingCash));metric(c,"Ventes du jour",money(todaySales));metric(c,"Ventes espèces",money(cashSales));metric(c,"Ventes carte",money(cardSales));
        addButtonRow(c,"＋ Apport de caisse",()->cashMoveDialog("Apport"));addButtonRow(c,"− Dépense / retrait",()->cashMoveDialog("Sortie"));addButtonRow(c,"Définir le fond de caisse",()->openingCashDialog());
        double expected=openingCash+cashSales;for(CashMove m:cashMoves)if(m.time.startsWith(today()))expected+=m.type.equals("Apport")?m.amount:-m.amount;
        final double expectedCash = expected;
        addButtonRow(c,"Clôturer la caisse / compter l'espèce",()->closeShift(expectedCash));
        title("Pièces de caisse récentes");int shown=0;for(int i=cashMoves.size()-1;i>=0&&shown<15;i--){CashMove m=cashMoves.get(i);LinearLayout item=card();item.addView(text(m.type+" · "+money(m.amount),15,true));small(item,m.time+" · "+m.note);shown++;}
    }
    private void metric(LinearLayout c,String label,String value){LinearLayout r=row();TextView l=text(label,14,false);l.setTextColor(muted);r.addView(l,new LinearLayout.LayoutParams(0,-2,1));r.addView(text(value,15,true));c.addView(r);View v=new View(this);v.setBackgroundColor(line);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(1));p.topMargin=dp(8);p.bottomMargin=dp(8);c.addView(v,p);}
    private void cashMoveDialog(String type){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);EditText amount=input("Montant en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText note=input("Motif / référence",InputType.TYPE_CLASS_TEXT);f.addView(amount);f.addView(note);dialog().setTitle(type+" de caisse").setView(f).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{double a=Double.parseDouble(westernDigits(amount.getText().toString().trim()));if(!Double.isFinite(a)||a<=0)throw new Exception();cashMoves.add(new CashMove(now(),type,note.getText().toString(),a));saveData();render();}catch(Exception e){toast("Montant invalide");}}).show();}
    private void configureLowStockThreshold() {
        EditText field = input("Low stock alert limit", InputType.TYPE_CLASS_NUMBER);
        field.setText(String.valueOf(lowStockThreshold));
        dialog().setTitle(tr("Low stock alert limit")).setView(field)
                .setNegativeButton(tr("Annuler"), null)
                .setPositiveButton(tr("Enregistrer"), (d, w) -> {
                    try {
                        int value = Integer.parseInt(westernDigits(field.getText().toString().trim()));
                        if (value < 0 || value > 1000000) throw new Exception("range");
                        lowStockThreshold = value;
                        saveData();
                        render();
                    } catch (Exception e) { toast("Montant invalide"); }
                }).show();
    }

    private void openingCashDialog(){EditText e=input("Fond de caisse en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setText(String.valueOf(openingCash));dialog().setTitle("Fond de caisse initial").setView(e).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{double value=Double.parseDouble(westernDigits(e.getText().toString().trim()));if(!Double.isFinite(value)||value<0)throw new Exception();openingCash=value;saveData();render();}catch(Exception ex){toast("Montant invalide");}}).show();}
    private void closeShift(double expected){EditText actual=input("Espèces réellement comptées (DA)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);dialog().setTitle("Clôture de caisse").setMessage("Espèces théoriques : "+money(expected)).setView(actual).setNegativeButton("Annuler",null).setPositiveButton("Comparer",(d,w)->{try{double a=Double.parseDouble(westernDigits(actual.getText().toString().trim()));if(!Double.isFinite(a)||a<0)throw new Exception();String result="Théorique : "+money(expected)+"\nCompté : "+money(a)+"\nÉcart : "+money(a-expected)+"\n"+now();dialog().setTitle("Résultat de clôture").setMessage(result).setPositiveButton("OK",null).show();cashMoves.add(new CashMove(now(),"Clôture", "Compté "+money(a)+" / théorique "+money(expected)+" / écart "+money(a-expected),0));saveData();}catch(Exception e){toast("Montant invalide");}}).show();}

    private String buildCartDetails() {
        JSONArray details = new JSONArray();
        for (CartLine line : cart) {
            try {
                JSONObject item = new JSONObject();
                item.put("productId", line.product.id);
                item.put("name", line.product.name);
                item.put("quantity", line.qty);
                item.put("unitPrice", line.product.price);
                item.put("unitCost", line.product.costPrice);
                details.put(item);
            } catch (Exception e) { CrashLogger.crash("Could not encode sale line", e); }
        }
        return details.toString();
    }

    private void collectPopularProducts(java.util.HashMap<String,Integer> popular, Sale sale) {
        try {
            if (sale.details != null && !sale.details.isEmpty()) {
                JSONArray lines = new JSONArray(sale.details);
                for (int i=0;i<lines.length();i++) {
                    JSONObject line = lines.getJSONObject(i);
                    String name = line.optString("name");
                    int qty = line.optInt("quantity");
                    if (!name.isEmpty() && qty > 0) popular.put(name, popular.getOrDefault(name, 0) + qty);
                }
                return;
            }
        } catch (Exception e) { CrashLogger.crash("Could not read sale details", e); }
        try {
            for (String it : sale.items.split(", ")) {
                int ix=it.lastIndexOf(" x");
                if(ix>0){String n=it.substring(0,ix);int q=Integer.parseInt(it.substring(ix+2));popular.put(n,popular.getOrDefault(n,0)+q);}
            }
        } catch (Exception ignored) { }
    }

    private void reportPage(){
        title("Rapports & statistiques");double day=0,week=0;int count=0;long seven=System.currentTimeMillis()-7L*24*60*60*1000;
        for(Sale s:sales)if(!s.refunded){try{Date d=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).parse(s.time);if(d!=null&&d.getTime()>=seven)week+=s.total;}catch(Exception ignored){}if(s.time.startsWith(today())){day+=s.total;count++;}}
        LinearLayout summary=card();metric(summary,"Ventes aujourd'hui",money(day));metric(summary,"Ventes sur 7 jours",money(week));metric(summary,"Transactions du jour",String.valueOf(count));int units=0;double stockValue=0,grossProfit=0;int knownCostSales=0;for(Product p:products){units+=p.stock;stockValue+=p.stock*p.price;}for(Sale sale:sales)if(!sale.refunded&&sale.costTotal>=0){grossProfit+=sale.total-sale.costTotal;knownCostSales++;}metric(summary,"Unités en stock",String.valueOf(units));metric(summary,"Valeur estimée du stock",money(stockValue));metric(summary,"Gross profit (known costs)",knownCostSales==0?"—":money(grossProfit));metric(summary,"Sales with cost data",String.valueOf(knownCostSales));
        title("Produits les plus vendus");java.util.HashMap<String,Integer> popular=new java.util.HashMap<>();for(Sale sale:sales)if(!sale.refunded)collectPopularProducts(popular,sale);
        ArrayList<String> names=new ArrayList<>(popular.keySet());names.sort((a,b)->Integer.compare(popular.get(b),popular.get(a)));if(names.isEmpty()){LinearLayout c=card();small(c,"Aucune vente enregistrée pour le moment.");}else for(int i=0;i<Math.min(5,names.size());i++){LinearLayout c=card();c.addView(text((i+1)+". "+names.get(i)+" · "+popular.get(names.get(i))+" unité(s)",15,true));}
        title("Dernières transactions");int shown=0;for(int i=sales.size()-1;i>=0&&shown<12;i--){Sale s=sales.get(i);LinearLayout c=card();c.addView(text((s.refunded?"REMBOURSÉ · ":"")+money(s.total)+" · "+s.payment,15,true));small(c,s.time+"\n"+s.items+"\nRemise : "+s.discount+"%");if(!s.refunded)addButtonRow(c,"Rembourser cette vente",()->refundSale(s));shown++;}
    }
    private void refundSale(Sale s){dialog().setTitle("Confirmer le remboursement").setMessage("Rembourser "+money(s.total)+" et remettre les produits en stock ?").setNegativeButton("Annuler",null).setPositiveButton("Rembourser",(d,w)->{if(s.refunded)return;s.refunded=true;try{if(s.details!=null&&!s.details.isEmpty()){JSONArray lines=new JSONArray(s.details);for(int i=0;i<lines.length();i++){JSONObject line=lines.getJSONObject(i);String productId=line.optString("productId");int q=line.optInt("quantity");for(Product p:products)if(p.id.equals(productId)){p.stock+=q;recordStockMove(p.name,q,"Remboursement");break;}}}else{for(String it:s.items.split(", ")){int ix=it.lastIndexOf(" x");if(ix>0){String n=it.substring(0,ix);int q=Integer.parseInt(it.substring(ix+2));for(Product p:products)if(p.name.equals(n)){p.stock+=q;recordStockMove(p.name,q,"Remboursement");break;}}}}}catch(Exception e){CrashLogger.crash("Refund stock restoration failed",e);}cashMoves.add(new CashMove(now(),"Remboursement","Vente "+s.id,s.payment.equals("Espèces")?s.total:0));saveData();render();toast("Remboursement enregistré");}).show();}
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BACKUP_EXPORT_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                try { writeTextFile(data.getData(), buildBackupJson().toString(2)); toast("Backup created"); }
                catch (Exception e) { CrashLogger.crash("Backup export failed", e); toast("Export failed"); }
            }
            return;
        }
        if (requestCode == INVENTORY_EXPORT_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                try { writeTextFile(data.getData(), buildInventoryCsv()); toast("Inventory exported"); }
                catch (Exception e) { CrashLogger.crash("Inventory export failed", e); toast("Export failed"); }
            }
            return;
        }
        if (requestCode == SALES_EXPORT_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                try { writeTextFile(data.getData(), buildSalesCsv()); toast("Sales exported"); }
                catch (Exception e) { CrashLogger.crash("Sales export failed", e); toast("Export failed"); }
            }
            return;
        }
        if (requestCode == BACKUP_IMPORT_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) restoreBackupFromUri(data.getData());
            return;
        }
        if (requestCode == EXCEL_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    ArrayList<ImportRow> imported = readExcelProducts(uri);
                    previewExcelImport(imported);
                } catch (Exception e) {
                    CrashLogger.crash("Excel inventory import failed", e);
                    dialog().setTitle(tr("Aperçu de l'importation"))
                            .setMessage(tr("Fichier Excel illisible ou colonnes manquantes.") + "\n" + e.getClass().getSimpleName())
                            .setPositiveButton("OK", null).show();
                }
            }
            return;
        }
        if (requestCode != BARCODE_REQUEST) return;
        if (scanningForProductBarcode) {
            scanningForProductBarcode = false;
            if (resultCode == RESULT_OK && data != null && productBarcodeField != null) {
                String scanned = data.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE);
                if (scanned != null && !scanned.trim().isEmpty()) {
                    productBarcodeField.setText(westernDigits(scanned.trim()));
                    productBarcodeField.setSelection(productBarcodeField.getText().length());
                    toast("Code-barres scanné");
                }
            }
            return;
        }
        if (resultCode != RESULT_OK || data == null) return;
        String code = data.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE);
        if (code == null || code.trim().isEmpty()) {
            toast("No barcode was returned");
            return;
        }
        code = westernDigits(code.trim());
        Product matched = null;
        for (Product p : products) if (code.equals(p.barcode)) { matched = p; break; }
        if (matched != null) {
            page = "Vente";
            addToCart(matched);
            return;
        }
        final String scannedCode = code;
        dialog().setTitle("Code-barres scanné").setMessage(scannedCode+"\nProduit inconnu. Voulez-vous créer le produit ?")
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Créer", (d,w) -> {
                Product draft = new Product(id(), "Produit "+scannedCode, "Divers", scannedCode, 0, 0);
                productDialog(draft, true);
            }).show();
    }
    private void toast(String s){Toast.makeText(this,tr(s),Toast.LENGTH_SHORT).show();}
}
