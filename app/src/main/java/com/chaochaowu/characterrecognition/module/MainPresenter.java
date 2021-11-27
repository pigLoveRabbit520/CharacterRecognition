package com.chaochaowu.characterrecognition.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.chaochaowu.characterrecognition.apiservice.BaiduOCRService;
import com.chaochaowu.characterrecognition.bean.AccessTokenBean;
import com.chaochaowu.characterrecognition.bean.RecognitionResultBean;
import com.chaochaowu.characterrecognition.utils.RegexUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author chaochaowu
 * @Description : MainPresenter
 * @class : MainPresenter
 * @time Create at 6/4/2018 4:21 PM
 */


public class MainPresenter implements MainContract.Presenter {

    private MainContract.View mView;
    private Context ctx;
    private BaiduOCRService baiduOCRService;

    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String API_KEY = "EqfYRa3DH1zy4aNoynIAskZ3";
    private static final String SECRET_KEY = "vxoIqFkde6TtYlP6GGi7tjuryO0Vt6SO";

    public MainPresenter(MainContract.View mView, Context ctx) {

        this.mView = mView;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://aip.baidubce.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        baiduOCRService = retrofit.create(BaiduOCRService.class);

    }


    @Override
    public void getAccessToken() {

        baiduOCRService.getAccessToken(CLIENT_CREDENTIALS,API_KEY,SECRET_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccessTokenBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull AccessTokenBean accessTokenBean) {
                        Log.e("Access token",accessTokenBean.getAccess_token());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Access token","error");
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }



    @Override
    public void getRecognitionResultByImage(Bitmap bitmap) {
        String encodeResult = bitmapToString(bitmap);

        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setVertexesLocation(true);
        param.setRecognizeGranularity(GeneralParams.GRANULARITY_SMALL);
        param.setImageFile(new File(filePath));

        OCR.getInstance(this.ctx).getRecognitionResultByImage(ACCESS_TOKEN,encodeResult)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RecognitionResultBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull RecognitionResultBean recognitionResultBean) {
                        Log.e("onnext",recognitionResultBean.toString());

                        ArrayList<String> wordList = new ArrayList<>();
                        List<RecognitionResultBean.WordsResultBean> wordsResult = recognitionResultBean.getWords_result();
                        for (RecognitionResultBean.WordsResultBean words:wordsResult) {
                            wordList.add(words.getWords());
                        }

                        ArrayList<String> numbs = RegexUtils.getNumbs(wordList);
                        StringBuilder s = new StringBuilder();

                        for (String numb : numbs) {
                            s.append(numb + "\n");
                        }

                        mView.updateUI(s.toString());

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("onerror",e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }



    private String bitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


}
