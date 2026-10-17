#!/usr/bin/env python3
"""Guard against recurrent IPA transcription errors. Not full native-speaker review."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
items=json.loads((root/'app/src/main/assets/examples.json').read_text(encoding='utf8'))
notes=json.loads((root/'app/src/main/assets/annotations.json').read_text(encoding='utf8'))
assert len(items)==len(notes)==500
pairs=dict(zip((x['id'] for x in items),(x['ipa'] for x in notes)))
assert all(x['id']==y['id'] for x,y in zip(items,notes))
assert all(x['ipa'].startswith('/') and x['ipa'].endswith('/') for x in notes)
assert all(not any(t in x['ipa'] for t in ('(',')','\u200d')) for x in notes)
assert all('yi' not in x['ipa'] for x in notes), 'French /ɥi/ may be mistyped /yi/'
expected={
    321: '/ʒə paʁ a ɥit œʁ/',
    337: '/ɛl vø plys də kafe/',
    345: '/il ɛ mwɛ̃ fatiɡe kjɛʁ/',
    470: '/kɑ̃t il etɛ pəti il ʒwɛ o futbol/',
}
for id,ipa in expected.items():assert pairs[id]==ipa,(id,pairs[id])
for word in ('suis','lui','cuisine','aujourd\'hui','pluie','huit'):
    candidates=[a['ipa'] for x,a in zip(items,notes) if word.casefold() in x['fr'].casefold()]
    if word=='suis':assert all('sɥi' in p for p in candidates), (word,candidates)
    elif word=='lui':assert all('lɥi' in p for p in candidates), (word,candidates)
    elif word=='cuisine':assert all('kɥizin' in p for p in candidates), (word,candidates)
    elif word=="aujourd'hui":assert all('oʒuʁdɥi' in p for p in candidates), (word,candidates)
    elif word=='pluie':assert all('plɥi' in p for p in candidates), (word,candidates)
    elif word=='huit':assert all('ɥit' in p for p in candidates), (word,candidates)
print('IPA high-confidence regression checks passed. This is not certification of all 500 readings.')
