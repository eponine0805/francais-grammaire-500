#!/usr/bin/env python3
"""Generate a draft IPA for original course sentences; must NOT claim expert verification."""
from pathlib import Path
import subprocess,json
root=Path(__file__).resolve().parents[1]/'app/src/main/assets'
course=json.loads((root/'course.json').read_text(encoding='utf8'))
result=[]
for unit in course['units']:
    proc=subprocess.run(['espeak-ng','-q','-v','fr-fr','--ipa=3',unit['fr']],capture_output=True,text=True,check=True)
    ipa=' '.join(proc.stdout.replace('ˈ','').replace('ˌ','').replace('-','').split())
    fixes={'syi':'sɥi','plyi':'plɥi','kyizin':'kɥizin','lyi':'lɥi','yit':'ɥit','oʒuʁdyi':'oʒuʁdɥi'}
    for a,b in fixes.items():ipa=ipa.replace(a,b)
    if not ipa or len(ipa)>180:raise RuntimeError(f'Failed IPA for course #{unit["id"]}')
    result.append({'id':unit['id'],'ipa':'/'+ipa+'/'})
assert len(result)==len(course['units'])
(root/'course_ipa.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('Estimated IPA for',len(result),'sentences; human accuracy audit NOT complete')
