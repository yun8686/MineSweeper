package yun.app.minesweeper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ryo on 2015/11/03.
 */
public class GameMain extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    final float VIEW_WIDTH = 800;
    final float VIEW_HEIGHT = 800;
    float scale;
    float move_x=100,move_y;

    private int drag_x,drag_y;
    Thread thread;
    boolean threadIsRunning;

    public int SIZE_X;
    public int SIZE_Y;
    public int SIZE_BLOCK = 100;
    Mass[][] board;
    Context context;
    SurfaceHolder surfaceHolder;
    GameMain(Context context,int size_x,int size_y){
        super(context);
        this.context = context;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        SIZE_X = size_x;
        SIZE_Y = size_y;
        board = new Mass[SIZE_X][SIZE_Y];
        Random rnd = new Random();
        //盤面の初期化
        for(int i=0;i<SIZE_X;i++){
            for(int j=0;j<SIZE_Y;j++){
                board[i][j] = new Mass();
                if(rnd.nextInt(10)==0){
                    board[i][j].isBomb = true;
                }
            }
        }
        //数字の配置
        for(int i=0;i<SIZE_X;i++){
            for(int j=0;j<SIZE_Y;j++) {
                setAround(board,i,j);
            }
        }
    }

    private void setAround(Mass[][] board,int x,int y){
        Mass mass = board[x][y];
        for(int i=x-1;i<=x+1;i++){
            for(int j=y-1;j<=y+1;j++){
                try {
                    if (mass != board[i][j]) {
                        mass.addAround(board[i][j]);
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    Log.e("範囲外","は無視する");
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        scale = Math.min(getWidth() / VIEW_WIDTH, getHeight() / VIEW_HEIGHT);
        Log.e("SCALE", "" + scale);
        //スレッドスタート
        thread = new Thread(this);
        thread.start();
        threadIsRunning = true;

//        Matrix matrix = new Matrix();
  //      img_close.setHeight(50);
    //    img_close.setWidth(50);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        threadIsRunning = false;
    }

    @Override
    public void run() {
        while (threadIsRunning) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            draw();
        }
    }


    //画像読み込み
    Resources res = this.getContext().getResources();
    Bitmap img_close = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.close), SIZE_BLOCK, SIZE_BLOCK, true);
    Bitmap[] img_open = {Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open0), SIZE_BLOCK, SIZE_BLOCK, true),
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open1), SIZE_BLOCK, SIZE_BLOCK, true),
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open2), SIZE_BLOCK, SIZE_BLOCK, true),
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open3), SIZE_BLOCK, SIZE_BLOCK, true),
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open4), SIZE_BLOCK, SIZE_BLOCK, true),
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open5), SIZE_BLOCK, SIZE_BLOCK, true),
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open6), SIZE_BLOCK, SIZE_BLOCK, true),
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open7), SIZE_BLOCK, SIZE_BLOCK, true),
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.open8), SIZE_BLOCK, SIZE_BLOCK, true)};
    Bitmap img_bomb = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.bomb),SIZE_BLOCK,SIZE_BLOCK,true);
    private void draw(){
        // lockCanvas()メソッドを使用して、描画するためのCanvasオブジェクトを取得する
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.translate(move_x,move_y);
        canvas.scale(scale, scale); // 端末の画面に合わせて拡大・縮小する

        // 画面全体を一色で塗りつぶすdrawColor()メソッドを用いて画面全体を白に指定
        canvas.drawColor(Color.BLUE);


        // Paintクラスをインスタンス化
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        // Canvas.drawRect()を呼び出すと、正方形や長方形を描画することが可能になる
        // （left：左辺,top：上辺,right：右辺,bottom：下辺,Paintインスタンス）
        for(int i=0;i<SIZE_X;i++){
            for(int j=0;j<SIZE_Y;j++){
                float x = SIZE_BLOCK*i;
                float y = SIZE_BLOCK*j;
                Mass mass = board[i][j];
                if(mass.isOpen) {
                    if(mass.isBomb) {
                        canvas.drawBitmap(img_bomb, x, y, paint);
                    }else {
                        canvas.drawBitmap(img_open[mass.bombCount], x, y, paint);
                        //canvas.drawText(""+mass.bombCount,x,y,paint);
                    }
                }else{
                    canvas.drawBitmap(img_close, x, y, paint);
                }
            }
        }


        // Canvasオブジェクトへの描画完了したら、unlockCanvasAndPost()メソッドを呼び出し、引数にはCanvasオブジェクトを指定する
        surfaceHolder.unlockCanvasAndPost(canvas);

    }

    float base_x,base_y;
    float pinchStartDist;
    PointF pinchStartPoint = new PointF();
    private float _fPinchScale = 1.0f,baseScale = 1.0f;
    int TouchMode;
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_DRAG = 1;
    private static final int TOUCH_ZOOM = 2;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        int pointerCnt = event.getPointerCount();
        if(pointerCnt == 1) {
            if(TouchMode == TOUCH_ZOOM) {
                baseScale = scale;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        TouchMode = TOUCH_NONE;
                        break;
                }
            }else {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        base_x = x;
                        base_y = y;

                        int i = (int) ((x - move_x) / (SIZE_BLOCK * scale));
                        int j = (int) ((y - move_y) / (SIZE_BLOCK * scale));
                        try {
                            //board[i][j].isOpen = true;
                            board[i][j].open();
                        } catch (Exception e) {
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        move_x -= base_x - x;
                        move_y -= base_y - y;
                        base_x = x;
                        base_y = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        TouchMode =TOUCH_NONE;
                        break;
                }
            }
        }else if(pointerCnt >= 2){
            Log.e("ERROR","2TOUCH" + event.getAction());
            Log.e("ACTION_POINTER_DOWN",""+MotionEvent.ACTION_POINTER_DOWN);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    pinchStartDist = getDistance(event);
                    if(pinchStartDist> 50f)
                    {
                        getCenterPoint(event, pinchStartPoint);
                        baseScale = scale;
                        TouchMode = TOUCH_ZOOM;
                    }
                    break;
                //ピンチ中
                case MotionEvent.ACTION_MOVE:
                    if(TouchMode == TOUCH_ZOOM && pinchStartDist > 0)
                    {
                        PointF	pt = new PointF();
                        getCenterPoint(event, pt);
                        _fPinchScale = getDistance(event) / pinchStartDist;
                        scale = Math.max(0.5f, baseScale*_fPinchScale);
                        invalidate();
                    }
                    break;
            }
        }
        return true;

    }

    //ピンチ距離取得用
    private float	getDistance(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return android.util.FloatMath.sqrt(x * x + y * y);
    }

    //ピンチ開始座標取得用
    private void getCenterPoint(MotionEvent event,PointF pt)
    {
        pt.x = (event.getX(0) + event.getX(1)) * 0.5f;
        pt.y = (event.getY(0) + event.getY(1)) * 0.5f;
    }

    private class Mass{
        public boolean isBomb;
        public boolean isOpen;

        public int bombCount = 0;
        public ArrayList<Mass> around = new ArrayList<Mass>();
        Mass(){

        }

        public void open(){
            synchronized (OpenLocker.lock) {
                if(this.isOpen)return;
                this.isOpen = true;
                if (this.bombCount == 0) {
                    ArrayList<Mass> center = new ArrayList<Mass>();
                    center.add(this);
                    while (center.size() > 0) {
                        Mass top = center.get(center.size() - 1);
                        center.remove(center.size() - 1);
                        for (Mass mass : top.around) {
                            if (!mass.isOpen) {
                                mass.isOpen = true;
                                if (mass.bombCount == 0)
                                    center.add(mass);
                            }
                        }
                    }
                }
            }
        }
        public void addAround(Mass mass){
            this.around.add(mass);
            if(mass.isBomb){
                bombCount++;
            }
        }
    }
}

class OpenLocker {  //排他制御
    static Object lock = new Object();
}
