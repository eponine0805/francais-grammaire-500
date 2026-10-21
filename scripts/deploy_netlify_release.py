#!/usr/bin/env python3
"""Deploy the verified web ZIP built together with the Android release.

Netlify clones only Git-tracked files. The 59-unit intensive.json file is
produced by GitHub Actions, so Netlify must use the published web artifact,
not assume that generated file was checked in.
"""
import argparse
import json
import re
import shutil
import tempfile
import urllib.request
import zipfile
from pathlib import Path, PurePosixPath

ROOT = Path(__file__).resolve().parents[1]
REPOSITORY = 'eponine0805/francais-grammaire-500'


def verify(archive: zipfile.ZipFile):
    members = archive.infolist()
    if len(members) > 700:
        raise RuntimeError('Unexpected release ZIP structure')
    total_size = sum(m.file_size for m in members)
    if total_size > 60 * 1024 * 1024:
        raise RuntimeError('Web ZIP unexpectedly large')
    seen = set()
    for member in members:
        name = member.filename
        path = PurePosixPath(name)
        if path.is_absolute() or '..' in path.parts or '\\' in name:
            raise RuntimeError(f'Unsafe archive path: {name}')
        if name in seen:
            raise RuntimeError(f'Duplicate archive entry: {name}')
        seen.add(name)
    required = ['index.html','app.js','style.css','sw.js',
                'manifest.webmanifest','data/course.json','data/intensive.json',
                'data/examples.json','data/annotations.json','data/course_ipa.json',
                'data/lessons.json']
    for name in required:
        if name not in seen:
            raise RuntimeError(f'Missing required web resource: {name}')
    # Verify release curriculum against the checked-out Git branch.
    for name in ('course.json','examples.json'):
        tracked = (ROOT/'app/src/main/assets'/name).read_bytes()
        if archive.read('data/'+name) != tracked:
            raise RuntimeError(f'Release is incompatible with current {name}. Update the release tag.')
    course = json.loads(archive.read('data/course.json'))
    intensive = json.loads(archive.read('data/intensive.json'))
    examples = json.loads(archive.read('data/examples.json'))
    if not (len(course['chapters'])==24 and len(course['units'])==59 and
            len(intensive['units'])==59 and len(examples)==500 and
            sum(len(u['tasks']) for u in intensive['units'])==1180):
        raise RuntimeError('Release does not contain the expected grammar curriculum')
    audio = [p for p in seen if p.startswith('audio/') and p.endswith('.ogg')]
    word_audio = [p for p in audio if re.fullmatch(r'audio/\d{4}\.ogg',p)]
    lesson_audio = [p for p in audio if re.fullmatch(r'audio/course_\d{4}\.ogg',p)]
    if len(word_audio)!=500 or len(lesson_audio)!=59 or len(audio)!=559:
        raise RuntimeError(f'Audio mismatch: {len(word_audio)} + {len(lesson_audio)} != 559')
    if any(archive.getinfo(name).file_size < 1800 for name in audio):
        raise RuntimeError('Audio file is empty or truncated')
    print('Verified release: 24 chapters, 59 lessons, 1180 exercises, 500 examples and 559 offline audio clips',flush=True)


def run():
    ap = argparse.ArgumentParser()
    source = ap.add_mutually_exclusive_group(required=True)
    source.add_argument('--release', help='Immutable GitHub release tag')
    source.add_argument('--site-zip', type=Path, help='Local ZIP for the same verification (CI/tests)')
    ap.add_argument('--out', type=Path, default=ROOT/'dist')
    args = ap.parse_args()
    with tempfile.TemporaryDirectory(prefix='fr-grammaire-netlify-') as temp:
        if args.release:
            if not re.fullmatch(r'v\d+\.\d+\.\d+',args.release):
                ap.error('Invalid release tag')
            url = f'https://github.com/{REPOSITORY}/releases/download/{args.release}/Netlify-site.zip'
            file = Path(temp)/'Netlify-site.zip'
            print(f'Downloading verified Web release {args.release}',flush=True)
            request = urllib.request.Request(url,headers={'User-Agent':'FrGrammaire-Netlify/1.0'})
            with urllib.request.urlopen(request,timeout=120) as response, file.open('wb') as output:
                shutil.copyfileobj(response,output)
        else:
            file = args.site_zip
        with zipfile.ZipFile(file) as archive:
            verify(archive)
            stage = Path(temp)/'dist'
            archive.extractall(stage)
            dest = args.out.resolve()
            if dest.exists():
                shutil.rmtree(dest)
            shutil.copytree(stage,dest)
        print(f'Netlify publish directory: {dest} ({len(list(dest.rglob("*.ogg")))} audio clips)',flush=True)


if __name__ == '__main__':
    run()
