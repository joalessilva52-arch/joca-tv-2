package br.com.jocatv;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.content.SharedPreferences;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int BG=Color.rgb(5,11,22), PANEL=Color.rgb(15,31,52), BLUE=Color.rgb(18,168,255), WHITE=Color.WHITE;
    private LinearLayout root, rows, nav;
    private TextView status, pageTitle;
    private EditText m3uUrl, server, username, password;
    private PlayerView playerView;
    private ExoPlayer player;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final ArrayList<Channel> live=new ArrayList<>(), movies=new ArrayList<>(), series=new ArrayList<>();
    private final Set<String> favorites=new LinkedHashSet<>();
    private String currentUser="", currentPass="", currentServer="";
    private String activePage="Início";
    private SharedPreferences prefs;

    static class Channel {
        String name, url, category, id;
        Channel(String n,String u,String c,String i){name=n;url=u;category=c;id=i;}
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("joca",MODE_PRIVATE);
        favorites.addAll(prefs.getStringSet("favorites",new HashSet<>()));
        buildHome();
    }

    private GradientDrawable shape(int color){
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(18);
        d.setStroke(2,0xFF14518A); return d;
    }
    private TextView text(String s,int size,int color){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private Button button(String s,Runnable action){
        Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(WHITE);
        b.setBackground(shape(PANEL)); b.setFocusable(true); b.setOnClickListener(v->action.run()); return b;
    }
    private EditText field(String hint){
        EditText e=new EditText(this); e.setSingleLine(true); e.setTextColor(WHITE); e.setHintTextColor(0xFF9DB4CE);
        e.setHint(hint); e.setPadding(14,6,14,6); e.setBackground(shape(PANEL)); return e;
    }
    private void base(String title){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,18,24,18); root.setBackgroundColor(BG);
        TextView brand=text("JOCA TV 2.0",30,BLUE); brand.setTypeface(null,Typeface.BOLD); root.addView(brand);
        root.addView(text("Sua TV, do seu jeito.",14,0xFFB7CBE3));
        nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setPadding(0,10,0,10);
        String[] pages={"Início","TV ao Vivo","Filmes","Séries","Favoritos","Adicionar lista","Xtream Codes"};
        for(String p:pages) nav.addView(button(p,()->showPage(p)),new LinearLayout.LayoutParams(0,52,1));
        root.addView(nav);
        pageTitle=text(title,22,WHITE); pageTitle.setTypeface(null,Typeface.BOLD); pageTitle.setPadding(0,12,0,10); root.addView(pageTitle);
        status=text("Pronto para adicionar uma lista autorizada.",14,0xFFB7CBE3); root.addView(status);
        ScrollView sc=new ScrollView(this); rows=new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL); sc.addView(rows);
        root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    private void buildHome(){
        base("Bem-vindo ao JOCA TV");
        rows.addView(text("Seu reprodutor de listas M3U e Xtream Codes.",18,WHITE));
        rows.addView(text("Adicione uma fonte de conteúdo que você tenha autorização para acessar.",15,0xFFB7CBE3));
        rows.addView(button("Adicionar lista M3U",()->showPage("Adicionar lista")));
        rows.addView(button("Entrar com Xtream Codes",()->showPage("Xtream Codes")));
        rows.addView(button("Abrir TV ao Vivo",()->showPage("TV ao Vivo")));
        rows.addView(button("Ver favoritos",()->showPage("Favoritos")));
    }
    private void showPage(String page){
        activePage=page;
        if(page.equals("Início")){buildHome();return;}
        base(page);
        if(page.equals("Adicionar lista")){
            m3uUrl=field("Cole a URL M3U (https://...)"); rows.addView(m3uUrl);
            rows.addView(button("Importar M3U",()->loadM3u(m3uUrl.getText().toString().trim())));
            rows.addView(text("Também é possível abrir uma URL M3U pública e autorizada. Arquivos locais podem ser adicionados em uma próxima atualização.",13,0xFFB7CBE3));
        } else if(page.equals("Xtream Codes")){
            server=field("Endereço do servidor (https://...)"); username=field("Usuário"); password=field("Senha");
            rows.addView(server); rows.addView(username); rows.addView(password);
            rows.addView(button("Conectar",()->connectXtream()));
            rows.addView(text("Use somente dados fornecidos pelo seu provedor autorizado. As credenciais não são salvas nesta versão.",13,0xFFB7CBE3));
        } else if(page.equals("TV ao Vivo")) showItems(live);
        else if(page.equals("Filmes")) showItems(movies);
        else if(page.equals("Séries")) showItems(series);
        else if(page.equals("Favoritos")){
            ArrayList<Channel> fav=new ArrayList<>();
            for(Channel c:allItems()) if(favorites.contains(c.id)) fav.add(c);
            showItems(fav);
        }
    }
    private ArrayList<Channel> allItems(){
        ArrayList<Channel> a=new ArrayList<>();a.addAll(live);a.addAll(movies);a.addAll(series);return a;
    }
    private void showItems(List<Channel> items){
        if(items.isEmpty()){rows.addView(text("Nenhum conteúdo carregado. Adicione uma playlist ou conecte seu serviço.",16,0xFFB7CBE3));return;}
        for(Channel c:items){
            LinearLayout line=new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL);
            Button play=button(c.name,()->play(c)); line.addView(play,new LinearLayout.LayoutParams(0,58,1));
            Button fav=button(favorites.contains(c.id)?"★":"☆",()->toggleFav(c)); line.addView(fav,new LinearLayout.LayoutParams(72,58));
            rows.addView(line);
        }
    }
    private void toggleFav(Channel c){
        if(favorites.contains(c.id)) favorites.remove(c.id); else favorites.add(c.id);
        prefs.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();
        status.setText("Favoritos atualizados."); showPage(activePage);
    }
    private void loadM3u(String url){
        if(url.isEmpty() || !url.startsWith("https://")){status.setText("Informe uma URL M3U HTTPS válida.");return;}
        status.setText("Baixando playlist...");
        io.execute(()->{
            try{
                HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection(); c.setConnectTimeout(12000); c.setReadTimeout(20000);
                c.setRequestProperty("User-Agent","JOCA-TV/2.0");
                if(c.getResponseCode()<200||c.getResponseCode()>299) throw new Exception("HTTP "+c.getResponseCode());
                StringBuilder sb=new StringBuilder(); try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"))){String l;while((l=r.readLine())!=null)sb.append(l).append('\n');}
                ArrayList<Channel> parsed=parseM3u(sb.toString());
                runOnUiThread(()->{live.clear();live.addAll(parsed);status.setText(parsed.size()+" itens importados.");showPage("TV ao Vivo");});
            }catch(Exception e){runOnUiThread(()->status.setText("Falha ao importar: "+e.getMessage()));}
        });
    }
    private ArrayList<Channel> parseM3u(String body){
        ArrayList<Channel> out=new ArrayList<>(); String name="Canal",group="Canais";
        for(String raw:body.split("\\r?\\n")){
            String l=raw.trim();
            if(l.startsWith("#EXTINF")){
                int comma=l.lastIndexOf(','); name=comma>=0?l.substring(comma+1).trim():"Canal";
                java.util.regex.Matcher m=java.util.regex.Pattern.compile("group-title=\\\"([^\\\"]*)\\\"").matcher(l);
                group=m.find()?m.group(1):"Canais";
            }else if((l.startsWith("http://")||l.startsWith("https://"))&&!l.startsWith("#")){
                out.add(new Channel(name,l,group,l));name="Canal";group="Canais";
            }
        }
        return out;
    }
    private void connectXtream(){
        String s=server.getText().toString().trim(), u=username.getText().toString().trim(), p=password.getText().toString();
        if(s.isEmpty()||u.isEmpty()||p.isEmpty()){status.setText("Preencha servidor, usuário e senha.");return;}
        if(!s.startsWith("https://")){status.setText("Por segurança, esta versão exige servidor HTTPS.");return;}
        currentServer=s.replaceAll("/+$",""); currentUser=u; currentPass=p; status.setText("Conectando ao serviço...");
        io.execute(()->{
            try{
                JSONObject auth=getJson(currentServer+"/player_api.php?username="+enc(u)+"&password="+enc(p));
                if(auth.has("user_info") && !auth.getJSONObject("user_info").optString("auth","1").equals("1")) throw new Exception("Credenciais não autorizadas.");
                JSONArray cats=getJsonArray(currentServer+"/player_api.php?username="+enc(u)+"&password="+enc(p)+"&action=get_live_categories");
                JSONArray streams=getJsonArray(currentServer+"/player_api.php?username="+enc(u)+"&password="+enc(p)+"&action=get_live_streams");
                ArrayList<Channel> parsed=new ArrayList<>();
                for(int i=0;i<streams.length();i++){
                    JSONObject o=streams.getJSONObject(i); String id=o.optString("stream_id");
                    String cat=o.optString("category_id","Canais"); String nm=o.optString("name","Canal");
                    String stream=currentServer+"/live/"+enc(u)+"/"+enc(p)+"/"+enc(id)+".ts";
                    parsed.add(new Channel(nm,stream,cat,"xtream:"+id));
                }
                runOnUiThread(()->{live.clear();live.addAll(parsed);status.setText("Conectado. "+cats.length()+" categorias e "+parsed.size()+" canais.");showPage("TV ao Vivo");});
            }catch(Exception e){runOnUiThread(()->status.setText("Não foi possível conectar: "+e.getMessage()));}
        });
    }
    private JSONObject getJson(String url)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(20000);
        try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"))){StringBuilder b=new StringBuilder();String l;while((l=r.readLine())!=null)b.append(l);return new JSONObject(b.toString());}
        finally{c.disconnect();}
    }
    private JSONArray getJsonArray(String url)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(25000);
        try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"))){StringBuilder b=new StringBuilder();String l;while((l=r.readLine())!=null)b.append(l);return new JSONArray(b.toString());}
        finally{c.disconnect();}
    }
    private String enc(String s)throws Exception{return java.net.URLEncoder.encode(s,"UTF-8");}
    private void play(Channel c){
        try{
            if(player==null){player=new ExoPlayer.Builder(this).build();playerView=new PlayerView(this);playerView.setPlayer(player);playerView.setUseController(true);
                rows.addView(playerView,0,new LinearLayout.LayoutParams(-1,300));}
            player.setMediaItem(MediaItem.fromUri(c.url));player.prepare();player.play();status.setText("Reproduzindo: "+c.name);
        }catch(Exception e){status.setText("Não foi possível iniciar a reprodução.");}
    }
    @Override protected void onStop(){super.onStop();if(player!=null){player.release();player=null;}}
    @Override protected void onDestroy(){super.onDestroy();io.shutdownNow();}
}
