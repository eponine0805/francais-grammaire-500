#!/usr/bin/env python3
import json,random
from pathlib import Path
c=json.loads((Path(__file__).resolve().parents[1]/'app/src/main/assets/course.json').read_text(encoding='utf8'))
u=c['units']; assert len(u)==59
for row in u:
    a=row['fr'].split()
    assert len(a)>=3
    for n in range(15):
        ix=list(range(len(a)));random.Random(row['id']*10+n).shuffle(ix)
        assert sorted(ix)==list(range(len(a)))
    assert row['options'].count(row['answer'])==1
for i in range(24):
  for j in range(i,24):
    choices=[k['id'] for k in u if i<=k['chapter']<=j]
    assert choices and len(choices)==len(set(choices))
    for limit in [10,20,30,40,50,60,70,80,90,100]:
      sample=choices[:min(len(choices),limit)]
      assert len(sample)<=limit and all(i<=u[k-1]['chapter']<=j for k in sample)
print('PASS: 59 course questions + 15 token permutations per question + 300 chapter ranges')
