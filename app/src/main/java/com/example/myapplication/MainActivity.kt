package com.example.myapplication

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var URL="http://m.martroo.com/"


    //웹<-웹뷰->앱 통신 테스트 URL
    //private var URL="file:///android_asset/exam.html"
    private var backBtnTime:Long=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("tak","onCreate")

        logMyToken(); //토큰 로그 찍기




        /** 파이어베이스 Dynamic Link 사용
         *  파이어베이스 dynamic Link와 별도로
         * Manifest에 scheme를 설정해줘야 한다.
         */
        //SMS나 카톡에서 URL를 클릭하면 URL 로그 찍기
        if(Intent.ACTION_VIEW.equals(intent.action)){
            var uri=intent.data
            Log.d("tak","클릭한 URL(SMS, 카톡): "+ uri.toString())

            //URL 갱신
            //URL= uri.toString()
        }




        /**
         웹뷰 세팅 및 구현
         */
        //이거 굳이안써줘도 웹뷰내에서 쿠키설정되는거같음
        //CookieManager.getInstance().setAcceptThirdPartyCookies(webview,true)

        var webSettings=webview.settings
        webSettings.javaScriptEnabled=true //자바 스크립트로 이루어져있는 기능을 사용하려면 true로 설정
        webSettings.useWideViewPort=true //html 컨텐츠가 웹뷰에 맞게 나타나도록함
        webSettings.setSupportZoom(true) //확대 축소 기능을 사용할수있는 속성
        webSettings.domStorageEnabled=true //로컬스트리지 사용여부 설정(팝업창들을 하루동안 보지않기)
        webSettings.setSupportMultipleWindows(true)
        webSettings.javaScriptCanOpenWindowsAutomatically=true

        //웹->앱의 코드를 사용가능하게 설정한다.
            webview.addJavascriptInterface(WebAppInterface(this),"Android")

        //웹 콘솔을 로그캣에 찍을수있게 설정
            webview.webChromeClient=object:WebChromeClient(){
                override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                    //Log.d("tak", cm?.message() + " -- From line " + cm?.lineNumber() + " of " + cm?.sourceId() );
                    return true
                }
            }


            webview.webViewClient=MyWebViewClient(this)



        //뷰만 하드웨어 가속, (Manifest에서 정의하면 전체 애플리케이션 하드웨어가속, 특정 액티비티만 하드웨어가속시킬수도 있다.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
       // webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.loadUrl(URL);

        //Log.d("tak",CookieManager.getInstance().getCookie(webview.url));

        //앱스토어<->현재앱 버젼 체크
        val marketVersionChecker=AppVersionChecker(this)
        marketVersionChecker.start()

    }

    fun logMyToken(){
        //파이어베이스 토큰 조회
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(object: OnCompleteListener<InstanceIdResult> {
                override fun onComplete(task: Task<InstanceIdResult>) {
                    if(task.isSuccessful){
                        var token=task.getResult()?.token
                        Log.d("tak","token: "+token)
                    }
                }
            });
    }

    //백그라운드시: FCM Push알림시 스플래쉬 화면-> URL 넘겨줌(제목이랑 내용은 Firebase에서 자체적으로 보여줌)
    //포그라운드시: FCM Push알림시 FirebaseMessaging onRecieve에서 받음-> URL 넘겨줌
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var pushedURL=intent?.getStringExtra("URL")
        if(pushedURL!=null)  {
            Log.d("tak","FCM!")
            URL= pushedURL.toString()
            webview.loadUrl(URL)
        }


    }


    override fun onBackPressed() {
        //뒤로가기버튼을 누를때, 웹뷰에서 역시 뒤로갈수있는 상황이면-> 전 페이지로 이동
        if(webview.canGoBack() ){
            webview.goBack()

        }
        //뒤로가기 2번
        else {
            val curTime= System.currentTimeMillis()
            if(curTime<=backBtnTime+2000){
                super.onBackPressed()
            }
            else {
                backBtnTime = curTime
                Toast.makeText(this,"한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show()
            }
        }

    }




}