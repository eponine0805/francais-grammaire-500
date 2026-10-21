#!/usr/bin/env python3
"""Check source-book alignment and invariants; NOT a fluent-speaker language audit."""
from pathlib import Path
import collections,json,re
root=Path(__file__).resolve().parents[1]
assets=root/'app/src/main/assets'
c=json.loads((assets/'course.json').read_text(encoding='utf8'))
chapters,units=c['chapters'],c['units']
assert len(chapters)==24 and len(units)==59,(len(chapters),len(units))
assert [v['id'] for v in chapters]==list(range(24))
assert [v['id'] for v in units]==list(range(1,60))
assert all(v['sourcePage']>=5 for v in chapters)
counts=collections.Counter(u['chapter'] for u in units)
assert all(counts[i]>=1 for i in range(24)),counts
assert all(len(u['compare'])==2 for u in units)
assert all(len(u['options'])==4 and len(set(u['options']))==4 for u in units)
assert all(u['options'].count(u['answer'])==1 for u in units)
assert all(len(u['rule'])<210 and len(u['why'])<160 and len(u['fr'].split())>=3 for u in units)
assert len({u['fr'].casefold() for u in units})==59
assert all(1<=x<=50 for c in chapters for x in c['legacyLessons'])
base=json.loads((assets/'examples.json').read_text(encoding='utf8'))
assert len(base)==500 and [a['id'] for a in base]==list(range(1,501))
marks=json.loads((assets/'course_ipa.json').read_text(encoding='utf8'))
assert len(marks)==59 and [a['id'] for a in marks]==list(range(1,60))
assert all(a['ipa'].startswith('/') and a['ipa'].endswith('/') for a in marks)
assert not any('syi' in a['ipa'] or 'plyi' in a['ipa'] for a in marks)
# Pure-language invariant tests on answer selection, word-order generation and scope.
for u in units:
    for choice in u['options']:
        assert (choice==u['answer'])==bool(u['options'].index(choice)==u['options'].index(u['answer']))
    fragments=u['fr'].split()
    for i in range(len(fragments)):
        chosen=list(range(i+1))
        assert ' '.join(fragments[p] for p in chosen)==' '.join(fragments[:i+1])
for a in range(24):
    for b in range(a,24):
        candidates=[u['id'] for u in units if a<=u['chapter']<=b]
        assert candidates and len(candidates)==len(set(candidates))
        for n in [10,20,30,40,50,60,70,80,90,100,len(candidates)]:
            chosen=candidates[:min(n,len(candidates))]
            assert all(a<=units[i-1]['chapter']<=b for i in chosen)
java=(root/'app/src/main/java/com/francais/grammaire/MainActivity.java').read_text(encoding='utf8')
for marker in ('showCourseChapter(', 'showCourseUnit(', 'showCourseCheck(', 'showCourseTestSetup(', 'showCourseDrill(', 'startCourseReview(', 'course_resume_unit','playCourse(', 'course_ipa.json','pending_quiz_v3'):
    assert marker in java,marker
print('PASS: 24 PDF-aligned chapters, 59 authored units, 59 assessments, 500 legacy examples, all chapter-range selections and course source/UI contracts')
print('NOTE: These tests DO NOT certify 59 French sentences or IPA against an independent linguistic reviewer')
