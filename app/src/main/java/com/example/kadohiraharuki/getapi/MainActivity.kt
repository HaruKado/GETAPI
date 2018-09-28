package com.example.kadohiraharuki.getapi

import android.database.CursorJoiner
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.text.PrecomputedText
import android.text.TextUtils.replace
import android.util.Log
import android.widget.Toast
import com.beust.klaxon.JsonObject
import com.parse.Parse
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*


//https://qiita.com/minme31/items/a9636cb0453524c64e67  (参考記事)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            //ボタンがクリックされたらAPIを叩く。
            HitAPITask().execute("https://api.github.com/users")

        }

    }

    inner class HitAPITask : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String?): String {
            //ここでAPIを叩きます。バックグラウンドで処理する内容です。
            //HTTP固有の機能をサポートするURLConnection
            var connection: HttpURLConnection? = null
            //文字、配列、行をバッファリングすることによって、文字型入力ストリームからテキストを効率良く読み込む
            var reader: BufferedReader? = null
            //Stringクラス同様に宣言した変数に、文字列を格納するために使用
            val buffer: StringBuffer

            try {
                //param[0]にはAPIのURI(String)を入れます(後ほど)。
                //AsynkTask<...>の一つめがStringな理由はURIをStringで指定するからです。
                val url = URL(params[0])
                connection = url.openConnection() as HttpURLConnection
                connection.connect()  //ここで指定したAPIを叩いてみてます。


                //ここから叩いたAPIから帰ってきたデータを使えるよう処理していきます。

                //とりあえず取得した文字をbufferに。
                val stream = connection.inputStream
                reader = BufferedReader(InputStreamReader(stream))
                buffer = StringBuffer()
                var line: String?
                while (true) {
                    line = reader.readLine()
                    if (line == null) {
                        break
                    }
                    buffer.append(line)
                }
                Log.d("CHECK", buffer.toString())



                val jsonText = buffer.toString()

                val parentJsonObj = JSONArray(jsonText)

                val parentJSONObject = parentJsonObj.getJSONObject(0)

                val login: String = parentJSONObject.getString("login")
                Log.d("CHECK2", login.format())




                //Stringでreturnしてあげましょう。
                return "$login"  //

                //ここから下は、接続エラーとかJSONのエラーとかで失敗した時にエラーを処理する為のものです。
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            //finallyで接続を切断してあげましょう。
            finally {
                connection?.disconnect()
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
                //textView.text =("失敗")
                //失敗した時はnullやエラーコードなどを返しましょう。
                return null.toString()

        }





        //返ってきたデータをビューに反映させる処理はonPostExecuteに書きます。これはメインスレッドです。
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == null) {
                return
            }

            textView.text = result
        }
    }
}







