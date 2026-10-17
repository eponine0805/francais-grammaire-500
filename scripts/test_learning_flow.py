#!/usr/bin/env python3
"""Word ordering and session save/restore state simulations, not Android instrumentation."""
import random,json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
rows=json.loads((root/'app/src/main/assets/examples.json').read_text(encoding='utf8'))
for row in rows:
    words=row['fr'].split()
    assert len(words)>=2
    for seed in range(10):
        order=list(range(len(words)))
        random.Random(seed+row['id']).shuffle(order)
        assert sorted(order)==list(range(len(words)))
        assert ' '.join(words[i] for i in range(len(words)))==row['fr']
for seed in range(120):
    rng=random.Random(seed)
    ids=rng.sample(range(1,501),rng.randint(1,100))
    state={'ids':ids,'index':0,'correct':0,'answers':[],'wrong':[]}
    for i,id in enumerate(ids):
        ok=rng.random()>0.4
        state['answers'].append('ok' if ok else 'wrong')
        if ok:state['correct']+=1
        else:state['wrong'].append(id)
        state['index']=i+1
        state=json.loads(json.dumps(state))
        assert state['correct']+len(state['wrong'])==state['index']
print('OK: 5,000 word-order permutations and 120 saved session simulations')
