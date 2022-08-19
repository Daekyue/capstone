package com.akj.sns_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class WritePostActivity extends BasicActivity {
    private static final String TAG = "WritePostActivity";
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        findViewById(R.id.checkWrite).setOnClickListener(onClickListener);
        findViewById(R.id.image).setOnClickListener(onClickListener);
        findViewById(R.id.video).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.checkWrite:
                    postUpdate();
                    break;
                case R.id.image:
                    myStartActivity(GalleryActivity.class, "image");
                    break;

                case R.id.video:
                    myStartActivity(GalleryActivity.class, "video");
                    break;
            }
        }
    };

    private void postUpdate(){ // 글작성
        String title = ((EditText)findViewById(R.id.titleEditText)).getText().toString();
        String contents = ((EditText)findViewById(R.id.contentsEditText)).getText().toString();

        if(title.length() > 0 && contents.length() > 0){
            user = FirebaseAuth.getInstance().getCurrentUser();


            WriteInfo writeInfo  = new WriteInfo(title, contents, user.getUid());
            uploader(writeInfo);
        }else{
            startToast("회원정보를 입력해주세요");
        }
    }

    private void uploader(WriteInfo writeInfo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").add(writeInfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void myStartActivity(Class c, String media ){
        Intent intent = new Intent(this,c);
        intent.putExtra("media",media);
        startActivity(intent);
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}