package com.zymixx.cameratesttask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.textclassifier.ConversationActions;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zymixx.cameratesttask.Event.AutoUpdateCamers;
import com.zymixx.cameratesttask.Event.DrawableFromURL;
import com.zymixx.cameratesttask.Event.GetXYcamBox;
import com.zymixx.cameratesttask.Event.RecreateMain;
import com.zymixx.cameratesttask.Event.SetDrawableInGui;
import com.zymixx.cameratesttask.Event.UpdateCamers;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import static android.content.ContentValues.TAG;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import static com.zymixx.cameratesttask.EnterInAccount.arrayOfCamInfo;
import static com.zymixx.cameratesttask.WorkAPI.updateCamers;

public class MainActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    View ViewForListener;
    static int countUserCam = 0;
    static int widthOfCam;
    static int paddingOfCam;
    ArrayList<ImageView>camersImageArray = new ArrayList<>();
    ArrayList<CamersBox>camersBoxPosArray = new ArrayList<>();
    ArrayList<LinearLayout> linerInfoLayoutArray = new ArrayList<>();
    ArrayList<Drawable> urlDrawableArray = new ArrayList<>();
    static int firstTime = 0; // костыль для проверки что пользователь впервые вошёл


    //для работы с перемещением камер
    ImageView shadow;
    ImageView viewForMove;
    LinearLayout LinLayoutForMove;
    boolean readyForReplase = false;
    static boolean longClic = false;
    static boolean avalible = true;
    boolean notFirst = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (firstTime == 0){ //TODO костыльная проверка на первый запуск
            firstTime++;
            Intent intent = new Intent(this, EnterInAccount.class);
            startActivity(intent);
        } else {
            countUserCam = 0;
            EventBus.getDefault().register(this);
            makeGUI();
            EventBus.getDefault().post(new GetXYcamBox());
           // EventBus.getDefault().post(new AutoUpdateCamers()); //TODO здесь включалось автообновление
            EventBus.getDefault().post(new DrawableFromURL());
        }
    }

    public void makeGUI(){
        frameLayout = findViewById(R.id.main_frame);
        frameLayout.setBackground(getDrawable(R.drawable.bg_grad));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int camInLine = 2;
        int dWidth = displayMetrics.widthPixels;
        int dHeight = displayMetrics.heightPixels;
        if (dWidth > dHeight) { camInLine += 2;} //проверка на поворот экрана
        paddingOfCam = dWidth / 8 / (camInLine + 1);
        widthOfCam = (dWidth - paddingOfCam * (camInLine + 1)) / camInLine;

        for (int i = 0; i < arrayOfCamInfo.size(); i++) {
            createCamersBox(); }

        shadow = new ImageView (this);
        shadow.setLayoutParams(new ViewGroup.LayoutParams(widthOfCam, widthOfCam));
        shadow.setBackgroundColor(Color.BLACK);
        shadow.setAlpha(0.5f);
        shadow.setVisibility(View.INVISIBLE);
        frameLayout.addView(shadow);

        ViewForListener = new View(this);
        ViewForListener.setLayoutParams(frameLayout.getLayoutParams());
        frameLayout.addView(ViewForListener);
        ViewForListener.setOnTouchListener(new OnChangePlace());
        ViewForListener.setOnLongClickListener(new OnChangePlace());

        fillCamersBox ();

    }

    public void createCamersBox(){
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(countCamersLayoutParms(paddingOfCam,50, widthOfCam, widthOfCam, imageView));
        imageView.setBackgroundColor(Color.RED);

        camersImageArray.add(imageView);
        frameLayout.addView(imageView);

    }

    public FrameLayout.LayoutParams countCamersLayoutParms (int left, int top, int height, int width, ImageView imageView){
        left = left * (countUserCam%2+1)  + (countUserCam%2 * width);
        top = top * (countUserCam/2+1) + (countUserCam/2 * width);

        FrameLayout.LayoutParams params = new FrameLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.leftMargin = left; //  координаты в контейнере по X
        params.topMargin = top; //  координаты в контейнере по Y
        params.height = height;
        params.width = width;


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout);

        CamersBox cb = new CamersBox(imageView, countUserCam, imageView.getX(), imageView.getY(), params, linearLayout,arrayOfCamInfo.get(countUserCam));
        camersBoxPosArray.add(cb);
        linerInfoLayoutArray.add(linearLayout);
        countUserCam++;
        return params; // возврощает паметры во фрейме
    }

    public void fillCamersBox (){
        for (CamersBox camersBox: camersBoxPosArray){
            int id = camersBox.camersID;
            LinearLayout linLyout =  camersBox.linearLayoutForInfo;
            linLyout.addView(createTextView(arrayOfCamInfo.get(id).camera_name));
            linLyout.addView(createTextView("Model: " + arrayOfCamInfo.get(id).camera_model));
            linLyout.addView(createTextView("Public: " + arrayOfCamInfo.get(id).camera_public));
            linLyout.addView(createTextView("Connected: " + arrayOfCamInfo.get(id).camera_connected_server));
            linLyout.bringToFront();
        }
    }

    

    //TODO подобного и не должно было быть, но когда я начал это реализоваывать постоянно выскакивала ошибка
    //TODO "A connection to was leaked. Did you forget to close a response body?"
    @Subscribe (threadMode = ThreadMode.ASYNC)
    public void setPreviewCamers (DrawableFromURL drawableFromURL){
            urlDrawableArray.removeAll(urlDrawableArray);// для теста изображения
            urlDrawableArray.add(loadImageFromWebOperations("https://i.ytimg.com/vi/zLm4O3y99cw/maxresdefault.jpg"));
            urlDrawableArray.add(loadImageFromWebOperations("https://i.ytimg.com/vi/eRpLtjQ-BHI/maxresdefault.jpg"));
            urlDrawableArray.add(loadImageFromWebOperations("https://i.ytimg.com/vi/aSGKjZjMDMM/maxresdefault.jpg"));
            urlDrawableArray.add(loadImageFromWebOperations("https://www.geocam.ru/images/photo/my/as/myasnikova-partizanskaya.jpg"));
            urlDrawableArray.add(loadImageFromWebOperations("https://i.ytimg.com/vi/zM9ar6MnIUo/maxresdefault.jpg"));




        for (CamersBox camer: camersBoxPosArray ){
            if (camer.cameraInfo.preview_url.equals("null")){
                camer.drawablePrevie = (urlDrawableArray.get(camer.camersID));
            } else {
                camer.drawablePrevie = (loadImageFromWebOperations(camer.cameraInfo.preview_url));
            }
        }
        EventBus.getDefault().post(new SetDrawableInGui());
    }
    URL urlCorent;
    InputStream is;
    Drawable d;
    public Drawable loadImageFromWebOperations(String url) {
        try {
            urlCorent = new URL(url);
            urlCorent.openConnection().connect();
             is = (InputStream) urlCorent.getContent();
             d = Drawable.createFromStream(is, "src name");

            return d;
        } catch (Exception e) {
            return null;
        }
        finally {
            try {
                is.close();
                ((InputStream) urlCorent.getContent()).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void setDrawableInGUI (SetDrawableInGui setDrawableInGui){
        for (CamersBox camer: camersBoxPosArray ) {
        camer.imageViewPreview.setImageDrawable(camer.drawablePrevie);
            camer.imageViewPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        }




    public TextView createTextView (String string) {
        TextView textView = new TextView(this);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(6, 0, 0, 0);
        textView.setText(string);
        return textView;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.update_menu:
                Toast.makeText(this, "update_menu", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new UpdateCamers());
                System.out.println("update_menu");
                return true;
            case R.id.settings_menu:
                Toast.makeText(this, "settings_menu", Toast.LENGTH_SHORT).show();
                System.out.println("settings_menu");
                return true;
            case R.id.profile_menu:
                Toast.makeText(this, "profile_menu", Toast.LENGTH_SHORT).show();
                System.out.println("profile_menu");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //
    // TODO Перенос записи на новое место при долгом касании

    public class OnChangePlace implements View.OnLongClickListener , View.OnTouchListener {
        @Override
        public boolean onLongClick(View v) {
            if (readyForReplase & notFirst){
            if (avalible) {
                longClic = true;
                shadow.bringToFront();
                viewForMove.bringToFront();
                LinLayoutForMove.bringToFront();
                shadow.setX(viewForMove.getX() - 10);
                shadow.setY(viewForMove.getY() + 10);
                shadow.setVisibility(View.VISIBLE);
            }
            avalible = false;
            System.out.println("LONG CLICK");
            }
            return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (readyForReplase){
            switch (event.getAction()){
                case MotionEvent.ACTION_MOVE:
                    if (longClic){
                        viewForMove.setX(event.getX()-widthOfCam/2);
                        viewForMove.setY(event.getY()-widthOfCam/2);
                        LinLayoutForMove.setX(event.getX()-widthOfCam/2);
                        LinLayoutForMove.setY(event.getY()-widthOfCam/2);
                        shadow.setX(event.getX()-widthOfCam/2 - 20);
                        shadow.setY(event.getY()-widthOfCam/2 + 24);
                        cheakBox (event.getX(), event.getY());
                          shadow.setVisibility(View.VISIBLE);
                    }
                    //  run = true;
                    return false;
                case MotionEvent.ACTION_DOWN:
                    notFirst = true;
                    System.out.println("ACTION DOWN");
                    print(event.getX(), event.getY());

                case  MotionEvent.ACTION_UP:
                    longClic = false;
                    avalible = true;
                    shadow.setVisibility(View.INVISIBLE);
                    onStopMove ();
                    //  print(event.getX(), event.getY());
                    return false;}
            }

            return false;
        }
    }

    public void print (float x, float y){ //Находим камеру которой коснулись
        for (ImageView img: camersImageArray){
            if (img.getX() < x & (img.getX() + widthOfCam) > x & img.getY() < y & (img.getY()+widthOfCam) > y){
                viewForMove = img;
                for (CamersBox camersBox: camersBoxPosArray){
                    if (camersBox.imageViewPreview.equals(img)){
                        LinLayoutForMove = camersBox.linearLayoutForInfo;
                    }
                }
            }
        }
    }

    //img camerBOX = то куда навели
    //viewForMove oldPos = то что двигают
    public void cheakBox (float x, float y){
       // camersImageArray.remove(viewForMove);
        for (ImageView img: camersImageArray){
            if (img.getX()+90 < x & (img.getX() + widthOfCam - 90) > x & img.getY()+90 < y & (img.getY()+ widthOfCam - 90) > y
                    & !(img.equals(viewForMove))){
                for (CamersBox camerBox: camersBoxPosArray){
                    if (camerBox.imageViewPreview.equals(img)){
                     //   img.setLayoutParams(camerBox.layoutParams);
                        for (CamersBox oldPos: camersBoxPosArray){
                            if (oldPos.imageViewPreview.equals(viewForMove)){
                                //img.setLayoutParams(oldPos.layoutParams);
                                img.setX(oldPos.xCamPos);
                                img.setY(oldPos.yCamPos);
                                camerBox.linearLayoutForInfo.setX(oldPos.xCamPos);
                                camerBox.linearLayoutForInfo.setY(oldPos.yCamPos);
                                float oldX = oldPos.xCamPos;
                                float oldY = oldPos.yCamPos;
                                ViewGroup.LayoutParams oldLP = oldPos.layoutParams;
                                oldPos.xCamPos = camerBox.xCamPos;
                                oldPos.yCamPos = camerBox.yCamPos;
                                oldPos.layoutParams = camerBox.layoutParams;
                                camerBox.xCamPos = oldX;
                                camerBox.yCamPos = oldY;
                                camerBox.layoutParams = oldLP;
                            }
                        }
                    }
                }
            }
        }//
      //
    }
    public void onStopMove (){
        for (CamersBox camersBox: camersBoxPosArray){
            if (camersBox.imageViewPreview.equals(viewForMove)){
                viewForMove.setX(camersBox.xCamPos);
                viewForMove.setY(camersBox.yCamPos);
                LinLayoutForMove.setX(camersBox.xCamPos);
                LinLayoutForMove.setY(camersBox.yCamPos);
            }
        }
    }

    public class CamersBox {
        Drawable drawablePrevie;
        CameraInfo cameraInfo;
        ImageView imageViewPreview;
        int camersID;
        float xCamPos;
        float yCamPos;
        LinearLayout linearLayoutForInfo;
        ViewGroup.LayoutParams layoutParams;

        CamersBox (ImageView iv, int id, float x, float y, ViewGroup.LayoutParams lp, LinearLayout ll, CameraInfo ci){
            this.imageViewPreview = iv;
             this.camersID = id;
             this.xCamPos = x;
             this.yCamPos = y;
             this.layoutParams = lp;
             this.linearLayoutForInfo = ll;
             this.cameraInfo = ci;

        }
    }

    @Override
    protected void onPause() {
        autoRunUpdate = false;
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        autoRunUpdate = true;
        super.onPostResume();
    }

    boolean autoRunUpdate = true;
    boolean needUpdate = true;

    @Subscribe (threadMode =  ThreadMode.ASYNC)
    public void autoUpdate(AutoUpdateCamers autoUpdateCamers){
        try {
            Thread.sleep(10000);
                if (needUpdate){
                updateCamers();
                    Thread.sleep(4000);
                EventBus.getDefault().post(new RecreateMain());
                    System.out.println("Обновил всё");
                } else {
                    needUpdate = true;
                }
        } catch (InterruptedException e) {
            Log.e(TAG, "autoUpdate: ", e );
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void reCreateMain (RecreateMain recreateMain){
        MainActivity.this.recreate();
    }

    @Subscribe (threadMode = ThreadMode.ASYNC)
    public void updateCamersNow (UpdateCamers updateCamers){
        needUpdate = false;
        updateCamers();
        MainActivity.this.recreate();
    }

    @Subscribe ( threadMode = ThreadMode.ASYNC)
    public void getXYcamBox (GetXYcamBox getXYcamBox){
        try {
            Thread.sleep(1800); //дилей чтобы оно прорисовалось
        } catch (InterruptedException e) {
            Log.e(TAG, "getXYcamBox: ",e);        }
        for (CamersBox camersBox: camersBoxPosArray){
            camersBox.xCamPos = camersBox.imageViewPreview.getX();
            camersBox.yCamPos = camersBox.imageViewPreview.getY();
        }
        readyForReplase = true;
    }


}