#!/usr/bin/env python3
"""Verify the REAL production Netlify site matches the patched GitHub web runtime.

No Netlify credential required. HTTPS read-only checks from GitHub-hosted runner.
"""
import argparse
import hashlib
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
    for file in ("0001.ogg","course_0001.ogg","extra_0007_00.ogg"):
        url=f"{base}/audio/{file}?production_audit={stamp}"
        st,headers,body=get(url)
        assert st==200 and len(body)>1800 and body[:4]==b"OggS", (file,st,len(body),headers.get("Content-Type"))
        range_status,range_headers,range_body=get(url,{"Range":"bytes=0-255"})
        assert range_status in (200,206) and range_body, (file,range_status)
        if range_status==206:
            assert len(range_body)==256 and range_headers.get("Content-Range","").startswith("bytes 0-255/"),(file,len(range_body),range_headers.get("Content-Range"))
        print(f"Audio OK: {file} {len(body)} bytes, HTTP Range={range_status}",flush=True)
    print("PRODUCTION VERIFIED: exact latest app.js, patched service worker and three working audio routes",flush=True)

if __name__=="__main__":
    try:run()
    except Exception as exc:
        print(f"ERROR: {exc}",file=sys.stderr,flush=True)
        raise
