package wacode.yuki.sougotwitterb

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import butterknife.bindView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import wacode.yuki.newontapusha.Utils.PrefUtils
import wacode.yuki.sougotwitterb.API.TwitterApi
import wacode.yuki.sougotwitterb.Entity.TwitterOAuth


class MainActivity : AppCompatActivity() {

    private val button: Button by bindView(R.id.button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        if(checkUserOauth()) startOauth()
        startApi("konnnitiha","eguti")
        setViews()
    }

    private fun setViews(){
        button.setOnClickListener {
            startApi(PrefUtils[this,USERID,0])
        }
    }

    private fun startOauth(){
        val intent = Intent(this, OAuthActivity::class.java)
        intent.putExtra(OAuthActivity.EXTRA_CONSUMER_KEY,resources.getText(R.string.APIKEY))
        intent.putExtra(OAuthActivity.EXTRA_CONSUMER_SECRET,resources.getText(R.string.APISECRET))
        startActivityForResult(intent,TWITTERRESULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        val intent:Intent = data

        val u_token = intent.getStringExtra(OAuthActivity.EXTRA_ACCESS_TOKEN)
        val u_secret = intent.getStringExtra(OAuthActivity.EXTRA_ACCESS_TOKEN_SECRET)

        PrefUtils.put(this,USERTOKEN,u_token)
        PrefUtils.put(this,USERSECRET,u_secret)

        startApi(u_token, u_secret)
    }

    private fun startApi(u_token:String,u_secret:String){
        val twitterApi = getRest().create(TwitterApi::class.java)
        twitterApi.createUser(generateJson(u_token,u_secret))
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(object :Subscriber<TwitterOAuth>(){
            override fun onCompleted() {
                Log.d(TAG, "COMPLETED")
            }

            override fun onError(p0: Throwable?) {
                p0!!.printStackTrace()
                Log.d(TAG, "ERROR")
            }

            override fun onNext(p0: TwitterOAuth?) {
                PrefUtils.put(this@MainActivity,USERID,p0!!.id)
            }

        })
    }

    private fun startApi(u_id: Int){
        val twitterApi = getRest().create(TwitterApi::class.java)
        twitterApi.postRequest(generateJson(u_id))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :Subscriber<TwitterOAuth>(){
                    override fun onCompleted() {
                        Log.d(TAG, "COMPLETED")
                    }

                    override fun onError(p0: Throwable?) {
                        p0!!.printStackTrace()
                        Log.d(TAG, "ERROR")
                    }

                    override fun onNext(p0: TwitterOAuth?) {
                        Log.d(TAG,"NEXT")
                    }

                })
    }

    private fun generateJson(u_token: String,u_secret: String)= "{\"$JSONKEY_UTOKEN\":\"$u_token\",\"$JSONKEY_USECRET\":\"$u_secret\"}"
    private fun generateJson(u_id:Int)="{\"$USERID\":\"$u_id\"}"

    private fun getRest() = Retrofit.Builder().baseUrl(ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create(Gson())).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build()
    private fun checkUserOauth() = PrefUtils[this,shouldOauth,true]

    companion object{
        private val TAG = MainActivity.javaClass.simpleName
        private val shouldOauth = "userShouldDoOauth"
        private val TWITTERRESULT =3
        private val USERTOKEN = "userToken"
        private val USERSECRET = "userSecret"
        private val USERID ="user_id"
        private val ENDPOINT ="http://kakijin.com"
        private val JSONKEY_UTOKEN ="access_token"
        private val JSONKEY_USECRET ="access_token_secret"
    }
}
