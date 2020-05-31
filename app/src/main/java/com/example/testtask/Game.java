package com.example.testtask;

import android.app.Application;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

public class Game extends Application {
    //SharedPref
    //'o'!='x' set
    //4 attacks в одну точку
    // settings start
    //start game blockMoves=false activePlayer=player1, Field()
    public static String TAG="MyGameLogs";
    public static MutableLiveData<Pair<Integer,Integer>> uiUpdater=new MutableLiveData<>();
    public static boolean touchFreeGame=true;


    private static Player player1;
    private static Player player2;
    public static Player activePlayer;
    public static Player winner;

    public static boolean draw;
    public static int countOfMoves;
    public static boolean gameFinished;
    public static boolean playingWithBot;
    private static boolean movesAreBlocked;


    @Override
    public void onCreate() {
        uiUpdater=new MutableLiveData<>();
        player1=new Player('X',"Player1");
        player2=new Bot('O');
        activePlayer=player1;
        playingWithBot=true;
        draw=false;
        gameFinished = false;
        movesAreBlocked=false;
        touchFreeGame=true;
        new Field();
        countOfMoves=0;
        super.onCreate();
    }

    public static class Field{
        static int height=15;
        static int width=15;
        static long movesCount=0;
        static char[][] field;
        static char e=' ';//empty cell
        public Field(int h,int w) {
            height = h;
            width=w;
            field=CreateField(h,w);
        }

        Field(){
            field=CreateField(height,width);
        }

        char[][] CreateField(int h, int w){
            char[][]f=new char[height][width];
            for (int i = 0; i <height ; i++) {
                for (int j = 0; j < width; j++) {
                    f[i][j]=e;
                }
            }
            return f;
        }
    }

    public static class Player{
        char moveSymbol;
        String name;
        Pair<Integer, Integer> move;
        Player(char moveSymbol,String name){
            this.name=name;
            this.moveSymbol=moveSymbol;
        }
    }

    public static boolean doMove(int x, int y){
        if(movesAreBlocked){
            Log.d(Game.TAG,"Move is blocked");
            return false;
        }
        if(countOfMoves>= Field.width*Field.height){
            uiUpdater.setValue(Pair.create(-1,-1));
            draw=true;
            gameFinished =true;
            return false;
        }
        if(Field.field[x][y]==Field.e){
            Field.field[x][y]= activePlayer.moveSymbol;
            if(Analyser.GameAnalyser.finishGameCheck(x,y)){
                gameFinished =true;
                movesAreBlocked=true;
                winner=activePlayer;
                if(winner==player1){
                    uiUpdater.setValue(Pair.create(1,-1));
                }
                else{
                    uiUpdater.setValue(Pair.create(-1,1));
                }
                Log.d(TAG,"Winner (from Game class): "+ winner.name);
            }
            else{
                switchPlayer();
            }
            countOfMoves++;
            return true;
        }
        else {
            return false;
        }
    }

    public static void switchPlayer(){
        if(activePlayer==player2){
            activePlayer=player1;
        }
        else{
            activePlayer=player2;
            if(playingWithBot && !movesAreBlocked){
                movesAreBlocked=true;
                player2.move=((Bot)player2).makeMove();
                movesAreBlocked=false;
                if(Game.doMove(player2.move.first, player2.move.second)){
                    movesAreBlocked=true;
                    uiUpdater.setValue(player2.move);
                    movesAreBlocked=false;
                }
                else{
                    switchPlayer();
                    Log.d(TAG,"Bot had incorrect move");
                }
            }
        }
        if(gameFinished)
            movesAreBlocked=true;
    }
    public static Player getPlayer1(){
        return player1;
    }

    public static Player getPlayer2(){
        return player2;
    }
}
