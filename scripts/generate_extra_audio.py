#!/usr/bin/env python3
"""Create 114 short female French clips for missing worked sentences.
Synthetic recordings require human pronunciation review before certification.
"""
from pathlib import Path
import io,json,subprocess
import numpy as np
import soundfile as sf
from supertonic import TTS
ROOT=Path(__file__).resolve().parents[1]/'app/src/main/assets'
OUT=ROOT/'audio'
SR=44100
def main():
    entries=json.loads((ROOT/'extra_audio_manifest.json').read_text(encoding='utf8'))
    assert len(entries)==114
    OUT.mkdir(parents=True,exist_ok=True)
    pending=[(file,x) for file,x in sorted(entries.items()) if not (OUT/file).exists()]
    if pending:
        tts=TTS(model='supertonic-3',auto_download=True)
        voice=tts.get_voice_style(voice_name='F2')
        for num,(file,entry) in enumerate(pending,1):
            np.random.seed(330000+entry['unit']*10+entry['index'])
            raw,_=tts.synthesize(entry['fr'],voice_style=voice,lang='fr',total_steps=10,speed=0.88,verbose=False)
            signal=np.asarray(raw,dtype=np.float32).reshape(-1)
            if len(signal)<SR*.25 or not np.isfinite(signal).all():raise RuntimeError('Bad signal '+file)
            peak=float(np.max(np.abs(signal)))
            if peak<.0005:raise RuntimeError('Silent '+file)
            active=np.flatnonzero(np.abs(signal)>max(.00015,peak*.004))
            signal=signal[max(0,int(active[0])-int(SR*.035)):min(len(signal),int(active[-1])+int(SR*.1))]
            signal=np.pad(signal,(int(SR*.055),int(SR*.10)))
            if not .3<=len(signal)/SR<=20:raise RuntimeError('Bad duration '+file)
            buf=io.BytesIO()
            sf.write(buf,signal,SR,format='WAV',subtype='PCM_16')
            proc=subprocess.run(['ffmpeg','-nostdin','-hide_banner','-loglevel','error','-y','-i','pipe:0','-ar','44100','-ac','1','-c:a','libvorbis','-q:a','7',str(OUT/file)],input=buf.getvalue(),capture_output=True)
            if proc.returncode or (OUT/file).stat().st_size<1800:raise RuntimeError('Encode failure '+file)
            if num%10==0:print(f'Generated {num}/{len(pending)}',flush=True)
    assert all((OUT/file).stat().st_size>=1800 for file in entries)
    print('All 114 additional audio recordings present')
if __name__=='__main__':main()
