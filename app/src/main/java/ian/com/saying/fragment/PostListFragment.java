package ian.com.saying.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import ian.com.saying.PostDetailActivity;
import ian.com.saying.R;
import ian.com.saying.model.Post;
import ian.com.saying.model.User;
import ian.com.saying.viewholder.PostViewHolder;

public abstract class PostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private FirebaseAuth mAuth;

    public PostListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = rootView.findViewById(R.id.messagesList);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
            Query postsQuery = getQuery(mDatabase);
            FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                    .setQuery(postsQuery, Post.class)
                    .build();

            mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {


                @NonNull
                @Override
                public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                    return new PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));
                }

                @Override
                protected void onBindViewHolder(@NonNull final PostViewHolder viewHolder, int position, @NonNull final Post model) {
                    final DatabaseReference postRef = getRef(position);

                    // Set click listener for the whole post view
                    final String postKey = postRef.getKey();
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d("dataExist", ":"+dataSnapshot.child(postKey).exists());
                                    if (dataSnapshot.child(postKey).exists()){
                                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                                        startActivity(intent);
                                    }else {
                                        Toast.makeText(getActivity(), "삭제된 게시글입니다.", Toast.LENGTH_LONG).show();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            // Launch PostDetailActivity
//                        if (!mDatabase.child("posts").child(postKey).getKey().isEmpty()){
//                            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
//                            intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
//                            startActivity(intent);
//                        }else{
//                            Toast.makeText(getActivity(), "게시글이 삭제되었습니다.", Toast.LENGTH_LONG).show();
//                        }

                        }
                    });

                    if (model.uid.equals(getUid())){
                        viewHolder.deleteView.setVisibility(View.VISIBLE);
                    }

                    // Determine if the current user has liked this post and set UI accordingly
                    if (model.stars.containsKey(getUid())) {
                        viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                    } else {
                        viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                    }

                    // Bind Post to ViewHolder, setting OnClickListener for the star button

                        viewHolder.bindToPost(model, new View.OnClickListener() {
                            @Override
                            public void onClick(View starView) {
                                Log.d("starsClick", "click");
                                // Need to write to both places the post is stored
                                DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                                DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                                if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
                                    Toast.makeText(getActivity(), "로그인을 해야 추천할 수 있습니다..", Toast.LENGTH_LONG).show();
                                }else {
                                // Run two transactions
                                onStarClicked(globalPostRef);
                                onStarClicked(userPostRef);
                                }
                            }
                        });


                    viewHolder.deleteView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
                            ab.setMessage("게시글을 삭제하시겠어요?");
                            ab.setCancelable(false);
                            ab.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                                    DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());
//                                DatabaseReference userfvorites = mDatabase.child("user-favorites").child(model.uid).child(postRef.getKey());

                                    onDeleteCilcked(globalPostRef);
                                    onDeleteCilcked(userPostRef);
//                                onDeleteCilcked(userfvorites);
                                }
                            });
                            ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }
                    });

                }
            };
            mRecycler.setAdapter(mAdapter);
        }

    private void onDeleteCilcked (final DatabaseReference postRef){
        Log.d("deleteGetKey", postRef.getKey());
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
                if (p.uid.equals(getUid())){
                    postRef.removeValue();
                }
               return   Transaction.success(mutableData);
            }
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    // [START post_stars_transaction]
    private void onStarClicked(final DatabaseReference postRef) {
        Log.d("getKey", postRef.getKey());
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    mDatabase.child("user-favorites").child(getUid()).child(postRef.getKey()).removeValue();
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                    mDatabase.child("user-favorites").child(getUid()).child(postRef.getKey()).removeValue();
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                    mDatabase.child("user-favorites").child(getUid()).child(postRef.getKey()).setValue(p);
                }
//                if (p.stars.get(getUid())){
//                    mDatabase.child("user-favorites").child(getUid()).child(postRef.getKey()).setValue(p);
//                }else{
//                    mDatabase.child("user-favorites").child(getUid()).child(postRef.getKey()).removeValue();
//                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
    // [END post_stars_transaction]

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}
