package ian.com.saying;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ian.com.saying.databinding.ActivitySignInBinding;
import ian.com.saying.model.User;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity2";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        binding.rlSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.rlSignin.getWindowToken(), 0);
            }
        });

        binding.btnLogin.setOnClickListener(this);
        binding.tvSignup.setOnClickListener(this);
        binding.tvAnony.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }
    }

    private void signIn(){
        if (!validateFrom()){
            return;
        }
        showProgressDialog();

        mAuth.signInWithEmailAndPassword(binding.etEmail.getText().toString().trim(),
                binding.etPw.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    hideProgressDialog();
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
                }else{
                    hideProgressDialog();
                    Toast.makeText(getApplicationContext(), "로그인이 실패했습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
            }
        });
    }

    private boolean validateFrom(){
        boolean result = true;

        if (TextUtils.isEmpty(binding.etEmail.getText().toString())){
            binding.etEmail.setError("아이디를 입력해주세요.");
            result = false;
        } else {
            binding.etEmail.setError(null);
        }

        if (TextUtils.isEmpty(binding.etPw.getText().toString())){
            binding.etPw.setError("암호를 입력해주세요.");
            result = false;
        } else {
            binding.etPw.setError(null);
        }

        return  result;
    }

    private void anonyIn(){
        showProgressDialog();
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInAnonymously:success");
                                hideProgressDialog();
//                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                                User user = new User("anonymous", "anonymous");
//                                mDatabase.child("users-anomymous").child(firebaseUser.getUid()).setValue(user);
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            } else {
                                hideProgressDialog();
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인하세요.", Toast.LENGTH_LONG).show();
                }
            });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                signIn();
                break;
            case R.id.tv_signup:
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                break;
            case R.id.tv_anony:
                anonyIn();
                break;
        }
    }
}
