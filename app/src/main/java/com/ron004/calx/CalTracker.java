package com.ron004.calx;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class CalTracker extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private int pCals = 0;
    private int pCalper = 0;
    private int usedCals = 0;
    SharedPreferences sharedpreferences;
    TextView txtProgress;
    ProgressBar progressBar;
    ArrayList<String> getAllItems;
    ArrayList<String> getItemCal;
    AutoCompleteTextView autoCompleteTextView;
    AutoCompleteTextView autoRestaurant;
    EditText manualCals;
    Button addCals;
    int finalCal = 0;

    public CalTracker() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CalTracker newInstance(String param1, String param2) {
        CalTracker fragment = new CalTracker();
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
        sharedpreferences = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tracker_frag, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        txtProgress = view.findViewById(R.id.txtProgress);
        progressBar = view.findViewById(R.id.progressBar);
        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setEnabled(false);
        autoRestaurant = view.findViewById(R.id.autoRestaurant);
        manualCals = view.findViewById(R.id.manualCal);
        addCals = view.findViewById(R.id.addButton);


        if(sharedpreferences.contains("Name")) {
            String getwt = sharedpreferences.getString("Weight", "");
            String getht = sharedpreferences.getString("Height", "");
            String getage = sharedpreferences.getString("Age", "");
            String getexl = sharedpreferences.getString("Exercise", "");
            String getgen = sharedpreferences.getString("Gender", "");
            usedCals = sharedpreferences.getInt("UsedCals", 0);

            pCals = calculateCalories(getwt, getht, getage, getgen, getexl);
            pCalper = calculatePercent(usedCals, pCals);

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt("TotalCals", pCals);
            editor.commit();
        }
        progressBar.setProgress(pCalper);
        txtProgress.setText(usedCals+"\n/"+ pCals + " Cals");

        final ArrayAdapter<CharSequence> adapter13 = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.restaurant_names, android.R.layout.simple_spinner_item);
        autoRestaurant.setThreshold(1);
        autoRestaurant.setAdapter(adapter13);

        autoRestaurant.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                autoRestaurant.setText(selectedItem);

                if(selectedItem.contains("Search Restaurant")) {
                    autoCompleteTextView.setEnabled(false);
                } else {
                    autoCompleteTextView.setEnabled(true);
                    ArrayList<String> getitems = populateData(selectedItem);
                    ArrayAdapter<String> adapter12 = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, getitems);
                    autoCompleteTextView.setThreshold(1);
                    autoCompleteTextView.setAdapter(adapter12);

                    autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String selectedItem = (String) parent.getItemAtPosition(position);
                            autoCompleteTextView.setText(selectedItem);
                            finalCal = getCalorie(selectedItem);
                            usedCals = usedCals + finalCal;
                        }
                    });
                }
            }
        });

        addCals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalCal == 0) {
                    finalCal = Integer.parseInt(manualCals.getText().toString());
                    usedCals = usedCals + finalCal;
                }

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("UsedCals", usedCals);
                editor.commit();
                pCalper = calculatePercent(usedCals, pCals);
                progressBar.setProgress(pCalper);
                txtProgress.setText(usedCals+"\n/"+ pCals + " Cals");
                finalCal = 0;
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {
            if(sharedpreferences.contains("Name")) {
                String getwt = sharedpreferences.getString("Weight", "");
                String getht = sharedpreferences.getString("Height", "");
                String getage = sharedpreferences.getString("Age", "");
                String getexl = sharedpreferences.getString("Exercise", "");
                String getgen = sharedpreferences.getString("Gender", "");
                usedCals = sharedpreferences.getInt("UsedCals", 0);
                pCals = calculateCalories(getwt, getht, getage, getgen, getexl);
                pCalper = calculatePercent(usedCals, pCals);
                System.out.println("pcalper: "+pCalper);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("TotalCals", pCals);
                editor.commit();
            }
            progressBar.setProgress(0);
            progressBar.setProgress(pCalper, true);
            txtProgress.setText(usedCals+"\n/"+ pCals + " Cals");
            autoRestaurant.setText("Search Restaurant");
            autoCompleteTextView.setText("Search Food Item");
            manualCals.setText("0");
            autoCompleteTextView.setEnabled(false);
        }
    }

    public ArrayList<String> populateData(String restaurant) {
        DatabaseHelper db = new DatabaseHelper(getActivity());
        Cursor c = db.getAllItems(restaurant);
        getAllItems = new ArrayList<String>();
        getItemCal = new ArrayList<String>();
        while(!c.isAfterLast()) {
            getAllItems.add(c.getString(1));
            getItemCal.add(c.getString(2));
            c.moveToNext();
        }
        return getAllItems;
    }

    public int getCalorie(String item) {
        int cal = 0;
        for(int i=0; i<getAllItems.size(); i++) {
            if(item.equals(getAllItems.get(i))) {
                cal = Integer.parseInt(getItemCal.get(i));
            }
        }
        return cal;
    }

    public int calculateCalories(String wt, String ht, String age, String gender, String exl) {
        int calories = 0;
        double BMR = 0.0;
        if(gender.equals("Male")) {
            BMR = 66.4730 + (13.7516 * Integer.parseInt(wt)) + (5.0033 * Integer.parseInt(ht)) - (6.7550 * Integer.parseInt(age));
        } else {
            BMR = 655.0955 + (9.5634 * Integer.parseInt(wt)) + (1.8496 * Integer.parseInt(ht)) - (4.6756 * Integer.parseInt(age));
        }

        switch (exl) {
            case "Sedentary":
                calories = (int) (BMR * 1.2);
                break;
            case "Lightly Active":
                calories = (int) (BMR * 1.375);
                break;
            case "Moderately Active":
                calories = (int) (BMR * 1.55);
                break;
            case "Very Active":
                calories = (int) (BMR * 1.725);
                break;
            case "Extremely Active":
                calories = (int) (BMR * 1.9);
                break;
        }
        return calories;
    }

    public int calculatePercent(int usedCal, int totalCal) {
        int calpercent;
        if(usedCal > totalCal) {
            int excess =  usedCal - totalCal;
            double div = ((double)excess/totalCal)*100;
            calpercent = (int)div;
            progressBar.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progressbarexcess, null));
            txtProgress.setTextColor(Color.RED);
        } else {
            double div = ((double)usedCal/totalCal)*100;
            calpercent = (int)div;
            progressBar.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progressbar, null));
            txtProgress.setTextColor(Color.parseColor("#00BAE8"));
        }
        return calpercent;
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
