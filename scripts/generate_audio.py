#!/usr/bin/env python3
"""Generate 500 offline French female recordings with Supertone Supertonic 3.

Run at build time; the Android app never contacts an API. Not an audio pronunciation audit.
"""
from pathlib import Path
import json, io, subprocess
import numpy as np
import soundfile as sf
from supertonic import TTS

ROOT=Path(__file__).resolve().parents[1]
EXAMPLES=json.loads((ROOT/'app/src/main/assets/examples.json').read_text(encoding='utf-8'))
OUT=ROOT/'app/src/main/assets/audio'
SR=44100

def encode(signal,path):
    memory=io.BytesIO()
    sf.write(memory,signal,SR,format='WAV',subtype='PCM_16')
    p=subprocess.run(['ffmpeg','-nostdin','-hide_banner','-loglevel','error','-y','-i','pipe:0','-ar',str(SR),'-ac','1','-c:a','libvorbis','-q:a','7',str(path)],input=memory.getvalue(),capture_output=True)
    if p.returncode: raise RuntimeError(p.stderr.decode('utf-8','replace'))
    if path.stat().st_size<1800: raise RuntimeError('Tiny audio: '+str(path))

def main():
    assert len(EXAMPLES)==500
    OUT.mkdir(parents=True,exist_ok=True)
    tts=TTS(model='supertonic-3',auto_download=True)
    style=tts.get_voice_style(voice_name='F2')
    for item in EXAMPLES:
        i=item['id'];path=OUT/f'{i:04}.ogg'
        if path.exists() and path.stat().st_size>1800:continue
        np.random.seed(210000+i)
        signal,_=tts.synthesize(item['fr'],voice_style=style,lang='fr',total_steps=10,speed=0.94,verbose=False)
        arr=np.asarray(signal,dtype=np.float32).reshape(-1)
        if arr.size<SR*0.25 or not np.isfinite(arr).all():raise RuntimeError('Bad audio: '+str(i))
        peak=float(np.max(np.abs(arr)))
        if peak<0.0005: raise RuntimeError('Unvoiced: '+str(i))
        active=np.flatnonzero(np.abs(arr)>max(0.00015,peak*0.004))
        arr=arr[max(0,int(active[0])-int(SR*0.025)):min(arr.size,int(active[-1])+int(SR*0.08))]
        arr=np.pad(arr,(int(SR*0.04),int(SR*0.10)))
        seconds=arr.size/SR
        if not 0.3<=seconds<=20:raise RuntimeError(f'Unusual duration {seconds:.1f}s in #{i}: {item["fr"]}')
        encode(arr,path)
        if i%25==0:print(f'Voice generation: {i}/500',flush=True)
    assert len(list(OUT.glob('*.ogg')))==500
    (OUT/'manifest.json').write_text(json.dumps({'model':'Supertone/supertonic-3','voice':'F2','lang':'fr','items':500,'sample_rate':SR,'synthetic_voice':True,'human_listening_review':False},ensure_ascii=False,indent=2),encoding='utf-8')
    print('Finished producing 500 French OGG files.')
if __name__=='__main__': main()
