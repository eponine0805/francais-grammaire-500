#!/usr/bin/env python3
"""Prepare 500 concise notes and IPA estimates. eSpeak is NOT a certified phonetic transcription."""
import json, subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[1]
folder=root/'app/src/main/assets'
examples=json.loads((folder/'examples.json').read_text(encoding='utf8'))
lessons=json.loads((folder/'lessons.json').read_text(encoding='utf8'))
assert len(examples)==500 and len(lessons)==50
notes=[]
for row in examples:
    lesson=lessons[row['lesson']-1]
    # The explanation MUST come from this exact example, not the shared section rule.
    # Section rules and caveats have their own screen.
    explanation=f"「{row['answer']}」：{row['reason']}"
    if not 1 <= len(explanation) <= 320:
        raise ValueError(f"Example #{row['id']} explanation length: {len(explanation)}")
    proc=subprocess.run(['espeak-ng','-q','-v','fr-fr','--ipa=3',row['fr']],
        capture_output=True,text=True,check=True)
    ipa=' '.join(proc.stdout.replace('ˈ','').replace('ˌ','').replace('-','').split())
    if not ipa or len(ipa)>150:raise RuntimeError(f"Invalid IPA for #{row['id']}")
    notes.append({'id':row['id'],'ipa':'/'+ipa+'/','brief':explanation})
assert [x['id'] for x in notes]==list(range(1,501))
assert all(len(x['brief'])<=320 for x in notes)
(folder/'annotations.json').write_text(json.dumps(notes,ensure_ascii=False,indent=1)+'\n',encoding='utf8')
print('Generated 500 concise notes and 500 unverified IPA estimates')
