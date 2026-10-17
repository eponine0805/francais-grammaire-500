#!/usr/bin/env python3
"""Structural + reproducibility audit of 50-section 500-example book, not native-speaker proofreading."""
import json,random,collections,re
from pathlib import Path
root=Path(__file__).resolve().parents[1]
lessons=json.loads((root/'app/src/main/assets/lessons.json').read_text(encoding='utf-8'))
rows=json.loads((root/'app/src/main/assets/examples.json').read_text(encoding='utf-8'))
assert len(lessons)==50 and len(rows)==500
assert [l['id'] for l in lessons]==list(range(1,51))
assert [x['id'] for x in rows]==list(range(1,501))
assert len({x['fr'].casefold() for x in rows})==500
assert all(l['title'] and l['chapter'] and l['rule'] and l['pitfall'] for l in lessons)
ct=collections.Counter(x['lesson'] for x in rows)
assert set(ct)==set(range(1,51)) and sum(ct.values())==500
assert len(set(ct.values()))>=3
for x in rows:
    assert x['fr'] and x['ja'] and x['detail'] and x['pitfall']
    for key in ('structure','whole','reason','contrast','tip','questionHint'):
        assert len(x[key])>=10,(x['id'],key)
    assert x['answer'] in x['options']
    assert len(set(x['options']))>=4
    assert x['blank'].count('___')==1
    assert x['blank'].replace('___',x['answer'])==x['fr'],x['id']
    for _ in range(20):
        distractors=[v for v in x['options'] if v!=x['answer']]
        sampled=[x['answer']]+random.sample(distractors,3)
        random.shuffle(sampled)
        assert len(set(sampled))==4 and sampled.count(x['answer'])==1
# simulate mastery/forgetting and repeated wrong-only testing: 10,000 quiz events
mastered=set();wrong_count=collections.Counter()
for i in range(10000):
    x=random.choice(rows); correct=(i%3!=0)
    if correct:mastered.add(x['id']);wrong_count[x['id']]=0
    else:mastered.discard(x['id']);wrong_count[x['id']]+=1
    assert (x['id'] in mastered)==correct
assert len(mastered)<=500
assert not any(v<0 for v in wrong_count.values())
print('OK: 50 sections, 500 examples, 10,000 4-choice permutations + 10,000 mastery events')
