#!/usr/bin/env python3
"""Verify the REAL production Netlify site matches the patched GitHub web runtime.

No Netlify credential required. HTTPS read-only checks from GitHub-hosted runner.
"""
import argparse
import hashlib
import json
from concurrent.futures import ThreadPoolExecutor, as_completed
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def get(url, headers=None, timeout=20):
    request = urllib.request.Request(url, headers={"User-Agent":"FrGrammaire-production-check/1.0", "Cache-Control":"no-cache", **(headers or {})})
    with urllib.request.urlopen(request,timeout=timeout) as result:
        return result.status, result.headers, result.read()

def run():
    parser=argparse.ArgumentParser()
    parser.add_argument("--url", default="https://francegrammarepo.netlify.app")
    parser.add_argument("--wait-seconds",type=int,default=220)
    args=parser.parse_args()
    base=args.url.rstrip("/")
    expected=(ROOT/"web/app.js").read_bytes()
    digest=hashlib.sha256(expected).hexdigest()
    stamp=str(int(time.time()))
    target=f"{base}/app.js?production_audit={stamp}"
    deadline=time.monotonic()+args.wait_seconds
    round_no=0
    while True:
        round_no+=1
        try:
            status,header,body=get(target)
            content_type=header.get("Content-Type","")
            found=hashlib.sha256(body).hexdigest()
            print(f"Check #{round_no}: {base} app.js status={status}, bytes={len(body)}, sha256={found[:16]}, Content-Type={content_type}", flush=True)
            if status==200 and found==digest:
                break
            if "text/html" in content_type or len(body)<1000:
                print("Site returned HTML/error instead of expected JavaScript.",flush=True)
            else:
                print(f"Stale/outdated deployment: expected sha256={digest[:16]}.",flush=True)
        except Exception as exc:
            print(f"Check #{round_no}: site not reachable: {type(exc).__name__}: {exc}",flush=True)
        if time.monotonic()>=deadline:
            raise RuntimeError("PRODUCTION NOT VERIFIED: Live /app.js did not match the current GitHub revision before timeout. Check Netlify Deploys.")
        time.sleep(15)
    for path in ("/","/sw.js"):
        st,headers,body=get(base+path+"?production_audit="+stamp)
        assert st==200 and len(body)>100, (path,st,len(body))
        if path=="/sw.js":
            sw=body.decode("utf-8")
            assert "partialAudio" in sw and "fr-grammaire-v062" in sw, "Netlify worker missing offline HTTP Range fix"
    st,headers,manifest_data=get(f"{base}/data/extra_audio_manifest.json?production_audit={stamp}")
    assert st==200, ("extra audio manifest",st)
    manifest=json.loads(manifest_data)
    extras=sorted(manifest)
    assert len(extras)==114, ("expected exactly 114 extra recordings",len(extras))
    # The full list of 114 extra files is validated in the release ZIP by the
    # Netlify build job. Probe representative real production URLs here, avoiding
    # excessive parallel requests and CDN timeouts.
    files=["0001.ogg","course_0001.ogg",extras[0],extras[len(extras)//2],extras[-1]]
    def check_audio(file):
        url=f"{base}/audio/{file}?production_audit={stamp}"
        # Get first 4 bytes. A missing static file may silently return SPA HTML with 200.
        st,headers,body=get(url,{"Range":"bytes=0-3"})
        assert st in (200,206) and body[:4]==b"OggS",(file,st,len(body),headers.get("Content-Type"))
        if st==206:
            assert len(body)==4 and headers.get("Content-Range","").startswith("bytes 0-3/"),(file,len(body),headers.get("Content-Range"))
        return file
    for file in files:
        check_audio(file)
    print(f"Audio verified: {len(files)} representative OGG routes (3 extra, 1 standard, 1 course) with HTTP Range and OggS signatures",flush=True)
    print("PRODUCTION VERIFIED: exact latest app.js, patched service worker and 5 sampled audio routes",flush=True)

if __name__=="__main__":
    try:run()
    except Exception as exc:
        print(f"ERROR: {exc}",file=sys.stderr,flush=True)
        raise
