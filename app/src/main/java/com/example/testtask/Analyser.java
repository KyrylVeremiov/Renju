package com.example.testtask;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class Analyser {
    private static int cellsToWin = 5;
    private static int potentialCount = 3;
    private static double[][] ATTACK_WEIGHT = new double[cellsToWin + 1][potentialCount];
    private static double breakPointWeight =100;

    public static void setWeights() {
        ATTACK_WEIGHT[0][0] = 0;
        ATTACK_WEIGHT[0][1] = 0;
        ATTACK_WEIGHT[0][2] = 0;

        ATTACK_WEIGHT[1][0] = 0;
        ATTACK_WEIGHT[2][0] = 0;
        ATTACK_WEIGHT[3][0] = 0;
        ATTACK_WEIGHT[4][0] = 0;
        ATTACK_WEIGHT[5][0] = 200;

        ATTACK_WEIGHT[1][1] = 0.1;
        ATTACK_WEIGHT[2][1] = 2;
        ATTACK_WEIGHT[3][1] = 4;
        ATTACK_WEIGHT[4][1] = 6;
//        ATTACK_WEIGHT[4][1] = 10;
        ATTACK_WEIGHT[5][1] = 200;

        ATTACK_WEIGHT[1][2] = 0.5;
        ATTACK_WEIGHT[2][2] = 5;
        ATTACK_WEIGHT[3][2] = 7;
//        ATTACK_WEIGHT[2][2] = 25;
//        ATTACK_WEIGHT[3][2] = 50;
        ATTACK_WEIGHT[4][2] = 100;
        ATTACK_WEIGHT[5][2] = 200;
    }

    private static class Attack {
        private int cells;
        private int potential;
        private int divider;

        Attack() {
            cells = 0;
            potential = 0;
            divider = 1;
        }

        Attack(int c, int p, int d) {
            this.cells = c;
            this.potential = p;
            this.divider = d;
        }

        public double countWeight(int cells, int potential, int divider) {
            return ATTACK_WEIGHT[cells][potential] / divider;
        }
    }

    public static class LineChecker {
        private static char substitutableSymbol;
        private static ArrayDeque<Attack> attacks;
        private static Attack currentAttack;
        private static int attackPlace;
        private int iterator;
        private boolean sixthCell;


        LineChecker() {
            currentAttack = new Attack();
            attacks = new ArrayDeque<Attack>();
            iterator = 1;
            sixthCell = false;
        }

        ArrayList<Attack> getAttacks(int x, int y, char symbol, int dx, int dy) {
            refreshLineChecker(symbol);
            for (int cellToCheckX = x + dx, cellToCheckY = y + dy; Math.abs(x - cellToCheckX) <= 5 && Math.abs(y - cellToCheckY) <= 5; cellToCheckX += dx, cellToCheckY += dy) {
                if (!checkCell(cellToCheckX, cellToCheckY)) break;
                iterator++;
            }
            returnCenter(x, y);
            for (int cellX = x - dx, cellY = y - dy; Math.abs(x - cellX) <= 5 && Math.abs(y - cellY) <= 5; cellX -= dx, cellY -= dy) {
                if (!checkCell(cellX, cellY)) break;
                iterator++;
            }
            attacks.add(currentAttack);

            return filter();
        }

        ArrayList<Attack> filter(){
            ArrayList<Attack> attacks1=new ArrayList<>();
            if(attackPlace>=5)
                while (attacks.size()!=0){
                    Attack attack=attacks.poll();
                    if(!(attack.cells==0 || attack.potential==0 && attack.cells<5)){
                        if(attack.cells>5)attack.cells=5;
                        attacks1.add(attack);
                    }
                }
            return attacks1;
        }

        void refreshLineChecker(char symbol){
            substitutableSymbol = symbol;
            currentAttack = new Attack();
            currentAttack.cells++;
            attacks = new ArrayDeque<Attack>();
            iterator = 1;
            sixthCell = false;
        }

        void returnCenter(int x, int y) {
            if (sixthCell) {
                checkCell(x + 6, y + 6);
            }
            iterator = 1;
            sixthCell = false;
            attacks.add(currentAttack);
            currentAttack = attacks.poll();
        }

        boolean checkCell(int x, int y) {
            if (x >= 0 && x < Game.Field.width && y >= 0 && y < Game.Field.height) {
                if (sixthCell && Game.Field.field[x][y] != substitutableSymbol) {
                    currentAttack.potential++;
                } else if (Game.Field.field[x][y] == Game.Field.e) {
                    currentAttack.potential++;
                    attacks.add(currentAttack);
                    currentAttack = new Attack();
                    currentAttack.potential++;
                    currentAttack.divider++;
                } else if (Game.Field.field[x][y] == substitutableSymbol) {
                    currentAttack.cells++;
                    if (iterator == 5) sixthCell = true;
                }
                else {//if(Game.Field.field[x][y]!=substitutableSymbol)
//                    attacks.add(currentAttack);
//                    currentAttack=new Attack();
                    return false;
                }
                attackPlace++;
                return true;
            } else {
                return false;
            }
        }
    }

    public static class GameAnalyser {
        private static final String TAG = "AnalyzerLogs";

        static boolean finishGameCheck(int x, int y) {
            return LineWinCheck(x,y,1,0) ||LineWinCheck(x,y,0,1)|| LineWinCheck(x,y,1,1)||LineWinCheck(x,y,1,-1);
        }

        static boolean LineWinCheck(int x, int y, int dx, int dy){
            int score=1;
            score+= CheckSubLine(x,y,dx,dy);
            score+=CheckSubLine(x,y,-dx,-dy);
            return score >= 5;
        }

        static int CheckSubLine(int x, int y, int dx, int dy){
            char symbol=Game.Field.field[x][y];
            int CellX=x+dx;
            int CellY=y+dy;
            int score=0;
            while (CellX>=0 && CellX<Game.Field.width && CellY>=0 && CellY<Game.Field.height&& Game.Field.field[CellX][CellY]==symbol){
                CellX+=dx;
                CellY+=dy;
                score++;
            }
            return score;
        }

        public static double[][] countFieldWeights(char playerSymbol, char opponentSymbol){
            setWeights();
            int height=Game.Field.height;
            int width=Game.Field.width;
            double[][] field=new double[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if(Game.Field.field[i][j]==Game.Field.e)
                       field[i][j]=countCellWeight(i,j,playerSymbol,opponentSymbol);
                    else
                        field[i][j]=0;
                }
            }
            return field;
        }

        public static double countCellWeight(int x,int y,char playerSymbol,char opponentSymbol){
            Pair<ArrayList<ArrayList<Attack>>,ArrayList<ArrayList<Attack>>> attacks=getAllAttacks(x,y,playerSymbol,opponentSymbol);
            if(attacks==null){
                return 0;
            }
            double weight=countPlayerWeight(attacks.first,true);
            weight+=countPlayerWeight(attacks.second,false);
            return weight;
        }

        static Pair<ArrayList<ArrayList<Attack>>,ArrayList<ArrayList<Attack>>> getAllAttacks(int x, int y, char playerSymbol, char opponentSymbol) {
            LineChecker lineChecker=new LineChecker();
            // Lines: - | / \
            ArrayList<ArrayList<Attack>> activePlayerAttacks=new ArrayList<>();
            char substitutableSymbol=playerSymbol;

            activePlayerAttacks.add(lineChecker.getAttacks(x,y,substitutableSymbol,1,0));
            activePlayerAttacks.add(lineChecker.getAttacks(x,y,substitutableSymbol,0,1));
            activePlayerAttacks.add(lineChecker.getAttacks(x,y,substitutableSymbol,1,1));
            activePlayerAttacks.add(lineChecker.getAttacks(x,y,substitutableSymbol,1,-1));

            ArrayList<ArrayList<Attack>> passivePlayerAttacks=new ArrayList<>();
            substitutableSymbol=opponentSymbol;

            passivePlayerAttacks.add(lineChecker.getAttacks(x,y,substitutableSymbol,1,0));
            passivePlayerAttacks.add(lineChecker.getAttacks(x,y,substitutableSymbol,0,1));
            passivePlayerAttacks.add(lineChecker.getAttacks(x,y,substitutableSymbol,1,1));
            passivePlayerAttacks.add(lineChecker.getAttacks(x,y,substitutableSymbol,1,-1));
            return Pair.create(activePlayerAttacks,passivePlayerAttacks);
        }
        static boolean isBreakPoint(ArrayList<Attack> attacks){
            Attack mainAttack=new Attack();
            for (Attack attack:
                 attacks) {
                if(attack.divider==1 && attack.cells>mainAttack.cells){
                    mainAttack=attack;
                }
            }
            if(mainAttack.potential==2 && mainAttack.cells>=3)return true;
            else if(mainAttack.cells>=4)return true;
            for (Attack attack :
                    attacks) {
                int score = mainAttack.cells;
                if(attack.divider==2){
                    if(mainAttack.potential==2 && attack.potential==2){
                        score++;
                    }
                    if(score+attack.cells>=4)return true;
                }
            }
            return false;
        }

        static double countPlayerWeight(ArrayList<ArrayList<Attack>> allAttacks, boolean turnToDoMove){
            int breakpoints=0;
            double weight=0;
            for (ArrayList<Attack> attacks :
                    allAttacks) {
                if(isBreakPoint(attacks)){
                    weight+=100;
                    Log.d(TAG,"isBreakpoint");
//                    if(++breakpoints==2){
//                        weight+=100;
//                    }
                }
                for (Attack attack :
                        attacks) {
                    //win
                    if(attack.cells==5 && turnToDoMove){
                        weight+=100;
                    }
                    weight+=ATTACK_WEIGHT[attack.cells][attack.potential]/attack.divider;
                }
            }
            return weight;
        }
    }
}