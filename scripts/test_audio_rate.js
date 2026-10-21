const fs=require('fs'),vm=require('vm'),assert=require('assert');
const js=fs.readFileSync('web/app.js','utf8');
const start=js.indexOf("const AUDIO_RATE_KEY="),end=js.indexOf("function autoPlay(",start);
assert(start>0&&end>start,'Voice control functions missing');
const code=js.slice(start,end);
class AudioMock{
 constructor(){this.playbackRate=1;this.defaultPlaybackRate=1;this.listeners={};AudioMock.instances.push(this)}
 set src(url){this.url=url;this.playbackRate=this.defaultPlaybackRate} // HTML media load resets rate
 get src(){return this.url}
 addEventListener(t,fn){this.listeners[t]=fn}
 play(){this.startedAtRate=this.playbackRate;return Promise.resolve()}
 pause(){this.paused=true}
 removeAttribute(){}
 load(){}
}
AudioMock.instances=[];
const memory=new Map();
const buttons=[.5,.6,.7,.8,.9,1].map(v=>({dataset:{val:String(v)},classList:{toggle(k,on){this[k]=on}},setAttribute(k,on){this[k]=on}}));
const meters=[{textContent:''}];
const ctx={localStorage:{getItem:k=>memory.get(k)||null,setItem:(k,v)=>memory.set(k,v)},
 document:{querySelectorAll:s=>s.includes('data-voice-current')?meters:buttons},
 Audio:AudioMock,toast:t=>{throw Error(t)},console};
vm.createContext(ctx);vm.runInContext(code,ctx);
vm.runInContext("changeVoiceRate(0.5);playFile('0001.ogg')",ctx);
assert.strictEqual(AudioMock.instances.at(-1).startedAtRate,.5);
assert.strictEqual(AudioMock.instances.at(-1).defaultPlaybackRate,.5);
AudioMock.instances.at(-1).playbackRate=1;
AudioMock.instances.at(-1).listeners.loadedmetadata();
assert.strictEqual(AudioMock.instances.at(-1).playbackRate,.5);
vm.runInContext("changeVoiceRate(0.6)",ctx);
assert.strictEqual(AudioMock.instances.at(-1).playbackRate,.6);
assert.strictEqual(AudioMock.instances.at(-1).defaultPlaybackRate,.6);
vm.runInContext("replayAtRate()",ctx);
assert.strictEqual(AudioMock.instances.at(-1).startedAtRate,.6);
AudioMock.instances.at(-1).playbackRate=1;
AudioMock.instances.at(-1).listeners.playing();
assert.strictEqual(AudioMock.instances.at(-1).playbackRate,.6);
assert.strictEqual(meters[0].textContent,'0.6倍');
assert.strictEqual(memory.get('fr-grammaire-voice-rate-v1'),'0.6');
vm.runInContext("changeVoiceRate(1.0);replayAtRate()",ctx);
assert.strictEqual(AudioMock.instances.at(-1).startedAtRate,1);
assert(vm.runInContext("voiceControl()",ctx).includes('data-act="sound-compare"'));
console.log('PASS: browser media-load reset, metadata reapply, playing reapply, 0.5/0.6x, rate persistence and replay');
