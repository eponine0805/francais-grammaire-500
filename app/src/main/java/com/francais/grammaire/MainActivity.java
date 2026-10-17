package com.francais.grammaire;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int BLACK=Color.rgb(28,29,31), GRAY=Color.rgb(113,117,123), BG=Color.rgb(250,250,250), LINE=Color.rgb(227,227,227), WHITE=Color.WHITE;
    private final Random random=new Random();
    private final ArrayList<Lesson> lessons=new ArrayList<>();
    private final ArrayList<Example> examples=new ArrayList<>();
    private SharedPreferences prefs;
    private LinearLayout base,mainArea;
    private MediaPlayer player;
    private int screen=0,selectedLesson=1,cardIndex=0,quizIndex=0,correctCount=0;
    private boolean revealed=false,answered=false,quizFromHome=false;
    private final ArrayList<Integer> deck=new ArrayList<>(),quiz=new ArrayList<>(),failed=new ArrayList<>();
    private final ArrayList<String> quizAnswers=new ArrayList<>();
    private String selectedAnswer="";

    private static class Lesson {
        final int id; final String chapter,title,rule,pitfall;
        Lesson(JSONObject o)throws Exception{id=o.getInt("id");chapter=o.getString("chapter");title=o.getString("title");rule=o.getString("rule");pitfall=o.getString("pitfall");}
    }
    private static class Example {
        final int id,lesson; final String fr,ja,blank,answer,detail,pitfall; final ArrayList<String> options=new ArrayList<>();
        Example(JSONObject o)throws Exception{id=o.getInt("id");lesson=o.getInt("lesson");fr=o.getString("fr");ja=o.getString("ja");blank=o.getString("blank");answer=o.getString("answer");detail=o.getString("detail");pitfall=o.getString("pitfall");JSONArray a=o.getJSONArray("options");for(int i=0;i<a.length();i++)options.add(a.getString(i));}
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(WHITE);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        prefs=getSharedPreferences("grammaire_v1",MODE_PRIVATE);
        try{
            JSONArray l=new JSONArray(assetText("lessons.json"));
            JSONArray e=new JSONArray(assetText("examples.json"));
            for(int i=0;i<l.length();i++)lessons.add(new Lesson(l.getJSONObject(i)));
            for(int i=0;i<e.length();i++)examples.add(new Example(e.getJSONObject(i)));
            if(lessons.size()!=50 || examples.size()!=500)throw new IllegalStateException("Lesson package incomplete");
        }catch(Exception ex){TextView v=new TextView(this);v.setText("教材データを読み込めません："+ex.getMessage());setContentView(v);return;}
        if(state!=null&&restore(state))return;
        home();
    }
    private String assetText(String name)throws Exception{
        try(InputStream in=getAssets().open(name);ByteArrayOutputStream b=new ByteArrayOutputStream()){
            byte[] a=new byte[8192];int n;while((n=in.read(a))!=-1)b.write(a,0,n);return b.toString(StandardCharsets.UTF_8.name());
        }
    }
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private GradientDrawable rounded(int color,int stroke,int r){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(r));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private TextView t(String str,int sp,boolean heavy,int color){TextView v=new TextView(this);v.setText(str);v.setTextSize(sp);v.setTextColor(color);if(heavy)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);v.setLineSpacing(dp(3),1.04f);return v;}
    private void gap(LinearLayout l,int height){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(height)));}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private void add(LinearLayout l,View v){l.addView(v,new LinearLayout.LayoutParams(-1,-2));}
    private void pad(View v,int x,int y){v.setPadding(dp(x),dp(y),dp(x),dp(y));}
    private Button btn(String label,boolean primary,View.OnClickListener click){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setTextColor(primary?WHITE:BLACK);b.setBackground(rounded(primary?BLACK:WHITE,primary?0:LINE,13));b.setMinHeight(dp(56));b.setOnClickListener(click);return b;}
    private LinearLayout panel(){LinearLayout v=vertical();pad(v,16,18);v.setBackground(rounded(WHITE,LINE,18));return v;}
    private void startScreen(String title,String caption,View.OnClickListener back){
        stopAudio();base=vertical();base.setBackgroundColor(BG);setContentView(base);
        LinearLayout header=vertical();pad(header,20,17);header.setBackgroundColor(WHITE);
        if(back!=null){TextView b=t("← 戻る",14,false,GRAY);b.setOnClickListener(back);add(header,b);gap(header,13);}
        add(header,t(title,24,true,BLACK));if(caption!=null){gap(header,5);add(header,t(caption,12,false,GRAY));}
        add(base,header);View line=new View(this);line.setBackgroundColor(LINE);base.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        ScrollView sc=new ScrollView(this);sc.setFillViewport(false);sc.setVerticalScrollBarEnabled(false);base.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        mainArea=vertical();pad(mainArea,18,20);sc.addView(mainArea);
    }
    private ArrayList<Integer> lessonIds(int lesson){ArrayList<Integer> r=new ArrayList<>();for(Example e:examples)if(e.lesson==lesson)r.add(e.id);return r;}
    private String sectionCaption(Lesson l){return String.format(java.util.Locale.JAPAN,"SECTION %02d / 50  ·  例文10題",l.id);}
    private Example ex(int id){if(id<1||id>examples.size())throw new IllegalArgumentException("Bad example id");return examples.get(id-1);}
    private Lesson lesson(int id){return lessons.get(id-1);}
    private boolean known(int id){return prefs.getBoolean("known_"+id,false);}
    private boolean starred(int id){return prefs.getBoolean("star_"+id,false);}
    private int doneInLesson(int id){int n=0;for(int k:lessonIds(id))if(known(k))n++;return n;}
    private int allDone(){int n=0;for(Example e:examples)if(known(e.id))n++;return n;}
    private void progress(LinearLayout root,int numerator,int total){FrameLayout track=new FrameLayout(this);track.setBackground(rounded(LINE,0,20));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(7));root.addView(track,lp);View fill=new View(this);fill.setBackground(rounded(BLACK,0,20));track.addView(fill,new FrameLayout.LayoutParams(Math.max(1,(int)(getResources().getDisplayMetrics().widthPixels*0.82*numerator/Math.max(1,total))),-1));}

    private void home(){
        screen=0;startScreen("Grammaire 500","文法を知識から、使える表現へ。",null);
        LinearLayout summary=panel();add(summary,t("学習の進み具合",13,false,GRAY));gap(summary,8);add(summary,t(allDone()+" / 500 例文",27,true,BLACK));gap(summary,12);progress(summary,allDone(),500);add(mainArea,summary);gap(mainArea,14);
        add(mainArea,btn("今日の10問  →",true,v->startMixedQuiz()));gap(mainArea,10);
        add(mainArea,btn("苦手な例文を復習",false,v->startWrongQuiz()));gap(mainArea,24);
        add(mainArea,t("文法別の目次",20,true,BLACK));gap(mainArea,5);
        LinkedHashMap<String,ArrayList<Lesson>> chapters=new LinkedHashMap<>();for(Lesson l:lessons){if(!chapters.containsKey(l.chapter))chapters.put(l.chapter,new ArrayList<>());chapters.get(l.chapter).add(l);}
        int idx=0;
        for(String chapter:chapters.keySet()){
            ArrayList<Lesson> list=chapters.get(chapter);final String key=chapter;int done=0;for(Lesson l:list)done+=doneInLesson(l.id);
            LinearLayout entry=panel();add(entry,t(String.format(java.util.Locale.JAPAN,"CHAPITRE %02d",++idx),11,false,GRAY));gap(entry,4);add(entry,t(chapter+"  →",18,true,BLACK));gap(entry,6);add(entry,t(list.size()+"セクション · "+done+" / "+(list.size()*10)+"例文学習済み",12,false,GRAY));entry.setOnClickListener(v->chapter(key));add(mainArea,entry);gap(mainArea,9);
        }
    }
    private void chapter(String name){
        screen=1;startScreen(name,"文法項目を選んで学ぶ",v->home());
        for(Lesson l:lessons){if(!l.chapter.equals(name))continue;LinearLayout entry=panel();add(entry,t(String.format(java.util.Locale.JAPAN,"%02d   %s  →",l.id,l.title),16,true,BLACK));gap(entry,8);add(entry,t(doneInLesson(l.id)+" / 10 例文習得",12,false,GRAY));entry.setOnClickListener(v->showLesson(l.id));add(mainArea,entry);gap(mainArea,10);}
    }
    private void showLesson(int id){
        selectedLesson=id;screen=2;Lesson l=lesson(id);startScreen(l.title,sectionCaption(l),v->chapter(l.chapter));
        LinearLayout rule=panel();add(rule,t("文法のポイント",12,true,GRAY));gap(rule,12);add(rule,t(l.rule,16,false,BLACK));gap(rule,16);add(rule,t("間違えやすいところ",12,true,GRAY));gap(rule,7);add(rule,t(l.pitfall,14,false,BLACK));add(mainArea,rule);gap(mainArea,15);
        add(mainArea,btn("例文カード10文を学ぶ  →",true,v->startCards(id)));gap(mainArea,9);
        add(mainArea,btn("このセクションの4択テスト",false,v->startQuiz(lessonIds(id))));gap(mainArea,20);
        add(mainArea,t("この文法の例文",18,true,BLACK));gap(mainArea,12);
        for(int k:lessonIds(id)){Example e=ex(k);LinearLayout p=panel();add(p,t(e.fr,17,true,BLACK));gap(p,8);add(p,t(e.ja,14,false,GRAY));gap(p,8);TextView pron=t("▶ 音声を聞く",13,true,BLACK);pron.setOnClickListener(v->play(k));add(p,pron);add(mainArea,p);gap(mainArea,9);}
    }
    private void startCards(int lessonId){selectedLesson=lessonId;deck.clear();deck.addAll(lessonIds(lessonId));Collections.shuffle(deck,random);cardIndex=0;revealed=false;showCard();}
    private void showCard(){
        screen=3;if(deck.isEmpty()){home();return;}if(cardIndex>=deck.size()){cardFinished();return;}
        Example e=ex(deck.get(cardIndex));Lesson l=lesson(e.lesson);
        startScreen(l.title,"例文カード  "+(cardIndex+1)+" / "+deck.size(),v->showLesson(selectedLesson));
        progress(mainArea,cardIndex,deck.size());gap(mainArea,18);
        LinearLayout card=panel();card.setMinimumHeight(dp(290));
        TextView num=t(String.format(java.util.Locale.JAPAN,"EXAMPLE %03d",e.id),11,false,GRAY);add(card,num);gap(card,28);
        TextView sentence=t(e.fr,25,true,BLACK);sentence.setGravity(Gravity.CENTER);add(card,sentence);gap(card,24);
        TextView translation=t(revealed?e.ja:"タップして日本語訳と解説を見る",revealed?18:14,revealed, revealed?BLACK:GRAY);translation.setGravity(Gravity.CENTER);add(card,translation);
        TextView explain=t(l.rule,14,false,GRAY);explain.setGravity(Gravity.CENTER);if(!revealed)explain.setVisibility(View.GONE);
        gap(card,14);add(card,explain);
        gap(card,27);TextView guide=t(revealed?"もう一度タップで次の例文へ →":"カードをタップ",12,false,GRAY);guide.setGravity(Gravity.CENTER);add(card,guide);
        card.setContentDescription(revealed?"次の例文へ":"日本語訳と文法解説を表示");
        card.setOnClickListener(v->{if(!revealed){revealed=true;translation.setText(e.ja);translation.setTextColor(BLACK);translation.setTextSize(18);explain.setVisibility(View.VISIBLE);guide.setText("もう一度タップで次の例文へ →");card.setContentDescription("次の例文へ");}else{cardIndex++;revealed=false;showCard();}});
        add(mainArea,card);gap(mainArea,14);
        add(mainArea,btn("▶  発音を聞く",false,v->play(e.id)));gap(mainArea,12);
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);
        Button unsure=btn("あとで復習",false,v->{prefs.edit().putBoolean("known_"+e.id,false).putInt("wrong_"+e.id,prefs.getInt("wrong_"+e.id,0)+1).apply();nextCard();});
        Button yes=btn("✓ 覚えた",true,v->{prefs.edit().putBoolean("known_"+e.id,true).putInt("wrong_"+e.id,0).apply();nextCard();});
        actions.addView(unsure,new LinearLayout.LayoutParams(0,dp(56),1));View between=new View(this);actions.addView(between,new LinearLayout.LayoutParams(dp(10),1));actions.addView(yes,new LinearLayout.LayoutParams(0,dp(56),1));add(mainArea,actions);gap(mainArea,16);
        TextView star=t(starred(e.id)?"★ お気に入りから外す":"☆ お気に入りに追加",14,true,GRAY);star.setGravity(Gravity.CENTER);star.setOnClickListener(v->{prefs.edit().putBoolean("star_"+e.id,!starred(e.id)).apply();star.setText(starred(e.id)?"★ お気に入りから外す":"☆ お気に入りに追加");});add(mainArea,star);
        // Audio playback is optional in local source checkout but bundled by CI APK build.
        play(e.id);
    }
    private void nextCard(){cardIndex++;revealed=false;showCard();}
    private void cardFinished(){screen=4;Lesson l=lesson(selectedLesson);startScreen("10文、完了！","例文の学習が終わりました",v->showLesson(selectedLesson));LinearLayout p=panel();add(p,t("学習した例文："+deck.size()+"文",22,true,BLACK));gap(p,12);add(p,t("この文法の習得記録："+doneInLesson(selectedLesson)+" / 10",15,false,GRAY));add(mainArea,p);gap(mainArea,15);add(mainArea,btn("この文法をテストする  →",true,v->startQuiz(lessonIds(selectedLesson))));gap(mainArea,9);add(mainArea,btn("もう一度カードを学ぶ",false,v->startCards(selectedLesson)));}
    private void startQuiz(ArrayList<Integer> ids){quizFromHome=false;quiz.clear();quiz.addAll(ids);Collections.shuffle(quiz,random);quizIndex=0;answered=false;correctCount=0;failed.clear();quizAnswers.clear();showQuiz();}
    private void startMixedQuiz(){ArrayList<Integer> ids=new ArrayList<>();for(Example e:examples)ids.add(e.id);Collections.shuffle(ids,random);startQuiz(new ArrayList<>(ids.subList(0,10)));quizFromHome=true;}
    private void startWrongQuiz(){ArrayList<Integer> ids=new ArrayList<>();for(Example e:examples)if(prefs.getInt("wrong_"+e.id,0)>0)ids.add(e.id);if(ids.isEmpty()){Toast.makeText(this,"復習対象の例文はありません",Toast.LENGTH_SHORT).show();return;}Collections.shuffle(ids,random);startQuiz(new ArrayList<>(ids.subList(0,Math.min(10,ids.size()))));quizFromHome=true;}
    private ArrayList<String> choicesFor(Example e){ArrayList<String> other=new ArrayList<>();for(String opt:e.options)if(!opt.equals(e.answer))other.add(opt);Collections.shuffle(other,new Random(4171L+e.id));ArrayList<String> res=new ArrayList<>();res.add(e.answer);for(int i=0;i<3;i++)res.add(other.get(i));Collections.shuffle(res,new Random(9013L+e.id));return res;}
    private void showQuiz(){
        screen=5;if(quizIndex>=quiz.size()){quizFinished();return;}
        Example e=ex(quiz.get(quizIndex));Lesson l=lesson(e.lesson);
        startScreen("文法テスト","QUESTION "+(quizIndex+1)+" / "+quiz.size()+"  ·  "+l.title,v->{if(quizFromHome)home();else showLesson(e.lesson);});
        progress(mainArea,quizIndex,quiz.size());gap(mainArea,18);
        LinearLayout p=panel();add(p,t("正しい語句を選んでください",12,false,GRAY));gap(p,18);TextView q=t(e.blank,23,true,BLACK);q.setGravity(Gravity.CENTER);add(p,q);gap(p,16);TextView ja=t(e.ja,15,false,GRAY);ja.setGravity(Gravity.CENTER);add(p,ja);add(mainArea,p);gap(mainArea,14);
        for(String opt:choicesFor(e)){Button b=btn(opt,false,v->answer(e,opt));b.setEnabled(!answered);add(mainArea,b);gap(mainArea,9);}
        if(answered){
            LinearLayout info=panel();add(info,t(selectedAnswer.equals(e.answer)?"✓ 正解":"正解："+e.answer,19,true,BLACK));gap(info,10);
            add(info,t(l.rule,15,false,BLACK));gap(info,9);add(info,t("注意："+l.pitfall,13,false,GRAY));add(mainArea,info);gap(mainArea,14);
            add(mainArea,btn(quizIndex+1<quiz.size()?"次の問題へ →":"結果を見る →",true,v->{quizIndex++;answered=false;selectedAnswer="";showQuiz();}));
        }
    }
    private void answer(Example e,String chosen){
        if(answered)return;
        answered=true;selectedAnswer=chosen;quizAnswers.add(chosen);
        if(chosen.equals(e.answer)){correctCount++;prefs.edit().putBoolean("known_"+e.id,true).putInt("wrong_"+e.id,0).apply();}
        else{failed.add(e.id);prefs.edit().putInt("wrong_"+e.id,prefs.getInt("wrong_"+e.id,0)+1).putBoolean("known_"+e.id,false).apply();}
        showQuiz();
    }
    private void quizFinished(){screen=6;startScreen("テスト結果","おつかれさま！",v->home());LinearLayout p=panel();add(p,t(correctCount+" / "+quiz.size()+" 正解",29,true,BLACK));gap(p,9);add(p,t("正答率 "+Math.round(100.0f*correctCount/quiz.size())+"%",18,false,GRAY));gap(p,10);progress(p,correctCount,quiz.size());add(mainArea,p);gap(mainArea,14);
        if(!failed.isEmpty()){add(mainArea,btn("間違えた"+failed.size()+"問だけ再テスト  →",true,v->startQuiz(new ArrayList<>(failed))));gap(mainArea,14);for(int id:failed){Example e=ex(id);LinearLayout item=panel();add(item,t(e.fr,16,true,BLACK));gap(item,6);add(item,t(e.ja,14,false,GRAY));add(mainArea,item);gap(mainArea,8);}}
        else {add(mainArea,t("全問正解！次のセクションに進もう。",16,true,BLACK));gap(mainArea,14);}
        add(mainArea,btn("目次に戻る",false,v->home()));
    }
    private void play(int id){
        stopAudio();try{
            String path=String.format(java.util.Locale.US,"audio/%04d.ogg",id);
            AssetFileDescriptor a=getAssets().openFd(path);
            player=new MediaPlayer();player.setDataSource(a.getFileDescriptor(),a.getStartOffset(),a.getLength());a.close();player.prepare();player.start();player.setOnCompletionListener(v->stopAudio());
        }catch(Exception ex){stopAudio();/* Developer source can run without generated audio. */}
    }
    private void stopAudio(){if(player!=null){try{player.stop();player.release();}catch(Exception ignored){}player=null;}}
    @Override protected void onPause(){super.onPause();stopAudio();}
    @Override protected void onDestroy(){stopAudio();super.onDestroy();}
    @Override protected void onSaveInstanceState(Bundle b){
        super.onSaveInstanceState(b);b.putInt("screen",screen);b.putInt("lesson",selectedLesson);b.putIntegerArrayList("deck",new ArrayList<>(deck));b.putInt("cardIndex",cardIndex);b.putBoolean("revealed",revealed);b.putIntegerArrayList("quiz",new ArrayList<>(quiz));b.putInt("quizIndex",quizIndex);b.putInt("correctCount",correctCount);b.putIntegerArrayList("failed",new ArrayList<>(failed));b.putStringArrayList("quizAnswers",new ArrayList<>(quizAnswers));b.putBoolean("answered",answered);b.putString("selectedAnswer",selectedAnswer);b.putBoolean("quizFromHome",quizFromHome);
    }
    private boolean restore(Bundle b){
        int s=b.getInt("screen",0);selectedLesson=b.getInt("lesson",1);if(selectedLesson<1||selectedLesson>50)return false;
        if(s==3){ArrayList<Integer> ids=b.getIntegerArrayList("deck");if(ids==null||ids.size()!=10)return false;for(int id:ids)if(id<1||id>500)return false;deck.clear();deck.addAll(ids);cardIndex=b.getInt("cardIndex",0);revealed=b.getBoolean("revealed",false);if(cardIndex<0||cardIndex>10)return false;showCard();return true;}
        if(s==5){ArrayList<Integer> ids=b.getIntegerArrayList("quiz");ArrayList<Integer> misses=b.getIntegerArrayList("failed");ArrayList<String> a=b.getStringArrayList("quizAnswers");if(ids==null||ids.isEmpty()||ids.size()>500||a==null||misses==null)return false;for(int id:ids)if(id<1||id>500)return false;quiz.clear();quiz.addAll(ids);failed.clear();failed.addAll(misses);quizAnswers.clear();quizAnswers.addAll(a);quizIndex=b.getInt("quizIndex",0);correctCount=b.getInt("correctCount",0);answered=b.getBoolean("answered",false);selectedAnswer=b.getString("selectedAnswer","");quizFromHome=b.getBoolean("quizFromHome",false);if(quizIndex<0||quizIndex>=quiz.size()||quizAnswers.size()!=quizIndex+(answered?1:0))return false;showQuiz();return true;}
        if(s==2){showLesson(selectedLesson);return true;}return false;
    }
    @Override public void onBackPressed(){if(screen==0){super.onBackPressed();return;}if(screen==5&&quizFromHome)home();else if(screen==3||screen==5||screen==4)showLesson(selectedLesson);else home();}
}
