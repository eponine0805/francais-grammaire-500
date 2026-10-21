/* v0.6.2 Netlify audio hotfix: offline audio with HTTP Range support. */
const CACHE='fr-grammaire-v062-shell';
const AUDIO='fr-grammaire-v062-audio';
const SHELL=['/','/index.html','/style.css','/app.js','/manifest.webmanifest','/icon-192.png','/icon-512.png','/data/course.json','/data/course_ipa.json','/data/lessons.json','/data/examples.json','/data/annotations.json','/data/intensive.json','/data/extra_audio_manifest.json'];
self.addEventListener('install',e=>e.waitUntil(caches.open(CACHE).then(c=>c.addAll(SHELL)).then(()=>self.skipWaiting())));
self.addEventListener('activate',e=>e.waitUntil(Promise.all([
 caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE&&k!==AUDIO).map(k=>caches.delete(k)))),self.clients.claim()
])));
function goodAudio(r){return r&&r.status===200&&r.ok&&(/audio\/|application\/ogg|application\/octet-stream/i).test(r.headers.get('Content-Type')||'audio/ogg')}
async function partialAudio(full,requested){
 const bytes=await full.arrayBuffer();const size=bytes.byteLength;
 const m=/^bytes=(\d*)-(\d*)$/.exec(requested||'');
 if(!m||(!m[1]&&!m[2]))return full;
 let start,end;
 if(!m[1]){const suffix=Number(m[2]);start=Math.max(0,size-suffix);end=size-1}
 else{start=Number(m[1]);end=m[2]?Math.min(size-1,Number(m[2])):size-1}
 if(start>=size||start>end)return new Response(null,{status:416,headers:{'Content-Range':'bytes */'+size,'Accept-Ranges':'bytes'}});
 const content=bytes.slice(start,end+1);
 return new Response(content,{status:206,headers:{'Content-Type':full.headers.get('Content-Type')||'audio/ogg','Content-Range':'bytes '+start+'-'+end+'/'+size,'Content-Length':String(content.byteLength),'Accept-Ranges':'bytes'}})
}
async function audioResponse(e){
 const request=e.request;const url=new URL(request.url);const store=await caches.open(AUDIO);
 const range=request.headers.get('Range');
 let hit=await store.match(url.pathname);
 if(hit)return range?partialAudio(hit,range):hit;
 try{
   const res=await fetch(request);
   if(goodAudio(res)&&!range){e.waitUntil(store.put(url.pathname,res.clone()).catch(err=>console.warn('Audio cache unavailable',err)))}
   else if(range&&res.status===206){
     // A 206 is not cacheable in CacheStorage. Download a separate complete 200.
     e.waitUntil(fetch(url.pathname,{cache:'no-store'}).then(r=>goodAudio(r)?store.put(url.pathname,r.clone()):undefined).catch(err=>console.warn('Audio precache unavailable',err)));
   }
   return res;
 }catch(err){
   hit=await store.match(url.pathname);
   if(hit)return range?partialAudio(hit,range):hit;
   throw err;
 }
}
self.addEventListener('fetch',e=>{
 const request=e.request;if(request.method!=='GET')return;
 const u=new URL(request.url);if(u.origin!==self.location.origin)return;
 if(u.pathname.startsWith('/audio/')){e.respondWith(audioResponse(e));return}
 e.respondWith(fetch(request).then(res=>{
   if(res.ok&&res.status===200){e.waitUntil(caches.open(CACHE).then(c=>c.put(request,res.clone())).catch(()=>{}))}
   return res;
 }).catch(()=>caches.match(request)));
});
self.addEventListener('message',e=>{if(e.data==='SKIP_WAITING')self.skipWaiting()});
