package wacode.yuki.sougotwitterb

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import butterknife.bindView
import twitter4j.AsyncTwitterFactory
import twitter4j.TwitterAdapter
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken

class OAuthActivity : AppCompatActivity() {
    private val button_browser: Button by bindView(R.id.button_start_login)
    private val button_oauth: Button by bindView(R.id.button_login)
    private val editText: EditText by bindView(R.id.edit_pin_code)

    private var mRequestToken: RequestToken? = null
    internal val factory = AsyncTwitterFactory()
    internal val twitter = factory.instance

    private val listener = object : TwitterAdapter() {
        override fun gotOAuthRequestToken(token: RequestToken?) {
            mRequestToken = token
        }

        override fun gotOAuthAccessToken(token: AccessToken?) {
            val intent = Intent()
            intent.putExtra(EXTRA_ACCESS_TOKEN, token!!.token)
            intent.putExtra(EXTRA_ACCESS_TOKEN_SECRET, token.tokenSecret)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)

        val intent = intent
        val consumer_key = intent.getStringExtra(EXTRA_CONSUMER_KEY)
        val consumer_secret = intent.getStringExtra(EXTRA_CONSUMER_SECRET)
        twitter.addListener(listener)
        twitter.setOAuthConsumer(consumer_key, consumer_secret)
        twitter.getOAuthRequestTokenAsync()

        button_browser.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse(mRequestToken!!.authorizationURL))
            startActivity(intent)
        }
        button_oauth.setOnClickListener {
            twitter.getOAuthAccessTokenAsync(mRequestToken,editText.text.toString())
        }
    }

    companion object {
        val EXTRA_CONSUMER_KEY = "consumer_key"
        val EXTRA_CONSUMER_SECRET = "consumer_secret"
        val EXTRA_ACCESS_TOKEN = "access_token"
        val EXTRA_ACCESS_TOKEN_SECRET = "access_token_secret"
    }
}
