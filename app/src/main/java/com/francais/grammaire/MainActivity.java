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
    private String selectedAnswer="";private int selectedExample=1;

    private static class Lesson {
        final int id; final String chapter,title,rule,pitfall;
        Lesson(JSONObject o)throws Exception{id=o.getInt("id");chapter=o.getString("chapter");title=o.getString("title");rule=o.getString("rule");pitfall=o.getString("pitfall");}
    }
    private static class Example {
        final int id,lesson; final String fr,ja,blank,answer,detail,pitfall,structure,whole,reason,contrast,tip,questionHint; final ArrayList<String> options=new ArrayList<>();
        Example(JSONObject o)throws Exception{id=o.getInt("id");lesson=o.getInt("lesson");fr=o.getString("fr");ja=o.getString("ja");blank=o.getString("blank");answer=o.getString("answer");detail=o.getString("detail");pitfall=o.getString("pitfall");structure=o.getString("structure");whole=o.getString("whole");reason=o.getString("reason");contrast=o.getString("contrast");tip=o.getString("tip");questionHint=o.getString("questionHint");JSONArray a=o.getJSONArray("options");for(int i=0;i<a.length();i++)options.add(a.getString(i));}
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
        migrateChangedExamples();if(state!=null&&restore(state))return;
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
    private String sectionCaption(Lesson l){return String.format(java.util.Locale.JAPAN,"SECTION %02d / 50  ·  %d例文",l.id,lessonIds(l.id).size());}
    private Example ex(int id){if(id<1||id>examples.size())throw new IllegalArgumentException("Bad example id");return examples.get(id-1);}
    private Lesson lesson(int id){return lessons.get(id-1);}
    private boolean known(int id){return prefs.getBoolean("known_"+id,false);}
    private boolean starred(int id){return prefs.getBoolean("star_"+id,false);}
    private int doneInLesson(int id){int n=0;for(int k:lessonIds(id))if(known(k))n++;return n;}
    private int allDone(){int n=0;for(Example e:examples)if(known(e.id))n++;return n;}
    private void progress(LinearLayout root,int numerator,int total){FrameLayout track=new FrameLayout(this);track.setBackground(rounded(LINE,0,20));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(7));root.addView(track,lp);View fill=new View(this);fill.setBackground(rounded(BLACK,0,20));track.addView(fill,new FrameLayout.LayoutParams(Math.max(1,(int)(getResources().getDisplayMetrics().widthPixels*0.82*numerator/Math.max(1,total))),-1));}

    private void home(){
        screen=0;startScreen("Grammaire 500","文法の理解 → 例文を読む → 確認問題",null);
        LinearLayout summary=panel();add(summary,t("学習の進み具合",13,false,GRAY));gap(summary,8);add(summary,t(allDone()+" / 500 例文",27,true,BLACK));gap(summary,12);progress(summary,allDone(),500);add(mainArea,summary);gap(mainArea,14);
        add(mainArea,btn("全範囲から10問  →",true,v->startMixedQuiz()));gap(mainArea,10);
        add(mainArea,btn("苦手な例文を復習",false,v->startWrongQuiz()));gap(mainArea,24);
        add(mainArea,t("文法別の目次",20,true,BLACK));gap(mainArea,5);
        LinkedHashMap<String,ArrayList<Lesson>> chapters=new LinkedHashMap<>();for(Lesson l:lessons){if(!chapters.containsKey(l.chapter))chapters.put(l.chapter,new ArrayList<>());chapters.get(l.chapter).add(l);}
        int idx=0;
        for(String chapter:chapters.keySet()){
            ArrayList<Lesson> list=chapters.get(chapter);final String key=chapter;int done=0;for(Lesson l:list)done+=doneInLesson(l.id);
            LinearLayout entry=panel();add(entry,t(String.format(java.util.Locale.JAPAN,"CHAPITRE %02d",++idx),11,false,GRAY));gap(entry,4);add(entry,t(chapter+"  →",18,true,BLACK));gap(entry,6);add(entry,t(list.size()+"セクション · "+done+" / "+list.stream().mapToInt(sec->lessonIds(sec.id).size()).sum()+"例文学習済み",12,false,GRAY));entry.setOnClickListener(v->chapter(key));add(mainArea,entry);gap(mainArea,9);
        }
    }
    private void chapter(String name){
        screen=1;startScreen(name,"文法項目を選んで学ぶ",v->home());
        for(Lesson l:lessons){if(!l.chapter.equals(name))continue;LinearLayout entry=panel();add(entry,t(String.format(java.util.Locale.JAPAN,"%02d   %s  →",l.id,l.title),16,true,BLACK));gap(entry,8);add(entry,t(doneInLesson(l.id)+" / "+lessonIds(l.id).size()+" 例文習得",12,false,GRAY));entry.setOnClickListener(v->showLesson(l.id));add(mainArea,entry);gap(mainArea,10);}
    }

    private void migrateChangedExamples(){
        if(prefs.getBoolean("content_revision_2",false))return;
        try{
            JSONArray ids=new JSONObject(assetText("changed_ids.json")).getJSONArray("changed_ids");
            SharedPreferences.Editor editor=prefs.edit();
            for(int i=0;i<ids.length();i++){int id=ids.getInt(i);editor.remove("known_"+id);editor.remove("wrong_"+id);}
            editor.putBoolean("content_revision_2",true).apply();
        }catch(Exception e){android.util.Log.e("Grammaire","Migration failed",e);}
    }
    private void extra(LinearLayout root,String heading,String content){
        LinearLayout panel=panel();
        TextView h=t(heading,13,true,GRAY);h.setLetterSpacing(0.06f);add(panel,h);gap(panel,10);
        TextView text=t(content,15,false,BLACK);text.setLineSpacing(dp(4),1.08f);
        add(panel,text);add(root,panel);gap(root,12);
    }
    private void showLesson(int id){
        selectedLesson=id;screen=2;Lesson l=lesson(id);ArrayList<Integer> ids=lessonIds(id);
        startScreen(l.title,"SECTION "+String.format(java.util.Locale.JAPAN,"%02d",id)+" / 50  ·  "+ids.size()+"例文",v->chapter(l.chapter));
        extra(mainArea,"01  文法の基本",l.rule);
        extra(mainArea,"02  間違えやすい点",l.pitfall);
        add(mainArea,t("03  例文を読み解く",20,true,BLACK));gap(mainArea,8);
        add(mainArea,t("例文を選ぶと、全文の構造・意味・その形になる理由を個別に確認できます。",14,false,GRAY));gap(mainArea,14);
        int position=0;
        for(int eid:ids){
            Example e=ex(eid);LinearLayout item=panel();
            add(item,t(String.format(java.util.Locale.JAPAN,"EXAMPLE %02d / %02d",++position,ids.size()),12,true,GRAY));gap(item,8);
            add(item,t(e.fr,19,true,BLACK));gap(item,7);add(item,t(e.ja,14,false,GRAY));gap(item,11);
            add(item,t("全文解説を読む   →",14,true,BLACK));
            item.setOnClickListener(v->showExample(e.id));
            add(mainArea,item);gap(mainArea,10);
        }
        gap(mainArea,11);
        add(mainArea,btn("04  この単元の確認問題（"+ids.size()+"問）  →",true,v->startQuiz(lessonIds(id))));
        gap(mainArea,16);
    }
    private void showExample(int id){
        selectedExample=id;Example e=ex(id);selectedLesson=e.lesson;ArrayList<Integer> ids=lessonIds(e.lesson);
        final int position=ids.indexOf(id);screen=3;
        startScreen("例文を読み解く",lesson(e.lesson).title+"  ·  "+(position+1)+" / "+ids.size(),v->showLesson(e.lesson));
        LinearLayout original=panel();add(original,t("EXAMPLE "+String.format(java.util.Locale.JAPAN,"%03d",id),12,true,GRAY));gap(original,15);
        TextView sentence=t(e.fr,24,true,BLACK);sentence.setGravity(Gravity.CENTER);add(original,sentence);gap(original,20);
        TextView meaning=t(e.ja,16,false,GRAY);meaning.setGravity(Gravity.CENTER);add(original,meaning);gap(original,20);
        add(original,btn("▶  例文を聞く",false,v->play(id)));add(mainArea,original);gap(mainArea,16);
        extra(mainArea,"01  文の構造",e.structure);
        extra(mainArea,"02  文全体の意味",e.whole);
        extra(mainArea,"03  この形になる理由",e.reason);
        extra(mainArea,"04  似た形との違い",e.contrast);
        extra(mainArea,"05  ここに注意",e.tip);
        add(mainArea,btn("この例文を1問だけ確認  →",true,v->{ArrayList<Integer> solo=new ArrayList<>();solo.add(id);startQuiz(solo);}));
        gap(mainArea,11);
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        Button prev=btn("← 前の例文",false,v->showExample(ids.get(position-1)));prev.setEnabled(position>0);
        nav.addView(prev,new LinearLayout.LayoutParams(0,dp(56),1));
        View sp=new View(this);nav.addView(sp,new LinearLayout.LayoutParams(dp(9),1));
        Button next=btn("次の例文 →",true,v->showExample(ids.get(position+1)));next.setEnabled(position+1<ids.size());
        nav.addView(next,new LinearLayout.LayoutParams(0,dp(56),1));add(mainArea,nav);
    }
    private void startQuiz(ArrayList<Integer> ids){quizFromHome=false;quiz.clear();quiz.addAll(ids);Collections.shuffle(quiz,random);quizIndex=0;answered=false;correctCount=0;failed.clear();quizAnswers.clear();showQuiz();}
    private void startMixedQuiz(){ArrayList<Integer> ids=new ArrayList<>();for(Example e:examples)ids.add(e.id);Collections.shuffle(ids,random);startQuiz(new ArrayList<>(ids.subList(0,10)));quizFromHome=true;}
    private void startWrongQuiz(){ArrayList<Integer> ids=new ArrayList<>();for(Example e:examples)if(prefs.getInt("wrong_"+e.id,0)>0)ids.add(e.id);if(ids.isEmpty()){Toast.makeText(this,"復習対象の例文はありません",Toast.LENGTH_SHORT).show();return;}Collections.shuffle(ids,random);startQuiz(new ArrayList<>(ids.subList(0,Math.min(10,ids.size()))));quizFromHome=true;}
    private ArrayList<String> choicesFor(Example e){ArrayList<String> other=new ArrayList<>();for(String opt:e.options)if(!opt.equals(e.answer))other.add(opt);Collections.shuffle(other,new Random(4171L+e.id));ArrayList<String> res=new ArrayList<>();res.add(e.answer);for(int i=0;i<3;i++)res.add(other.get(i));Collections.shuffle(res,new Random(9013L+e.id));return res;}
    private void showQuiz(){
        screen=5;if(quizIndex>=quiz.size()){quizFinished();return;}
        Example e=ex(quiz.get(quizIndex));Lesson l=lesson(e.lesson);
        startScreen("文法テスト","QUESTION "+(quizIndex+1)+" / "+quiz.size()+"  ·  "+l.title,v->{if(quizFromHome)home();else showLesson(e.lesson);});
        progress(mainArea,quizIndex,quiz.size());gap(mainArea,18);
        LinearLayout p=panel();add(p,t("正しい語句を選んでください",12,false,GRAY));gap(p,18);TextView q=t(e.blank,23,true,BLACK);q.setGravity(Gravity.CENTER);add(p,q);gap(p,16);TextView ja=t(e.ja,15,false,GRAY);ja.setGravity(Gravity.CENTER);add(p,ja);gap(p,11);add(p,t(e.questionHint,13,false,GRAY));add(mainArea,p);gap(mainArea,14);
        for(String opt:choicesFor(e)){Button b=btn(opt,false,v->answer(e,opt));b.setEnabled(!answered);add(mainArea,b);gap(mainArea,9);}
        if(answered){
            LinearLayout info=panel();add(info,t(selectedAnswer.equals(e.answer)?"✓ 正解":"正解："+e.answer,19,true,BLACK));gap(info,10);
            add(info,t("完成した文："+e.fr,17,true,BLACK));gap(info,12);add(info,t(e.reason,14,false,BLACK));gap(info,11);add(info,t(e.whole,14,false,BLACK));gap(info,9);add(info,t("注意："+e.tip,13,false,GRAY));add(mainArea,info);gap(mainArea,14);
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
        super.onSaveInstanceState(b);b.putInt("screen",screen);b.putInt("lesson",selectedLesson);b.putInt("selectedExample",selectedExample);b.putIntegerArrayList("deck",new ArrayList<>(deck));b.putInt("cardIndex",cardIndex);b.putBoolean("revealed",revealed);b.putIntegerArrayList("quiz",new ArrayList<>(quiz));b.putInt("quizIndex",quizIndex);b.putInt("correctCount",correctCount);b.putIntegerArrayList("failed",new ArrayList<>(failed));b.putStringArrayList("quizAnswers",new ArrayList<>(quizAnswers));b.putBoolean("answered",answered);b.putString("selectedAnswer",selectedAnswer);b.putBoolean("quizFromHome",quizFromHome);
    }
    private boolean restore(Bundle b){
        int s=b.getInt("screen",0);selectedLesson=b.getInt("lesson",1);if(selectedLesson<1||selectedLesson>50)return false;
        if(s==3){int id=b.getInt("selectedExample",1);if(id<1||id>500)return false;showExample(id);return true;}
        if(s==5){ArrayList<Integer> ids=b.getIntegerArrayList("quiz");ArrayList<Integer> misses=b.getIntegerArrayList("failed");ArrayList<String> a=b.getStringArrayList("quizAnswers");if(ids==null||ids.isEmpty()||ids.size()>500||a==null||misses==null)return false;for(int id:ids)if(id<1||id>500)return false;quiz.clear();quiz.addAll(ids);failed.clear();failed.addAll(misses);quizAnswers.clear();quizAnswers.addAll(a);quizIndex=b.getInt("quizIndex",0);correctCount=b.getInt("correctCount",0);answered=b.getBoolean("answered",false);selectedAnswer=b.getString("selectedAnswer","");quizFromHome=b.getBoolean("quizFromHome",false);if(quizIndex<0||quizIndex>=quiz.size()||quizAnswers.size()!=quizIndex+(answered?1:0))return false;showQuiz();return true;}
        if(s==2){showLesson(selectedLesson);return true;}return false;
    }
    @Override public void onBackPressed(){if(screen==0){super.onBackPressed();return;}if(screen==5&&quizFromHome)home();else if(screen==3||screen==5||screen==4)showLesson(selectedLesson);else home();}
}
