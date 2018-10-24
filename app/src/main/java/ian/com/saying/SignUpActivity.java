package ian.com.saying;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ian.com.saying.databinding.ActivitySignUpBinding;
import ian.com.saying.model.User;

public class SignUpActivity extends BaseActivity {

    private static final String TAG = "SignUpActivity";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding.rlSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.rlSignup.getWindowToken(), 0);
            }
        });

        binding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

    }

    private void signUp(){
        Log.d(TAG, "Signup");

        if (!validateFrom()){
            return;
        }

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(binding.etEmail.getText().toString().trim(),
                binding.etPw.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                        hideProgressDialog();
                        if (task.isSuccessful()){
                            hideProgressDialog();
                            User user = new User(binding.etNick.getText().toString().trim(),
                                    binding.etEmail.getText().toString().trim());
                            mDatabase.child("users").child(task.getResult().getUser().getUid()).setValue(user);
                            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                            finish();
                            Toast.makeText(getApplicationContext(), "회원가입이 되었습니다.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            hideProgressDialog();
                            Toast.makeText(getApplicationContext(), "회원가입이 실패하였습니다.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean validateFrom(){
        boolean result = true;

        if (TextUtils.isEmpty(binding.etEmail.getText().toString())){
            binding.etEmail.setError("이메일을 입력해 주세요.");
            result = false;
        }else {
            binding.etEmail.setError(null);
        }

        if (TextUtils.isEmpty(binding.etNick.getText().toString())){
            binding.etNick.setError("별명을 입력해주세요.");
            result = false;
        } else {
            binding.etNick.setError(null);
        }

        if (TextUtils.isEmpty(binding.etPw.getText().toString())){
            binding.etPw.setError("암호를 입력해주세요.");
            result = false;
        } else {
            binding.etPw.setError(null);
        }

        if (TextUtils.isEmpty(binding.etConpw.getText().toString())){
            binding.etConpw.setError("암호를 확인해주세요.");
            result = false;
        } else {
            binding.etConpw.setError(null);
        }

        if (!TextUtils.equals(binding.etPw.getText().toString(), binding.etConpw.getText().toString())){
            binding.etPw.setError("암호가 일치하지 않습니다.");
            binding.etConpw.setError("암호가 일치하지 않습니다.");
            result = false;
        }else{
            binding.etPw.setError(null);
        }



        return result;
    }
}
