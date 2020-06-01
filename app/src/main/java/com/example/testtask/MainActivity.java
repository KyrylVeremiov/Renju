package com.example.testtask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.Objects;

import edu.washington.cs.touchfreelibrary.sensors.CameraGestureSensor;
import edu.washington.cs.touchfreelibrary.sensors.ClickSensor;

public class MainActivity extends AppCompatActivity implements Observer<Pair<Integer, Integer>>, CameraGestureSensor.Listener, ClickSensor.Listener{
        private static int height=Game.Field.height;
        private static int width=Game.Field.width;
        private static Button[][] buttons;
        private static TableLayout tableLayout;
        private static int buttonWidth=40;
        private static int buttonHeight=40;
        private static int currentCellX=7;
        private static int currentCellY=7;
        public static String TAG="MyMainActivityLogs";
        private static CameraGestureSensor mGestureSensor;
        private static boolean mOpenCVInitiated;
        /** OpenCV library initialization. */
        private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        mOpenCVInitiated = true;
                        CameraGestureSensor.loadLibrary();
                        mGestureSensor.start(); 	// your main gesture sensor object
                    } break;
                    default:
                    {
                        super.onManagerConnected(status);
                    } break;
                }
            }
        };
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
//            mGestureSensor = new CameraGestureSensor(this);
//            mGestureSensor.addGestureListener(this);
//            mGestureSensor.addClickListener(this);
            tableLayout=findViewById(R.id.tableLayout);
            height=Game.Field.height;
            width=Game.Field.width;
            buttons=new Button[height][width];
            Game.uiUpdater.removeObserver(MainActivity.this);
            Game.uiUpdater.observe(MainActivity.this,MainActivity.this);

            for (int i = 0; i < height; i++) {
                final TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                for (int j = 0; j < width; j++) {
                    CreateButton(i,j,false);
                    tableRow.addView(buttons[i][j],j);
                }
                tableLayout.addView(tableRow,i);
            }
        }

        @Override
        public void onPause() {
//            mGestureSensor.stop();
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
//            mGestureSensor.start();
        }


        public void CreateButton(int i,int j,boolean currentButton){
            char cell=Game.Field.field[i][j];
            if(currentButton){
                buttons[i][j]=new Button(this,null,0,R.style.current_button);
                buttons[currentCellX][currentCellY]=new Button(this,null,0,R.style.player1_button);
            }
            else if(cell==Game.getPlayer1().moveSymbol){
                buttons[i][j]=new Button(this,null,0,R.style.player1_button);
            }
            else if(cell==Game.getPlayer2().moveSymbol){
                buttons[i][j]=new Button(this,null,0,R.style.player2_button);
            }
            else{
                buttons[i][j]=new Button(this,null,0,R.style.empty_button);
            }
            buttons[i][j].setText(Character.valueOf(cell).toString());
            buttons[i][j].setWidth(buttonWidth);
            buttons[i][j].setHeight(buttonHeight);
            final int x = i;
            final int y = j;
            buttons[i][j].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(Game.doMove(x,y)){
                            CreateButton(x,y,false);
                            updateUI(x,y);
                            Log.d(TAG,"move is done: x: "+x+", y: "+y);
                        }
                        else{
                            Toast.makeText(MainActivity.this,"Move is incorrect",Toast.LENGTH_LONG).show();
                            Log.d(TAG,"move is incorrect: x: "+x+", y: "+y);
                        }
                    }catch (Exception e){
                        Log.d(TAG,"move is failed: x: "+x+", y: "+y, e);
                    }
                }
            });
    }

    public void updateUI(int x,int y){
        TableRow tableRow= (TableRow) tableLayout.getChildAt(x);
        tableRow.removeViewAt(y);
        tableRow.addView(buttons[x][y],y);
    }

    @Override
    public void onChanged(Pair<Integer, Integer> pair) {
        Log.d(TAG,"Bot have done a move: " + pair.first+" "+pair.second);
        if(Game.playingWithBot  && pair.first>=0 && pair.second>=0){
            CreateButton(pair.first,pair.second,false);
            updateUI(pair.first,pair.second);
        }
        else if(pair.first<0 && pair.second<0){
            Toast.makeText(MainActivity.this,"It's draw!",Toast.LENGTH_LONG).show();
            Log.d(TAG,"It's draw! (from MainActivity class)");
        }
        else if(pair.first<0 ||pair.second<0){
            Toast.makeText(MainActivity.this,"Winner: "+ Game.winner.name,Toast.LENGTH_LONG).show();
            Log.d(TAG,"Winner: "+ Game.winner.name+ "(from MainActivity class)");
        }
    }

    public void newCurrentCell(int newCurrentCellX, int newCurrentCellY){
        CreateButton(newCurrentCellX,newCurrentCellY,true);

        currentCellY=newCurrentCellY;
        currentCellX=newCurrentCellX;

    }
    @Override
    public void onGestureUp(CameraGestureSensor caller, long gestureLength) {
        if(currentCellY>0){
            newCurrentCell(currentCellX,currentCellY-1);
        }
    }

    @Override
    public void onGestureDown(CameraGestureSensor caller, long gestureLength) {

    }

    @Override
    public void onGestureLeft(CameraGestureSensor caller, long gestureLength) {

    }

    @Override
    public void onGestureRight(CameraGestureSensor caller, long gestureLength) {

    }

    @Override
    public void onSensorClick(ClickSensor caller) {

    }
}