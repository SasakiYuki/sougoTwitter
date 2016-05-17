package wacode.yuki.sougotwitterb

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import butterknife.bindView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import wacode.yuki.newontapusha.Utils.PrefUtils
import wacode.yuki.sougotwitterb.API.TwitterApi
import wacode.yuki.sougotwitterb.Entity.TwitterOAuth
import java.io.IOException


class MainActivity : AppCompatActivity(){

    private var gcm: GoogleCloudMessaging? = null
    private var registerTask: AsyncTask<Void, Void, String>? = null
    private var progressDialog:ProgressDialog? = null

    private val button: Button by bindView(R.id.button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressDialog = ProgressDialog(this)
        if(userOauthEntered) startOauth()
        setViews()
    }

    private fun setViews(){
        button.setOnClickListener {
            gcmRegister()
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
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        val intent: Intent = data

        val u_token = intent.getStringExtra(OAuthActivity.EXTRA_ACCESS_TOKEN)
        val u_secret = intent.getStringExtra(OAuthActivity.EXTRA_ACCESS_TOKEN_SECRET)

        PrefUtils.put(this, USERTOKEN, u_token)
        PrefUtils.put(this, USERSECRET, u_secret)
        PrefUtils.put(this,shouldOauth,false)

        startApi(u_token, u_secret)
    }

    /**
     * API回り
     */
    private fun startApi(u_token:String,u_secret:String){
        val twitterApi = restClient.create(TwitterApi::class.java)
        twitterApi.createUser(generateJson(u_token,u_secret))
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(object :Subscriber<TwitterOAuth>(){

            override fun onCompleted() {
                Log.d(TAG, "COMPLETED")
                progressDialog!!.dismiss()
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

    private fun startApi(u_id: Int,regiId:String){
        val twitterApi = restClient.create(TwitterApi::class.java)
        twitterApi.postRequest(generateJson(regiId),u_id)
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

    /**
     * GCM回り
     */
    private fun gcmRegister() {
        var registrationId = ""
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this)
            registrationId = getRegistrationId()
        } else {
            val textView = findViewById(R.id.textView) as TextView
            textView.text = "この端末には対応してません、申し訳ありません。"
        }

        if (registrationId == "") {
            registerTask = object : AsyncTask<Void, Void, String>() {
                override fun doInBackground(vararg params: Void): String? {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(this@MainActivity)
                    }
                    try {
                        registrationId = gcm!!.register(SENDER_ID)
                        storeRegistrationId(registrationId)
                        startApi(PrefUtils[this@MainActivity,USERID,2],registrationId)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    return null
                }
                override fun onPostExecute(resultPostExecute: String) {
                    registerTask = null
                }
            }
            registerTask!!.execute(null, null, null)
        } else {
            startApi(PrefUtils[this,USERID,2],PrefUtils[this,REGISTERID,""])
        }
    }

    private fun checkPlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                val textView = findViewById(R.id.textView) as TextView
                textView.text = "この端末には対応してません、申し訳ありません。"
            }
            return false
        }
        return true
    }

    private fun getRegistrationId(): String {
        var registrationId = PrefUtils[this, PROPERTY_REG_ID, ""]
        val registeredVersion = PrefUtils[this, PROPERTY_APP_VERSION, Integer.MIN_VALUE]
        val currentVersion = appVersion
        if (registeredVersion != currentVersion) {
            registrationId = ""
        }
        return registrationId
    }

    private fun storeRegistrationId(regId: String) {
        PrefUtils.put(this, PROPERTY_REG_ID, regId)
        PrefUtils.put(this, PROPERTY_APP_VERSION, appVersion)
    }

    private fun generateJson(u_token: String,u_secret: String)= "{\"$JSONKEY_UTOKEN\":\"$u_token\",\"$JSONKEY_USECRET\":\"$u_secret\"}"
    private fun generateJson(regiId: String)="{\"$REGISTERID\":\"$regiId\"}"

    private val restClient:Retrofit
        get() = Retrofit.Builder().client(getClient()).baseUrl(ENDPOINT).addConverterFactory(GsonConverterFactory.create(Gson())).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build()
    private val userOauthEntered:Boolean
        get() = PrefUtils[this,shouldOauth,true]
    private val appVersion:Int
        get() = this.packageManager.getPackageInfo(this.packageName,0).versionCode
    private fun getClient():OkHttpClient{
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    override fun onDestroy() {
        if (registerTask != null) {
            registerTask!!.cancel(true)
        }
        gcm!!.close()
        super.onDestroy()
    }

    companion object{
        private val TAG = MainActivity.javaClass.simpleName
        private val shouldOauth = "userShouldDoOauth"
        private val TWITTERRESULT =3
        private val USERTOKEN = "userToken"
        private val USERSECRET = "userSecret"
        private val USERID ="user_id"
        private val REGISTERID ="registration_id"
        private val ENDPOINT ="http://kakijin.com"
        private val JSONKEY_UTOKEN ="access_token"
        private val JSONKEY_USECRET ="access_token_secret"
        private val PROPERTY_REG_ID = "registration_id"
        private val PROPERTY_APP_VERSION = "appVersion"
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private val SENDER_ID = "406400740658"
    }
}
