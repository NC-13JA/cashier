package com.nc13ja.cashier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.content.Intent;
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
    private LinearLayout root, body, nav;
    private String page = "Vente", category = "Tout";
    private boolean dark = false;
    private String language = "fr";
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
            {"Scanner","مسح","Scan","スキャン"},{"Rechercher / ajouter","بحث / إضافة","Search / add","検索 / 追加"},
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
            {"Clôture","إغلاق","Closing","締め"},{"Remboursement","استرداد","Refund","返金"}
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
            tr("About Cashier")
        };
        dialog().setTitle(tr("Settings")).setItems(options, (d, which) -> {
            if (which == 0) chooseLanguage();
            else if (which == 1) { dark = !dark; saveData(); render(); }
            else if (which == 2) startBarcodeScan(tr("Scannez le code-barres"));
            else if (which == 3) dialog().setTitle(tr("About Cashier")).setMessage(tr("Cashier works offline and stores data on this device.")).setPositiveButton("OK", null).show();
        }).setNegativeButton(tr("Annuler"), null).show();
    }
    private static final int BARCODE_REQUEST = 7101;
    private void startBarcodeScan(String prompt) {
        try {
            Intent intent = new Intent(this, BarcodeScannerActivity.class);
            intent.putExtra("prompt", prompt);
            intent.putExtra("language", language);
            startActivityForResult(intent, BARCODE_REQUEST);
        } catch (Exception e) {
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
        double price;
        int stock;
        Product(String id, String name, String category, String barcode, double price, int stock) {
            this.id=id; this.name=name; this.category=category; this.barcode=barcode; this.price=price; this.stock=stock;
        }
    }
    static class CartLine {
        Product product; int qty;
        CartLine(Product p, int q) { product=p; qty=q; }
    }
    static class Sale {
        String id, time, payment, items;
        double total, discount;
        boolean refunded;
        Sale(String id, String time, String payment, String items, double total, double discount, boolean refunded) {
            this.id=id; this.time=time; this.payment=payment; this.items=items; this.total=total; this.discount=discount; this.refunded=refunded;
        }
    }
    static class CashMove {
        String time, type, note;
        double amount;
        CashMove(String time, String type, String note, double amount) { this.time=time; this.type=type; this.note=note; this.amount=amount; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("cashier_local_data", MODE_PRIVATE);
        dark = prefs.getBoolean("dark", false);
        language = prefs.getString("language", "fr");
        openingCash = Double.longBitsToDouble(prefs.getLong("openingCash", Double.doubleToLongBits(0)));
        loadData();
        if (products.isEmpty()) seedProducts();
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
            for (int i=0;i<a.length();i++) { JSONObject o=a.getJSONObject(i); products.add(new Product(o.optString("id",id()),o.optString("name"),o.optString("category","Divers"),o.optString("barcode",""),o.optDouble("price"),o.optInt("stock"))); }
            JSONArray s = new JSONArray(prefs.getString("sales","[]"));
            for (int i=0;i<s.length();i++) { JSONObject o=s.getJSONObject(i); sales.add(new Sale(o.optString("id"),o.optString("time"),o.optString("payment"),o.optString("items"),o.optDouble("total"),o.optDouble("discount"),o.optBoolean("refunded"))); }
            JSONArray m = new JSONArray(prefs.getString("moves","[]"));
            for (int i=0;i<m.length();i++) { JSONObject o=m.getJSONObject(i); cashMoves.add(new CashMove(o.optString("time"),o.optString("type"),o.optString("note"),o.optDouble("amount"))); }
        } catch(Exception ignored) { }
    }
    private void saveData() {
        try {
            JSONArray p=new JSONArray(), s=new JSONArray(), m=new JSONArray();
            for(Product x:products){ JSONObject o=new JSONObject(); o.put("id",x.id);o.put("name",x.name);o.put("category",x.category);o.put("barcode",x.barcode);o.put("price",x.price);o.put("stock",x.stock);p.put(o); }
            for(Sale x:sales){ JSONObject o=new JSONObject();o.put("id",x.id);o.put("time",x.time);o.put("payment",x.payment);o.put("items",x.items);o.put("total",x.total);o.put("discount",x.discount);o.put("refunded",x.refunded);s.put(o); }
            for(CashMove x:cashMoves){ JSONObject o=new JSONObject();o.put("time",x.time);o.put("type",x.type);o.put("note",x.note);o.put("amount",x.amount);m.put(o); }
            prefs.edit().putString("products",p.toString()).putString("sales",s.toString()).putString("moves",m.toString()).putLong("openingCash",Double.doubleToLongBits(openingCash)).putBoolean("dark",dark).apply();
        } catch(Exception ignored) { }
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
        LinearLayout cats=card(); HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout cr=row();String[] cs={"Tout","Boissons","Snacks","Épicerie","Divers"};for(String c:cs){TextView b=text(c,12,true);b.setPadding(dp(12),dp(10),dp(12),dp(10));b.setTextColor(category.equals(c)?BLUE:fg);cr.addView(b);b.setOnClickListener(v->{category=c;render();});}hs.addView(cr);cats.addView(hs);
        title("Produits rapides");
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);
        int shown=0;LinearLayout gr=null;
        for(Product p:products){if(!category.equals("Tout")&&!p.category.equals(category))continue;if(shown%2==0){gr=row();grid.addView(gr,new LinearLayout.LayoutParams(-1,-2));}
            LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setPadding(dp(12),dp(12),dp(12),dp(12));tile.setBackground(shape(panel,14));
            TextView name=text(p.name,14,true);tile.addView(name);TextView price=text(money(p.price),14,true);price.setTextColor(BLUE);tile.addView(price);small(tile,"Stock : "+p.stock);
            LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1);tp.setMargins(dp(3),dp(3),dp(3),dp(6));gr.addView(tile,tp);tile.setOnClickListener(v->addToCart(p));shown++;
        }
        body.addView(grid);
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
        double total=cartTotal();StringBuilder items=new StringBuilder();for(CartLine c:cart){if(items.length()>0)items.append(", ");items.append(c.product.name).append(" x").append(c.qty);c.product.stock-=c.qty;}
        Sale s=new Sale(id(),now(),payment,items.toString(),total,discountPercent,false);sales.add(s);String receipt="CASHIER\n"+now()+"\n"+items+"\nRemise : "+discountPercent+"%\nTOTAL : "+money(total)+"\nPaiement : "+payment+"\nMerci !";
        cart.clear();discountPercent=0;saveData();dialog().setTitle("Vente enregistrée").setMessage(receipt).setPositiveButton("OK",(d,w)->{page="Vente";render();}).show();
    }
    private void discountDialog(){EditText e=input("Remise en % (0 à 100)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setText(String.valueOf(discountPercent));dialog().setTitle("Remise rapide").setView(e).setNegativeButton("Annuler",null).setPositiveButton("Appliquer",(d,w)->{try{discountPercent=Math.max(0,Math.min(100,Double.parseDouble(e.getText().toString())));}catch(Exception ignored){}render();}).show();}

    private void stockPage(){
        title("Stock & produits");LinearLayout actions=card();addButtonRow(actions,"＋ Ajouter un produit",()->productDialog(null));
        addButtonRow(actions,"Scanner un code-barres pour rechercher",()->startBarcodeScan(tr("Scannez un produit")));
        int low=0;for(Product p:products)if(p.stock<=3)low++;
        TextView warning=text(low+" produit(s) à stock faible (≤ 3)",14,true);warning.setTextColor(low>0?Color.rgb(225,125,35):BLUE);actions.addView(warning);
        for(Product p:new ArrayList<>(products)){LinearLayout c=card();TextView n=text(p.name,17,true);c.addView(n);small(c,p.category+" · "+money(p.price)+" · Code : "+(p.barcode.isEmpty()?"—":p.barcode));TextView st=text("Stock : "+p.stock,14,true);st.setTextColor(p.stock<=3?Color.rgb(225,80,70):BLUE);c.addView(st);
            LinearLayout rr=row();TextView edit=button("Modifier",()->productDialog(p));rr.addView(edit,new LinearLayout.LayoutParams(0,-2,1));TextView del=button("Supprimer",()->dialog().setMessage("Supprimer "+p.name+" ?").setNegativeButton("Annuler",null).setPositiveButton("Supprimer",(d,w)->{products.remove(p);saveData();render();}).show());LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(0,-2,1);dp.leftMargin=dp(8);rr.addView(del,dp);c.addView(rr);
        }
    }
    private void productDialog(Product existing){ productDialog(existing, false); }
    private void productDialog(Product existing, boolean isNewScannedProduct){
        LinearLayout form=new LinearLayout(this);form.setPadding(dp(4),dp(4),dp(4),dp(4));form.setOrientation(LinearLayout.VERTICAL);
        EditText name=input("Nom du produit",InputType.TYPE_CLASS_TEXT);EditText cat=input("Catégorie",InputType.TYPE_CLASS_TEXT);EditText barcode=input("Code-barres",InputType.TYPE_CLASS_TEXT);EditText price=input("Prix en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText stock=input("Quantité en stock",InputType.TYPE_CLASS_NUMBER);
        EditText[] all={name,cat,barcode,price,stock};for(EditText e:all){form.addView(e,new LinearLayout.LayoutParams(-1,dp(52)));if(e==name)e.setHint("Nom du produit");}
        if(existing!=null){name.setText(existing.name);cat.setText(existing.category);barcode.setText(existing.barcode);price.setText(String.valueOf(existing.price));stock.setText(String.valueOf(existing.stock));}
        dialog().setTitle(existing==null?"Nouveau produit":"Modifier le produit").setView(form).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{String n=name.getText().toString().trim();double pr=Double.parseDouble(price.getText().toString());int qty=Integer.parseInt(stock.getText().toString());if(n.isEmpty()||pr<0||qty<0)throw new Exception();if(existing==null)products.add(new Product(id(),n,cat.getText().toString().trim().isEmpty()?"Divers":cat.getText().toString().trim(),barcode.getText().toString().trim(),pr,qty));else{existing.name=n;existing.category=cat.getText().toString().trim().isEmpty()?"Divers":cat.getText().toString().trim();existing.barcode=barcode.getText().toString().trim();existing.price=pr;existing.stock=qty;if(isNewScannedProduct)products.add(existing);}saveData();render();}catch(Exception ex){toast("Vérifiez le nom, le prix et le stock");}}).show();
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
    private void cashMoveDialog(String type){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);EditText amount=input("Montant en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText note=input("Motif / référence",InputType.TYPE_CLASS_TEXT);f.addView(amount);f.addView(note);dialog().setTitle(type+" de caisse").setView(f).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{double a=Double.parseDouble(amount.getText().toString());if(a<=0)throw new Exception();cashMoves.add(new CashMove(now(),type,note.getText().toString(),a));saveData();render();}catch(Exception e){toast("Montant invalide");}}).show();}
    private void openingCashDialog(){EditText e=input("Fond de caisse en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setText(String.valueOf(openingCash));dialog().setTitle("Fond de caisse initial").setView(e).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{openingCash=Math.max(0,Double.parseDouble(e.getText().toString()));saveData();render();}catch(Exception ex){toast("Montant invalide");}}).show();}
    private void closeShift(double expected){EditText actual=input("Espèces réellement comptées (DA)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);dialog().setTitle("Clôture de caisse").setMessage("Espèces théoriques : "+money(expected)).setView(actual).setNegativeButton("Annuler",null).setPositiveButton("Comparer",(d,w)->{try{double a=Double.parseDouble(actual.getText().toString());String result="Théorique : "+money(expected)+"\nCompté : "+money(a)+"\nÉcart : "+money(a-expected)+"\n"+now();dialog().setTitle("Résultat de clôture").setMessage(result).setPositiveButton("OK",null).show();cashMoves.add(new CashMove(now(),"Clôture", "Compté "+money(a)+" / théorique "+money(expected)+" / écart "+money(a-expected),0));saveData();}catch(Exception e){toast("Montant invalide");}}).show();}

    private void reportPage(){
        title("Rapports & statistiques");double day=0,week=0;int count=0;long seven=System.currentTimeMillis()-7L*24*60*60*1000;
        for(Sale s:sales)if(!s.refunded){try{Date d=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).parse(s.time);if(d!=null&&d.getTime()>=seven)week+=s.total;}catch(Exception ignored){}if(s.time.startsWith(today())){day+=s.total;count++;}}
        LinearLayout summary=card();metric(summary,"Ventes aujourd'hui",money(day));metric(summary,"Ventes sur 7 jours",money(week));metric(summary,"Transactions du jour",String.valueOf(count));int units=0;double stockValue=0;for(Product p:products){units+=p.stock;stockValue+=p.stock*p.price;}metric(summary,"Unités en stock",String.valueOf(units));metric(summary,"Valeur estimée du stock",money(stockValue));
        title("Produits les plus vendus");java.util.HashMap<String,Integer> popular=new java.util.HashMap<>();for(Sale s:sales)if(!s.refunded){try{String[] items=s.items.split(", ");for(String it:items){int ix=it.lastIndexOf(" x");if(ix>0){String n=it.substring(0,ix);int q=Integer.parseInt(it.substring(ix+2));popular.put(n,popular.getOrDefault(n,0)+q);}}}catch(Exception ignored){}}
        ArrayList<String> names=new ArrayList<>(popular.keySet());names.sort((a,b)->Integer.compare(popular.get(b),popular.get(a)));if(names.isEmpty()){LinearLayout c=card();small(c,"Aucune vente enregistrée pour le moment.");}else for(int i=0;i<Math.min(5,names.size());i++){LinearLayout c=card();c.addView(text((i+1)+". "+names.get(i)+" · "+popular.get(names.get(i))+" unité(s)",15,true));}
        title("Dernières transactions");int shown=0;for(int i=sales.size()-1;i>=0&&shown<12;i--){Sale s=sales.get(i);LinearLayout c=card();c.addView(text((s.refunded?"REMBOURSÉ · ":"")+money(s.total)+" · "+s.payment,15,true));small(c,s.time+"\n"+s.items+"\nRemise : "+s.discount+"%");if(!s.refunded)addButtonRow(c,"Rembourser cette vente",()->refundSale(s));shown++;}
    }
    private void refundSale(Sale s){dialog().setTitle("Confirmer le remboursement").setMessage("Rembourser "+money(s.total)+" et remettre les produits en stock ?").setNegativeButton("Annuler",null).setPositiveButton("Rembourser",(d,w)->{if(s.refunded)return;s.refunded=true;try{for(String it:s.items.split(", ")){int ix=it.lastIndexOf(" x");if(ix>0){String n=it.substring(0,ix);int q=Integer.parseInt(it.substring(ix+2));for(Product p:products)if(p.name.equals(n)){p.stock+=q;break;}}}}catch(Exception ignored){}cashMoves.add(new CashMove(now(),"Remboursement","Vente "+s.id,s.total));saveData();render();toast("Remboursement enregistré");}).show();}
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != BARCODE_REQUEST || resultCode != RESULT_OK || data == null) return;
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
