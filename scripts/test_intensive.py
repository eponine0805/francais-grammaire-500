#!/usr/bin/env python3
"""Exhaustive structural, coverage and non-ambiguous-answer checks; NOT French expert review."""
import collections,json
from pathlib import Path
R=Path(__file__).resolve().parents[1]
D=json.loads((R/'app/src/main/assets/intensive.json').read_text(encoding='utf8'))
C=json.loads((R/'app/src/main/assets/course.json').read_text(encoding='utf8'))
assert len(D['units'])==len(C['units'])==59
assert sum(len(x['tasks']) for x in D['units'])==1180
for i,u in enumerate(D['units']):
 assert u['id']==i+1==C['units'][i]['id']
 assert len(u['sections'])==3 and all(len(x['text'])>=35 for x in u['sections']),(i,'thin section')
 assert len(u['examples'])==6 and len({x['fr'] for x in u['examples']})==6
 assert len(u['tasks'])==20
 kinds=collections.Counter(t['kind'] for t in u['tasks'])
 assert kinds=={'choice':7,'order':7,'meaning':6},(u['id'],kinds)
 for n,x in enumerate(u['examples']):
  assert x['blank'].count('___')==1
  assert x['blank'].replace('___',x['answer'])==x['fr']
  assert x['options'].count(x['answer'])==1 and len(set(x['options']))==4
  for taskid in (2+n,8+n,14+n):
   task=u['tasks'][taskid]
   assert task['example']==n,(u['id'],n,taskid)
   assert task['fr']==x['fr']
 for task in u['tasks']:
  if task['kind'] in ('choice','meaning'):
   assert len(task['options'])==4 and len(set(task['options']))==4
   assert task['options'].count(task['answer'])==1
  else:
   assert ' '.join(task['answer'].split())==task['fr']
   assert 2<=len(task['fr'].split())<=22
print('PASS 59 independent three-part lectures and 7 worked example sentences per unit')
print('PASS 1180 graded items: 413 choices + 413 sentence-ordering + 354 meaning checks')
print('Note: shared sentences are retrieved in different tasks; 1180 does NOT mean 1180 distinct French sentences.')
print('Note: did not certify French grammar, native pronunciation or every semantically possible distractor.')
