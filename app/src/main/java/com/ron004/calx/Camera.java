package com.ron004.calx;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telecom.Call;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.function.UnaryOperator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Camera.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Camera#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Camera extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private CameraSource mCameraSource;
    private TextRecognizer mTextRecognizer;
    private SurfaceView mSurfaceView;
    private TextView mTextView;
    private Button mCapture;
    private Button mCancel;
    private ImageView mImage;
    private Spinner restaurant_name;
    private ArrayAdapter<CharSequence> adapter10;
    SharedPreferences sharedpreferences;
    private String category;
    int totalCals;
    int usedCals;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Camera() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFeed.
     */
    // TODO: Rename and change types and number of parameters
    public static Camera newInstance(String param1, String param2) {
        Camera fragment = new Camera();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_frag, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mSurfaceView = view.findViewById(R.id.camera);
        mTextView = view.findViewById(R.id.text_view);
        mCapture = view.findViewById(R.id.capture);
        mCancel = view.findViewById(R.id.cancel);
        mImage = view.findViewById(R.id.imageView);
        mImage.setVisibility(View.INVISIBLE);

        restaurant_name = view.findViewById(R.id.restaurantname);

        adapter10 = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.restaurant_names, android.R.layout.simple_spinner_item);
        adapter10.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        restaurant_name.setAdapter(adapter10);
        restaurant_name.setVisibility(View.VISIBLE);

        sharedpreferences = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        if(sharedpreferences.contains("Name")) {
            totalCals = sharedpreferences.getInt("TotalCals", 0);
            String diettype = sharedpreferences.getString("Diet", "");
            usedCals = sharedpreferences.getInt("UsedCals", 0);

            String getdate = sharedpreferences.getString("Date", "");
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String today = df.format(c);

            if(today.equals(getdate)) {

            } else {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                DatabaseHelper db = new DatabaseHelper(getActivity());
                db.writeTrend(getdate, usedCals);
                editor.putInt("UsedCals", 0);
                editor.putString("Date", today);
                editor.commit();
            }

            if (diettype.equals("Anything")) {
                category = "";
            } else if (diettype.equals("Vegetarian")) {
                category = "VEG";
            } else if (diettype.equals("Eggetarian")) {
                category = "EGG";
            }
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startTextRecognizer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraSource != null) {
            startTextRecognizer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraSource != null){
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            try {
                mCameraSource.release();
            } catch (NullPointerException ignored) {  }
            mCameraSource = null;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public void startTextRecognizer() {
        mTextRecognizer = new TextRecognizer.Builder(getActivity().getApplicationContext()).build();

        if (!mTextRecognizer.isOperational()) {
            Toast.makeText(getActivity().getApplicationContext(), "Oops ! Not able to start the text recognizer ...", Toast.LENGTH_SHORT).show();
        } else {
            mCameraSource = new CameraSource.Builder(getActivity().getApplicationContext(), mTextRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(15.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            mCameraSource.start(mSurfaceView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    if (mCameraSource != null) {
                        try {
                            mCameraSource.release();
                        } catch (NullPointerException ignored) {  }
                        mCameraSource = null;
                    }
                }
            });

            mTextRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                }
            });

            mCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(restaurant_name.getSelectedItem().toString().equals("Select Restaurant")) {
                        Toast.makeText(getActivity(), "Please Set Profile and Select Restaurant before Capturing!", Toast.LENGTH_LONG).show();
                    }
                    else {
                        mCameraSource.takePicture(new CameraSource.ShutterCallback() {
                            @Override
                            public void onShutter() {

                            }
                        }, new CameraSource.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] bytes) {
                                restaurant_name.setVisibility(View.INVISIBLE);
                                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Bitmap bitmap = rotate(bitmapImage, 90);
                                mImage.setVisibility(View.VISIBLE);
                                mImage.setImageBitmap(bitmap);

                                Frame imageFrame = new Frame.Builder()
                                        .setBitmap(bitmap)
                                        .build();

                                SparseArray<TextBlock> textBlocks = mTextRecognizer.detect(imageFrame);
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < textBlocks.size(); ++i) {
                                    TextBlock item = textBlocks.valueAt(i);
                                    if (item != null && item.getValue() != null) {
                                        for (Text line : item.getComponents()) {
                                            //extract scanned text lines here
                                            stringBuilder.append(line.getValue().toUpperCase() + "\n");
                                        }

                                    }
                                }

                                final String fullText = stringBuilder.toString();
                                System.out.println("text detected: " + fullText);
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            mTextView.setText(fullText);
                                            getData();
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });

            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImage.setVisibility(View.INVISIBLE);
                    restaurant_name.setVisibility(View.VISIBLE);
                    mTextView.setText("");
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void getData() {
        String getvalue = restaurant_name.getSelectedItem().toString();
        String retrievedText = mTextView.getText().toString();
        DatabaseHelper db = new DatabaseHelper(getActivity());
        Cursor c;
        if(category.equals("")) {
            c = db.getAllItems(getvalue);
        } else {
            c = db.getItems(getvalue, category);
        }
        ArrayList<String> getItemsList = new ArrayList<String>();
        ArrayList<String> getCalList = new ArrayList<String>();
        ArrayList<String> matchedItemList = new ArrayList<String>();
        ArrayList<Integer> matchedCalList = new ArrayList<Integer>();
        while(!c.isAfterLast()) {
            getItemsList.add(c.getString(1));
            getCalList.add(c.getString(2));
            c.moveToNext();
        }

        getItemsList.replaceAll(new MyOperator());

        for(int i=0; i< getItemsList.size(); i++) {
            if(retrievedText.contains(getItemsList.get(i))) {
                matchedItemList.add(getItemsList.get(i));
                matchedCalList.add(Integer.valueOf(getCalList.get(i)));
            }
        }
        System.out.println(matchedItemList);
        System.out.println(matchedCalList);
        suggestItem(matchedItemList, matchedCalList, totalCals);
    }

    public void suggestItem(ArrayList<String> items, ArrayList<Integer> calItems, final int totalCal) {
        final int breakfast = (int)(((double)15/100)*totalCal);
        int lunch = (int)(((double)50/100)*totalCal);
        int snack = (int)(((double)15/100)*totalCal);
        int dinner = (int)(((double)20/100)*totalCal);

        int suggest = 0;
        String suggestion = "";

        DateFormat df = new SimpleDateFormat("HH");
        int currTime = Integer.parseInt(df.format(Calendar.getInstance().getTime()));

        int minval = 1000;
        int diff = 0;
        if(currTime >= 5 && currTime <= 11) {
            for(int i=0; i< calItems.size(); i++) {
                diff = Math.abs(breakfast - calItems.get(i));
                if(diff < minval) {
                    minval = diff;
                    suggest = calItems.get(i);
                    suggestion = items.get(i);
                }
            }
            if(suggest > breakfast) {
                lunch = lunch - minval;
            } else {
                lunch = lunch + minval;
            }
        } else if(currTime >= 12 && currTime <= 15) {
            minval = 1000;
            diff = 0;
            for(int i=0; i< calItems.size(); i++) {
                diff = Math.abs(lunch - calItems.get(i));
                if(diff < minval) {
                    minval = diff;
                    suggest = calItems.get(i);
                    suggestion = items.get(i);
                }
            }
            if(suggest > lunch) {
                snack = snack - minval;
            } else {
                snack = snack + minval;
            }
        } else if(currTime >= 16 && currTime <= 19) {
            minval = 1000;
            diff = 0;
            for(int i=0; i< calItems.size(); i++) {
                diff = Math.abs(snack - calItems.get(i));
                if(diff < minval) {
                    minval = diff;
                    suggest = calItems.get(i);
                    suggestion = items.get(i);
                }
            }
            if(suggest > snack) {
                dinner = dinner - minval;
            } else {
                dinner = dinner + minval;
            }
        } else {
            minval = 1000;
            diff = 0;
            for(int i=0; i< calItems.size(); i++) {
                diff = Math.abs(dinner - calItems.get(i));
                if(diff < minval) {
                    minval = diff;
                    suggest = calItems.get(i);
                    suggestion = items.get(i);
                }
            }
        }

        final int finalSuggest = suggest;
        new AlertDialog.Builder(getContext())
                .setTitle("Suggested Food")
                .setMessage("We suggest you to have: "+suggestion)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        usedCals = usedCals + finalSuggest;
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt("UsedCals", usedCals);
                        editor.commit();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

@TargetApi(Build.VERSION_CODES.N)
class MyOperator implements UnaryOperator<String>
{
    @Override
    public String apply(String t) {
        return t.toUpperCase();
    }
}
