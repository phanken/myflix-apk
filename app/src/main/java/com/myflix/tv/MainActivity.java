package com.myflix.tv;
import android.app.*;import android.os.*;import android.graphics.Color;import android.view.*;import android.webkit.*;import android.net.Uri;import android.content.*;import android.webkit.JavascriptInterface;import org.json.*;import java.net.*;import java.io.*;
public class MainActivity extends Activity{
 WebView w;static final String HOME="https://myflix-9n6o.onrender.com";static final int PR=77;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);immersive();w=new WebView(this);w.setBackgroundColor(Color.BLACK);w.setFocusable(true);w.setFocusableInTouchMode(true);
 WebSettings s=w.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);s.setMixedContentMode(0);s.setUserAgentString(s.getUserAgentString()+" MyFlixAndroidTV/3.0");
 w.addJavascriptInterface(new Bridge(),"MyFlixTV");
w.setWebViewClient(new WebViewClient(){
 @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
  Uri u=r.getUrl();
  if("http".equals(u.getScheme())||"https".equals(u.getScheme())){v.loadUrl(u.toString());return true;}
  return true;
 }
 @Override public void onPageFinished(WebView v,String u){inject();v.requestFocus();}
});
setContentView(w);
w.loadUrl(HOME);}
 void immersive(){getWindow().getDecorView().setSystemUiVisibility(5894);}
 void openHlsFromWebView(String u){
  try{
    if(u==null||u.isEmpty())return;
    Intent i=new Intent(MainActivity.this,PlayerActivity.class);
    i.putExtra("url",u);
    i.putExtra("title","MyFlix");
    startActivityForResult(i,PR);
  }catch(Throwable e){}
 }
 public class Bridge{
  @JavascriptInterface public void play(String u,String t){
    openNative(u,t);
  }

  @JavascriptInterface public void playEpisode(String slug,int episodeNumber,String title){
    final String safeSlug=slug;
    final int wanted=episodeNumber;
    final String safeTitle=title;
    new Thread(()->{
      try{
        if(safeSlug==null||safeSlug.trim().isEmpty())throw new Exception("missing slug");
        URL api=new URL(HOME+"/api/movie/"+URLEncoder.encode(safeSlug,"UTF-8"));
        HttpURLConnection c=(HttpURLConnection)api.openConnection();
        c.setConnectTimeout(12000);c.setReadTimeout(12000);
        c.setRequestProperty("Accept","application/json");
        c.setRequestProperty("User-Agent","MyFlixAndroidTV/3.4");
        int code=c.getResponseCode();
        InputStream in=(code>=200&&code<300)?c.getInputStream():c.getErrorStream();
        BufferedReader br=new BufferedReader(new InputStreamReader(in));
        StringBuilder buf=new StringBuilder();String line;
        while((line=br.readLine())!=null)buf.append(line);
        br.close();c.disconnect();

        if(code<200||code>=300)throw new Exception("api http "+code);
        JSONObject root=new JSONObject(buf.toString());
        JSONArray servers=root.optJSONArray("episodes");
        if(servers==null)throw new Exception("no episodes");

        String m3u8=null;String epName="Tập "+wanted;
        outer:
        for(int si=0;si<servers.length();si++){
          JSONObject sv=servers.optJSONObject(si);
          if(sv==null)continue;
          JSONArray arr=sv.optJSONArray("server_data");
          if(arr==null)continue;
          for(int ei=0;ei<arr.length();ei++){
            JSONObject ep=arr.optJSONObject(ei);
            if(ep==null)continue;
            String nm=ep.optString("name","");
            int n=extractEpisodeNumber(nm,ei+1);
            if(n==wanted){
              String u=ep.optString("link_m3u8","");
              if(u.isEmpty())u=ep.optString("link_m3u8_hls","");
              if(!u.isEmpty()){m3u8=u;epName=nm.isEmpty()?epName:nm;break outer;}
            }
          }
        }

        if(m3u8==null||m3u8.isEmpty())throw new Exception("no m3u8");
        final String finalUrl=m3u8;
        final String finalTitle=(safeTitle==null||safeTitle.isEmpty()?safeSlug:safeTitle)+" · "+epName;
        runOnUiThread(()->openNative(finalUrl,finalTitle));
      }catch(Throwable ex){
        runOnUiThread(()->js("window.__nativeFailed&&window.__nativeFailed()"));
      }
    }).start();
  }

  private int extractEpisodeNumber(String name,int fallback){
    try{
      String digits=name.replaceAll("[^0-9]","");
      if(!digits.isEmpty())return Integer.parseInt(digits);
    }catch(Throwable ignored){}
    return fallback;
  }

  private void openNative(String u,String t){
    try{
      if(u==null||u.trim().isEmpty()||!(u.startsWith("http://")||u.startsWith("https://"))){
        js("window.__nativeFailed&&window.__nativeFailed()");return;
      }
      Intent i=new Intent(MainActivity.this,PlayerActivity.class);
      i.putExtra("url",u);i.putExtra("title",t);
      startActivityForResult(i,PR);
    }catch(Throwable ex){
      js("window.__nativeFailed&&window.__nativeFailed()");
    }
  }
 }
 void js(String x){w.evaluateJavascript(x,null);}
 void inject(){
  StringBuilder q=new StringBuilder();
  q.append("javascript:(function(){");
  q.append("if(window.__TV39)return;window.__TV39=1;");
  q.append("var st=document.createElement('style');st.textContent='.rail{scroll-behavior:auto!important;grid-auto-columns:220px!important;gap:22px!important}.card{transition:transform .10s ease,box-shadow .10s ease!important}.tvf{outline:none!important;z-index:999!important;position:relative!important}.card.tvf{transform:scale(1.13)!important;box-shadow:0 18px 45px rgba(0,0,0,.78)!important}.watchEp.tvf{transform:scale(1.07)!important;background:#e50914!important;border-color:#e50914!important}.tvf:not(.card):not(.watchEp){filter:brightness(1.25)!important}';document.head.appendChild(st);");
  q.append("setTimeout(function(){try{var oldPlay=window.playEpisode;if(typeof oldPlay==='function'){window.__webPlayEpisode=oldPlay;window.playEpisode=function(si,ei){try{var ep=currentEpisodes&&currentEpisodes[si]&&currentEpisodes[si].server_data&&currentEpisodes[si].server_data[ei];var u=ep&&(ep.link_m3u8||ep.link_m3u8_hls||'');var ttl=((currentMovie&&currentMovie.name)||'MyFlix')+' · '+((ep&&ep.name)||('Tập '+(ei+1)));if(u&&window.MyFlixTV&&typeof window.MyFlixTV.play==='function'){window.MyFlixTV.play(u,ttl);return;} }catch(e){} return oldPlay(si,ei);};}}catch(e){}},50);");
  q.append("function A(){return Array.from(document.querySelectorAll('a,button,input,.card,.genreItem,.watchEp,.genreToggle')).filter(function(e){var r=e.getBoundingClientRect(),c=getComputedStyle(e);return r.width>2&&r.height>2&&c.display!='none'&&c.visibility!='hidden'&&!e.disabled})}");
  q.append("function F(e){if(!e)return;document.querySelectorAll('.tvf').forEach(function(x){x.classList.remove('tvf')});e.classList.add('tvf');try{e.focus({preventScroll:true})}catch(x){e.focus()}e.scrollIntoView({block:'nearest',inline:'center'})}");
  q.append("window.tvInit=function(){var a=A();if(!document.querySelector('.tvf')&&a.length)F(a[0])};");
  q.append("window.tvMove=function(d){var a=A(),c=document.querySelector('.tvf');if(!c){F(a[0]);return}var r=c.getBoundingClientRect(),cx=r.left+r.width/2,cy=r.top+r.height/2,b=null,bs=1e20;for(var i=0;i<a.length;i++){var e=a[i];if(e===c)continue;var z=e.getBoundingClientRect(),dx=z.left+z.width/2-cx,dy=z.top+z.height/2-cy,p,n;if(d=='l'&&dx<-4){p=-dx;n=Math.abs(dy)}else if(d=='r'&&dx>4){p=dx;n=Math.abs(dy)}else if(d=='u'&&dy<-4){p=-dy;n=Math.abs(dx)}else if(d=='d'&&dy>4){p=dy;n=Math.abs(dx)}else continue;var sc=p+n*2.5;if(sc<bs){bs=sc;b=e}}if(b)F(b)};");
  q.append("window.tvOk=function(){var e=document.querySelector('.tvf');if(e)e.click()};");
  q.append("new MutationObserver(function(){setTimeout(window.tvInit,20)}).observe(document.body,{childList:true,subtree:true});setTimeout(window.tvInit,200);");
  q.append("})();");
  js(q.toString());
 }
 public boolean dispatchKeyEvent(KeyEvent e){if(e.getAction()!=0)return super.dispatchKeyEvent(e);switch(e.getKeyCode()){case 21:js("tvMove('l')");return true;case 22:js("tvMove('r')");return true;case 19:js("tvMove('u')");return true;case 20:js("tvMove('d')");return true;case 23:case 66:js("tvOk()");return true;case 4:if(w.canGoBack())w.goBack();else finish();return true;}return super.dispatchKeyEvent(e);}
 protected void onActivityResult(int r,int x,Intent d){
 super.onActivityResult(r,x,d);
 if(r==PR){
  if(x==RESULT_OK) js("if(typeof playRelative=='function')playRelative(1)");
  else js("window.__nativeFailed&&window.__nativeFailed()");
 }
}
 protected void onResume(){super.onResume();immersive();if(w!=null)w.onResume();}protected void onPause(){if(w!=null)w.onPause();super.onPause();}
}