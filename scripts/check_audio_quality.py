#!/usr/bin/env python3
"""Verify decoding, boundaries, unique bytes and pace of 559 recordings, not phoneme quality."""
import json,io,hashlib
from pathlib import Path
import numpy as np,soundfile as sf
root=Path(__file__).resolve().parents[1]
a=root/'app/src/main/assets'
examples=json.loads((a/'examples.json').read_text())
units=json.loads((a/'course.json').read_text())['units']
recordings=[(f'{x["id"]:04d}.ogg',x["fr"]) for x in examples]+[(f'course_{x["id"]:04d}.ogg',x["fr"]) for x in units]
assert len(recordings)==len(list((a/'audio').glob('*.ogg')))==559
hashes=set();fast=[];durations=[]
for name,phrase in recordings:
    raw=(a/'audio'/name).read_bytes()
    digest=hashlib.sha256(raw).hexdigest();assert digest not in hashes,name;hashes.add(digest)
    sig,sr=sf.read(io.BytesIO(raw),dtype='float32')
    assert sr==44100 and sig.ndim==1 and np.isfinite(sig).all(),name
    t=len(sig)/sr;peak=float(np.max(np.abs(sig)))
    assert .3<t<15 and .002<peak<.995,(name,t,peak)
    active=np.flatnonzero(np.abs(sig)>max(.00015,peak*.004))
    assert len(active)>0 and active[0]/sr>=.025 and (len(sig)-active[-1]-1)/sr>=.04,name
    wps=len(phrase.split())/t
    if wps>4.7:fast.append({'file':name,'phrase':phrase,'words_per_second':round(wps,2)})
    durations.append(round(t,3))
report={'audio_count':559,'version':'0.5.1','sample_rate_hz':44100,'duration_min_seconds':min(durations),'duration_max_seconds':max(durations),'fast_at_original_speed':fast,'default_playback_speed':0.8,'limitations':'This cannot validate any French phoneme, stress, liaison or semantic content; listening review pending.'}
out=root/'docs/AUDIO_QA_v051.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print('PASS: 559 files decoded; no corrupt, duplicated, clipped or extreme silence recorded')
print('Fast speech queue:',len(fast))
