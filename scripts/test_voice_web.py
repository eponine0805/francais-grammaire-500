#!/usr/bin/env python3
"""Chromium with fake Audio + localStorage confirms rate, pitch, and no accidental replay."""
import json
from pathlib import Path
from playwright.sync_api import sync_playwright
R=Path(__file__).resolve().parents[1]
WEB=R/'web'; ASSET=R/'app/src/main/assets'
fixtures={k:json.loads((ASSET/f'{k}.json').read_text(encoding='utf8')) for k in ['course','course_ipa','examples','annotations','lessons','intensive']}
html=(WEB/'index.html').read_text(encoding='utf8').replace('<script src="/app.js" defer></script>','')
script=(WEB/'app.js').read_text()
mock='''(initial)=>{window._store=Object.assign({},initial);Object.defineProperty(window,'localStorage',{configurable:true,value:{getItem:k=>window._store[k]??null,setItem:(k,v)=>window._store[k]=String(v)}});window.audioEvents=[];window.Audio=class FakeAudio {constructor(url){this.src=url;this.currentTime=0;this.playbackRate=1;this.preservesPitch=false;window.audioEvents.push(this)}play(){this.playedAt=this.playbackRate;return Promise.resolve()}pause(){this.paused=true}};window.fetch=async u=>({ok:true,json:async()=>window._fixtures[u.split('/').pop().replace('.json','')]});}'''
with sync_playwright() as p:
 browser=p.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
 for preference in [None,'0.7']:
  page=browser.new_page(viewport={'width':375,'height':812})
  page.set_content(html)
  page.add_style_tag(content=(WEB/'style.css').read_text())
  page.evaluate('(fixtures)=>window._fixtures=fixtures',fixtures)
  page.evaluate(mock,{'fr-grammaire-voice-rate-v1':preference} if preference is not None else {})
  page.add_script_tag(content=script)
  page.locator('[data-act="chapter"][data-id="0"]').wait_for()
  page.locator('[data-act="chapter"][data-id="0"]').click()
  page.locator('[data-act="unit"]').first.click()
  rate=float(preference) if preference else 0.8
  page.wait_for_function('(rate)=>window.audioEvents.length===1 && window.audioEvents[0].playedAt===rate',arg=rate)
  assert page.locator('[data-act="voice-rate"]').count()==4
  assert page.evaluate('audioEvents[0].preservesPitch') is True
  if preference is None:
   page.locator('[data-act="voice-rate"][data-val="0.7"]').click()
   assert page.evaluate('audioEvents[0].playbackRate')==0.7
   assert page.evaluate('audioEvents.length')==1, 'speed change replayed unexpectedly'
   assert page.evaluate('localStorage.getItem("fr-grammaire-voice-rate-v1")')=='0.7'
  assert page.locator('[data-act="voice-rate"][aria-pressed="true"]').count()==1
  page.locator('[data-act="voice-rate"][data-val="1"]').click()
  page.locator('[data-act="sound-course"]').click()
  page.wait_for_function('window.audioEvents.length===2 && window.audioEvents[1].playedAt===1')
  page.locator('[data-nav="catalog"]').click()
  page.locator('[data-act="legacy"]').first.click()
  page.wait_for_function('window.audioEvents.length===3 && window.audioEvents[2].playedAt===1')
  page.locator('[data-act="voice-rate"][data-val="0.9"]').click()
  page.locator('[data-act="sound-legacy"]').click()
  page.wait_for_function('window.audioEvents.length===4 && window.audioEvents[3].playedAt===0.9')
  assert page.evaluate('audioEvents[2].paused') is True
  page.close()
 browser.close()
print('PASS: Chromium 375px Android-like mobile; 0.8 default / 0.7 persisted preference; pitch preservation and no rate-change replay; both course and example sound')
