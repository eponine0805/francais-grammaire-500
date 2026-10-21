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
                'data/lessons.json','data/extra_audio_manifest.json']
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
    manifest=json.loads(archive.read('data/extra_audio_manifest.json'))
    if len(manifest)!=114:raise RuntimeError('Missing additional example audio manifest')
    for unit in intensive['units']:
        for worked in unit['examples']:
            if not worked.get('ipa') or not worked.get('audioFile'):
                raise RuntimeError('An example has no phonetic annotation or audio reference')
        for task in unit['tasks']:
            if not task.get('ipa') or not task.get('spokenFr') or not task.get('audioFile'):
                raise RuntimeError('A quiz answer lacks a corresponding sentence/audio/IPA')
    audio = [p for p in seen if p.startswith('audio/') and p.endswith('.ogg')]
    word_audio = [p for p in audio if re.fullmatch(r'audio/\d{4}\.ogg',p)]
    lesson_audio = [p for p in audio if re.fullmatch(r'audio/course_\d{4}\.ogg',p)]
    extra_audio = [p for p in audio if re.fullmatch(r'audio/extra_\d{4}_\d{2}\.ogg',p)]
    if len(word_audio)!=500 or len(lesson_audio)!=59 or len(extra_audio)!=114 or len(audio)!=673:
        raise RuntimeError(f'Audio mismatch: {len(word_audio)}+{len(lesson_audio)}+{len(extra_audio)} != 673')
    if {path.split('/',1)[1] for path in extra_audio}!=set(manifest):
        raise RuntimeError('Extra voice manifest and release ZIP disagree')
    if any(archive.getinfo(name).file_size < 1800 for name in audio):
        raise RuntimeError('Audio file is empty or truncated')
    print('Verified release: 24 chapters, 59 lessons, 1180 exercises, 500 examples and 673 offline audio clips',flush=True)


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
            # Deploy current tested runtime while retaining pinned v0.6.1 audio.
            # Netlify clones only tracked sources; this overlay is required.
            for patched in ('app.js', 'sw.js'):
                source = ROOT/'web'/patched
                if not source.is_file():
                    raise RuntimeError(f'Missing web runtime: {patched}')
                shutil.copyfile(source, stage/patched)
            dest = args.out.resolve()
            if dest.exists():
                shutil.rmtree(dest)
            shutil.copytree(stage,dest)
        print(f'Netlify publish directory: {dest} ({len(list(dest.rglob("*.ogg")))} audio clips)',flush=True)


if __name__ == '__main__':
    run()
