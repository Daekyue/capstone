package com.akj.sns_project;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akj.sns_project.activity.BasicActivity;
import com.akj.sns_project.adapter.ReplyAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Fragment_Review extends BasicActivity {
    //파이어스토어에 접근하기 위한 객체를 생성한다.
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<String> postid;
    private ArrayList<ReplyInfo> replyList;
    private RecyclerView recyclerView;              // recyclerView
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_review);

        postid = new ArrayList<>();
        user = getIntent().getStringExtra("userid");

        //CollectionReference 는 파이어스토어의 컬렉션을 참조하는 객체다.
        CollectionReference productRef = db.collection("movie");
        //get()을 통해서 해당 컬렉션의 정보를 가져온다.
        productRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                //작업이 성공적으로 마쳤을때
                if (task.isSuccessful()) {
                    postid.clear();
                    //컬렉션 아래에 있는 모든 정보를 가져온다.
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        postid.add(document.getId());
                        //document.getData() or document.getId() 등등 여러 방법으로
                        //데이터를 가져올 수 있다.
                    }
                    db.collection("replys")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        replyList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d("gk WKwmdskrp dho EH", user + " => " + document.getData().get("id"));
                                            if(user.equals(document.getData().get("id").toString()))
                                                for (int i = 0; i<postid.size(); i++){
                                                    if (postid.get(i).equals(document.getData().get("saveLocation").toString())){
                                                        replyList.add(new ReplyInfo(
                                                                document.getData().get("contents").toString(),
                                                                new Date(document.getDate("createdAt").getTime()),
                                                                document.getData().get("saveLocation").toString(),
                                                                document.getData().get("id").toString()));
                                                    }
                                                }
                                        }
                                        Collections.sort(replyList,new ListCompartor());

                                        recyclerView = findViewById(R.id.recyclerView);
                                        recyclerView.setHasFixedSize(true);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(Fragment_Review.this));

                                        RecyclerView.Adapter mAdapter = new ReplyAdapter(Fragment_Review.this, replyList);
                                        recyclerView.setAdapter(mAdapter);
                                        recyclerView.scrollToPosition(replyList.size()-1);

                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                } else {

                }
            }
        });
    }
}
