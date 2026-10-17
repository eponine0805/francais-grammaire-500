# Français Grammaire 500 — フランス語文法の学習アプリ

## v0.3.1

- 50文法項目・500例文。参考書形式で規則→短い例文の解説→確認問題。
- **各例文の文法解説は200文字以内**。重要な文法規則と解答の根拠を表示。
- IPAは500例文の各文に付属。ただし**eSpeakによる自動推定であり、専門家校閲済みではありません**。機械的生成で発音やリエゾンの誤りが起こり得ます。
- **4択テスト＋語順並べ替えテスト**。答えを選ぶと緑／赤で即時判定し、説明と正解を表示。固定フッターから次問へ移動。
- 解答済みの例文、誤答数、累計回答数、XP、日次学習回数、連続学習日数、復習予定を端末に保存。テスト途中も再開可能。
- 文法項目別の学習履歴と過去40回のテスト記録を確認できる。
- 女性合成音声を500件、GitHub Actionsのビルド時に生成してAPKに同梱。再生には通信不要。
- アプリ自体は完全オフライン、ログイン不要・広告なし。

**注意：** 今回のIPAは音声ファイルから検証された転写ではなく、eSpeakの自動推定。語学教材としての正確さを保証するものではなく、公開販売前にフランス語話者・専門家の確認が必要です。テスト成績は端末内SharedPreferencesへの保存のみで、別のAPK署名への交換や端末初期化時にデータが失われる可能性があります。

## 参考にしたUX

- Busuu Grammar Review：項目別の習熟度・弱点復習 https://help.busuu.com/hc/en-us/articles/12575126438674-What-is-Grammar-Review
- Babbel Grammar Guide：短い文法解説 https://support.babbel.com/hc/en-us/articles/19260489559058-Grammar-guide
- TV5MONDE 初級教材：段階的な練習 https://enseigner.tv5monde.com/articles-dossiers/articles/premiere-classe-avec-tv5monde-des-exercices-concus-pour-debuter-en

## 開発・検証

````bash
python3 scripts/check_catalog.py
python3 scripts/test_learning_flow.py
````

GitHub Actions `Build Android APK` でAndroid APKをビルド。500例文の音声生成には時間がかかります。Android実機操作の自動テストは未実施です。

## v0.3.1 の反映修正

v0.3.0 のAPKに新しい画面コードが取り込まれていなかった問題を修正。固定次問ボタン・語順組み立て・日次学習記録を実際のJava画面へ反映。IPAの注記データは `brief` キーから読み込みます。実機UIの検証と専門家のIPA校閲は未実施。
