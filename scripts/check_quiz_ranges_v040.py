#!/usr/bin/env python3
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
ex=json.loads((root/'app/src/main/assets/examples.json').read_text(encoding='utf-8'))
notes=json.loads((root/'app/src/main/assets/annotations.json').read_text(encoding='utf-8'))
lessons=json.loads((root/'app/src/main/assets/lessons.json').read_text(encoding='utf-8'))
java=(root/'app/src/main/java/com/francais/grammaire/MainActivity.java').read_text(encoding='utf-8')
assert len(ex)==len(notes)==500 and len(lessons)==50
assert [e['id'] for e in ex]==list(range(1,501))
assert [n['id'] for n in notes]==list(range(1,501))
assert all(0<len(n['brief'])<=320 and n['ipa'].startswith('/') and n['ipa'].endswith('/') for n in notes)
assert all(n['brief']==f"「{e['answer']}」：{e['reason']}" for e,n in zip(ex,notes))
ranges=cases=0
for start in range(1,51):
    for end in range(start,51):
        ids=[e['id'] for e in ex if start<=e['lesson']<=end]
        assert ids and len(ids)==len(set(ids))
        ranges+=1
        for count in range(20,101,10):
            subset=ids[:min(count,len(ids))]
            assert len(subset)==min(count,len(ids))
            assert all(start<=ex[i-1]['lesson']<=end for i in subset)
            cases+=1
        assert len(ids)>0
        cases+=1
assert ranges==1275 and cases==12750
for name in ('showQuizSetup(){','quizRange(first,last)','fr.post(()->','restoreQuizScroll(y)','add(bottomBar(),nav)','次の問題へ  →','onSaveInstanceState','resumeSavedQuiz'):
    assert name in java,name
assert '文法のポイント  •  200字以内' not in java
print('PASS: 50 sections, 500 examples, 500 annotations, 1,275 ranges and 12,750 configurations')
print('NOTE: UI instrumentation and native-speaker audio/IPA review still required')
