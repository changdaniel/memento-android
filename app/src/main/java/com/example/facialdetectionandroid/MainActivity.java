package com.example.facialdetectionandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import dalvik.system.InMemoryDexClassLoader;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static String TAG = "MainActivity";
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray;

    CascadeClassifier faceCascade;


    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:{
                    javaCameraView.enableView();

                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    File mCascadeFile = new File(cascadeDir,"haarcascade_frontalface_alt.xml");

                    FileOutputStream os = new FileOutputStream(mCascadeFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1)
                    {
                        os.write(buffer, 0, bytesRead);
                    }

                    is.close();
                    os.close();

                    // Load the cascade classifier
                    faceCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    faceCascade.load( mCascadeFile.getAbsolutePath() );
                    if (faceCascade.empty()) {
                        Log.e(TAG, "Failed to load cascade classifier");
                        faceCascade = null;
                    }




                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };

    static
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onPause(){
        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }
    @Override
    protected void onDestroy(){
        if(javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (OpenCVLoader.initDebug())
        {
            Log.d(TAG, "OpenCV is Configured or Connected Succesfully.");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else
        {
            Log.d(TAG, "OpenCV is NOT working or loaded.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallBack);
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);


        return detectAndDisplay(mRgba, faceCascade);
    }

    public Mat detectAndDisplay(Mat frame, CascadeClassifier faceCascade){

        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(frameGray, frameGray);
        // -- Detect faces

        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);
        List<Rect> listOfFaces = faces.toList();

        for (Rect face : listOfFaces) {
            Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0, 255), 3);
            //Mat faceROI = frameGray.submat(face);

        }

        return frame;

    }
}
