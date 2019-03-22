package ameya.com.maptest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private final int request_code = 1;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference mstorage = storage.getReference();
    private ImageView dpiv;
    private Button changeEmail;
    private Button changePassword;
    private Button sendEmail;
    private DatabaseReference mDatabase;
    private EditText oldEmail, newEmail, password, newPassword;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //get current user
        dpiv = findViewById(R.id.dp_iv);
        final FirebaseUser user = auth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ProfileActivity.this, Login.class));
                    finish();
                }
            }
        };

        Button btnChangeEmail = findViewById(R.id.change_email_button);
        Button btnChangePassword = findViewById(R.id.change_password_button);
        Button btnSendResetEmail = findViewById(R.id.sending_pass_reset_button);
        changeEmail = findViewById(R.id.changeEmail);
        changePassword = findViewById(R.id.changePass);
        sendEmail = findViewById(R.id.send);
        Button signOut = findViewById(R.id.sign_out);

        oldEmail = findViewById(R.id.old_email);
        newEmail = findViewById(R.id.new_email);
        password = findViewById(R.id.password);
        newPassword = findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newEmail.getText().toString().trim().equals("")) {
                    user.updateEmail(newEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProfileActivity.this, "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                sendEmail.setVisibility(View.GONE);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newPassword.getText().toString().trim().equals("")) {
                    if (newPassword.getText().toString().trim().length() < 6) {
                        newPassword.setError("Password too short, enter minimum 6 characters");
                        progressBar.setVisibility(View.GONE);
                    } else {
                        user.updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ProfileActivity.this, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else if (newPassword.getText().toString().trim().equals("")) {
                    newPassword.setError("Enter password");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.VISIBLE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (!oldEmail.getText().toString().trim().equals("")) {
                    auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProfileActivity.this, "Reset password email is sent!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    oldEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        if (user == null)
            Picasso.get().load(R.drawable.ic_user_icon).transform(new CircleTransform()).into(dpiv);
        else
            Picasso.get().load(user.getPhotoUrl()).resize(350, 350).transform(new CircleTransform()).into(dpiv);

        dpiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, request_code);
            }
        });
        Button toggle_pinfo = findViewById(R.id.change_pinfo_toggle_bt);
        Button toggle_paceinfo = findViewById(R.id.change_paceinfo_toggle_bt);
        Button savepinfo = findViewById(R.id.change_personal_info_bt);
        Button savepaceinfo = findViewById(R.id.change_pace_info_bt);

        toggle_pinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout id = findViewById(R.id.personal_info_container);
                id.setVisibility(View.VISIBLE);
            }
        });
        toggle_paceinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout id = findViewById(R.id.pace_info_container);
                id.setVisibility(View.VISIBLE);
            }
        });
        savepinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeinfo();
                LinearLayout id = findViewById(R.id.personal_info_container);
                id.setVisibility(View.GONE);
            }
        });
        savepaceinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeinfo();
                LinearLayout id = findViewById(R.id.pace_info_container);
                id.setVisibility(View.GONE);
            }
        });

    }

    //sign out method
    private void signOut() {
        auth.signOut();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void changeinfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String fname, lname, major, grad;
            String year;
            mDatabase = mDatabase.child("/user-data/" + "/" + user.getUid() + "/");
            if (!((EditText) findViewById(R.id.pinfo_fname)).getText().toString().trim().isEmpty()) {
                fname = ((EditText) findViewById(R.id.pinfo_fname)).getText().toString().trim();
                mDatabase.child("firstname").setValue(fname);
                user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fname).build());
            }

            if (!((EditText) findViewById(R.id.pinfo_lname)).getText().toString().trim().isEmpty()) {
                lname = ((EditText) findViewById(R.id.pinfo_lname)).getText().toString().trim();
                mDatabase.child("lastname").setValue(lname);

            }


            if (!((EditText) findViewById(R.id.pace_info_major_minor)).getText().toString().trim().isEmpty()) {
                major = ((EditText) findViewById(R.id.pace_info_major_minor)).getText().toString().trim();
                mDatabase.child("major").setValue(major);
            }

            if (((RadioButton) (findViewById(R.id.grad_rdb_bt))).isChecked() || ((RadioButton) findViewById(R.id.ugrad_rd_bt)).isChecked()) {
                grad = findViewById(R.id.grad_rdb_bt).isSelected() ? "Graduate" : "Undergraduate";
                mDatabase.child("Graduation").setValue(grad);
            }

            year = ((EditText) findViewById(R.id.pace_info_grad_year)).getText().toString().trim();

            SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.US);
            try {
                format.parse(year);
            } catch (ParseException e) {
                year = null;
            }
            if (year != null)
                mDatabase.child("Year").setValue(year);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this,MapsActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == request_code && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            final StorageReference filepath = mstorage.child("Images").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child(Objects.requireNonNull(Objects.requireNonNull(uri).getLastPathSegment()));
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            // Pass it to Picasso to download, show in ImageView and caching
                            Picasso.get().load(uri.toString()).into(dpiv);
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Uri photoUrl = user.getPhotoUrl();

                                                if (photoUrl != null)
                                                    Picasso.get().load(photoUrl).resize(250, 250).transform(new CircleTransform()).into(dpiv);
                                                Log.d("Changing DP:", "User profile updated.");
                                            }
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                    /*final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                        Uri photoUrl = user.getPhotoUrl();
                                        if (photoUrl != null)
                                            Picasso.get().load(photoUrl).resize(350, 350).transform(new CircleTransform()).into(dpiv);
                                        Log.d("Changing DP:", "User profile updated.");
                                    }
                            });*/
                }

            });
        }
    }
}
