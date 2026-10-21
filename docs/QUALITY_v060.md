# v0.6.0 review notes

- Curriculum: supplied Nagoya University 2022 French introductory grammar, sequence used as structure only.
- 59 unique three-part text lectures (intro, method, pitfalls); 7 worked sentences per lesson; 1180 assessment tasks.
- 20 tasks per unit: 1 conceptual choice + 1 core sentence order + 6 existing/custom example blanks + 6 orders + 6 meaning selections. Thus repeated sentences are intentionally retrieved from several angles, NOT 1180 original sentence pairs.
- Automated data test: validates every answer appears once in its 4 choices, fill reconstructs original sentence, order targets valid, full unit count; does not prove semantic correctness of grammar or distractors.
- Preserve Android app ID and progress data keys; Web uses old localStorage key. No Android-Web cloud sync.
- Audio: 559 existing clips, original content retained; new text examples may have no sound. No native-speaker full audio/IPA QC.
- Important follow-up: full human editorial QA, instrumented Android UI tests and signing stable release key.
