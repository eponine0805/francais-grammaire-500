#!/usr/bin/env python3
"""Produce an entirely static Netlify/PWA site using the same data and audio as the Android APK.

Use --release to fetch a prerecorded APK from GitHub. `--apk` uses
an existing local APK (used by Actions to build identical web/APK packages).
`--no-audio` is for local UI testing only: never distribute it as offline-ready.
"""
import argparse
import json
import shutil
import urllib.request
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
p=argparse.ArgumentParser()
p.add_argument('--apk',type=Path)
p.add_argument('--release')
p.add_argument('--no-audio',action='store_true')
p.add_argument('--out',type=Path,default=ROOT/'dist')
a=p.parse_args()
if not(a.apk or a.release or a.no_audio):p.error('Specify --apk or --release for a complete Netlify build')
if a.out.exists():shutil.rmtree(a.out)
shutil.copytree(ROOT/'web',a.out)
(a.out/'data').mkdir(exist_ok=True)
(a.out/'audio').mkdir(exist_ok=True)
assets=ROOT/'app/src/main/assets'
for name in ('course.json','course_ipa.json','lessons.json','examples.json','annotations.json','intensive.json','extra_audio_manifest.json'):
    shutil.copyfile(assets/name,a.out/'data'/name)

apk=a.apk
if a.release:
    if apk: p.error('Choose --apk OR --release')
    import tempfile
    tmp=Path(tempfile.gettempdir())/f'Grammaire500-{a.release}.apk'
    url=f'https://github.com/eponine0805/francais-grammaire-500/releases/download/{a.release}/Grammaire500.apk'
    req=urllib.request.Request(url,headers={'User-Agent':'FrancaisGrammaire-Netlify-build/1.0'})
    with urllib.request.urlopen(req,timeout=100) as r, tmp.open('wb') as f:
        shutil.copyfileobj(r,f)
    apk=tmp
if apk:
    assert apk.exists(),f'APK not found: {apk}'
    with zipfile.ZipFile(apk) as z:
        for name in ('examples.json','course.json'):
            actual=z.read('assets/'+name)
            expected=(assets/name).read_bytes()
            assert actual==expected,f'{name} is not same as APK: refusing mismatched audio'
        files=[x for x in z.namelist() if x.startswith('assets/audio/') and x.endswith('.ogg')]
        words=[x for x in files if x.split('/')[-1][:4].isdigit()]
        course=[x for x in files if x.split('/')[-1].startswith('course_')]
        extras=[x for x in files if x.split('/')[-1].startswith('extra_')]
        assert len(words)==500 and len(course)==59 and len(extras)==114,(len(words),len(course),len(extras))
        for f in files:
            target=a.out/'audio'/Path(f).name
            target.write_bytes(z.read(f))
            assert target.stat().st_size>1800
    print(f'Netlify site includes {len(files)} audio files from matching APK: {apk}')
else:print('LOCAL DEV ONLY: no audio files. Do not claim offline TTS works.')

c=json.loads((a.out/'data/course.json').read_text(encoding='utf-8'))
assert len(c['chapters'])==24 and len(c['units'])==59
assert len(json.loads((a.out/'data/intensive.json').read_text(encoding='utf8'))['units'])==59
assert len(json.loads((a.out/'data/extra_audio_manifest.json').read_text(encoding='utf8')))==114
legacy=json.loads((a.out/'data/examples.json').read_text(encoding='utf-8'))
assert len(legacy)==500
print('Web build:',a.out,'with',len(c['units']),'grammar units and',len(legacy),'examples')
