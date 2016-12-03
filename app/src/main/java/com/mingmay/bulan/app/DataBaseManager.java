package com.mingmay.bulan.app;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mingmay.bulan.model.BuLanSaveModel;
import com.mingmay.bulan.model.Tag;

public class DataBaseManager extends SQLiteOpenHelper {
	public static final int BULAN_STATUS_CONTENT_UPLOAD_SUCCESS = 0;
	public static final int BULAN_STATUS_CONTENT_UPLOAD_FAILE = -1;
	public static final int BULAN_STATUS_IMAGE_UPLOAD_SUCCESS = 1;
	public static final int BULAN_STATUS_IMAGE_UPLOAD_FAILE = -2;

	public static final String NAME = "cc.db";
	public static final int VERSION = 1;

	private static DataBaseManager dbManager;
	private SQLiteDatabase db;

	private DataBaseManager(Context paramContext,
			SQLiteDatabase.CursorFactory paramCursorFactory) {
		super(paramContext, NAME, paramCursorFactory, VERSION);
		db = getWritableDatabase();
	}

	public static DataBaseManager getInstance(Context context) {
		if (dbManager == null)
			dbManager = new DataBaseManager(context, null);
		return dbManager;
	}

	public static DataBaseManager getInstance() {
		return dbManager;
	}

	public static final String BULAN_PUBLISH_SAVE = "CREATE TABLE bulan_publish ("
			+ "_ID integer PRIMARY KEY AUTOINCREMENT,"
			+ "title varchar,"
			+ "iconPath varchar,"
			+ "isOpen varchar,"
			+ "tag varchar,"
			+ "state integer," + "lastTime integer," + "content varchar)";
	public static final String TAG_TABLE = "CREATE TABLE tag ("
			+ "_ID integer PRIMARY KEY AUTOINCREMENT, " + "t_id varchar,"
			+ "name varchar, " + "bg integer)";

	public void clearTags() {
		db.execSQL("delete from tag");
	}

	public long insertTag(Tag tag) {
		deleteTag(tag);
		ContentValues values = new ContentValues();
		values.put("t_id", tag.id);
		values.put("name", tag.name);
		values.put("bg", tag.bgResId);
		return db.insert("tag", null, values);
	}

	public void deleteTag(Tag tag) {
		db.delete("tag", "t_id=?", new String[] { String.valueOf(tag.id) });
	}

	public List<Tag> getCachTags() {
		Cursor c = db.rawQuery("select *from tag", null);
		if (c != null) {
			List<Tag> tags;
			if (c.getCount() > 0) {
				tags = new ArrayList<Tag>();
				while (c.moveToNext()) {
					Tag t = new Tag();
					t.id = c.getInt((c.getColumnIndex("t_id")));
					t.name = c.getString(c.getColumnIndex("name"));
					t.bgResId = c.getInt(c.getColumnIndex("bg"));
					tags.add(t);
				}
				c.close();
				return tags;
			}
		}
		return null;
	}

	public long savePublishBuLan(String title, String iconPath, String isOpen,
			String tag, String content) {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("iconPath", iconPath);
		values.put("isOpen", isOpen);
		values.put("tag", tag);
		values.put("content", content);
		values.put("state", BuLanSaveModel.STATE_CAN_EDIT);
		values.put("lastTime", System.currentTimeMillis());

		long id = db.insert("bulan_publish", null, values);
		return id;
	}

	public void updateBulan(String id, String title, String iconPath,
			String isOpen, String tag, String content) {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("iconPath", iconPath);
		values.put("isOpen", isOpen);
		values.put("tag", tag);
		values.put("content", content);
		values.put("state", BuLanSaveModel.STATE_CAN_EDIT);
		values.put("lastTime", System.currentTimeMillis());
		db.update("bulan_publish", values, "_ID=?", new String[] { id });
	}

	public void updateBulanInfo(long id, String iconPath, String isOpen,
			String tag,int state) {
		BuLanSaveModel bs = getBulanSave(id);
		ContentValues cv = new ContentValues();
		cv.put("iconPath", iconPath);
		cv.put("isOpen", isOpen);
		cv.put("tag", tag);
		cv.put("state",state);
		db.update("bulan_publish", cv, "_ID=?",
				new String[] { String.valueOf(id) });
		bs = getBulanSave(id);
		Log.d("", "");
	}

	public void updateBulanState(String id, int state) {
		db.execSQL("update bulan_publish set state=?  where _ID=?",
				new String[] { String.valueOf(state), id });
	}

	public void updateBulan(String id, String title, String iconPath,
			String isOpen, String tag, String content, int state) {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("iconPath", iconPath);
		values.put("isOpen", isOpen);
		values.put("tag", tag);
		values.put("content", content);
		values.put("state", state);
		values.put("lastTime", System.currentTimeMillis());
		db.update("bulan_publish", values, "_ID=?", new String[] { id });
	}

	public void deleteBulanSave(long id) {
		db.delete("bulan_publish", "_ID=?", new String[] { "" + id });
	}

	public BuLanSaveModel getBulanSave(long id) {
		Cursor c = db.rawQuery("select *from bulan_publish  where _ID=?",
				new String[] { String.valueOf(id) });
		if (c != null) {
			if (c.moveToFirst()) {
				BuLanSaveModel bulan = BuLanSaveModel.cursorToModel(c);
				c.close();
				return bulan;
			}
		}
		return null;
	}

	public List<BuLanSaveModel> getBulanSaveList() {
		Cursor c = db.rawQuery("select *from bulan_publish", null);
		List<BuLanSaveModel> results = null;
		if (c != null) {
			results = new ArrayList<BuLanSaveModel>();
			if (c.moveToNext()) {
				BuLanSaveModel bulan = BuLanSaveModel.cursorToModel(c);
				results.add(bulan);
			}
			c.close();
		}
		return results;
	}

	public String getBulanSaveContent(long id) {
		Cursor c = db.rawQuery("select *from bulan_publish  where _ID=?",
				new String[] { "" + id });
		if (c != null) {
			if (c.moveToFirst()) {
				String content = c.getString(c.getColumnIndex("content"));
				c.close();
				return content;
			}
		}
		return null;
	}

	public List<BuLanSaveModel> loadSaves() {
		Cursor c = db.rawQuery("select *from bulan_publish", null);
		List<BuLanSaveModel> saves = null;
		if (c != null) {
			saves = new ArrayList<BuLanSaveModel>();
			while (c.moveToNext()) {
				saves.add(BuLanSaveModel.cursorToModel(c));
			}
			c.close();
		}
		return saves;
	}

	public BuLanSaveModel loadSave(int id) {
		Cursor c = db.rawQuery("select *from bulan_publish where _ID=?",
				new String[] { "" + id });
		BuLanSaveModel model = null;
		if (c != null) {
			while (c.moveToFirst()) {
				model = BuLanSaveModel.cursorToModel(c);
			}
			c.close();
		}
		db.close();
		return model;
	}

	public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
		paramSQLiteDatabase.execSQL(TAG_TABLE);
		paramSQLiteDatabase.execSQL(BULAN_PUBLISH_SAVE);
	}

	public void close() {
		if (db != null) {
			db.close();
		}
	}

	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1,
			int paramInt2) {
		paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS tag");
		paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS bulan_publish");
		onCreate(paramSQLiteDatabase);
	}
}