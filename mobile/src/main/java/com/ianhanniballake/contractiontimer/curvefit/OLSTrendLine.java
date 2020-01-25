package com.ianhanniballake.contractiontimer.curvefit;

import android.util.Log;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.Arrays;

public abstract class OLSTrendLine implements TrendLine {

    final static String TAG = "OLSTrendLine";

    RealMatrix coef = null; // will hold prediction coefs once we get values

    protected abstract double[] xVector(double x); // create vector of values from x
    protected abstract boolean logY(); // set true to predict log of y (note: y must be positive)

    @Override
    public void setValues(double[] y, double[] x) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("The numbers of y and x values must be equal (%d != %d)",y.length,x.length));
        }
        double[][] xData = new double[x.length][];
        for (int i = 0; i < x.length; i++) {
            // the implementation determines how to produce a vector of predictors from a single x
            xData[i] = xVector(x[i]);
        }
        if(logY()) { // in some models we are predicting ln y, so we replace each y with ln y
            y = Arrays.copyOf(y, y.length); // user might not be finished with the array we were given
            for (int i = 0; i < x.length; i++) {
                y[i] = Math.log(y[i]);
            }
        }
        Log.i(TAG, "fit X: ");
        for(int p = 0; p < x.length; p++) {
            StringBuilder sb = new StringBuilder();
            for(int p1 = 0; p1 < xData[p].length; p1++) {
                sb.append(xData[p][p1] + "\t");
            }
            Log.i(TAG, " " + sb.toString());
        }
        Log.i(TAG, "fit y: " );
        StringBuilder sb = new StringBuilder();
        for(int p = 0; p < y.length; p++) {
            sb.append(y[p] + "\t");
        }
        Log.i(TAG, " " + sb.toString());
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.newSampleData(y, xData);
        ols.setNoIntercept(true); // let the implementation include a constant in xVector if desired
        ols.newSampleData(y, xData); // provide the data to the model
        coef = MatrixUtils.createColumnRealMatrix(ols.estimateRegressionParameters()); // get our coefs
        Log.i(TAG, "fit coefs: " + coef.toString());
    }

    @Override
    public double predict(double x) {
        double yhat = coef.preMultiply(xVector(x))[0]; // apply coefs to xVector
        if (logY()) yhat = (Math.exp(yhat)); // if we predicted ln y, we still need to get y
        return yhat;
    }
}