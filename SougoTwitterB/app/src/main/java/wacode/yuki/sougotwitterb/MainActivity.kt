package wacode.yuki.sougotwitterb

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import butterknife.bindView
import wacode.yuki.newontapusha.Utils.PrefUtils

class MainActivity : AppCompatActivity() {

    private val button: Button by bindView(R.id.button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(checkUserOauth()) startOauth()
        setViews()
    }

    private fun setViews(){
        button.setOnClickListener {
            //非同期
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

        Log.d("test",u_token)
        Log.d("test",u_secret)
    }

    private fun checkUserOauth() = PrefUtils[this,shouldOauth,true]

    companion object{
        private val shouldOauth = "userShouldDoOauth"
        private val TWITTERRESULT =3
        private val USERTOKEN = "userToken"
        private val USERSECRET = "userSecret"
    }
}
