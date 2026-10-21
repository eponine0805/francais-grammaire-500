#!/usr/bin/env python3
"""Generate ORIGINAL 59-course-sentence OGG files with the existing Supertonic female voice."""
from pathlib import Path
import json,io,subprocess
import numpy as np
import soundfile as sf
from supertonic import TTS
ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/'app/src/main/assets/course.json'
OUT=ROOT/'app/src/main/assets/audio'
RATE=44100

def main():
    units=json.loads(DATA.read_text(encoding='utf8'))['units']
    assert len(units)==59, len(units)
    OUT.mkdir(parents=True,exist_ok=True)
    tts=TTS(model='supertonic-3',auto_download=True)
    style=tts.get_voice_style(voice_name='F2')
    for n,u in enumerate(units,1):
        target=OUT/f'course_{u["id"]:04d}.ogg'
        if target.exists() and target.stat().st_size>1800:continue
        np.random.seed(304100+u['id'])
        voice,_=tts.synthesize(u['fr'],voice_style=style,lang='fr',total_steps=10,speed=0.94,verbose=False)
        samples=np.asarray(voice,dtype=np.float32).reshape(-1)
        if len(samples)<RATE*.23 or not np.isfinite(samples).all():raise ValueError(f'Invalid audio #{u["id"]}')
        peak=float(np.max(np.abs(samples)))
        if peak<0.0005:raise ValueError(f'Silent audio #{u["id"]}')
        active=np.flatnonzero(np.abs(samples)>max(.00015,peak*.004))
        samples=samples[max(0,int(active[0])-int(RATE*.025)):min(samples.size,int(active[-1])+int(RATE*.085))]
        samples=np.pad(samples,(int(RATE*.045),int(RATE*.1)))
        buff=io.BytesIO();sf.write(buff,samples,RATE,format='WAV',subtype='PCM_16')
        process=subprocess.run(['ffmpeg','-nostdin','-hide_banner','-loglevel','error','-y','-i','pipe:0','-ac','1','-ar',str(RATE),'-c:a','libvorbis','-q:a','7',str(target)],input=buff.getvalue(),capture_output=True)
        if process.returncode or target.stat().st_size<1800:raise RuntimeError(f'Failed encoding {u["id"]}: {process.stderr.decode()}')
        if n%10==0:print('Course voices',n,'/',len(units),flush=True)
    assert len(list(OUT.glob('course_*.ogg')))==len(units)
    (OUT/'course_manifest.json').write_text(json.dumps({'model':'Supertone/supertonic-3','voice':'F2','lang':'fr','items':len(units),'human_listening_review':False},ensure_ascii=False,indent=2))
    print('Created',len(units),'offline course clips')
if __name__=='__main__':main()
