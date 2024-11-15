package com.milanac007.demo.im.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.milanac007.demo.im.App;
import com.milanac007.demo.im.db.entity.GroupEntity;
import com.milanac007.demo.im.db.entity.GroupMemberEntity;
import com.milanac007.demo.im.db.entity.MessageEntity;
import com.milanac007.demo.im.db.entity.SessionEntity;
import com.milanac007.demo.im.db.entity.User;
import com.milanac007.demo.im.db.entity.UserEntity;
import com.milanac007.demo.im.db.entity.msg.BuddyVerifyMessage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DataBaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = App.getContext().getPackageName() +  ".db";
	private static final int DATABASE_VERSION = 1;
	private Context context;
	private Map<String, Dao> daos = new HashMap<>();

	public static final int INSERT = 0;
	public static final int DELETE = 1;
	public static final int UPDATE = 2;


	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}


	@Override
	public void close() {
		super.close();
		daos.clear();
	}

	public synchronized Dao getDao(Class clazz) throws SQLException
	{
		Dao dao = null;
		String className = clazz.getSimpleName();

		if (daos.containsKey(className)) {
			dao = daos.get(className);
		}
		if (dao == null) {
			dao = super.getDao(clazz);
			daos.put(className, dao);
		}
		return dao;
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTableIfNotExists(connectionSource, User.class);
			TableUtils.createTableIfNotExists(connectionSource, UserEntity.class);
			TableUtils.createTableIfNotExists(connectionSource, GroupEntity.class);
			TableUtils.createTableIfNotExists(connectionSource, GroupMemberEntity.class);
			TableUtils.createTableIfNotExists(connectionSource, SessionEntity.class);
			TableUtils.createTableIfNotExists(connectionSource, MessageEntity.class);
			TableUtils.createTableIfNotExists(connectionSource, BuddyVerifyMessage.class);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
		try {
			//TODO 根据实际情况 删除需要更新的表
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, UserEntity.class, true);
			TableUtils.dropTable(connectionSource, GroupEntity.class, true);
			TableUtils.dropTable(connectionSource, GroupMemberEntity.class, true);
			TableUtils.dropTable(connectionSource, SessionEntity.class, true);
			TableUtils.dropTable(connectionSource, MessageEntity.class, true);
			TableUtils.dropTable(connectionSource, BuddyVerifyMessage.class, true);

			onCreate(sqLiteDatabase, connectionSource);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static DataBaseHelper instance = null;

	/**
	 * 单例获取该Helper
	 *
	 * @param context
	 * @return
	 */
	public static synchronized DataBaseHelper getHelper(Context context)
	{
		if (instance == null)
		{
			synchronized (DataBaseHelper.class)
			{
				if (instance == null)
					instance = new DataBaseHelper(context);
			}
		}

		return instance;
	}


}
