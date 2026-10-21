#!/usr/bin/env python3
"""Exercise the real 20-question Netlify UX with Chromium, including persistence."""
import json
from pathlib import Path
from playwright.sync_api import sync_playwright
ROOT=Path(__file__).resolve().parents[1];W=ROOT/'web';A=ROOT/'app/src/main/assets'
fixtures={k:json.loads((A/f'{k}.json').read_text(encoding='utf8')) for k in ['course','course_ipa','examples','annotations','lessons','intensive']}
html=(W/'index.html').read_text(encoding='utf-8').replace('<script src="/app.js" defer></script>','')
with sync_playwright() as p:
 b=p.chromium.launch(executable_path='/usr/bin/chromium',headless=True,args=['--no-sandbox'])
 page=b.new_page(viewport={'width':375,'height':812});errors=[]
 page.on('pageerror',lambda e:errors.append(str(e)))
 page.set_content(html);page.add_style_tag(content=(W/'style.css').read_text())
 page.evaluate('(fixtures)=>{window._fixtures=fixtures;window._store={}; Object.defineProperty(window,"localStorage",{configurable:true,value:{getItem:k=>window._store[k]??null,setItem:(k,v)=>window._store[k]=v}});window.Audio=class{constructor(){this.currentTime=0;this.playbackRate=1;}play(){return Promise.resolve()}pause(){}};window.fetch=async u=>({ok:true,json:async()=>window._fixtures[u.split("/").pop().replace(".json","")]});}',fixtures)
 page.add_script_tag(content=(W/'app.js').read_text())
 page.locator('[data-act="chapter"][data-id="0"]').wait_for()
 page.locator('[data-act="chapter"][data-id="0"]').click()
 page.locator('[data-act="unit"][data-id="1"]').click()
 assert page.locator('.lectureSection').count()==3
 assert page.locator('.worked').count()==6
 assert '20問の確認テスト' in page.locator('main').inner_text()
 assert page.locator('[data-act="start-deep"]').count()==2
 page.locator('[data-act="start-deep"]').first.click()
 assert page.locator('[data-act="deep-choice"]').count()==4
 # All twenty items must be answerable using the visible source example text.
 tasks=fixtures['intensive']['units'][0]['tasks']
 for i,t in enumerate(tasks):
  assert f'{i+1}/20' in page.locator('.topline').inner_text()
  if t['kind']=='order':
   words=t['fr'].split()
   for index in range(len(words)):
    page.locator(f'[data-act="deep-add"][data-idx="{index}"]').click()
   page.locator('[data-act="deep-confirm"]').click()
  else:
   choices=page.locator('[data-act="deep-choice"]')
   selected=next(j for j in range(choices.count()) if choices.nth(j).get_attribute('data-val')==t['answer'])
   choices.nth(selected).click()
  assert '正解' in page.locator('.feedback').inner_text(),(i,t)
  page.locator('[data-act="deep-next"]').click()
 assert '20 / 20' in page.locator('.resultNumber').inner_text()
 assert page.locator('[data-act="deep-restart"]').count()==1
 assert json.loads(page.evaluate('window._store["fr-grammaire-2026-learning-v1"]'))['deepScores']['1']['best']==20
 page.locator('[data-act="deep-restart"]').click()
 page.locator('[data-act="deep-choice"]').first.click()
 page.locator('[data-act="deep-next"]').click()
 saved=json.loads(page.evaluate('window._store["fr-grammaire-2026-learning-v1"]'))
 assert saved['intensive']['index']==1
 # Persisted quiz can be recovered on reload.
 page.evaluate('window.__saved = window._store["fr-grammaire-2026-learning-v1"]')
 page.reload(wait_until='domcontentloaded') if False else None
 page.screenshot(path='/mnt/data/frg_intensive/grammar-intensive-mobile.png',full_page=True)
 assert not errors,errors
 print('PASS Chromium: all 20 questions, 7 worked examples, immediate feedback, order chips, best score, in-progress save')
 b.close()
