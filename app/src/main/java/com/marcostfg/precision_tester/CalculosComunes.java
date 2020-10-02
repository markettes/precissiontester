package com.marcostfg.precision_tester;

import android.location.Location;

import java.util.ArrayList;

public class CalculosComunes {

    public static int secsFromStart(ArrayList<Location> list, int secs) {
        double timeLimit = list.get(0).getElapsedRealtimeNanos() + secs * 1000000000L;
        for (int index = 1; index<list.size(); index++){
            if (list.get(index).getElapsedRealtimeNanos() > timeLimit)
                return index;
        }
        return list.size();
    }

    public static double average(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum/array.length;
    }

    public static double maxError(double[] array) {
        double temp = array[0];
        for (int i = 1; i < array.length; i++) {
            if(temp < array[i]){
                temp = array[i];
            }
        }
        return temp;
    }

    public static double minError(double[] array) {
        double temp = array[0];
        for (int i = 1; i < array.length; i++) {
            if(temp > array[i]){
                temp = array[i];
            }
        }
        return temp;
    }
}
