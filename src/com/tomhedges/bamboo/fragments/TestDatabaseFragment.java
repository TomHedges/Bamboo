// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.fragments;

import com.tomhedges.bamboo.R;
import com.tomhedges.bamboo.model.Comment;
import com.tomhedges.bamboo.util.dao.CommentsDataSource;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import java.util.List;
import java.util.Random;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class TestDatabaseFragment extends ListFragment implements OnClickListener {

	private CommentsDataSource datasource;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		datasource = new CommentsDataSource(this.getActivity());
		datasource.open();

		List<Comment> values = datasource.getAllComments();

		// use the SimpleCursorAdapter to show the
		// elements in a ListView
		ArrayAdapter<Comment> adapter = new ArrayAdapter<Comment>(this.getActivity(),
				android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.test_db, container, false);

        Button bAdd = (Button) v.findViewById(R.id.add);
        bAdd.setOnClickListener(this);
        Button bDelete = (Button) v.findViewById(R.id.delete);
        bDelete.setOnClickListener(this);
        
        return v;
	}

	// Will be called via the onClick attribute
	// of the buttons in main.xml
	public void onClick(View view) {
		@SuppressWarnings("unchecked")
		ArrayAdapter<Comment> adapter = (ArrayAdapter<Comment>) getListAdapter();
		Comment comment = null;
		switch (view.getId()) {
		case R.id.add:
			String[] comments = new String[] { "Cool", "Very nice", "Hate it" };
			int nextInt = new Random().nextInt(3);
			// save the new comment to the database
			comment = datasource.createComment(comments[nextInt]);
			adapter.add(comment);
			break;
		case R.id.delete:
			if (getListAdapter().getCount() > 0) {
				comment = (Comment) getListAdapter().getItem(0);
				datasource.deleteComment(comment);
				adapter.remove(comment);
			}
			break;
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		datasource.open();
		super.onResume();
	}

	@Override
	public void onPause() {
		datasource.close();
		super.onPause();
	}
}
