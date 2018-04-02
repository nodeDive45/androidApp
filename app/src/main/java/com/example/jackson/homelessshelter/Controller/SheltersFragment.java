package com.example.jackson.homelessshelter.Controller;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.jackson.homelessshelter.Model.DrawerLocker;
import com.example.jackson.homelessshelter.Model.Shelter;
import com.example.jackson.homelessshelter.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SheltersFragment extends Fragment {

    private DatabaseReference database;
    private DatabaseReference shelters;
    private ArrayAdapter adapt;
    private List<String> list;
    private ListView listView;
    private DrawerLocker lockheed;
    private Spinner genderSpinner;
    private Spinner ageSpinner;
    private EditText searchCriteria;
    private List<Shelter> baseList;
    private Shelter queryDetails;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        lockheed.unlocked(true);
        return inflater.inflate(R.layout.activity_list_of_shelters, container, false);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        lockheed = (DrawerLocker) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    // Initialize references and populate list

    private void initialize() {
        list = new ArrayList<>();
        adapt = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
        baseList = new ArrayList<>();
        listView = getActivity().findViewById(R.id.listView);
        listView.setAdapter(adapt);
        // Allow list items to be selected
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);
                giveDetails(itemValue);
            }
        });
        database = FirebaseDatabase.getInstance().getReference();
        shelters = database.child("shelter");
        // Populate list with shelter names
        shelters.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Shelter addMe = dataSnapshot.getValue(Shelter.class);
                baseList.add(addMe);
                list.add(dataSnapshot.child("name").getValue(String.class));
                adapt.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                baseList.remove(dataSnapshot.getValue(String.class));
                adapt.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        ageSpinner = (Spinner) getActivity().findViewById(R.id.ageSpinner);
        genderSpinner = (Spinner) getActivity().findViewById(R.id.genderSpinner);
        searchCriteria = (EditText) getActivity().findViewById(R.id.searchContent);
        searchCriteria.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                list = generateList(ageSpinner.getSelectedItem().toString(),
                        genderSpinner.getSelectedItem().toString(),
                        charSequence);
                adapt.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                list = generateList(ageSpinner.getSelectedItem().toString(),
                        genderSpinner.getSelectedItem().toString(),
                        searchCriteria.getText().toString());
                adapt.notifyDataSetChanged();
            }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    return;
                }
        });
        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                list = generateList(ageSpinner.getSelectedItem().toString(),
                        genderSpinner.getSelectedItem().toString(),
                        searchCriteria.getText().toString());
                adapt.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
    }

    private ArrayList<String> generateList(String ageFilt, String gendFilt, CharSequence search) {
        list.clear();
        for (Shelter shelts : baseList) {
            list.add(shelts.getName());
        }
        if (!Objects.equals(gendFilt, "Both")) {
            for (Shelter shelts : baseList) {
                boolean confirmed = false;
                String checking = shelts.getRestrictions();
                while (checking.split(",").length > 1 && !confirmed) {
                    if (Objects.equals(checking.split(",")[0], gendFilt)) {
                        confirmed = true;
                    }
                    checking = checking.split(",")[1];
                }
                if (Objects.equals(checking, gendFilt) || confirmed) {
                } else {
                    list.remove(shelts.getName());
                }
            }
        }
        if (!Objects.equals(ageFilt, "Anyone")) {
            for (Shelter shelts : baseList) {
                boolean confirmed = false;
                String checking = shelts.getRestrictions();
                while (checking.split(",").length > 1 && !confirmed) {
                    if (Objects.equals(checking.split(",")[0], ageFilt)) {
                        confirmed = true;
                    }
                    checking = checking.split(",")[1];
                }
                System.out.println("confirmed?" + confirmed);
                if (Objects.equals(checking, ageFilt) || confirmed) {
                } else {
                    list.remove(shelts.getName());
                }
            }
        }
        if (search.length() != 0) {
            List<String> legend = new ArrayList<>();
            for (String name : list) {
                if (!name.toLowerCase().contains(search.toString().toLowerCase())) {
                    legend.add(name);
                }
            }
            list.removeAll(legend);
        }
        return (ArrayList<String>) list;
    }

    // Create popup window with shelter details

    private void giveDetails(String itemValue) {
        DatabaseReference shelters = FirebaseDatabase.getInstance().getReference("shelter");
        Query query = shelters.orderByChild("name").equalTo(itemValue);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        queryDetails = snap.getValue(Shelter.class);
                        queryDetails.setCurrentShelter(snap.getRef());
                        System.out.println("here" + snap.getRef());
                    }
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("Shelter", queryDetails);
                    android.support.v4.app.Fragment det = new ShelterDetails();
                    android.support.v4.app.FragmentManager fm = getFragmentManager();
                    android.support.v4.app.FragmentTransaction trans = fm.beginTransaction();
                    det.setArguments(bundle);
                    trans.replace(R.id.frag_container, det).addToBackStack("last");
                    trans.commit();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}