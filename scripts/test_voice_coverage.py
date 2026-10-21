#!/usr/bin/env python3
"""Validate every displayed example and exam answer resolves to one voice and IPA."""
from pathlib import Path
import re,json
P=Path(__file__).resolve().parents[1]/'app/src/main/assets'
data=json.loads((P/'intensive.json').read_text(encoding='utf8'))
extras=json.loads((P/'extra_audio_manifest.json').read_text(encoding='utf8'))
assert len(data['units'])==59 and len(extras)==114
for unit in data['units']:
    assert len(unit['examples'])==6 and len(unit['tasks'])==20
    for e in unit['examples']:
        assert e['ipa'].startswith('/') and e['ipa'].endswith('/')
        assert re.fullmatch(r'(?:\d{4}|extra_\d{4}_\d{2})\.ogg',e['audioFile'])
        if e['audioFile'].startswith('extra_'):
            assert e['audioFile'] in extras and extras[e['audioFile']]['fr']==e['fr']
    for task in unit['tasks']:
        assert task['spokenFr'] and task['ipa'].startswith('/') and task['ipa'].endswith('/')
        expected=(f"course_{unit['id']:04d}.ogg" if task['example']<0 else unit['examples'][task['example']]['audioFile'])
        assert task['audioFile']==expected
        if task['example']>=0:assert task['spokenFr']==unit['examples'][task['example']]['fr']
print('PASS: 354 worked examples + 1180 answer results have correct IPA/audio references; 114 missing voices mapped')
