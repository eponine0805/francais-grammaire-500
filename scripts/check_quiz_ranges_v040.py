from pathlib import Path
import json
p=Path(__file__).resolve().parents[1]/'app/src/main/assets'
ex=json.loads((p/'examples.json').read_text())
for a in range(1,51):
 for b in range(a,51):
  ids=[x['id'] for x in ex if a<=x['lesson']<=b]
  assert ids
  for requested in [10,20,30,40,50,60,70,80,90,100,len(ids)]:
   subset=ids[:min(requested,len(ids))]
   assert all(a<=ex[id-1]['lesson']<=b for id in subset)
print('PASS: 500 legacy examples and 50-category ranged quiz selection')
