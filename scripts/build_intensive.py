#!/usr/bin/env python3
"""Build a coherent textbook-first 59 x 20 grammar program.
Sources: supplied 2022 introductory text chapter sequence; original teacher-authored
59-unit commentary; vetted app examples. Does not certify French language quality.
"""
import json, random, re, collections
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'
course=json.loads((ASSETS/'course.json').read_text(encoding='utf-8'))
examples=json.loads((ASSETS/'examples.json').read_text(encoding='utf-8'))
bylesson=collections.defaultdict(list)
for row in examples:bylesson[row['lesson']].append(row)
lect={}
for line in (ROOT/'scripts/lecture_notes.txt').read_text(encoding='utf-8').splitlines():
    if not line.strip():continue
    id,lead,method,pitfall=line.split('|')
    lect[int(id)]=[{'heading':'なぜ・どんな場面で使う？','text':lead}, {'heading':'作り方と使い分け','text':method}, {'heading':'間違えやすいポイント','text':pitfall}]
assert len(lect)==59
special=collections.defaultdict(list)
for line in (ROOT/'scripts/custom_drills.txt').read_text(encoding='utf-8').splitlines():
    if not line.strip():continue
    fields=line.split('|')
    if len(fields)==5:
        id,fr,ja,blank,op=fields
        answer=op.split(',')[0]
    else:id,fr,ja,blank,answer,op=fields
    opts=op.split(',')
    assert len(set(opts))==4 and answer in opts,(id,fr,opts,answer)
    assert blank.replace('___',answer)==fr,(id,fr,blank,answer)
    special[int(id)].append({'fr':fr,'ja':ja,'blank':blank,'answer':answer,'options':opts,
        'why':f'{answer} が空欄に入る。{lect[int(id)][1]["text"]}','origin':'追加例文'})
legacy={
 4:[8],5:[9],6:[5],7:[7,6],8:[10],9:[11],10:[10],11:[1,2],12:[3],13:[30,31],14:[4],
 15:[27],16:[25],17:[15],18:[15],19:[25],20:[27],21:[13],22:[16],23:[17],24:[18,20],
 25:[38],26:[46],27:[47],28:[46,47],29:[22,23,24],30:[40],31:[41],32:[19,21],33:[40],
 34:[36],36:[12],42:[44],43:[45],45:[35],48:[39],49:[26],50:[37],52:[28],53:[48],54:[50]
}
units=[]
for u in course['units']:
    uid=u['id']
    candidates=[]
    if uid in special:candidates=special[uid]
    else:
        for lid in legacy.get(uid,[]):candidates += bylesson[lid]
        # Non-overlapping coverage and representational spread across multiple conjugations.
        candidates=sorted(candidates,key=lambda e:e['id'])
        if len(candidates)<6:raise ValueError(f'Insufficient unit-specific examples for #{uid} {u["title"]}: {len(candidates)}')
        if len(legacy.get(uid,[]))>1:
            group=[list(bylesson[x]) for x in legacy[uid]]
            picked=[]
            while len(picked)<6:
                for a in group:
                    if a: picked.append(a.pop(0))
                    if len(picked)==6:break
            # restore original lists because group refers same shared lists
            candidates=picked
        else:
            pick=[0,1,2,3,4,len(candidates)-1]
            candidates=[candidates[n] for n in pick]
    assert len(candidates)>=6,(uid,len(candidates))
    candidates=candidates[:6]
    items=[]
    for i,row in enumerate(candidates):
        origin=row.get('origin','従来の関連例文')
        opts=row['options']
        distractors=[x for x in opts if x!=row['answer']]
        if len(distractors)<3:raise ValueError(uid)
        chosen=[row['answer']]+distractors[:3]
        rand=random.Random(uid*100+i)
        rand.shuffle(chosen)
        items.append({'fr':row['fr'],'ja':row['ja'],'blank':row['blank'],'answer':row['answer'],
            'options':chosen,'why':row.get('why',row.get('reason',u['rule'])),'origin':origin,
            'audio':row['id'] if 'id' in row and 'origin' not in row else 0})
    assert len(set(x['fr'] for x in items))==6,(uid,[x['fr'] for x in items])
    # 2 concept checks + six different sentences each practiced in three directions.
    tasks=[{'kind':'choice','prompt':u['question'],'fr':'','ja':'','answer':u['answer'],'options':u['options'],'why':u['why'],'example':-1},
           {'kind':'order','prompt':'次の日本語をフランス語の語順にしてください。','fr':u['fr'],'ja':u['ja'],'answer':u['fr'],'why':u['focus'],'example':-1}]
    for i,e in enumerate(items):
        tasks.append({'kind':'choice','prompt':e['blank'],'fr':e['fr'],'ja':e['ja'],
                      'answer':e['answer'],'options':e['options'],'why':e['why'],'example':i})
    for i,e in enumerate(items):
        tasks.append({'kind':'order','prompt':'語順を整えて、次の意味の文を作ってください。','fr':e['fr'],'ja':e['ja'],
                      'answer':e['fr'],'why':e['why'],'example':i})
    for i,e in enumerate(items):
        meanings=[(j,v['ja']) for j,v in enumerate(items) if j!=i and v['ja']!=e['ja']]
        meanings+= [(-1,u['ja'])] if u['ja']!=e['ja'] else []
        opts=[e['ja']]
        for _,ja in meanings:
            if ja not in opts:opts.append(ja)
            if len(opts)==4:break
        if len(opts)<4:raise ValueError((uid,i,'insufficient meanings'))
        random.Random(56700+uid*11+i).shuffle(opts)
        tasks.append({'kind':'meaning','prompt':'次のフランス語の文の意味を選んでください。','fr':e['fr'],'ja':'',
                      'answer':e['ja'],'options':opts,'why':f'「{e["fr"]}」＝「{e["ja"]}」。{u["focus"]}','example':i})
    assert len(tasks)==20
    units.append({'id':uid,'chapter':u['chapter'],'title':u['title'],
       'sourcePage':course['chapters'][u['chapter']]['sourcePage'],'intro':u['rule'],
       'sections':lect[uid], 'comparison':u['compare'], 'summary':u['focus'],
       'core':{'fr':u['fr'],'ja':u['ja'],'why':u['focus']},'examples':items,
       'tasks':tasks})
# metadata shows transparent character of exercise variants
result={'edition':'0.6.0','structure':'59 lesson readings + 7 worked example sentences each + 20 assessment tasks each',
'source':course['source'],'disclosure':'教材解説は独自執筆。20問は規則1問・例文の語順1問・関連6例文の空欄/語順/意味を反復する計20題。原文・選択肢の自動整合検査と専門家による言語監修は別。',
'units':units}
assert len(units)==59 and sum(len(u['tasks']) for u in units)==1180
for u in units:
    for t in u['tasks']:
        if t['kind']=='choice': assert t['options'].count(t['answer'])==1,(u['id'],t)
        elif t['kind']=='meaning':assert t['options'].count(t['answer'])==1,(u['id'],t)
        else:assert t['fr'] and t['answer']==t['fr']
    for e in u['examples']:assert e['blank'].replace('___',e['answer'])==e['fr']
(ASSETS/'intensive.json').write_text(json.dumps(result,ensure_ascii=False,indent=1)+'\n',encoding='utf-8')
print('Wrote:',len(units),'units,',sum(len(u['tasks']) for u in units),'tasks; size', (ASSETS/'intensive.json').stat().st_size)
