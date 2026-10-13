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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
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
    private double discountPercent = 0;
    private double openingCash = 0;
    private final int BLUE = Color.rgb(48, 105, 235);
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
    private String money(double v) { return String.format(Locale.FRANCE, "%,.2f DA", v); }
    private String now() { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()); }
    private String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); }
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
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg);
        setContentView(root);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(18),dp(12),dp(18),dp(10));
        TextView brand = text("Cashier",22,true); header.addView(brand,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView mode = text(dark ? "☀" : "☾",24,true); mode.setGravity(Gravity.CENTER); mode.setBackground(shape(panel,16)); header.addView(mode,new LinearLayout.LayoutParams(dp(48),dp(44)));
        mode.setOnClickListener(v->{dark=!dark;saveData();render();}); root.addView(header);
        body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(14),dp(4),dp(14),dp(12));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        nav = new LinearLayout(this);nav.setPadding(dp(5),dp(6),dp(5),dp(6));nav.setBackgroundColor(panel);
        String[] tabs={"Vente","Stock","Caisse","Rapports"};
        for(String t:tabs){TextView b=text((t.equals(page)?"● ":"")+t,13,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(3),dp(12),dp(3),dp(12));b.setTextColor(t.equals(page)?BLUE:muted);nav.addView(b,new LinearLayout.LayoutParams(0,-2,1));b.setOnClickListener(v->{page=t;render();});}
        root.addView(nav);
        if(page.equals("Vente")) salePage(); else if(page.equals("Stock")) stockPage(); else if(page.equals("Caisse")) cashPage(); else reportPage();
    }
    private TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(fg);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),line);return d;}
    private LinearLayout card(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(13),dp(12),dp(13),dp(12));l.setBackground(shape(panel,16));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);body.addView(l,p);return l;}
    private TextView button(String label,Runnable action){TextView b=text(label,14,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(12),dp(13),dp(12),dp(13));b.setBackground(shape(BLUE,12));b.setTextColor(Color.WHITE);b.setOnClickListener(v->action.run());return b;}
    private void addButtonRow(LinearLayout parent,String label,Runnable action){TextView b=button(label,action);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(8);parent.addView(b,p);}
    private EditText input(String hint,int type){EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(15);e.setTextColor(fg);e.setHintTextColor(muted);e.setHint(hint);e.setInputType(type);e.setPadding(dp(12),dp(8),dp(12),dp(8));e.setBackground(shape(bg,10));return e;}
    private LinearLayout row(){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setOrientation(LinearLayout.HORIZONTAL);return r;}
    private void title(String s){TextView t=text(s,20,true);t.setPadding(dp(2),dp(8),dp(2),dp(12));body.addView(t);}
    private void small(LinearLayout parent,String s){TextView t=text(s,13,false);t.setTextColor(muted);parent.addView(t);}
    private double cartTotal(){double n=0;for(CartLine c:cart)n+=c.product.price*c.qty;return n*(1-discountPercent/100.0);}
    private void salePage(){
        title("Nouvelle vente");
        LinearLayout search=card();
        LinearLayout sr=row(); EditText find=input("Nom ou code-barres…",InputType.TYPE_CLASS_TEXT);sr.addView(find,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView scan=button("Scanner",()->{try{IntentIntegrator integrator=new IntentIntegrator(this);integrator.setPrompt("Scannez le code-barres");integrator.setBeepEnabled(false);integrator.setOrientationLocked(false);integrator.initiateScan();}catch(Exception e){toast("Scanner indisponible");}});LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(105),dp(50));sp.leftMargin=dp(8);sr.addView(scan,sp);search.addView(sr);
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
        cart.clear();discountPercent=0;saveData();new AlertDialog.Builder(this).setTitle("Vente enregistrée").setMessage(receipt).setPositiveButton("OK",(d,w)->{page="Vente";render();}).show();
    }
    private void discountDialog(){EditText e=input("Remise en % (0 à 100)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setText(String.valueOf(discountPercent));new AlertDialog.Builder(this).setTitle("Remise rapide").setView(e).setNegativeButton("Annuler",null).setPositiveButton("Appliquer",(d,w)->{try{discountPercent=Math.max(0,Math.min(100,Double.parseDouble(e.getText().toString())));}catch(Exception ignored){}render();}).show();}

    private void stockPage(){
        title("Stock & produits");LinearLayout actions=card();addButtonRow(actions,"＋ Ajouter un produit",()->productDialog(null));
        addButtonRow(actions,"Scanner un code-barres pour rechercher",()->{try{IntentIntegrator i=new IntentIntegrator(this);i.setPrompt("Scannez un produit");i.setBeepEnabled(false);i.initiateScan();}catch(Exception e){toast("Scanner indisponible");}});
        int low=0;for(Product p:products)if(p.stock<=3)low++;
        TextView warning=text(low+" produit(s) à stock faible (≤ 3)",14,true);warning.setTextColor(low>0?Color.rgb(225,125,35):BLUE);actions.addView(warning);
        for(Product p:new ArrayList<>(products)){LinearLayout c=card();TextView n=text(p.name,17,true);c.addView(n);small(c,p.category+" · "+money(p.price)+" · Code : "+(p.barcode.isEmpty()?"—":p.barcode));TextView st=text("Stock : "+p.stock,14,true);st.setTextColor(p.stock<=3?Color.rgb(225,80,70):BLUE);c.addView(st);
            LinearLayout rr=row();TextView edit=button("Modifier",()->productDialog(p));rr.addView(edit,new LinearLayout.LayoutParams(0,-2,1));TextView del=button("Supprimer",()->new AlertDialog.Builder(this).setMessage("Supprimer "+p.name+" ?").setNegativeButton("Annuler",null).setPositiveButton("Supprimer",(d,w)->{products.remove(p);saveData();render();}).show());LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(0,-2,1);dp.leftMargin=dp(8);rr.addView(del,dp);c.addView(rr);
        }
    }
    private void productDialog(Product existing){
        LinearLayout form=new LinearLayout(this);form.setPadding(dp(4),dp(4),dp(4),dp(4));form.setOrientation(LinearLayout.VERTICAL);
        EditText name=input("Nom du produit",InputType.TYPE_CLASS_TEXT);EditText cat=input("Catégorie",InputType.TYPE_CLASS_TEXT);EditText barcode=input("Code-barres",InputType.TYPE_CLASS_TEXT);EditText price=input("Prix en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText stock=input("Quantité en stock",InputType.TYPE_CLASS_NUMBER);
        EditText[] all={name,cat,barcode,price,stock};for(EditText e:all){form.addView(e,new LinearLayout.LayoutParams(-1,dp(52)));if(e==name)e.setHint("Nom du produit");}
        if(existing!=null){name.setText(existing.name);cat.setText(existing.category);barcode.setText(existing.barcode);price.setText(String.valueOf(existing.price));stock.setText(String.valueOf(existing.stock));}
        new AlertDialog.Builder(this).setTitle(existing==null?"Nouveau produit":"Modifier le produit").setView(form).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{String n=name.getText().toString().trim();double pr=Double.parseDouble(price.getText().toString());int qty=Integer.parseInt(stock.getText().toString());if(n.isEmpty()||pr<0||qty<0)throw new Exception();if(existing==null)products.add(new Product(id(),n,cat.getText().toString().trim().isEmpty()?"Divers":cat.getText().toString().trim(),barcode.getText().toString().trim(),pr,qty));else{existing.name=n;existing.category=cat.getText().toString().trim().isEmpty()?"Divers":cat.getText().toString().trim();existing.barcode=barcode.getText().toString().trim();existing.price=pr;existing.stock=qty;}saveData();render();}catch(Exception ex){toast("Vérifiez le nom, le prix et le stock");}}).show();
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
    private void cashMoveDialog(String type){LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);EditText amount=input("Montant en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText note=input("Motif / référence",InputType.TYPE_CLASS_TEXT);f.addView(amount);f.addView(note);new AlertDialog.Builder(this).setTitle(type+" de caisse").setView(f).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{double a=Double.parseDouble(amount.getText().toString());if(a<=0)throw new Exception();cashMoves.add(new CashMove(now(),type,note.getText().toString(),a));saveData();render();}catch(Exception e){toast("Montant invalide");}}).show();}
    private void openingCashDialog(){EditText e=input("Fond de caisse en DA",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setText(String.valueOf(openingCash));new AlertDialog.Builder(this).setTitle("Fond de caisse initial").setView(e).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{try{openingCash=Math.max(0,Double.parseDouble(e.getText().toString()));saveData();render();}catch(Exception ex){toast("Montant invalide");}}).show();}
    private void closeShift(double expected){EditText actual=input("Espèces réellement comptées (DA)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);new AlertDialog.Builder(this).setTitle("Clôture de caisse").setMessage("Espèces théoriques : "+money(expected)).setView(actual).setNegativeButton("Annuler",null).setPositiveButton("Comparer",(d,w)->{try{double a=Double.parseDouble(actual.getText().toString());String result="Théorique : "+money(expected)+"\nCompté : "+money(a)+"\nÉcart : "+money(a-expected)+"\n"+now();new AlertDialog.Builder(this).setTitle("Résultat de clôture").setMessage(result).setPositiveButton("OK",null).show();cashMoves.add(new CashMove(now(),"Clôture", "Compté "+money(a)+" / théorique "+money(expected)+" / écart "+money(a-expected),0));saveData();}catch(Exception e){toast("Montant invalide");}}).show();}

    private void reportPage(){
        title("Rapports & statistiques");double day=0,week=0;int count=0;long seven=System.currentTimeMillis()-7L*24*60*60*1000;
        for(Sale s:sales)if(!s.refunded){try{Date d=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).parse(s.time);if(d!=null&&d.getTime()>=seven)week+=s.total;}catch(Exception ignored){}if(s.time.startsWith(today())){day+=s.total;count++;}}
        LinearLayout summary=card();metric(summary,"Ventes aujourd'hui",money(day));metric(summary,"Ventes sur 7 jours",money(week));metric(summary,"Transactions du jour",String.valueOf(count));int units=0;double stockValue=0;for(Product p:products){units+=p.stock;stockValue+=p.stock*p.price;}metric(summary,"Unités en stock",String.valueOf(units));metric(summary,"Valeur estimée du stock",money(stockValue));
        title("Produits les plus vendus");java.util.HashMap<String,Integer> popular=new java.util.HashMap<>();for(Sale s:sales)if(!s.refunded){try{String[] items=s.items.split(", ");for(String it:items){int ix=it.lastIndexOf(" x");if(ix>0){String n=it.substring(0,ix);int q=Integer.parseInt(it.substring(ix+2));popular.put(n,popular.getOrDefault(n,0)+q);}}}catch(Exception ignored){}}
        ArrayList<String> names=new ArrayList<>(popular.keySet());names.sort((a,b)->Integer.compare(popular.get(b),popular.get(a)));if(names.isEmpty()){LinearLayout c=card();small(c,"Aucune vente enregistrée pour le moment.");}else for(int i=0;i<Math.min(5,names.size());i++){LinearLayout c=card();c.addView(text((i+1)+". "+names.get(i)+" · "+popular.get(names.get(i))+" unité(s)",15,true));}
        title("Dernières transactions");int shown=0;for(int i=sales.size()-1;i>=0&&shown<12;i--){Sale s=sales.get(i);LinearLayout c=card();c.addView(text((s.refunded?"REMBOURSÉ · ":"")+money(s.total)+" · "+s.payment,15,true));small(c,s.time+"\n"+s.items+"\nRemise : "+s.discount+"%");if(!s.refunded)addButtonRow(c,"Rembourser cette vente",()->refundSale(s));shown++;}
    }
    private void refundSale(Sale s){new AlertDialog.Builder(this).setTitle("Confirmer le remboursement").setMessage("Rembourser "+money(s.total)+" et remettre les produits en stock ?").setNegativeButton("Annuler",null).setPositiveButton("Rembourser",(d,w)->{if(s.refunded)return;s.refunded=true;try{for(String it:s.items.split(", ")){int ix=it.lastIndexOf(" x");if(ix>0){String n=it.substring(0,ix);int q=Integer.parseInt(it.substring(ix+2));for(Product p:products)if(p.name.equals(n)){p.stock+=q;break;}}}}catch(Exception ignored){}cashMoves.add(new CashMove(now(),"Remboursement","Vente "+s.id,s.total));saveData();render();toast("Remboursement enregistré");}).show();}
    @Override protected void onActivityResult(int requestCode,int resultCode,android.content.Intent data){IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);if(result!=null){if(result.getContents()!=null){String code=result.getContents();page="Vente";boolean found=false;for(Product p:products)if(p.barcode.equals(code)){addToCart(p);found=true;break;}if(!found){new AlertDialog.Builder(this).setTitle("Code-barres scanné").setMessage(code+"\nProduit inconnu. Voulez-vous créer le produit ?").setNegativeButton("Annuler",null).setPositiveButton("Créer",(d,w)->{Product p=new Product(id(),"Produit "+code,"Divers",code,0,0);products.add(p);saveData();productDialog(p);}).show();}}return;}super.onActivityResult(requestCode,resultCode,data);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
