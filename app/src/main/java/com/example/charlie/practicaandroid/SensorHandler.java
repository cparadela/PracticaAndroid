package com.example.charlie.practicaandroid;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.DecimalFormat;

/**
 * Created by migue on 21/01/2017.
 */

public class SensorHandler implements SensorEventListener {
    // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private final float EPSILON=  0.000001f;

    public float [] rotationCurrent = new float[9];
    public SensorHandler(){
        rotationCurrent[0]=1.0f;
        rotationCurrent[4]=1.0f;
        rotationCurrent[8]=1.0f;
    }

    public void onSensorChanged(SensorEvent event) {
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;

            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        //rotationCurrent = rotationCurrent * deltaRotationMatrix;
        float [] tempMatrix = new float[9];
        for (int i=0; i<3; i++ ) {
            for (int j=0; j<3; j++ ) {
                tempMatrix[i*3 + j]=0;
                for (int z = 0; z < 3; z++) {
                    tempMatrix[i*3 + j] += rotationCurrent[i*3+z]*deltaRotationMatrix[z*3+j];

                }
            }

        }

        rotationCurrent=tempMatrix;
        Log.d("Rotation Current:", " "+rotationCurrent[0]+" "+rotationCurrent[1]+" "+rotationCurrent[2]);
        float[] gyroscopeOrientation = new float[3];
        SensorManager.getOrientation(rotationCurrent,
                gyroscopeOrientation);

        Log.d( "Orientacion","\n\nOrientation X (Roll) :" + (new DecimalFormat("#.##").format((double)gyroscopeOrientation[0]))
                + "\n\n" + "Orientation Y (Pitch) :"
                + (new DecimalFormat("#.##").format((double)gyroscopeOrientation[1]))+ "\n\n"
                + "Orientation Z (Yaw) :" + (new DecimalFormat("#.##").format((double)gyroscopeOrientation[2])));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}

