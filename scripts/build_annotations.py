#!/usr/bin/env python3
"""Prepare 500 concise notes and IPA estimates. eSpeak is NOT a certified phonetic transcription."""
import json, subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[1]
folder=root/'app/src/main/assets'
examples=json.loads((folder/'examples.json').read_text(encoding='utf8'))
lessons=json.loads((folder/'lessons.json').read_text(encoding='utf8'))
assert len(examples)==500 and len(lessons)==50
# Dictionary-backed fixes for systematic eSpeak French glide errors.
# Each mapping is applied to IPA text only; source examples and audio are unchanged.
IPA_GLIDE_FIXES = {
    "syi": "sɥi",          # suis
    "oʒuʁdyi": "oʒuʁdɥi", # aujourd'hui
    "lyil": "lɥil",        # l'huile
    "kɔ̃dyiʁ": "kɔ̃dɥiʁ", # conduire
    "dəpyi": "dəpɥi",     # depuis
    "kyizin": "kɥizin",   # cuisine
    "lyi": "lɥi",         # lui, celui
    "plyi": "plɥi",       # pluie
    "yit": "ɥit",         # huit
}
# Verified contextual readings; these cannot safely be inferred from spelling alone.
IPA_CONTEXT_FIXES = {
    321: "/ʒə paʁ a ɥit œʁ/",        # no liaison after pars à
    337: "/ɛl vø plys də kafe/",     # positive quantity plus de
    345: "/il ɛ mwɛ̃ fatiɡe kjɛʁ/",  # qu'hier; no English language fallback
    470: "/kɑ̃t il etɛ pəti il ʒwɛ o futbol/", # French football, not English
}
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
    for wrong,correct in IPA_GLIDE_FIXES.items():
        ipa=ipa.replace(wrong,correct)
    ipa=IPA_CONTEXT_FIXES.get(row['id'],'/'+ipa+'/')
    if not ipa or len(ipa)>150 or any(c in ipa for c in ('(',')','\u200d')):
        raise RuntimeError(f"Invalid or mixed-language IPA for #{row['id']}: {ipa}")
    notes.append({'id':row['id'],'ipa':ipa,'brief':explanation})
assert [x['id'] for x in notes]==list(range(1,501))
assert all(len(x['brief'])<=320 for x in notes)
(folder/'annotations.json').write_text(json.dumps(notes,ensure_ascii=False,indent=1)+'\n',encoding='utf8')
print('Generated 500 concise notes and 500 unverified IPA estimates')
