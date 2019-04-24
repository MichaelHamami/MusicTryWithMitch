package com.hamami.musictrywithmitch.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamami.musictrywithmitch.R;

public class InformationFromDeveloperFragment extends Fragment {

        private static final String TAG = "InfoDeveloperFragment";

        private TextView mAppName;
        private ImageView mIconApp;
        private EditText mInformation;
        private String mResult;



        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }



        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            return inflater.inflate(R.layout.fragment_information_from_developer,container,false);
        }

        // called after onCreateView
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
        {
            Log.d(TAG, "onViewCreated: we get here?");
            mAppName = view.findViewById(R.id.developer_app_name);
            mIconApp = view.findViewById(R.id.developer_app_icon);
            mInformation = view.findViewById(R.id.developer_information);
            mAppName.setText("Music is Life");
            mInformation.setText("should be text from somewhare");
            retrieveInformationFromFireStore();
        }


        public void retrieveInformationFromFireStore()
        {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference reference = database.collection("Information")
                    .document("owner");

            reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        DocumentSnapshot doc = task.getResult();
                        Log.d(TAG, "onComplete: "+ doc);
                        mResult  = doc.get("input").toString();
                        mInformation.setText(mResult.toString());
                    }
                    else
                    {
                        mResult = " didn't get information";
                        mInformation.setText(mResult.toString());
                    }
                }
            });
        }



}
