// Original code from - http://www.vogella.com/tutorials/AndroidSQLite/article.html

package com.tomhedges.bamboo.util.dao;

import java.util.ArrayList;
import java.util.List;
import com.tomhedges.bamboo.model.Comment;
import com.tomhedges.bamboo.util.localdatabase.MySQLiteHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CommentsDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_COMMENT };

	public CommentsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		Log.w(MySQLiteHelper.class.getName(), "Open writeable database");
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		Log.w(MySQLiteHelper.class.getName(), "Close database");
		dbHelper.close();
	}

	public Comment createComment(String comment) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_COMMENT, comment);
		long insertId = database.insert(MySQLiteHelper.TABLE_COMMENTS, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Comment newComment = cursorToComment(cursor);
		cursor.close();
		Log.w(MySQLiteHelper.class.getName(), "Create comment '" + comment + "' at index: " + insertId);
		return newComment;
	}

	public void deleteComment(Comment comment) {
		long id = comment.getId();
		Log.w(MySQLiteHelper.class.getName(), "Delete comment '" + comment + "' at index: " + id);
		database.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public List<Comment> getAllComments() {
		Log.w(MySQLiteHelper.class.getName(), "Get all comments");
		
		List<Comment> comments = new ArrayList<Comment>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Comment comment = cursorToComment(cursor);
			comments.add(comment);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return comments;
	}

	private Comment cursorToComment(Cursor cursor) {
		Log.w(MySQLiteHelper.class.getName(), "Convert Cursor to Comment");
		Comment comment = new Comment();
		comment.setId(cursor.getLong(0));
		comment.setComment(cursor.getString(1));
		return comment;
	}
}
