package com.example.kadohiraharuki.getapi

import android.content.Context
import android.content.Intent
import android.database.CursorJoiner
import android.graphics.BitmapFactory
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.text.PrecomputedText
import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.beust.klaxon.JsonObject
import com.example.kadohiraharuki.getapi.R.id.listView
import com.parse.Parse
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.sql.Connection
import java.util.*


//https://qiita.com/minme31/items/a9636cb0453524c64e67  (参考記事)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            //ボタンがクリックされたらAPIを叩く。
            val s =  search.toString()
            val uri = Uri.parse("https://api.github.com/search/users?q="+s.trim()+"+sort:followers")
            HitAPITask().execute(uri)

        }
        listView.setOnItemClickListener { parent, view, position, id ->
            /*val url: String = "https://github.com"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)*/
            val intent = Intent(this, webview::class.java)
                startActivity(intent)

        }



        //試しリスト
        val names = listOf(
                "Ichiro",
                "Ziro",
                "Taro",
                "Shiro")
        val location = listOf(
                "Hokkaido",
                "Tokyo",
                "Aizu",
                "Kobe")
        val images = listOf(
                R.drawable.giraffe,
                R.drawable.hippo,
                R.drawable.lion,
                R.drawable.ic_launcher_round)

        data class UserData(val name: String, val loc: String, val imageId: Int)
        val users = List(names.size) { i -> UserData(names[i], location[i], images[i])}

        data class ViewHolder(val nameTextView: TextView, val locTextView: TextView, val userImgView: ImageView)

        class UserListAdapter(context: Context, users: List<UserData>) : ArrayAdapter<UserData>(context, 0, users) {
            private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
                var view = convertView
                var holder: ViewHolder

                if (view == null) {
                    view = layoutInflater.inflate(R.layout.list_item, parent, false)
                    holder = ViewHolder(
                            view.findViewById(R.id.nameTextView),
                            view.findViewById(R.id.locTextView),
                            view.findViewById(R.id.userImgView)
                    )
                    view.tag = holder
                } else {
                    holder = view.tag as ViewHolder
                }

                val user = getItem(position) as UserData
                holder.nameTextView.text = user.name
                holder.locTextView.text = user.loc
                holder.userImgView.setImageBitmap(BitmapFactory.decodeResource(context.resources, user.imageId))


                return view
            }
        }
        //listViewにUserListAdapterをセット
        val arrayAdapter = UserListAdapter(this, users)
        val listView : ListView = findViewById(R.id.listView)
        listView.adapter = arrayAdapter
    }

    /*override fun onResume() {
        super.onResume()

        val intent = intent
        val action = intent.action

        if (action == Intent.ACTION_VIEW) {
            val uri: Uri? = intent.data
            if (uri != null) {

                // 受け取るURIが
                // simplemio://callback#access_token=token&state=success&token_type=Bearer&expires_in=7776000
                // となっていて，正しくエンコードできないので # を ? に置き換える

                var uriStr = uri.toString()
                uriStr = uriStr.replace('#', '?')
                val validUri = Uri.parse(uriStr)

                val token = validUri.getQueryParameter("access_token")
                val state = validUri.getQueryParameter("state")

                if (state != "success") {
                    Toast.makeText(this, "正しく認証することができませんでした．", Toast.LENGTH_LONG).show()
                } else {
                    MioUtil.saveToken(this, token)
                    Toast.makeText(this, "トークン:" + token, Toast.LENGTH_LONG).show()
                }
            }
        }
    }*/



    inner class HitAPITask : AsyncTask<Uri, String, String>() {

        override fun doInBackground(vararg params: Uri?): String {
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
                val uri = URI(params[0].toString())
                connection = uri as HttpURLConnection
                connection.connect()
                  //ここで指定したAPIを叩いてみてます。


                //ここから叩いたAPIから帰ってきたデータを使えるよう処理していきます。

                //とりあえず取得した文字をbufferに。
                val stream = connection
                reader = BufferedReader(InputStreamReader(stream as InputStream))
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
                //val mapper = jacksonObjectMapper()

                //val login = mapper.readValue<login>(jsonText)

                val parentJsonObj = JSONArray(jsonText)
                Log.d("CHECK2", parentJsonObj.toString())

                val parentJSONObject = parentJsonObj.getJSONObject(1)

                val login: String = parentJSONObject.getString("login")
                Log.d("CHECK3", login.format())


                fun getParseItems(jsonObject: JsonObject) {

                }

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







