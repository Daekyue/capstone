package com.akj.sns_project;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.graphics.Color;
import android.media.Rating;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akj.sns_project.activity.PostActivity;
import com.akj.sns_project.adapter.PosterAdapter;
import com.akj.sns_project.adapter.ReplyAdapter;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MovieInfo extends Fragment {


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String userUid;

    private String mParam1;
    private String mParam2;

    private Movie movie;
    private View view;
    private int movieID;
    private String title;
    private float rate;
    private String releaseDate;
    private String overView;
    private String posterURL;
    private int[] genreList;
    private ArrayList<String> genres;

    private TextView textViewTitle;
    private TextView textViewReleaseDate;
    private TextView textViewOverView;
    private TextView textViewGenre;

    static RequestQueue requestQueue;
    private RatingBar ratingBar;
    private ImageView imageviewPoster;
    private RecyclerView recyclerViewMovieComment;
    private ReplyAdapter replyAdapter;
    private Button buttonMovieComment;
    private EditText editTextMovieComment;

    private String savelocationforReply;
    private ArrayList<ReplyInfo> replyList;
    private ReplyInfo replyInfo;

    private FirebaseUser user;
    FirebaseFirestore firestore;

    public MovieInfo(Movie movie) { //?????? ????????? ??????
        this.genreList = movie.genre_ids;
        this.movieID = movie.id;
        this.movie = movie;
        this.title = movie.title;
        this.rate = movie.vote_average;
        this.releaseDate = movie.release_date;
        this.overView = movie.overview;
        this.posterURL = movie.GetPosterPath();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MovieInfo.
     */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_movie_info, container, false);
        //Movie movie = (Movie) getActivity().getIntent().getSerializableExtra("movie");    // postinfo ???????????????
        textViewOverView = view.findViewById(R.id.textViewOverView);
        textViewTitle = view.findViewById(R.id.textViewMovieTitle);
        textViewReleaseDate = view.findViewById(R.id.textViewMovieReleaseDate);
        imageviewPoster = view.findViewById(R.id.imageViewPoster);
        ratingBar = view.findViewById(R.id.ratingBarMovie);
        recyclerViewMovieComment = view.findViewById(R.id.RecyclerViewMovieComment);
        buttonMovieComment = view.findViewById(R.id.buttonMovieComment);
        editTextMovieComment = view.findViewById(R.id.editTextMovieComment);
        textViewGenre = view.findViewById(R.id.textViewGenre);


        textViewTitle.setText(title);
        textViewTitle.setTextColor(Color.parseColor("#AAAAAA"));
        textViewOverView.setText(overView);
        textViewReleaseDate.setText(releaseDate);
        Glide.with(getActivity()).load(posterURL).into(imageviewPoster);
        buttonMovieComment.setOnClickListener(onClickListener);
        ratingBar.setRating(rate);

        recyclerViewMovieComment.setHasFixedSize(true); // ?????? ???????????? ????????? recyclerview??? ??? ????????? ????????? ????????? ????????????
        recyclerViewMovieComment.setLayoutManager(new LinearLayoutManager(getActivity())); // recyclerview??? ???????????? ???????????? linearlayoutmanager

        //?????? ?????? ????????? ???????????? ??????????????? ????????????.
        genres = new ArrayList<String>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                makeRequest("https://api.themoviedb.org/3/genre/movie/list?api_key=3c314dc629a0e72e9328fe7c33981cf2&language=ko-KR");
            }
        }).start();

        if (requestQueue == null) {

            requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        }


        //?????? ????????? ???????????? ????????? ???????????? ?????? ?????? ????????? ????????? ?????? ????????? ???????????? ??????
        //?????? ?????? ????????????????????? ?????? ?????? ???????????? ???????????? ???????????? ??????

        firestore = FirebaseFirestore.getInstance();

        Map<String, Object> movie = new HashMap<>();
        movie.put("ID",movieID);
        movie.put("title", title);
        movie.put("releaseDate", releaseDate);
        movie.put("rate", rate);

        //firebase??? ?????? ?????? ??????
        firestore.collection("movie").document(title).set(movie).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast toast = Toast.makeText(getActivity(), "??????", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast toast = Toast.makeText(getActivity(), "??????", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });

        savelocationforReply = title;

        firestore.collection("replys")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            replyList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if (savelocationforReply.equals(document.getData().get("saveLocation").toString())) {
                                    //Toast toast = Toast.makeText(getActivity(), "??????????????????", Toast.LENGTH_SHORT);
                                    //toast.show();
                                    replyList.add(new ReplyInfo(
                                            document.getData().get("contents").toString(),
                                            new Date(document.getDate("createdAt").getTime()),
                                            document.getData().get("saveLocation").toString(),
                                            document.getData().get("id").toString()));
                                }
                            }

                            Collections.sort(replyList,new ListCompartor());

                            recyclerViewMovieComment.setHasFixedSize(true);
                            recyclerViewMovieComment.setLayoutManager(new LinearLayoutManager(getActivity()));

                            RecyclerView.Adapter mAdapter = new ReplyAdapter(getActivity(), replyList);
                            recyclerViewMovieComment.setAdapter(mAdapter);
                            recyclerViewMovieComment.scrollToPosition(replyList.size()-1);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        /*

        DocumentReference productRef = firestore.collection("movie").document("????????????");
        //get()??? ????????? ?????? ????????? ????????? ????????????.
        productRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //????????? ??????????????? ????????????
                if (task.isSuccessful()) {
                    //????????? ???????????? ?????? DocumentSnapshot ??? ????????? ????????? ?????????.
                    DocumentSnapshot document = task.getResult();

                    //????????? ?????????
                } else {

                }
            }
        });

        DocumentReference docIdRef = firestore.collection("movie").document();
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toast toast = Toast.makeText(getActivity(), "?????? ??????", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {

                    }
                } else {
                    Toast toast = Toast.makeText(getActivity(), "??????", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });



         */


        return view;

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.buttonMovieComment:
                    storageUpload();
                    editTextMovieComment.setText("");
                    firestore.collection("replys")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        replyList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            if (savelocationforReply.equals(document.getData().get("saveLocation").toString())) {
                                                //Toast toast = Toast.makeText(getActivity(), "??????????????????", Toast.LENGTH_SHORT);
                                                //toast.show();
                                                replyList.add(new ReplyInfo(
                                                        document.getData().get("contents").toString(),
                                                        new Date(document.getDate("createdAt").getTime()),
                                                        document.getData().get("saveLocation").toString(),
                                                        document.getData().get("id").toString()));
                                            }
                                        }


                                        Collections.sort(replyList,new ListCompartor());

                                        recyclerViewMovieComment.setHasFixedSize(true);
                                        recyclerViewMovieComment.setLayoutManager(new LinearLayoutManager(getActivity()));

                                        RecyclerView.Adapter mAdapter = new ReplyAdapter(getActivity(), replyList);
                                        recyclerViewMovieComment.setAdapter(mAdapter);
                                        recyclerViewMovieComment.scrollToPosition(replyList.size()-1);

                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });

            }
        }
    };

    private void storageUpload() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        userUid = user.getUid().toString();
        final String contents = editTextMovieComment.getText().toString();

        if (contents.length() > 0) {
            // ???????????????????????? ?????? ????????? ?????????
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            final DocumentReference documentReference = replyInfo == null ?
                    firebaseFirestore.collection("replys").document() :
                    firebaseFirestore.collection("replys").document(replyInfo.getId());
            final Date date = replyInfo == null ? new Date() : replyInfo.getCreatedAt(); // postInfo??? NULL?????? new Date?????? NULL??? ????????? postinfo??? createdAt?????? ?????????
            // ????????? ????????? ?????? ?????????

            storeUpload(documentReference, new ReplyInfo(contents, date, title, userUid));
        } else {
            Toast toast = Toast.makeText(getActivity(), "????????????????????????", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    // ???????????????????????? ???????????? ????????? ????????? ?????? ??????
    private void storeUpload(DocumentReference documentReference, ReplyInfo replyInfo) {
        documentReference.set(replyInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast toast = Toast.makeText(getActivity(), "??????", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast toast = Toast.makeText(getActivity(), "??????", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
    }
    public void makeRequest(String url) {
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("output popular", response); //?????? ???????????????
                Gson gson = new Gson();  //gson??????????????? ??????


                MovieGenreList MovieGenreList = gson.fromJson(response, MovieGenreList.class); //gson?????? Json?????? object??? ??????

                //genreList?????? ?????? ?????? ??? ????????? MoviegenreList ?????? ?????? id??? ???????????? ????????? ??????
                for(int j =0; j < MovieGenreList.genres.size(); j++)
                {
                    for(int i = 0; i < genreList.length;  i++)
                    {
                        if(genreList[i] == MovieGenreList.genres.get(j).getId())
                        {
                            genres.add(MovieGenreList.genres.get(j).getName());
                        }
                    }
                }


                for(int k = 0; k < genres.size(); k++)
                {
                    if(k == 0)
                    {
                        textViewGenre.setText("#" + genres.get(0) + " ");
                    }
                    else
                    {
                        textViewGenre.append("#" + genres.get(k) +" " );
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error ", error.getMessage());
            }
        }
        ) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        request.setShouldCache(false);
        Log.v("SendRequest", "?????? ??????");
        //requestQueue.add(request);
        AppController.getInstance(getActivity()).addToRequestQueue(request);  //gson???????????? ?????? ??????
    }




}