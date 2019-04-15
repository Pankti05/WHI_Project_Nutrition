package com.ron004.calx;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UserProfile extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    SharedPreferences sharedpreferences;

    public UserProfile() {
        // Required empty public constructor
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
        View rootView = inflater.inflate(R.layout.profile_frag, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final Spinner diet_type = view.findViewById(R.id.diet_type);

        final ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.diet_cat, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        diet_type.setAdapter(adapter1);

        final Spinner body_fat = view.findViewById(R.id.body_fat);

        final ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.bodyfat_cat, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        body_fat.setAdapter(adapter2);

        final Spinner ex_levels = view.findViewById(R.id.ex_levels);

        final ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.exlevels_cat, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ex_levels.setAdapter(adapter3);

        final Spinner gender = view.findViewById(R.id.gender);

        final ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.gender, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter4);

        final EditText username = view.findViewById(R.id.user_name);
        final EditText user_weight = view.findViewById(R.id.user_wt);
        final EditText user_height = view.findViewById(R.id.user_ht);
        final EditText user_age = view.findViewById(R.id.user_age);

        if(sharedpreferences.contains("Name")) {
            String getusername = sharedpreferences.getString("Name", "");
            username.setText(getusername);
            String getdiet = sharedpreferences.getString("Diet", "");
            diet_type.setSelection(adapter1.getPosition(getdiet));
            String getwt = sharedpreferences.getString("Weight", "");
            user_weight.setText(getwt);
            String getht = sharedpreferences.getString("Height", "");
            user_height.setText(getht);
            String getage = sharedpreferences.getString("Age", "");
            user_age.setText(getage);
            String getgender = sharedpreferences.getString("Gender", "");
            gender.setSelection(adapter4.getPosition(getgender));
            String getbfat = sharedpreferences.getString("Fat", "");
            body_fat.setSelection(adapter2.getPosition(getbfat));
            String getexl = sharedpreferences.getString("Exercise", "");
            ex_levels.setSelection(adapter3.getPosition(getexl));
        }



        Button save = view.findViewById(R.id.savebtn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uname  = username.getText().toString();
                String uwt  = user_weight.getText().toString();
                String uht  = user_height.getText().toString();
                String dtype = diet_type.getSelectedItem().toString();
                String bfat = body_fat.getSelectedItem().toString();
                String exl = ex_levels.getSelectedItem().toString();
                String uage = user_age.getText().toString();
                String ugen = gender.getSelectedItem().toString();
                int usedCals = 0;
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                String formattedDate = df.format(c);

                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString("Name", uname);
                editor.putString("Weight", uwt);
                editor.putString("Height", uht);
                editor.putString("Age", uage);
                editor.putString("Gender", ugen);
                editor.putString("Diet", dtype);
                editor.putString("Fat", bfat);
                editor.putString("Exercise", exl);
                editor.putInt("UsedCals", usedCals);
                editor.putString("Date", formattedDate);
                editor.commit();
                Toast.makeText(getActivity(), "Profile Updated!", Toast.LENGTH_LONG).show();
            }
        });

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
