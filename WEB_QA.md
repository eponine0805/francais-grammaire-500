# Android and Netlify shared-course QA

- The Web version uses course.json, course_ipa.json, lessons.json, examples.json, and annotations.json copied byte-for-byte from the Android source tree.
- During build, the published Android APK is treated as the source of truth for 559 offline voice clips; the builder asserts `assets/course.json` and `assets/examples.json` match the repository copies exactly.
- Count checks: 24 chapters, 59 guided grammar units, 500 legacy examples, 500 audio clips + 59 additional course clips.
- Web PWA: offline app shell, optional on-device audio cache, localStorage study history, backup/restore JSON.
- Differences: Web and Android maintain **separate** local study progress; there is no cloud sync or shared account. Auto-play may be blocked by browsers until user interaction.
- This is not a verification of French IPA accuracy or spoken pronunciation. All course IPA is automatically derived and awaits expert validation.
- Run `node --check web/app.js` and `python3 scripts/build_web.py --apk [APK]`; inspect the built contents.
