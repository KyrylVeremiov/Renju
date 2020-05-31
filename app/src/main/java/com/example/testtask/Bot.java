package com.example.testtask;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;

import java.util.Arrays;
import java.util.Objects;

public class Bot extends Game.Player {
    private static String botName = "Bot";
    private static char moveSymbol;
    public static String TAG="MyBotLogs";


    Bot(char moveSymbol) {
        super(moveSymbol, botName);
        Bot.moveSymbol = moveSymbol;
    }

    public static Pair<Integer, Integer> makeMove() {
        int height = Game.Field.height;
        int weight = Game.Field.width;
        int xMax = height / 2;
        int yMax = weight / 2;
        double[][] field = Analyser.GameAnalyser.countFieldWeights(Bot.moveSymbol, Game.getPlayer1().moveSymbol);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < weight; j++) {
                if (field[xMax][yMax] < field[i][j]) {
                    xMax = i;
                    yMax = j;
                }
            }
        }
        Log.d(TAG,"Field:\n"+ Arrays.deepToString(field));
        return Pair.create(xMax,yMax);
    }

}
