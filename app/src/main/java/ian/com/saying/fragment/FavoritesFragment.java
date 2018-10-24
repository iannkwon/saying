package ian.com.saying.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class FavoritesFragment extends PostListFragment {
    public FavoritesFragment(){}
    @Override
    public Query getQuery(final DatabaseReference databaseReference) {
        // [START my_top_posts_query]
        // My top posts by number of stars
        final String myUserId = getUid();

        final Query favoritesPostsQuery = databaseReference.child("user-favorites").child(getUid());
        // [END favoritesPostsQuery]
        return favoritesPostsQuery;
    }
}
