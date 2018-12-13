package com.coffe.shentao.bitmapcompress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import net.bither.util.NativeUtil;

import id.zelory.compressor.Compressor;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private Button weixi,qurity,size,screen,luban,compressor;
    Bitmap bitmap;

    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        weixi=findViewById(R.id.wixi);
        qurity=findViewById(R.id.qurity);
        size=findViewById(R.id.size);
        screen=findViewById(R.id.screen);
        luban=findViewById(R.id.luban);
        compressor=findViewById(R.id.compressor);

        weixi.setOnClickListener(this);
        qurity.setOnClickListener(this);
        size.setOnClickListener(this);
        screen.setOnClickListener(this);
        luban.setOnClickListener(this);
        compressor.setOnClickListener(this);
        //预处理的图片 ---然后创建每一个 自己的文件
        bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.foodbackground);
        layout=findViewById(R.id.layout);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.wixi:
                File saveFile = new File(Environment.getExternalStorageDirectory(), "last.jpg");
                Log.e("===compressImage===", "====开始==压缩==saveFile==" + saveFile.getAbsolutePath());
                NativeUtil.compressBitmap(bitmap, saveFile.getAbsolutePath());
                Log.e("===compressImage===", "====完成==压缩==saveFile==" + saveFile.getAbsolutePath());
                break;
            case R.id.qurity:
                File saveFile1 = new File(Environment.getExternalStorageDirectory(), "quilty.jpg");
                NativeUtil.compressImageToFile(bitmap,saveFile1);
                break;
            case R.id.size:
                File saveFile2 = new File(Environment.getExternalStorageDirectory(), "size.jpg");
                NativeUtil.compressBitmapToFile(bitmap,saveFile2);
                break;
            case R.id.screen:
                File saveFile3 = new File(Environment.getExternalStorageDirectory(), "pix.jpg");
                Log.e("===compressImage===", "采样率压缩找不到这个代码里面写死的图片哦~~~~");
                NativeUtil.pixeCompressBitmap("/sdcard/foodbackground.jpg",saveFile3);
                break;
            case R.id.luban:
                Luban.with(this)
                        .load("/sdcard/city.jpg")
                        .ignoreBy(100)
                        .setTargetDir("/sdcard/data")
                        .filter(new CompressionPredicate() {
                            @Override
                            public boolean apply(String path) {
                                //动态图片不可以
                                return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                            }
                        })
                        .setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                                // TODO 压缩开始前调用，可以在方法内启动 loading UI
                                Log.v("Tanrong","鲁班压缩开始");
                            }

                            @Override
                            public void onSuccess(File file) {
                                // TODO 压缩成功后调用，返回压缩后的图片文件
                                Log.v("Tanrong","鲁班压缩成功");
                            }

                            @Override
                            public void onError(Throwable e) {
                                // TODO 当压缩过程出现问题时调用
                                Log.v("Tanrong","鲁班压缩失败");
                            }
                        }).launch();
                break;
            case R.id.compressor:
              new Compressor(this)
                        .compressToFileAsFlowable(new File("/sdcard/city.jpg"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<File>() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void accept(File file) {
                            Log.v("Tanrong",file.getPath()+"---"+file.length()/1024);
                               bitmap=BitmapFactory.decodeFile(file.getAbsolutePath()) ;
                                layout.setBackground(new BitmapDrawable(bitmap));
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                throwable.printStackTrace();
                                Log.v("Tanrong", "Compressor压缩失败");
                            }
                        });
                break;

        }

    }
}
