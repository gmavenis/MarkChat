package com.marwfair.markchat.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class SignInFragment extends Fragment {


    private EditText usernameEt;
    private Button signinBtn;

    private OnSignInListener signInListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SignInFragment.
     */
    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();
        return fragment;
    }
    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.getRetainInstance();

        // Inflate the layout for this fragment
        ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.fragment_sign_in, container, false);

        usernameEt = (EditText)layout.findViewById(R.id.name_input);
        signinBtn = (Button)layout.findViewById(R.id.signin);

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEt.getText().toString();
                if (!username.isEmpty()) {
                    signInListener.onSignIn(username);
                } else {
                    Toast.makeText(getActivity(), getText(R.string.blank_username), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            signInListener = (OnSignInListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSignInListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        signInListener = null;
    }

        /**
         * This interface must be implemented by activities that contain this
         * fragment to allow an interaction in this fragment to be communicated
         * to the activity and potentially other fragments contained in that
         * activity.
         */
    public interface OnSignInListener {
        public void onSignIn(String username);
    }

}
