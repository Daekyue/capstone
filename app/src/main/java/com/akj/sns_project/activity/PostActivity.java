package com.akj.sns_project.activity;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akj.sns_project.ListCompartor;
import com.akj.sns_project.PostInfo;
import com.akj.sns_project.R;
import com.akj.sns_project.ReplyInfo;
import com.akj.sns_project.adapter.ReplyAdapter;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class PostActivity extends BasicActivity {
    private FirebaseUser user;
    private ArrayList<ReplyInfo> replyList;
    private ArrayList<ReplyInfo> replyList2;
    private FirebaseFirestore firebaseFirestore;
    private ReplyAdapter replyAdapter;
    private ReplyInfo replyInfo;
    private int successCount;
    private String savelocationforReply;
    private LinearLayout parent;
    private RecyclerView recyclerView;              // recyclerView
    private String userUid;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userUid = user.getUid().toString();
         db = FirebaseFirestore.getInstance();

        PostInfo postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");    // postinfo ???????????????
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText(postInfo.getTitle());

        // ???????????? ????????? ?????? ????????????
        TextView createdAtTextView = findViewById(R.id.createAtTextView);
        createdAtTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postInfo.getCreatedAt()));

        // contentsLayout??? contents(????????? ??????)??? ????????????
        LinearLayout contentsLayout = findViewById(R.id.contentsLayout);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ArrayList<String> contentsList = postInfo.getContents();

        // 11.20 ???????????? ????????? ?????? ????????????
        TextView likeCount = findViewById(R.id.likeCount);
        likeCount.setText(String.valueOf(postInfo.getlike()));

        // 11.20 ???????????? ????????? ?????? ????????????
        TextView unlikeCount = findViewById(R.id.unlikeCount);
        unlikeCount.setText(String.valueOf(postInfo.getUnlike()));

        // ????????? ????????? ????????? ?????? ???????????? ????????? ?????????
        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(contentsList)) {
            contentsLayout.setTag(contentsList);
            contentsLayout.removeAllViews();

            for (int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (Patterns.WEB_URL.matcher(contents).matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-29021.appspot.com/o/posts")) {   // ??? ????????? ???????????? ???????????? ?????? ??????
                    // ?????? ????????? ????????? URL????????????????????? ??? ???????????? ????????????????????? ?????? ???????????????????????? ???????????? ????????? ???????????? ???????????????????????? 11.23 ??????
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY); // ????????? ????????? ????????? ????????????
                    contentsLayout.addView(imageView);
                    Glide.with(this).load(contents).override(1000).thumbnail(0.1f).into(imageView);
                } else {          // ????????? ?????? ?????? ??????
                    TextView textView = new TextView(this);
                    textView.setLayoutParams(layoutParams);
                    textView.setText(contents);
                    textView.setTextColor(Color.rgb(0, 0, 0));
                    contentsLayout.addView(textView);
                }
            }
        }


        // ????????? ?????? ??????
        savelocationforReply = postInfo.getsaveLocation();

        db.collection("replys")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            replyList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if (savelocationforReply.equals(document.getData().get("saveLocation").toString())) {
                                    replyList.add(new ReplyInfo(
                                            document.getData().get("contents").toString(),
                                            new Date(document.getDate("createdAt").getTime()),
                                            document.getData().get("saveLocation").toString(),
                                            document.getData().get("id").toString()));
                                }
                            }

                            Collections.sort(replyList,new ListCompartor());  //?????? ????????? ?????? ??????



                            recyclerView = findViewById(R.id.recyclerView);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(PostActivity.this));

                            RecyclerView.Adapter mAdapter = new ReplyAdapter(PostActivity.this, replyList);
                            recyclerView.setAdapter(mAdapter);
                            recyclerView.scrollToPosition(replyList.size()-1);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        findViewById(R.id.replySend).setOnClickListener(onClickListener);
        parent = findViewById(R.id.replyLayout);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.replySend:
                    storageUpload();
                    ((EditText) findViewById(R.id.replyText)).setText("");
                    db.collection("replys")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        replyList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            if (savelocationforReply.equals(document.getData().get("saveLocation").toString())) {
                                                replyList.add(new ReplyInfo(
                                                        document.getData().get("contents").toString(),
                                                        new Date(document.getDate("createdAt").getTime()),
                                                        document.getData().get("saveLocation").toString(),
                                                        document.getData().get("id").toString()));
                                            }
                                        }

                                        Collections.sort(replyList,new ListCompartor());//?????? ????????? ?????? ??????


                                        recyclerView = findViewById(R.id.recyclerView);
                                        recyclerView.setHasFixedSize(true);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(PostActivity.this));

                                        RecyclerView.Adapter mAdapter = new ReplyAdapter(PostActivity.this, replyList);
                                        recyclerView.setAdapter(mAdapter);
                                        recyclerView.scrollToPosition(replyList.size()-1);   //????????? ????????? ?????????
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });

            }
        }
    };

    private void storageUpload() {
        final String contents = ((EditText) findViewById(R.id.replyText)).getText().toString();

        if (contents.length() > 0) {
            // ???????????????????????? ?????? ????????? ?????????
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            final DocumentReference documentReference = replyInfo == null ?
                    firebaseFirestore.collection("replys").document() :
                    firebaseFirestore.collection("replys").document(replyInfo.getId());
            final Date date = replyInfo == null ? new Date() : replyInfo.getCreatedAt(); // postInfo??? NULL?????? new Date?????? NULL??? ????????? postinfo??? createdAt?????? ?????????
            // ????????? ????????? ?????? ?????????

            storeUpload(documentReference, new ReplyInfo(contents, date, savelocationforReply, userUid));
        } else {
            startToast("????????? ??????????????????.");
        }
    }


    // ???????????????????????? ???????????? ????????? ????????? ?????? ??????
    private void storeUpload(DocumentReference documentReference, ReplyInfo replyInfo) {
        documentReference.set(replyInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
