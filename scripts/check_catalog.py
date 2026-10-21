#!/usr/bin/env python3
"""Validation of unchanged 500 original exercises; not linguistic proof."""
import json,collections,random
from pathlib import Path
assets=Path(__file__).resolve().parents[1]/'app/src/main/assets'
rows=json.loads((assets/'examples.json').read_text(encoding='utf8'))
lessons=json.loads((assets/'lessons.json').read_text(encoding='utf8'))
notes=json.loads((assets/'annotations.json').read_text(encoding='utf8'))
assert len(rows)==500 and len(lessons)==50 and len(notes)==500
assert [r['id'] for r in rows]==list(range(1,501))
assert [r['id'] for r in notes]==list(range(1,501))
assert all(r['answer'] in r['options'] and len(set(r['options']))>=4 for r in rows)
assert all(r['blank'].count('___')==1 and r['blank'].replace('___',r['answer'])==r['fr'] for r in rows)
assert all(0<len(n['brief'])<=320 and n['ipa'].startswith('/') and n['ipa'].endswith('/') for n in notes)
assert len(set((r['fr'] for r in rows)))==500
print('PASS: 500 existing exercises, 50 categories, 500 IPA and quiz invariants')
