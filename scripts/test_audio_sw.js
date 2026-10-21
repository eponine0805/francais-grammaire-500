/* Netlify-style first 206 followed by cache-backed 206 and offline 200. */
const fs=require('fs'),vm=require('vm');
const worker=fs.readFileSync('web/sw.js','utf8'),handlers={},stored=new Map();
const payload=new Uint8Array(9871).map((_,i)=>i%251);
const store={match:async u=>stored.get(String(u))?.clone(),put:async(u,res)=>{if(res.status!==200)throw Error('Cache.put(206)');stored.set(String(u),res.clone())},addAll:async()=>{}};
let rangeFetches=0,fullFetches=0;
const scope={URL,Request,Response,Headers,Promise,ArrayBuffer,console,
 self:{location:{origin:'https://example.netlify.app'},addEventListener:(name,fn)=>handlers[name]=fn,skipWaiting:()=>{},clients:{claim:async()=>{}}},
 caches:{open:async()=>store,keys:async()=>[],delete:async()=>{}},
 fetch:async req=>{
  const range=typeof req==='string'?null:req.headers.get('Range');
  if(range){rangeFetches++;return new Response(payload,{status:206,headers:{'Content-Type':'audio/ogg'}})}
  fullFetches++;return new Response(payload,{status:200,headers:{'Content-Type':'audio/ogg'}});
 }};
vm.runInNewContext(worker,scope);
async function load(range){
 let pending,background=[];
 const request=new Request('https://example.netlify.app/audio/extra_0007_00.ogg',range?{headers:{Range:range}}:{});
 handlers.fetch({request,respondWith:p=>pending=p,waitUntil:p=>background.push(p)});
 const result=await pending;await Promise.all(background);return result;
}
(async()=>{
 let r=await load('bytes=0-4095');
 if(r.status!==206||!stored.has('/audio/extra_0007_00.ogg'))throw Error('206 not streamed or full asset not precached');
 let n=rangeFetches;
 r=await load('bytes=0-99');
 if(r.status!==206||r.headers.get('Content-Range')!=='bytes 0-99/9871'||new Uint8Array(await r.arrayBuffer()).length!==100||rangeFetches!==n)throw Error('Invalid cached 206');
 r=await load('bytes=-1024');
 if(r.status!==206||new Uint8Array(await r.arrayBuffer()).length!==1024)throw Error('Invalid suffix-range');
 r=await load();
 if(r.status!==200||new Uint8Array(await r.arrayBuffer()).length!==9871)throw Error('Invalid offline full file');
 console.log('PASS first 206, full pre-cache, replay 206, suffix 206, offline 200');
})().catch(e=>{console.error(e);process.exit(1)});
