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
    private final ArrayList<CourseChapter> courseChapters=new ArrayList<>();
    private final ArrayList<CourseUnit> courseUnits=new ArrayList<>();
    private int currentCourseChapter=0,currentCourseUnit=1,coursePhase=0;
    private boolean conceptAnswered=false,conceptCorrect=false,courseQuizFromReview=false;
    private final ArrayList<Integer> conceptPicked=new ArrayList<>(),conceptQueue=new ArrayList<>();
    private int conceptIndex=0,conceptTotalCorrect=0;
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
    private int quizReturnTab=0; // 0: 学習 / 8: テスト / 9: 復習
    private ScrollView activeScroll;

    private static class Lesson {
        final int id; final String chapter,title,rule,pitfall;
        Lesson(JSONObject o)throws Exception{id=o.getInt("id");chapter=o.getString("chapter");title=o.getString("title");rule=o.getString("rule");pitfall=o.getString("pitfall");}
    }
    private static class Example {
        final int id,lesson; final String fr,ja,blank,answer,detail,pitfall,structure,whole,reason,contrast,tip,questionHint; String compact,ipa; final ArrayList<String> options=new ArrayList<>();
        Example(JSONObject o)throws Exception{id=o.getInt("id");lesson=o.getInt("lesson");fr=o.getString("fr");ja=o.getString("ja");blank=o.getString("blank");answer=o.getString("answer");detail=o.getString("detail");pitfall=o.getString("pitfall");structure=o.getString("structure");whole=o.getString("whole");reason=o.getString("reason");contrast=o.getString("contrast");tip=o.getString("tip");questionHint=o.getString("questionHint");compact=o.optString("compact",reason);ipa=o.optString("ipa","");JSONArray a=o.getJSONArray("options");for(int i=0;i<a.length();i++)options.add(a.getString(i));}
    }
    private static class CourseChapter {
        final int id,sourcePage;final String title,goal;final ArrayList<Integer> legacy=new ArrayList<>();
        CourseChapter(JSONObject obj)throws Exception{
            id=obj.getInt("id");sourcePage=obj.getInt("sourcePage");title=obj.getString("title");goal=obj.getString("goal");
            JSONArray ids=obj.getJSONArray("legacyLessons");for(int i=0;i<ids.length();i++)legacy.add(ids.getInt(i));
        }
    }
    private static class CourseUnit {
        final int id,chapter;final String title,rule,fr,ja,focus,question,answer,why;String ipa="";
        final ArrayList<String> contrast=new ArrayList<>(),options=new ArrayList<>();
        CourseUnit(JSONObject obj)throws Exception{
            id=obj.getInt("id");chapter=obj.getInt("chapter");title=obj.getString("title");
            rule=obj.getString("rule");fr=obj.getString("fr");ja=obj.getString("ja");focus=obj.getString("focus");
            question=obj.getString("question");answer=obj.getString("answer");why=obj.getString("why");
            JSONArray a=obj.getJSONArray("compare"),b=obj.getJSONArray("options");
            for(int i=0;i<a.length();i++)contrast.add(a.getString(i));
            for(int i=0;i<b.length();i++)options.add(b.getString(i));
        }
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
            JSONObject course=new JSONObject(assetText("course.json"));
            JSONArray chapters=course.getJSONArray("chapters"),units=course.getJSONArray("units");
            for(int i=0;i<chapters.length();i++)courseChapters.add(new CourseChapter(chapters.getJSONObject(i)));
            for(int i=0;i<units.length();i++)courseUnits.add(new CourseUnit(units.getJSONObject(i)));
            if(courseChapters.size()!=24 || courseUnits.size()<50)throw new IllegalStateException("PDF-based course incomplete");
            JSONArray soundMarks=new JSONArray(assetText("course_ipa.json"));
            if(soundMarks.length()!=courseUnits.size())throw new IllegalStateException("Course phonetics incomplete");
            for(int i=0;i<soundMarks.length();i++){
                JSONObject item=soundMarks.getJSONObject(i);CourseUnit u=courseUnits.get(i);
                if(item.getInt("id")!=u.id)throw new IllegalStateException("Course IPA ID mismatch");
                u.ipa=item.getString("ipa");
            }
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
    /** Three persistent destinations; navigation has one clear owner per learning goal. */
    private void showRootTabs(LinearLayout header){
        gap(header,14);
        LinearLayout tabs=new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        String[] names={"文法を学ぶ","文法テスト","復習"};
        int[] screens={0,8,9};
        for(int i=0;i<names.length;i++){
            final int target=screens[i];boolean selected=screen==target;
            TextView tab=t(names[i],13,selected,selected?WHITE:BLACK);
            tab.setGravity(Gravity.CENTER);
            tab.setMinHeight(dp(43));
            tab.setContentDescription(names[i]+(selected?"、選択中":""));
            tab.setBackground(rounded(selected?BLACK:BG,selected?0:LINE,11));
            tab.setOnClickListener(v->{if(target==0)home();else if(target==8)showQuizSetup();else showReviewHub();});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(43),1);
            if(i>0)lp.leftMargin=dp(7);
            tabs.addView(tab,lp);
        }
        add(header,tabs);
    }
    private void startScreen(String title,String caption,View.OnClickListener back){
        stopAudio();base=vertical();base.setBackgroundColor(BG);setContentView(base);
        LinearLayout header=vertical();pad(header,20,17);header.setBackgroundColor(WHITE);
        if(back!=null){TextView b=t("← 戻る",14,false,GRAY);b.setOnClickListener(back);add(header,b);gap(header,13);}
        add(header,t(title,24,true,BLACK));if(caption!=null){gap(header,5);add(header,t(caption,12,false,GRAY));}
        if(screen==0||screen==8||screen==9)showRootTabs(header);
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
    // Book-first learning path: 24 chapters, concept comparisons, application, and retrieval.
    private void home(){
        screen=0;startScreen("フランス語 文法","しくみを理解 → 文で確かめる → 忘れる前に復習",null);
        int done=0;for(CourseUnit u:courseUnits)if(prefs.getBoolean("cu_done_"+u.id,false))done++;
        LinearLayout summary=panel();eyebrow(summary,"BOOK COURSE / 体系学習");gap(summary,10);
        add(summary,t(done+" / "+courseUnits.size()+" 文法のしくみを確認",22,true,BLACK));gap(summary,11);
        progress(summary,done,courseUnits.size());gap(summary,9);
        add(summary,t("初級教科書の序章＋23章  •  重要事項を段階的に学習",13,false,GRAY));gap(summary,7);
        add(summary,t("今日 "+prefs.getInt("today_count_"+today(),0)+"問  •  XP "+prefs.getInt("xp",0)+"  •  継続 "+prefs.getInt("study_streak",0)+"日",12,false,GRAY));
        add(mainArea,summary);gap(mainArea,13);
        int next=1;for(CourseUnit u:courseUnits){if(!prefs.getBoolean("cu_done_"+u.id,false)){next=u.id;break;}}
        final int open=next;
        add(mainArea,btn("▶  続きの文法から学ぶ  →",true,v->showCourseUnit(open)));gap(mainArea,9);
        add(mainArea,btn("学習記録を見る",false,v->showHistory()));gap(mainArea,9);
        add(mainArea,btn("500例文の演習・索引を開く  →",false,v->legacyCatalog()));gap(mainArea,18);
        add(mainArea,t("教科書に沿って学ぶ",20,true,BLACK));gap(mainArea,5);
        add(mainArea,t("章を選択。短い規則 → 対比 → 例文 → 理解チェックの順に進みます。",13,false,GRAY));gap(mainArea,13);
        for(CourseChapter c:courseChapters){
            int total=0,completed=0;
            for(CourseUnit u:courseUnits)if(u.chapter==c.id){total++;if(prefs.getBoolean("cu_done_"+u.id,false))completed++;}
            LinearLayout entry=panel();eyebrow(entry,c.id==0?"序章":String.format(Locale.JAPAN,"第%02d章",c.id));gap(entry,5);
            add(entry,t(c.title+"   →",18,true,BLACK));gap(entry,5);
            add(entry,t(total+"項目  •  理解チェック "+completed+" / "+total,12,false,GRAY));gap(entry,7);
            progress(entry,completed,total);
            entry.setOnClickListener(v->showCourseChapter(c.id));add(mainArea,entry);gap(mainArea,9);
        }
        gap(mainArea,8);add(mainArea,btn("文法別500例文の演習・索引  →",false,v->legacyCatalog()));gap(mainArea,20);
        add(mainArea,t("参考：小栗栖等『フランス語初級文法』2022年9月版。構成を参考にした独自の例文・解説です。",12,false,GRAY));
    }
    private void legacyCatalog(){
        screen=16;startScreen("例文500・文法索引","文法別の例文集を使って知識を広げる",v->home());
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
    private CourseUnit cu(int id){if(id<1||id>courseUnits.size())throw new IllegalArgumentException("Invalid course unit");return courseUnits.get(id-1);}
    private ArrayList<Integer> unitIds(int chapter){ArrayList<Integer> ids=new ArrayList<>();for(CourseUnit u:courseUnits)if(u.chapter==chapter)ids.add(u.id);return ids;}
    private void showCourseChapter(int chapter){
        currentCourseChapter=chapter;screen=10;CourseChapter c=courseChapters.get(chapter);
        startScreen(c.title,"第"+chapter+"章  •  原著 p."+c.sourcePage+"  •  "+unitIds(chapter).size()+"項目",v->home());
        LinearLayout intro=panel();eyebrow(intro,"この章の目標");gap(intro,10);add(intro,t(c.goal,16,false,BLACK));
        add(mainArea,intro);gap(mainArea,14);
        for(int id:unitIds(chapter)){
            CourseUnit u=cu(id);boolean done=prefs.getBoolean("cu_done_"+id,false);
            LinearLayout entry=panel();eyebrow(entry,"STEP "+String.format(Locale.JAPAN,"%02d",unitIds(chapter).indexOf(id)+1)+(done?"  ✓ 確認済み":"  • 未確認"));gap(entry,7);
            add(entry,t(u.title+"   →",18,true,BLACK));gap(entry,5);
            add(entry,t(u.rule,13,false,GRAY));
            entry.setOnClickListener(v->showCourseUnit(id));add(mainArea,entry);gap(mainArea,9);
        }
        if(!c.legacy.isEmpty()){
            gap(mainArea,10);add(mainArea,t("さらに例文で練習",19,true,BLACK));gap(mainArea,8);
            add(mainArea,t("従来の500例文から関連する文法事項を掲載。音声・IPAと確認テストが使えます。",13,false,GRAY));gap(mainArea,8);
            for(int id:c.legacy){Lesson l=lesson(id);add(mainArea,btn(l.title+"  →",false,v->showLesson(id)));gap(mainArea,7);}
        }
    }
    private void showCourseUnit(int id){
        currentCourseUnit=id;CourseUnit u=cu(id);currentCourseChapter=u.chapter;screen=11;
        prefs.edit().putInt("course_resume_unit",id).apply();
        CourseChapter ch=courseChapters.get(u.chapter);ArrayList<Integer> within=unitIds(u.chapter);
        startScreen(u.title,""+(u.chapter==0?"序章":"第"+u.chapter+"章")+"  •  "+(within.indexOf(id)+1)+" / "+within.size(),v->showCourseChapter(u.chapter));
        LinearLayout goal=panel();eyebrow(goal,"01 / 理解する");gap(goal,10);
        add(goal,t(u.rule,16,false,BLACK));add(mainArea,goal);gap(mainArea,11);
        LinearLayout diff=panel();eyebrow(diff,"02 / 形を見比べる");gap(diff,10);
        for(int i=0;i<u.contrast.size();i++){
            TextView v=t((i==0?"A   ":"B   ")+u.contrast.get(i),16,i==1,BLACK);add(diff,v);
            if(i==0)gap(diff,9);
        }
        add(mainArea,diff);gap(mainArea,11);
        LinearLayout sample=panel();eyebrow(sample,"03 / 文に当てはめる");gap(sample,12);
        TextView courseFrench=t(u.fr,21,true,BLACK);add(sample,courseFrench);gap(sample,5);
        add(sample,t(u.ipa+"  •  IPA自動推定",12,false,GRAY));gap(sample,7);
        add(sample,t(u.ja,15,false,GRAY));gap(sample,11);
        add(sample,t("POINT  "+u.focus,14,false,BLACK));gap(sample,10);
        add(sample,btn("▶  この例文を聴く",false,v->playCourse(u.id)));
        add(mainArea,sample);gap(mainArea,12);
        TextView notice=t("女性合成音声を端末内で再生。IPAは自動推定のため、辞書や音声との全件一致監修は未完了です。",12,false,GRAY);
        add(mainArea,notice);gap(mainArea,11);
        LinearLayout footer=bottomBar();
        add(footer,btn("04  この文法を使えるか確認  →",true,v->{coursePhase=0;conceptAnswered=false;conceptPicked.clear();showCourseCheck(id);}));
        courseFrench.post(()->{if(screen==11&&currentCourseUnit==id&&!isFinishing())playCourse(id);});
    }
    private ArrayList<String> courseTokens(CourseUnit u){ArrayList<String> words=new ArrayList<>();for(String part:u.fr.trim().split("\\s+"))words.add(part);return words;}
    private String conceptComposed(CourseUnit u){ArrayList<String> words=courseTokens(u);StringBuilder sb=new StringBuilder();for(int i:conceptPicked){if(sb.length()>0)sb.append(' ');sb.append(words.get(i));}return sb.toString();}
    private void restoreConceptScroll(int position){ScrollView view=activeScroll;if(view!=null)view.post(()->{if(screen==12&&activeScroll==view)view.scrollTo(0,position);});}
    private void markConcept(CourseUnit u,boolean correct){
        int times=prefs.getInt("cu_attempt_"+u.id,0)+1,oldStage=prefs.getInt("cu_stage_"+u.id,0);
        int stage=correct?Math.min(5,oldStage+1):0;
        long[] days={0,1,3,7,14,30};
        String day=today(),previous=prefs.getString("last_study_day","");
        Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_MONTH,-1);
        String priorDay=new SimpleDateFormat("yyyyMMdd",Locale.US).format(c.getTime());
        int streak=prefs.getInt("study_streak",0);
        if(!day.equals(previous))streak=previous.equals(priorDay)?streak+1:1;
        prefs.edit().putInt("cu_attempt_"+u.id,times).putInt("cu_stage_"+u.id,stage)
            .putInt("cu_wrong_"+u.id,correct?0:prefs.getInt("cu_wrong_"+u.id,0)+1)
            .putLong("cu_due_"+u.id,System.currentTimeMillis()+(correct?days[stage]:0)*86400000L)
            .putBoolean("cu_done_"+u.id,correct||prefs.getBoolean("cu_done_"+u.id,false))
            .putInt("xp",prefs.getInt("xp",0)+(correct?12:2))
            .putInt("total_attempts",prefs.getInt("total_attempts",0)+1)
            .putInt("total_correct",prefs.getInt("total_correct",0)+(correct?1:0))
            .putInt("today_count_"+day,prefs.getInt("today_count_"+day,0)+1)
            .putString("last_study_day",day).putInt("study_streak",streak).commit();
    }
    private void showCourseCheck(int id){
        currentCourseUnit=id;CourseUnit u=cu(id);screen=12;
        startScreen("理解チェック",u.title+"  •  "+(coursePhase==0?"1 / 2  判断":"2 / 2  語順"),v->showCourseUnit(id));
        LinearLayout p=panel();eyebrow(p,coursePhase==0?"規則を使って選ぼう":"日本語から文章を組み立てよう");gap(p,12);
        add(p,t(coursePhase==0?u.question:u.ja,20,true,BLACK));
        add(mainArea,p);gap(mainArea,10);
        if(coursePhase==0){
            for(String option:u.options){
                Button b=btn(option,false,v->{if(conceptAnswered)return;conceptAnswered=true;conceptCorrect=option.equals(u.answer);showCourseCheck(id);});
                b.setEnabled(!conceptAnswered);add(mainArea,b);gap(mainArea,8);
            }
        }else{
            ArrayList<String> tokens=courseTokens(u);
            LinearLayout chosen=panel();eyebrow(chosen,"組み立て中");gap(chosen,7);
            add(chosen,t(conceptComposed(u).isEmpty()?"下の語句を順に押してください":conceptComposed(u),18,true,BLACK));
            add(mainArea,chosen);gap(mainArea,9);
            ArrayList<Integer> scramble=new ArrayList<>();for(int i=0;i<tokens.size();i++)scramble.add(i);
            Collections.shuffle(scramble,new Random(891L+id));
            GridLayout chips=new GridLayout(this);chips.setColumnCount(2);
            for(int index:scramble){
                Button b=btn(tokens.get(index),false,v->{if(conceptAnswered || conceptPicked.contains(index))return;conceptPicked.add(index);int scroll=activeScroll!=null?activeScroll.getScrollY():0;showCourseCheck(id);restoreConceptScroll(scroll);});
                boolean used=conceptPicked.contains(index);b.setEnabled(!used&&!conceptAnswered);b.setAlpha(used?0.35f:1f);
                GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);lp.width=0;lp.height=dp(50);lp.setMargins(dp(3),dp(3),dp(3),dp(3));chips.addView(b,lp);
            }
            add(mainArea,chips);gap(mainArea,7);
            if(!conceptPicked.isEmpty()&&!conceptAnswered)add(mainArea,btn("一つ戻す",false,v->{conceptPicked.remove(conceptPicked.size()-1);int scroll=activeScroll!=null?activeScroll.getScrollY():0;showCourseCheck(id);restoreConceptScroll(scroll);}));
        }
        if(conceptAnswered){
            LinearLayout result=panel();eyebrow(result,conceptCorrect?"✓ 正解":"✕ 惜しい");gap(result,8);
            add(result,t(coursePhase==0?"正解："+u.answer:u.fr,17,true,conceptCorrect?Color.rgb(23,115,50):Color.rgb(174,38,38)));
            gap(result,6);add(result,t(coursePhase==0?u.why:u.focus,14,false,BLACK));
            add(mainArea,result);gap(mainArea,9);
        }
        LinearLayout bottom=bottomBar();
        if(!conceptAnswered&&coursePhase==1){
            Button submit=btn("答え合わせ  "+conceptPicked.size()+" / "+courseTokens(u).size(),true,v->{if(conceptPicked.size()!=courseTokens(u).size())return;conceptAnswered=true;conceptCorrect=conceptComposed(u).equals(u.fr);showCourseCheck(id);});
            submit.setEnabled(conceptPicked.size()==courseTokens(u).size());add(bottom,submit);
        }else if(conceptAnswered){
            if(conceptCorrect){
                add(bottom,btn(coursePhase==0?"次：語順も確認  →":"次の文法へ  →",true,v->{
                    if(coursePhase==0){coursePhase=1;conceptAnswered=false;conceptPicked.clear();showCourseCheck(id);}
                    else{markConcept(u,true);int next=id+1;coursePhase=0;conceptAnswered=false;conceptPicked.clear();if(next<=courseUnits.size())showCourseUnit(next);else home();}
                }));
            }else{
                add(bottom,btn("もう一度考える  →",true,v->{markConcept(u,false);conceptAnswered=false;conceptPicked.clear();showCourseCheck(id);}));
            }
        }else if(coursePhase==0){add(bottom,t("選択肢を押すとすぐに正誤を表示",13,false,GRAY));}
    }
    private void showCourseTestSetup(){
        screen=17;startScreen("教科書コースの理解度テスト","序章〜第23章から理解チェックを出題",v->showQuizSetup());
        LinearLayout setup=panel();eyebrow(setup,"範囲を選ぶ");gap(setup,8);
        ArrayList<String> names=new ArrayList<>();for(CourseChapter ch:courseChapters)names.add((ch.id==0?"序章":"第"+ch.id+"章")+"  "+ch.title);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names);
        Spinner from=new Spinner(this);from.setAdapter(adapter);from.setSelection(prefs.getInt("course_range_start",0));add(setup,from);
        gap(setup,9);Spinner to=new Spinner(this);to.setAdapter(adapter);to.setSelection(prefs.getInt("course_range_end",23));add(setup,to);gap(setup,10);
        add(setup,btn("全範囲  序章〜第23章",false,v->{from.setSelection(0);to.setSelection(23);}));gap(setup,16);
        eyebrow(setup,"問題数");gap(setup,8);
        TextView label=t("10問",18,true,BLACK);add(setup,label);gap(setup,4);
        SeekBar slider=new SeekBar(this);slider.setMax(9);slider.setProgress(0);add(setup,slider);
        CheckBox all=new CheckBox(this);all.setText("選んだ範囲の全問");add(setup,all);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int value,boolean user){label.setText(all.isChecked()?"範囲内の全問":(10+value*10)+"問");}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        all.setOnCheckedChangeListener((b,selected)->label.setText(selected?"範囲内の全問":(10+slider.getProgress()*10)+"問"));
        add(mainArea,setup);gap(mainArea,10);
        add(mainArea,t("このテストは各文法事項の判断問題を出題。500例文の語順問題とは別に、規則の理解を確認できます。",13,false,GRAY));
        LinearLayout bottom=bottomBar();add(bottom,btn("この設定で開始  →",true,v->{
            int a=from.getSelectedItemPosition(),b=to.getSelectedItemPosition();if(a>b){Toast.makeText(this,"範囲が逆になっています",Toast.LENGTH_SHORT).show();return;}
            ArrayList<Integer> ids=new ArrayList<>();for(CourseUnit x:courseUnits)if(x.chapter>=a&&x.chapter<=b)ids.add(x.id);
            Collections.shuffle(ids,random);int n=all.isChecked()?ids.size():Math.min(ids.size(),10+slider.getProgress()*10);
            prefs.edit().putInt("course_range_start",a).putInt("course_range_end",b).apply();
            startCourseDrill(new ArrayList<>(ids.subList(0,n)),false);
        }));
    }
    private void startCourseDrill(ArrayList<Integer> ids,boolean review){
        if(ids.isEmpty()){Toast.makeText(this,"対象がありません",Toast.LENGTH_SHORT).show();return;}
        conceptQueue.clear();conceptQueue.addAll(ids);conceptIndex=0;conceptTotalCorrect=0;courseQuizFromReview=review;conceptAnswered=false;showCourseDrill();
    }
    private void showCourseDrill(){
        if(conceptIndex>=conceptQueue.size()){
            screen=19;startScreen("コース確認結果","理解できた文法を記録しました",v->courseQuizExit());
            LinearLayout p=panel();eyebrow(p,"RESULT");gap(p,8);add(p,t(conceptTotalCorrect+" / "+conceptQueue.size()+" 正解",27,true,BLACK));gap(p,12);
            progress(p,conceptTotalCorrect,conceptQueue.size());add(mainArea,p);
            gap(mainArea,12);add(mainArea,btn("復習・学習に戻る",true,v->courseQuizExit()));return;
        }
        screen=18;CourseUnit u=cu(conceptQueue.get(conceptIndex));
        startScreen("文法の理解度テスト",(conceptIndex+1)+" / "+conceptQueue.size()+"  •  "+u.title,v->courseQuizExit());
        progress(mainArea,conceptIndex,conceptQueue.size());gap(mainArea,12);
        LinearLayout p=panel();eyebrow(p,"規則を選ぶ");gap(p,11);add(p,t(u.question,19,true,BLACK));add(mainArea,p);gap(mainArea,12);
        for(String opt:u.options){
            Button b=btn(opt,false,v->{if(conceptAnswered)return;conceptAnswered=true;conceptCorrect=opt.equals(u.answer);if(conceptCorrect)conceptTotalCorrect++;markConcept(u,conceptCorrect);showCourseDrill();});
            b.setEnabled(!conceptAnswered);add(mainArea,b);gap(mainArea,7);
        }
        if(conceptAnswered){
            LinearLayout info=panel();eyebrow(info,conceptCorrect?"✓ 正解":"✕ 不正解");gap(info,7);
            add(info,t("正解  "+u.answer,17,true,conceptCorrect?Color.rgb(23,115,50):Color.rgb(174,38,38)));gap(info,6);add(info,t(u.why,14,false,BLACK));
            add(mainArea,info);
        }
        LinearLayout footer=bottomBar();
        if(conceptAnswered)add(footer,btn("次の問題  →",true,v->{conceptIndex++;conceptAnswered=false;showCourseDrill();}));
        else add(footer,t("選択するとすぐに正誤と理由を表示",13,false,GRAY));
    }
    private void courseQuizExit(){if(courseQuizFromReview)showReviewHub();else showCourseTestSetup();}
    private void startCourseReview(boolean due){
        ArrayList<Integer> ids=new ArrayList<>();long now=System.currentTimeMillis();
        for(CourseUnit u:courseUnits){if(due){if(prefs.getInt("cu_attempt_"+u.id,0)>0&&prefs.getLong("cu_due_"+u.id,Long.MAX_VALUE)<=now)ids.add(u.id);}
            else if(prefs.getInt("cu_wrong_"+u.id,0)>0)ids.add(u.id);}
        Collections.shuffle(ids,random);if(ids.size()>10)ids=new ArrayList<>(ids.subList(0,10));startCourseDrill(ids,true);
    }
    private void chapter(String name){
        screen=1;startScreen(name,"学習する文法事項を選択",v->legacyCatalog());
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
        // Keep previous/next accessible while the learner reads the grammatical explanation.
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        Button previous=btn("← 前",false,v->showExample(ids.get(at-1)));previous.setEnabled(at>0);previous.setAlpha(at>0?1f:0.35f);
        nav.addView(previous,new LinearLayout.LayoutParams(0,dp(56),1));View sep=new View(this);nav.addView(sep,new LinearLayout.LayoutParams(dp(9),1));
        Button next=btn("次の例文 →",true,v->showExample(ids.get(at+1)));next.setEnabled(at+1<ids.size());next.setAlpha(at+1<ids.size()?1f:0.35f);
        nav.addView(next,new LinearLayout.LayoutParams(0,dp(56),2));add(bottomBar(),nav);
        // Opening a fresh example plays exactly once; rendering the quiz or toggling controls does not replay it.
        fr.post(()->{if(screen==3 && selectedExample==id && !isFinishing())play(id);});
    }
    /** Reference-book style quiz settings, matching the familiar vocabulary-app range controls. */
    private void showQuizSetup(){
        screen=8;
        startScreen("文法テスト","出題範囲と問題数を決めてテスト開始",v->home());
        if(prefs.contains("pending_quiz_v3")){
            add(mainArea,btn("▶  前回のテストを再開",true,v->resumeSavedQuiz()));gap(mainArea,12);
        }
        LinearLayout textbook=panel();eyebrow(textbook,"体系的な理解チェック");gap(textbook,9);
        add(textbook,t("序章〜第23章で学んだ文法を試す",17,true,BLACK));gap(textbook,8);
        add(textbook,t("範囲・問題数を指定して、文法の仕組みそのものを確認。",13,false,GRAY));gap(textbook,11);
        add(textbook,btn("教科書コースから出題  →",true,v->showCourseTestSetup()));
        add(mainArea,textbook);gap(mainArea,16);
        add(mainArea,t("500例文から出題",19,true,BLACK));gap(mainArea,8);
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
        add(settings,btn("全50単元を選択  （1〜50）",false,v->{
            start.setSelection(0);end.setSelection(49);
            Toast.makeText(this,"全50単元を指定しました",Toast.LENGTH_SHORT).show();
        }));
        gap(settings,16);eyebrow(settings,"COUNT / 問題数");gap(settings,7);
        TextView count=t("10問",18,true,BLACK);add(settings,count);gap(settings,5);
        SeekBar seek=new SeekBar(this);seek.setMax(9);
        int previousCount=prefs.getInt("quiz_range_count",10);
        seek.setProgress(Math.max(0,Math.min(9,Math.round((previousCount-10)/10f))));add(settings,seek);
        TextView hint=t("10〜100問から10問刻みで選択（範囲内が少ない場合は全問）",12,false,GRAY);add(settings,hint);gap(settings,12);
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
    /** Due review and wrong-answer remediation live together, not on the learning index. */
    private void showReviewHub(){
        screen=9;startScreen("復習","忘れる前の復習と間違い直しをまとめて管理",v->home());
        int dueCount=0,wrongCount=0;
        long now=System.currentTimeMillis();
        for(Example e:examples){
            if(prefs.getInt("wrong_"+e.id,0)>0)wrongCount++;
            if(prefs.getInt("attempts_"+e.id,0)>0 && prefs.getLong("due_"+e.id,Long.MAX_VALUE)<=now)dueCount++;
        }
        int conceptDue=0,conceptWrong=0;
        for(CourseUnit u:courseUnits){
            if(prefs.getInt("cu_wrong_"+u.id,0)>0)conceptWrong++;
            if(prefs.getInt("cu_attempt_"+u.id,0)>0 && prefs.getLong("cu_due_"+u.id,Long.MAX_VALUE)<=System.currentTimeMillis())conceptDue++;
        }
        LinearLayout concept=panel();eyebrow(concept,"教科書コースの復習");gap(concept,9);
        add(concept,t("復習予定 "+conceptDue+"項目   •   苦手 "+conceptWrong+"項目",17,true,BLACK));gap(concept,8);
        Button cd=btn("忘れる前に復習  →",true,v->startCourseReview(true));cd.setEnabled(conceptDue>0);cd.setAlpha(conceptDue>0?1f:.45f);add(concept,cd);gap(concept,7);
        Button cw=btn("苦手な規則を解き直す  →",false,v->startCourseReview(false));cw.setEnabled(conceptWrong>0);cw.setAlpha(conceptWrong>0?1f:.45f);add(concept,cw);
        add(mainArea,concept);gap(mainArea,15);
        add(mainArea,t("500例文の復習",19,true,BLACK));gap(mainArea,9);
        LinearLayout summary=panel();eyebrow(summary,"REVIEW / 復習待ち");gap(summary,10);
        add(summary,t("今日の復習  "+dueCount+"問     苦手  "+wrongCount+"問",18,true,BLACK));gap(summary,7);
        add(summary,t("どちらも10問ずつ練習できます。重複する問題は両方に表示される場合があります。",12,false,GRAY));
        add(mainArea,summary);gap(mainArea,13);
        LinearLayout due=panel();eyebrow(due,"01 / 間隔を空けた復習");gap(due,8);
        add(due,t("今日の復習",19,true,BLACK));gap(due,8);
        add(due,t(dueCount>0?"復習予定日を迎えた問題から最大10問を出題。":"現在、復習予定の問題はありません。",13,false,GRAY));gap(due,12);
        Button dueButton=btn(dueCount>0?"今日の復習を始める  →":"復習予定なし",true,v->startDueQuiz());
        dueButton.setEnabled(dueCount>0);dueButton.setAlpha(dueCount>0?1f:0.45f);add(due,dueButton);
        add(mainArea,due);gap(mainArea,13);
        LinearLayout wrong=panel();eyebrow(wrong,"02 / 間違い直し");gap(wrong,8);
        add(wrong,t("間違えた問題",19,true,BLACK));gap(wrong,8);
        add(wrong,t(wrongCount>0?"未克服の間違いから最大10問を出題。正解すると苦手から外れます。":"現在、間違いの復習対象はありません。",13,false,GRAY));gap(wrong,12);
        Button wrongButton=btn(wrongCount>0?"苦手問題を解き直す  →":"苦手問題なし",false,v->startWrongQuiz());
        wrongButton.setEnabled(wrongCount>0);wrongButton.setAlpha(wrongCount>0?1f:0.45f);add(wrong,wrongButton);
        add(mainArea,wrong);gap(mainArea,15);
        add(mainArea,btn("学習記録を見る  →",false,v->showHistory()));
    }
    private void returnToQuizOrigin(){
        if(quizFromHome&&quizReturnTab==9)showReviewHub();
        else if(quizFromHome&&quizReturnTab==8)showQuizSetup();
        else if(quizFromHome)home();
        else showLesson(selectedLesson);
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
        gap(mainArea,14);add(mainArea,t("教科書コースの理解状況",19,true,BLACK));gap(mainArea,7);
        int courseDone=0;for(CourseUnit unit:courseUnits)if(prefs.getBoolean("cu_done_"+unit.id,false))courseDone++;
        add(mainArea,t(courseDone+" / "+courseUnits.size()+"項目で理解チェック済み",14,true,BLACK));gap(mainArea,8);
        for(CourseChapter c:courseChapters){int n=0,passed=0;for(CourseUnit u:courseUnits)if(u.chapter==c.id){n++;if(prefs.getBoolean("cu_done_"+u.id,false))passed++;}
            if(passed==0)continue;LinearLayout item=panel();add(item,t(c.title+"   "+passed+" / "+n,14,true,BLACK));item.setOnClickListener(v->showCourseChapter(c.id));add(mainArea,item);gap(mainArea,8);}
        gap(mainArea,16);add(mainArea,t("500例文の習得状況",19,true,BLACK));gap(mainArea,8);
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
        startScreen("文法チェック",l.title+"    "+(quizIndex+1)+" / "+quiz.size(),v->returnToQuizOrigin());
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
        screen=6;logFinishedResult();prefs.edit().remove("pending_quiz_v3").commit();startScreen("確認テストの結果","解説を読んで、必要な部分を復習しましょう",v->returnToQuizOrigin());
        LinearLayout summary=panel();eyebrow(summary,"SCORE");gap(summary,10);
        add(summary,t(correctCount+" / "+quiz.size()+" 正解",29,true,BLACK));gap(summary,12);
        add(summary,t("正答率 "+Math.round(100f*correctCount/Math.max(1,quiz.size()))+"%   •   総XP "+prefs.getInt("xp",0),16,false,GRAY));gap(summary,12);progress(summary,correctCount,quiz.size());
        add(mainArea,summary);gap(mainArea,16);
        if(!failed.isEmpty()){
            add(mainArea,btn("間違えた"+failed.size()+"問をもう一度  →",true,v->startQuiz(new ArrayList<>(failed))));gap(mainArea,16);
            add(mainArea,t("間違えた問題の解説",18,true,BLACK));gap(mainArea,9);
            // Full-range quizzes can contain 500 problems; avoid inflating hundreds of cards on one result screen.
            int shown=Math.min(30,failed.size());
            for(int i=0;i<shown;i++){int id=failed.get(i);Example e=ex(id);LinearLayout item=panel();add(item,t(e.fr,16,true,BLACK));gap(item,6);add(item,t(e.ja,14,false,GRAY));gap(item,8);add(item,t("解説を読む →",13,true,BLACK));item.setOnClickListener(v->showExample(e.id));add(mainArea,item);gap(mainArea,9);}
            if(failed.size()>shown){add(mainArea,t("残り"+(failed.size()-shown)+"問は上の再テストですべて復習できます。",14,false,GRAY));gap(mainArea,12);}
        }else{add(mainArea,t("全問正解。次の文法事項へ進みましょう。",16,true,BLACK));gap(mainArea,13);}
        add(mainArea,btn(quizFromHome?(quizReturnTab==9?"復習に戻る":quizReturnTab==8?"文法テストに戻る":"学習に戻る"):"単元に戻る",false,v->returnToQuizOrigin()));
    }
    private void playCourse(int id){
        stopAudio();try{
            String name=String.format(Locale.US,"audio/course_%04d.ogg",id);
            AssetFileDescriptor fd=getAssets().openFd(name);
            player=new MediaPlayer();player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());fd.close();
            player.prepare();player.start();player.setOnCompletionListener(v->stopAudio());
        }catch(Exception err){stopAudio();Toast.makeText(this,"音声を再生できません",Toast.LENGTH_SHORT).show();}
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
        super.onSaveInstanceState(b);b.putInt("screen",screen);b.putInt("lesson",selectedLesson);b.putInt("selectedExample",selectedExample);b.putIntegerArrayList("deck",new ArrayList<>(deck));b.putInt("cardIndex",cardIndex);b.putBoolean("revealed",revealed);b.putIntegerArrayList("quiz",new ArrayList<>(quiz));b.putInt("quizIndex",quizIndex);b.putInt("correctCount",correctCount);b.putIntegerArrayList("failed",new ArrayList<>(failed));b.putStringArrayList("quizAnswers",new ArrayList<>(quizAnswers));b.putBoolean("answered",answered);b.putString("selectedAnswer",selectedAnswer);b.putBoolean("quizFromHome",quizFromHome);b.putInt("quizReturnTab",quizReturnTab);b.putIntegerArrayList("orderPicked",new ArrayList<>(orderPicked));b.putInt("currentCourseChapter",currentCourseChapter);b.putInt("currentCourseUnit",currentCourseUnit);b.putInt("coursePhase",coursePhase);b.putBoolean("conceptAnswered",conceptAnswered);b.putBoolean("conceptCorrect",conceptCorrect);b.putIntegerArrayList("conceptPicked",new ArrayList<>(conceptPicked));b.putIntegerArrayList("conceptQueue",new ArrayList<>(conceptQueue));b.putInt("conceptIndex",conceptIndex);b.putInt("conceptTotalCorrect",conceptTotalCorrect);b.putBoolean("courseQuizFromReview",courseQuizFromReview);
    }
    private boolean restore(Bundle b){
        int s=b.getInt("screen",0);selectedLesson=b.getInt("lesson",1);if(selectedLesson<1||selectedLesson>50)return false;
        currentCourseChapter=b.getInt("currentCourseChapter",0);currentCourseUnit=b.getInt("currentCourseUnit",1);
        coursePhase=b.getInt("coursePhase",0);conceptAnswered=b.getBoolean("conceptAnswered",false);conceptCorrect=b.getBoolean("conceptCorrect",false);
        ArrayList<Integer> picks=b.getIntegerArrayList("conceptPicked");conceptPicked.clear();if(picks!=null)conceptPicked.addAll(picks);
        ArrayList<Integer> queued=b.getIntegerArrayList("conceptQueue");conceptQueue.clear();if(queued!=null)conceptQueue.addAll(queued);
        conceptIndex=b.getInt("conceptIndex",0);conceptTotalCorrect=b.getInt("conceptTotalCorrect",0);courseQuizFromReview=b.getBoolean("courseQuizFromReview",false);
        if(s==10&&currentCourseChapter>=0&&currentCourseChapter<24){showCourseChapter(currentCourseChapter);return true;}
        if(s==11&&currentCourseUnit>=1&&currentCourseUnit<=courseUnits.size()){showCourseUnit(currentCourseUnit);return true;}
        if(s==12&&currentCourseUnit>=1&&currentCourseUnit<=courseUnits.size()){showCourseCheck(currentCourseUnit);return true;}
        if(s==16){legacyCatalog();return true;}
        if(s==17){showCourseTestSetup();return true;}
        if((s==18||s==19)&&!conceptQueue.isEmpty()&&conceptIndex<=conceptQueue.size()){showCourseDrill();return true;}
        if(s==3){int eid=b.getInt("selectedExample",1);if(eid<1||eid>500)return false;showExample(eid);return true;}
        if(s==5){ArrayList<Integer> ids=b.getIntegerArrayList("quiz");ArrayList<Integer> misses=b.getIntegerArrayList("failed");ArrayList<String> a=b.getStringArrayList("quizAnswers");if(ids==null||ids.isEmpty()||ids.size()>500||a==null||misses==null)return false;for(int id:ids)if(id<1||id>500)return false;quiz.clear();quiz.addAll(ids);failed.clear();failed.addAll(misses);quizAnswers.clear();quizAnswers.addAll(a);quizIndex=b.getInt("quizIndex",0);correctCount=b.getInt("correctCount",0);answered=b.getBoolean("answered",false);selectedAnswer=b.getString("selectedAnswer","");quizFromHome=b.getBoolean("quizFromHome",false);quizReturnTab=b.getInt("quizReturnTab",0);orderPicked.clear();ArrayList<Integer> o=b.getIntegerArrayList("orderPicked");if(o!=null)orderPicked.addAll(o);if(quizIndex<0||quizIndex>=quiz.size()||quizAnswers.size()!=quizIndex+(answered?1:0))return false;showQuiz();return true;}
        if(s==2){showLesson(selectedLesson);return true;}if(s==7){showHistory();return true;}if(s==8){showQuizSetup();return true;}if(s==9){showReviewHub();return true;}return false;
    }
    @Override public void onBackPressed(){if(screen==0){super.onBackPressed();return;}if(screen==5||screen==6)returnToQuizOrigin();else if(screen==10)home();else if(screen==11)showCourseChapter(currentCourseChapter);else if(screen==12)showCourseUnit(currentCourseUnit);else if(screen==17)showQuizSetup();else if(screen==18||screen==19)courseQuizExit();else if(screen==16)home();else if(screen==8||screen==9)home();else if(screen==3)showLesson(selectedLesson);else home();}
}
