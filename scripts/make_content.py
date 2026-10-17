#!/usr/bin/env python3
"""Build the 500-example A1/A2 catalogue from explicitly authored lesson examples.

Lines use: French sentence with [test target] | natural Japanese translation.
The square brackets indicate the ONE token tested, not literal text.
"""
from pathlib import Path
import json, re
ROOT=Path(__file__).resolve().parents[1]
LESSONS=[]; EXAMPLES=[]
def add(chapter,title,rule,pitfall,choices,lines):
    n=len(LESSONS)+1
    rows=[s.strip() for s in lines.strip().splitlines() if s.strip()]
    if len(rows)!=10: raise ValueError(f'{n} {title}: need 10 examples, got {len(rows)}')
    opts=[x.strip() for x in choices.split('|')]
    if len(opts)<4 or len(set(opts))!=len(opts): raise ValueError(f'{n} bad options')
    LESSONS.append(dict(id=n,chapter=chapter,title=title,rule=rule,pitfall=pitfall))
    for r in rows:
        fr,ja=[p.strip() for p in r.split(' | ',1)]
        m=re.findall(r'\[([^\[\]]+)\]',fr)
        if len(m)!=1: raise ValueError(f'{n}: invalid marker in {fr}')
        correct=m[0]
        row_opts=opts
        if correct not in row_opts and fr.startswith("["+correct+"]") and correct[0].isupper():
            row_opts=[v[0].upper()+v[1:] for v in opts]
        if correct not in row_opts: raise ValueError(f'{n}: {correct} not in {row_opts}')
        if not ja or not fr: raise ValueError(f'{n}: empty translation')
        EXAMPLES.append(dict(id=len(EXAMPLES)+1,lesson=n,fr=fr.replace('['+correct+']',correct),ja=ja,blank=fr.replace('['+correct+']','___'),answer=correct,options=row_opts,detail=f'{title}：{rule}',pitfall=pitfall))

# ── 1 文の構造 ──
add('文の基本','主語の人称代名詞','フランス語は原則として主語を明示し、動詞は主語の人称と数に応じて活用します。','je は母音・無音のhの前で j’ になります。','Je|Tu|Il|Nous|Vous|Ils', '''
[Je] parle français. | 私はフランス語を話します。
[Tu] parles français. | 君はフランス語を話します。
[Il] parle français. | 彼はフランス語を話します。
[Nous] parlons français. | 私たちはフランス語を話します。
[Vous] parlez français. | あなたはフランス語を話します。
[Ils] parlent français. | 彼らはフランス語を話します。
[Je] travaille ici. | 私はここで働いています。
[Tu] travailles ici. | 君はここで働いています。
[Nous] étudions ensemble. | 私たちは一緒に勉強します。
[Vous] habitez à Paris. | あなたはパリに住んでいます。
''')
add('文の基本','elle・elles・on の使い方','elle は「彼女」、elles は女性だけの複数を指します。on は口語で「私たち」の意味にも使われ、動詞は三人称単数形になります。','on を使うときの動詞は nous と違って三人称単数形です。','Elle|Elles|On|Nous|Ils', '''
[Elle] est française. | 彼女はフランス人です。
[Elles] sont françaises. | 彼女たちはフランス人です。
[On] parle français ici. | ここではフランス語を話します。
[On] va au cinéma. | 私たちは映画館へ行きます。
[Nous] allons au cinéma. | 私たちは映画館へ行きます。
[Elles] travaillent ensemble. | 彼女たちは一緒に働いています。
[Elle] travaille aujourd'hui. | 彼女は今日働きます。
[Ils] sont japonais. | 彼らは日本人です。
[On] mange à midi. | 私たちは正午に食事をします。
[Nous] sommes prêts. | 私たちは準備ができています。
''')
add('基本動詞','être：現在形','être（〜である）の現在形は je suis / tu es / il est / nous sommes / vous êtes / ils sont です。','être の活用形は規則動詞とは異なります。','suis|es|est|sommes|êtes|sont', '''
Je [suis] étudiant. | 私は学生です（男性形）。
Tu [es] gentil. | 君は親切です（男性形）。
Il [est] malade. | 彼は具合が悪いです。
Nous [sommes] contents. | 私たちはうれしいです（男性または男女混合）。
Vous [êtes] en retard. | あなたは遅れています。
Ils [sont] fatigués. | 彼らは疲れています。
Je [suis] prêt. | 私は準備ができています（男性形）。
Elle [est] journaliste. | 彼女は記者です。
Elles [sont] ici. | 彼女たちはここにいます。
Nous [sommes] à Paris. | 私たちはパリにいます。
''')
add('基本動詞','avoir：現在形','avoir（持っている）の現在形は j’ai / tu as / il a / nous avons / vous avez / ils ont です。年齢・空腹・暑さにも使います。','年齢は être ではなく avoir を用いて J’ai vingt ans. と言います。','ai|as|a|avons|avez|ont', '''
J’[ai] un livre. | 私は本を一冊持っています。
Tu [as] une sœur. | 君には姉か妹がいます。
Il [a] un chat. | 彼は猫を飼っています。
Nous [avons] une voiture. | 私たちは車を持っています。
Vous [avez] raison. | あなたの言うとおりです。
Ils [ont] faim. | 彼らはお腹がすいています。
J’[ai] vingt ans. | 私は20歳です。
Elle [a] froid. | 彼女は寒がっています。
Nous [avons] soif. | 私たちは喉が渇いています。
Vous [avez] le temps. | あなたには時間があります。
''')
add('名詞と冠詞','不定冠詞 un・une・des','不特定の数えられる名詞に、男性単数 un、女性単数 une、複数 des を使います。','フランス語の名詞には文法上の性があります。','un|une|des|le|la|les', '''
J'ai [un] stylo. | 私はペンを一本持っています。
Elle a [une] voiture. | 彼女は車を一台持っています。
Nous avons [des] livres. | 私たちは本を何冊か持っています。
Il achète [un] journal. | 彼は新聞を一部買います。
Je cherche [une] chaise. | 私は椅子を一脚探しています。
Tu as [des] amis. | 君には友人が何人かいます。
C'est [un] cadeau. | これは贈り物です。
Voilà [une] maison. | あそこに家があります。
J'entends [des] oiseaux. | 鳥の鳴き声が聞こえます。
Elle porte [un] manteau. | 彼女はコートを着ています。
''')
add('名詞と冠詞','定冠詞 le・la・les・l’','特定できる名詞や一般的なものを表す名詞には、男性単数 le、女性単数 la、複数 les を使います。母音・無音hの前では l’ です。','好き嫌いの対象など、一般的な名詞にも定冠詞を使います。','le|la|les|l’', '''
Je ferme [la] porte. | 私はそのドアを閉めます。
Il lit [le] journal. | 彼はその新聞を読んでいます。
Elle aime [les] chats. | 彼女は猫が好きです。
J'ouvre [la] fenêtre. | 私はその窓を開けます。
Nous regardons [les] étoiles. | 私たちは星を眺めます。
Tu connais [le] professeur. | 君はその先生を知っています。
Je prends [l’]ascenseur. | 私はエレベーターに乗ります。
Il regarde [l’]horloge. | 彼は時計を見ています。
Elle cherche [les] clés. | 彼女は鍵を探しています。
Je préfère [la] musique. | 私は音楽の方が好きです。
''')
add('名詞と冠詞','部分冠詞 du・de la・de l’','数えられないものの不特定の量は、男性 du、女性 de la、母音・無音hの前 de l’ を使います。','否定文では通常 du / de la / de l’ が de / d’ になります（être構文などを除く）。','du|de la|de l’|un|une|des', '''
Je bois [du] café. | 私はコーヒーを飲みます。
Elle boit [de l’]eau. | 彼女は水を飲みます。
Nous mangeons [du] pain. | 私たちはパンを食べます。
Il achète [de la] viande. | 彼は肉を買います。
Tu veux [du] lait ? | 君は牛乳が欲しい？
Je prends [de la] soupe. | 私はスープをいただきます。
Elle ajoute [de l’]huile. | 彼女は油を加えます。
Nous achetons [du] fromage. | 私たちはチーズを買います。
Il faut [de la] patience. | 忍耐が必要です。
Je voudrais [de l’]air frais. | 私は新鮮な空気が欲しいです。
''')
add('名詞と冠詞','名詞の性：男性名詞・女性名詞','名詞には男性・女性の文法上の性があり、冠詞や形容詞がそれに一致します。','語尾だけでは性を確定できないため、名詞は冠詞と一緒に覚えます。','un|une|le|la|des|les', '''
C'est [un] problème. | これは問題です。
Voilà [une] table. | あそこにテーブルがあります。
J'achète [un] téléphone. | 私は電話機を買います。
Elle porte [une] robe. | 彼女はワンピースを着ています。
Je vois [le] soleil. | 私は太陽を見ます。
Je regarde [la] lune. | 私は月を見ます。
Il ouvre [une] porte. | 彼はドアを開けます。
Nous avons [un] jardin. | 私たちには庭があります。
Elle préfère [le] chocolat. | 彼女はチョコレートの方が好きです。
Ils visitent [la] ville. | 彼らはその町を訪れます。
''')
add('名詞と冠詞','名詞の複数形','名詞は原則として -s を付けて複数にします。-eau は -eaux など例外があります。','単数・複数で発音が変わらない語も多いので冠詞を手掛かりにします。','livres|chats|voitures|amis|maisons|bateaux|journaux|enfants|fleurs|chevaux', '''
J'achète deux [livres]. | 私は本を二冊買います。
Il aime les [chats]. | 彼は猫が好きです。
Nous avons trois [voitures]. | 私たちは車を三台持っています。
Elle invite ses [amis]. | 彼女は友人たちを招きます。
Je vois cinq [maisons]. | 私には家が五軒見えます。
Ils regardent les [bateaux]. | 彼らは船を眺めています。
Nous lisons les [journaux]. | 私たちは新聞を読んでいます。
Les [enfants] jouent. | 子どもたちは遊んでいます。
Elle achète des [fleurs]. | 彼女は花を買います。
Il observe les [chevaux]. | 彼は馬たちを観察しています。
''')
add('形容詞','形容詞の性数一致','形容詞は修飾する名詞の性・数と一致します。女性形は通常 -e、複数形は通常 -s を加えます。','beau/belle など不規則変化もあります。','petit|petite|petits|petites|grand|grande|grands|grandes|content|contente', '''
C'est un [petit] chat. | これは小さな猫です。
C'est une [petite] maison. | これは小さな家です。
Ce sont de [petits] chiens. | これは小さな犬たちです。
Ce sont de [petites] fleurs. | これは小さな花々です。
Il est [grand]. | 彼は背が高いです。
Elle est [grande]. | 彼女は背が高いです。
Ils sont [grands]. | 彼らは背が高いです。
Elles sont [grandes]. | 彼女たちは背が高いです。
Il est [content]. | 彼はうれしいです。
Elle est [contente]. | 彼女はうれしいです。
''')
add('形容詞','形容詞の語順','多くの形容詞は名詞の後ろに置きますが、petit / grand / beau / bon などは通常前に置きます。','意味によって前後の位置が変わる形容詞もあります。','petit|grande|beau|bon|rouge|française|intéressant|blanche|nouveau|noire', '''
J'ai un [petit] chien. | 私は小さな犬を飼っています。
Elle vit dans une [grande] maison. | 彼女は大きな家に住んでいます。
C'est un [beau] jardin. | これは美しい庭です。
Il prépare un [bon] repas. | 彼はおいしい食事を作ります。
Elle porte une robe [rouge]. | 彼女は赤いワンピースを着ています。
J'apprends la langue [française]. | 私はフランス語を学んでいます。
Nous regardons un film [intéressant]. | 私たちは面白い映画を見ます。
Elle a une voiture [blanche]. | 彼女は白い車を持っています。
Il achète un [nouveau] téléphone. | 彼は新しい電話機を買います。
J'ai une veste [noire]. | 私は黒いジャケットを持っています。
''')
add('限定詞','所有形容詞 mon・ma・mes','所有形容詞は所有者でなく、後ろの名詞の性・数に合わせます（mon / ma / mes など）。','女性名詞でも母音で始まる単数名詞の前では mon を使います（mon amie）。','mon|ma|mes|ton|ta|tes|son|sa|ses', '''
C'est [mon] frère. | これは私の兄弟です。
Voilà [ma] sœur. | あちらが私の姉妹です。
Je cherche [mes] clés. | 私は自分の鍵を探しています。
Tu aimes [ton] travail. | 君は自分の仕事が好きです。
Où est [ta] voiture ? | 君の車はどこですか？
Tu connais [tes] voisins ? | 君は近所の人たちを知っていますか？
Il prend [son] sac. | 彼は自分のかばんを持ちます。
Elle ferme [sa] porte. | 彼女は自分のドアを閉めます。
Il appelle [ses] amis. | 彼は自分の友人たちに電話します。
Je parle avec [mon] amie. | 私は女友達と話します。
''')
add('限定詞','指示形容詞 ce・cet・cette・ces','「この・その」の意味で男性 ce、男性かつ母音始まり cet、女性 cette、複数 ces を使います。','cet は男性名詞の母音・無音hの前だけです。','ce|cet|cette|ces', '''
J'aime [ce] livre. | 私はこの本が好きです。
Je connais [cet] homme. | 私はこの男性を知っています。
Regarde [cette] maison. | この家を見て。
Je préfère [ces] chaussures. | 私はこの靴の方が好きです。
[Ce] film est drôle. | この映画は面白いです。
[Cet] hôtel est calme. | このホテルは静かです。
[Cette] fille est gentille. | この女の子は親切です。
[Ces] enfants jouent. | この子どもたちは遊んでいます。
[Cet] arbre est vieux. | この木は古いです。
[Ce] restaurant est ouvert. | このレストランは営業しています。
''')
add('基本構文','c’est と il est','c’est は人や物を提示・特定し、il est / elle est は既出の人物・物の性質を述べる際によく使います。','C’est un professeur. と Il est professeur. のように、冠詞の有無も違います。','C’est|Il est|Elle est|Ce sont|Ils sont|Elles sont', '''
[C’est] un livre. | これは本です。
[Il est] professeur. | 彼は教師です。
[Elle est] médecin. | 彼女は医師です。
[Ce sont] mes amis. | こちらは私の友人たちです。
[Ils sont] sympathiques. | 彼らは感じがいいです。
[Elles sont] étudiantes. | 彼女たちは学生です。
[C’est] intéressant. | それは面白いです。
[Il est] en retard. | 彼は遅刻しています。
[Elle est] à la maison. | 彼女は家にいます。
[Ce sont] des fleurs. | これらは花です。
''')
add('動詞の現在形','-er動詞：parler','parler などの規則 -er 動詞は -e / -es / -e / -ons / -ez / -ent と活用します。','-ent は通常発音しません。','parle|parles|parlons|parlez|parlent|parler', '''
Je [parle] avec Marie. | 私はマリーと話します。
Tu [parles] anglais. | 君は英語を話します。
Elle [parle] japonais. | 彼女は日本語を話します。
Nous [parlons] ensemble. | 私たちは一緒に話します。
Vous [parlez] vite. | あなたは速く話します。
Ils [parlent] lentement. | 彼らはゆっくり話します。
Je veux [parler] français. | 私はフランス語を話したいです。
On [parle] ici. | ここでは話をします。
Elles [parlent] souvent. | 彼女たちはよく話します。
Nous aimons [parler]. | 私たちは話すのが好きです。
''')
add('動詞の現在形','-er動詞：aimer・regarder','-er 動詞は規則的に活用し、aimer は「好き」、regarder は「見る」を表します。','aimer + 不定詞で「〜するのが好き」です。','aime|aimes|aimons|aimez|aiment|regarde|regardes|regardons|regardez|regardent', '''
J'[aime] le café. | 私はコーヒーが好きです。
Tu [aimes] le chocolat. | 君はチョコレートが好きです。
Elle [aime] la musique. | 彼女は音楽が好きです。
Nous [aimons] voyager. | 私たちは旅行が好きです。
Vous [aimez] lire. | あなたは読書が好きです。
Ils [aiment] le sport. | 彼らはスポーツが好きです。
Je [regarde] la télévision. | 私はテレビを見ます。
Tu [regardes] le ciel. | 君は空を見ます。
Nous [regardons] un film. | 私たちは映画を見ます。
Elles [regardent] les photos. | 彼女たちは写真を見ます。
''')
add('動詞の現在形','-ir動詞：finir','finir 型の規則動詞は -is / -is / -it / -issons / -issez / -issent と活用します。','-ir 動詞すべてが finir 型ではありません。','finis|finit|finissons|finissez|finissent|finir', '''
Je [finis] mon travail. | 私は仕事を終えます。
Tu [finis] ton repas. | 君は食事を終えます。
Il [finit] son livre. | 彼は本を読み終えます。
Nous [finissons] à cinq heures. | 私たちは5時に終わります。
Vous [finissez] aujourd'hui. | あなたは今日終えます。
Ils [finissent] leurs devoirs. | 彼らは宿題を終えます。
Je dois [finir] maintenant. | 私は今終えなければなりません。
Elle [finit] tôt. | 彼女は早く終わります。
Elles [finissent] ensemble. | 彼女たちは一緒に終えます。
Nous voulons [finir] ce travail. | 私たちはこの仕事を終えたいです。
''')
add('動詞の現在形','-re動詞：attendre','attendre など一部の -re 動詞は j’attends / tu attends / il attend / nous attendons / vous attendez / ils attendent と活用します。','すべての -re 動詞が同じ活用になるわけではありません。','attends|attend|attendons|attendez|attendent|attendre', '''
J'[attends] le bus. | 私はバスを待っています。
Tu [attends] ton ami. | 君は友人を待っています。
Elle [attend] le train. | 彼女は電車を待っています。
Nous [attendons] ici. | 私たちはここで待っています。
Vous [attendez] longtemps. | あなたは長い間待っています。
Ils [attendent] dehors. | 彼らは外で待っています。
Je dois [attendre]. | 私は待たなければなりません。
Il [attend] sa sœur. | 彼は姉妹を待っています。
Elles [attendent] le professeur. | 彼女たちは先生を待っています。
Nous allons [attendre] ici. | 私たちはここで待ちます。
''')
add('動詞の現在形','aller：現在形','aller（行く）は je vais / tu vas / il va / nous allons / vous allez / ils vont と活用します。','aller + 不定詞の近接未来は別の重要項目です。','vais|vas|va|allons|allez|vont|aller', '''
Je [vais] à Paris. | 私はパリへ行きます。
Tu [vas] à l'école. | 君は学校へ行きます。
Elle [va] au travail. | 彼女は仕事へ行きます。
Nous [allons] au marché. | 私たちは市場へ行きます。
Vous [allez] à la gare. | あなたは駅へ行きます。
Ils [vont] au musée. | 彼らは博物館へ行きます。
Je veux [aller] à Lyon. | 私はリヨンへ行きたいです。
On [va] au parc. | 私たちは公園へ行きます。
Elles [vont] au cinéma. | 彼女たちは映画館へ行きます。
Nous aimons [aller] à pied. | 私たちは歩いて行くのが好きです。
''')
add('動詞の現在形','faire：現在形','faire（する・作る）は je fais / tu fais / il fait / nous faisons / vous faites / ils font と活用します。','天候にも Il fait beau. のように faire を使います。','fais|fait|faisons|faites|font|faire', '''
Je [fais] du sport. | 私は運動をします。
Tu [fais] la cuisine. | 君は料理をします。
Il [fait] beau. | 天気がいいです。
Nous [faisons] une promenade. | 私たちは散歩をします。
Vous [faites] du vélo. | あなたは自転車に乗ります。
Ils [font] leurs devoirs. | 彼らは宿題をします。
Je veux [faire] une pause. | 私は休憩したいです。
Elle [fait] du piano. | 彼女はピアノを弾きます。
Elles [font] du tennis. | 彼女たちはテニスをします。
Nous allons [faire] les courses. | 私たちは買い物をします。
''')
add('動詞の現在形','venir：現在形','venir（来る）は je viens / tu viens / il vient / nous venons / vous venez / ils viennent と活用します。','venir de + 不定詞は「〜したばかり」という近接過去になります。','viens|vient|venons|venez|viennent|venir', '''
Je [viens] demain. | 私は明日来ます。
Tu [viens] avec moi ? | 君は私と一緒に来る？
Elle [vient] ce soir. | 彼女は今夜来ます。
Nous [venons] de Tokyo. | 私たちは東京出身です。
Vous [venez] souvent ici. | あなたはよくここに来ます。
Ils [viennent] en train. | 彼らは電車で来ます。
Je peux [venir] demain. | 私は明日来られます。
Il [vient] à midi. | 彼は正午に来ます。
Elles [viennent] ensemble. | 彼女たちは一緒に来ます。
Nous voulons [venir] aussi. | 私たちも来たいです。
''')
add('動詞の現在形','prendre：現在形','prendre（取る・乗る）は je prends / tu prends / il prend / nous prenons / vous prenez / ils prennent と活用します。','prendre le train は「電車に乗る」、prendre un café は「コーヒーを飲む」です。','prends|prend|prenons|prenez|prennent|prendre', '''
Je [prends] le bus. | 私はバスに乗ります。
Tu [prends] un café ? | 君はコーヒーを飲む？
Elle [prend] son sac. | 彼女はかばんを持ちます。
Nous [prenons] le train. | 私たちは電車に乗ります。
Vous [prenez] du thé ? | あなたはお茶を飲みますか？
Ils [prennent] des photos. | 彼らは写真を撮ります。
Je vais [prendre] une douche. | 私はシャワーを浴びます。
On [prend] le métro. | 私たちは地下鉄に乗ります。
Elles [prennent] le déjeuner. | 彼女たちは昼食を取ります。
Nous devons [prendre] un taxi. | 私たちはタクシーに乗らなければなりません。
''')
add('助動詞的表現','pouvoir：〜できる','pouvoir + 不定詞で能力・可能性・許可を表します。現在形は peux / peux / peut / pouvons / pouvez / peuvent。','pouvoir の後ろの動詞は不定詞です。','peux|peut|pouvons|pouvez|peuvent|pouvoir', '''
Je [peux] nager. | 私は泳げます。
Tu [peux] venir. | 君は来られます。
Elle [peut] conduire. | 彼女は運転できます。
Nous [pouvons] partir. | 私たちは出発できます。
Vous [pouvez] entrer. | あなたは入ってもいいです。
Ils [peuvent] aider. | 彼らは手伝えます。
Je voudrais [pouvoir] voyager. | 私は旅行できるようになりたいです。
Il [peut] attendre. | 彼は待つことができます。
Elles [peuvent] parler français. | 彼女たちはフランス語を話せます。
Nous [pouvons] essayer. | 私たちは試せます。
''')
add('助動詞的表現','vouloir：〜したい','vouloir + 不定詞で「〜したい」を表します。現在形は veux / veux / veut / voulons / voulez / veulent。','Je veux... は状況によって強い言い方になるため、依頼では Je voudrais... が丁寧です。','veux|veut|voulons|voulez|veulent|vouloir', '''
Je [veux] dormir. | 私は眠りたいです。
Tu [veux] manger ? | 君は食べたい？
Il [veut] partir. | 彼は出発したがっています。
Nous [voulons] apprendre. | 私たちは学びたいです。
Vous [voulez] boire ? | あなたは飲みたいですか？
Elles [veulent] voyager. | 彼女たちは旅行したがっています。
Je [veux] apprendre le français. | 私はフランス語を学びたいです。
Elle [veut] étudier. | 彼女は勉強したがっています。
Ils [veulent] jouer. | 彼らは遊びたがっています。
Nous [voulons] rester ici. | 私たちはここにいたいです。
''')
add('助動詞的表現','devoir：〜しなければならない','devoir + 不定詞で義務を表します。現在形は dois / dois / doit / devons / devez / doivent。','文脈により「〜のはずだ」という推量にもなります。','dois|doit|devons|devez|doivent|devoir', '''
Je [dois] partir. | 私は出発しなければなりません。
Tu [dois] travailler. | 君は働かなければなりません。
Elle [doit] étudier. | 彼女は勉強しなければなりません。
Nous [devons] attendre. | 私たちは待たなければなりません。
Vous [devez] écouter. | あなたは聞かなければなりません。
Ils [doivent] rentrer. | 彼らは帰らなければなりません。
Tu [dois] venir demain. | 君は明日来なければなりません。
Il [doit] pleuvoir demain. | 明日は雨が降るはずです。
Elles [doivent] partir tôt. | 彼女たちは早く出発しなければなりません。
Nous [devons] faire attention. | 私たちは注意しなければなりません。
''')
add('否定と疑問','基本の否定 ne...pas','一般的な否定文では活用動詞を ne ... pas で挟みます。母音の前で ne は n’ になります。','口語で ne が省略されることがありますが、初級では完全形を学びます。','ne|n’|pas|jamais|plus|rien', '''
Je [ne] parle pas anglais. | 私は英語を話しません。
Il [n’]a pas faim. | 彼はお腹がすいていません。
Nous ne mangeons [pas] ici. | 私たちはここでは食べません。
Elle [ne] vient pas demain. | 彼女は明日来ません。
Je [n’]aime pas le café. | 私はコーヒーが好きではありません。
Tu ne travailles [pas] aujourd'hui. | 君は今日は働きません。
Ils [ne] comprennent pas. | 彼らは理解していません。
Vous [n’]êtes pas en retard. | あなたは遅れていません。
Je ne suis [pas] fatigué. | 私は疲れていません（男性形）。
Elle [ne] regarde pas la télévision. | 彼女はテレビを見ません。
''')
add('否定と疑問','jamais・plus・rien の否定','ne...jamais（決して〜ない）、ne...plus（もう〜ない）、ne...rien（何も〜ない）で意味が変わります。','ne...rien は直接目的語として使え、語順に注意が必要です。','jamais|plus|rien|pas|personne', '''
Je ne fume [jamais]. | 私は決してたばこを吸いません。
Il ne travaille [plus]. | 彼はもう働いていません。
Je ne vois [rien]. | 私には何も見えません。
Elle ne mange [jamais] de viande. | 彼女は決して肉を食べません。
Nous ne venons [plus] ici. | 私たちはもうここへ来ません。
Je n'entends [rien]. | 私には何も聞こえません。
Ils ne sortent [jamais] le soir. | 彼らは夜に決して外出しません。
Tu n'as [plus] de café. | 君にはもうコーヒーがありません。
Elle ne dit [rien]. | 彼女は何も言いません。
Je ne connais [personne] ici. | 私はここに知り合いが誰もいません。
''')
add('否定と疑問','est-ce que による疑問文','平叙文の前に Est-ce que を置くと、語順を大きく変えずに疑問文を作れます。','母音始まりの前では est-ce qu’ になります。','Est-ce que|Est-ce qu’|Pourquoi|Quand|Où', '''
[Est-ce que] tu parles français ? | 君はフランス語を話しますか？
[Est-ce qu’]il vient ce soir ? | 彼は今夜来ますか？
[Est-ce que] vous avez faim ? | あなたはお腹がすいていますか？
[Est-ce qu’]elle travaille demain ? | 彼女は明日働きますか？
[Est-ce que] nous partons bientôt ? | 私たちはもうすぐ出発しますか？
[Est-ce qu’]ils sont prêts ? | 彼らは準備ができていますか？
[Est-ce que] tu aimes le thé ? | 君はお茶が好きですか？
[Est-ce qu’]on peut entrer ? | 入ってもいいですか？
[Est-ce que] vous comprenez ? | あなたは分かりますか？
[Est-ce qu’]il pleut ? | 雨が降っていますか？
''')
add('否定と疑問','疑問詞：いつ・どこ・なぜ','où（どこ）、quand（いつ）、comment（どのように）、pourquoi（なぜ）、combien（いくら・いくつ）などで情報を尋ねます。','疑問詞の後に est-ce que を使う形もあります。','Où|Quand|Comment|Pourquoi|Combien|Qui|Quel', '''
[Où] habites-tu ? | 君はどこに住んでいますか？
[Quand] pars-tu ? | 君はいつ出発しますか？
[Comment] allez-vous ? | お元気ですか？
[Pourquoi] pleures-tu ? | 君はなぜ泣いているの？
[Combien] ça coûte ? | それはいくらですか？
[Qui] est là ? | そこにいるのは誰ですか？
[Quel] âge as-tu ? | 君は何歳ですか？
[Où] est la gare ? | 駅はどこですか？
[Pourquoi] es-tu triste ? | 君はなぜ悲しいの？
[Quand] est ton anniversaire ? | 君の誕生日はいつですか？
''')
add('否定と疑問','倒置疑問文','改まった質問では主語人称代名詞と動詞をハイフンで結ぶ倒置が使われます。','母音の連続を避けるため -t- を挿入する場合があります。','-tu|-vous|-il|-elle|-ils|-elles|-t-il|-t-elle', '''
Parles[-tu] français ? | 君はフランス語を話しますか？
Avez[-vous] le temps ? | あなたには時間がありますか？
Est[-il] ici ? | 彼はここにいますか？
Est[-elle] prête ? | 彼女は準備できていますか？
Sont[-ils] étudiants ? | 彼らは学生ですか？
Viennent[-elles] demain ? | 彼女たちは明日来ますか？
Va[-t-il] au travail ? | 彼は仕事に行きますか？
A[-t-elle] un chat ? | 彼女は猫を飼っていますか？
Comprenez[-vous] ? | あなたは分かりますか？
Viens[-tu] ce soir ? | 君は今夜来ますか？
''')
add('否定と疑問','否定文の冠詞 de','直接目的語の不定冠詞・部分冠詞は、否定文では原則 de / d’ に変わります。','être を使う否定文や定冠詞は通常この規則の対象外です。','de|d’|du|des|de la|un|une', '''
Je n'ai pas [de] voiture. | 私は車を持っていません。
Elle ne boit pas [de] café. | 彼女はコーヒーを飲みません。
Nous n'avons pas [de] livres. | 私たちには本がありません。
Je n'ai pas [d’]argent. | 私にはお金がありません。
Il ne mange pas [de] viande. | 彼は肉を食べません。
Tu n'as pas [d’]amis ici. | 君にはここに友達がいません。
Je ne veux pas [de] soupe. | 私はスープはいりません。
Elle n'achète pas [de] pain. | 彼女はパンを買いません。
Nous ne prenons pas [de] sucre. | 私たちは砂糖を取りません。
Ils n'ont pas [de] chien. | 彼らは犬を飼っていません。
''')
add('前置詞','à と定冠詞の縮約','à + le = au、à + les = aux。à la と à l’ は縮約しません。目的地などを表します。','都市には通常 à、男性形の国名には au、女性形の国名には en を使います。','au|à la|à l’|aux|à|en', '''
Je vais [au] cinéma. | 私は映画館に行きます。
Tu vas [à la] gare. | 君は駅に行きます。
Elle va [à l’]école. | 彼女は学校に行きます。
Nous allons [aux] toilettes. | 私たちはトイレに行きます。
Il habite [à] Paris. | 彼はパリに住んでいます。
Ils vont [en] France. | 彼らはフランスに行きます。
Je parle [au] professeur. | 私は先生と話します。
Nous sommes [à la] maison. | 私たちは家にいます。
Elle va [à l’]hôpital. | 彼女は病院に行きます。
Ils vont [aux] États-Unis. | 彼らはアメリカ合衆国に行きます。
''')
add('前置詞','de と定冠詞の縮約','de + le = du、de + les = des。de la / de l’ はそのままです。起点や所属を表します。','部分冠詞 du と縮約形 du は形が同じですが働きが異なります。','du|de la|de l’|des|de|d’', '''
Je viens [du] Japon. | 私は日本から来ました。
Elle sort [de la] maison. | 彼女は家から出ます。
Il revient [de l’]école. | 彼は学校から帰ってきます。
Nous parlons [des] enfants. | 私たちはその子どもたちについて話しています。
Je viens [de] Paris. | 私はパリから来ました。
Il parle [d’]amour. | 彼は愛について話します。
La porte [du] bureau est ouverte. | オフィスのドアは開いています。
La fenêtre [de la] chambre est fermée. | 寝室の窓は閉まっています。
La couleur [de l’]eau change. | 水の色が変わります。
Le nom [des] rues est important. | それらの通りの名前は重要です。
''')
add('前置詞','場所を表す前置詞','sur（上）、sous（下）、devant（前）、derrière（後ろ）、dans（中）、entre（間）などで位置を示します。','前置詞は日本語の助詞と一対一には対応しません。','sur|sous|devant|derrière|dans|entre|près de|loin de|à côté de|en face de', '''
Le livre est [sur] la table. | 本はテーブルの上にあります。
Le chat est [sous] la chaise. | 猫は椅子の下にいます。
Je suis [devant] la gare. | 私は駅の前にいます。
Le jardin est [derrière] la maison. | 庭は家の裏にあります。
Les clés sont [dans] le sac. | 鍵はかばんの中にあります。
Il est [entre] deux amis. | 彼は二人の友人の間にいます。
L'hôtel est [près de] la gare. | ホテルは駅の近くにあります。
Nous habitons [loin de] Paris. | 私たちはパリから遠い所に住んでいます。
La pharmacie est [à côté de] la banque. | 薬局は銀行の隣にあります。
Le café est [en face de] la mairie. | カフェは市役所の向かいにあります。
''')
add('前置詞','時刻と時間の前置詞','à（時刻）、en（所要時間）、pendant（期間）、depuis（継続起点）、avant / après（前・後）を区別します。','depuis は現在まで継続する事柄と一緒に使うことが多いです。','à|en|pendant|depuis|avant|après|vers|jusqu’à', '''
Je pars [à] huit heures. | 私は8時に出発します。
Il fait cela [en] dix minutes. | 彼はそれを10分でします。
Nous étudions [pendant] deux heures. | 私たちは2時間勉強します。
J'habite ici [depuis] 2020. | 私は2020年からここに住んでいます。
Elle arrive [avant] midi. | 彼女は正午より前に着きます。
Nous sortons [après] le déjeuner. | 私たちは昼食の後に外出します。
Le train part [vers] neuf heures. | 電車は9時ごろに出発します。
Je reste ici [jusqu’à] demain. | 私は明日までここにいます。
Il court [pendant] une heure. | 彼は1時間走ります。
Elle travaille [depuis] ce matin. | 彼女は今朝から働いています。
''')
add('数量・比較','数量表現と de','数量を表す beaucoup de、peu de、un peu de、assez de、trop de などの後に名詞を置きます。','数量詞の後ろは原則として不定冠詞・部分冠詞ではなく de です。','beaucoup de|peu de|un peu de|assez de|trop de|combien de|plus de|moins de', '''
J'ai [beaucoup de] travail. | 私には仕事がたくさんあります。
Il y a [peu de] monde. | 人はほとんどいません。
Je voudrais [un peu de] sucre. | 私は砂糖を少し欲しいです。
Nous avons [assez de] temps. | 私たちには十分な時間があります。
Tu mets [trop de] sel. | 君は塩を入れすぎです。
[Combien de] frères as-tu ? | 君には兄弟が何人いますか？
Elle veut [plus de] café. | 彼女はもっとコーヒーが欲しいです。
Il mange [moins de] pain. | 彼はパンを食べる量が少ないです。
Nous avons [beaucoup de] livres. | 私たちには本がたくさんあります。
Je prends [un peu de] lait. | 私は牛乳を少し入れます。
''')
add('数量・比較','比較級 plus・moins・aussi','形容詞の比較は plus ... que（より〜）、moins ... que（それほど〜でない）、aussi ... que（同じくらい〜）。','形容詞は主語に性数一致させます。','plus|moins|aussi|très', '''
Paul est [plus] grand que Marc. | ポールはマルクより背が高いです。
Cette chambre est [moins] chère que l'autre. | この部屋はもう一方ほど高価ではありません。
Elle est [aussi] rapide que moi. | 彼女は私と同じくらい速いです。
Ce livre est [plus] intéressant que celui-là. | この本はあの本より面白いです。
Il est [moins] fatigué qu'hier. | 彼は昨日ほど疲れていません。
Tu es [aussi] gentil que lui. | 君は彼と同じくらい親切です。
Le train est [plus] rapide que le bus. | 電車はバスより速いです。
Cette robe est [moins] longue que celle-là. | このワンピースはあれほど長くありません。
Nous sommes [aussi] contents que vous. | 私たちはあなたと同じくらいうれしいです。
Paris est [plus] grand que Lyon. | パリはリヨンより大きいです。
''')
add('数量・比較','最上級 le plus・la plus','最上級では定冠詞＋plus / moins＋形容詞を用います。定冠詞と形容詞は名詞の性数に一致します。','比較の対象を述べる際は de を使うことが多いです。','le plus|la plus|les plus|le moins|la moins', '''
C'est [le plus] grand bâtiment. | これは一番大きな建物です。
Elle est [la plus] rapide de la classe. | 彼女はクラスで一番速いです。
Ce sont [les plus] beaux jardins. | これらは最も美しい庭園です。
C'est [le moins] cher des trois. | これは三つの中で一番安いです。
Elle est [la moins] âgée du groupe. | 彼女はグループで一番若いです。
Il est [le plus] grand de la famille. | 彼は家族で一番背が高いです。
C'est [la plus] belle photo. | これは一番美しい写真です。
Ce sont [les plus] petites maisons. | これらは一番小さい家々です。
C'est [le moins] difficile. | これが一番難しくありません。
Elle est [la plus] calme de nous tous. | 彼女は私たちの中で最も落ち着いています。
''')
add('動詞と構文','代名動詞の現在形','se lever や se coucher など、主語に合わせて再帰代名詞 me / te / se / nous / vous / se を使います。','je me lève のように動詞自体も活用し、否定では ne が代名詞の前に来ます。','me|te|se|nous|vous|m’|t’|s’', '''
Je [me] lève tôt. | 私は早起きします。
Tu [te] couches tard. | 君は遅く寝ます。
Il [se] réveille à sept heures. | 彼は7時に目を覚まします。
Nous [nous] promenons dans le parc. | 私たちは公園を散歩します。
Vous [vous] reposez ici. | あなたはここで休みます。
Elles [se] préparent. | 彼女たちは準備をします。
Je [m’]habille rapidement. | 私は素早く服を着ます。
Tu [t’]appelles comment ? | 君の名前は何ですか？
Elle [s’]amuse beaucoup. | 彼女はとても楽しんでいます。
Nous [nous] lavons les mains. | 私たちは手を洗います。
''')
add('動詞と構文','命令形','tu / nous / vous の命令形は通常主語代名詞を省略します。-er 動詞の tu 命令形は通常語尾の -s を取りません。','肯定命令文では目的語代名詞を動詞の後ろに置き、ハイフンでつなぎます。','Parle|Parlons|Parlez|Écoute|Écoutons|Écoutez|Regarde|Regardons|Regardez|Attendez', '''
[Parle] plus lentement ! | もっとゆっくり話して！
[Parlons] français ! | フランス語で話しましょう！
[Parlez] plus fort ! | もっと大きな声で話してください！
[Écoute] bien ! | よく聞いて！
[Écoutons] cette chanson ! | この歌を聴きましょう！
[Écoutez] le professeur ! | 先生の話を聞いてください！
[Regarde] cette photo ! | この写真を見て！
[Regardons] le film ! | 映画を見ましょう！
[Regardez] ici ! | こちらを見てください！
[Attendez] un instant ! | 少し待ってください！
''')
add('未来','近接未来 aller + 不定詞','aller の現在形の後に不定詞を置いて「これから〜する」を表します。','活用するのは aller で、後ろの動詞は不定詞のままです。','vais|vas|va|allons|allez|vont', '''
Je [vais] manger. | 私はこれから食べます。
Tu [vas] dormir. | 君はこれから寝ます。
Elle [va] partir demain. | 彼女は明日出発する予定です。
Nous [allons] visiter Paris. | 私たちはパリを訪れる予定です。
Vous [allez] comprendre. | あなたはじきに分かるでしょう。
Ils [vont] arriver. | 彼らはもうすぐ到着します。
Je [vais] étudier ce soir. | 私は今夜勉強するつもりです。
On [va] prendre le train. | 私たちは電車に乗るつもりです。
Elles [vont] acheter du pain. | 彼女たちはパンを買いに行くつもりです。
Nous [allons] commencer. | 私たちは始めるところです。
''')
add('未来','単純未来の基本','規則的な単純未来は、不定詞の語幹に -ai / -as / -a / -ons / -ez / -ont を付けます。','aller、être、avoir などは未来形の語幹が不規則です。','parlerai|parleras|parlera|parlerons|parlerez|parleront|finirai|finiras|finira|finirons', '''
Je [parlerai] demain. | 私は明日話します。
Tu [parleras] plus tard. | 君は後で話すでしょう。
Elle [parlera] au professeur. | 彼女は先生と話すでしょう。
Nous [parlerons] ce soir. | 私たちは今夜話します。
Vous [parlerez] français. | あなたはフランス語を話すでしょう。
Ils [parleront] ensemble. | 彼らは一緒に話すでしょう。
Je [finirai] demain. | 私は明日終わらせます。
Tu [finiras] bientôt. | 君はもうすぐ終えるでしょう。
Il [finira] ce travail. | 彼はこの仕事を終えるでしょう。
Nous [finirons] à midi. | 私たちは正午に終えるでしょう。
''')
add('未来','不規則な単純未来','être→ser-、avoir→aur-、aller→ir-、faire→fer-、venir→viendr- など不規則な未来の語幹があります。','語尾は規則未来と同じです。','serai|seras|sera|aurai|auras|aura|irai|iras|ira|ferai|viendrai|pourrai', '''
Je [serai] là demain. | 私は明日そこにいます。
Tu [seras] content. | 君はうれしくなるでしょう（男性形）。
Elle [sera] prête. | 彼女は準備ができているでしょう。
J'[aurai] du temps. | 私には時間があるでしょう。
Tu [auras] une réponse. | 君は返事をもらうでしょう。
Il [aura] vingt ans. | 彼は20歳になるでしょう。
J'[irai] en France. | 私はフランスへ行くつもりです。
Tu [iras] à Lyon. | 君はリヨンへ行くでしょう。
Elle [ira] à Paris. | 彼女はパリへ行くでしょう。
Je [ferai] les courses. | 私は買い物をするつもりです。
''')
add('過去','複合過去：avoir を使う形','複合過去は avoir の現在形＋過去分詞で作れます（j’ai parlé など）。','être を使う動詞もあるので助動詞を一律に avoir にしないでください。','ai|as|a|avons|avez|ont', '''
J'[ai] mangé. | 私は食べました。
Tu [as] travaillé hier. | 君は昨日働きました。
Il [a] regardé le film. | 彼はその映画を見ました。
Nous [avons] visité Paris. | 私たちはパリを訪れました。
Vous [avez] terminé. | あなたは終えました。
Ils [ont] pris le train. | 彼らは電車に乗りました。
J'[ai] acheté un livre. | 私は本を買いました。
Elle [a] compris. | 彼女は理解しました。
Elles [ont] parlé longtemps. | 彼女たちは長く話しました。
Nous [avons] fait la cuisine. | 私たちは料理をしました。
''')
add('過去','複合過去：過去分詞の形','規則動詞の過去分詞は -er→-é、finir型 -ir→-i、attendre型 -re→-u です。不規則形も多くあります。','fait、pris、écrit などは不規則な過去分詞です。','parlé|fini|attendu|vendu|choisi|réussi|fait|pris|écrit|lu', '''
J'ai [parlé] avec lui. | 私は彼と話しました。
Elle a [fini] son travail. | 彼女は仕事を終えました。
Nous avons [attendu] le bus. | 私たちはバスを待ちました。
Il a [vendu] sa voiture. | 彼は車を売りました。
Tu as [choisi] ce livre. | 君はこの本を選びました。
Ils ont [réussi] l'examen. | 彼らは試験に合格しました。
J'ai [fait] du sport. | 私は運動しました。
Elle a [pris] le train. | 彼女は電車に乗りました。
Nous avons [écrit] une lettre. | 私たちは手紙を書きました。
Il a [lu] ce roman. | 彼はこの小説を読みました。
''')
add('過去','複合過去：être を使う形','aller、venir、arriver、partir など一部の動詞の複合過去は être＋過去分詞で作ります。','être を使う場合、過去分詞は原則主語と性数一致します。','suis|es|est|sommes|êtes|sont', '''
Je [suis] allé à Paris. | 私はパリへ行きました（男性形）。
Tu [es] venu hier. | 君は昨日来ました（男性形）。
Il [est] parti tôt. | 彼は早く出発しました。
Nous [sommes] arrivés. | 私たちは到着しました（男性または男女混合）。
Vous [êtes] rentrés. | あなたたちは帰宅しました（男性または男女混合）。
Ils [sont] tombés. | 彼らは転びました。
Je [suis] restée ici. | 私はここに残りました（女性話者）。
Elle [est] sortie. | 彼女は外出しました。
Elles [sont] venues ensemble. | 彼女たちは一緒に来ました。
Nous [sommes] partis hier. | 私たちは昨日出発しました（男性または男女混合）。
''')
add('過去','複合過去：性数一致','être を用いる複合過去では、過去分詞は主語と性数一致します。男性単数 -Ø、女性単数 -e、男性複数 -s、女性複数 -es が基本です。','avoir を使う複合過去は原則一致しませんが、先行する直接目的語など例外があります。','allé|allée|allés|allées|arrivé|arrivée|arrivés|arrivées|parti|parties', '''
Il est [allé] à Lyon. | 彼はリヨンへ行きました。
Elle est [allée] à Lyon. | 彼女はリヨンへ行きました。
Ils sont [allés] à Lyon. | 彼らはリヨンへ行きました。
Elles sont [allées] à Lyon. | 彼女たちはリヨンへ行きました。
Il est [arrivé] tôt. | 彼は早く到着しました。
Elle est [arrivée] tôt. | 彼女は早く到着しました。
Ils sont [arrivés] tôt. | 彼らは早く到着しました。
Elles sont [arrivées] tôt. | 彼女たちは早く到着しました。
Il est [parti] ce matin. | 彼は今朝出発しました。
Elles sont [parties] hier. | 彼女たちは昨日出発しました。
''')
add('過去','半過去：規則活用','半過去は現在形の nous の語幹から -ons を外し、-ais / -ais / -ait / -ions / -iez / -aient を付けます。','être だけは ét- という不規則な語幹になります。','parlais|parlait|parlions|parliez|parlaient|finissais|finissait|finissions|finissiez|finissaient', '''
Je [parlais] souvent. | 私はよく話していました。
Tu [parlais] lentement. | 君はゆっくり話していました。
Il [parlait] français. | 彼はフランス語を話していました。
Nous [parlions] ensemble. | 私たちは一緒に話していました。
Vous [parliez] trop vite. | あなたは速すぎる話し方をしていました。
Ils [parlaient] doucement. | 彼らは穏やかに話していました。
Je [finissais] à cinq heures. | 私は5時に終わっていました。
Elle [finissait] tard. | 彼女は遅く終えていました。
Nous [finissions] ensemble. | 私たちは一緒に終えていました。
Elles [finissaient] tôt. | 彼女たちは早く終えていました。
''')
add('過去','半過去：状態・習慣','半過去は過去の状態、背景、繰り返しの習慣などを表します。','一度きりの完了した出来事は複合過去を使うことが多いです。','étais|était|étions|étaient|avais|avait|avions|avaient', '''
J'[étais] étudiant. | 私は学生でした（男性形）。
Tu [étais] jeune. | 君は若かったです。
Il [était] malade. | 彼は具合が悪かったです。
Nous [étions] amis. | 私たちは友達でした。
Ils [étaient] heureux. | 彼らは幸せでした。
J'[avais] un chien. | 私は犬を飼っていました。
Elle [avait] peur. | 彼女は怖がっていました。
Nous [avions] le temps. | 私たちには時間がありました。
Elles [avaient] faim. | 彼女たちはお腹がすいていました。
Il [était] huit heures. | 8時でした。
''')
add('過去','複合過去と半過去の対比','複合過去は出来事や完了、半過去は背景・状態・習慣を表すことが多いです。','同じ日本語の「〜した」でも、文脈により時制が変わります。','dormais|a sonné|pleuvait|sommes sortis|lisais|est entrée|était|a commencé|jouaient|est tombé', '''
Je [dormais] quand il est arrivé. | 彼が到着したとき、私は眠っていました。
Le téléphone [a sonné] pendant le repas. | 食事中に電話が鳴りました。
Il [pleuvait] quand je suis sorti. | 私が外に出たとき雨が降っていました。
Nous [sommes sortis] malgré la pluie. | 私たちは雨にもかかわらず外出しました（男性または男女混合）。
Je [lisais] quand elle a appelé. | 彼女が電話したとき私は読書していました。
Elle [est entrée] pendant le cours. | 彼女は授業中に入ってきました。
La maison [était] calme. | 家は静かでした。
Le film [a commencé] à huit heures. | 映画は8時に始まりました。
Les enfants [jouaient] dehors. | 子どもたちは外で遊んでいました。
Il [est tombé] soudainement. | 彼は突然転びました。
''')
add('人称代名詞','直接目的語代名詞','直接目的語を置き換える me / te / le / la / nous / vous / les は、通常活用動詞の前に置きます。','複合過去で前に置く直接目的語は過去分詞の一致を引き起こす場合があります。','me|te|le|la|nous|vous|les|l’', '''
Il [me] regarde. | 彼は私を見ています。
Je [te] connais. | 私は君を知っています。
Je [le] vois. | 私は彼を見ます。
Je [la] cherche. | 私は彼女を探しています。
Il [nous] attend. | 彼は私たちを待っています。
Je [vous] remercie. | 私はあなたに感謝します。
Je [les] connais. | 私は彼らを知っています。
Elle [l’]aime. | 彼女は彼を愛しています。
Tu [me] comprends ? | 君は私を理解していますか？
Nous [les] regardons. | 私たちは彼らを見ています。
''')
add('人称代名詞','間接目的語代名詞 lui・leur','parler à などの「à＋人」を、単数なら lui、複数なら leur で置き換えます。','lui は男性にも女性にも使え、leur は複数の人を指します。','lui|leur|me|te|nous|vous', '''
Je [lui] parle. | 私は彼に話します。
Elle [lui] téléphone. | 彼女は彼女に電話します。
Nous [leur] écrivons. | 私たちは彼らに手紙を書きます。
Je [te] réponds. | 私は君に返事をします。
Il [me] demande un service. | 彼は私に頼みごとをします。
Elle [nous] parle souvent. | 彼女は私たちによく話しかけます。
Je [vous] téléphone demain. | 私は明日あなたに電話します。
Tu [leur] donnes un livre. | 君は彼らに本を一冊あげます。
Il [lui] offre des fleurs. | 彼は彼女に花を贈ります。
Nous [leur] expliquons la règle. | 私たちは彼らに規則を説明します。
''')
add('人称代名詞','代名詞 y・en','y は主に「à＋物・場所」など、en は主に「de＋物」や数量を置き換えます。','y と en の意味は前置詞や文脈に依存します。','y|en|le|la|lui|leur', '''
J'[y] vais demain. | 私は明日そこへ行きます。
Tu [y] habites ? | 君はそこに住んでいるの？
Il [y] pense souvent. | 彼はそのことをよく考えます。
Nous [y] retournons. | 私たちはそこへ戻ります。
Elle [en] parle. | 彼女はそれについて話します。
J'[en] veux deux. | 私はそれを二つ欲しいです。
Tu [en] as ? | 君はそれを持っていますか？
Il [en] vient. | 彼はそこから来ました。
Nous [en] avons besoin. | 私たちにはそれが必要です。
Elle [y] croit. | 彼女はそれを信じています。
''')
add('複文','関係代名詞 qui・que・où','qui は主語、que は直接目的語、où は場所・時を示す関係節を導きます。','que は母音の前で qu’ になり、qui は通常省略しません。','qui|que|qu’|où', '''
C'est un homme [qui] parle français. | これはフランス語を話す男性です。
Voilà le livre [que] je lis. | あそこに私が読んでいる本があります。
C'est la ville [où] je suis né. | これは私が生まれた町です（男性話者）。
Je connais la fille [qui] chante. | 私は歌っている女の子を知っています。
Voilà le film [qu’]il aime. | これが彼の好きな映画です。
C'est le café [où] nous allons. | これが私たちの行くカフェです。
J'aime les gens [qui] sourient. | 私は笑顔の人々が好きです。
C'est une chanson [que] tu connais. | これは君の知っている歌です。
Voici le jour [où] nous partons. | これが私たちの出発する日です。
Voilà une amie [qui] travaille ici. | あちらにここで働いている女友達がいます。
''')
add('丁寧な表現','条件法現在：丁寧な依頼','je voudrais（〜が欲しい）、je pourrais（〜できるだろう）などの条件法現在は、丁寧な希望や依頼に使えます。','条件法の活用は未来形の語幹＋半過去形の語尾を基本とします。','voudrais|voudrait|voudrions|pourrais|pourriez|pourrait|aimerais|aimeriez', '''
Je [voudrais] un café. | コーヒーを一杯いただきたいです。
Je [voudrais] réserver une table. | 席を予約したいのですが。
Elle [voudrait] partir. | 彼女は出発したいと思っています。
Nous [voudrions] deux billets. | 私たちは切符を二枚いただきたいです。
Je [pourrais] venir demain. | 私は明日なら来られるかもしれません。
Vous [pourriez] répéter ? | もう一度言っていただけますか？
Il [pourrait] aider. | 彼なら手伝えるかもしれません。
J'[aimerais] apprendre le français. | 私はフランス語を学びたいです。
Vous [aimeriez] du thé ? | お茶はいかがですか？
Nous [voudrions] payer. | お会計をお願いしたいです。
''')

# Keep the learner-facing book at exactly 50 sections / 500 examples.
# Five optional/overlapping themes remain in the authoring source for a future edition.
SKIP_TITLES={'-er動詞：aimer・regarder','倒置疑問文','最上級 le plus・la plus','不規則な単純未来','半過去：状態・習慣'}
kept=[];out_ex=[]
for original in LESSONS:
    if original['title'] in SKIP_TITLES:continue
    nid=len(kept)+1
    chapter=dict(original);chapter['id']=nid
    kept.append(chapter)
    for ex in EXAMPLES:
        if ex['lesson']!=original['id']:continue
        row=dict(ex);row['lesson']=nid;row['id']=len(out_ex)+1
        out_ex.append(row)
LESSONS=kept;EXAMPLES=out_ex
assert len(LESSONS)==50,(len(LESSONS),len(EXAMPLES))
assert len(EXAMPLES)==500,(len(LESSONS),len(EXAMPLES))
from collections import Counter
counts=Counter(x['fr'].casefold() for x in EXAMPLES)
assert all(n==1 for n in counts.values()),[(fr,n) for fr,n in counts.items() if n>1]
assert all(x['answer'] in x['options'] and x['blank'].count('___')==1 for x in EXAMPLES)
assert all(len(set(x['options']))==len(x['options']) for x in EXAMPLES)
assert all(len([i for i in EXAMPLES if i['lesson']==l['id']])==10 for l in LESSONS)
OUT=ROOT/'app/src/main/assets'
OUT.mkdir(parents=True,exist_ok=True)
(OUT/'lessons.json').write_text(json.dumps(LESSONS,ensure_ascii=False,indent=1)+'\n',encoding='utf-8')
(OUT/'examples.json').write_text(json.dumps(EXAMPLES,ensure_ascii=False,indent=1)+'\n',encoding='utf-8')
print('Wrote',len(LESSONS),'sections,',len(EXAMPLES),'unique examples')
