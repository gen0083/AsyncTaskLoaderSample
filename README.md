AsyncTaskLoaderSample
======================

## What's this?

AsyncTaskLoaderの動きを確認するために作ったサンプルです。

段階を経て動きが確認できるように、ブランチを切って各段階の動作を確認できるようにしてみました。WIPです。

以下のサイトを参考にさせて頂いています。

+ [AsyncTaskLoader - Android Developers](http://developer.android.com/reference/android/content/AsyncTaskLoader.html)
+ [非同期処理 - mixi-inc/AndroidTraning](http://mixi-inc.github.io/AndroidTraining/fundamentals/2.08.async-processing.html)

なんでそういう実装をするのかよく分からず、実装しながら動きを確認してみました。

## Loaderに最小限のメソッドを実装

コンストラクタとloadInBackgroundメソッドだけを実装しても非同期処理は実行されません。

非同期処理を開始するには、onStartLoadingメソッドをオーバーライドし、`forceLoad()`を呼ぶ必要があります。

## forceLoad()を実装

onStartLoadingで`forceLoad()`を呼び出すと、loadInBackgroundの中に実装した非同期処理を実行してくれるようになります。

しかし、この状態ではすでに非同期処理が終了しているにもかかわらず、一旦ホーム画面に行ってから再度アプリに戻ってきた時に再び非同期処理が実行されてしまいます。

利用するLoaderのインスタンスは同じなので、再度非同期処理を実行するのはムダです。

Loaderで処理結果をキャッシュし、キャッシュがあるときはそれを返すようにすると何度も非同期処理をしなくてすむようになります。

## キャッシュ機構を実装

キャッシュを返す機構の実装をするには、以下のことを頭に入れておくといいでしょう。

+ 非同期処理が走っている間はLoaderManagerは処理の終了を待つ。
+ 非同期処理が終わっていればLoaderManagerはActivityのonLoadFinishedに処理結果をコールバックする。
+ 非同期処理が終わるとLoaderManagerはLoaderの状態を停止状態として管理する。（mStarted=falseにする）
+ 画面回転時には直接onLoadFinishedへ処理結果をコールバックする。（LoaderManagerが処理結果を持ってる）
+ Activityがバックグランドから戻ってくると、LoaderManagerはLoaderのonStartLoadingを呼ぶ。（mStarted=falseであるため）
+ そのためLoaderのonStartLoadingでキャッシュがあればキャッシュを処理結果として通知するようにすればよい。

## キャンセル処理

restartLoaderへの対応（WIP）

## LICENCE

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.