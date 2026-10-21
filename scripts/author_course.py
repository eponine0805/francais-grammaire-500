# Original beginner course structure inspired by Ogurisu's 2022 French grammar text.
# Explanations and practice items authored for this app; book audio and exercises are NOT copied.
import json
from pathlib import Path
OUT=Path(__file__).resolve().parents[1]/"app/src/main/assets"
chapters=[
('序章｜綴りと発音','フランス語の文字と音の関係を理解し、短い語を声に出す',5,[]),
('名詞と冠詞','名詞の性と数を区別し、適切な冠詞を選ぶ',15,[5,6,7,8,9]),
('形容詞','名詞の性・数に合わせて形容詞を変化させる',27,[10,11]),
('être・前置詞と冠詞','主語に合わせてêtreを変化させ、à・deと冠詞を使う',41,[1,2,3,14,30,31]),
('avoir・疑問文と否定文','avoirの活用と疑問・否定の基本形を使う',51,[4,25,27,29]),
('第1群規則動詞','-er動詞の現在形と主語の一致を身につける',67,[15]),
('規則動詞の否定・疑問','一般動詞でも疑問・否定を作れる',81,[]),
('指示形容詞・第2群動詞','ce系の指示と-finir型・rendre型の活用を学ぶ',89,[13,16,17]),
('aller・venir','移動の表現と近接未来・近接過去の形を知る',97,[18,20,38]),
('代名詞の格と位置','直接・間接目的語を見分け、代名詞を動詞の前に置く',109,[46,47]),
('叙法動詞・複合過去','pouvoir等と、助動詞＋過去分詞の基本形を使う',125,[22,23,24,40,41]),
('faire・prendre・mettre','頻出不規則動詞と複合過去の語順を確認する',139,[19,21]),
('代名動詞','se leverなどの仕組みと過去形をつくる',153,[36]),
('所有形容詞・非人称表現','mon/ma/mesと天気・時間などの定型表現を理解する',175,[12]),
('関係詞①','qui・queで文をつなぎ、先行詞の役割を見分ける',189,[49]),
('指示代名詞・関係詞②','celui系と関係副詞oùを使い分ける',203,[]),
('半過去・大過去','継続・習慣の過去と、それ以前の過去を区別する',213,[44,45]),
('比較・最上級・受動態','比べ方と受動態、onの使い方を理解する',225,[34,35]),
('単純未来・否定表現','未来形とpas以外の否定表現を使う',235,[39,26]),
('命令形・疑問詞','指示・依頼の命令形と疑問詞を選べる',249,[37,28]),
('疑問詞・中性代名詞','疑問表現を整理し、y・enを活用する',261,[48]),
('条件法・話法','丁寧な依頼、仮定、発言の伝え方を知る',273,[50]),
('時制の一致・間接疑問文','間接話法・間接疑問文の基本語順を見極める',289,[]),
('接続法と文学的過去','接続法の基本用途を知り、文学的時制は読解事項として認識する',303,[]),
]
cs=[]
def add(ch,title,rule,left,right,fr,ja,focus,q,opts,answer,why):
    assert len(opts)==4 and answer in opts
    cs.append(dict(id=len(cs)+1,chapter=ch,title=title,rule=rule,compare=[left,right],fr=fr,ja=ja,focus=focus,question=q,options=opts,answer=answer,why=why))
# 0
add(0,'綴りとアクセント記号','é・è・ê・ç などの記号は文字の一部。省略しない。アクセント記号の有無によって意味や発音が変わることがある。','cafe（誤）','café（正）','Je prends un café.','私はコーヒーを一杯飲みます。','café は最後の é までが正式な綴り。','「コーヒー」の正しい綴りは？',['cafe','café','caffé','caféé'],'café','é はアクセント記号を含めて一文字として覚える。')
add(0,'読まない語末字','フランス語には発音しない語末の文字が多い。ただし例外があるため、綴りだけで判断せず実際の音を確かめよう。','petit（末尾 t は通常無音）','petite（末尾 t を発音）','Il est petit.','彼は小柄です。','petit の末尾の t は通常読まない。','petit の語末について基本的に正しい説明は？',['常に t を読む','通常 t を読まない','最後の i を読まない','語頭 p を読まない'],'通常 t を読まない','女性形 petite では通常 t を発音する点も比較。')
add(0,'リエゾンと連続する音','語と語がつながると、普段は無音の子音が聞こえる場合がある（リエゾン）。すべての境界で起きるわけではない。','les livres（通常 /le livʁ/）','les amis（/lez‿ami/）','Les amis arrivent.','友達が到着します。','les と amis の間は /z/ でつながる。','les amis の連音として正しいのは？',['常に切って読む','/z/ でつなぐ','/s/ を強く読む','les を省く'],'/z/ でつなぐ','語末 s が母音の前で /z/ と発音される代表例。')
# 1
add(1,'名詞の性','フランス語の名詞には男性・女性という文法上の性がある。見た目では必ずしも判別できないので冠詞と一緒に覚える。','un livre（男性）','une table（女性）','Voici une table.','ここにテーブルがあります。','table は女性名詞なので une を使う。','「1つのテーブル」は？',['un table','une table','des table','le table'],'une table','table は女性名詞、単数なので une。')
add(1,'名詞の数と複数','多くの名詞の複数形は -s を付ける。発音が単数と同じことも多く、冠詞・文脈も手がかりになる。','un livre','des livres','J’ai deux livres.','私は本を2冊持っています。','deux の後の livres は複数形。','「本を2冊」は？',['deux livre','deux livres','deux livrees','deux le livre'],'deux livres','数詞が2以上なら名詞を複数にする。')
add(1,'不定冠詞 un・une・des','不特定の数えられる名詞に使う。男性単数 un、女性単数 une、複数 des。','un ami / une amie','des amis','Elle a une voiture.','彼女は車を1台持っています。','voiture は女性名詞単数なので une。','「車を1台」は？',['un voiture','une voiture','des voiture','du voiture'],'une voiture','名詞の性と数から選ぶ。')
add(1,'定冠詞と部分冠詞','特定・一般的なものを示す le/la/les、量を決めない物質を示す du/de la/de l’ を区別する。','J’aime le café.（コーヒー一般が好き）','Je bois du café.（コーヒーを飲む）','Je bois du lait.','私は牛乳を飲みます。','具体的な量を言わない lait に du を付ける。','「私は牛乳を飲みます」の冠詞は？',['le','une','du','des'],'du','量が不特定の男性不可算名詞に部分冠詞 du。')
#2
add(2,'形容詞の性数一致','形容詞は修飾する名詞の性・数に合わせて形が変わる場合がある。規則的には女性形に -e、複数形に -s を付ける。','un petit livre','une petite maison','La maison est petite.','その家は小さいです。','maison は女性単数なので petite。','女性単数の maison に合う形は？',['petit','petite','petits','petites'],'petite','女性単数には petite。')
add(2,'形容詞の位置','多くの形容詞は名詞の後に置くが、petit・grand・beauなどよく使う語は前に置くことがある。','une voiture rouge','une petite voiture','J’ai une voiture rouge.','私は赤い車を持っています。','rouge は通常名詞の後に置く。','自然な「赤い車」は？',['rouge voiture','voiture rouge','voiture de rouge','la rouge voiture'],'voiture rouge','色を表す形容詞は一般に名詞の後。')
add(2,'êtreの補語としての形容詞','être の後の形容詞も、主語の名詞・代名詞が指す対象に性数一致する。','Il est content.','Elle est contente.','Elle est contente.','彼女はうれしそうです。','女性を指す elle に対して contente。','女性が「満足している」は？',['Elle est content.','Elle est contente.','Elle sont contents.','Elle a content.'],'Elle est contente.','主語 elle に合わせて -e を付ける。')
#3
add(3,'主語代名詞','je / tu / il・elle / nous / vous / ils・elles を主語に置く。tu は親しい相手、vous は丁寧な相手や複数に使う。','Tu vas bien ?','Vous allez bien ?','Vous êtes professeur.','あなたは先生です。','vous は相手への敬意や複数を表せる。','丁寧に相手に「あなたは？」と呼びかけるなら？',['tu','vous','ils','nous'],'vous','初対面などでは vous が基本。')
add(3,'être の現在形','「〜である」のêtreは suis, es, est, sommes, êtes, sont と主語によって変化する。','Je suis japonais.','Nous sommes japonais.','Nous sommes ici.','私たちはここにいます。','nous には sommes を使う。','Nous ___ ici.',['suis','es','sommes','sont'],'sommes','nous に対応するêtre現在形は sommes。')
add(3,'à / de と定冠詞の縮約','à+le は au、à+les は aux。de+le は du、de+les は des。la と l’ は通常縮約しない。','à + le → au','de + les → des','Je vais au cinéma.','私は映画館に行きます。','cinéma は男性名詞なので à + le → au。','à + le は？',['à le','au','aux','du'],'au','男性単数の定冠詞 le と à は縮約する。')
#4
add(4,'avoir の現在形','「持つ」のavoirは ai, as, a, avons, avez, ont。年齢や空腹などの表現にも使う。','J’ai un livre.','Nous avons un livre.','J’ai vingt ans.','私は20歳です。','年齢を表すときは être ではなく avoir を使う。','「私は20歳」は？',['Je suis vingt ans.','J’ai vingt ans.','Je vais vingt ans.','Je peux vingt ans.'],'J’ai vingt ans.','フランス語で年齢は avoir＋数詞＋ans。')
add(4,'疑問文の作り方','初級では平叙文＋上げ調子、est-ce que＋平叙文の語順が使いやすい。倒置形もある。','Tu aimes le café ?','Est-ce que tu aimes le café ?','Est-ce que vous parlez français ?','あなたはフランス語を話しますか。','est-ce que の後は通常の主語＋動詞の語順。','est-ce que の後に続くのは？',['vous parlez','parlez-vous','parle vous','parlent vous'],'vous parlez','est-ce que 型は平叙文の語順を保つ。')
add(4,'ne … pas の否定','書き言葉では活用した動詞を ne と pas で挟む。母音の前は n’ にする。口語では ne が省略されることもある。','Je parle.','Je ne parle pas.','Je n’ai pas faim.','私はお腹が空いていません。','ai は母音で始まるため ne → n’。','Je ___ parle pas.',['ne','pas','non','jamais'],'ne','基本の否定は ne＋動詞＋pas。')
#5
add(5,'-er動詞の活用','parler型の現在形は je -e / tu -es / il -e / nous -ons / vous -ez / ils -ent。発音は綴りと一致しないことがある。','Je parle.','Nous parlons.','Vous parlez français.','あなたはフランス語を話します。','vous の活用語尾は -ez。','Vous ___ français.',['parle','parles','parlez','parlons'],'parlez','vous の -er動詞語尾は -ez。')
add(5,'-er動詞の綴りと発音','parle, parles, parlent は通常同じように発音される。語尾の違いは主語を見て書く。','Il parle.','Ils parlent.','Ils parlent anglais.','彼らは英語を話します。','ils には parlent。語末の -ent は通常発音しない。','Ils ___ anglais.',['parle','parles','parlons','parlent'],'parlent','ils の三人称複数形には -ent。')
#6
add(6,'動詞の否定を作る','-er動詞でも ne＋活用形＋pas が基本。母音始まりの動詞の前では n’ にする。','Elle danse.','Elle ne danse pas.','Il n’écoute pas la radio.','彼はラジオを聞きません。','écoute の前では ne を n’ にする。','Il ___ écoute pas.',['ne','n’','pas','non'],'n’','母音で始まる動詞には n’ を使う。')
add(6,'動詞を疑問にする','est-ce que の後は通常の主語・動詞の順序。倒置疑問文では Parlez-vous ? のように順が入れ替わる。','Vous travaillez.','Est-ce que vous travaillez ?','Est-ce que tu étudies ?','君は勉強していますか。','est-ce que＋tu＋étudies の語順。','自然な疑問文は？',['Est-ce que tu étudies ?','Est-ce que étudies tu ?','Tu est-ce étudies ?','Étudie est-ce que tu ?'],'Est-ce que tu étudies ?','est-ce que の後は平叙文と同じ語順。')
#7
add(7,'指示形容詞','ce（男性・子音）/ cet（男性・母音・無音h）/ cette（女性）/ ces（複数）で「この・その」を表す。','ce livre','cette table','J’aime cette chanson.','私はこの歌が好きです。','chanson は女性単数だから cette。','「このテーブル」は？',['ce table','cet table','cette table','ces table'],'cette table','table は女性単数。')
add(7,'第2群規則動詞 -ir','finir型の現在形は je finis / nous finissons / ils finissent。すべての -ir動詞がこの型とは限らない。','Je finis.','Nous finissons.','Nous finissons tôt.','私たちは早く終わります。','nous 形は -issons。','Nous ___ tôt.',['finis','finit','finissons','finissent'],'finissons','finir型の nous には -issons。')
add(7,'rendre型の -re動詞','rendre型の現在形は je rends / nous rendons / ils rendent。prendreなどは別の不規則な型。','Je rends ce livre.','Nous rendons ces livres.','Il rend le livre.','彼はその本を返します。','三人称単数は rend。','Il ___ le livre.',['rends','rend','rendons','rendent'],'rend','il の現在形は rend。')
#8
add(8,'aller・venirの活用','aller（行く）は vais, vas, va, allons, allez, vont。venir（来る）は viens, viens, vient, venons, venez, viennent。','Je vais à Paris.','Je viens de Paris.','Nous allons à Paris.','私たちはパリに行きます。','nous に対する aller の形は allons。','Nous ___ à Paris.',['vais','va','allez','allons'],'allons','nous＋aller は nous allons。')
add(8,'近接未来と近接過去','aller＋不定詞は「これから〜する」、venir de＋不定詞は「〜したばかり」。','Je vais partir.','Je viens de partir.','Je vais dormir.','これから寝ます。','vais の後は動詞の不定詞 dormir。','「これから出発する」は？',['Je vais partir.','Je viens de partir.','Je suis parti.','Je partirais.'],'Je vais partir.','aller＋不定詞で近い未来を表す。')
#9
add(9,'直接目的語代名詞','「彼を・彼女を・彼らを」などの直接目的語は le/la/les。一般に活用した動詞の前に置く。','Je vois Marie.','Je la vois.','Je le connais.','私は彼を知っています。','le は直接目的語「彼を」。','Je connais Paul. → Je ___ connais.',['lui','leur','le','y'],'le','Paul は男性単数の直接目的語。')
add(9,'間接目的語代名詞','「〜に」に当たる à＋人を、lui（単数）/leur（複数）で置き換える場合がある。','Je parle à Marie.','Je lui parle.','Je lui téléphone.','私は彼／彼女に電話します。','téléphoner à + 人 → lui téléphoner。','Je téléphone à Marie. → Je ___ téléphone.',['la','le','lui','les'],'lui','人を表す à＋単数の間接目的語を lui に。')
add(9,'目的語代名詞の位置','通常、目的語代名詞は活用形の動詞より前。否定文では ne＋代名詞＋動詞＋pas。','Je le vois.','Je ne le vois pas.','Je ne la connais pas.','私は彼女を知りません。','la は動詞 connais の前に置く。','「私は彼女を見ます」は？',['Je vois la.','Je la vois.','Je lui vois.','Je vois elle.'],'Je la vois.','直接目的語代名詞は通常動詞の前。')
#10
add(10,'pouvoir・vouloir・devoir','pouvoir＋不定詞＝可能、vouloir＋不定詞＝希望、devoir＋不定詞＝義務・推量の基本形。','Je peux partir.','Je veux partir.','Je peux venir demain.','私は明日来られます。','peux＋不定詞 venir で可能を表す。','「私は来られます」は？',['Je veux venir.','Je peux venir.','Je viens de venir.','Je suis venu.'],'Je peux venir.','pouvoir は可能。')
add(10,'複合過去の基本','完了した出来事を表す複合過去は、現在形のavoir/être＋過去分詞。多くの他動詞ではavoirを使う。','Je parle.','J’ai parlé.','J’ai regardé un film.','私は映画を観ました。','ai＋過去分詞 regardé が複合過去。','「私は話しました」は？',['Je parlais.','Je parle.','J’ai parlé.','Je parlerai.'],'J’ai parlé.','avoir＋過去分詞の形。')
add(10,'過去分詞の基本形','規則動詞の過去分詞は -er→-é、-ir（finir型）→-i、-re（rendre型）→-u。例外は別に覚える。','parler → parlé','finir → fini','Nous avons fini.','私たちは終えました。','finir の過去分詞は fini。','finir の過去分詞は？',['finis','fini','finissant','finiré'],'fini','第2群規則動詞の過去分詞は -i。')
#11
add(11,'faire・prendre・mettre','faire（する）/ prendre（取る）/ mettre（置く・着る）は頻出不規則動詞。形を主語と一緒に覚える。','Nous faisons.','Nous prenons.','Je prends le train.','私は電車に乗ります。','prendre の je 形は prends。','Je ___ le train.',['prendre','prends','prend','prenons'],'prends','je＋prendre は prends。')
add(11,'複合過去の否定・目的語','否定は助動詞を ne と pas で挟む。目的語代名詞は助動詞の前に置く。','J’ai vu Paul.','Je ne l’ai pas vu.','Je n’ai pas mangé.','私は食べませんでした。','n’＋ai＋pas＋mangé の並び。','複合過去の否定として正しいのは？',['Je ne ai mangé pas.','Je n’ai pas mangé.','Je n’ai mangé pas.','Je pas ai mangé.'],'Je n’ai pas mangé.','ne と pas は助動詞 ai を挟む。')
#12
add(12,'代名動詞の現在形','se lever は「起きる」。me/te/se/nous/vous/se が主語に一致し、動詞自身も活用する。','Je me lève.','Nous nous levons.','Je me couche tôt.','私は早く寝ます。','je に対応して me、coucher は couche。','Je ___ lève.',['se','me','te','nous'],'me','je に対応する再帰代名詞は me。')
add(12,'代名動詞の複合過去','代名動詞の複合過去は基本的にêtreを使う。ただし過去分詞の性数一致は目的語の役割も関係する。','Elle se lève.','Elle s’est levée.','Elle s’est levée tôt.','彼女は早起きしました。','se lever の複合過去は s’est levée。','Elle ___ levée.',['a se','s’a','s’est','est se'],'s’est','再帰代名詞＋êtreで複合過去をつくる。')
#13
add(13,'所有形容詞 mon・ma・mes','所有者だけでなく、後に続く名詞の性・数で mon/ma/mes を選ぶ。女性単数でも母音前は mon。','mon livre','ma maison','Ma sœur est ici.','私の姉／妹はここにいます。','sœur は女性単数なので ma。','「私の家」は？',['mon maison','ma maison','mes maison','moi maison'],'ma maison','maison は女性単数。')
add(13,'天候・時刻の非人称 il','天候や時刻の il は具体的な「彼」を指さない。il pleut、il fait froid、il est huit heures など。','Il est heureux.（彼は幸せだ）','Il fait froid.（寒い）','Il pleut aujourd’hui.','今日は雨が降っています。','il は特定の人物を指さない。','「雨が降っています」は？',['Il est pleut.','Il pleut.','Elle pleut.','Je pleut.'],'Il pleut.','天候を表す非人称動詞 pleuvoir は通常 il と使う。')
#14
add(14,'関係代名詞 qui','先行詞が関係節内の「主語」なら qui を使う。qui の直後には原則として動詞が続く。','un homme qui parle','un livre qui est ici','Voici une personne qui parle français.','こちらはフランス語を話す人です。','qui は parle の主語の働き。','「話す人」は？',['une personne que parle','une personne qui parle','une personne où parle','une personne de parle'],'une personne qui parle','後ろの動詞の主語になる場合は qui。')
add(14,'関係代名詞 que','先行詞が関係節内の直接目的語なら que（母音前は qu’）。後ろには主語が続くことが多い。','le film qui commence','le film que je regarde','Le livre que je lis est intéressant.','私が読んでいる本は面白いです。','que は je lis の目的語。','「私が読んでいる本」は？',['le livre qui je lis','le livre que je lis','le livre où je lis','le livre de je lis'],'le livre que je lis','je lis の目的語になっている。')
#15
add(15,'指示代名詞 celui系','celui/celle/ceux/celles は「こちらのもの・人」を名詞の代わりに指す。性数で形が変わる。','celui de Paul','celle de Marie','Je préfère celle-ci.','私はこちら（女性名詞のもの）が好きです。','celle は女性単数名詞を受ける。','女性単数名詞を受けるのは？',['celui','celle','ceux','celles'],'celle','女性単数は celle。')
add(15,'関係副詞 où','場所や時間を表す名詞に続く関係節を où で導く。qui/queとの役割の違いを意識する。','la ville où je vis','la ville que je visite','C’est la ville où j’habite.','ここが私の住んでいる街です。','où は場所を表す la ville を説明。','「私が住む街」は？',['la ville qui j’habite','la ville où j’habite','la ville que j’habite à','la ville de j’habite'],'la ville où j’habite','場所を受ける関係副詞 où。')
#16
add(16,'半過去の形と用法','半過去は過去の習慣・背景・続いていた状態を表す。nous現在形の -ons を除いた語幹に -ais/-ait/-ions など。','Je travaille.','Je travaillais.','Quand j’étais petit, je lisais beaucoup.','小さい頃はよく本を読んでいました。','étais・lisais は過去の状態・習慣。','「昔はよく読んでいた」に合う形は？',['j’ai lu','je lisais','je lirai','je lis'],'je lisais','過去の習慣には半過去。')
add(16,'複合過去と半過去','複合過去は出来事の発生・完了、半過去はそのときの背景・継続。組み合わせて使える。','Il pleuvait.（雨が降っていた）','Il a commencé à pleuvoir.（降り始めた）','Je dormais quand il a téléphoné.','彼が電話したとき私は寝ていました。','背景 dormais と出来事 a téléphoné を対比。','「電話した」という完了した出来事は？',['a téléphoné','téléphonait','téléphonera','téléphone'],'a téléphoné','完結した出来事には複合過去。')
add(16,'大過去の基本','過去のある時点より前に起きたことは、大過去（avoir/êtreの半過去＋過去分詞）で表す。','J’ai mangé.','J’avais mangé.','J’avais déjà fini quand il est arrivé.','彼が着いたとき私はすでに終えていました。','avais fini は「到着より前」を表す。','「すでに終えていた」は？',['J’ai fini.','J’avais fini.','Je finirai.','Je finis.'],'J’avais fini.','大過去は半過去の助動詞＋過去分詞。')
#17
add(17,'比較級','plus / aussi / moins＋形容詞・副詞＋que で「より・同程度・より少なく」。形容詞は名詞に一致。','plus grand que','aussi grand que','Elle est plus grande que moi.','彼女は私より背が高いです。','plus grande que で優等比較。','「〜より大きい」は？',['plus … que','très … de','aussi … de','moins … avec'],'plus … que','plus＋形容詞＋que。')
add(17,'最上級','le/la/les＋plus/moins＋形容詞などで「最も〜」。形容詞の位置と冠詞の一致に注意。','une grande ville','la plus grande ville','C’est la plus grande ville.','それは最も大きな都市です。','ville は女性単数なので la plus grande。','「最も大きな都市」は？',['le plus grande ville','la plus grande ville','la plus grand ville','les plus grande ville'],'la plus grande ville','冠詞と形容詞が女性単数。')
add(17,'受動態と on','受動態はêtre＋過去分詞。行為者を示さずに on＋能動形で言い換えられる場合がある。','On ouvre la porte.','La porte est ouverte.','La porte est fermée.','ドアは閉められています。','être＋過去分詞が性数一致。','「ドアは閉められている」に合うのは？',['La porte est fermé.','La porte est fermée.','La porte a fermée.','La porte sont fermées.'],'La porte est fermée.','porte は女性単数で fermée。')
#18
add(18,'単純未来','単純未来は不定詞に -ai/-as/-a/-ons/-ez/-ont などを付ける（不規則語幹あり）。','Je parle.','Je parlerai.','Demain, je travaillerai.','明日、私は働くでしょう。','travaillerai は単純未来。','「私は話すでしょう」は？',['Je parlais.','Je parlerai.','J’ai parlé.','Je parle.'],'Je parlerai.','parler＋-ai で単純未来。')
add(18,'否定 pas以外','ne…jamais（決して〜ない）、ne…plus（もう〜ない）、ne…rien（何も〜ない）を整理する。','Je mange encore.','Je ne mange plus.','Je ne fume jamais.','私は決してタバコを吸いません。','ne…jamais が頻度の否定。','「決して〜ない」にする語は？',['toujours','jamais','encore','aussi'],'jamais','ne…jamais が基本。')
#19
add(19,'命令形','tu・nous・vous の形を使い、通常主語を省く。-er型の肯定tu命令は原則 -s を落とす。','Tu parles.','Parle !','Écoutez, s’il vous plaît.','聞いてください。','vous に対する命令形は écoutez。','tu に「話して！」は？',['Parles !','Parle !','Parlez !','Parlent !'],'Parle !','-er型の肯定tu命令は多くの場合 -s を落とす。')
add(19,'疑問形容詞 quel','quel/quelle/quels/quelles は後ろの名詞の性と数に一致する。','Quel livre ?','Quelle heure ?','Quelle heure est-il ?','何時ですか。','heure は女性単数なので quelle。','「何時？」は？',['Quel heure ?','Quelle heure ?','Quels heure ?','Quelles heure ?'],'Quelle heure ?','heure は女性名詞単数。')
#20
add(20,'疑問副詞 quand・où・pourquoi','時間は quand、場所は où、理由は pourquoi。疑問文の語順との組み合わせを覚える。','Où vas-tu ?','Pourquoi pars-tu ?','Où habitez-vous ?','どこに住んでいますか。','où は場所を尋ねる。','「なぜ？」は？',['où','qui','pourquoi','quand'],'pourquoi','理由を尋ねる疑問副詞。')
add(20,'y・en の基本','y は場所や à＋物、en は de＋物や数量に関わる要素を受けることが多い。位置は目的語代名詞と同様。','Je vais à Paris → J’y vais.','Je veux du café → J’en veux.','J’y vais demain.','私は明日そこへ行きます。','y は前に出た場所を指す。','「そこへ行く」は？',['J’en vais.','J’y vais.','Je le vais.','Je lui vais.'],'J’y vais.','aller＋場所を y で表す。')
#21
add(21,'条件法による丁寧な依頼','条件法現在は語幹が単純未来、語尾が半過去に近い。voudrais などは丁寧な希望・依頼に頻出。','Je veux un café.','Je voudrais un café.','Je voudrais de l’eau.','お水をいただきたいです。','voudrais は直接的な veux より柔らかな言い方。','店で丁寧に注文するのは？',['Je veux un café.','Je voudrais un café.','Je prendrai vouloir.','Je suis un café.'],'Je voudrais un café.','丁寧に希望を伝える条件法。')
add(21,'仮定と条件法','現実とは違う仮定を述べる基本形は si＋半過去、主節に条件法現在。si節に条件法は通常置かない。','Si j’ai le temps, je viens.','Si j’avais le temps, je viendrais.','Si j’avais le temps, je voyagerais.','時間があれば旅行するのに。','si節は avais（半過去）、主節は voyagerais（条件法）。','「もし時間があれば旅行するのに」の主節は？',['je voyage','je voyagerais','je voyageais','j’ai voyagé'],'je voyagerais','現実と異なる仮定の帰結は条件法。')
#22
add(22,'間接疑問文','疑問を文の中に埋め込むと、通常は倒置せず平叙文の語順にする。si は「〜かどうか」。','Où est-il ?','Je sais où il est.','Je ne sais pas où il habite.','彼がどこに住んでいるか分かりません。','間接疑問は où il habite の語順。','自然な間接疑問は？',['Je sais où est-il.','Je sais où il est.','Je sais est où il.','Je sais il où est.'],'Je sais où il est.','間接疑問では通常倒置しない。')
add(22,'時制の一致と話法','発言を過去形の動詞で伝えると、従属節の時制や時間表現が変わる場合がある。初級では直接話法と間接話法の違いから理解する。','Il dit : « Je suis fatigué. »','Il a dit qu’il était fatigué.','Elle a dit qu’elle était malade.','彼女は病気だったと言いました。','a dit の後の était は過去の発言内容を伝える。','過去の発言を伝える自然な形は？',['Elle a dit qu’elle était malade.','Elle a dit que elle est malade hier.','Elle a dit elle malade.','Elle a dit que était-elle malade.'],'Elle a dit qu’elle était malade.','間接話法は que節で伝え、人称・時制を調整する。')
#23
add(23,'接続法の基本形','必要・希望・不確実な判断などを表す que節で接続法を用いることがある。il faut que が代表例。','Il vient.','Il faut qu’il vienne.','Il faut que tu viennes.','君が来る必要があります。','il faut que の後は接続法 viennes。','「君が来る必要がある」は？',['Il faut que tu viens.','Il faut que tu viennes.','Il faut tu venir.','Il faut que tu viendras.'],'Il faut que tu viennes.','il faut que の後は接続法。')
add(23,'書き言葉の過去時制','単純過去や前過去は主に文学作品・歴史的叙述で現れる。会話の基礎では複合過去を優先し、まず見分け方を学ぶ。','Il entra.（単純過去・文語）','Il est entré.（複合過去・一般的）','Il entra dans la maison.','彼は家に入りました。（文語）','entra は entrer の単純過去で、現代会話では普通使わない。','通常の会話で「彼は入った」は？',['Il entra.','Il est entré.','Il entrât.','Il fût entré.'],'Il est entré.','日常の完了した出来事には複合過去を用いる。')

units=cs
assert len(chapters)==24
assert sorted(set(x['chapter'] for x in units))==list(range(24))
assert len({u['id'] for u in units})==len(units)
assert all(len(u['options'])==4 and u['options'].count(u['answer'])==1 for u in units)
assert all(len(u['rule'])<=200 and len(u['why'])<=150 for u in units)
for i,ch in enumerate(chapters):
 assert sum(u['chapter']==i for u in units)>0
out={'source':{'title':'フランス語初級文法','author':'小栗栖等（名古屋大学）','edition':'2022年9月版','scope':'序章＋第1〜23章','attribution':'教育順序と文法事項を参照。例文と説明・問題はアプリ用に独自作成し原著の音声は利用しない。'},'chapters':[dict(id=i,title=t,goal=g,sourcePage=p,legacyLessons=ls) for i,(t,g,p,ls) in enumerate(chapters)],'units':units}
(OUT/'course.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print(len(chapters),'chapters',len(units),'original teaching units',len(units),'knowledge checks',len(units))
print([(i, sum(x['chapter']==i for x in units)) for i in range(len(chapters))])
