package wacode.yuki.sougotwitterb.API

import retrofit2.Call
import retrofit2.http.*
import rx.Observable
import wacode.yuki.sougotwitterb.Entity.TwitterOAuth


/**
 * Created by Yuki on 2016/04/30.
 */
interface TwitterApi {
    @Headers("Content-Type: application/json")
    @POST("/api/users")
    fun createUser(@Body sJson:String):Observable<TwitterOAuth>

    @Headers("Content-Type: application/json")
    @POST("/api/requests/create")
    fun postRequest(@Body sJson: String):Observable<TwitterOAuth>

    @Headers("Content-Type: application/json")
    @POST("/api/users/{id}/requests/create")
    fun postRequest(@Body sJson: String,@Path("id")id:Int):Observable<TwitterOAuth>
}