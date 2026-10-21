/* Offline-first shell/data with on-demand caching of prerecorded French speech. */
const CACHE='fr-grammaire-v060-shell';
const AUDIO='fr-grammaire-v050-audio';
const SHELL=['/','/index.html','/style.css','/app.js','/manifest.webmanifest','/icon-192.png','/icon-512.png','/data/course.json','/data/course_ipa.json','/data/lessons.json','/data/examples.json','/data/annotations.json','/data/intensive.json'];
self.addEventListener('install',e=>e.waitUntil(caches.open(CACHE).then(c=>c.addAll(SHELL)).then(()=>self.skipWaiting())));
self.addEventListener('activate',e=>e.waitUntil(Promise.all([caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE&&k!==AUDIO).map(k=>caches.delete(k)))),self.clients.claim()])));
self.addEventListener('fetch',e=>{if(e.request.method!=='GET')return;const u=new URL(e.request.url);if(u.origin!==self.location.origin)return;if(u.pathname.startsWith('/audio/')){e.respondWith(caches.open(AUDIO).then(async c=>{const hit=await c.match(e.request);if(hit)return hit;const res=await fetch(e.request);if(res.ok)await c.put(e.request,res.clone());return res}));return}e.respondWith(caches.match(e.request).then(hit=>hit||fetch(e.request).then(async res=>{if(res.ok){const c=await caches.open(CACHE);c.put(e.request,res.clone())}return res})))});
self.addEventListener('message',e=>{if(e.data==='SKIP_WAITING')self.skipWaiting()});
