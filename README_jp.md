# media-sample-scala

これは、ScalaとPlay Frameworkの開発者のためのサンプルプロジェクトです。

このプロジェクトでは、以下の基本的なトピックを扱っています:

* ユーザ登録
* ユーザログイン
* 記事一覧表示
* 記事詳細表示
* 記事投稿
* DBテーブルのリレーション
* DBトランザクションの制御
* 画像ファイルのアップロードと表示
* 認証 (TODO)
* 単体テスト (TODO)


## 序文

このプロジェクトは、Mac OS XまたはLinuxで使用することを想定しています。

以下に説明する手順はMac OS Xの場合のみです。

## 使用法

まず、MySQLをインストールする必要があります。

```bash
brew install mysql
mysql.server start
```

そして、以下のようにsampleuserを作成し、パスワードを設定します。

```bash
CREATE USER 'sampleuser'@'localhost' IDENTIFIED by 'changeme';
GRANT ALL PRIVILEGES ON *.* TO 'sampleuser'@'localhost';
```

MySQLを再起動してください。

```bash
mysql.server restart
```

次に、以下のように画像ファイルを保存するためのディレクトリを作成してください。

```bash
mkdir /tmp/mediasample
```

その後、次のようにこのアプリを起動することができます。

```bash
# media-sample-scalaディレクトリの中で実行
sbt run
```

[`localhost:9000`](http://localhost:9000) このURLへブラウザからアクセスできます。

## TODO

- [ ] 画像ファイルアップロード、表示に複数の拡張子を対応させる
- [ ] 認証機能
- [ ] 単体テスト機能
