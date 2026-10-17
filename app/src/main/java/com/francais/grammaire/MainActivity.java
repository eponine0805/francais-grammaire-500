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
import android.widget.GridLayout;
import android.view.HapticFeedbackConstants;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.CheckBox;
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
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

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
    private final ArrayList<Integer> orderPicked=new ArrayList<>();
    private int wrongTotal=0;
    private boolean returningToHome=false;
    private boolean resultLogged=false;
    private String selectedAnswer="";
    private int selectedExample=1;
    private int quizReturnTab=0;
    private ScrollView activeScroll;

    private static class Lesson {
        final int id; final String chapter,title,rule,pitfall;
        Lesson(JSONObject o)throws Exception{id=o.getInt("id");chapter=o.getString("chapter");title=o.getString("title");rule=o.getString("rule");pitfall=o.getString("pitfall");}
    }
    private static class Example {
        final int id,lesson; final String fr,ja,blank,answer,detail,pitfall,structure,whole,reason,contrast,tip,questionHint; String compact,ipa; final ArrayList<String> options=new ArrayList<>();
        Example(JSONObject o)throws Exception{id=o.getInt("id");lesson=o.getInt("lesson");fr=o.getString("fr");ja=o.getString("ja");blank=o.getString("blank");answer=o.getString("answer");detail=o.getString("detail");pitfall=o.getString("pitfall");structure=o.getString("structure");whole=o.getString("whole");reason=o.getString("reason");contrast=o.getString("contrast");tip=o.getString("tip");questionHint=o.getString("questionHint");compact=o.optString("compact",reason);ipa=o.optString("ipa","");JSONArray a=o.getJSONArray("options");for(int i=0;i<a.length();i++)options.add(a.getString(i));}
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
            JSONArray annotations=new JSONArray(assetText("annotations.json"));
            if(annotations.length()!=500)throw new IllegalStateException("IPA/annotation pack incomplete");
            for(int i=0;i<annotations.length();i++){
                JSONObject note=annotations.getJSONObject(i);Example example=examples.get(i);
                if(note.getInt("id")!=example.id)throw new IllegalStateException("IPA ID mismatch at "+i);
                example.ipa=note.getString("ipa");example.compact=note.getString("brief");
                if(example.compact.isEmpty()||example.compact.length()>320||example.ipa.isEmpty())throw new IllegalStateException("Invalid annotation "+example.id);
            }
            if(lessons.size()!=50 || examples.size()!=500)throw new IllegalStateException("Lesson package incomplete");
        }catch(Exception ex){TextView v=new TextView(this);v.setText("教材データを読み込めません："+ex.getMessage());setContentView(v);return;}
        migrateChangedExamples();
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
    private void mainTabs(LinearLayout parent){
        gap(parent,12);LinearLayout tabs=new LinearLayout(this);
        String[] labels={"文法を学ぶ","文法テスト","復習"};int[] destinations={0,8,9};
        for(int i=0;i<3;i++){final int target=destinations[i];boolean active=(screen==target);
            TextView tab=t(labels[i],13,active,active?WHITE:BLACK);tab.setGravity(Gravity.CENTER);
            tab.setBackground(rounded(active?BLACK:BG,active?0:LINE,10));tab.setMinHeight(dp(46));
            tab.setContentDescription(labels[i]+(active?"、選択中":""));
            tab.setOnClickListener(v->{if(target==0)home();else if(target==8)showQuizSetup();else showReviewHub();});
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(46),1);
            if(i>0)params.leftMargin=dp(7);tabs.addView(tab,params);
        }
        add(parent,tabs);
    }
    private void startScreen(String title,String caption,View.OnClickListener back){
        stopAudio();base=vertical();base.setBackgroundColor(BG);setContentView(base);
        LinearLayout header=vertical();pad(header,20,17);header.setBackgroundColor(WHITE);
        if(back!=null){TextView b=t("← 戻る",14,false,GRAY);b.setOnClickListener(back);add(header,b);gap(header,13);}
        add(header,t(title,24,true,BLACK));if(caption!=null){gap(header,5);add(header,t(caption,12,false,GRAY));}
        if(screen==0||screen==8||screen==9)mainTabs(header);
        add(base,header);View line=new View(this);line.setBackgroundColor(LINE);base.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        ScrollView sc=new ScrollView(this);sc.setFillViewport(false);sc.setVerticalScrollBarEnabled(false);activeScroll=sc;base.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
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

    private void migrateChangedExamples(){
        if(prefs.getBoolean("content_revision_2",false))return;
        try{
            JSONObject j=new JSONObject(assetText("changed_ids.json"));
            JSONArray ids=j.getJSONArray("changed_ids");
            SharedPreferences.Editor edit=prefs.edit();
            for(int i=0;i<ids.length();i++){
                int id=ids.getInt(i);
                edit.remove("known_"+id);edit.remove("wrong_"+id);
            }
            edit.putBoolean("content_revision_2",true).apply();
        }catch(Exception e){android.util.Log.e("Grammaire", "Content revision failed", e);}
    }
    private void eyebrow(LinearLayout target,String label){
        TextView x=t(label,11,true,GRAY);x.setLetterSpacing(0.09f);add(target,x);
    }
    private void explainBlock(LinearLayout target,String heading,String text){
        LinearLayout p=panel();eyebrow(p,heading);gap(p,9);TextView v=t(text,15,false,BLACK);v.setLineSpacing(dp(4),1.09f);add(p,v);add(target,p);gap(target,12);
    }
    private void home(){
        screen=0;startScreen("フランス語 文法500","文法の仕組みを、例文から理解する",null);
        LinearLayout report=panel();eyebrow(report,"YOUR PROGRESS");gap(report,12);
        add(report,t("習得した例文  "+allDone()+" / 500",21,true,BLACK));gap(report,11);
        progress(report,allDone(),500);gap(report,8);
        add(report,t("今日の回答  "+prefs.getInt("today_count_"+today(),0)+" / 10   •   連続学習 "+prefs.getInt("study_streak",0)+"日",13,true,BLACK));gap(report,8);
        add(report,t("総回答数 "+prefs.getInt("total_attempts",0)+"   •   正答 "+prefs.getInt("total_correct",0)+"   •   XP "+prefs.getInt("xp",0),12,false,GRAY));gap(report,8);
        add(report,t("文法別50セクション・例文500文",12,false,GRAY));
        add(mainArea,report);gap(mainArea,15);
        add(mainArea,btn("学習記録を見る  →",false,v->showHistory()));gap(mainArea,24);
        add(mainArea,t("目次",22,true,BLACK));gap(mainArea,7);
        add(mainArea,t("文法事項を選び、解説 → 例文 → 確認問題の順で学びます。",14,false,GRAY));gap(mainArea,15);
        LinkedHashMap<String,ArrayList<Lesson>> chapters=new LinkedHashMap<>();
        for(Lesson l:lessons){if(!chapters.containsKey(l.chapter))chapters.put(l.chapter,new ArrayList<>());chapters.get(l.chapter).add(l);}
        int idx=0;
        for(String key:chapters.keySet()){
            ArrayList<Lesson> list=chapters.get(key);int total=0,done=0;
            for(Lesson l:list){total+=lessonIds(l.id).size();done+=doneInLesson(l.id);}
            LinearLayout entry=panel();eyebrow(entry,String.format(java.util.Locale.JAPAN,"CHAPTER %02d",++idx));gap(entry,5);
            add(entry,t(key+"   →",19,true,BLACK));gap(entry,7);
            add(entry,t(list.size()+"単元・"+total+"文  /  習得 "+done+"文",13,false,GRAY));
            entry.setOnClickListener(v->chapter(key));add(mainArea,entry);gap(mainArea,10);
        }
    }
    private void chapter(String name){
        screen=1;startScreen(name,"学習する文法事項を選択",v->home());
        for(Lesson l:lessons){if(!l.chapter.equals(name))continue;
            int total=lessonIds(l.id).size();
            LinearLayout entry=panel();eyebrow(entry,String.format(java.util.Locale.JAPAN,"SECTION %02d / 50",l.id));gap(entry,8);
            add(entry,t(l.title+"   →",18,true,BLACK));gap(entry,9);
            add(entry,t(total+"例文  •  習得 "+doneInLesson(l.id)+" / "+total,13,false,GRAY));
            entry.setOnClickListener(v->showLesson(l.id));add(mainArea,entry);gap(mainArea,10);
        }
    }
    private void showLesson(int id){
        selectedLesson=id;screen=2;Lesson l=lesson(id);int count=lessonIds(id).size();
        startScreen(l.title,String.format(java.util.Locale.JAPAN,"SECTION %02d / 50   •   %d例文",id,count),v->chapter(l.chapter));
        explainBlock(mainArea,"01  THE RULE / 文法の規則",l.rule);
        explainBlock(mainArea,"02  WATCH OUT / 間違いやすい点",l.pitfall);
        add(mainArea,t("03  例文を読み解く",20,true,BLACK));gap(mainArea,6);
        add(mainArea,t("例文をタップすると、文の構造・その形になる理由・注意点を確認できます。",13,false,GRAY));gap(mainArea,14);
        int n=0;
        for(int eid:lessonIds(id)){
            final Example e=ex(eid);
            LinearLayout item=panel();eyebrow(item,String.format(java.util.Locale.JAPAN,"EXAMPLE %02d  /  %02d",++n,count));gap(item,8);
            add(item,t(e.fr,18,true,BLACK));gap(item,7);
            add(item,t(e.ja,14,false,GRAY));gap(item,10);
            add(item,t("構文と解説を読む  →",13,true,BLACK));
            item.setOnClickListener(v->showExample(e.id));add(mainArea,item);gap(mainArea,9);
        }
        gap(mainArea,10);add(mainArea,btn("04  この単元の確認問題（"+count+"問）  →",true,v->startQuiz(lessonIds(id))));
        gap(mainArea,18);
    }
    private void showExample(int id){
        selectedExample=id;Example e=ex(id);selectedLesson=e.lesson;Lesson l=lesson(e.lesson);
        ArrayList<Integer> ids=lessonIds(e.lesson);int at=ids.indexOf(id);screen=3;
        startScreen("例文と文法",l.title+"   •   "+(at+1)+" / "+ids.size(),v->showLesson(e.lesson));
        LinearLayout sentence=panel();eyebrow(sentence,"EXAMPLE "+String.format(Locale.JAPAN,"%03d",id));gap(sentence,12);
        TextView fr=t(e.fr,24,true,BLACK);fr.setGravity(Gravity.CENTER);add(sentence,fr);gap(sentence,9);
        TextView ipa=t(e.ipa+"  ※自動推定",13,false,GRAY);ipa.setGravity(Gravity.CENTER);add(sentence,ipa);gap(sentence,13);
        TextView ja=t(e.ja,16,false,BLACK);ja.setGravity(Gravity.CENTER);add(sentence,ja);gap(sentence,12);
        add(sentence,btn("▶  発音を聞く",false,v->play(id)));add(mainArea,sentence);gap(mainArea,13);
        explainBlock(mainArea,"この例文の文法ポイント",e.compact);
        add(mainArea,btn("この例文を問題で確認",true,v->{ArrayList<Integer> one=new ArrayList<>();one.add(e.id);startQuiz(one);}));gap(mainArea,12);
        TextView star=t(starred(id)?"★  保存済み":"☆  この例文を保存",14,false,GRAY);
        star.setGravity(Gravity.CENTER);star.setOnClickListener(v->{prefs.edit().putBoolean("star_"+id,!starred(id)).apply();star.setText(starred(id)?"★  保存済み":"☆  この例文を保存");});add(mainArea,star);gap(mainArea,15);
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        Button previous=btn("← 前",false,v->showExample(ids.get(at-1)));previous.setEnabled(at>0);previous.setAlpha(at>0?1f:0.35f);
        nav.addView(previous,new LinearLayout.LayoutParams(0,dp(56),1));View sep=new View(this);nav.addView(sep,new LinearLayout.LayoutParams(dp(9),1));
        Button next=btn("次の例文 →",true,v->showExample(ids.get(at+1)));next.setEnabled(at+1<ids.size());next.setAlpha(at+1<ids.size()?1f:0.35f);
        nav.addView(next,new LinearLayout.LayoutParams(0,dp(56),2));add(bottomBar(),nav);
        // Start audio once when a new example screen opens.
        fr.post(()->{if(screen==3 && selectedExample==id && !isFinishing())play(id);});
    }
    /** Flexible scope and question count, separate from focused one-section tests. */
    private void showQuizSetup(){
        screen=8;
        startScreen("文法テスト","範囲と問題数を設定",v->home());
        if(prefs.contains("pending_quiz_v3")){add(mainArea,btn("▶  前回のテストを再開",true,v->resumeSavedQuiz()));gap(mainArea,12);}
        LinearLayout settings=panel();
        eyebrow(settings,"RANGE / 文法単元の範囲");gap(settings,10);
        add(settings,t("開始する単元",14,true,BLACK));gap(settings,5);
        ArrayList<String> names=new ArrayList<>();
        for(Lesson item:lessons)names.add(String.format(Locale.JAPAN,"%02d  %s",item.id,item.title));
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names);
        Spinner start=new Spinner(this);start.setAdapter(adapter);
        start.setSelection(Math.max(0,Math.min(49,prefs.getInt("quiz_range_start",1)-1)));add(settings,start);gap(settings,10);
        add(settings,t("終了する単元",14,true,BLACK));gap(settings,5);
        Spinner end=new Spinner(this);end.setAdapter(adapter);
        end.setSelection(Math.max(0,Math.min(49,prefs.getInt("quiz_range_end",50)-1)));add(settings,end);
        gap(settings,10);
        add(settings,btn("全50単元を選択  （1〜50）",false,v->{start.setSelection(0);end.setSelection(49);
            Toast.makeText(this,"全50単元に設定しました",Toast.LENGTH_SHORT).show();}));
        gap(settings,16);eyebrow(settings,"COUNT / 問題数");gap(settings,7);
        TextView count=t("10問",18,true,BLACK);add(settings,count);gap(settings,5);
        SeekBar seek=new SeekBar(this);seek.setMax(9);
        int previousCount=prefs.getInt("quiz_range_count",10);
        seek.setProgress(Math.max(0,Math.min(9,Math.round((previousCount-10)/10f))));add(settings,seek);
        add(settings,t("10〜100問から10問刻みで選択（範囲内が少ない場合は全問）",12,false,GRAY));gap(settings,12);
        CheckBox all=new CheckBox(this);all.setText("選択した単元の全問を出題");all.setTextSize(15);
        all.setChecked(prefs.getBoolean("quiz_range_all",false));add(settings,all);gap(settings,9);
        final Runnable updateCount=()->{seek.setEnabled(!all.isChecked());seek.setAlpha(all.isChecked()?0.45f:1f);
            count.setText(all.isChecked()?"範囲内の全問":(10+seek.getProgress()*10)+"問");};
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean user){updateCount.run();}
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){}
        });
        all.setOnCheckedChangeListener((v,checked)->updateCount.run());updateCount.run();
        add(mainArea,settings);gap(mainArea,12);
        add(mainArea,t("出題順は毎回シャッフルされます。選んだ範囲以外の例文は出題しません。",13,false,GRAY));
        LinearLayout footer=bottomBar();
        add(footer,btn("この範囲でテスト開始  →",true,v->{
            int first=start.getSelectedItemPosition()+1,last=end.getSelectedItemPosition()+1;
            if(first>last){Toast.makeText(this,"開始単元が終了単元より後になっています",Toast.LENGTH_LONG).show();return;}
            ArrayList<Integer> candidates=quizRange(first,last);
            if(candidates.isEmpty()){Toast.makeText(this,"この範囲には例文がありません",Toast.LENGTH_SHORT).show();return;}
            int requested=10+seek.getProgress()*10;
            int questions=all.isChecked()?candidates.size():Math.min(requested,candidates.size());
            prefs.edit().putInt("quiz_range_start",first).putInt("quiz_range_end",last)
                .putInt("quiz_range_count",requested).putBoolean("quiz_range_all",all.isChecked()).apply();
            Collections.shuffle(candidates,random);
            startQuiz(new ArrayList<>(candidates.subList(0,questions)));
            quizFromHome=true;quizReturnTab=8;saveQuizSession();
        }));
    }
    private ArrayList<Integer> quizRange(int first,int last){
        ArrayList<Integer> ids=new ArrayList<>();
        for(Example example:examples)if(example.lesson>=first&&example.lesson<=last)ids.add(example.id);
        return ids;
    }
    private void startQuiz(ArrayList<Integer> ids){
        quizFromHome=false;quiz.clear();quiz.addAll(ids);Collections.shuffle(quiz,random);
        quizIndex=0;answered=false;selectedAnswer="";correctCount=0;failed.clear();quizAnswers.clear();orderPicked.clear();resultLogged=false;quizReturnTab=0;saveQuizSession();showQuiz();
    }
    private void startWrongQuiz(){ArrayList<Integer> ids=new ArrayList<>();for(Example e:examples)if(prefs.getInt("wrong_"+e.id,0)>0)ids.add(e.id);if(ids.isEmpty()){Toast.makeText(this,"復習対象の問題はありません",Toast.LENGTH_SHORT).show();return;}Collections.shuffle(ids,random);startQuiz(new ArrayList<>(ids.subList(0,Math.min(10,ids.size()))));quizFromHome=true;quizReturnTab=9;saveQuizSession();}
    private ArrayList<String> choicesFor(Example e){
        ArrayList<String> other=new ArrayList<>();for(String o:e.options)if(!o.equals(e.answer)&&!other.contains(o))other.add(o);
        Collections.shuffle(other,new Random(4171L+e.id));ArrayList<String> res=new ArrayList<>();res.add(e.answer);
        for(int i=0;i<3;i++)res.add(other.get(i));Collections.shuffle(res,new Random(9013L+e.id));return res;
    }
    private void startDueQuiz(){
        ArrayList<Integer> due=new ArrayList<>();long now=System.currentTimeMillis();
        for(Example e:examples)if(prefs.getInt("attempts_"+e.id,0)>0 && prefs.getLong("due_"+e.id,Long.MAX_VALUE)<=now)due.add(e.id);
        if(due.isEmpty()){Toast.makeText(this,"復習の予定はまだありません",Toast.LENGTH_SHORT).show();return;}
        Collections.shuffle(due,random);startQuiz(new ArrayList<>(due.subList(0,Math.min(10,due.size()))));quizFromHome=true;quizReturnTab=9;saveQuizSession();
    }
    private void showReviewHub(){
        screen=9;startScreen("復習","今日の復習と間違い直しをまとめて管理",v->home());
        int todayDue=0,wrong=0;long now=System.currentTimeMillis();
        for(Example item:examples){
            if(prefs.getInt("wrong_"+item.id,0)>0)wrong++;
            if(prefs.getInt("attempts_"+item.id,0)>0 && prefs.getLong("due_"+item.id,Long.MAX_VALUE)<=now)todayDue++;
        }
        LinearLayout summary=panel();eyebrow(summary,"REVIEW");gap(summary,9);
        add(summary,t("今日の復習 "+todayDue+"問  /  苦手 "+wrong+"問",18,true,BLACK));gap(summary,7);
        add(summary,t("それぞれ最大10問。両方に入る問題もあります。",12,false,GRAY));add(mainArea,summary);gap(mainArea,12);
        LinearLayout a=panel();eyebrow(a,"01 / 忘れかけた文法");gap(a,8);
        add(a,t("今日の復習",19,true,BLACK));gap(a,7);
        add(a,t(todayDue>0?"復習予定日を迎えた問題を出題します。":"今日は復習予定がありません。",13,false,GRAY));gap(a,12);
        Button startDue=btn(todayDue>0?"今日の復習を始める  →":"復習予定なし",true,v->startDueQuiz());
        startDue.setEnabled(todayDue>0);startDue.setAlpha(todayDue>0?1f:0.45f);add(a,startDue);add(mainArea,a);gap(mainArea,12);
        LinearLayout b=panel();eyebrow(b,"02 / 間違えた文法");gap(b,8);
        add(b,t("間違えた問題",19,true,BLACK));gap(b,7);
        add(b,t(wrong>0?"未克服の間違いを出題。正解すると苦手から外れます。":"復習待ちの間違いはありません。",13,false,GRAY));gap(b,12);
        Button startWrong=btn(wrong>0?"苦手問題を解き直す  →":"苦手問題なし",false,v->startWrongQuiz());
        startWrong.setEnabled(wrong>0);startWrong.setAlpha(wrong>0?1f:0.45f);add(b,startWrong);add(mainArea,b);gap(mainArea,12);
        add(mainArea,btn("学習記録を見る  →",false,v->showHistory()));
    }
    private void returnToOrigin(){
        if(quizFromHome && quizReturnTab==9)showReviewHub();
        else if(quizFromHome && quizReturnTab==8)showQuizSetup();
        else if(quizFromHome)home();else showLesson(selectedLesson);
    }
    private void showHistory(){
        screen=7;startScreen("学習記録","正答・苦手・取り組んだ回数を確認",v->home());
        LinearLayout summary=panel();eyebrow(summary,"累計");gap(summary,10);
        int attempts=prefs.getInt("total_attempts",0),correct=prefs.getInt("total_correct",0);
        add(summary,t(""+attempts+"問に挑戦  •  "+correct+"問正解",20,true,BLACK));gap(summary,8);
        add(summary,t("正答率 "+(attempts==0?0:Math.round(100f*correct/attempts))+"%   •   XP "+prefs.getInt("xp",0),15,false,GRAY));
        add(mainArea,summary);gap(mainArea,16);
        add(mainArea,t("最近のテスト",19,true,BLACK));gap(mainArea,9);
        String log=prefs.getString("quiz_history_v3","[]");
        try{JSONArray ar=new JSONArray(log);
            if(ar.length()==0)add(mainArea,t("最初のテストを終えると履歴が記録されます。",14,false,GRAY));
            for(int i=ar.length()-1;i>=Math.max(0,ar.length()-15);i--){
                JSONObject rec=ar.getJSONObject(i);LinearLayout item=panel();
                add(item,t(rec.getString("date")+"  •  "+rec.getInt("correct")+" / "+rec.getInt("total")+" 正解",15,true,BLACK));
                add(mainArea,item);gap(mainArea,7);
            }
        }catch(Exception error){add(mainArea,t("テスト履歴を読み込めませんでした",14,false,GRAY));}
        gap(mainArea,16);add(mainArea,t("文法項目別の習得状況",19,true,BLACK));gap(mainArea,8);
        for(Lesson l:lessons){int attemptsOf=0,mastery=0;for(int id:lessonIds(l.id)){attemptsOf+=prefs.getInt("attempts_"+id,0);if(known(id))mastery++;}
            if(attemptsOf==0)continue;LinearLayout item=panel();
            add(item,t(l.title,15,true,BLACK));gap(item,5);
            add(item,t("回答 "+attemptsOf+"回   •   習得 "+mastery+" / "+lessonIds(l.id).size(),13,false,GRAY));
            item.setOnClickListener(v->showLesson(l.id));add(mainArea,item);gap(mainArea,8);
        }
    }
    private void logFinishedResult(){
        if(resultLogged)return;resultLogged=true;
        try{JSONArray arr=new JSONArray(prefs.getString("quiz_history_v3","[]"));
            JSONObject row=new JSONObject();row.put("date",new SimpleDateFormat("yyyy/MM/dd HH:mm",Locale.JAPAN).format(new java.util.Date()));
            row.put("correct",correctCount);row.put("total",quiz.size());arr.put(row);
            JSONArray limited=new JSONArray();for(int i=Math.max(0,arr.length()-40);i<arr.length();i++)limited.put(arr.get(i));
            prefs.edit().putString("quiz_history_v3",limited.toString()).commit();
        }catch(Exception error){android.util.Log.e("Grammaire","Failed to save test history",error);}
    }
    private boolean isOrderQuiz(Example e){return quiz.size()>1&&quizIndex%2==1&&e.fr.trim().split("\\s+").length>=3&&e.fr.trim().split("\\s+").length<=12;}
    private ArrayList<String> sentenceTokens(Example e){ArrayList<String> out=new ArrayList<>();for(String tok:e.fr.trim().split("\\s+"))out.add(tok);return out;}
    private ArrayList<Integer> shuffledTokens(Example e){
        ArrayList<Integer> ids=new ArrayList<>();for(int i=0;i<sentenceTokens(e).size();i++)ids.add(i);
        Collections.shuffle(ids,new Random(95249L+e.id));return ids;
    }
    private String composedAnswer(Example e){ArrayList<String> words=sentenceTokens(e);StringBuilder out=new StringBuilder();for(int p:orderPicked){if(out.length()>0)out.append(' ');out.append(words.get(p));}return out.toString();}
    private String today(){return new SimpleDateFormat("yyyyMMdd",Locale.US).format(new java.util.Date());}
    private void recordAnswer(Example e,boolean ok){
        String day=today();String prior=prefs.getString("last_study_day","");
        int streak=prefs.getInt("study_streak",0);
        Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_MONTH,-1);
        String yesterday=new SimpleDateFormat("yyyyMMdd",Locale.US).format(c.getTime());
        if(!day.equals(prior))streak=yesterday.equals(prior)?streak+1:1;
        int times=prefs.getInt("attempts_"+e.id,0)+1;
        int level=ok?Math.min(5,prefs.getInt("stage_"+e.id,0)+1):0;
        long[] days={1,1,3,7,14,30};
        prefs.edit().putInt("attempts_"+e.id,times).putInt("stage_"+e.id,level)
          .putLong("due_"+e.id,System.currentTimeMillis()+days[level]*86400000L)
          .putInt("total_attempts",prefs.getInt("total_attempts",0)+1)
          .putInt("total_correct",prefs.getInt("total_correct",0)+(ok?1:0))
          .putInt("today_count_"+day,prefs.getInt("today_count_"+day,0)+1)
          .putInt("xp",prefs.getInt("xp",0)+(ok?10:2))
          .putString("last_study_day",day).putInt("study_streak",streak)
          .putBoolean("known_"+e.id,ok)
          .putInt("wrong_"+e.id,ok?0:prefs.getInt("wrong_"+e.id,0)+1).commit();
    }
    private LinearLayout bottomBar(){
        LinearLayout b=vertical();pad(b,16,12);b.setBackgroundColor(WHITE);
        View line=new View(this);line.setBackgroundColor(LINE);base.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        base.addView(b,new LinearLayout.LayoutParams(-1,-2));return b;
    }
    private Button optionButton(String label,int rowMode,View.OnClickListener click){
        Button b=btn(label,false,click);b.setTextSize(16);b.setGravity(Gravity.CENTER);
        boolean chosen=answered&&label.equals(selectedAnswer);
        if(answered&&label.equals(ex(quiz.get(quizIndex)).answer)){
            b.setBackground(rounded(Color.rgb(227,245,231),Color.rgb(52,144,80),12));b.setTextColor(Color.rgb(23,108,47));
        }else if(chosen){b.setBackground(rounded(Color.rgb(252,233,233),Color.rgb(197,55,55),12));b.setTextColor(Color.rgb(159,36,36));}
        b.setEnabled(!answered);return b;
    }
    private void showQuiz(){
        screen=5;if(quizIndex>=quiz.size()){quizFinished();return;}
        Example e=ex(quiz.get(quizIndex));Lesson l=lesson(e.lesson);boolean order=isOrderQuiz(e);
        startScreen("文法チェック",l.title+"    "+(quizIndex+1)+" / "+quiz.size(),v->returnToOrigin());
        progress(mainArea,quizIndex,quiz.size());gap(mainArea,11);
        LinearLayout q=panel();eyebrow(q,order?"語順を組み立てる":"空欄に入る語句を選ぶ");gap(q,12);
        TextView s=t(order?e.ja:e.blank,21,true,BLACK);s.setGravity(Gravity.CENTER);add(q,s);gap(q,12);
        if(!order){TextView ja=t(e.ja,14,false,GRAY);ja.setGravity(Gravity.CENTER);add(q,ja);}
        else{TextView sub=t("単語を順番にタップして文章を完成させよう",13,false,GRAY);sub.setGravity(Gravity.CENTER);add(q,sub);}
        add(mainArea,q);gap(mainArea,11);
        if(order){
            ArrayList<String> words=sentenceTokens(e);
            LinearLayout assembled=panel();eyebrow(assembled,"あなたの答え   •   タップで1語取り消し");gap(assembled,8);
            GridLayout current=new GridLayout(this);current.setColumnCount(3);
            for(int k=0;k<orderPicked.size();k++){
                final int pos=k;int tokenId=orderPicked.get(k);
                Button chip=btn(words.get(tokenId),false,v->{if(!answered){int y=activeScroll.getScrollY();orderPicked.remove(pos);saveQuizSession();showQuiz();restoreQuizScroll(y);}});
                chip.setTextSize(13);chip.setMinHeight(dp(42));pad(chip,3,3);
                GridLayout.LayoutParams par=new GridLayout.LayoutParams();par.width=0;par.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);par.height=dp(49);par.setMargins(dp(2),dp(2),dp(2),dp(2));current.addView(chip,par);
            }
            add(assembled,current);
            if(orderPicked.isEmpty())add(assembled,t("ここに単語が並びます",13,false,GRAY));
            add(mainArea,assembled);gap(mainArea,10);
            GridLayout bank=new GridLayout(this);bank.setColumnCount(3);
            for(int i:shuffledTokens(e)){
                final int token=i;Button chip=btn(words.get(i),false,v->{if(!answered&&!orderPicked.contains(token)){int y=activeScroll.getScrollY();orderPicked.add(token);saveQuizSession();showQuiz();restoreQuizScroll(y);}});
                chip.setTextSize(13);chip.setMinHeight(dp(44));pad(chip,3,3);
                boolean used=orderPicked.contains(token);chip.setEnabled(!answered&&!used);chip.setAlpha(used?0.25f:1f);
                GridLayout.LayoutParams par=new GridLayout.LayoutParams();par.width=0;par.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);par.height=dp(50);par.setMargins(dp(2),dp(2),dp(2),dp(2));bank.addView(chip,par);
            }
            add(mainArea,bank);
        }else{
            for(String option:choicesFor(e)){
                Button choice=optionButton(option,0,v->answer(e,option));add(mainArea,choice);gap(mainArea,7);
            }
        }
        if(answered){
            gap(mainArea,9);
            LinearLayout details=panel();eyebrow(details,"この文の文法解説");gap(details,9);
            add(details,t(e.compact,14,false,BLACK));gap(details,8);
            add(details,t(e.ipa+"  ※IPA自動推定",12,false,GRAY));gap(details,8);
            add(details,btn("▶  正しい例文の発音",false,v->play(e.id)));add(mainArea,details);
        }
        gap(mainArea,12);
        LinearLayout footer=bottomBar();
        if(answered){
            boolean ok=selectedAnswer.equals(e.answer)||selectedAnswer.equals(e.fr);
            TextView status=t(ok?"✓  正解！ +10 XP":"✕  不正解  •  正解を確認",18,true,ok?Color.rgb(23,115,50):Color.rgb(170,35,35));
            status.setGravity(Gravity.CENTER);add(footer,status);gap(footer,5);
            TextView correct=t(e.fr,14,true,BLACK);correct.setGravity(Gravity.CENTER);add(footer,correct);gap(footer,8);
            Button next=btn(quizIndex+1<quiz.size()?"次の問題へ  →":"結果を見る  →",true,v->{quizIndex++;answered=false;selectedAnswer="";orderPicked.clear();saveQuizSession();showQuiz();});
            add(footer,next);
        }else if(order){
            add(footer,btn("答え合わせ  （"+orderPicked.size()+" / "+sentenceTokens(e).size()+"語）",orderPicked.size()==sentenceTokens(e).size(),v->{if(orderPicked.size()==sentenceTokens(e).size())answer(e,composedAnswer(e));}));
        }else{
            TextView instruction=t("答えを選ぶとすぐに正誤が分かります",13,false,GRAY);instruction.setGravity(Gravity.CENTER);add(footer,instruction);
        }
    }
    private void restoreQuizScroll(int y){ScrollView sc=activeScroll;if(sc!=null)sc.post(()->{if(screen==5&&activeScroll==sc)sc.scrollTo(0,y);});}
    private void answer(Example e,String chosen){
        if(answered)return;answered=true;selectedAnswer=chosen;quizAnswers.add(chosen);
        boolean ok=chosen.equals(isOrderQuiz(e)?e.fr:e.answer);
        if(ok){correctCount++;}else{failed.add(e.id);}
        recordAnswer(e,ok);saveQuizSession();showQuiz();
    }
    private void saveQuizSession(){
        try{
            JSONObject data=new JSONObject();data.put("index",quizIndex);data.put("correct",correctCount);
            data.put("answered",answered);data.put("selected",selectedAnswer);data.put("home",quizFromHome);data.put("returnTab",quizReturnTab);
            JSONArray ids=new JSONArray();for(int id:quiz)ids.put(id);data.put("ids",ids);
            JSONArray answers=new JSONArray();for(String a:quizAnswers)answers.put(a);data.put("answers",answers);
            JSONArray fails=new JSONArray();for(int id:failed)fails.put(id);data.put("failed",fails);
            JSONArray ordered=new JSONArray();for(int id:orderPicked)ordered.put(id);data.put("order",ordered);
            if(quizIndex>=quiz.size())prefs.edit().remove("pending_quiz_v3").commit();
            else prefs.edit().putString("pending_quiz_v3",data.toString()).commit();
        }catch(Exception error){android.util.Log.e("Grammaire","Cannot save quiz",error);}
    }
    private void resumeSavedQuiz(){
        try{
            JSONObject data=new JSONObject(prefs.getString("pending_quiz_v3",""));
            JSONArray ids=data.getJSONArray("ids"),answers=data.getJSONArray("answers"),fails=data.getJSONArray("failed"),ordered=data.getJSONArray("order");
            if(ids.length()<1||ids.length()>500)throw new IllegalStateException("bad saved quiz");
            quiz.clear();for(int i=0;i<ids.length();i++){int id=ids.getInt(i);if(id<1||id>500)throw new IllegalStateException("bad example");quiz.add(id);}
            quizIndex=data.getInt("index");if(quizIndex<0||quizIndex>=quiz.size())throw new IllegalStateException("bad index");
            answered=data.getBoolean("answered");selectedAnswer=data.getString("selected");correctCount=data.getInt("correct");quizFromHome=data.getBoolean("home");quizReturnTab=data.optInt("returnTab",0);
            quizAnswers.clear();for(int i=0;i<answers.length();i++)quizAnswers.add(answers.getString(i));
            failed.clear();for(int i=0;i<fails.length();i++)failed.add(fails.getInt(i));
            orderPicked.clear();for(int i=0;i<ordered.length();i++)orderPicked.add(ordered.getInt(i));
            if(quizAnswers.size()!=quizIndex+(answered?1:0))throw new IllegalStateException("invalid saved history");
            showQuiz();
        }catch(Exception error){prefs.edit().remove("pending_quiz_v3").apply();Toast.makeText(this,"保存データを復元できませんでした",Toast.LENGTH_SHORT).show();home();}
    }
    private void quizFinished(){
        screen=6;logFinishedResult();prefs.edit().remove("pending_quiz_v3").commit();startScreen("確認テストの結果","解説を読んで、必要な部分を復習しましょう",v->returnToOrigin());
        LinearLayout summary=panel();eyebrow(summary,"SCORE");gap(summary,10);
        add(summary,t(correctCount+" / "+quiz.size()+" 正解",29,true,BLACK));gap(summary,12);
        add(summary,t("正答率 "+Math.round(100f*correctCount/Math.max(1,quiz.size()))+"%   •   総XP "+prefs.getInt("xp",0),16,false,GRAY));gap(summary,12);progress(summary,correctCount,quiz.size());
        add(mainArea,summary);gap(mainArea,16);
        if(!failed.isEmpty()){
            add(mainArea,btn("間違えた"+failed.size()+"問をもう一度  →",true,v->startQuiz(new ArrayList<>(failed))));gap(mainArea,16);
            add(mainArea,t("間違えた問題の解説",18,true,BLACK));gap(mainArea,9);
            int shown=Math.min(30,failed.size());
            for(int i=0;i<shown;i++){int id=failed.get(i);Example e=ex(id);LinearLayout item=panel();add(item,t(e.fr,16,true,BLACK));gap(item,6);add(item,t(e.ja,14,false,GRAY));gap(item,8);add(item,t("解説を読む →",13,true,BLACK));item.setOnClickListener(v->showExample(e.id));add(mainArea,item);gap(mainArea,9);}
            if(failed.size()>shown){add(mainArea,t("残り"+(failed.size()-shown)+"問は上の再テストですべて復習できます。",14,false,GRAY));gap(mainArea,12);}
        }else{add(mainArea,t("全問正解。次の文法事項へ進みましょう。",16,true,BLACK));gap(mainArea,13);}
        add(mainArea,btn(quizFromHome?(quizReturnTab==9?"復習に戻る":quizReturnTab==8?"文法テストに戻る":"学習に戻る"):"単元に戻る",false,v->returnToOrigin()));
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
        super.onSaveInstanceState(b);b.putInt("screen",screen);b.putInt("lesson",selectedLesson);b.putInt("selectedExample",selectedExample);b.putIntegerArrayList("deck",new ArrayList<>(deck));b.putInt("cardIndex",cardIndex);b.putBoolean("revealed",revealed);b.putIntegerArrayList("quiz",new ArrayList<>(quiz));b.putInt("quizIndex",quizIndex);b.putInt("correctCount",correctCount);b.putIntegerArrayList("failed",new ArrayList<>(failed));b.putStringArrayList("quizAnswers",new ArrayList<>(quizAnswers));b.putBoolean("answered",answered);b.putString("selectedAnswer",selectedAnswer);b.putBoolean("quizFromHome",quizFromHome);b.putInt("quizReturnTab",quizReturnTab);b.putIntegerArrayList("orderPicked",new ArrayList<>(orderPicked));
    }
    private boolean restore(Bundle b){
        int s=b.getInt("screen",0);selectedLesson=b.getInt("lesson",1);if(selectedLesson<1||selectedLesson>50)return false;
        if(s==3){int eid=b.getInt("selectedExample",1);if(eid<1||eid>500)return false;showExample(eid);return true;}
        if(s==5){ArrayList<Integer> ids=b.getIntegerArrayList("quiz");ArrayList<Integer> misses=b.getIntegerArrayList("failed");ArrayList<String> a=b.getStringArrayList("quizAnswers");if(ids==null||ids.isEmpty()||ids.size()>500||a==null||misses==null)return false;for(int id:ids)if(id<1||id>500)return false;quiz.clear();quiz.addAll(ids);failed.clear();failed.addAll(misses);quizAnswers.clear();quizAnswers.addAll(a);quizIndex=b.getInt("quizIndex",0);correctCount=b.getInt("correctCount",0);answered=b.getBoolean("answered",false);selectedAnswer=b.getString("selectedAnswer","");quizFromHome=b.getBoolean("quizFromHome",false);quizReturnTab=b.getInt("quizReturnTab",0);orderPicked.clear();ArrayList<Integer> o=b.getIntegerArrayList("orderPicked");if(o!=null)orderPicked.addAll(o);if(quizIndex<0||quizIndex>=quiz.size()||quizAnswers.size()!=quizIndex+(answered?1:0))return false;showQuiz();return true;}
        if(s==2){showLesson(selectedLesson);return true;}if(s==7){showHistory();return true;}if(s==8){showQuizSetup();return true;}if(s==9){showReviewHub();return true;}return false;
    }
    @Override public void onBackPressed(){
        if(screen==0){super.onBackPressed();return;}
        if(screen==5||screen==6)returnToOrigin();
        else if(screen==8||screen==9)home();
        else if(screen==3)showLesson(selectedLesson);
        else home();
    }
}
