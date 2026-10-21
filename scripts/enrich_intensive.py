#!/usr/bin/env python3
"""Assign full-sentence IPA and audio paths to every worked example and answer.
IPA is automatically estimated and is not a French specialist's certification.
"""
from pathlib import Path
import json,subprocess,shutil
BASE=Path(__file__).resolve().parents[1]/'app/src/main/assets'
data=json.loads((BASE/'intensive.json').read_text(encoding='utf8'))
legacy={x['id']:x['ipa'] for x in json.loads((BASE/'annotations.json').read_text(encoding='utf8'))}
course={x['id']:x['ipa'] for x in json.loads((BASE/'course_ipa.json').read_text(encoding='utf8'))}
core={x['id']:x['fr'] for x in json.loads((BASE/'course.json').read_text(encoding='utf8'))['units']}
engine=shutil.which('espeak-ng') or shutil.which('espeak')
assert engine,"IPA requires espeak-ng"
extra={}
def transcribe(fr):
    result=subprocess.run([engine,'-q','-v','fr-fr','--ipa=3',fr],check=True,text=True,capture_output=True)
    ipa=' '.join(result.stdout.replace('ˈ','').replace('ˌ','').replace('-','').split())
    for bad,good in {'syi':'sɥi','lyi':'lɥi','plyi':'plɥi','kyizin':'kɥizin','yit':'ɥit','oʒuʁdyi':'oʒuʁdɥi'}.items():ipa=ipa.replace(bad,good)
    assert ipa and len(ipa)<140,(fr,ipa)
    return '/'+ipa+'/'
for unit in data['units']:
    uid=unit['id']
    for i,e in enumerate(unit['examples']):
        old=e['audio']
        if old:
            e['ipa']=legacy[old]
            e['audioFile']=f'{old:04d}.ogg'
        else:
            file=f'extra_{uid:04d}_{i:02d}.ogg'
            e['ipa']=transcribe(e['fr'])
            e['audioFile']=file
            extra[file]={'unit':uid,'index':i,'fr':e['fr'],'ipa':e['ipa']}
    for task in unit['tasks']:
        k=task['example']
        if k<0:
            task['spokenFr']=core[uid]
            task['ipa']=course[uid]
            task['audioFile']=f'course_{uid:04d}.ogg'
        else:
            e=unit['examples'][k]
            assert task['fr']==e['fr']
            task['spokenFr']=e['fr']
            task['ipa']=e['ipa']
            task['audioFile']=e['audioFile']
assert len(extra)==114,len(extra)
(BASE/'intensive.json').write_text(json.dumps(data,ensure_ascii=False,indent=1)+'\n',encoding='utf8')
(BASE/'extra_audio_manifest.json').write_text(json.dumps(extra,ensure_ascii=False,indent=1)+'\n',encoding='utf8')
print('IPA/audio mapping complete: 354 worked examples, 1180 answers, 114 missing recordings')
